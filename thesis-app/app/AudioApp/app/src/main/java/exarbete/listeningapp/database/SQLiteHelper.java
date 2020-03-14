package exarbete.listeningapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import exarbete.listeningapp.SharedPrefsHandler;
import exarbete.listeningapp.recording.ListeningSession;
import exarbete.listeningapp.recording.Recording;
import exarbete.listeningapp.retrofit.RowListeningSession;
import exarbete.listeningapp.retrofit.RowPosition;
import exarbete.listeningapp.retrofit.RowRecording;

/**
 * Created by svett_000 on 29/04/2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper{

    private static final String TAG = SQLiteHelper.class.getSimpleName();

    //Variables for columns and values
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "AudioAppLocal";

    public static final String TABLE_LISTENING_SESSIONS = "listeningSessions";
    public static final String TABLE_RECORDINGS = "recordings";
    public static final String TABLE_POSITIONS = "positions";

    //listeningSessions
    public static final String KEY_USER_ID = "userID";
    public static final String KEY_SESSION_ID = "sessionID";
    public static final String KEY_START_TIME = "startTime";
    public static final String KEY_END_TIME = "endTime";

    //recordings
    public static final String KEY_RECORDING_ID = "recordingId";
    public static final String KEY_FILE_PATH = "filePath";
    public static final String KEY_URL = "url";
    public static final String KEY_UPLOAD_DATE = "uploadDate";
    public static final String KEY_USER_FILENAME = "userFilename";
    public static final String KEY_RECORDING_START_DATE = "recordingStartDate";
    public static final String KEY_RECORDING_END_DATE = "recordingEndDate";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_LAST_EDITED = "lastEdited";

    //positions
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_DATETIME = "datetime";

    private static SQLiteHelper instance;
    private static SQLiteDatabase database;

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new SQLiteHelper(context);
        }
    }

    public static synchronized SQLiteHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(TAG +
                    " is not initialized, must call initialize(..) method first.");
        }

        return instance;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String listeningSessionsQuery = "CREATE TABLE " + TABLE_LISTENING_SESSIONS + "("
                + KEY_USER_ID + " INTEGER,"
                + KEY_SESSION_ID + " INTEGER PRIMARY KEY,"
                + KEY_START_TIME + " TEXT,"
                + KEY_END_TIME + " TEXT" + ")";
        db.execSQL(listeningSessionsQuery);

        String recordingsQuery = "CREATE TABLE " + TABLE_RECORDINGS + "("
                + KEY_SESSION_ID + " INTEGER,"
                + KEY_RECORDING_ID + " INTEGER PRIMARY KEY,"
                + KEY_URL + " TEXT,"
                + KEY_FILE_PATH + " TEXT,"
                + KEY_UPLOAD_DATE + " TEXT,"
                + KEY_USER_FILENAME + " TEXT,"
                + KEY_RECORDING_START_DATE + " TEXT,"
                + KEY_RECORDING_END_DATE + " TEXT,"
                + KEY_DURATION + " INTEGER,"
                + KEY_LAST_EDITED + " TEXT,"
                + " FOREIGN KEY ("+KEY_SESSION_ID+") REFERENCES "+TABLE_LISTENING_SESSIONS+"("+KEY_SESSION_ID+")"
                + " ON DELETE CASCADE)";
        db.execSQL(recordingsQuery);

        String positionsTableQuery = "CREATE TABLE " + TABLE_POSITIONS + "("
                + KEY_SESSION_ID + " INTEGER,"
                + KEY_DATETIME + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT,"
                + " PRIMARY KEY (" + KEY_SESSION_ID + ", " + KEY_DATETIME + ")"
                + " FOREIGN KEY ("+KEY_SESSION_ID+") REFERENCES "+TABLE_LISTENING_SESSIONS+"("+KEY_SESSION_ID+")"
                + " ON DELETE CASCADE)";
        db.execSQL(positionsTableQuery);

        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Delete old tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTENING_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSITIONS);

        // Recreate tables
        onCreate(db);
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void addListeningSession(long userID, long sessionID, String startTime, String endTime){
        // Hämtar applikationens lokala databas.
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, userID);
        values.put(KEY_SESSION_ID, sessionID);
        values.put(KEY_START_TIME, startTime);
        values.put(KEY_END_TIME, endTime);

        database.insert(TABLE_LISTENING_SESSIONS, null, values);

        Log.d(TAG, "Session " + sessionID + " added to SQLite database");
    }

    public void addRecording(long sessionID, long recordingID, String url, String filePath, String uploadDate, String userFileName, String recordingStartDate, String recordingEndDate, int duration, String lastEdited){

        ContentValues values = new ContentValues();
        values.put(KEY_SESSION_ID, sessionID);
        values.put(KEY_RECORDING_ID, recordingID);
        values.put(KEY_URL, url);
        values.put(KEY_FILE_PATH, filePath);
        values.put(KEY_UPLOAD_DATE, uploadDate);
        values.put(KEY_USER_FILENAME, userFileName);
        values.put(KEY_RECORDING_START_DATE, recordingStartDate);
        values.put(KEY_RECORDING_END_DATE, recordingEndDate);
        values.put(KEY_DURATION, duration);
        values.put(KEY_LAST_EDITED, lastEdited);

        database.insert(TABLE_RECORDINGS, null, values);

        Log.d(TAG, "Recording " + recordingID + " added to SQLite database");
    }

    public void addPosition(long sessionID, String datetime, double latitude, double longitude){

        ContentValues values = new ContentValues();
        values.put(KEY_SESSION_ID, sessionID);
        values.put(KEY_DATETIME, datetime);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);

        database.insert(TABLE_POSITIONS, null, values);

        Log.d(TAG, "Position " + latitude + ", " + longitude + " added to SQLite database");

    }

    public String getTableName(int tableNumber){
        if(tableNumber == 0){
            return TABLE_LISTENING_SESSIONS;
        }else if(tableNumber == 1){
            return TABLE_RECORDINGS;
        }else if(tableNumber == 2){
            return TABLE_POSITIONS;
        }else{
            return null;
        }
    }

    public String getColumnData(int table, String column) {
        String columnData = "";
        String selectColumnQuery = "SELECT " + column + " FROM " + getTableName(table);
        Cursor cursor = database.rawQuery(selectColumnQuery, null);
        cursor.moveToFirst();
        columnData = cursor.getString(0);
        cursor.close();

        return columnData;
    }

    public void updateListeningSessionEndTime(long sessionID, String endTime){
        ContentValues values = new ContentValues();
        values.put(KEY_END_TIME, endTime);

        database.update(TABLE_LISTENING_SESSIONS, values, "sessionID="+sessionID, null);

        Log.d(TAG, "End time for session: " + sessionID + " has been updated in SQLite database.");
    }

    public void updateRecordingOnUpload(long oldRecordingID, long newRecordingID, String url, String uploadDate) {

        ContentValues values = new ContentValues();
        values.put(KEY_RECORDING_ID, newRecordingID);
        values.put(KEY_URL, url);
        values.put(KEY_UPLOAD_DATE, uploadDate);

        database.update(TABLE_RECORDINGS, values, "recordingID=" + oldRecordingID, null);

        Log.d(TAG, "Updated recording " + newRecordingID + " in SQLite database.");
    }

    public void syncDatabase(long userID, List<RowListeningSession> listeningSessionsRows, List<RowRecording> recordingsRows, List<RowPosition> positionsRows) {

        // Delete old tables
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTENING_SESSIONS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDINGS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_POSITIONS);

        // Recreate tables
        onCreate(database);

        File folder = new File(SharedPrefsHandler.getInstance().getString(SharedPrefsHandler.FOLDER_PATH_KEY, null));

        FilenameFilter recordingFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.toLowerCase().endsWith(".m4a")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        File[] files = folder.listFiles(recordingFilter);

        for (RowListeningSession rls : listeningSessionsRows) {
            addListeningSession(userID, rls.getSessionID(), rls.getStartTime(), rls.getEndTime());
        }

        for (RowRecording rr : recordingsRows) {
            addRecording(rr.getSessionID(), rr.getRecordingID(), rr.getUrl(), "", rr.getUploadDate(), rr.getUserFilename(), rr.getRecordingStartDate(), rr.getRecordingEndDate(), rr.getDuration(), rr.getLastEdited());

            for (File file : files) {
                if (file.getName().equals(rr.getUserFilename())) {
                    String filePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("/") + 1);
                    updateRecordingFilePath(rr.getRecordingID(), filePath);
                }
            }
        }

        for (RowPosition rp : positionsRows) {
            addPosition(rp.getSessionID(), rp.getDatetime(), rp.getLatitude(), rp.getLongitude());
        }
    }

    public void updateRecordingFilePath(long recordingID, String filePath) {

        ContentValues values = new ContentValues();
        values.put(KEY_FILE_PATH, filePath);

        database.update(TABLE_RECORDINGS, values, "recordingID=" + recordingID, null);

        Log.d(TAG, "Added file path to recording " + recordingID + " in SQLite database.");
    }

    public List<ListeningSession> getListeningSessions() {
        List<ListeningSession> listeningSessions = new ArrayList<ListeningSession>();

        String sqlQuery = "SELECT * " +
                          "FROM " + TABLE_LISTENING_SESSIONS + " " +
                          "ORDER BY " + KEY_START_TIME;

        Cursor cursor = database.rawQuery(sqlQuery, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                long sessionID = cursor.getLong(1);
                String startTime = cursor.getString(2);
                String endTime = cursor.getString(3);

                String recordingsQuery = "SELECT " + KEY_RECORDING_ID + " " +
                                         "FROM " + TABLE_RECORDINGS + " " +
                                         "WHERE " + KEY_SESSION_ID + " = " + sessionID;

                Cursor recordingsCursor = database.rawQuery(recordingsQuery, null);
                int numberOfRecordings = recordingsCursor.getCount();
                recordingsCursor.close();

                Log.i("TESTEST", "Session ID: " + sessionID + ". Number of recordings: " + numberOfRecordings);

                ListeningSession listeningSession = new ListeningSession(sessionID, startTime, endTime, numberOfRecordings);
                listeningSessions.add(listeningSession);
            } while(cursor.moveToNext());
        }

        cursor.close();

        return listeningSessions;
    }

    public List<Recording> getRecordings(long sessionID) {
        List<Recording> recordings = new ArrayList<Recording>();

        String sqlQuery = "SELECT " + KEY_RECORDING_ID + ", " + KEY_FILE_PATH + ", " + KEY_URL + ", " + KEY_USER_FILENAME + ", " + KEY_RECORDING_START_DATE + ", " + KEY_DURATION + " " +
                          "FROM " + TABLE_RECORDINGS + " " +
                          "WHERE " + KEY_SESSION_ID + " = " + sessionID + " " +
                          "ORDER BY " + KEY_RECORDING_START_DATE;

        Cursor cursor = database.rawQuery(sqlQuery, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                long recordingID = cursor.getLong(0);
                String filePath = cursor.getString(1);
                String url = cursor.getString(2);
                String userFilename = cursor.getString(3);
                String recordingStartDate = cursor.getString(4);
                int duration = cursor.getInt(5);

                Recording recording = new Recording(recordingID, filePath, url, userFilename, recordingStartDate, duration);
                recordings.add(recording);
            } while(cursor.moveToNext());
        }

        cursor.close();

        return recordings;
    }

    public void deleteRecording(long recordingID) {

        String sqlQuery = "DELETE FROM " + TABLE_RECORDINGS +
                          " WHERE " + KEY_RECORDING_ID + " = " + recordingID;

        database.execSQL(sqlQuery);

        Log.d(TAG, "Recording: " + recordingID + " has been deleted from the SQLite database.");
    }

    public void updateUserFileName(long recordingID, String newUserFileName, String lastEdited){
        ContentValues values = new ContentValues();
        values.put(KEY_USER_FILENAME, newUserFileName);
        values.put(KEY_LAST_EDITED, lastEdited);

        database.update(TABLE_RECORDINGS, values, "recordingID="+recordingID, null);

        Log.d(TAG, "User file name and last edited for recording: " + recordingID + " has been updated in SQLite database.");
    }

    public void deleteSession(long sessionID) {


        String sqlQuery = "DELETE FROM " + TABLE_LISTENING_SESSIONS +
                " WHERE " + KEY_SESSION_ID + " = " + sessionID;

        database.execSQL(sqlQuery);

        Log.d(TAG, "Session: " + sessionID + " has been deleted from the SQLite database.");
    }
}
