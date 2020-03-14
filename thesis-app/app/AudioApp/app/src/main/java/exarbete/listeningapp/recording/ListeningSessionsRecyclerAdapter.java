package exarbete.listeningapp.recording;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import exarbete.listeningapp.R;
import exarbete.listeningapp.database.SQLiteHelper;
import exarbete.listeningapp.retrofit.DeleteSessionService;
import exarbete.listeningapp.retrofit.MessageResponse;
import exarbete.listeningapp.retrofit.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Based on Gabriele Mariotti's open SimpleAdapter, available here:
 * https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
 * <p/>
 * Created by Daniel on 2016-04-12.
 */
public class ListeningSessionsRecyclerAdapter extends RecyclerView.Adapter<ListeningSessionsRecyclerAdapter.ViewHolder> {

    private final Activity activity;
    private List<ListeningSession> listeningSessions;
    private ListeningSessionsFragment listeningSessionFragment;

    public void add(ListeningSession listeningSession, int position) {
        position = position == -1 ? getItemCount() : position;
        listeningSessions.add(position, listeningSession);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        if (position < getItemCount()) {
            listeningSessions.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout listeningSessionItem;
        ImageButton listeningSessionOptions;
        TextView listeningSessionDate;
        TextView listeningSessionStartTime;
        TextView listeningSessionEndTime;
        TextView listeningSessionRecordings;

        public ViewHolder(View view) {
            super(view);
            listeningSessionItem = (RelativeLayout) itemView.findViewById(R.id.listening_sessions_item);
            listeningSessionOptions = (ImageButton) itemView.findViewById(R.id.recordings_item_options_button);
            listeningSessionDate = (TextView) itemView.findViewById(R.id.listening_session_date);
            listeningSessionStartTime = (TextView) itemView.findViewById(R.id.listening_session_starttime);
            listeningSessionEndTime = (TextView) itemView.findViewById(R.id.listening_session_endtime);
            listeningSessionRecordings = (TextView) itemView.findViewById(R.id.listening_sessions_num_of_recordings);
        }
    }

    public ListeningSessionsRecyclerAdapter(Activity activity, List<ListeningSession> listeningSessions, ListeningSessionsFragment listeningSessionFragment) {
        this.activity = activity;
        this.listeningSessionFragment = listeningSessionFragment;
        if (listeningSessions != null) {
            this.listeningSessions = listeningSessions;
        } else  {
            this.listeningSessions = new ArrayList<ListeningSession>();
        }
    }

    @Override
    public ListeningSessionsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(activity).inflate(R.layout.listening_sessions_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat targetFormat = new SimpleDateFormat("EEEE, MMM d, yyyy");
        Date date = new Date();
        try {
            date = originalFormat.parse(listeningSessions.get(position).getStartTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);

        holder.listeningSessionDate.setText(formattedDate);
        holder.listeningSessionStartTime.setText("Start: " + listeningSessions.get(position).getStartTime());
        holder.listeningSessionEndTime.setText("End: " + listeningSessions.get(position).getEndTime());
        holder.listeningSessionRecordings.setText(listeningSessions.get(position).getNumberOfRecordings() + " recordings");

        final ListeningSession listeningSession = listeningSessions.get(position);

        holder.listeningSessionItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
                Fragment previous = activity.getFragmentManager().findFragmentByTag("dialog");

                if (previous != null) {
                    transaction.remove(previous);
                }

                DialogFragment recordingsDialogFragment = RecordingsDialogFragment.newInstanceOf(listeningSession.getSessionID());
                recordingsDialogFragment.show(activity.getFragmentManager(), "dialog");
            }
        });

        holder.listeningSessionOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder recordingOptionsDialog = new AlertDialog.Builder(activity);
                recordingOptionsDialog.setTitle("Options")
                        .setItems(new String[]{"Delete session"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    try {
                                        deleteSession(listeningSession);
                                    } catch (IOException e) {
                                        Log.e("DeleteSession", "deleteSession could not run!");
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                recordingOptionsDialog.create().show();
            }
        });
    }

    private void deleteSession(ListeningSession listeningSession) throws IOException {

        boolean unuploadedRecordings = false;
        final ProgressDialog progressDialog = ProgressDialog.show(activity, "Deleting session and recordings", "", true);

        List<Recording> sessionRecordings = SQLiteHelper.getInstance().getRecordings(listeningSession.getSessionID());

        for (Recording recording : sessionRecordings) {
            if (recording.getUrl().isEmpty() || recording.getUrl() == null) {
                unuploadedRecordings = true;
                break;
            }
        }

        if (unuploadedRecordings) {

            progressDialog.dismiss();
            Toast.makeText(activity, "Some recordings haven't been uploaded. Try again later.", Toast.LENGTH_LONG).show();
            listeningSessionFragment.refreshListeningSessions();

        } else {

            for (Recording recording : sessionRecordings) {
                File file = new File(recording.getFilePath() + recording.getName());

                if (file.exists()) {
                    file.delete();
                }
            }

            SQLiteHelper.getInstance().deleteSession(listeningSession.getSessionID());

            DeleteSessionService deleteSessionService = ServiceGenerator.createService(DeleteSessionService.class);
            Call<MessageResponse> deleteSessionCall = deleteSessionService.deleteSession(listeningSession.getSessionID());
            deleteSessionCall.enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    progressDialog.dismiss();
                    listeningSessionFragment.refreshListeningSessions();
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    listeningSessionFragment.refreshListeningSessions();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listeningSessions.size();
    }
}