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
public class ChangePasswordDialog extends DialogFragment {

    AlertDialog passwordDialog;
    private View dialogView;
    private EditText newPasswordField;
    private EditText newPasswordConfirmField;
    private EditText oldPasswordField;

    private ProgressDialog progressDialog;
    private SQLiteManager localDatabase;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        dialogBuilder.setView(dialogView);

        newPasswordField = (EditText) dialogView.findViewById(R.id.field_new_password);
        newPasswordConfirmField = (EditText) dialogView.findViewById(R.id.field_new_password_confirm);
        oldPasswordField = (EditText) dialogView.findViewById(R.id.field_old_password);

        dialogBuilder
                .setPositiveButton("Bekräfta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Överskuggad i onStart() för att förhindra användaren från att trycka ner dialogen om lösenordet är fel.
                        }
                    })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChangePasswordDialog.this.getDialog().cancel();
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

        Boolean inputIsValid = true;
        String newPassword = newPasswordField.getText().toString();
        String newPasswordConfirm = newPasswordConfirmField.getText().toString();
        String oldPassword = oldPasswordField.getText().toString();

        newPasswordField.setError(null);
        newPasswordConfirmField.setError(null);
        oldPasswordField.setError(null);

        // Kontroll av lösenord
        if (newPassword.isEmpty()) {
            newPasswordField.setError("Vänligen ange ett lösenord.");
            inputIsValid = false;
        }
        if (oldPassword.isEmpty()) {
            oldPasswordField.setError("Fel lösenord!");
            inputIsValid = false;
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            newPasswordConfirmField.setError("Lösenorden är inte likadana.");
            inputIsValid = false;
        }
        if (newPassword.equals(oldPassword) && newPasswordConfirm.equals(oldPassword)) {
            newPasswordField.setError("Lösenordet är samma som det gamla.");
            inputIsValid = false;
        }

        if (inputIsValid) {
            confirmAndChangePassword(newPassword, oldPassword);
    }
    }

    private void confirmAndChangePassword (final String newPassword, final String oldPassword) {

        progressDialog.setMessage("Ändrar ditt lösenord...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("ChangePasswordDialog", "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        if (errorCode == 32) {
                            oldPasswordField.setError(errorMsg);
                        }  else {
                            Context context = getActivity();
                            int duration = Toast.LENGTH_LONG;
                            Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                            toastLoginError.show();
                        }

                    } else {
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        String changePasswordToastText = "Lösenord ändrat!";
                        Toast toastLoginError = Toast.makeText(context, changePasswordToastText, duration);
                        toastLoginError.show();

                        JSONObject user = jsonObject.getJSONObject("user");
                        String email = user.getString("email");
                        String updatedAt = user.getString("updated_at");

                        localDatabase.setUpdatedAt(1, email, updatedAt);
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

                Log.e("ChangePasswordDialog", "Login error: " + errorMsg);

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
                params.put("tag", "change_password");
                params.put("email", localDatabase.getEmail(1));
                params.put("new_password", newPassword);
                params.put("old_password", oldPassword);

                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_change_password");
    }
}