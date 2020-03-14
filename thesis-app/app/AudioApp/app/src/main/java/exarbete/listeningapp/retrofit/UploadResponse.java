package exarbete.listeningapp.retrofit;

/**
 * Created by Daniel on 2016-05-03.
 */
public class UploadResponse {

    private String tag;
    private boolean error;
    private String message;
    private long recordingID;
    private String url;
    private String uploadDate;

    public UploadResponse(String tag, boolean error, String message, long recordingID, String url, String uploadDate, String lastEdited) {
        this.tag = tag;
        this.error = error;
        this.message = message;
        this.recordingID = recordingID;
        this.url = url;
        this.uploadDate = uploadDate;
    }

    public String getTag() {
        return tag;
    }

    public boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
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
}
