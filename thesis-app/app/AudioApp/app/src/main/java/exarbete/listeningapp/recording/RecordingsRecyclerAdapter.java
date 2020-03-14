package exarbete.listeningapp.recording;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import exarbete.listeningapp.R;
import exarbete.listeningapp.SharedPrefsHandler;
import exarbete.listeningapp.database.SQLiteHelper;
import exarbete.listeningapp.retrofit.DeleteRecordingService;
import exarbete.listeningapp.retrofit.MessageResponse;
import exarbete.listeningapp.retrofit.ServiceGenerator;
import exarbete.listeningapp.retrofit.UpdateRecordingService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Based on Gabriele Mariotti's open SimpleAdapter, available here:
 * https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
 *
 * Created by Daniel on 2016-04-12.
 */
public class RecordingsRecyclerAdapter extends RecyclerView.Adapter<RecordingsRecyclerAdapter.ViewHolder> {

    private final Context context;
    private List<Recording> recordings;
    private RecordingsDialogFragment recordingsDialogFragment;

    public void add(Recording recording, int position) {
        position = position == -1 ? getItemCount() : position;
        recordings.add(position, recording);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        if (position < getItemCount()) {
            recordings.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout recordingItem;
        ImageButton recordingOptions;
        TextView recordingFilename;
        TextView recordingDate;
        TextView recordingDuration;

        public ViewHolder(View view) {
            super(view);
            recordingItem = (LinearLayout) itemView.findViewById(R.id.recordings_item_info_layout);
            recordingOptions = (ImageButton) itemView.findViewById(R.id.recordings_item_options_button);
            recordingFilename = (TextView) itemView.findViewById(R.id.listening_session_date);
            recordingDate = (TextView) itemView.findViewById(R.id.listening_session_endtime);
            recordingDuration = (TextView) itemView.findViewById(R.id.listening_sessions_num_of_recordings);
        }
    }

    public RecordingsRecyclerAdapter(Context context, RecordingsDialogFragment recordingsDialogFragment, List<Recording> recordings) {
        this.context = context;
        this.recordingsDialogFragment = recordingsDialogFragment;
        if (recordings != null) {
            this.recordings = recordings;
        } else  {
            recordings = new ArrayList<Recording>();
        }
    }

    @Override
    public RecordingsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.recordings_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.recordingFilename.setText(recordings.get(position).getName());
        holder.recordingDate.setText(recordings.get(position).getDate());
        holder.recordingDuration.setText(recordings.get(position).formatDuration(recordings.get(position).getDuration()));

        File f = new File(recordings.get(position).getFilePath() + recordings.get(position).getName());

        if(!f.exists()) {
            holder.recordingFilename.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
        } else {
            holder.recordingFilename.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
        }

        final Recording recording = recordings.get(position);
        holder.recordingItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(recording.getFilePath() + recording.getName());

                if (file.exists()) {
                    openFile(file);
                } else if (!recording.getUrl().isEmpty() || recording.getUrl() != null) {
                    downloadFile(recording.getRecordingID(), recording.getName(), recording.getUrl());

                }
            }
        });

        holder.recordingOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder recordingOptionsDialog = new AlertDialog.Builder(context);
                recordingOptionsDialog.setTitle("Options")
                        .setItems(new String[]{"Rename recording", "Delete recording"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("New filename");

                                    // Set up the input
                                    final EditText input = new EditText(context);
                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                    builder.setView(input);

                                    // Set up the buttons
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String newFilename = input.getText().toString() + ".m4a";
                                            renameFile(recording, newFilename);
                                        }
                                    });
                                    builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    builder.show();

                                } else if (which == 1) {
                                    try {
                                        deleteRecording(recording);
                                    } catch (IOException e) {
                                        Log.e("DeleteRecording", "Delete recording could not run!");
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                recordingOptionsDialog.create().show();
            }
        });
    }

    private void openFile(File file) {
        Uri uri = Uri.fromFile(file);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        it.setDataAndType(uri, "audio/m4a");
        context.startActivity(it);
    }

    private void renameFile(Recording recording, String newName){

        final ProgressDialog progressDialog = ProgressDialog.show(context, "Renaming recording", "", true);

        String lastEdited = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        File file = new File(recording.getFilePath()+recording.getName());
        File to = new File(recording.getFilePath()+newName);
        file.renameTo(to);
        SQLiteHelper.getInstance().updateUserFileName(recording.getRecordingID(), newName, lastEdited);

        UpdateRecordingService updateRecordingService = ServiceGenerator.createService(UpdateRecordingService.class);
        Call<MessageResponse> updateRecordingCall = updateRecordingService.updateRecording(recording.getRecordingID(), newName, lastEdited);
        updateRecordingCall.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                progressDialog.dismiss();
                recordingsDialogFragment.refreshRecordingsList();
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                progressDialog.dismiss();
                recordingsDialogFragment.refreshRecordingsList();
            }
        });
    }

    private void deleteRecording(final Recording recording) throws IOException {

        final ProgressDialog progressDialog = ProgressDialog.show(context, "Deleting recording", "", true);

        if (!recording.getUrl().isEmpty() && recording.getUrl() != null) {
            DeleteRecordingService deleteRecordingService = ServiceGenerator.createService(DeleteRecordingService.class);
            Call<MessageResponse> deleteRecordingCall = deleteRecordingService.deleteRecording(recording.getRecordingID(), recording.getUrl());
            deleteRecordingCall.enqueue(new Callback<MessageResponse>() {

                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {

                    if (response.isSuccessful()) {

                        Log.i("DeleteRecording", "SUCCESS!");

                        File file = new File(recording.getFilePath() + recording.getName());

                        if (file.exists()) {
                            file.delete();
                        }

                        SQLiteHelper.getInstance().deleteRecording(recording.getRecordingID());

                        progressDialog.dismiss();
                        recordingsDialogFragment.refreshRecordingsList();

                    } else {
                        Log.i("DeleteRecording", "NO SUCCESS!");
                        progressDialog.dismiss();
                        recordingsDialogFragment.refreshRecordingsList();
                    }

                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    Log.e("DeleteRecording", "ON FAILURE!");
                    progressDialog.dismiss();
                    recordingsDialogFragment.refreshRecordingsList();
                }
            });

        } else {
            progressDialog.dismiss();
            Toast.makeText(context, "File is not yet uploaded. Try again later.", Toast.LENGTH_LONG).show();
            recordingsDialogFragment.refreshRecordingsList();
        }


    }

    private void downloadFile(final long recordingID, final String fileName, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final String fullFolderPath = SharedPrefsHandler.getInstance().getString(SharedPrefsHandler.FOLDER_PATH_KEY, Environment.DIRECTORY_DOWNLOADS);
        String folder = fullFolderPath.substring(fullFolderPath.lastIndexOf("/"), fullFolderPath.length()) + "/";

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                SQLiteHelper localDatabase = SQLiteHelper.getInstance();
                localDatabase.updateRecordingFilePath(recordingID, fullFolderPath + "/");
                recordingsDialogFragment.refreshRecordingsList();
            }
        };

        Uri uri = Uri.parse(url);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(folder, fileName);
        context.registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager.enqueue(request);

    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }
}