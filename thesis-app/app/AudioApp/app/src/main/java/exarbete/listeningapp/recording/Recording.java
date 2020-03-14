package exarbete.listeningapp.recording;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daniel on 2016-04-11.
 */
public class Recording {

    private MediaMetadataRetriever mmr;
    private long recordingID;
    private String filePath;
    private String url;
    private String name;
    private String date;
    private int duration;

    public Recording(String filePath) {
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);

        this.filePath = filePath;
        this.name = filePath.substring(filePath.lastIndexOf("/") + 1);
        File file = new File(filePath);
        this.date = formatDate(file.lastModified());
        this.duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        mmr.release();
    }

    public Recording(long recordingID, String filePath, String url, String name, String date, int duration) {
        this.recordingID = recordingID;
        this.filePath = filePath;
        this.url = url;
        this.name = name;
        this.date = date;
        this.duration = duration;
    }

    private String formatDate(long lastModified) {
       return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                new Date(lastModified)
        );
    }

    public String formatDuration(int milliseconds) {
        String durationFormatted = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

        return durationFormatted;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getRecordingID() {
        return recordingID;
    }

    public void setRecordingID(long recordingID) {
        this.recordingID = recordingID;
    }
}
