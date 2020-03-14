package se.su.dsv.viking_prep_pvt_15_group9.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import se.su.dsv.viking_prep_pvt_15_group9.MainActivity;
import se.su.dsv.viking_prep_pvt_15_group9.R;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.model.Message;

/**
 * Created by Daniel on 2015-05-28.
 */
public class MessageListAdapter extends BaseAdapter {
    private Activity mainActivity;
    private LayoutInflater inflater;
    private List<Message> messages;
    private SQLiteManager localDatabase;
    private ImageLoader imageLoader = AppRequestManager.getInstance().getImageLoader();

    public MessageListAdapter(Activity mainActivity, List<Message> messages) {
        this.mainActivity = mainActivity;
        this.messages = messages;

        localDatabase = ((MainActivity) mainActivity).getLocalDatabase();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int location) {
        return messages.get(location);
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
            convertView = inflater.inflate(R.layout.list_row_messages, null);
        }

        if (imageLoader == null) {
            imageLoader = AppRequestManager.getInstance().getImageLoader();
        }

        TextView senderName = (TextView) convertView.findViewById(R.id.sender_name);
        TextView messageDate = (TextView) convertView.findViewById(R.id.date_sent);
        TextView messageTime = (TextView) convertView.findViewById(R.id.time_sent);
        TextView messageText = (TextView) convertView.findViewById(R.id.text_message);

        Message m = messages.get(position);

        senderName.setText(m.getSenderTitle());
        messageText.setText(m.getMessageText());

        if (m.getDateSent().isEmpty()) {
            messageDate.setText("Skickar...");
            messageTime.setText("");
        } else {
            messageDate.setText(m.getDateSent());
            messageTime.setText(m.getTimeSent());
        }

        return convertView;
    }
}