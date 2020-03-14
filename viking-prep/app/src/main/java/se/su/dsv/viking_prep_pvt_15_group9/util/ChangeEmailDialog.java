package se.su.dsv.viking_prep_pvt_15_group9.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import se.su.dsv.viking_prep_pvt_15_group9.R;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;

/**
 * Created by Daniel on 2015-05-27.
 */
public class ChangeEmailDialog extends DialogFragment {

    private AlertDialog passwordDialog;
    private View dialogView;
    private EditText newEmailField;
    private EditText passwordField;
    private TextView editProfileEmailText;

    private ProgressDialog progressDialog;
    private SQLiteManager localDatabase;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.dialog_change_email, null);
        dialogBuilder.setView(dialogView);

        newEmailField = (EditText) dialogView.findViewById(R.id.field_new_email);
        passwordField = (EditText) dialogView.findViewById(R.id.field_password);

        dialogBuilder
                .setPositiveButton("Bekräfta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Överskuggad i onStart() för att förhindra användaren från att trycka ner dialogen om lösenordet är fel.
                    }
                })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChangeEmailDialog.this.getDialog().cancel();
                    }
                });

        // Lägger till en databashanterare.
        localDatabase = new SQLiteManager(getActivity());

        // Lägger till en progress dialog.
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        return dialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        passwordDialog = (AlertDialog)getDialog();

        if(passwordDialog != null) {
            Button positiveButton = (Button) passwordDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlInput();
                }
            });
        }
    }

    private void controlInput() {

        Boolean emailIsValid = false;
        Boolean passwordIsValid = false;
        String newEmail = newEmailField.getText().toString();
        String password = passwordField.getText().toString();

        newEmailField.setError(null);
        passwordField.setError(null);

        // Kontroll av email
        if (newEmail.isEmpty()) {
            newEmailField.setError("E-postadress saknas.");
            newEmailField.requestFocus();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            newEmailField.setError("Ogiltig e-postadress.");
            newEmailField.requestFocus();
        } else if (newEmail.equals(localDatabase.getEmail(1))) {
            newEmailField.setError("E-postadressen är samma som den existerande.");
            newEmailField.requestFocus();
        } else {
            emailIsValid = true;
        }

        // Kontroll av lösenord
        if (password.isEmpty()) {
            passwordField.setError("Lösenord saknas.");
        } else {
            passwordIsValid = true;
        }

        if (emailIsValid && passwordIsValid) {
            confirmAndChangeEmail(newEmail, password);
        }
    }

    private void confirmAndChangeEmail(final String newEmail, final String password) {

        progressDialog.setMessage("Byter din e-postadress...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("ChangeEmailDialog", "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        if (errorCode == 32) {
                            passwordField.setError(errorMsg);
                        } else if (errorCode == 41) {
                            newEmailField.setError(errorMsg);
                        } else {
                            Context context = getActivity();
                            int duration = Toast.LENGTH_LONG;
                            Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                            toastLoginError.show();
                        }

                    } else {
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        String changePasswordToastText = "E-postadress ändrad!";
                        Toast toastLoginError = Toast.makeText(context, changePasswordToastText, duration);
                        toastLoginError.show();

                        JSONObject user = jsonObject.getJSONObject("user");
                        String email = user.getString("email");
                        String updatedAt = user.getString("updated_at");

                        localDatabase.setEmail(1, email, updatedAt);
                        editProfileEmailText.setText(email);
                        passwordDialog.dismiss();
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

                Log.e("ChangeEmailDialog", "Login error: " + errorMsg);

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
                params.put("tag", "change_email");
                params.put("unique_id", localDatabase.getUniqueID(1));
                params.put("old_email", localDatabase.getEmail(1));
                params.put("new_email", newEmail);
                params.put("password", password);

                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_change_email");
    }

    public void setEmailText(TextView textView) {
        editProfileEmailText = textView;
    }
}
