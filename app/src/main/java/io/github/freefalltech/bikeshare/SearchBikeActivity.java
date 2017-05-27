package io.github.freefalltech.bikeshare;
//26 May 2017, Coded and maintained by abilash senthilkumar. github : @nextbiggeek


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;


public class SearchBikeActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Location mLastLocation, mCurrentLocation;
    private LocationRequest mLocationRequest;
    boolean mRequestLocationUpdates;
    TextView coordinateTextView, addressTextView, lastUpdatedTime;
    String mLastUpdateTime, mAddressOutput;
    private ResultReceiver mResultReceiver;
    int permissionLocationCheckFine;
    GoogleApiClient mGoogleApiClient;

    SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bike);
        formatTextviews();
        checkForPermissions();
        mResultReceiver = new AddressResultReceiver(new Handler());

        //declare the map
         mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);



    }

    public void letsStartListeningLocation(View v){

        mRequestLocationUpdates = true;



        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
        }
        //mLocationRequest is initialized

        createLocationRequest();

        //then the locationSettings builder is also initialized
        LocationSettingsRequest.Builder mLocationSettingsRequest = new
                LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        mLocationSettingsRequest.build());


    }

    public void letsStopListeningLocation(View v){
        mGoogleApiClient.disconnect();
        Toast.makeText(this, "LocationListener turned off", Toast.LENGTH_SHORT);
    }





    @Override
    protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();

    }

    protected void createLocationRequest(){
        //sets settings for the locationrequest
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            /* allotted interval is 10 sec, and the fastest interval it can recieve
            due to other apps is 5 seconds to revent exceptions
            */
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
    }

    public void checkAddress(View v){
        startAddressIntentService();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(mRequestLocationUpdates){
           LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUi();



    }

    private void updateUi() {
        lastUpdatedTime.setText(mLastUpdateTime);
        coordinateTextView.setText("Latitude is " + String.valueOf(mCurrentLocation.getLatitude())
                + "Longitude is" + String.valueOf(mCurrentLocation.getLongitude()));
        mapFragment.getMapAsync(this);

    }




    protected void startAddressIntentService(){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        startService(intent);
    }


    //permissions
    private void checkForPermissions() {
        //check if permissions are granted, and then continue
        permissionLocationCheckFine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocationCheckFine != PackageManager.PERMISSION_GRANTED) {
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
        coordinateTextView = (TextView) findViewById(R.id.coordinateTextView);
        addressTextView = (TextView) findViewById(R.id.addressTextView);
        lastUpdatedTime = (TextView) findViewById(R.id.lastUpdatedTime);


    }


    //to add markers, and more map settings after the map gets initialized
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //create a latlong variable of my location for now and add a marker
        LatLng myLocationLatLong = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(myLocationLatLong).title("mAddressOutput");
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocationLatLong));

        //add a circle around my location
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(myLocationLatLong);
        circleOptions.radius(500); //metres
        Circle circle = googleMap.addCircle(circleOptions);
        googleMap.setMinZoomPreference(10.0f);
        

    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.v("TAG", "SUCCESS");
            addressTextView.setText(mAddressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                //Toast.makeText(this, "address found", Toast.LENGTH_SHORT);
            }

        }
    }
}



