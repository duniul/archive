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
import android.widget.RadioButton;
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
 * Created by miaha_000 on 6/2/2015.
 */
public class MatchmakingDialog extends DialogFragment {

    private RadioButton exercise1;
    private RadioButton exercise2;
    private RadioButton exercise3;
    private EditText fromAgeInput;
    private EditText toAgeInput;
    private RadioButton manButton;
    private RadioButton womanButton;
    private RadioButton bothButton;
    private View dialogView;
    private int fromAge;
    private int toAge;
    private int numberOfExcercises = 0;
    private String preferedGender;
    AlertDialog matchmakingDialog;
    private ProgressDialog progressDialog;
    private SQLiteManager localDatabase;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        progressDialog = new ProgressDialog(getActivity());
        localDatabase = new SQLiteManager(getActivity());

        dialogView = inflater.inflate(R.layout.dialog_matchmaking, null);
        dialogBuilder.setView(dialogView);

        fromAgeInput = (EditText) dialogView.findViewById(R.id.field_min_age);
        toAgeInput = (EditText) dialogView.findViewById(R.id.field_max_age);

        manButton = (RadioButton) dialogView.findViewById(R.id.radio_button_gender_man);
        womanButton = (RadioButton)dialogView.findViewById(R.id.radio_button_gender_female);
        bothButton = (RadioButton)dialogView.findViewById(R.id.radio_button_gender_both);

        exercise1 = (RadioButton) dialogView.findViewById(R.id.r_button_excercise_one);
        exercise2 = (RadioButton) dialogView.findViewById(R.id.r_button_excercise_two);
        exercise3 = (RadioButton) dialogView.findViewById(R.id.r_button_excercise_three);

        if(localDatabase.isMatchmakingInfoStored()) {
            Map<String, String> storedInfo = localDatabase.getMatchmakingInfo();
            if(storedInfo.get("sessions_per_week").equals("1")) {
                exercise1.setChecked(true);
                exercise2.setChecked(false);
                exercise3.setChecked(false);
            } else if(storedInfo.get("sessions_per_week").equals("3")) {
                exercise1.setChecked(false);
                exercise2.setChecked(true);
                exercise3.setChecked(false);
            } else {
                exercise1.setChecked(false);
                exercise2.setChecked(false);
                exercise3.setChecked(true);
            }

            fromAgeInput.setText(storedInfo.get("pref_min_age"));
            toAgeInput.setText(storedInfo.get("pref_max_age"));

            if(storedInfo.get("pref_sex").equals("Man")) {
                manButton.setChecked(true);
                womanButton.setChecked(false);
                bothButton.setChecked(false);
            } else if(storedInfo.get("pref_sex").equals("Woman")) {
                manButton.setChecked(false);
                womanButton.setChecked(true);
                bothButton.setChecked(false);
            } else {
                manButton.setChecked(false);
                womanButton.setChecked(false);
                bothButton.setChecked(true);
            }
        }

        dialogBuilder
                .setPositiveButton("Bekräfta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MatchmakingDialog.this.getDialog().cancel();
                    }
                });

        return dialogBuilder.create();
    }

    public void onStart(){
        super.onStart();

        matchmakingDialog = (AlertDialog)getDialog();

        if(matchmakingDialog != null) {
            Button positiveButton = (Button) matchmakingDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlInput();
                }
            });
        }

    }

    public void controlInput(){

        String minAge = fromAgeInput.getText().toString();
        String maxAge = toAgeInput.getText().toString();

        if(minAge.isEmpty()){
            fromAgeInput.setError("Ange från vilket år");
            fromAgeInput.requestFocus();
        }else{
            try{
                fromAge = Integer.parseInt(minAge);

            }catch(NumberFormatException e){
                fromAgeInput.setError("Gick ej att parsa maxAge");
                fromAgeInput.requestFocus();

                Context context = getActivity();
                String errorMessage = "Gick ej att parsa minAge";
                Toast toastError = Toast.makeText(context, errorMessage, Toast.LENGTH_LONG);
                toastError.show();
            }
        }

        if(maxAge.isEmpty()){
            toAgeInput.setError("Ange till vilket år");
            toAgeInput.requestFocus();
        }else{
            try{
                toAge = Integer.parseInt(maxAge);
            }catch(NumberFormatException e){
                fromAgeInput.setError("Gick ej att parsa maxAge");
                fromAgeInput.requestFocus();

                Context context = getActivity();
                String errorMessage = "gick ej att parsa maxAge";
                Toast toastError = Toast.makeText(context, errorMessage, Toast.LENGTH_LONG);
                toastError.show();
            }
        }

        if(exercise1.isChecked()){
            numberOfExcercises = 1;
        }else if(exercise2.isChecked()){
            numberOfExcercises = 3;
        }else{
            numberOfExcercises = 5;
        }

        if (manButton.isChecked()){
            preferedGender = "Man";
        }else if(womanButton.isChecked()){
            preferedGender = "Woman";
        }else{
            preferedGender = "Both";
        }

        if (fromAge > toAge){
            fromAgeInput.setError("Från-ålder måste vara mindre än till-ålder");
            fromAgeInput.requestFocus();
        } else {
           storeMatchmakingInfo(numberOfExcercises, preferedGender, fromAge, toAge);
        }
    }

public void storeMatchmakingInfo(final int numberOfExcercises, final String preferedGender, final int fromAge, final int toAge){
    progressDialog.setMessage("Sparar...");
    progressDialog.show();

    Response.Listener<String> listener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            progressDialog.dismiss();
            Log.d("MatchmakinglDialog", "Svar från databasen: " + response.toString());

            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean error = jsonObject.getBoolean("error");

                if (error) {
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
                    String registerToastText = "Dina preferenser har sparats!";
                    Toast toastLoginError = Toast.makeText(context, registerToastText, duration);
                    toastLoginError.show();

                    JSONObject matchmakingInfo = jsonObject.getJSONObject("info");
                    int storedSessionsPerWeek = matchmakingInfo.getInt("sessions_per_week");
                    int storedMinAge = matchmakingInfo.getInt("pref_min_age");
                    int storedMaxAge = matchmakingInfo.getInt("pref_max_age");
                    String storedPrefSex = matchmakingInfo.getString("pref_sex");

                    if (localDatabase.isMatchmakingInfoStored()) {
                        localDatabase.deleteStoredMatchmakingInfo();
                    }
                    localDatabase.setMatchmakingInfo(storedSessionsPerWeek, storedPrefSex, storedMinAge, storedMaxAge);
                    matchmakingDialog.dismiss();
                }

            }catch(JSONException e){
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

            Log.e("SearchPeopleFragment", "Error: " + errorMsg);

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

            params.put("tag", "store_matchmaking_info");
            params.put("user_id", Integer.toString(localDatabase.getUserID(1)));
            params.put("sessions_per_week", Integer.toString(numberOfExcercises));
            params.put("pref_sex", preferedGender);
            params.put("pref_min_age", Integer.toString(fromAge));
            params.put("pref_max_age", Integer.toString(toAge));

            return params;
        }
    };

    AppRequestManager.getInstance().addToRequestQueue(strReq, "req_store_matchmaking_info");
}
}
