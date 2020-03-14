package exarbete.listeningapp.retrofit;

/**
 * Created by Daniel on 2016-05-04.
 */
public class RowPosition {
    long sessionID;
    String datetime;
    double latitude;
    double longitude;

    public RowPosition(long sessionID, String datetime, double latitude, double longitude) {
        this.sessionID = sessionID;
        this.datetime = datetime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getSessionID() {
        return sessionID;
    }

    public String getDatetime() {
        return datetime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}