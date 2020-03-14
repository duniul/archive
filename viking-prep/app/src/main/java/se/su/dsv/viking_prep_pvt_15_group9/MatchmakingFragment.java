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
import se.su.dsv.viking_prep_pvt_15_group9.util.MatchmakingDialog;
import se.su.dsv.viking_prep_pvt_15_group9.util.SearchListAdapter;

/**
 * Created by miaha_000 on 6/1/2015.
 */
public class MatchmakingFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    private ProgressDialog progressDialog;
    private SQLiteManager localDatabase;

    private EditText areaField;
    private EditText cityField;
    private Button searchButton;
    private Button filterButton;

    private String inputArea;
    private String inputCity;

    private ListView peopleListView;

    private List<Person> matchingPeopleList = new ArrayList<Person>();
    private Map<String, String> matchmakingInfo;
    private SearchListAdapter searchListAdapter;
    private int isMatchable;
    private int numberOfExercises;
    private int minAge;
    private int maxAge;
    private String preferedSex;


    public static MatchmakingFragment newInstanceOf() {
        MatchmakingFragment fragment = new MatchmakingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localDatabase = new SQLiteManager(getActivity());
        setHasOptionsMenu(true);

        // Lägger till en progress dialog.
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_matchmaking, container, false);

        searchButton = (Button) rootView.findViewById(R.id.button_search);
        filterButton = (Button) rootView.findViewById(R.id.button_filter);
        peopleListView = (ListView) rootView.findViewById(R.id.listview_found_people);

        areaField = (EditText) rootView.findViewById(R.id.field_search_area);
        cityField = (EditText) rootView.findViewById(R.id.field_search_city);


        searchButton.setOnClickListener(this);
        filterButton.setOnClickListener(this);

        searchListAdapter = new SearchListAdapter(getActivity(), matchingPeopleList);
        peopleListView.setOnItemClickListener(searchListAdapter);
        peopleListView.setAdapter(searchListAdapter);

        return rootView;
    }


    @Override
    public void onClick(View v) {
        Context context = getActivity();
        int duration = Toast.LENGTH_LONG;
        if (v.getId() == R.id.button_filter) {
            MatchmakingDialog matchmakingDialog = new MatchmakingDialog();
            matchmakingDialog.show(getFragmentManager(), "matchmakingDialog");

        } else if (v.getId() == R.id.button_search) {

            inputArea = areaField.getText().toString();
            inputCity = cityField.getText().toString();

            if (inputArea.trim().isEmpty()) {
                areaField.setError("Ange ort.");
            } else if (inputCity.trim().isEmpty()) {
                areaField.setError("Ange stad.");
            } else if (!localDatabase.isMatchmakingInfoStored()) {
                String errorMessage = "Vänligen fyll i dina preferenser först!";
                Toast formToast = Toast.makeText(context, errorMessage, duration);

                formToast.show();
                MatchmakingDialog matchmakingDialog = new MatchmakingDialog();
                matchmakingDialog.show(getFragmentManager(), "matchmakingDialog");


            } else {
                findMatches(inputArea, inputCity);
            }
        }
    }


    private void findMatches(final String inputArea, final String inputCity) {
        progressDialog.setMessage("Hittar matchande personer...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("MatchmakingFragment", "Svar från databasen: " + response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (error) {
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

                            if (userID != localDatabase.getUserID(1)) {
                                Person p = new Person(userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl);
                                if (p.getAge() <= localDatabase.getMatchmakingPrefMaxAge() && p.getAge() >= localDatabase.getMatchmakingPrefMinAge()) {
                                    matchingPeopleList.add(new Person(userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl));
                                }
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

                Log.e("MatchmakingFragment", "Error: " + errorMsg);

                Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                toastLoginError.show();
                progressDialog.dismiss();
            }
        };

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DB_API_URL, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> matchmakingInfo = localDatabase.getMatchmakingInfo();
                Map<String, String> params = new HashMap<String, String>();

                params.put("tag", "find_matches");
                params.put("area", inputArea);
                params.put("city", inputCity);
                params.put("sessions_per_week", matchmakingInfo.get("sessions_per_week"));
                params.put("pref_sex", matchmakingInfo.get("pref_sex"));
                params.put("pref_min_age", matchmakingInfo.get("pref_min_age"));
                params.put("pref_max_age", matchmakingInfo.get("pref_max_age"));

                return params;
            }
        };

        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_find_matches");
    }
}
