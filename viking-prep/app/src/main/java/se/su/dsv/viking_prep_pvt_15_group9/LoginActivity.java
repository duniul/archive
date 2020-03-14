package se.su.dsv.viking_prep_pvt_15_group9;

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

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SessionManager;

/**
 * Den här aktiviteten är det första användarens stöter på innan den ska logga in
 * eller registrera sig.
 *
 * @author Daniel
 */

public class LoginActivity extends Activity {

    // LogCat-tagg
    private static final String LOGTAG = LoginActivity.class.getSimpleName();

    // Alla views som hanteras i koden.
    private EditText fieldEmail;
    private EditText fieldPassword;
    private ProgressDialog progressDialog;

    // Objekt från helper-klasserna.
    private SQLiteManager localDatabase;
    private SessionManager session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView textTitle = (TextView)findViewById(R.id.titleText);
        fieldEmail = (EditText)findViewById(R.id.emailField);
        fieldPassword = (EditText)findViewById(R.id.passwordField);

        // Lägger in Norse-fonten och sätter den på rubriken.
        Typeface typeFaceNorse = Typeface.createFromAsset(getAssets(),"Norse-Bold.ttf");
        textTitle.setTypeface(typeFaceNorse);

        // Ändrar fonten på lösenords-hinten.
        fieldPassword.setTypeface(Typeface.DEFAULT);
        fieldPassword.setTransformationMethod(new PasswordTransformationMethod());

        // Lägger till en progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Lägger till en databashanterare.
        localDatabase = new SQLiteManager(getApplicationContext());

        if (localDatabase.isUserStored(1)) {
            fieldEmail.setText(localDatabase.getEmail(1));
            fieldPassword.requestFocus();
        }

        // Lägger till en session manager.
        session = new SessionManager(getApplicationContext());

        // Kontrollerar om användaren är inloggad sedan tidigare eller inte.
        // Är man det flyttas man direkt till startsidan.
        if (session.isLoggedIn()) {
            Intent toMain = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(toMain);
            finish();
        }

    }

    // Den här metoden hanterar vad som händer när användaren klickar "Logga in".
    public void onLoginClick(View v) {
        Boolean emailIsValid = false;
        Boolean passwordIsValid = false;
        String email = fieldEmail.getText().toString();
        String password = fieldPassword.getText().toString();

        if (email.isEmpty()) {
            fieldEmail.setError("E-postadress saknas.");
            fieldEmail.requestFocus();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            fieldEmail.setError("Ogiltig e-postadress.");
            fieldEmail.requestFocus();
        }
        else {
            emailIsValid = true;
        }

        if (password.isEmpty()) {
            fieldPassword.setError("Lösenord saknas.");
        } else {
            passwordIsValid = true;
        }

        if (emailIsValid && passwordIsValid) {
            loginUser(email, password);
        } else {
            emailIsValid = false;
            passwordIsValid = false;
        }
    }

    private void loginUser(final String userEmail, final String userPassword) {
        progressDialog.setMessage("Loggar in...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d(LOGTAG, "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        if (errorCode == 31) {
                            fieldEmail.setError(errorMsg);
                        } else if (errorCode == 32) {
                            fieldPassword.setError(errorMsg);
                        } else {
                            Context context = getApplicationContext();
                            int duration = Toast.LENGTH_LONG;
                            Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                            toastLoginError.show();
                        }

                    } else {
                        JSONObject user = jsonObject.getJSONObject("user");
                        int userID = user.getInt("user_id");
                        String uniqueID = user.getString("unique_id");
                        String name = user.getString("name");
                        String surname = user.getString("surname");
                        String email = user.getString("email");
                        String area = user.getString("area");
                        String city = user.getString("city");
                        String dateOfBirth = user.getString("date_of_birth");
                        String sex = user.getString("sex");
                        String createdAt = user.getString("created_at");
                        String updatedAt = user.getString("updated_at");
                        String pictureUrl = user.getString("picture_url");;

                        // Tar bort eventuell existerande användare och lägger in användaruppgifter i lokal databas (SQLite)
                        if (localDatabase.isUserStored(1)) {
                            localDatabase.deleteUser(1);
                        }
                        localDatabase.addUser(1, userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl);

                        session.setLoggedIn(true);

                        Intent toMain = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(toMain);
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

                Log.e(LOGTAG, "Login error: " + errorMsg);

                Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                toastLoginError.show();
                progressDialog.dismiss();
            }
        };

        StringRequest strReq = new StringRequest(Request.Method.POST,
            AppConfig.DB_API_URL, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", userEmail);
                params.put("password", userPassword);

                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_login");
    }

    // Den metoden hanterar vad som händer när användaren klickar "Skapa konto".
    public void onRegisterClick(View view) {
        Intent register = new Intent(this, RegisterActivity.class);
        startActivity(register);
        finish();
    }

}