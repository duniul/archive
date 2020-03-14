package se.su.dsv.viking_prep_pvt_15_group9;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.model.Person;
import se.su.dsv.viking_prep_pvt_15_group9.util.SearchListAdapter;

/**
 * Created by Daniel on 2015-05-28.
 */
public class SearchPeopleFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private EditText searchEmailField;
    private EditText searchNameField;
    private EditText searchSurnameField;
    private Button searchEmailButton;
    private Button searchNameButton;
    private ListView foundPeopleListView;
    private List<Person> foundPeopleList = new ArrayList<Person>();
    private ProgressDialog progressDialog;
    private SearchListAdapter searchListAdapter;
    private SQLiteManager localDatabase;

    public static SearchPeopleFragment newInstanceOf() {
        SearchPeopleFragment fragment = new SearchPeopleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setTitle("Sök personer");

        localDatabase = new SQLiteManager(getActivity());
        setHasOptionsMenu(true);

        // Lägger till en progress dialog.
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_search_people, container, false);

        searchEmailField = (EditText) rootView.findViewById((R.id.field_search_email));
        searchNameField = (EditText) rootView.findViewById((R.id.field_search_name));
        searchSurnameField = (EditText) rootView.findViewById((R.id.field_search_surname));
        searchEmailButton = (Button) rootView.findViewById((R.id.button_search_email));
        searchNameButton = (Button) rootView.findViewById((R.id.button_search_name));

        foundPeopleListView = (ListView) rootView.findViewById(R.id.listview_found_people);
        searchListAdapter = new SearchListAdapter(getActivity(), foundPeopleList);
        foundPeopleListView.setAdapter(searchListAdapter);

        searchEmailButton.setOnClickListener(this);
        searchNameButton.setOnClickListener(this);
        foundPeopleListView.setOnItemClickListener(searchListAdapter);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        Context context = getActivity();
        int duration = Toast.LENGTH_LONG;

        if (view.getId() == R.id.button_search_email) {
            String searchedEmail = searchEmailField.getText().toString();
            foundPeopleList.clear();

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(searchedEmail).matches()) {
                Toast toastInvalidEmail = Toast.makeText(context, "Kontrollera e-postadressen.", duration);
                toastInvalidEmail.show();
            } else if (searchedEmail.equals(localDatabase.getEmail(1)))  {
                Toast toastInvalidEmail = Toast.makeText(context, "Du kan inte söka på din egen e-post.", duration);
                toastInvalidEmail.show();
            } else if (searchedEmail.trim().isEmpty()) {
                Toast toastInvalidEmail = Toast.makeText(context, "Tomt sökfält.", duration);
                toastInvalidEmail.show();
            } else {
                searchByEmail(searchedEmail);
            }

        } else {
            String searchedName = searchNameField.getText().toString();
            String searchedSurname = searchSurnameField.getText().toString();
            foundPeopleList.clear();

            if (searchedName.trim().isEmpty() && searchedSurname.trim().isEmpty()) {
                Toast toastInvalidEmail = Toast.makeText(context, "Tomma sökfält.", duration);
                toastInvalidEmail.show();
            }  else {
                searchByName(searchedName, searchedSurname);
            }
        }
    }

    private void searchByEmail(final String searchedEmail) {
        progressDialog.setMessage("Söker bland användare...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("SearchPeopleFragment", "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastSearchError = Toast.makeText(context, errorMsg, duration);
                        toastSearchError.show();

                    } else {

                        JSONObject person = jsonObject.getJSONObject("user");
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

                        Person foundPerson = new Person(userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl);
                        foundPeopleList.add(foundPerson);
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

                params.put("tag", "search_by_email");
                params.put("user_id", Integer.toString(localDatabase.getUserID(1)));
                params.put("searched_email", searchedEmail);

                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_search_by_email");
    }

    private void searchByName(final String searchedName, final String searchedSurname) {
        progressDialog.setMessage("Söker bland användare...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("SearchPeopleFragment", "Svar från databasen: " + response.toString());

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastSearchError = Toast.makeText(context, errorMsg, duration);
                        toastSearchError.show();

                    } else {
                        JSONObject people = jsonObject.getJSONObject("people");
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

                            // Kontrollerar så att den hittade personen inte är samma person som gjort sökningen
                            if (userID != localDatabase.getUserID(1)) {
                                foundPeopleList.add(new Person(userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl));
                            }
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

                params.put("tag", "search_by_name");
                params.put("user_id", Integer.toString(localDatabase.getUserID(1)));
                params.put("name", searchedName);
                params.put("surname", searchedSurname);

                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_search_by_name");
    }
}