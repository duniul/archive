package se.su.dsv.viking_prep_pvt_15_group9;

/**
 * @author Daniel
 */

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;

public class MyProfileFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private NetworkImageView profilePicture;
    private TextView nameText;
    private TextView ageAndLocationText;
    private String name;
    private String age;
    private String location;
    private SQLiteManager localDatabase;

    public static MyProfileFragment newInstanceOf() {
        MyProfileFragment fragment = new MyProfileFragment();
        return fragment;
    }

    public MyProfileFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localDatabase = new SQLiteManager(getActivity());
        name = localDatabase.getFullName(1);
        age = localDatabase.getAge(1);
        location = localDatabase.getLocation(1);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);
        profilePicture = (NetworkImageView) rootView.findViewById(R.id.profile_picture);
        profilePicture.setImageUrl(localDatabase.getPictureUrl(1), AppRequestManager.getInstance().getImageLoader());

        nameText = (TextView) rootView.findViewById((R.id.text_name));
        ageAndLocationText = (TextView) rootView.findViewById((R.id.text_age_and_location));
        Button buttonEditProfile = (Button) rootView.findViewById(R.id.button_edit_profile);
        buttonEditProfile.setOnClickListener(this);


        nameText.setText(name);
        ageAndLocationText.setText(age + " år - " + location);

        return rootView;

    }

    @Override
    public void onClick(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, EditProfileFragment.newInstanceOf()).addToBackStack("")
                .commit();
    }
}
