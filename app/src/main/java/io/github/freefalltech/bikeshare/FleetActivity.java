package io.github.freefalltech.bikeshare;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class FleetActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    boolean mRequestLocationUpdates;
    TextView addressTextView;
    String mLastUpdateTime;
    int permissionLocationCheckFine;
    GoogleApiClient mGoogleApiClient;

    SupportMapFragment mapFragment;
    int humidity, temp, pressure;
    String API_KEY, API_PASS;

    //LatLng variabls for 5 places in Mysore;
    //LatLng latLng1, latLng2, latLng3, latLng4, latLng5;

    //MarkerOptionGlobalVariables
    MarkerOptions markerOptions1, markerOptions2, markerOptions3, markerOptions4, markerOptions5;

    //and latitudes and longitudes of places are split into two arrays. therefore array's index should match
    double[] latArray = {12.302494, 12.302842, 12.305103, 12.312557, 12.298020};
    double[] longArray = {76.665334, 76.643230, 76.655098, 76.658174, 76.664368};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleet);
        API_KEY = BuildConfig.CLOUDANT_API_KEY;
        API_PASS = BuildConfig.CLOUDANT_API_PASS;
        //formatTextviews();
        checkForPermissions();

        //declare the map
        mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        new Runnable(){
            @Override
            public void run() {
                new UpdateVariables().execute();
                new Handler().postDelayed(this,10000);
            }
        }.run();

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
        updateUi();
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
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
        //updateUi();
    }

    private void updateUi() {
        //lastUpdatedTime.setText("Location last updated on " + mLastUpdateTime);
//        coordinateTextView.setText("Latitude is " + String.valueOf(mCurrentLocation.getLatitude())
        //              + "Longitude is" + String.valueOf(mCurrentLocation.getLongitude()));
        mapFragment.getMapAsync(this);
        //startAddressIntentService();


    }
/*

    protected void startAddressIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        startService(intent);
    }*/


    //permissions
    private void checkForPermissions() {
        //check if permissions are granted, and then continue
        permissionLocationCheckFine = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocationCheckFine != PackageManager.PERMISSION_GRANTED) {
            //permission not granted. proceed to ask
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
/*
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

        addressTextView = (TextView) findViewById(R.id.addressTextView);
        //lastUpdatedTime = (TextView) findViewById(R.id.lastUpdatedTime);

        bikeFinderTextOne.setTypeface(futura);
        bikeFinderTextTwo.setTypeface(futura);
        bikeFinderTextThree.setTypeface(futura);
        findBikeText.setTypeface(futuraItalic);
        //typefaces set


    }*/


    //to add markers, and more map settings after the map gets initialized
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        markerOptions4 = new MarkerOptions().position(mysoreRuralTerminus).title("Mysore Rural Terminus");
        markerOptions5 = new MarkerOptions().position(inoxMysore).title("INOX Mysore");


        //add markers for the above latlng
        googleMap.addMarker(markerOptions1);
        googleMap.addMarker(markerOptions2);
        googleMap.addMarker(markerOptions3);
        googleMap.addMarker(markerOptions4);
        googleMap.addMarker(markerOptions5);


        googleMap.moveCamera(CameraUpdateFactory.newLatLng(mysoreJunction));

        //TODO make the markers a global variable, and then use them to verify onMarkerClick

        googleMap.setMinZoomPreference(15.0f);
        LatLng cycleStart = new LatLng(12.315098, 76.645260), cycleEnd = markerOptions2.getPosition();
        animateMarker(cycleStart, cycleEnd, false);

    }


    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    public void animateMarker(final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker) {


        final Marker marker = mMap.addMarker(new MarkerOptions()
                .position(startPosition)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("bicycle_marker", 62, 174))));


        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 5000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        //sliderlayout is shown

        RelativeLayout sliderLayout = (RelativeLayout) findViewById(R.id.sliderLayout);
        sliderLayout.setVisibility(View.VISIBLE);
        TextView titleTextDockInfo = (TextView) findViewById(R.id.dockTitle);
        titleTextDockInfo.setText("Critical Station Kiosk");

        return false;
    }


    void updateVars() {
        String output = "";
        try {
            URL url = new URL("https://" + API_KEY + ":" + API_PASS + "@" + API_KEY + ".cloudant.com/testdatabase/_all_docs?include_docs=true");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            // open the stream and put it into BufferedReader
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Encoding", "gzip");
            conn.setRequestProperty("Authorization", "Basic NWU1M2JhYzEtZDJmYS00MTMzLTljMjUtN2U5ZTVkZGEwNGZiLWJsdWVtaXg6ZjA0YTlmYzg4ZWZiODIxOTM0ODMwNmRmYzRmMTMyOWVhYjUxODdlYTI5NWE2NjViMjk1NWY2N2E0M2JiNTU0ZA==");
            conn.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
            HttpsURLConnection.setFollowRedirects(true);
            Log.d("LOL", String.valueOf(conn.getResponseCode()));
/*
            // get redirect url from "location" header field
           String newUrl = conn.getHeaderField("Location");
            Log.d("NEW URL", newUrl+"::::lol");
            // get the cookie if need, for login
            String cookies = conn.getHeaderField("Set-Cookie");

            // open the new connnection again
            conn = (HttpsURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("User-Agent", "Mozilla");
*/
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                output += inputLine;
            }
            br.close();
            //Log.d("RESPONSE",IOUtils.toString(url));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                JSONArray rows = new JSONObject(output).getJSONArray("rows");
                if (rows != null) {
                    int pos = 0, maxval = -1;
                    for (int i = 0; i < rows.length(); i++) {
                        String record = rows.getJSONObject(i).getString("id");
                        if (record.contains("record")) {
                            int val = Integer.valueOf(record.substring(6));
                            if (val > maxval) {
                                maxval = val;
                                pos = i;
                            }
                        }
                    }
                    String key = rows.getJSONObject(pos).getString("id");
                    Log.d("TAG_KEY", key);

                    extractValuesFromKey(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setTextViewVars() {
        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                ((TextView) findViewById(R.id.temperatureNumber)).setText(String.valueOf(temp));
                ((TextView) findViewById(R.id.humidityNumber)).setText(String.valueOf(humidity));
            }
        };
        mHandler.sendEmptyMessage(0);

    }

    private void extractValuesFromKey(String key) {
        String output = "";
        try {
            URL url = new URL("https://" + API_KEY + ":" + API_PASS + "@" + API_KEY + ".cloudant.com/testdatabase/_all_docs?include_docs=true&&key=" + "%22" + key + "%22");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Encoding", "gzip");
            conn.setRequestProperty("Authorization", "Basic NWU1M2JhYzEtZDJmYS00MTMzLTljMjUtN2U5ZTVkZGEwNGZiLWJsdWVtaXg6ZjA0YTlmYzg4ZWZiODIxOTM0ODMwNmRmYzRmMTMyOWVhYjUxODdlYTI5NWE2NjViMjk1NWY2N2E0M2JiNTU0ZA==");
            conn.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
            HttpsURLConnection.setFollowRedirects(true);
            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                output += inputLine;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                JSONArray rows = new JSONObject(output).getJSONArray("rows");
                JSONObject d = rows.getJSONObject(0).getJSONObject("doc").getJSONObject("payload")
                        .getJSONObject("d");
                temp = d.getInt("temp");
                humidity = d.getInt("humidity");
                pressure = d.getInt("pressure");
                setTextViewVars();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class UpdateVariables extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            updateVars();
            return null;
        }
    }
}