package io.github.freefalltech.bikeshare;
//26 May 2017, Coded and maintained by abilash senthilkumar. github : @nextbiggeek


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.TextViewCompat;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;



public class SearchBikeActivity extends FragmentActivity {

    private GoogleMap mMap;
    //int permissionLocationCheckFine, permissionLocationCheckCoarse;
   // public static final int PERMISSION_LOCATION_COARSE = 0; 
   // public static final int PERMISSION_LOCATION_FINE = 0; 



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bike);
        formatTextviews();

    }

    private void formatTextviews() {
        //initialize typeface
        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/helvetica.ttf");


        //initialize txtviews
        TextView openerMainText = (TextView) findViewById(R.id.opener_main_text);


        //apply the typeface
        openerMainText.setTypeface(helvetica);


    }


}
