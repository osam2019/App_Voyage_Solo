package com.example.ada.voyage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ada.voyage.main.MainActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;


public class AuthenticationActivity extends AppCompatActivity implements // Google sign in activity
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private TextView mDetailTextView;

    //Button btnToBlog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);



        mStatusTextView = (TextView) findViewById(R.id.status);
        mDetailTextView = (TextView) findViewById(R.id.detail);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

         //Configure Google Sign In */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

    }






    public void gotohomewithToast (View view) {

        Intent i = new Intent (AuthenticationActivity.this, AppBarActivity.class);
        startActivity(i);
        //setContentView(R.layout.app_bar_main);
        Toast.makeText(getApplicationContext(), "등록되었습니다", Toast.LENGTH_SHORT).show();


    }

    public void gotohome (View view) {
        //Intent i = new Intent (AuthenticationActivity.this, AppBarActivity.class);
        //startActivity(i);

        setContentView(R.layout.app_bar_main);
    }

    public void gotoCal (View view) {
        Intent i = new Intent (AuthenticationActivity.this, AppBarActivity.class);
        startActivity(i);
        //setContentView(R.layout.calendar_activity_event);
        //setContentView(R.layout.fragment_calendar);
    }

    public void gotoList(View view) {
        setContentView(R.layout.fragment_calendar);

        //Intent i = new Intent (AuthenticationActivity.this, AppBarSubActivity.class);
        //startActivity(i);
    }

    public void gotoMoney(View view) {
        setContentView(R.layout.activity_view_money_detail);

    }

    public void gotoConfirm(View view) {
        Toast.makeText(getApplicationContext(), "예약이 등록되었습니다", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.fragment_calendar);
    }

    public void gotoSelect(View view) {
        setContentView(R.layout.activity_money_detail);
    }

    public void openCamera(View view) {
        //Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //startActivity(intent);
        //setContentView(R.layout.livestream_layout);
        Intent intent = new Intent(getApplicationContext(), LiveStreamActivity.class);
        startActivity(intent);

    }

    @Override
    public void onStart() {
        super.onStart();
        /** Check if user is signed in (non-null) and update UI accordingly.
         if user is already signed in, go to MainActivity immediately. */
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /** Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                /** Google Sign In was successful; now authenticate with Firebase */
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else {
                /** Google Sign In failed, update UI appropriately */
                updateUI(null);
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        //showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(AuthenticationActivity.this, "인증성공", Toast.LENGTH_SHORT).show();

                            /** Sign in success, update UI with the signed-in user's information */
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {

                            /** If sign in fails, display a message to the user. */
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        //hideProgressDialog();

                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
         /** Firebase sign out */
        mAuth.signOut();

        /** Google sign out */
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        /** This code was given by Firebase, but we won't need it for this application */
        /** Firebase sign out */
        mAuth.signOut();

        /** Google revoke access */
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        //hideProgressDialog();
        /** If user is not null (if user just signed in or is already signed in)
        , do not stay in the Authentication Activity; move on to the MainActivity right away */
        if (user != null) {
             Intent i = new Intent (AuthenticationActivity.this, MainActivity.class);
             startActivity(i);
             finish();
        } else {
            /** If the user is not signed in, prompt the user to sign in
            using the sign in button.
             */
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

//            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /** An unresolvable error has occurred and Google APIs (including Sign-In) will not
        * be available. */
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        } else if (i == R.id.sign_out_button) {
            signOut();
        } else if (i == R.id.disconnect_button) {
            revokeAccess();
        }
    }



}
