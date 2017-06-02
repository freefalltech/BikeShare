package io.github.freefalltech.bikeshare;
//26 May 2017, Coded and maintained by abilash senthilkumar. github : @nextbiggeek


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.Date;



public class SearchBikeActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //private GoogleMap mMap;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    boolean mRequestLocationUpdates;
    TextView addressTextView, lastUpdatedTime, titleTextDockInfo, bikeAvailableNumber, dockAvailableNumber;
    String mLastUpdateTime, mAddressOutput;
    private ResultReceiver mResultReceiver;
    int permissionLocationCheckFine;
    GoogleApiClient mGoogleApiClient;

    SupportMapFragment mapFragment;


    //LatLng variabls for 5 places in Mysore;
    //LatLng latLng1, latLng2, latLng3, latLng4, latLng5;

    //MarkerOptionGlobalVariables
    MarkerOptions markerOptions1, markerOptions2, markerOptions3, markerOptions4, markerOptions5;

    //and latitudes and longitudes of places are split into two arrays. therefore array's index should match
    double[] latArray = {12.302494, 12.302842, 12.305103, 12.312557, 12.298020};
    double[] longArray = {76.665334, 76.643230, 76.655098, 76.658174, 76.664368};


    //Dock INFORMATION global variables
    String BIKES_AVAILABLE, DOCKS_AVAILABLE;


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

    public void letsStartListeningLocation() {

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


    @Override
    protected void onStart() {
        letsStartListeningLocation();
        super.onStart();
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    protected void createLocationRequest() {
        //sets settings for the locationrequest
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            /* allotted interval is 10 sec, and the fastest interval it can recieve
            due to other apps is 5 seconds to revent exceptions
            */
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mRequestLocationUpdates) {
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
        //lastUpdatedTime.setText("Location last updated on " + mLastUpdateTime);
//        coordinateTextView.setText("Latitude is " + String.valueOf(mCurrentLocation.getLatitude())
        //              + "Longitude is" + String.valueOf(mCurrentLocation.getLongitude()));
        mapFragment.getMapAsync(this);
        startAddressIntentService();


    }


    protected void startAddressIntentService() {
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
                                            String permissions[],  int[] grantResults) {
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
        Typeface futura = Typeface.createFromAsset(getAssets(), "fonts/futur.ttf");
        Typeface futuraItalic = Typeface.createFromAsset(getAssets(), "fonts/futura_italic.ttf");


        //initialize txtviews
        TextView bikeFinderTextOne = (TextView) findViewById(R.id.bikeFinderTxt1);
        TextView bikeFinderTextTwo = (TextView) findViewById(R.id.bikeFinderTxt2);
        TextView bikeFinderTextThree = (TextView) findViewById(R.id.bikeFinderTxt3);
        TextView findBikeText = (TextView) findViewById(R.id.findBikeText);
        titleTextDockInfo = (TextView) findViewById(R.id.dockTitle);
        bikeAvailableNumber = (TextView) findViewById(R.id.bikesAvailableNumber);
        dockAvailableNumber = (TextView) findViewById(R.id.docksAvailableNumber);


        addressTextView = (TextView) findViewById(R.id.addressTextView);
        //lastUpdatedTime = (TextView) findViewById(R.id.lastUpdatedTime);

        bikeFinderTextOne.setTypeface(futura);
        bikeFinderTextTwo.setTypeface(futura);
        bikeFinderTextThree.setTypeface(futura);
        findBikeText.setTypeface(futuraItalic);
        titleTextDockInfo.setTypeface(futura);
        //typefaces set


    }


    //to add markers, and more map settings after the map gets initialized
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);

        //googleMap.setMyLocationEnabled(true);
        //create a latlong variable of my location for now and add a marker

        /*
        LatLng myLocationLatLong = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(myLocationLatLong).title("mAddressOutput");
        googleMap.addMarker(markerOptions);
        */


        //temporary "my location" based on mysore junction
        LatLng mysoreJunction = new LatLng(12.316991, 76.645130);
        //BitmapDescriptor bitmapDescriptor =  BitmapDescriptorFactory.fromResource(R.drawable.my_location_icon);
        MarkerOptions markerOptionsMysoreJunction = new MarkerOptions().position(mysoreJunction).title("Mysore Railway Station").
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        googleMap.addMarker(markerOptionsMysoreJunction);

        //so currently 5 places in indra nagar, bangalore is taken
        LatLng mysoreZoo = new LatLng(latArray[0], longArray[0]);
        LatLng KSRTC = new LatLng(latArray[1], longArray[1]);
        LatLng mysorePalace = new LatLng(latArray[2], longArray[2]);
        LatLng mysoreRuralTerminus = new LatLng(latArray[3], longArray[3]);
        LatLng inoxMysore = new LatLng(latArray[4], longArray[4]);

        //markers for the
        markerOptions1 = new MarkerOptions().position(mysoreZoo).title("Mysore Zoo");
        markerOptions2 = new MarkerOptions().position(KSRTC).title("KSRTC");
        markerOptions3 = new MarkerOptions().position(mysorePalace).title("Mysore Palace");
        markerOptions4 = new MarkerOptions().position(mysoreRuralTerminus).title("Mysore Mofussil Terminus");
        markerOptions5 = new MarkerOptions().position(inoxMysore).title("INOX Mysore");


        //add markers for the above latlng
        googleMap.addMarker(markerOptions1);
        googleMap.addMarker(markerOptions2);
        googleMap.addMarker(markerOptions3);
        googleMap.addMarker(markerOptions4);
        googleMap.addMarker(markerOptions5);


        googleMap.moveCamera(CameraUpdateFactory.newLatLng(mysoreJunction));

        //TODO make the markers a global variable, and then use them to verify onMarkerClick

        googleMap.setMinZoomPreference(13.0f);


    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent(SearchBikeActivity.this,LoginActivity.class));
            finish();
        }*/
    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        //sliderlayout is shown

        RelativeLayout sliderLayout = (RelativeLayout) findViewById(R.id.sliderLayout);
        sliderLayout.setVisibility(View.VISIBLE);
        titleTextDockInfo = (TextView) findViewById(R.id.dockTitle);


        String titleName;
        int markerInt;
        if(arg0.getTitle().equals("Mysore Zoo")){
            titleName = arg0.getTitle();
            titleTextDockInfo.setText(titleName);
            BIKES_AVAILABLE = "12";
            DOCKS_AVAILABLE = "3";
            bikeAvailableNumber.setText(BIKES_AVAILABLE);
            dockAvailableNumber.setText(DOCKS_AVAILABLE);
        }else if(arg0.getTitle().equals("KSRTC")){
            titleName = arg0.getTitle();
            titleTextDockInfo.setText(titleName);
            BIKES_AVAILABLE = "11";
            DOCKS_AVAILABLE = "7";
            bikeAvailableNumber.setText(BIKES_AVAILABLE);
            dockAvailableNumber.setText(DOCKS_AVAILABLE);
            markerInt = 2;
        }else if(arg0.getTitle().equals("Mysore Palace")){
            titleName = arg0.getTitle();
            titleTextDockInfo.setText(titleName);
            BIKES_AVAILABLE = "16";
            DOCKS_AVAILABLE = "2";
            bikeAvailableNumber.setText(BIKES_AVAILABLE);
            dockAvailableNumber.setText(DOCKS_AVAILABLE);
            markerInt = 2;
        }else if(arg0.getTitle().equals("Mysore Mofussil Terminus")){
            titleName = arg0.getTitle();
            titleTextDockInfo.setText(titleName);
            BIKES_AVAILABLE = "18";
            DOCKS_AVAILABLE = "1";
            bikeAvailableNumber.setText(BIKES_AVAILABLE);
            dockAvailableNumber.setText(DOCKS_AVAILABLE);
            markerInt = 2;
        }else if(arg0.getTitle().equals("INOX Mysore")){
            titleName = arg0.getTitle();
            titleTextDockInfo.setText(titleName);
            BIKES_AVAILABLE = "13";
            DOCKS_AVAILABLE = "2";
            bikeAvailableNumber.setText(BIKES_AVAILABLE);
            dockAvailableNumber.setText(DOCKS_AVAILABLE);
            markerInt = 2;
        }

        return false;
    }



    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
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
                // TODO: Show address to user
            }

        }
    }

    public void sideBarIntent(View v){
        Intent intent = new Intent(SearchBikeActivity.this, SidebarActivity.class);
        startActivity(intent);
    }
}



