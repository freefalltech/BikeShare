package io.github.freefalltech.bikeshare;
//26 May 2017, Coded and maintained by abilash senthilkumar. github : @nextbiggeek


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;



public class SearchBikeActivity extends FragmentActivity {

    private GoogleMap mMap;
    int permissionLocationCheckFine, permissionLocationCheckCoarse;
    //public static final int PERMISSION_LOCATION_COARSE = 0; 
    //public int PERMISSION_LOCATION_FINE = 0; 



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bike);
        formatTextviews();
        checkForPermissions();

    }

    private void checkForPermissions() {
        //check if permissions are granted, and then continue
        permissionLocationCheckFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionLocationCheckFine != PackageManager.PERMISSION_GRANTED){
            //permission not granted. proceed to ask
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    12);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 12: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!



                } else {

                    // permission denied, boo!
                    //for now, the app keeps asking until the user agrees. NOTE: ITS A BAD UX PRACTICE
                    checkForPermissions();
                }
                return;
            }

        }
    }

    private void formatTextviews() {
        //initialize typeface
        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/helvetica.ttf");
        Typeface arialblack = Typeface.createFromAsset(getAssets(), "fonts/arialblack.ttf");
        Typeface adam = Typeface.createFromAsset(getAssets(), "fonts/adam.otf");




        //initialize txtviews


    }


}
