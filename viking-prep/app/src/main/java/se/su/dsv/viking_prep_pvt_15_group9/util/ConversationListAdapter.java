package se.su.dsv.viking_prep_pvt_15_group9.util;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import se.su.dsv.viking_prep_pvt_15_group9.MainActivity;
import se.su.dsv.viking_prep_pvt_15_group9.MessagesFragment;
import se.su.dsv.viking_prep_pvt_15_group9.R;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.model.Conversation;
import se.su.dsv.viking_prep_pvt_15_group9.model.Person;

/**
 * Created by Daniel on 2015-05-28.
 */
public class ConversationListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private Activity mainActivity;
    private LayoutInflater inflater;
    private List<Conversation> conversations;
    private SQLiteManager localDatabase;
    private ProgressDialog progressDialog;
    private ImageLoader imageLoader = AppRequestManager.getInstance().getImageLoader();

    public ConversationListAdapter(Activity mainActivity, List<Conversation> conversations) {
        this.mainActivity = mainActivity;
        this.conversations = conversations;

        localDatabase = ((MainActivity) mainActivity).getLocalDatabase();
        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setCancelable(false);
    }

    @Override
    public int getCount() {
        return conversations.size();
    }

    @Override
    public Object getItem(int location) {
        return conversations.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_conversations, null);
        }

        if (imageLoader == null) {
            imageLoader = AppRequestManager.getInstance().getImageLoader();
        }

        NetworkImageView thumbnail = (NetworkImageView) convertView.findViewById(R.id.thumbnail);
        TextView conversationName = (TextView) convertView.findViewById(R.id.person_name);
        TextView lastMessageDateAndTime = (TextView) convertView.findViewById(R.id.last_message_date_and_time_sent);
        TextView lastMessageSentBy = (TextView) convertView.findViewById(R.id.last_message_sent_by);
        TextView lastMessageText = (TextView) convertView.findViewById(R.id.last_message_text);

        Conversation c = conversations.get(position);

        thumbnail.setImageUrl(c.getPerson().getPictureUrl(), AppRequestManager.getInstance().getImageLoader());
        conversationName.setText(c.getPerson().getFullName());
        lastMessageDateAndTime.setText(c.getLastMessageDate() + " - " + c.getLastMessageTime());
        lastMessageSentBy.setText("Skickat av " + c.getLastMessageSenderTitle() + ":");
        if (c.getLastMessageText().length() > 90) {
            lastMessageText.setText("\"" + c.getLastMessageText().substring(0, 88) + "...\"");
        } else {
            lastMessageText.setText("\"" + c.getLastMessageText() + "\"");
        }


        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        FragmentManager fragmentManager = mainActivity.getFragmentManager();
        Conversation clickedConversation = conversations.get(position);

        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        Bitmap profilePicture;

        Person person = clickedConversation.getPerson();
        int userID = person.getUserID();
        String uniqueID = person.getUniqueID();
        String name = person.getName();
        String surname = person.getSurname();
        String email = person.getEmail();
        String area = person.getArea();
        String city = person.getCity();
        String dateOfBirth = person.getDateOfBirth();
        String sex = person.getSex();
        String createdAt = person.getCreatedAt();
        String updatedAt = person.getUpdatedAt();
        String pictureUrl = person.getPictureUrl();

        localDatabase.deleteUser(2);
        localDatabase.addUser(2, userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl);

        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, MessagesFragment.newInstanceOf()).addToBackStack("")
                .commit();
    }
}