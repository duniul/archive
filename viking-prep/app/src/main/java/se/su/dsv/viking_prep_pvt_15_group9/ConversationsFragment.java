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
import se.su.dsv.viking_prep_pvt_15_group9.model.Conversation;
import se.su.dsv.viking_prep_pvt_15_group9.model.Message;
import se.su.dsv.viking_prep_pvt_15_group9.model.Person;
import se.su.dsv.viking_prep_pvt_15_group9.util.ConversationListAdapter;

/**
 * Created by Daniel on 2015-05-31.
 */
public class ConversationsFragment extends Fragment {

    private View rootView;

    private List<Conversation> conversationList = new ArrayList<Conversation>();
    private ListView conversationListView;

    private ConversationListAdapter conversationListAdapter;
    private SQLiteManager localDatabase;
    private ProgressDialog progressDialog;

    public static ConversationsFragment newInstanceOf(){

        ConversationsFragment fragment = new ConversationsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        localDatabase = new SQLiteManager(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_conversations, container, false);

        conversationListView = (ListView) rootView.findViewById((R.id.list_conversations));
        conversationListAdapter = new ConversationListAdapter(getActivity(), conversationList);
        conversationListView.setAdapter(conversationListAdapter);
        conversationListView.setOnItemClickListener(conversationListAdapter);

        if (conversationList.isEmpty()) {
            loadConversations();
        }

        return rootView;
    }

    public void loadConversations() {
        progressDialog.setMessage("Hämtar konversationer...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ConversationsFragment", "Svar från databasen: " + response.toString());
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        int errorCode = Integer.parseInt(jsonObject.getString("error_code"));
                        if (errorCode != 71) {
                            Context context = getActivity();
                            int duration = Toast.LENGTH_LONG;
                            Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                            toastLoginError.show();
                        }

                    } else {

                        JSONObject conversations = jsonObject.getJSONObject("conversations");
                        int numberOfConversations = jsonObject.getInt("number_of_conversations");

                        for (int i = 1; i <= numberOfConversations; i++) {
                            JSONObject conversation = conversations.getJSONObject(Integer.toString(i));
                            JSONObject user2 = conversation.getJSONObject("user2");
                            JSONObject lastMessage = conversation.getJSONObject("last_message");

                            int user1ID = conversation.getInt("user1_id");

                            int user2ID = user2.getInt("user_id");
                            String u2UniqueID = user2.getString("unique_id");
                            String u2Name = user2.getString("name");
                            String u2Surname = user2.getString("surname");
                            String u2Email = user2.getString("email");
                            String u2Area = user2.getString("area");
                            String u2City = user2.getString("city");
                            String u2DateOfBirth = user2.getString("date_of_birth");
                            String u2Sex = user2.getString("sex");
                            String u2CreatedAt = user2.getString("created_at");
                            String u2UpdatedAt = user2.getString("updated_at");
                            String u2PictureUrl = user2.getString("picture_url");
                            Person person = new Person(user2ID, u2UniqueID, u2Name, u2Surname, u2Email, u2Area, u2City, u2DateOfBirth, u2Sex,u2CreatedAt, u2UpdatedAt, u2PictureUrl);

                            int lastMessageSenderID = lastMessage.getInt("user_id_sender");
                            int lastMessageRecieverID = lastMessage.getInt("user_id_reciever");
                            String lastMessageText = lastMessage.getString("message_text");
                            String lastMessageDateSent = lastMessage.getString("date_sent");
                            String lastMessageTimeSent = lastMessage.getString("time_sent");
                            String lastMessageSenderName;
                            if (lastMessageSenderID == localDatabase.getUserID(1)) {
                                lastMessageSenderName = localDatabase.getName(1);
                            } else {
                                lastMessageSenderName = u2Name;
                            }
                            Message message = new Message(lastMessageSenderName, lastMessageSenderID, lastMessageRecieverID, lastMessageText, lastMessageDateSent, lastMessageTimeSent);

                            conversationList.add(new Conversation(user1ID, user2ID, person, message));
                        }
                    }

                    conversationListAdapter.notifyDataSetChanged();

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

                Log.e("ConversationsFragment", "Registration error: " + errorMsg);

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
                params.put("tag", "load_conversations");
                params.put("user_id", Integer.toString(localDatabase.getUserID(1)));

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_load_conversations");
    }
}
