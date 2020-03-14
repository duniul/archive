package se.su.dsv.viking_prep_pvt_15_group9.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Den här klassen hanterar applikationens lokala databas med hjälp av SQLite.
 * Extendar hjälpklassen SQLiteOpenHelper.
 *
 * @author Daniel
 */
public class SQLiteManager extends SQLiteOpenHelper {

    // LogCat-tagg
    private static final String LOGTAG = SQLiteManager.class.getSimpleName();

    private SQLiteDatabase database;

    // Statiska variabler för databasens version, namn, tillhörande tabeller och tabellernas kolumner.
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "VikingPrepLocal";

    public static final String TABLE_CURRENT_USER = "current_user";
    public static final String TABLE_SELECTED_USER = "selected_user";
    public static final String TABLE_MATCHMAKING_INFO = "matchmaking_info";

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_UNIQUE_ID = "unique_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME = "surname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_AREA = "area";
    public static final String KEY_CITY = "city";
    public static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    public static final String KEY_SEX = "sex";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_UPDATED_AT = "updated_at";
    public static final String KEY_PICTURE_URL = "picture_url";

    public static final String KEY_SESSIONS_PER_WEEK = "sessions_per_week";
    public static final String KEY_PREF_SEX = "pref_sex";
    public static final String KEY_PREF_MIN_AGE = "pref_min_age";
    public static final String KEY_PREF_MAX_AGE = "pref_max_age";

    // Konstruktor
    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Metoden körs första gången databasen skapas, och skapar då de nödvändiga tabellerna.
     * @param db SQLite-databasen som tabellen ska läggas in i.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // En sträng som motsvarar ett CREATE TABLE-kommando i SQLite.
        String currentUserTableQuery = "CREATE TABLE " + TABLE_CURRENT_USER + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY,"
                + KEY_UNIQUE_ID + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_SURNAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_AREA + " TEXT,"
                + KEY_CITY + " TEXT,"
                + KEY_DATE_OF_BIRTH + " TEXT,"
                + KEY_SEX + " TEXT,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_UPDATED_AT + " TEXT,"
                + KEY_PICTURE_URL + " TEXT" + ")";
        db.execSQL(currentUserTableQuery);

        String selectedUserTableQuery = "CREATE TABLE " + TABLE_SELECTED_USER + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY,"
                + KEY_UNIQUE_ID + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_SURNAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_AREA + " TEXT,"
                + KEY_CITY + " TEXT,"
                + KEY_DATE_OF_BIRTH + " TEXT,"
                + KEY_SEX + " TEXT,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_UPDATED_AT + " TEXT,"
                + KEY_PICTURE_URL + " TEXT" + ")";
        db.execSQL(selectedUserTableQuery);

        String matchmakingInfoTableQuery = "CREATE TABLE " + TABLE_MATCHMAKING_INFO + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY,"
                + KEY_SESSIONS_PER_WEEK + " INTEGER,"
                + KEY_PREF_SEX + " TEXT,"
                + KEY_PREF_MIN_AGE + " INTEGER,"
                + KEY_PREF_MAX_AGE + " INTEGER" + ")";
        db.execSQL(matchmakingInfoTableQuery);

