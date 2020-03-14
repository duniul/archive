package exarbete.listeningapp.recording;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import exarbete.listeningapp.PermissionHandler;
import exarbete.listeningapp.R;

public class RecorderFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = RecorderFragment.class.getSimpleName();
    private static final int RECORDER_PERMISSIONS_REQUEST = 0;
    private static String[] RECORDER_PERMISSIONS = {Manifest.permission.RECORD_AUDIO,
                                                                 Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                 Manifest.permission.ACCESS_FINE_LOCATION};

    private Button startPauseRecordButton = null;
    private Button stopRecordButton = null;
    private AudioRecorder mAudioRecorder = null;
    private View.OnClickListener startPauseRecordListener = null;
    private View.OnClickListener stopRecordListener = null;

    public RecorderFragment() {
        // Required empty public constructor
    }

    public static RecorderFragment newInstanceOf() {
        RecorderFragment fragment = new RecorderFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAudioRecorder = new AudioRecorder(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_recorder, container, false);

        startPauseRecordButton = (Button) fragmentView.findViewById(R.id.startListeningButton);
        stopRecordButton = (Button) fragmentView.findViewById(R.id.stopListeningButton);

        startPauseRecordButton.setOnClickListener(this);
        stopRecordButton.setOnClickListener(this);

        return fragmentView;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.startListeningButton:
                if(!mAudioRecorder.isListening()) {
                    startRecording();
                }
                break;

            case R.id.stopListeningButton:
                if (mAudioRecorder.isListening()) {
                    stopRecording();
                }
                break;
        }
    }

    private void requestNecessaryPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            Snackbar.make(getActivity().findViewById(R.id.mainLayout), getString(R.string.recorder_permission_rationale_short), Snackbar.LENGTH_LONG)
                    .setAction("More", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(Html.fromHtml(getString(R.string.recorder_permission_rationale_previously_denied)))
                                    .setTitle(getString(R.string.permission_dialog_title));
                            builder.setPositiveButton("Request permissions", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    requestPermissions(RECORDER_PERMISSIONS, RECORDER_PERMISSIONS_REQUEST);
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Empty onClick to dismiss the dialog.
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    })
                    .show();
        } else {
            requestPermissions(RECORDER_PERMISSIONS, RECORDER_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == RECORDER_PERMISSIONS_REQUEST) {
            if (PermissionHandler.verifyPermissions(grantResults)) {
                Snackbar.make(getActivity().findViewById(R.id.mainLayout), getString(R.string.recorder_permission_granted), Snackbar.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(Html.fromHtml(getString(R.string.recorder_permission_rationale_denied)))
                        .setTitle(getString(R.string.permission_dialog_title));
                builder.setPositiveButton("Request again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermissions(RECORDER_PERMISSIONS, RECORDER_PERMISSIONS_REQUEST);
                    }
                });
                builder.setNegativeButton("I'm sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Snackbar.make(getActivity().findViewById(R.id.mainLayout), getString(R.string.recorder_permission_denied), Snackbar.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startRecording() {
        if (PermissionHandler.checkPermissions(getActivity(), RECORDER_PERMISSIONS)) {
            if (!mAudioRecorder.isListening()) {
                mAudioRecorder.startListening();
                startPauseRecordButton.setEnabled(false);
                stopRecordButton.setEnabled(true);
            }
        } else {
            requestNecessaryPermissions();
        }
    }

    public void stopRecording() {
        if (PermissionHandler.checkPermissions(getActivity(), RECORDER_PERMISSIONS)) {
            Snackbar.make(getActivity().findViewById(R.id.mainLayout), getString(R.string.recording_saved_local), Snackbar.LENGTH_LONG).show();
            mAudioRecorder.stopListening();
            startPauseRecordButton.setEnabled(true);
            stopRecordButton.setEnabled(false);

            int viewPagerPosition = 1;
            String recordingsFragmentTag = "android:switcher:" + R.id.viewPager + ":" + viewPagerPosition;
            ListeningSessionsFragment listeningSessionsFragment = (ListeningSessionsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(recordingsFragmentTag);
            listeningSessionsFragment.refreshListeningSessions();
        } else {
            requestNecessaryPermissions();
        }
    }
}
