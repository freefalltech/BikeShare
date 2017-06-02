package io.github.freefalltech.bikeshare;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SidebarActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sidebar);
        mAuth = FirebaseAuth.getInstance();
    }

    private void formatTextViews() {

        //initialize typeface
        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/helvetica.ttf");
        Typeface arialblack = Typeface.createFromAsset(getAssets(), "fonts/arialblack.ttf");
        Typeface adam = Typeface.createFromAsset(getAssets(), "fonts/adam.otf");
        Typeface futura = Typeface.createFromAsset(getAssets(), "fonts/futur.ttf");
        Typeface futuraItalic = Typeface.createFromAsset(getAssets(), "fonts/futura_italic.ttf");


        TextView t1 = (TextView) findViewById(R.id.text1);
        TextView t2 = (TextView) findViewById(R.id.text2);
        TextView t3 = (TextView) findViewById(R.id.text3);
        TextView t4 = (TextView) findViewById(R.id.text4);

        t1.setTypeface(futura);
        t2.setTypeface(futura);
        t3.setTypeface(futura);
        t4.setTypeface(futura);




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
