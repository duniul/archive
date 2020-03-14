package se.su.dsv.viking_prep_pvt_15_group9.util;

/**
 * Created by miaha_000 on 5/25/2015.
 */

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import se.su.dsv.viking_prep_pvt_15_group9.R;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.model.UserTipObject;

public class UserTipListAdapter extends ArrayAdapter<UserTipObject> {

    private int resource;
    private LayoutInflater inflater;
    private Context context;

    public UserTipListAdapter(Context ctx, int resourceId, List<UserTipObject> objects) {
        super(ctx, resourceId, objects);
        resource = resourceId;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = (RelativeLayout) inflater.inflate(resource, null);
        UserTipObject userTip = getItem(position);

       // TextView userTipName = (TextView) convertView.findViewById(R.id.user_tip_name);
       // userTipName.setText(userTip.getUserName());

        TextView name = (TextView) convertView.findViewById(R.id.user_tip_name);
        name.setText(userTip.getUserName());
        TextView description = (TextView) convertView.findViewById(R.id.user_tip_description);
        description.setText(userTip.getDescription());


        //ImageView får sättas sen
        NetworkImageView firstImage = (NetworkImageView)convertView.findViewById(R.id.firstTipImage);
        firstImage.setImageUrl(userTip.getFirstImage(), AppRequestManager.getInstance().getImageLoader());
        NetworkImageView secondImage = (NetworkImageView)convertView.findViewById(R.id.secondTipImage);
        secondImage.setImageUrl(userTip.getSecondImage(), AppRequestManager.getInstance().getImageLoader());

        return convertView;
    }
}
