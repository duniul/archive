package se.su.dsv.viking_prep_pvt_15_group9;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SessionManager;
import se.su.dsv.viking_prep_pvt_15_group9.util.DatePickerDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {

	// LogCat-tagg
    private static final String LOGTAG = RegisterActivity.class.getSimpleName();

    // Alla views som hanteras i koden.
    private TextView titleRegister;
    private EditText fieldName;
    private EditText fieldSurname;
    private EditText fieldEmail;
    private EditText fieldPassword;
    private EditText fieldConfirmPassword;
    private EditText fieldArea;
    private EditText fieldCity;
    private EditText fieldDateOfBirth;
    private RadioButton raButtonMale;
    private RadioButton raButtonFemale;
    private ProgressDialog progressDialog;

    // Objekt från helper-klasserna.
    private SessionManager session;
    private SQLiteManager localDatabase;
    private DatePickerDialog datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        titleRegister = (TextView) findViewById(R.id.registerTitle);
        fieldName = (EditText) findViewById(R.id.nameField);
        fieldSurname = (EditText) findViewById(R.id.surnameField);
        fieldEmail = (EditText) findViewById(R.id.emailField);
        fieldPassword = (EditText) findViewById(R.id.passwordField);
        fieldConfirmPassword = (EditText) findViewById(R.id.confirmPasswordField);
        fieldArea = (EditText) findViewById(R.id.areaField);
        fieldCity = (EditText) findViewById(R.id.cityField);
        fieldDateOfBirth = (EditText) findViewById(R.id.dateOfBirthField);
        raButtonMale = (RadioButton) findViewById(R.id.maleRadioButton);
        raButtonFemale = (RadioButton) findViewById(R.id.femaleRadioButton);

        // Lägger in Norse-fonten och sätter den på rubriken.
        Typeface typeFaceNorse = Typeface.createFromAsset(getAssets(), "Norse-Bold.ttf");
        titleRegister.setTypeface(typeFaceNorse);

        // Ändrar fonten på lösenords-hinten.
        fieldPassword.setTypeface(Typeface.DEFAULT);
        fieldPassword.setTransformationMethod(new PasswordTransformationMethod());
        fieldConfirmPassword.setTypeface(Typeface.DEFAULT);
        fieldConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());

        // Lägger till en progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Lägger till en session manager.
        session = new SessionManager(getApplicationContext());

        // Lägger till en databashanterare.
        localDatabase = new SQLiteManager(getApplicationContext());

        // Lägger till en datepicker.
        datePicker = new DatePickerDialog();

        // Kontrollerar om användaren är inloggad sedan tidigare eller inte.
        // Är man det flyttas man direkt till startsidan.
        if (session.isLoggedIn()) {
            Intent toMain = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(toMain);
            finish();
        }
    }

    public void onRegisterClick(View view) {

        // Tar alla värden från fälten och omvandlar till strängar.
        int validInputs = 0;
        String name = fieldName.getText().toString().trim();
        String surname = fieldSurname.getText().toString().trim();
        String email = fieldEmail.getText().toString().trim();
        String password = fieldPassword.getText().toString().trim();
        String confirmedPassword = fieldConfirmPassword.getText().toString().trim();
        String area = fieldArea.getText().toString().trim();
        String city = fieldCity.getText().toString().trim();
        String dateOfBirth = fieldDateOfBirth.getText().toString().trim();
        String sex = "";

        // Omvandlar valet av kön till en string
        if (raButtonMale.isChecked()) {
            sex = "Man";
        } else if (raButtonFemale.isChecked()) {
            sex = "Woman";
        }

        // Kontroll av förnamn
        if (name.isEmpty()) {
            fieldName.setError("Vänligen ange ett namn.");
        } else {
            validInputs++;
        }

        // Kontroll av efternamn
        if (surname.isEmpty()) {
            fieldSurname.setError("Vänligen ange ett efternamn.");
        } else {
            validInputs++;
        }

        // Kontroll av e-postadress
        if (email.isEmpty()) {
            fieldEmail.setError("Vänligen ange en e-postadress.");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            fieldEmail.setError("Ogiltig e-postadress.");
        } else {
            validInputs++;
        }

        // Kontroll av lösenord
        if (password.isEmpty()) {
            fieldPassword.setError("Vänligen ange ett lösenord.");
        } else if (!password.equals(confirmedPassword)) {
            fieldConfirmPassword.setError("Lösenorden är inte likadana.");
        } else {
            validInputs++;
        }

        // Kontroll av ort
        if (area.isEmpty()) {
            fieldArea.setError("Vänligen ange en ort.");
        } else {
            validInputs++;
        }

        // Kontroll av stad
        if (city.isEmpty()) {
            fieldCity.setError("Vänligen ange en stad.");
        } else {
            validInputs++;
        }

        // Kontroll av födelsedatum
        if (dateOfBirth.isEmpty()) {
            fieldDateOfBirth.setError("Vänligen ange ett födelsedatum.");
        } else {
            validInputs++;
        }

        // Om alla 5 kontroller stämmer så körs metoden registerUser.
        if (validInputs == 7) {
            registerUser(name, surname, email, password, area, city, dateOfBirth, sex);

        } else {
            validInputs = 0;
        }
    }

    /**
     * Gör en request med hjälp av volley och kopplas upp mot PHP-api:n. Detaljerna sparas på
     * en MySQL-databas på DSV:s people-server.
     * @param name förnamnet användaren fyllde i.
     * @param surname efternamnet användaren fyllde i.
     * @param email e-postadressen användaren fyllde i.
     * @param password lösenordet användaren fyllde i.
     * @param area orten användaren fyllde i.
     * @param city staden användaren fyllde i.
     * @param dateOfBirth födelsedatumet användaren fyllde i.
     * @param sex könet användaren fyllde i.
     */
    private void registerUser(final String name, final String surname, final String email, final String password, final String area, final String city, final String dateOfBirth, final String sex) {
        progressDialog.setMessage("Skapar ditt konto...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOGTAG, "Svar från databasen: " + response.toString());
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        if (errorCode == 41) {
                            fieldEmail.setError(errorMsg);
                        } else {
                            Context context = getApplicationContext();
                            int duration = Toast.LENGTH_LONG;
                            Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                            toastLoginError.show();
                        }
                    } else {
                        // Visar en toast som bekräftar att kontot skapats.
                        Context context = getApplicationContext();
                        int duration = Toast.LENGTH_LONG;
                        String registerToastText = "Konto skapat!";
                        Toast toastLoginError = Toast.makeText(context, registerToastText, duration);
                        toastLoginError.show();

                        JSONObject user = jsonObject.getJSONObject("user");
                        int storedUserID = user.getInt("user_id");
                        String storedUniqueID = user.getString("unique_id");
                        String storedName = user.getString("name");
                        String storedSurname = user.getString("surname");
                        String storedEmail = user.getString("email");
                        String storedArea = user.getString("area");
                        String storedCity = user.getString("city");
                        String storedDateOfBirth = user.getString("date_of_birth");
                        String storedSex = user.getString("sex");
                        String storedCreatedAt = user.getString("created_at");
                        String storedUpdatedAt = user.getString("updated_at");
                        String pictureUrl = user.getString("picture_url");;

                        // Tar bort eventuell existerande användare och lägger in användaruppgifter i lokal databas (SQLite)
                        if (localDatabase.isUserStored(1)) {
                            localDatabase.deleteUser(1);
                        }
                        localDatabase.addUser(1, storedUserID, storedUniqueID, storedName, storedSurname, storedEmail, storedArea, storedCity, storedDateOfBirth, storedSex, storedCreatedAt, storedUpdatedAt, pictureUrl);
                        if(!pictureUrl.equals("none")) {
                            localDatabase.setPictureUrl(1, pictureUrl);
                        }

                        // Går tillbaka till login-aktiviteten.
                        Intent toLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(toLogin);
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                String errorMsg = error.getMessage();
                int duration = Toast.LENGTH_LONG;

                Log.e(LOGTAG, "Registration error: " + errorMsg);

                Toast toastRegistrationError = Toast.makeText(context, errorMsg, duration);
                toastRegistrationError.show();
                progressDialog.dismiss();
            }
        };

        // Skickar en StringRequest som via PHP kopplas till MySQL-databasen online.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DB_API_URL, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "register");
                params.put("name", name);
                params.put("surname", surname);
                params.put("email", email);
                params.put("password", password);
                params.put("area", area);
                params.put("city", city);
                params.put("date_of_birth", dateOfBirth);
                params.put("sex", sex);

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_register");;
    }

    public void onDatePickerClick(View view) {
        datePicker.setDateField(fieldDateOfBirth);
        datePicker.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}