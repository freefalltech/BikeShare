package io.github.freefalltech.bikeshare;

import android.os.ResultReceiver;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by abilashsenthilkumar on 27/05/17.
 */

public class FetchAddressIntentService extends IntentService {
    protected ResultReceiver mReceiver;

    public FetchAddressIntentService(){
        super("FetchAddressIs");
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage ="";
        List<Address> addresses = null;
        //getting location parceled from the intent extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf("lol", "No receiver received. There is nowhere to send the results.");
            return;
        }


        try{
            //third argument is 1 temporarly for now. single address will be returned
            addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
        }catch (IOException ioException){
            //catching io problems
            errorMessage = "service not available";
            Log.e("ERROR_ADDRESS_GEOCODER", errorMessage, ioException);
        }catch (IllegalArgumentException illegalArgException){
            //invalid lat and long supplied
            errorMessage = "Coordinates not provided properly";
            Log.e("INVALID_COORDINATES", errorMessage, illegalArgException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "Address not found";
                Log.e("ADDRESS_NOT_FOUND", errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i("ADDRESS_FOUND", TextUtils.join(System.getProperty("line.separator"), addressFragments));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }


    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
