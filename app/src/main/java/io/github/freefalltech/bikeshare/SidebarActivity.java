package io.github.freefalltech.bikeshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class SidebarActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sidebar);
        mAuth = FirebaseAuth.getInstance();
    }

    public void returnBack(View v){
        //intent goes back to the searchBikeActivity
        Intent intent = new Intent(SidebarActivity.this, SearchBikeActivity.class);
        startActivity(intent);
    }

    public void myActivityIntent(View v){
        //intent goes back to the searchBikeActivity
        Intent intent = new Intent(SidebarActivity.this, MyActivity.class);
        startActivity(intent);
    }

    public void openMyAccount(View v){
        //intent goes back to the searchBikeActivity
        Intent intent = new Intent(SidebarActivity.this, MyAccount.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        if(mAuth.getCurrentUser()!=null){
            mAuth.signOut();
            finish();
        }
    }

    public void fleetActivityIntent(View view) {
        Intent intent = new Intent(SidebarActivity.this, FleetActivity.class);
        startActivity(intent);
    }
}
