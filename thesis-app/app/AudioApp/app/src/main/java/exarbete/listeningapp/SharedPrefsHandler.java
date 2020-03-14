package exarbete.listeningapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class SharedPrefsHandler {

    public static final String PREFERENCES_FILE_KEY = "exarbete.audioapp.sharedprefs";

    public static final String FIRST_RUN_KEY = "firstRun";
    public static final String FOLDER_PATH_KEY = "folderName";

    public static final String USER_LOGGED_IN_KEY = "userLoggedIn";
    public static final String USER_GOOGLE_ID_KEY = "userGoogleId";
    public static final String USER_GOOGLE_NAME_KEY = "userGoogleDisplayName";
    public static final String USER_GOOGLE_EMAIL_KEY = "userGoogleEmail";
    public static final String USER_GOOGLE_PICTURE_URL_KEY = "userGoogleURL";
    public static final String USER_ID_KEY = "userId";
    public static final String USER_MAX_AMPLITUDE_KEY = "12000";


    private static SharedPrefsHandler sharedPrefsHandlerInstance;
    private static SharedPreferences sharedPrefs;
    private Context context;

    private SharedPrefsHandler(Context context) {
        this.context = context;
        sharedPrefs = context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
    }

    public static synchronized void initialize(Context context) {
        if (sharedPrefsHandlerInstance == null) {
            sharedPrefsHandlerInstance = new SharedPrefsHandler(context);
        }
    }

    public static synchronized SharedPrefsHandler getInstance() {
        if (sharedPrefsHandlerInstance == null) {
            throw new IllegalStateException(SharedPrefsHandler.class.getSimpleName() +
                    " is not initialized, must call initialize(..) method first.");
        }
        return sharedPrefsHandlerInstance;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPrefs;
    }

    public String getString(String key, String defaultValue) {
        return sharedPrefs.getString(key, defaultValue);
    }

    public boolean isUserLoggedIn() {
        return sharedPrefs.getBoolean(USER_LOGGED_IN_KEY, false);
    }

    public boolean isFirstRun() {
        return sharedPrefs.getBoolean(FIRST_RUN_KEY, true);
    }

    public void putDefaultPreferences() {
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();

        // Sets default folder to store local recordings.
        prefEditor.putString(FOLDER_PATH_KEY, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getString(R.string.app_name));

        // Marks user as not logged in.
        prefEditor.putBoolean(USER_LOGGED_IN_KEY, false);

        // Sets first run to false.
        prefEditor.putBoolean(FIRST_RUN_KEY, false);
        prefEditor.apply();
    }

    public void putUserDetails(String googleID, String userName, String userEmail, String userPictureURL) {
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();

        // Stores user details and marks user as logged in.
        prefEditor.putBoolean(USER_LOGGED_IN_KEY, true);
        prefEditor.putString(USER_GOOGLE_ID_KEY, googleID);
        prefEditor.putString(USER_GOOGLE_NAME_KEY, userName);
        prefEditor.putString(USER_GOOGLE_EMAIL_KEY, userEmail);
        if (userPictureURL != null) {
            prefEditor.putString(USER_GOOGLE_PICTURE_URL_KEY, userPictureURL);
        } else {
            prefEditor.putString(USER_GOOGLE_PICTURE_URL_KEY, "");
        }

        prefEditor.apply();
    }

    public void putUserID(long userID) {
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();

        // Stores user details and marks user as logged in.
        prefEditor.putLong(USER_ID_KEY, userID);

        prefEditor.apply();
    }

    public void removeUserDetails() {
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();

        // Removes user details and marks user as logged out.
        prefEditor.putBoolean(USER_LOGGED_IN_KEY, false);
        prefEditor.remove(USER_GOOGLE_ID_KEY);
        prefEditor.remove(USER_GOOGLE_NAME_KEY);
        prefEditor.remove(USER_GOOGLE_EMAIL_KEY);
        prefEditor.remove(USER_GOOGLE_PICTURE_URL_KEY);

        prefEditor.apply();
    }

    //   användaren sätter maxamplitude
    public void setUserMaxAmplitudeKey(int amplitude){
        SharedPreferences.Editor prefEditor = sharedPrefs.edit();
        prefEditor.putInt(USER_MAX_AMPLITUDE_KEY, amplitude);

    }

    public long getUserID() {
        return sharedPrefs.getLong(USER_ID_KEY, -1);
    }
}