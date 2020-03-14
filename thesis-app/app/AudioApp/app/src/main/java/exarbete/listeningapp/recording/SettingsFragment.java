package exarbete.listeningapp.recording;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import exarbete.listeningapp.MainActivity;
import exarbete.listeningapp.R;
import exarbete.listeningapp.SharedPrefsHandler;

/**
 * Created by Barre on 2016-04-30.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private TextView amplitudelevel = null;
    private AudioRecorder testRecorder = null;
    private SeekBar seekBar = null;
    private Button testButton = null;
    private Button confirmButton = null;
    private int amplitudeInterval = 0;


    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstanceOf() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings, container, false);

        amplitudelevel = (TextView) fragmentView.findViewById(R.id.amplitude_level);
        seekBar = (SeekBar)fragmentView.findViewById(R.id.progess_bar_amplitude);
        testButton = (Button) fragmentView.findViewById(R.id.test_amplitude_button);
        confirmButton = (Button) fragmentView.findViewById(R.id.confirm_button);
        seekBar.setMax(30000);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                amplitudeInterval = progress;
                amplitudelevel.setText("" + amplitudeInterval);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.i(TAG, "" + seekBar.getProgress());
                testTrigger(seekBar.getProgress());
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setAmplitude();
            }
        });
        return fragmentView;
    }


    public void testTrigger(int amplitudeToTest){
        if(testRecorder == null){
            testRecorder = new AudioRecorder(this.getActivity());
        }
        if(!testRecorder.isListening()){
            testButton.setText("Stop testing");
            MainActivity activity = (MainActivity) this.getActivity();
            activity.setViewPagerEnabled(false);
            testRecorder.testAmplitude(amplitudeToTest, testButton);
        }else{
            testRecorder.setTriggered(true);
        }
    }

    public void setAmplitude(){
        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(SharedPrefsHandler.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
        prefEditor.putInt(SharedPrefsHandler.USER_MAX_AMPLITUDE_KEY, amplitudeInterval);
        prefEditor.apply();
        Snackbar.make(this.getActivity().findViewById(R.id.drawer_layout), amplitudeInterval + " saved as trigger volume.", Snackbar.LENGTH_LONG).show();
        Log.i(TAG, "" + sharedPrefs.getInt(SharedPrefsHandler.USER_MAX_AMPLITUDE_KEY, 12000));
    }

    public void stopTesting(){
        if(testRecorder != null){
            testRecorder.setTriggered(true);
        }
    }


}
