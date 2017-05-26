package io.github.freefalltech.bikeshare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SearchBikeActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    int permissionLocationCheckFine, permissionLocationCheckCoarse;
    public static final int PERMISSION_LOCATION_COARSE = 0; 
    public static final int PERMISSION_LOCATION_FINE = 0; 



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bike);
        getPermissionsFromUser();

    }

    //obtain permission from user
    public void getPermissionsFromUser() { 
        permissionLocationCheckCoarse = ContextCompat.checkSelfPermission(SearchBikeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION); 
        permissionLocationCheckFine = ContextCompat.checkSelfPermission(SearchBikeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION); 
        if (permissionLocationCheckFine != PackageManager.PERMISSION_GRANTED){ 
            //not granted fine location services 
            ActivityCompat.requestPermissions(SearchBikeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_FINE);
        } else if(permissionLocationCheckCoarse != PackageManager.PERMISSION_GRANTED) { 

            //not granted coarse location services 
            ActivityCompat.requestPermissions(SearchBikeActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_LOCATION_COARSE);

        }else{
            // permission already granted. continue running as usual  
        }

    }

    LocationManager locationManager;
    LocationListener locationListener;

    private void continueListeningLocation() {

        //string value to interchange between gps or network provider
        String  locationProvider = LocationManager.GPS_PROVIDER;


        //aquire reference to locationmanager
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                // Define a listener that responds to location updates
                 locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // Called when a new location is found by the network location provider.
                        makeUseOfNewLocation(location);
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);

            //a

            return;
        }

    }

    public void stopListeningLocation(){
        locationManager.removeUpdates(locationListener);
    }


    public void locationStartListening(View v){
        //user clicks listen to location button
        Toast.makeText(this, "app starts listening location", Toast.LENGTH_SHORT).show();
        continueListeningLocation();
    }

    public void locationStopListening(View v){
        //user clicks listen to location button
        Toast.makeText(this, "app stops listening location", Toast.LENGTH_SHORT).show();
        stopListeningLocation();
    }


    /**
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! \
                    continueListeningLocation();

                } else {

                    // permission denied, boo!
                    //TODO HANDLE THE RESPONSE
                }
                return;
            }

        }
    }**/

}
