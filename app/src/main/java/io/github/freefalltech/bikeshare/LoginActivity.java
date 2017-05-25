package io.github.freefalltech.bikeshare;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 0;
    GoogleApiClient mGoogleApiClient;
    private static boolean isLogin = true;
    private FirebaseAuth mAuth;
    private String TAG = "Google Sign In";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/dancing_rainbow.ttf");
        ((TextView)findViewById(R.id.header)).setTypeface(tf);


        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null)
            proceed();

        //Google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.oauth_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "AutoManage failed", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton gmail = (SignInButton) findViewById(R.id.gmail_login_button);

        TextView textView = (TextView) gmail.getChildAt(0);
        textView.setText(R.string.google_sign_in);
        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        FrameLayout aadhaar = (FrameLayout) findViewById(R.id.aadhaar_login_button);
        textView = (TextView) findViewById(R.id.aadhaar_login_text);
        aadhaar.measure(0, 0);
        textView.getLayoutParams().width = (int) ((aadhaar.getMeasuredWidth() * 4.09) / 5.08);

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText emailInput = (EditText) findViewById(R.id.input_email),
                        passwordInput = (EditText) findViewById(R.id.input_password);
                String email = emailInput.getText().toString(),
                        password = passwordInput.getText().toString();
                boolean isError = false;
                emailInput.setError(null);
                passwordInput.setError(null);
                if (!isEmailValid(email)) {
                    emailInput.setError("Enter a valid email");
                    emailInput.requestFocus();
                    isError = true;
                }
                if (!isLogin && password.length() < 8) {
                    passwordInput.setError("Minimum 8 characters required");
                    if (!isError) passwordInput.requestFocus();
                    isError = true;
                }

                if (!isError) {
                    if (isLogin) {
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) proceed();
                                        } else {
                                            showDialog("Something went wrong", "Please try logging in later");
                                        }
                                    }
                                });
                    } else {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "registerWithEmail:success");
                                            final FirebaseUser curUser = mAuth.getCurrentUser();
                                            curUser.sendEmailVerification()
                                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                showDialog("Email verification sent", "Verify your email and log in");
                                                                switchLoginRegister(findViewById(R.id.switch_text_view));
                                                                emailInput.setText("");
                                                                emailInput.requestFocus();
                                                                passwordInput.setText("");
                                                            } else {
                                                                showDialog("Something went wrong in sending email for verification",
                                                                        "Please log in with the registered credentials to continue");
                                                            }
                                                            mAuth.signOut();
                                                        }
                                                    });

                                        } else {
                                            showDialog("Something went wrong", "Please try registering later");
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }


    private void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(message)
                .setTitle(title);
        builder.create().show();
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticcd ated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            Log.d(TAG, "googleSignIn:success");
            firebaseAuthWithGoogle(account);
            //startActivity(new Intent(LoginActivity.this,SearchBikeActivity.class));
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this, "Sign In failed" + result.getStatus().getStatusCode(), Toast.LENGTH_LONG).show();

            //Log.d(TAG,result.getStatus().getStatusMessage());
        }
    }

    private void proceed() {
        startActivity(new Intent(LoginActivity.this, SearchBikeActivity.class));
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            proceed();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null)
                                proceed();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void switchLoginRegister(View view) {
        ((TextView) view).setText(isLogin ? R.string.already_a_member : R.string.not_a_member);
        ((AppCompatButton) findViewById(R.id.btn_login)).setText(isLogin ? "Register" : "Login");
        isLogin = !isLogin;
        EditText emailInput = (EditText) findViewById(R.id.input_email),
                passwordInput = (EditText) findViewById(R.id.input_password);
        emailInput.setError(null);
        passwordInput.setError(null);
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            View header = findViewById(R.id.header);
            if (isViewOverlapping(header, findViewById(R.id.email_login_container))) {
                header.setVisibility(View.GONE);
                Log.d(TAG, "OVERLAP!!");
            } else {
                header.setVisibility(View.VISIBLE);
                Log.d(TAG, "NO OVERLAP");
            }
        }
    }

    private boolean isViewOverlapping(View v1, View v2) {
        Rect rect1 = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
        Log.d(TAG,rect1.toString());
        Rect rect2 = new Rect(v2.getLeft(), v2.getTop(), v2.getRight(), v2.getBottom());
        Log.d(TAG,rect2.toString());
        return rect1.intersect(rect2);
    }
}
