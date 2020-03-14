package se.su.dsv.viking_prep_pvt_15_group9;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.util.ChangeEmailDialog;
import se.su.dsv.viking_prep_pvt_15_group9.util.ChangePasswordDialog;
import se.su.dsv.viking_prep_pvt_15_group9.util.DatePickerDialog;
import se.su.dsv.viking_prep_pvt_15_group9.util.PhotoDialog;

/**
 * @author Daniel
 */
public class EditProfileFragment extends Fragment implements View.OnClickListener, PhotoDialog.PhotoDialogListener {

    private View rootView;

    private Button editPictureButton;
    private TextView userEmailText;
    private Button changeEmailButton;
    private Button changePasswordButton;
    private Button saveEditsButton;
    private EditText nameField;
    private EditText surnameField;
    private EditText areaField;
    private EditText cityField;
    private EditText dateOfBirthField;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private NetworkImageView profileImage;

    private ProgressDialog progressDialog;
    private SQLiteManager localDatabase;
    private DatePickerDialog datePicker;
    private PhotoDialog photoDialog;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private final int PIC_CROP = 3;

    public static EditProfileFragment newInstanceOf() {
        EditProfileFragment fragment = new EditProfileFragment();
        return fragment;
    }

    public EditProfileFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lägger till en databashanterare.
        localDatabase = new SQLiteManager(getActivity());

        // Lägger till en datepicker.
        datePicker = new DatePickerDialog();

        // Lägger till en progress dialog.
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        photoDialog = new PhotoDialog();

        ((MainActivity) getActivity()).setActionBarTitle("Redigera profil");

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        HashMap<String, String> userDetails = localDatabase.getUserDetails(1);
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editPictureButton = (Button) rootView.findViewById(R.id.button_edit_picture);
        userEmailText = (TextView) rootView.findViewById(R.id.text_user_email);
        changeEmailButton = (Button) rootView.findViewById(R.id.button_change_email);
        changePasswordButton = (Button) rootView.findViewById(R.id.button_change_password);
        saveEditsButton = (Button) rootView.findViewById(R.id.button_save_edits);
        nameField = (EditText) rootView.findViewById(R.id.field_name);
        surnameField = (EditText) rootView.findViewById(R.id.field_surname);
        areaField = (EditText) rootView.findViewById(R.id.field_area);
        cityField = (EditText) rootView.findViewById(R.id.field_city);
        dateOfBirthField = (EditText) rootView.findViewById(R.id.field_date_of_birth);
        maleRadioButton = (RadioButton) rootView.findViewById(R.id.radio_button_male);
        femaleRadioButton = (RadioButton) rootView.findViewById(R.id.radio_button_female);
        profileImage = (NetworkImageView) rootView.findViewById(R.id.profile_picture);
        profileImage.setImageUrl(localDatabase.getPictureUrl(1), AppRequestManager.getInstance().getImageLoader());

        editPictureButton.setOnClickListener(this);
        changeEmailButton.setOnClickListener(this);
        changePasswordButton.setOnClickListener(this);
        saveEditsButton.setOnClickListener(this);
        dateOfBirthField.setOnClickListener(this);

