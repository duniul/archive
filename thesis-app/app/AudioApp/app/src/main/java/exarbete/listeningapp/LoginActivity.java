package exarbete.listeningapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

public class LoginActivity extends AppCompatActivity {

    private AccountHandler accountHandler;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);
        accountHandler = AccountHandler.getInstance(this);
        accountHandler.signInOnStart();
    }
    @Override
    public void onStart() {
        super.onStart();
        accountHandler.getGoogleApiClient().connect();
    }

    public void onSignInClick(View view) {
        accountHandler.signIn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == accountHandler.getSignInCode()) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            accountHandler.handleSignInResult(result);
        }
    }
}
