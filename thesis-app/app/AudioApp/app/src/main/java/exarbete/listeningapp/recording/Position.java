package exarbete.listeningapp.recording;

/**
 * Created by Baris Akdag on 2016-04-27.
 */
public class Position {


    private double longitude;
    private double latitude;
    private String time;

    public Position(double longitude, double latitude, String time){
        this.longitude = longitude;
        this.latitude=latitude;
        this.time=time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    @Override
    public String toString(){
        return "Latitude: "+ latitude + " Longitude: " + longitude + " Time: " + time;
    }
}