        userEmailText.setText(userDetails.get("email"));
        nameField.setText(userDetails.get("name"));
        surnameField.setText(userDetails.get("surname"));
        areaField.setText(userDetails.get("area"));
        cityField.setText(userDetails.get("city"));
        dateOfBirthField.setText(userDetails.get("date_of_birth"));
        if (userDetails.get("sex").equals("Man")) {
            maleRadioButton.setChecked(true);
        } else {
            femaleRadioButton.setChecked(true);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_profile, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save_changes:
                String email = userEmailText.getText().toString();
                String name = nameField.getText().toString();
                String surname = surnameField.getText().toString();
                String area = areaField.getText().toString();
                String city = cityField.getText().toString();
                String dateOfBirth = dateOfBirthField.getText().toString();
                String sex;
                if (maleRadioButton.isChecked()) {
                    sex = "Man";
                } else {
                    sex = "Woman";
                }

                progressDialog.setMessage("Sparar dina ändringar...");
                editProfileDetails(email, name, surname, area, city, dateOfBirth, sex);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.button_edit_picture:
                photoDialog.setTargetFragment(this, 1);
                photoDialog.show(getFragmentManager(), "photoDialog");
                break;
            case R.id.button_change_email:
                ChangeEmailDialog changeEmailDialog = new ChangeEmailDialog();
                changeEmailDialog.setEmailText(userEmailText);
                changeEmailDialog.show(getFragmentManager(), "changeEmailDialog");
                break;
            case R.id.button_change_password:
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
                changePasswordDialog.show(getFragmentManager(), "changePasswordDialog");
                break;
            case R.id.field_date_of_birth:
                datePicker.setDateField(dateOfBirthField);
                datePicker.show(getFragmentManager(), "datePicker");
                break;
            case R.id.button_save_edits:
                String email = userEmailText.getText().toString();
                String name = nameField.getText().toString();
                String surname = surnameField.getText().toString();
                String area = areaField.getText().toString();
                String city = cityField.getText().toString();
                String dateOfBirth = dateOfBirthField.getText().toString();
                String sex;
                if (maleRadioButton.isChecked()) {
                    sex = "Man";
                } else {
                    sex = "Woman";
                }

                progressDialog.setMessage("Sparar dina ändringar...");
                editProfileDetails(email, name, surname, area, city, dateOfBirth, sex);
            default:
                break;
        }
    }

    @Override
    public void onPhotoDialogConfirmClicked(Bitmap image) {
        progressDialog.setMessage("Sparar profilbild...");
        progressDialog.show();
        profileImage.setImageBitmap(image);
        uploadImage(image);
    }

    private void uploadImage(Bitmap image) {

        // Convert it to byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Compress image to lower quality scale 1 - 100 and change to JPEG
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] imageByteArray = stream.toByteArray();

        // Create the ParseFile
        final ParseFile imageFile = new ParseFile("toughviking.jpeg", imageByteArray);

        // Create a New Class called "ImageUpload" in Parse
        final ParseObject imageUpload = new ParseObject("ImageUpload");

        // Create a column named "ImageName" and set the string
        imageUpload.put("ImageName", "Tough Viking");

        // Create a column named "ImageFile" and insert the image
        imageUpload.put("ImageFile", imageFile);

        // Överskuggar saveInBackground för att direkt kunna spara url:en.
        imageFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                savePictureUrlToDatabase(imageFile.getUrl());
            }
        });

        // Create the class and the columns
        imageUpload.saveInBackground();
    }

    public void savePictureUrlToDatabase(final String imageUrl) {

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MessagesFragment", "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        progressDialog.dismiss();
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        if (errorCode != 141) {
                            Context context = getActivity();
                            int duration = Toast.LENGTH_LONG;
                            Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                            toastLoginError.show();
                        }

                    } else {
                        progressDialog.dismiss();
                        localDatabase.setPictureUrl(1, imageUrl);
                        Log.w("EditProfileFragment", "Profilbildslänk " + imageUrl + " har sparats.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Context context = getActivity();
                String errorMsg = error.getMessage();
                int duration = Toast.LENGTH_LONG;

                Log.e("MessagesFragment", "Registration error: " + errorMsg);

                Toast toastRegistrationError = Toast.makeText(context, errorMsg, duration);
                toastRegistrationError.show();
            }
        };

        // Skickar en StringRequest som via PHP kopplas till MySQL-databasen online.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DB_API_URL, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "store_picture_url");
                params.put("user_id", Integer.toString(localDatabase.getUserID(1)));
                params.put("picture_url", imageUrl);

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_store_picture_url");
    }

    private void editProfileDetails(final String email, final String name, final String surname, final String area, final String city, final String dateOfBirth, final String sex) {
        progressDialog.setMessage("Sparar dina ändringar...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("EditProfileFragment", "Svar från databasen: " + response.toString());
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                        toastLoginError.show();
                    } else {
                        // Visar en toast som bekräftar att kontot skapats.
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        String registerToastText = "Din profil har uppdaterats!";
                        Toast toastLoginError = Toast.makeText(context, registerToastText, duration);
                        toastLoginError.show();

                        JSONObject user = jsonObject.getJSONObject("user");
                        String storedEmail = user.getString("email");
                        String storedName = user.getString("name");
                        String storedSurname = user.getString("surname");
                        String storedArea = user.getString("area");
                        String storedCity = user.getString("city");
                        String storedDateOfBirth = user.getString("date_of_birth");
                        String storedSex = user.getString("sex");
                        String storedUpdatedAt = user.getString("updated_at");

                        localDatabase.updateUserDetails(1, storedEmail, storedName, storedSurname, storedArea, storedCity, storedDateOfBirth, storedSex, storedUpdatedAt);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getActivity();
                String errorMsg = error.getMessage();
                int duration = Toast.LENGTH_LONG;

                Log.e("EditProfileFragment", "Registration error: " + errorMsg);

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
                params.put("tag", "edit_user_details");
                params.put("email", email);
                params.put("name", name);
                params.put("surname", surname);
                params.put("area", area);
                params.put("city", city);
                params.put("date_of_birth", dateOfBirth);
                params.put("sex", sex);

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_edit_user_details");;
    }


}