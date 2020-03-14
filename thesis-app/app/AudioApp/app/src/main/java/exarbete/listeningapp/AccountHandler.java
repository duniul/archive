package exarbete.listeningapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.File;

import exarbete.listeningapp.database.SQLiteHelper;
import exarbete.listeningapp.retrofit.ServiceGenerator;
import exarbete.listeningapp.retrofit.CheckUserResponse;
import exarbete.listeningapp.retrofit.CheckUserService;

import exarbete.listeningapp.retrofit.SyncLocalDatabaseResponse;
import exarbete.listeningapp.retrofit.SyncLocalDatabaseService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Axel Nauclér on 12/04/2016.
 */
public class AccountHandler implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = AccountHandler.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private static AccountHandler instance;
    private static GoogleApiClient googleApiClient;

    private static CheckUserService checkUserService = ServiceGenerator.createService(CheckUserService.class);
    private static SyncLocalDatabaseService syncLocalDatabaseService = ServiceGenerator.createService(SyncLocalDatabaseService.class);

    private FragmentActivity activity;
    private GoogleSignInOptions gso;

    public static AccountHandler getInstance(FragmentActivity activity) {
        if (instance == null) {
            instance = new AccountHandler(activity);
            instance.update();
            return instance;
        } else {
            instance.activity = activity;
            instance.update();
            return instance;
        }
    }

    private AccountHandler(FragmentActivity activity) {
        this.activity = activity;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    private void update() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(activity)
                    .enableAutoManage(activity, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        googleApiClient.connect();
    }

    public void signInOnStart() {

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in.");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult success:" + result.isSuccess());
        if (result.isSuccess()) {

            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();

            //This code retrieves the user information and stores it in SharedPrefs
            String googleID = account.getId();
            String userName = account.getDisplayName();
            final String userEmail = account.getEmail();
            String userPictureURL = null;
            Uri photoUri = account.getPhotoUrl();
            if(photoUri != null){
                userPictureURL = account.getPhotoUrl().toString();
            }

            SharedPrefsHandler sharedPrefsHandler = SharedPrefsHandler.getInstance();
            sharedPrefsHandler.putUserDetails(googleID, userName, userEmail, userPictureURL);

            Call<CheckUserResponse> checkUserCall = checkUserService.checkUser(googleID, userEmail);
            checkUserCall.enqueue(new Callback<CheckUserResponse>() {

                @Override
                public void onResponse(Call<CheckUserResponse> call, Response<CheckUserResponse> response) {
                    CheckUserResponse checkUserResponse = response.body();
                    if (response.isSuccessful()) {
                        Log.i("RetrofitTest", "SUCCESSFUL");
                        Log.i("RetrofitTest", "Response: " + checkUserResponse.getMessage());
                        SharedPrefsHandler.getInstance().putUserID(checkUserResponse.getUserID());
                        syncLocalDatabase(checkUserResponse.getUserID());

                        // If the user signed in from the main activity, update the header in the navigation drawer.
                        // Else if the user signed in from the login activity, start the main activity and update drawer header.
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).updateDrawerHeader();
                        } else if(activity instanceof LoginActivity){
                            Intent intent = new Intent(activity, MainActivity.class);
                            activity.startActivity(intent);
                            activity.finish();
                        }

                    } else {
                        Log.i("RetrofitTest", "NOT SUCCESSFUL");
                        Log.i("RetrofitTest", "Response: " + checkUserResponse);
                    }
                }

                @Override
                public void onFailure(Call<CheckUserResponse> call, Throwable t) {
                    Log.e("RetrofitTest", "FAILED COMPLETELY");
                    Log.e("RetrofitTest", "Error Message: " + t.getMessage());
                    Log.e("RetrofitTest", "Error toString: " + t.toString());
                    t.printStackTrace();
                }
            });

        } else {
            SharedPreferences sharedPrefs = activity.getSharedPreferences(SharedPrefsHandler.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = sharedPrefs.edit();
            prefEditor.putBoolean(SharedPrefsHandler.USER_LOGGED_IN_KEY, false);
            prefEditor.apply();
        }
    }

    private void syncLocalDatabase(final long userID) {

        Call<SyncLocalDatabaseResponse> syncLocalDatabaseCall = syncLocalDatabaseService.sync(userID);
        syncLocalDatabaseCall.enqueue(new Callback<SyncLocalDatabaseResponse>() {
            @Override
            public void onResponse(Call<SyncLocalDatabaseResponse> call, Response<SyncLocalDatabaseResponse> response) {
                SyncLocalDatabaseResponse syncLocalDatabaseResponse = response.body();
                if (response.isSuccessful()) {

                    Log.i("RetrofitSyncTest", "SUCCESSFUL");
                    Log.i("RetrofitSyncTest", "Response: " + syncLocalDatabaseResponse.getMessage());
                    File folder = new File(SharedPrefsHandler.getInstance().getString(SharedPrefsHandler.FOLDER_PATH_KEY, "-1"));
                    if(!folder.exists()){
                        folder.mkdir();
                    }
                    SQLiteHelper.getInstance().syncDatabase(userID,
                            syncLocalDatabaseResponse.getListeningSessionsRows(),
                            syncLocalDatabaseResponse.getRecordingsRows(),
                            syncLocalDatabaseResponse.getPositionsRows());

                } else {
                    Log.i("RetrofitSyncTest", "UNSUCCESSFUL");
                }
            }

            @Override
            public void onFailure(Call<SyncLocalDatabaseResponse> call, Throwable t) {
                Log.e("RetrofitTest", "FAILED COMPLETELY");
                Log.e("RetrofitTest", "Error Message: " + t.getMessage());
                Log.e("RetrofitTest", "Error toString: " + t.toString());
                t.printStackTrace();
            }
        });
    }

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        SharedPrefsHandler.getInstance().removeUserDetails();
                    }
                }
        );
    }

    public void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        SharedPrefsHandler.getInstance().removeUserDetails();

                        // If the user signed signed out from the main activity, update the header in the navigation drawer.
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).updateDrawerHeader();
                        }
                    }
                }
        );
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    public int getSignInCode() {
        return RC_SIGN_IN;
    }

    public GoogleSignInOptions getGso() {
        return gso;
    }

}