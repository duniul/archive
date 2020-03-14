package exarbete.listeningapp.retrofit;

import java.util.List;

/**
 * Created by Daniel on 2016-05-04.
 */
public class SyncLocalDatabaseResponse {

    private String tag;
    private boolean error;
    private String message;
    private long userID;
    private List<RowListeningSession> listeningSessionsRows;
    private List<RowRecording> recordingsRows;
    private List<RowPosition> positionsRows;

    public SyncLocalDatabaseResponse(long userID, List<RowListeningSession> listeningSessionsRows, List<RowRecording> recordingsRows, List<RowPosition> positionsRows) {
        this.userID = userID;
        this.listeningSessionsRows = listeningSessionsRows;
        this.recordingsRows = recordingsRows;
        this.positionsRows = positionsRows;
    }

    public String getTag() {
        return tag;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public long getUserID() {
        return userID;
    }

    public List<RowListeningSession> getListeningSessionsRows() {
        return listeningSessionsRows;
    }

    public List<RowRecording> getRecordingsRows() {
        return recordingsRows;
    }

    public List<RowPosition> getPositionsRows() {
        return positionsRows;
    }
}
