package com.medina.falcutila.farey;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PricesActivity extends AppCompatActivity {

    ListView price_list;
    TextView chosen_origin_display;
    TextView chosen_destination_display;
    TextView taxi_price_display;

    TextView time_text_display;
    TextView distance_text_display;

    // ListView Items

    ArrayList<String> items;

    // For Listview Item Details

    String[] from = {"price_text"};
    int[] to = {R.id.price_text};

    ArrayAdapter<String> adapter;

    Formula formula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        price_list = (ListView) findViewById(R.id.price_list);
        chosen_origin_display = (TextView) findViewById(R.id.chosen_origin_display);
        chosen_destination_display = (TextView) findViewById(R.id.chosen_destination_display);

        formula = new Formula();

        items = new ArrayList<>();

        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.pricelist_item, items);
        price_list.setAdapter(adapter);

        setChosenDisplay();
        new APICall().execute();
    }

    public void setChosenDisplay() {
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");

        chosen_origin_display.setText(origin);
        chosen_destination_display.setText(destination);
    }

    public void setPriceDisplay(String s) {
        String distance_string = getIntent().getStringExtra("distance_string");
        String distance_int = getIntent().getStringExtra("distance_int");
        String time_string = getIntent().getStringExtra("time_string");
        String time_int = getIntent().getStringExtra("time_int");

        int dist_init = Integer.parseInt(distance_int);
        int time_init = Integer.parseInt(time_int);

        float dist_final = formula.toKilometers(dist_init);
        int time_final = formula.toMinutes(time_init);

        try{
            Log.d("HTTP", "try");
            items.clear();
            // JSON Parsing of data
            JSONArray jsonArray = new JSONArray(s);

            for(int a = 0; a < jsonArray.length(); a++) {
                JSONObject app = jsonArray.getJSONObject(a);

                int base = Integer.parseInt(app.getString("base_fare"));
                int per_min = Integer.parseInt(app.getString("per_min"));
                float per_km = Float.parseFloat(app.getString("per_km"));

                float compute_price = formula.Formula(dist_final, time_final, base, per_min, per_km);
                Log.d("HTTP", a+1 + " " + compute_price);
                items.add("₱ " + compute_price);
            }

            Log.d("HTTP", items.size() + "");

            price_list.setAdapter(adapter);
        } catch(Exception e) {
            Log.d("HTTP", e.getMessage());
        }
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

    /* ----------------------------- ASYNCTASK FOR API CALLS (JANJAN) ------------------------------- */

    private class APICall extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("HTTP", "APICall onPreExecute");
        }

        @Override
        protected String doInBackground(String... params) {
            // implement API in background and store the response in current variable
            String current = "";

            try {

                URL url;
                HttpURLConnection urlConnection = null;

                try {
                    url = new URL("https://farey-3d3a4.firebaseio.com/ride_shares/.json");
                    Log.d("HTTP", "Requesting: " + url.toString());

                    urlConnection = (HttpURLConnection) url
                            .openConnection();

                    InputStream in = urlConnection.getInputStream();

                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();

                    while (data != -1) {
                        current += (char) data;
                        data = isw.read();
                    }

                    // return the data to onPostExecute method
                    return current;

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();

                Log.d("HTTP", "Error: " + e.getMessage());
                return "Exception: " + e.getMessage();
            }
            return current;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String s) {
            Log.d("HTTP", "REACHED POST EXECUTE");
            setPriceDisplay(s);
        }
    }

    /* ---------------------------------------------------------------------------------------------- */
}
