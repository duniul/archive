package exarbete.listeningapp;

import android.app.Application;


/**
 * This class only runs once during each launch. In order for it to run, the user needs to quit
 * the apps process and restart it.
 * <p>
 * Created by Daniel on 2016-04-11.
 */
public class SubApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Checks if this is the first time the app is launched since installation,
        // if true it installs the default preferences.
        SharedPrefsHandler.initialize(getApplicationContext());
        SharedPrefsHandler sharedPrefsHandler = SharedPrefsHandler.getInstance();
        if (sharedPrefsHandler.isFirstRun()) {
            sharedPrefsHandler.putDefaultPreferences();
        }
    }
}