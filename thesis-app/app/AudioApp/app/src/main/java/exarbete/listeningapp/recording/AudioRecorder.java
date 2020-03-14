package exarbete.listeningapp.recording;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import exarbete.listeningapp.MainActivity;
import exarbete.listeningapp.R;
import exarbete.listeningapp.database.SQLiteHelper;
import exarbete.listeningapp.SharedPrefsHandler;
import exarbete.listeningapp.retrofit.MessageResponse;
import exarbete.listeningapp.retrofit.ServiceGenerator;
import exarbete.listeningapp.retrofit.StoreListeningSessionService;
import exarbete.listeningapp.retrofit.UpdateListeningSessionService;
import exarbete.listeningapp.retrofit.UploadRecordingService;
import exarbete.listeningapp.retrofit.UploadResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Daniel on 2016-04-09.
 */
public class AudioRecorder {
    private static final String TAG = AudioRecorder.class.getSimpleName();

    private AsyncTask silenceTask = null;
    private AsyncTask spikeTask = null;
    private boolean triggered = false;
    private boolean listening = false;
    private boolean paused = false;

    private String filePath = null;
    private String filename = null;
    private String fullFilePath = null;
    private int maxAmplitude;
    private GPSTracker tracker = null;

    //Standard values for higher quality audio, compatability with all phones? Think so.
    private final int encodingRate = 96000;
    private final int samplingRate = 44100;

    private Button testAmplitudeButton;
    private int amplitudeToTest;
    private Activity activity = null;
    private MediaRecorder mRecorder = null;

    private ListeningSession listeningSession = null;
    private SQLiteHelper localDatabase = null;
    private Date recordingStartTime;

    public AudioRecorder(Activity activity) {
        this.activity = activity;
        this.localDatabase = SQLiteHelper.getInstance();
    }

