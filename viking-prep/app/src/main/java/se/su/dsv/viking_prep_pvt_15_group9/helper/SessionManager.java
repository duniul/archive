package se.su.dsv.viking_prep_pvt_15_group9.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Den här klassen hanterar användarens inloggningssession i applikationen. En session startas
 * när användaren loggar in, och avslutas när användaren loggar ut.
 *
 * @author Daniel
 */
public class SessionManager {

    // LogCat-tagg
    private static String LOGTAG = SessionManager.class.getSimpleName();

    // Shared preferences
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefEditor;
    private Context mContext;

    // Filnamn för de delade inställningarna
    private static final String PREF_FILE_NAME = "VikingPrepAccount";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // Konstruktor
    public SessionManager(Context pContext) {

        this.mContext = pContext;
        sharedPrefs = mContext.getSharedPreferences(PREF_FILE_NAME, mContext.MODE_PRIVATE);
        prefEditor = sharedPrefs.edit();
    }

    /**
     * Sätter en användares login-status (inloggad/utloggad) och commitar ändringen.
     */
    public void setLoggedIn(boolean isLoggedIn) {

        prefEditor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        prefEditor.commit();

        Log.d(LOGTAG, "Den inloggade sessionen har ändrats.");
    }

    /**
     * Returnerar en boolean som visar om en användare är inloggad (dvs. att en session är aktiv)
     * eller inte.
     */
    public boolean isLoggedIn(){

        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}