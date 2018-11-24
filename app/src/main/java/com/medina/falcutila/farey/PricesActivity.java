package com.medina.falcutila.farey;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class PricesActivity extends AppCompatActivity {

    TextView chosen_origin_display;
    TextView chosen_destination_display;
    TextView taxi_price_display;

    TextView time_text_display;
    TextView distance_text_display;

    TextView grab_price;
    TextView angkas_price;
    TextView owto_price;
    TextView uhop_price;
    TextView micab_price;
    TextView hype_price;

    Formula formula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        chosen_origin_display = (TextView) findViewById(R.id.chosen_origin_display);
        chosen_destination_display = (TextView) findViewById(R.id.chosen_destination_display);
        taxi_price_display = (TextView) findViewById(R.id.taxi_price_display);

        time_text_display = (TextView) findViewById(R.id.time_text_display);
        distance_text_display = (TextView) findViewById(R.id.distance_text_display);

        grab_price = (TextView) findViewById(R.id.grab_price);
        angkas_price = (TextView) findViewById(R.id.angkas_price);
        owto_price = (TextView) findViewById(R.id.owto_price);
        uhop_price = (TextView) findViewById(R.id.uhop_price);
        micab_price = (TextView) findViewById(R.id.micab_price);
        hype_price = (TextView) findViewById(R.id.hype_price);

        formula = new Formula();

        setChosenDisplay();
        setPriceDisplay();
    }

    public void setChosenDisplay() {
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");

        chosen_origin_display.setText(origin);
        chosen_destination_display.setText(destination);
    }

    public void setPriceDisplay() {
        String distance_string = getIntent().getStringExtra("distance_string");
        String distance_int = getIntent().getStringExtra("distance_int");
        String time_string = getIntent().getStringExtra("time_string");
        String time_int = getIntent().getStringExtra("time_int");

        int dist_init = Integer.parseInt(distance_int);
        int time_init = Integer.parseInt(time_int);

        float dist_final = formula.toKilometers(dist_init);
        int time_final = formula.toMinutes(time_init);

        taxi_price_display.setText("₱" + formula.TaxiAirport(dist_final, time_final));

        time_text_display.setText(time_string);
        distance_text_display.setText(distance_string);

        grab_price.setText("₱" + formula.GrabCarFare(dist_final, time_final));
        angkas_price.setText("NYS");
        owto_price.setText("₱" + formula.owtoFare(dist_final, time_final));
        uhop_price.setText("₱" + formula.uHopSedanFare(dist_final, time_final));
        micab_price.setText("₱" + formula.micabFare(dist_init, time_final));
        hype_price.setText("₱" + formula.hypeFare(dist_final, time_final));

        Log.d("HTTP", "Taxi Airport: " + formula.TaxiAirport(dist_final, time_final));
        Log.d("HTTP", "Taxi Common: " + formula.TaxiCommon(dist_final, time_final));
        Log.d("HTTP", "Grab Car: " + formula.GrabCarFare(dist_final, time_final));
        Log.d("HTTP", "Grab 6 Seater: " + formula.Grab6SeaterFare(dist_final, time_final));
        Log.d("HTTP", "Grab Car Plus: " + formula.GrabCarPlusFare(dist_final, time_final));
        Log.d("HTTP", "uHop Sedan: " + formula.uHopSedanFare(dist_final, time_final));
        Log.d("HTTP", "uHop SUV: " + formula.uHopSUVFare(dist_final, time_final));
        Log.d("HTTP", "uHop Van: " + formula.uHopVanFare(dist_final, time_final));
        Log.d("HTTP", "Micab: " + formula.micabFare(dist_init, time_final));
        Log.d("HTTP", "Owto Car: " + formula.owtoFare(dist_final, time_final));
        Log.d("HTTP", "Hype Car: " + formula.hypeFare(dist_final, time_final));
    }

    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void bookRide(View v) {
        switch(v.getId()) {
            case R.id.grab_book:
                startNewActivity(this, "com.grabtaxi.passenger");
                break;
            case R.id.angkas_book:
                startNewActivity(this, "com.angkas.passenger");
                break;
            case R.id.owto_book:
                startNewActivity(this, "com.owtoph.rider");
                break;
            case R.id.hype_book:
                startNewActivity(this, "com.hype.user");
                break;
            case R.id.micab_book:
                startNewActivity(this, "com.micab.gleek.passenger");
                break;
            case R.id.uhop_book:
                startNewActivity(this, "com.uhop.app");
                break;
        }
    }
}
