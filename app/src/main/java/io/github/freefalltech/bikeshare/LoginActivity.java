package io.github.freefalltech.bikeshare;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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

import java.util.Timer;
import java.util.TimerTask;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 0;
    GoogleApiClient mGoogleApiClient;
    private static boolean isLogin = true;
    private FirebaseAuth mAuth;
    private String TAG = "Google Sign In";
    private MaterialProgressBar progressBar;
    private View greyScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/dancing_rainbow.ttf");
        ((TextView) findViewById(R.id.header)).setTypeface(tf);

        progressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);
        greyScreen = findViewById(R.id.grey_out_screen);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().isEmailVerified())
                proceed();
            else {
                mAuth.getCurrentUser().reload().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (mAuth.getCurrentUser().isEmailVerified())
                            proceed();
                        else showVerificationSteps();
                    }
                });
            }
        } else {
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
                    grayScale(true);
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
                        grayScale(true);
                        if (isLogin) {
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            grayScale(false);
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d(TAG, "signInWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                    if (user.isEmailVerified())
                                                        proceed();
                                                    else showVerificationSteps();
                                                }
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
                                                if (curUser != null)
                                                    curUser.sendEmailVerification()
                                                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    grayScale(false);
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
                                                else
                                                    grayScale(false);
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
    }

    private void grayScale(boolean enable){
        int visibility = enable?View.VISIBLE: View.GONE;
        progressBar.setVisibility(visibility);
        greyScreen.setVisibility(visibility);
        touchEvents(!enable);
    }

    private void touchEvents(boolean enabled) {
        if (enabled)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        else getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showDialog(String title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
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
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show();
            grayScale(false);
            Log.d(TAG, "" + result.getStatus().getStatusCode());
        }
    }

    private void proceed() {
        startActivity(new Intent(LoginActivity.this, SearchBikeActivity.class));
        finish();
    }

    private void showVerificationSteps() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setCancelable(false);

        final AlertDialog verifyEmailDialog = builder.setTitle("Account unverified")
                .setMessage("You cannot continue until you verify your email (reload the app after verification)")
                .setPositiveButton("Send verification email again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_email)
                .create();
        verifyEmailDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button b = verifyEmailDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onClick(View view) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Email verification sent", Toast.LENGTH_LONG).show();
                                    } else {
                                        recreate();
                                    }
                                }
                            });
                            b.setEnabled(false);
                            b.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            Timer buttonTimer = new Timer();
                            buttonTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            b.setEnabled(true);
                                            b.setTextColor(getResources().getColor(R.color.colorAccent));
                                        }
                                    });
                                }
                            }, 20000);
                        }
                    }
                });
            }
        });
        verifyEmailDialog.show();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        grayScale(false);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    proceed();
                                    Log.d(TAG, "emailVerify:success");
                                } else
                                    showVerificationSteps();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed",
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
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
        Log.d(TAG, rect1.toString());
        Rect rect2 = new Rect(v2.getLeft(), v2.getTop(), v2.getRight(), v2.getBottom());
        Log.d(TAG, rect2.toString());
        return rect1.intersect(rect2);
    }
}
