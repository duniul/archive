package exarbete.listeningapp.retrofit;

/**
 * Created by Daniel on 2016-05-04.
 */
public class RowListeningSession {
    long userID;
    long sessionID;
    String startTime;
    String endTime;

    public RowListeningSession(long userID, long sessionID, String startTime, String endTime) {
        this.userID = userID;
        this.sessionID = sessionID;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getUserID() {
        return userID;
    }

    public long getSessionID() {
        return sessionID;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
