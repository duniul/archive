package exarbete.listeningapp.recording;

import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exarbete.listeningapp.R;
import exarbete.listeningapp.database.SQLiteHelper;

/**
 * Created by Daniel on 2016-05-05.
 */
public class RecordingsDialogFragment extends DialogFragment {

    private static String TAG = RecordingsDialogFragment.class.getSimpleName();

    private static String ARG_SESSION_ID = "sessionID";

    private long sessionID;
    private List<Recording> recordingsList = new ArrayList<Recording>();
    private RecyclerView recordingsDialogRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private RecordingsRecyclerAdapter recylerAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public RecordingsDialogFragment() {
        // Required empty public constructor
    }

    public static RecordingsDialogFragment newInstanceOf(long sessionID) {
        RecordingsDialogFragment fragment = new RecordingsDialogFragment();

        // Supply sessionID input as an argument.
        Bundle args = new Bundle();
        args.putLong(ARG_SESSION_ID, sessionID);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionID = getArguments().getLong(ARG_SESSION_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_dialog_recordings, container, false);

        recordingsDialogRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recordings_recycler_view);
        recylerAdapter = new RecordingsRecyclerAdapter(getActivity(), this, recordingsList);
        recordingsDialogRecyclerView.setAdapter(recylerAdapter);

        layoutManager = new LinearLayoutManager(fragmentView.getContext());
        layoutManager.setAutoMeasureEnabled(true);
        recordingsDialogRecyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.recordings_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecordingsList();
            }
        });

        refreshRecordingsList();

        return fragmentView;
    }

    public void refreshRecordingsList() {

        SQLiteHelper localDatabase = SQLiteHelper.getInstance();

        recordingsList.clear();
        recordingsList.addAll(localDatabase.getRecordings(sessionID));

        Collections.reverse(recordingsList);

        recylerAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);

        ListeningSessionsFragment.refresh();
    }

}
