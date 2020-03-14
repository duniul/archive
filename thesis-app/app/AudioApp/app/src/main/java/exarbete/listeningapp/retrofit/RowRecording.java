package exarbete.listeningapp.retrofit;

/**
 * Created by Daniel on 2016-05-04.
 */
public class RowRecording {
    long sessionID;
    long recordingID;
    String url;
    String uploadDate;
    String userFilename;
    String recordingStartDate;
    String recordingEndDate;
    int duration;
    String lastEdited;

    public RowRecording(long sessionID, long recordingID, String url, String uploadDate, String userFilename, String recordingStartDate, String recordingEndDate, int duration, String lastEdited) {
        this.sessionID = sessionID;
        this.recordingID = recordingID;
        this.url = url;
        this.uploadDate = uploadDate;
        this.userFilename = userFilename;
        this.recordingStartDate = recordingStartDate;
        this.recordingEndDate = recordingEndDate;
        this.duration = duration;
        this.lastEdited = lastEdited;
    }

    public long getSessionID() {
        return sessionID;
    }

    public long getRecordingID() {
        return recordingID;
    }

    public String getUrl() {
        return url;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public String getUserFilename() {
        return userFilename;
    }

    public String getRecordingStartDate() {
        return recordingStartDate;
    }

    public String getRecordingEndDate() {
        return recordingEndDate;
    }

    public int getDuration() {
        return duration;
    }

    public String getLastEdited() {
        return lastEdited;
    }
}