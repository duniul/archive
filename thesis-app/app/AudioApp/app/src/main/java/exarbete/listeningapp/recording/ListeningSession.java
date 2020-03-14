package exarbete.listeningapp.recording;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Axel Nauclér on 29/04/2016.
 */
public class ListeningSession {

    private long sessionID;
    private String startTime;
    private String endTime;
    private int numberOfRecordings;

    public ListeningSession(){
        startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                new Date(new Date().getTime())
        );
    }

    public ListeningSession(long sessionID, String startTime, String endTime, int numberOfRecordings) {
        this.sessionID = sessionID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfRecordings = numberOfRecordings;
    }

    public void finalize(){
        endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                new Date(new Date().getTime())
        );
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public int hashCode() {
        return startTime != null ? startTime.hashCode() : 0;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public long getSessionID() {
        return sessionID;
    }

    public int getNumberOfRecordings() {
        return numberOfRecordings;
    }

    public void setNumberOfRecordings(int numberOfRecordings) {
        this.numberOfRecordings = numberOfRecordings;
    }
}
