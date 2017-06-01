package io.github.freefalltech.bikeshare;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {
    public int interval = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
         Handler handler = new Handler();
         Runnable runnable = new Runnable() {
            @Override
            public void run() {
                openLoginScreen();
            }
        };
        handler.postDelayed(runnable, interval);

    }

    private void openLoginScreen() {
        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