    public int getSharedMaxAmplitude(){
        SharedPreferences sharedPrefs = activity.getSharedPreferences(SharedPrefsHandler.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
        maxAmplitude = sharedPrefs.getInt(SharedPrefsHandler.USER_MAX_AMPLITUDE_KEY, 12000);
        return maxAmplitude;
    }

    public void testAmplitude(int amplitudeToTest, Button testAmplitudeButton) {
        if (listening) {
            Log.e(TAG, "AudioRecorder is already in use.");
        } else {
            if(checkFolder()) {
                SharedPreferences sharedPrefs = activity.getSharedPreferences(SharedPrefsHandler.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
                filePath = sharedPrefs.getString(SharedPrefsHandler.FOLDER_PATH_KEY, null);
                filename = createFileName();
                fullFilePath = filePath + "/" + filename;

                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setAudioEncodingBitRate(encodingRate);
                mRecorder.setAudioSamplingRate(samplingRate);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // three gpp or m4a? Samsung saves the file format as m4a, according to the web m4a is the new era.
                mRecorder.setOutputFile(fullFilePath);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e(TAG, "prepare() failed");
                }
                mRecorder.start();
                listening = true;
                paused = false;
                Log.i(TAG, "kmr vi hit");
                this.testAmplitudeButton = testAmplitudeButton;
                this.amplitudeToTest = amplitudeToTest;
                new ListenForSpikeSetting().execute(mRecorder);
            } else {
                Toast.makeText(activity, "Could not listen for audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ListenForSpikeSetting extends AsyncTask<MediaRecorder, Void, Void> {
        @Override
        protected Void doInBackground(MediaRecorder... recorders) {
            getRecorderMaxAmplitude();
            do {
                try {
                    Thread.sleep(200);
                    if(isCancelled()){
                        return null;
                    }
                    int currentAmp = getRecorderMaxAmplitude();
                    if (currentAmp > amplitudeToTest) {
                        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                        long[] pattern = {0, 100, 50, 100};
                        if(v.hasVibrator()){
                            v.vibrate(pattern, -1);
                        }
                        if(currentAmp > 30000){
                            currentAmp = 30000;
                        }
                        Snackbar.make(activity.findViewById(R.id.drawer_layout), "Sound spike trigger: " + currentAmp, Snackbar.LENGTH_LONG).show();
                        Log.i(TAG, "Triggered i amplitudetest");
                        triggered = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!triggered);
            return null;
        }
        @Override
        protected void onPostExecute(Void results){
            stopAndDelete();
            MainActivity ma = (MainActivity) activity;
            ma.setViewPagerEnabled(true);
            testAmplitudeButton.setText("Test Amplitude");
            triggered = false;
            listening = false;
        }
    }

    public void startListening() {
        if (listening) {
            Log.e(TAG, "AudioRecorder is already in use.");
        } else {
            if(checkFolder()) {
                SharedPreferences sharedPrefs = activity.getSharedPreferences(SharedPrefsHandler.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
                filePath = sharedPrefs.getString(SharedPrefsHandler.FOLDER_PATH_KEY, null);
                filename = createFileName();
                fullFilePath = filePath + "/" + filename;

                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setAudioEncodingBitRate(encodingRate);
                mRecorder.setAudioSamplingRate(samplingRate);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // three gpp or m4a? Samsung saves the file format as m4a, according to the web m4a is the new era.
                mRecorder.setOutputFile(fullFilePath);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e(TAG, "prepare() failed");
                }

                mRecorder.start();
                MainActivity ma = (MainActivity) activity;
                ma.setViewPagerEnabled(false);


                listeningSession = new ListeningSession();
                //long userID, long sessionID, String startTime, String endTime
                long userID = SharedPrefsHandler.getInstance().getUserID();
                long sessionID = userID - Math.abs(listeningSession.hashCode());
                listeningSession.setSessionID(sessionID);
                String startTime = listeningSession.getStartTime();
                String endTime = startTime;

                storeListeningSession(userID, sessionID, startTime, endTime);

                listening = true;
                paused = false;
                triggered = false;
                notifyRecordingStateChange(listening);
                startGps();
                new ListenForAudioSpike().execute(mRecorder);
            } else {
                Toast.makeText(activity, "Could not create local folder to store listening. Recording was not started.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void storeListeningSession(long userIDLong, long sessionID, String startTime, String endTime) {

        localDatabase.addListeningSession(userIDLong, sessionID, startTime, endTime);

        StoreListeningSessionService storeListeningSessionService = ServiceGenerator.createService(StoreListeningSessionService.class);
        Call<MessageResponse> storeListeningSessionCall = storeListeningSessionService.storeListeningSession(userIDLong, sessionID, startTime, endTime);
        storeListeningSessionCall.enqueue(new Callback<MessageResponse>() {

            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                MessageResponse messageResponse = response.body();
                // TODO
                if (response.isSuccessful()) {
                    Log.i("RetrofitListeningTest", "SUCCESSFUL");
                    Log.i("RetrofitListeningTest", "Response: " + messageResponse.getMessage());

                } else {
                    Log.i("RetrofitListeningTest", "NOT SUCCESSFUL");
                    Log.i("RetrofitListeningTest", "Response: " + messageResponse);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                // TODO
                Log.e("RetrofitListeningTest", "FAILED COMPLETELY");
                Log.e("RetrofitListeningTest", "Error Message: " + t.getMessage());
                Log.e("RetrofitListeningTest", "Error toString: " + t.toString());
                t.printStackTrace();
            }
        });
    }

    private void storeRecording(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String recordingEndTime = dateFormat.format(new Date());
        String recordingStartString = dateFormat.format(recordingStartTime);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(fullFilePath);
        long userID = SharedPrefsHandler.getInstance().getUserID();
        final long recordingID = userID - Math.abs(recordingStartString.hashCode());

        Log.i("TEST1", ""+recordingID);

        int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        long sessionID = listeningSession.getSessionID();

        Log.i("TEST1", ""+sessionID);
        String lastEdited = dateFormat.format(new Date());

        localDatabase.addRecording(sessionID, recordingID, "", SharedPrefsHandler.getInstance().getString(SharedPrefsHandler.FOLDER_PATH_KEY, "") + "/", "", filename, recordingStartString, recordingEndTime, duration, lastEdited);

        RequestBody recordingFile = RequestBody.create(MediaType.parse("multipart/form-data"), new File(fullFilePath));

        UploadRecordingService uploadRecordingService = ServiceGenerator.createService(UploadRecordingService.class);
        Call<UploadResponse> uploadRecordingCall = uploadRecordingService.upload(userID,
                                                                                    sessionID,
                                                                                    recordingID,
                                                                                    filename,
                                                                                    filename,
                                                                                    recordingStartString,
                                                                                    recordingEndTime,
                                                                                    duration,
                                                                                    lastEdited,
                                                                                    recordingFile);
        uploadRecordingCall.enqueue(new Callback<UploadResponse>() {

            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse uploadResponse = response.body();
                if (response.isSuccessful()) {

                    localDatabase.updateRecordingOnUpload(recordingID,
                                                            uploadResponse.getRecordingID(),
                                                            uploadResponse.getUrl(),
                                                            uploadResponse.getUploadDate());

                    Log.i("RetrofitListeningTest", "SUCCESSFUL");
                    Log.i("RetrofitListeningTest", "Response: " + uploadResponse.getMessage());

                } else {
                    Log.i("RetrofitListeningTest", "NOT SUCCESSFUL");
                    Log.i("RetrofitListeningTest", "Response: " + uploadResponse);
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                // TODO
                Log.e("RetrofitListeningTest", "FAILED COMPLETELY");
                Log.e("RetrofitListeningTest", "Error Message: " + t.getMessage());
                Log.e("RetrofitListeningTest", "Error toString: " + t.toString());
                t.printStackTrace();
            }
        });
        //Add to SQLite and MySQL
        //Upload audio file here
        Log.i(TAG, "Recording added to SQLite: " + localDatabase.getColumnData(1, localDatabase.KEY_RECORDING_ID));
    }

    private void startRecording(boolean removePrevious){
        if(removePrevious){
            stopAndDelete();
        }else{
            try{
                mRecorder.stop();
                storeRecording();
            }catch(java.lang.RuntimeException re){
                File file = new File(fullFilePath);
                file.delete();
                fullFilePath = null;
                return;
            }
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncodingBitRate(encodingRate);
        mRecorder.setAudioSamplingRate(samplingRate);
        filename = createFileName();
        fullFilePath = filePath + "/" + filename;
        Log.i(TAG, fullFilePath);
        mRecorder.setOutputFile(fullFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        mRecorder.start();
        if(removePrevious){
            Log.i(TAG, "Start listening for silence");
            new ListenForSilence().execute(mRecorder);
        }else{
            Log.i(TAG, "Start listening for spike");
            new ListenForAudioSpike().execute(mRecorder);
        }
    }

    private class ListenForSilence extends AsyncTask<MediaRecorder, Void, Void>{
        @Override
        protected Void doInBackground(MediaRecorder... recorders){
            silenceTask = this;

            getRecorderMaxAmplitude();
            do{
                waitTenSeconds();
                if(isCancelled()){
                    return null;
                }
                if(mRecorder == null || this.isCancelled() || this == null){
                    Log.i(TAG, "Recorder interrupted while listening for silence");
                    return null;
                }
                int currentAmp = getRecorderMaxAmplitude();
                if(currentAmp < (getSharedMaxAmplitude() - (getSharedMaxAmplitude()/4))){
                    Log.i(TAG, "Silence trigger: " + currentAmp);
                    Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 500};
                    if(v.hasVibrator()){
                        v.vibrate(pattern, -1);
                    }
                    triggered = false;
                    return null;
                }
            }while(triggered);
            return null;
        }

        @Override
        protected void onPostExecute(Void results){
            startRecording(false);
        }
        //Needs to be for the correct thread - could be placed in outer class?
        private void waitTenSeconds(){
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e)
            {
                Log.d(TAG, "interrupted");
            }
        }
    }

    private class ListenForAudioSpike extends AsyncTask<MediaRecorder, Void, Void>{

        @Override
        protected Void doInBackground(MediaRecorder... recorders) {
            spikeTask = this;

            getRecorderMaxAmplitude();

            do{
                waitOneSecond();
                if(isCancelled()){
                    return null;
                }
                if(mRecorder == null || this.isCancelled() || this == null){
                    Log.i(TAG, "Recorder interrupted while listening for spike");
                    return null;
                }

                int currentAmp = getRecorderMaxAmplitude();
                if(currentAmp > getSharedMaxAmplitude()){
                    Log.i(TAG, "Sound spike trigger: " + currentAmp);
                    Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 100, 50, 100};
                    if(v.hasVibrator()){
                        v.vibrate(pattern, -1);
                    }
                    triggered = true;
                    recordingStartTime = new Date();
                    return null;
                }
            }while(!triggered);
            return null;
        }

        @Override
        protected void onPostExecute(Void results){
            startRecording(true);
        }

        private void waitOneSecond(){
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                Log.d(TAG, "interrupted");
            }
        }
    }

    private int getRecorderMaxAmplitude(){
        if(mRecorder != null){
            return mRecorder.getMaxAmplitude();
        }else{
            return 0;
        }
    }

    private void notifyRecordingStateChange(boolean recording){
        NotificationHandler.notifyRecordingChange(recording, activity);
    }

    private void stopAndDelete(){
        try{
            mRecorder.stop();
        }catch(java.lang.RuntimeException re){
            File file = new File(fullFilePath);
            file.delete();
            fullFilePath = null;
            return;
        }
        File file = new File(fullFilePath);
        file.delete();
        fullFilePath = null;
    }

    public void stopListening() {
        if (!listening) {
            Log.e(TAG, "Must start listening before stoppping.");
        } else {
            if(spikeTask != null){
                spikeTask.cancel(true);
            }
            if(silenceTask != null){
                silenceTask.cancel(true);
            }
            if(!triggered){
                stopAndDelete();
            } else{
                try{
                    mRecorder.stop();
                    storeRecording();
                }catch(java.lang.RuntimeException re){
                    File file = new File(fullFilePath);
                    file.delete();
                    fullFilePath = null;
                    return;
                }
            }
            listeningSession.finalize();

            String newEndTime = listeningSession.getEndTime();
            localDatabase.updateListeningSessionEndTime(listeningSession.getSessionID(), newEndTime);

            UpdateListeningSessionService updateListeningSessionService = ServiceGenerator.createService(UpdateListeningSessionService.class);
            Call<MessageResponse> updateListeningSessionCall = updateListeningSessionService.updateListeningSession(listeningSession.getSessionID(), newEndTime);
            updateListeningSessionCall.enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    MessageResponse messageResponse = response.body();
                    if (response.isSuccessful()) {
                        Log.i("RetrofitListeningTest", "SUCCESSFUL");
                        Log.i("RetrofitListeningTest", "Response: " + messageResponse.getMessage());

                    } else {
                        Log.i("RetrofitListeningTest", "NOT SUCCESSFUL");
                        Log.i("RetrofitListeningTest", "Response: " + messageResponse);
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    Log.e("RetrofitListeningUpdate", "FAILED COMPLETELY");
                    Log.e("RetrofitListeningUpdate", "Error Message: " + t.getMessage());
                    Log.e("RetrofitListeningUpdate", "Error toString: " + t.toString());
                    t.printStackTrace();
                }
            });

            mRecorder.release();
            mRecorder = null;
            filePath = null;
            fullFilePath = null;
            filename = null;
            listening = false;
            paused = false;
            stopGps();
            notifyRecordingStateChange(listening);
            MainActivity ma = (MainActivity) activity;
            ma.setViewPagerEnabled(true);
        }
    }

    private Boolean checkFolder() {
        SharedPreferences sharedPrefs = activity.getSharedPreferences(SharedPrefsHandler.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
        File folder = new File(sharedPrefs.getString(SharedPrefsHandler.FOLDER_PATH_KEY, null));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        } else {
            return true;
        }

        if (!success) {
            Log.e(TAG, "File.mkdir() was unsuccessful. Could not create local folder.");
        }

        return success;
    }

    private String createFileName() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        String dateString = dateFormat.format(date);

        return activity.getString(R.string.recording_file_name_base) + "_" + dateString + ".m4a";
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered){
        this.triggered = triggered;
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFullFilePath() {
        return fullFilePath;
    }

    public void setFullFilePath(String fullFilePath) {
        this.fullFilePath = fullFilePath;
    }

    private void startGps() {
        tracker = new GPSTracker(listeningSession, localDatabase, activity);
    }

    private void stopGps(){
        tracker.stopGps();
    }
}
