package io.github.freefalltech.bikeshare;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MyActivity extends AppCompatActivity {


    //note pass the refreshtextview parameters as follows: calorie, elevation, weather, distance, humidity.

    TextView calorieNumberText;
    TextView elevationNumberText;
    TextView distanceNumberText;
    TextView weatherNumberText;
    TextView humidityNumberText;


    //TODO get from JSON

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity);
        //formatting textviews to respective typefaces
        formatTextViews();

    }

    private void formatTextViews() {

        //initialize typeface
        Typeface helvetica = Typeface.createFromAsset(getAssets(), "fonts/helvetica.ttf");
        Typeface arialblack = Typeface.createFromAsset(getAssets(), "fonts/arialblack.ttf");
        Typeface adam = Typeface.createFromAsset(getAssets(), "fonts/adam.otf");
        Typeface futura = Typeface.createFromAsset(getAssets(), "fonts/futur.ttf");
        Typeface futuraItalic = Typeface.createFromAsset(getAssets(), "fonts/futura_italic.ttf");


        TextView myActivityText1 = (TextView) findViewById(R.id.myActivityText1);
         calorieNumberText = (TextView) findViewById(R.id.colorieNumberText);
        TextView elevationTitleText = (TextView) findViewById(R.id.elevationTitleText);
         elevationNumberText = (TextView) findViewById(R.id.elevationNumberText);
        TextView distanceTitleText = (TextView) findViewById(R.id.distanceTitleText);
         distanceNumberText = (TextView) findViewById(R.id.distanceNumberText);
        TextView weatherText1 = (TextView) findViewById(R.id.weatherText1);
        TextView weatherText2 = (TextView) findViewById(R.id.weatherText2);
         weatherNumberText = (TextView) findViewById(R.id.weatherNumText);
         humidityNumberText = (TextView) findViewById(R.id.humidityNumberText);



        myActivityText1.setTypeface(futura);
        calorieNumberText.setTypeface(helvetica);
        elevationTitleText.setTypeface(futura);
        elevationNumberText.setTypeface(helvetica);
        distanceTitleText.setTypeface(futura);
        distanceNumberText.setTypeface(helvetica);
        weatherText1.setTypeface(futura);
        weatherText2.setTypeface(futura);
        weatherNumberText.setTypeface(helvetica);
        humidityNumberText.setTypeface(helvetica);


    }

    public void refreshTextViews(String calorie, String elevation, String weather, String distance, String humidity){
        calorieNumberText.setText(calorie);
        elevationNumberText.setText(elevation);
        weatherNumberText.setText(weather);
        humidityNumberText.setText(humidity);
        distanceNumberText.setText(distance);

    }
}
