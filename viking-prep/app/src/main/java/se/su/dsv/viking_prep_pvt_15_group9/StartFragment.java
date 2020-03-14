package se.su.dsv.viking_prep_pvt_15_group9;

/**
 * Created by Barre on 2015-05-04.
 */

import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SessionManager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Calendar;

public class StartFragment extends Fragment implements View.OnClickListener {
    View rootView;
    SessionManager session;
    Button counter;
    SQLiteManager localdatabase;

    public static StartFragment newInstanceOf(){

        StartFragment fragment = new StartFragment();
        return fragment;
    }


    public StartFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_start, container, false);
        Button findFriendButton = (Button) rootView.findViewById(R.id.hittaTräningsvänButton);
        Button profileButton = (Button) rootView.findViewById(R.id.profilButton);
        Button friendsButton = (Button) rootView.findViewById(R.id.vännerButton);
        Button obstaclesButton = (Button) rootView.findViewById(R.id.hinderButton);
        Button searchPersonButton =(Button) rootView.findViewById(R.id.find_person_button);
        Button conversationsButton =(Button) rootView.findViewById(R.id.conversations_button);

        counter = (Button) rootView.findViewById(R.id.welcome_message);
        localdatabase= new SQLiteManager(getActivity());
        // DayCounter
        Calendar thatDay = Calendar.getInstance();
        thatDay.set(Calendar.DAY_OF_MONTH,29);
        thatDay.set(Calendar.MONTH,7);
        thatDay.set(Calendar.YEAR,2015);
        Calendar today = Calendar.getInstance();
        long millisLeft = thatDay.getTimeInMillis()-today.getTimeInMillis();
        long daysLeft = millisLeft/ (24*60*60*1000);

        String userName = localdatabase.getName(1);
        counter.setText("Välkommen "+ userName + " - " + daysLeft + " dagar kvar");



        session = new SessionManager(getActivity());

        findFriendButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);
        friendsButton.setOnClickListener(this);
        obstaclesButton.setOnClickListener(this);
        searchPersonButton.setOnClickListener(this);
        conversationsButton.setOnClickListener(this);


        return rootView;


    }

    //@Override
    //public void onAttach(Activity activity) {
    //    super.onAttach(activity);
    //    ((MainActivity)activity).onSectionAttached(1);
    //}

    // Byter Fragment vid klick på knapparna, titeln ändras däremot inte!
    @Override
    public void onClick(View v) {
        android.app.FragmentManager fragmentManager = getFragmentManager();
        if(v.getId()==R.id.hinderButton) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, ObstaclesFragment.newInstanceOf()).addToBackStack("")
                    .commit();
        }else if(v.getId()==R.id.vännerButton){
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, FriendsFragment.newInstanceOf()).addToBackStack("")
                    .commit();
        }else if(v.getId()==R.id.hittaTräningsvänButton){
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, MatchmakingFragment.newInstanceOf()).addToBackStack("")
                    .commit();
        }else if(v.getId()==R.id.profilButton){
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, MyProfileFragment.newInstanceOf()).addToBackStack("")
                    .commit();
        }else if(v.getId()==R.id.conversations_button){
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, ConversationsFragment.newInstanceOf()).addToBackStack("")
                    .commit();
        }else if(v.getId()==R.id.find_person_button){
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, SearchPeopleFragment.newInstanceOf()).addToBackStack("")
                    .commit();
        }

    }


}
