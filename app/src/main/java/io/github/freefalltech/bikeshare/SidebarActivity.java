package io.github.freefalltech.bikeshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SidebarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sidebar);
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
}