        Log.d(LOGTAG, "Database tables created");
    }

    /**
     * Metoden som körs när SQLite-databasen uppgraderas till en ny version.
     * @param db SQLite-databasen som tabellen ska läggas in i.
     * @param newVersion Värdet på den nya databasversionen.
     * @param oldVersion Värdet på den gamla databasversionen.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Tar bort/droppar tabeller som existerar i den gamla tabellen.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENT_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SELECTED_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATCHMAKING_INFO);

        // Återskapar sedan tabellerna.
        onCreate(db);
    }

    public SQLiteDatabase getDatabase() {
        return this.getWritableDatabase();
    }

    public String getTableName(int tableNumber) {
        if (tableNumber == 1) {
            return TABLE_CURRENT_USER;
        } else {
            return TABLE_SELECTED_USER;
        }
    }

    /**
     * Används för att lägga till en användare i den lokala SQLite-databasen (inte MySQL-databasen).
     * @param uniqueID Användarens unika ID från huvuddatabasen.
     * @param name Användarens namn.
     * @param surname Användarens efternamn.
     * @param email Användarens email.
     * @param sex Användarens kön.
     * @param dateOfBirth Användarens födelsedatum.
     * @param createdAt Tiden som användarkontot skapades.
     * @param updatedAt Tiden då användarkontot senast uppdaterades/modifierades.
     */
    public void addUser(int table, int userID, String uniqueID, String name, String surname, String email, String area, String city, String dateOfBirth, String sex, String createdAt, String updatedAt, String pictureUrl) {

        // Hämtar applikationens lokala databas.
        database = this.getWritableDatabase();

        // Sparar parametervärdena och mappar dom till motsvarande kolumn i tabellen.
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, userID);
        values.put(KEY_UNIQUE_ID, uniqueID);
        values.put(KEY_NAME, name);
        values.put(KEY_SURNAME, surname);
        values.put(KEY_EMAIL, email);
        values.put(KEY_AREA, area);
        values.put(KEY_CITY, city);
        values.put(KEY_DATE_OF_BIRTH, dateOfBirth);
        values.put(KEY_SEX, sex);
        values.put(KEY_CREATED_AT, createdAt);
        values.put(KEY_UPDATED_AT, updatedAt);
        values.put(KEY_PICTURE_URL, pictureUrl);

        database.insert(getTableName(table), null, values);

        Log.d(LOGTAG, "Ny användare " + email + " har lagts till i SQLite-databasen.");
    }

    /**
     * Återskapar databasen och raderar allt som finns i tabellen för nuvarande användare.
     * */
    public void deleteUser(int table) {
        // Hämtar applikationens lokala databas.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tar bort alla rader i tabellen current_user.
        db.delete(getTableName(table), null, null);
        db.close();

        Log.d(LOGTAG, "Tog bort nuvarande användare från SQLite!");
    }

    public String getColumnData(int table, String column) {
        String columnData = "";
        String selectColumnQuery = "SELECT " + column + " FROM " + getTableName(table);
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectColumnQuery, null);
        cursor.moveToFirst();
        columnData = cursor.getString(0);
        cursor.close();

        return columnData;
    }

    /**
     * Kollar om det finns en användare sparad i tabellen current_user.
     * */
    public boolean isUserStored(int table) {
        boolean isStored;
        String selectAllQuery = "SELECT  * FROM " + getTableName(table);
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectAllQuery, null);

        if (cursor.getCount() == 0) {
            isStored = false;
        } else {
            isStored = true;
        }

        cursor.close();

        return isStored;
    }

    public void updateUserDetails(int table, String email, String name, String surname, String area, String city, String dateOfBirth, String sex, String updatedAt) {
        // Hämtar applikationens lokala databas.
        database = this.getWritableDatabase();

        // Sparar parametervärdena och mappar dom till motsvarande kolumn i tabellen.
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_SURNAME, surname);
        values.put(KEY_AREA, area);
        values.put(KEY_CITY, city);
        values.put(KEY_DATE_OF_BIRTH, dateOfBirth);
        values.put(KEY_SEX, sex);
        values.put(KEY_UPDATED_AT, updatedAt);

        // Uppdaterar värdena i tabellen. Till "id" returneras ett värde som motsvarar raden i
        // databasen som användaren lagts på, eller -1 om det uppstod ett fel.
        database.update(getTableName(table), values, null, null);

        Log.d(LOGTAG, "Användare " + email + " har uppdaterats i SQLite-databasen.");

    }

    public HashMap<String, String> getUserDetails(int table) {
        HashMap<String, String> currentUserDetails = new HashMap<String, String>();
        String selectUserQuery = "SELECT email, name, surname, area, city, date_of_birth, sex FROM " + getTableName(table);
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectUserQuery, null);
        cursor.moveToFirst();

        currentUserDetails.put("email", cursor.getString(0));
        currentUserDetails.put("name", cursor.getString(1));
        currentUserDetails.put("surname", cursor.getString(2));
        currentUserDetails.put("area", cursor.getString(3));
        currentUserDetails.put("city", cursor.getString(4));
        currentUserDetails.put("date_of_birth", cursor.getString(5));
        currentUserDetails.put("sex", cursor.getString(6));

        cursor.close();

        return currentUserDetails;
    }

    public int getUserID(int table) {
        return Integer.parseInt(getColumnData(table, KEY_USER_ID));
    }

    public String getUniqueID(int table) {
        return getColumnData(table, KEY_UNIQUE_ID);
    }

    public String getFullName(int table) {
        String currentUserFullName = "";
        String selectFullNameQuery = "SELECT name, surname FROM " + getTableName(table);
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectFullNameQuery, null);
        cursor.moveToFirst();
        currentUserFullName = cursor.getString(0) + " " + cursor.getString(1);
        cursor.close();

        return currentUserFullName;
    }

    public String getName(int table) {
        return getColumnData(table, KEY_NAME);
    }

    public String getSurname(int table) {
        return getColumnData(table, KEY_SURNAME);
    }

    public String getEmail(int table) {
        return getColumnData(table, KEY_EMAIL);
    }

    public String getAge(int table) {
        Calendar calToday = Calendar.getInstance();
        Calendar calDateOfBirth = Calendar.getInstance();
        int currentUserAge;
        int year;
        int month;
        int day;

        String selectAgeQuery = "SELECT date_of_birth FROM " + getTableName(table);
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectAgeQuery, null);
        cursor.moveToFirst();

        year = Integer.parseInt(cursor.getString(0).substring(0,4));
        month = Integer.parseInt(cursor.getString(0).substring(5,7));
        day = Integer.parseInt(cursor.getString(0).substring(8));
        calDateOfBirth.set(year, month, day);

        cursor.close();

        currentUserAge = calToday.get(Calendar.YEAR) - calDateOfBirth.get(Calendar.YEAR);
        if (calToday.get(Calendar.DAY_OF_YEAR) >= calDateOfBirth.get(Calendar.DAY_OF_YEAR)) {
            currentUserAge--;
        }

        return Integer.toString(currentUserAge);
    }

    public String getLocation(int table) {
        String currentUserLocation = "";
        String selectFullNameQuery = "SELECT area, city FROM " + getTableName(table);
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectFullNameQuery, null);
        cursor.moveToFirst();
        currentUserLocation = cursor.getString(0) + ", " + cursor.getString(1);
        cursor.close();

        return currentUserLocation;
    }

    public void setUpdatedAt(int table, String email, String updatedAt) {
        // Hämtar applikationens lokala databas.
        database = this.getWritableDatabase();

        // Sparar parametervärdena och mappar dom till motsvarande kolumn i tabellen.
        ContentValues values = new ContentValues();
        values.put(KEY_UPDATED_AT, updatedAt);

        // Uppdaterar värdena i tabellen.
        database.update(getTableName(table), values, null, null);

        Log.d(LOGTAG, "Användare " + email + " har uppdaterats i SQLite-databasen.");
    }

    public void setEmail (int table, String email, String updatedAt) {
        // Hämtar applikationens lokala databas.
        database = this.getWritableDatabase();
        String oldEmail = getEmail(table);

        // Sparar parametervärdena och mappar dom till motsvarande kolumn i tabellen.
        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email);
        values.put(KEY_UPDATED_AT, updatedAt);

        // Uppdaterar värdena i tabellen.
        database.update(getTableName(table), values, null, null);

        Log.d(LOGTAG, "E-postbytet: " + oldEmail + " till " + email + " har lagts till i SQLite-databasen.");
    }

    public HashMap<String, String> getMatchmakingInfo() {
        HashMap<String, String> matchmakingInfo = new HashMap<String, String>();
        String selectUserQuery = "SELECT * FROM " + TABLE_MATCHMAKING_INFO;
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectUserQuery, null);
        cursor.moveToFirst();

        matchmakingInfo.put("sessions_per_week", cursor.getString(1));
        matchmakingInfo.put("pref_sex", cursor.getString(2));
        matchmakingInfo.put("pref_min_age", cursor.getString(3));
        matchmakingInfo.put("pref_max_age", cursor.getString(4));

        cursor.close();

        return matchmakingInfo;
    }

    public void setMatchmakingInfo(int sessionsPerWeek, String prefSex, int minAge, int maxAge) {
        // Hämtar applikationens lokala databas.
        database = this.getWritableDatabase();

        // Sparar parametervärdena och mappar dom till motsvarande kolumn i tabellen.
        ContentValues values = new ContentValues();
        values.put(KEY_SESSIONS_PER_WEEK, sessionsPerWeek);
        values.put(KEY_PREF_SEX, prefSex);
        values.put(KEY_PREF_MIN_AGE, minAge);
        values.put(KEY_PREF_MAX_AGE, maxAge);

        database.insert(TABLE_MATCHMAKING_INFO, null, values);

        Log.d(LOGTAG, "Nya matchmakingvärden har lagts till i SQLite-databasen.");
    }

    public int getMatchmakingPrefMinAge() {
        int prefMinAge = 0;
        String selectMinAgeQuery = "SELECT " + KEY_PREF_MIN_AGE + " FROM " + TABLE_MATCHMAKING_INFO;
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectMinAgeQuery, null);
        cursor.moveToFirst();
        prefMinAge = cursor.getInt(0);
        cursor.close();

        return prefMinAge;
    }

    public int getMatchmakingPrefMaxAge() {
        int prefMaxAge = 0;
        String selectMaxAgeQuery = "SELECT " + KEY_PREF_MAX_AGE + " FROM " + TABLE_MATCHMAKING_INFO;
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectMaxAgeQuery, null);
        cursor.moveToFirst();
        prefMaxAge = cursor.getInt(0);
        cursor.close();

        return prefMaxAge;
    }

    public void deleteStoredMatchmakingInfo() {
        // Hämtar applikationens lokala databas.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tar bort alla rader i tabellen current_user.
        db.delete(TABLE_MATCHMAKING_INFO, null, null);
        db.close();

        Log.d(LOGTAG, "Rensade matchmaking-info från SQLite!");
    }

    public boolean isMatchmakingInfoStored() {
        boolean isStored;
        String selectAllQuery = "SELECT  * FROM " + TABLE_MATCHMAKING_INFO;
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectAllQuery, null);

        if (cursor.getCount() == 0) {
            isStored = false;
        } else {
            isStored = true;
        }

        cursor.close();

        return isStored;
    }

    public String getPictureUrl(int table) {
        return getColumnData(table, KEY_PICTURE_URL);
	}

    public void setPictureUrl(int table, String pictureUrl) {
        // Hämtar applikationens lokala databas.
        database = this.getWritableDatabase();

        // Sparar parametervärdena och mappar dom till motsvarande kolumn i tabellen.
        ContentValues values = new ContentValues();
        values.put(KEY_PICTURE_URL, pictureUrl);

        // Uppdaterar värdena i tabellen.
        database.update(getTableName(table), values, null, null);

        Log.d(LOGTAG, "Profilbildslänken " + pictureUrl + " har lagts till i SQLite-databasen.");
    }
}
