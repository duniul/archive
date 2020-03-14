package se.su.dsv.viking_prep_pvt_15_group9;

/**
 * @author Daniel
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;

public class SelectedProfileFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private NetworkImageView profilePicture;
    private TextView nameText;
    private TextView ageAndLocationText;
    private Button addFriendButton;
    private Button sendMessageButton;

    private String name;
    private String age;
    private String location;
    private SQLiteManager localDatabase;
    private ProgressDialog progressDialog;

    public static SelectedProfileFragment newInstanceOf() {
        SelectedProfileFragment fragment = new SelectedProfileFragment();
        return fragment;
    }

    public SelectedProfileFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        localDatabase = mainActivity.getLocalDatabase();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        name = localDatabase.getFullName(2);
        age = localDatabase.getAge(2);
        location = localDatabase.getLocation(2);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_selected_profile, container, false);
        profilePicture = (NetworkImageView) rootView.findViewById(R.id.profile_picture);
        profilePicture.setImageUrl(localDatabase.getPictureUrl(2), AppRequestManager.getInstance().getImageLoader());

        nameText = (TextView) rootView.findViewById((R.id.text_name));
        ageAndLocationText = (TextView) rootView.findViewById((R.id.text_age_and_location));
        addFriendButton = (Button) rootView.findViewById(R.id.button_add_friend);
        addFriendButton.setOnClickListener(this);
        sendMessageButton = (Button) rootView.findViewById(R.id.button_send_message);
        sendMessageButton.setOnClickListener(this);

        checkIfFriend();

        nameText.setText(name);
        ageAndLocationText.setText(age + " år - " + location);

        return rootView;

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_send_message) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, MessagesFragment.newInstanceOf()).addToBackStack("")
                    .commit();
        } else if (v.getId() == R.id.button_add_friend) {
            String buttonText = addFriendButton.getText().toString();

            if (buttonText.equals("Lägg till som vän")) {
                addFriend();
                addFriendButton.setText("Ta bort som vän");
            } else {
                removeFriend();
                addFriendButton.setText("Lägg till som vän");
            }
        }

    }

    private void addFriend() {
        progressDialog.setMessage("Lägger till vän...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SelectedProfileFragment", "Svar från databasen: " + response.toString());
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg") + " " + jsonObject.getString("error_code");
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                        toastLoginError.show();

                    } else {
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastLoginError = Toast.makeText(context, "Vän tillagd!", duration);
                        toastLoginError.show();

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

                Log.e("SelectedProfileFragment", "Registration error: " + errorMsg);

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
                params.put("tag", "add_friend");
                params.put("user1_id", Integer.toString(localDatabase.getUserID(1)));
                params.put("user2_id", Integer.toString(localDatabase.getUserID(2)));

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_add_friend");;
    }

    private void removeFriend() {
        progressDialog.setMessage("Tar bort vän...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SelectedProfileFragment", "Svar från databasen: " + response.toString());
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg") + " " + jsonObject.getString("error_code");
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                        toastLoginError.show();

                    } else {
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastLoginError = Toast.makeText(context, "Vän borttagen!", duration);
                        toastLoginError.show();

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

                Log.e("SelectedProfileFragment", "Registration error: " + errorMsg);

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
                params.put("tag", "remove_friend");
                params.put("user1_id", Integer.toString(localDatabase.getUserID(1)));
                params.put("user2_id", Integer.toString(localDatabase.getUserID(2)));

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_remove_friend");;
    }

    private void checkIfFriend() {
        progressDialog.setMessage("Laddar profil...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SelectedProfileFragment", "Svar från databasen: " + response.toString());
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg") + " " + jsonObject.getString("error_code");
                        Log.d("SelectedProfileFragment", "errormsg");

                    } else {
                        boolean isFriend = jsonObject.getBoolean("is_friend");

                        if (isFriend) {
                            addFriendButton.setText("Ta bort som vän");
                        }

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

                Log.e("SelectedProfileFragment", "Registration error: " + errorMsg);

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
                params.put("tag", "check_if_friend");
                params.put("user1_id", Integer.toString(localDatabase.getUserID(1)));
                params.put("user2_id", Integer.toString(localDatabase.getUserID(2)));

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_check_if_friend");;
    }
}
