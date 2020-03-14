package exarbete.listeningapp.recording;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import exarbete.listeningapp.R;
import exarbete.listeningapp.SharedPrefsHandler;
import exarbete.listeningapp.database.SQLiteHelper;
import exarbete.listeningapp.retrofit.MessageResponse;
import exarbete.listeningapp.retrofit.ServiceGenerator;
import exarbete.listeningapp.retrofit.StorePositionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by svett_000 on 04/05/2016.
 */
public class GPSTracker {

    private final String TAG = "GPSTracker";
    private StorePositionService storePositionService = ServiceGenerator.createService(StorePositionService.class);
    private ListeningSession listeningSession = null;
    private SQLiteHelper localDatabase = null;
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private String locationProvider = null;
    private Activity activity = null;

    public GPSTracker(ListeningSession listeningSession, SQLiteHelper localDatabase, Activity activity){
        this.listeningSession = listeningSession;
        this.localDatabase = localDatabase;
        this.activity = activity;
        startGPS();
    }

    private void startGPS(){
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            // Whenever the location is updated, this will be done.
            @Override
            public void onLocationChanged(Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                long timeMillis = location.getTime();

                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                        new Date(timeMillis)
                );

                Position p = new Position(longitude,latitude,time);

                long userID = SharedPrefsHandler.getInstance().getUserID();
                long sessionID = userID - Math.abs(listeningSession.hashCode());

                localDatabase.addPosition(sessionID, time, p.getLatitude(), p.getLongitude());
                // TODO DATABASE UPLOAD
                Call<MessageResponse> storePositionCall = storePositionService.storePosition(sessionID, p.getTime(), p.getLatitude(), p.getLongitude());
                storePositionCall.enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                        MessageResponse messageResponse = response.body();
                        if (response.isSuccessful()) {
                            Log.i("StorePositionCall", "SUCCESSFUL");
                            Log.i("StorePositionCall", "Response: " + messageResponse.getMessage());
                        } else {
                            Log.i("StorePositionCall", "NOT SUCCESSFUL");
                            Log.i("StorePositionCall", "Response: " + messageResponse);
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        Log.e("StorePositionCall", "FAILED COMPLETELY");
                        Log.e("StorePositionCall", "Error Message: " + t.getMessage());
                        Log.e("StorePositionCall", "Error toString: " + t.toString());
                        t.printStackTrace();
                    }
                });

                Snackbar.make(activity.findViewById(R.id.drawer_layout), p.toString(), Snackbar.LENGTH_LONG).show();
                Log.d(TAG, p.toString());
            }

            // Not interesting..
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            // Not interesting..
            @Override
            public void onProviderEnabled(String provider) {

            }

            // Checks if the GPS is off. Intent to to enable GPS if it's disabled.
            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        };
        // Parameter for requestLocationUpdates: GPS Provider / Network provider , Location for each 10 Milliseconds, Location for each meter from last known location, Listener.
        // Or use this insteaf of GPS Provider : LocationManager.NETWORK_PROVIDER;
        locationProvider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(locationProvider, 5000, 0, locationListener);

        // This part I had to include, any chance of doing it in another way?
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
    }

    public void stopGps(){
        // This part I had to include, any chance of doing it in another way?
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }

        locationManager.removeUpdates(locationListener);
    }
}
