package exarbete.listeningapp.recording;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import exarbete.listeningapp.PermissionHandler;
import exarbete.listeningapp.R;
import exarbete.listeningapp.database.SQLiteHelper;

/**
 * Created by Daniel on 2016-04-11.
 */
public class ListeningSessionsFragment extends Fragment {

    private static final String TAG = ListeningSessionsFragment.class.getSimpleName();
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 0;
    private static String[] READ_EXTERNAL_STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static ListeningSessionsFragment current;

    private List<ListeningSession> listeningSessionsList = new ArrayList<ListeningSession>();
    private RecyclerView listeningSessionsRecyclerView;
    private TextView noListeningSessions = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private ListeningSessionsRecyclerAdapter recylerAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<ListeningSessionsRecyclerSectionAdapter.Section> sections;
    private ListeningSessionsRecyclerSectionAdapter mSectionedAdapter;

    public ListeningSessionsFragment() {
        // Required empty public constructor
    }

    public static ListeningSessionsFragment newInstanceOf() {
        ListeningSessionsFragment fragment = new ListeningSessionsFragment();
        current = fragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_listening_sessions, container, false);

        noListeningSessions = (TextView) fragmentView.findViewById(R.id.listening_sessions_not_available);

        listeningSessionsRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.listening_sessions_recycler_view);
        recylerAdapter = new ListeningSessionsRecyclerAdapter(getActivity(), listeningSessionsList, this);
        mSectionedAdapter = new ListeningSessionsRecyclerSectionAdapter(getContext(), R.layout.recordings_divider, R.id.recordings_divider_text_view, recylerAdapter);
        listeningSessionsRecyclerView.setAdapter(mSectionedAdapter);

        layoutManager = new LinearLayoutManager(fragmentView.getContext());
        listeningSessionsRecyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.listening_sessions_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshListeningSessions();
            }
        });

        refreshListeningSessions();

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_listening_sessions_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.listening_sessions_menu_refresh:
                refreshListeningSessions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void refresh() {
        current.refreshListeningSessions();
    }

    public void refreshListeningSessions() {
        if (PermissionHandler.checkPermissions(getActivity(), READ_EXTERNAL_STORAGE_PERMISSION)) {
            SQLiteHelper localDatabase = SQLiteHelper.getInstance();

            listeningSessionsList.clear();
            listeningSessionsList.addAll(localDatabase.getListeningSessions());

            if (listeningSessionsList.isEmpty() || listeningSessionsList == null) {
                noListeningSessions.setVisibility(View.VISIBLE);
                noListeningSessions.setText("No sessions are available.");
            } else {
                noListeningSessions.setVisibility(View.INVISIBLE);
            }

            Collections.reverse(listeningSessionsList);

            sections = placeSections();
            ListeningSessionsRecyclerSectionAdapter.Section[] dummy = new ListeningSessionsRecyclerSectionAdapter.Section[sections.size()];
            mSectionedAdapter.setSections(sections.toArray(dummy));

            mSectionedAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);

        } else {
            noListeningSessions.setVisibility(View.VISIBLE);
            noListeningSessions.setText("No sessions available because permissions were not given.");
            swipeRefreshLayout.setRefreshing(false);
            requestReadExternalStoragePermission();
        }
    }

    private void requestReadExternalStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(getActivity().findViewById(R.id.mainLayout), getString(R.string.read_external_permission_rationale_short), Snackbar.LENGTH_LONG)
                    .setAction("More", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(Html.fromHtml(getString(R.string.read_external_permission_rationale_previously_denied)))
                                    .setTitle(getString(R.string.permission_dialog_title));
                            builder.setPositiveButton("Request permissions", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    requestPermissions(READ_EXTERNAL_STORAGE_PERMISSION, READ_EXTERNAL_STORAGE_REQUEST_CODE);
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
            requestPermissions(READ_EXTERNAL_STORAGE_PERMISSION, READ_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (PermissionHandler.verifyPermissions(grantResults)) {
                Snackbar.make(getActivity().findViewById(R.id.mainLayout), getString(R.string.read_external_permission_granted), Snackbar.LENGTH_SHORT).show();
                refreshListeningSessions();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(Html.fromHtml(getString(R.string.read_external_permission_rationale_denied)))
                        .setTitle(getString(R.string.permission_dialog_title));
                builder.setPositiveButton("Request again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermissions(READ_EXTERNAL_STORAGE_PERMISSION, READ_EXTERNAL_STORAGE_REQUEST_CODE);
                    }
                });
                builder.setNegativeButton("I'm sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Snackbar.make(getActivity().findViewById(R.id.mainLayout), getString(R.string.read_external_permission_denied), Snackbar.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private List<ListeningSessionsRecyclerSectionAdapter.Section> placeSections() {
        List<ListeningSessionsRecyclerSectionAdapter.Section> sections = new ArrayList<ListeningSessionsRecyclerSectionAdapter.Section>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getDefault());

        int[] sectionRows = {-1, -1, -1, -1, -1, -1};
        String[] sectionLabels = {"Today", "Yesterday", "Last 7 days", "Last 14 days", "Last 30 days", "Older than 30 days"};

        for (int i = listeningSessionsList.size() - 1; i >= 0; i--) {

            try {
                Date recordingDate = format.parse(listeningSessionsList.get(i).getStartTime());

                if (getDaysSince(recordingDate) <= 0) {
                    sectionRows[0] = i;
                } else if (getDaysSince(recordingDate) == 1) {
                    sectionRows[1] = i;
                } else if (getDaysSince(recordingDate) <= 6) {
                    sectionRows[2] = i;
                } else if (getDaysSince(recordingDate) <= 13) {
                    sectionRows[3] = i;
                } else if (getDaysSince(recordingDate) <= 29) {
                    sectionRows[4] = i;
                } else {
                    sectionRows[5] = i;
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < sectionRows.length; i++) {
            if (sectionRows[i] != -1) {
                sections.add(new ListeningSessionsRecyclerSectionAdapter.Section(sectionRows[i], sectionLabels[i]));
            }
        }

        return sections;

    }

    public long getDaysSince(Date date) {
        Calendar recordDate = toCalendar(date.getTime());

        Calendar today = toCalendar(System.currentTimeMillis());

        // Get the represented date in milliseconds
        long recordDateMillis = recordDate.getTimeInMillis();
        long todayMillis = today.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = todayMillis - recordDateMillis;

        return diff / (24 * 60 * 60 * 1000);
    }

    private Calendar toCalendar(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

}