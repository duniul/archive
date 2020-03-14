package se.su.dsv.viking_prep_pvt_15_group9;

/**
 * Created by Barre on 2015-05-04.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Button;
import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.model.Person;
import se.su.dsv.viking_prep_pvt_15_group9.util.SearchListAdapter;


public class FriendsFragment extends Fragment implements View.OnClickListener {

    private ListView friendsListView;
    private List<Person> friendsList = new ArrayList<Person>();
    private SearchListAdapter searchListAdapter;
    private SQLiteManager localDatabase;
    private EditText searchField;
    private Button searchButton;
    private View rootView;
    private ProgressDialog progressDialog;
    private int userId;
    private String currentUserId;

    public static FriendsFragment newInstanceOf(){
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localDatabase = new SQLiteManager(getActivity());
        setHasOptionsMenu(true);

        userId = localDatabase.getUserID(1);
        currentUserId = Integer.toString(userId);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        searchField = (EditText) rootView.findViewById(R.id.field_search);
        searchButton = (Button) rootView.findViewById(R.id.button_search);
        searchButton.setOnClickListener(this);
        friendsListView = (ListView)rootView.findViewById(R.id.friends_list);
        searchListAdapter = new SearchListAdapter(getActivity(), friendsList);
        friendsListView.setAdapter(searchListAdapter);

        friendsListView.setOnItemClickListener(searchListAdapter);

        if (friendsList.isEmpty()) {
            showFriends();
        }

        return rootView;

    }

    @Override
    public void onClick(View v) {

        String searchString = searchField.getText().toString().trim().toLowerCase();

        if (searchString.isEmpty()) {
            searchListAdapter.setPeopleList(friendsList);
            searchListAdapter.notifyDataSetChanged();

        } else {
            List<Person> filteredList = new ArrayList<Person>();

            for (Person friend : friendsList) {
                if (friend.getFullName().toLowerCase().contains(searchString)) {
                    filteredList.add(friend);
                }
            }

            searchListAdapter.setPeopleList(filteredList);
            searchListAdapter.notifyDataSetChanged();
        }
    }

    private void showFriends() {
        progressDialog.setMessage("Söker bland användare...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("SearchPeopleFragment", "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject people = jsonObject.getJSONObject("people");
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        if (errorCode != 101) {
                            Context context = getActivity();
                            int duration = Toast.LENGTH_LONG;
                            Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                            toastLoginError.show();
                        }

                    } else {

                        int numberOfPeople = jsonObject.getInt("number_of_people");

                        for (int i = 1; i <= numberOfPeople; i++) {
                            JSONObject person = people.getJSONObject(Integer.toString(i));
                            int userID = person.getInt("user_id");
                            String uniqueID = person.getString("unique_id");
                            String name = person.getString("name");
                            String surname = person.getString("surname");
                            String email = person.getString("email");
                            String area = person.getString("area");
                            String city = person.getString("city");
                            String dateOfBirth = person.getString("date_of_birth");
                            String sex = person.getString("sex");
                            String createdAt = person.getString("created_at");
                            String updatedAt = person.getString("updated_at");
                            String pictureUrl = person.getString("picture_url");

                            friendsList.add(new Person(userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl));
                        }
                    }

                    searchListAdapter.notifyDataSetChanged();

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

                params.put("tag", "load_friends");
                params.put("user_id", Integer.toString(localDatabase.getUserID(1)));


                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_load_friends");
    }

}
