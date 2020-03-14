package se.su.dsv.viking_prep_pvt_15_group9;

/**
 * Created by Barre on 2015-05-04.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.app.FragmentTransaction;


public class ObstaclesFragment extends Fragment implements View.OnClickListener {

    View rootView;

    public static ObstaclesFragment newInstanceOf() {
        ObstaclesFragment fragment = new ObstaclesFragment();
        return fragment;
    }


    public ObstaclesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_obstacles, container, false);



        Button b1 = (Button) rootView.findViewById(R.id.balancebutton);
        Button b2 = (Button) rootView.findViewById(R.id.carryalogbutton);
        Button b3 = (Button) rootView.findViewById(R.id.icetankbutton);
        Button b4 = (Button) rootView.findViewById(R.id.monkeybarbutton);
        Button b5 = (Button) rootView.findViewById(R.id.pipesbutton);
        Button b6 = (Button) rootView.findViewById(R.id.reebokbutton);
        Button b7 = (Button) rootView.findViewById(R.id.ropesbutton);
        Button b8 = (Button) rootView.findViewById(R.id.tractortiresbutton);

//iv2 (carry a log): bilden heter, missvisande nog, ropepicture, så är inte fel i koden.
        ImageView iv1 = (ImageView) rootView.findViewById(R.id.balancepicture);
        ImageView iv2 = (ImageView) rootView.findViewById(R.id.carryalogpicture);
        ImageView iv3 = (ImageView) rootView.findViewById(R.id.icetankpicture);
        ImageView iv4 = (ImageView) rootView.findViewById(R.id.monkeybarpicture);
        ImageView iv5 = (ImageView) rootView.findViewById(R.id.pipespicture);
        ImageView iv6 = (ImageView) rootView.findViewById(R.id.reebokpicure);
        ImageView iv7 = (ImageView) rootView.findViewById(R.id.ropespicture);
        ImageView iv8 = (ImageView) rootView.findViewById(R.id.tractortirespicutre);


//Lyssnare till Buttons
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        b5.setOnClickListener(this);
        b6.setOnClickListener(this);
        b7.setOnClickListener(this);
        b8.setOnClickListener(this);

//Lyssnare till ImageView
        iv1.setOnClickListener(this);
        iv2.setOnClickListener(this);
        iv3.setOnClickListener(this);
        iv4.setOnClickListener(this);
        iv5.setOnClickListener(this);
        iv6.setOnClickListener(this);
        iv7.setOnClickListener(this);
        iv8.setOnClickListener(this);

        return rootView;

    }

    @Override
    public void onClick(View v) {

        ChosenObstacleFragment fragment = new ChosenObstacleFragment();
        Bundle b = new Bundle();

        if(v.getId()==R.id.balancebutton || v.getId()== R.id.balancepicture) {
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Balance");
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();

        }else if(v.getId()==R.id.carryalogbutton || v.getId()== R.id.carryalogpicture){
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Carry-A-Log");
            fragment.setArguments(b);
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();
        }
        else if(v.getId()==R.id.icetankbutton || v.getId()== R.id.icetankpicture){
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Ice Tank");
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();

        }else if(v.getId()==R.id.monkeybarbutton || v.getId()== R.id.monkeybarpicture){
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Monkeybar");
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();

        }else if(v.getId()==R.id.pipesbutton || v.getId()== R.id.pipespicture){
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Pipes");
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();

        }else if(v.getId()==R.id.reebokbutton || v.getId()== R.id.reebokpicure){
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Reebok 10000 volt");
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();

        }else if(v.getId()==R.id.ropespicture || v.getId()== R.id.ropesbutton){
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Ropes");
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();

        }else if(v.getId()==R.id.tractortiresbutton || v.getId()== R.id.tractortirespicutre){
            FragmentTransaction fragmentManager = getFragmentManager().beginTransaction();
            b.putString("chosenObstacle", "Tractor tires");
            fragment.setArguments(b);
            fragmentManager.replace(R.id.content_frame, fragment);
            fragmentManager.commit();
        }
    }
}