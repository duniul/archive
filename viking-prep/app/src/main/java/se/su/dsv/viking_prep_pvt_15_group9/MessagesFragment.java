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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
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
import se.su.dsv.viking_prep_pvt_15_group9.model.Message;
import se.su.dsv.viking_prep_pvt_15_group9.util.MessageListAdapter;

/**
 * Created by Daniel on 2015-05-31.
 */
public class MessagesFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    private NetworkImageView thumbnail;
    private TextView conversationTitle;
    private List<Message> messageList = new ArrayList<Message>();
    private ListView messageListView;
    private EditText messageField;
    private Button sendButton;

    private ProgressDialog progressDialog;
    private MessageListAdapter messageListAdapter;
    private SQLiteManager localDatabase;

    public static MessagesFragment newInstanceOf() {
        MessagesFragment fragment = new MessagesFragment();
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

        rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        conversationTitle = (TextView) rootView.findViewById(R.id.conversation_title);
        conversationTitle.setText(localDatabase.getFullName(2));
        thumbnail = (NetworkImageView) rootView.findViewById(R.id.thumbnail);
        thumbnail.setImageUrl(localDatabase.getPictureUrl(2), AppRequestManager.getInstance().getImageLoader());
        messageListView = (ListView) rootView.findViewById((R.id.list_messages));
        messageField = (EditText) rootView.findViewById(R.id.field_message_text);
        sendButton = (Button) rootView.findViewById(R.id.button_send_message);
        messageListAdapter = new MessageListAdapter(getActivity(), messageList);
        messageListView.setAdapter(messageListAdapter);
        sendButton.setOnClickListener(this);

        if(messageList.isEmpty()) {
            loadMessages();
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {

        String messageText = messageField.getText().toString();
        messageField.getText().clear();

        if (!messageText.isEmpty()) {
            sendMessage(messageText);
        }
    }

    private void loadMessages() {
        progressDialog.setMessage("Hämtar meddelanden...");
        progressDialog.show();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MessagesFragment", "Svar från databasen: " + response.toString());
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

                        JSONObject messages = jsonObject.getJSONObject("messages");
                        int numberOfMessages = jsonObject.getInt("number_of_messages");

                        for (int i = 1; i <= numberOfMessages; i++) {
                            JSONObject message = messages.getJSONObject(Integer.toString(i));
                            int senderUserID = message.getInt("user_id_sender");
                            int recieverUserID = message.getInt("user_id_reciever");
                            String messageText = message.getString("message_text");
                            String dateSent = message.getString("date_sent");
                            String timeSent = message.getString("time_sent");

                            if (senderUserID == localDatabase.getUserID(1)) {
                                messageList.add(new Message(localDatabase.getName(1), senderUserID, recieverUserID, messageText, dateSent, timeSent));
                            } else {
                                messageList.add(new Message(localDatabase.getName(2), recieverUserID, senderUserID, messageText, dateSent, timeSent));
                            }

                        }
                    }

                    messageListAdapter.notifyDataSetChanged();

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
                params.put("tag", "load_messages");
                params.put("user1_id", Integer.toString(localDatabase.getUserID(1)));
                params.put("user2_id", Integer.toString(localDatabase.getUserID(2)));

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_load_messages");

    }

    private void sendMessage(final String messageText) {

        String senderName = localDatabase.getName(1);
        int senderUserID = localDatabase.getUserID(1);
        int recieverUserID = localDatabase.getUserID(2);

        messageList.add(new Message(senderName, senderUserID, recieverUserID, messageText));
        messageListAdapter.notifyDataSetChanged();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ConversationFragment", "Svar från databasen: " + response.toString());

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

                        JSONObject message = jsonObject.getJSONObject("message");
                        String storedMessageText = message.getString("message_text");
                        String dateSent = message.getString("date_sent");
                        String timeSent = message.getString("time_sent");

                        messageList.get(messageList.size() - 1).setDateSent(dateSent);
                        messageList.get(messageList.size() - 1).setTimeSent(timeSent);
                        messageListAdapter.notifyDataSetChanged();
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

                Log.e("ConversationFragment", "Registration error: " + errorMsg);

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
                params.put("tag", "store_message");
                params.put("user_id_sender", Integer.toString(localDatabase.getUserID(1)));
                params.put("user_id_reciever", Integer.toString(localDatabase.getUserID(2)));
                params.put("message_text", messageText);

                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_store_message");;
    }
}