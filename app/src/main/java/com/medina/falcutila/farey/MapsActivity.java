package com.medina.falcutila.farey;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // SharedPreferences to store data

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private GoogleMap mMap;

    /* For 1 second text change listener */

    long delay = 1000; // 1 seconds after user stops typing
    long last_text_edit = 0;
    Handler handler = new Handler();

    // Containers

    RelativeLayout transitionsContainer;
    RelativeLayout input_con;
    RelativeLayout prices_display_con;
    ListView result_con;
    LinearLayout taxi_icon_con;

    // Images, TextViews, Inputs, and Buttons

    EditText origin_input;
    EditText destination_input;
    Button compute_butt;
    ImageView whitebg;
    ImageButton get_location;
    TextView distance_display;
    TextView taxi_cab_price;

    // Result Container Status

    Boolean result_on = false;

    // ListView Items

    ArrayList<Map<String, String>> items;

    // For Listview Item Details

    String[] from = {"location_name", "location_address", "location_placeid"};
    int[] to = {R.id.loc_name, R.id.loc_add, R.id.loc_place_id};

    SimpleAdapter adapter;

    // Google Maps API

    String search_url = "https://maps.googleapis.com/maps/api/place/details/json";
    String autocomp_url = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
    String dist_url = "https://maps.googleapis.com/maps/api/distancematrix/json";
    String geocode_url = "https://maps.googleapis.com/maps/api/geocode/json";

    // Google Maps API Key

    String API_KEY = "AIzaSyApUoTRdW6dG7k-eoaBW9azQPJ_JGaLU8s";

    // For Random String

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    String SESSION_TOKEN;

    Boolean origin_chosen = true;

    String final_origin_name;
    String final_destination_name;

    String final_origin_placeid;
    String final_destination_placeid;

    // Map LatLng

    LatLng origin_latlng;
    LatLng destination_latlng;

    float origin_lat = 0.0f;
    float origin_long = 0.0f;

    float destination_lat;
    float destination_long;

    float user_loc_lat = 0.0f;
    float user_loc_long = 0.0f;

    String user_loc_name;
    String user_loc_placeid;

    // Map Markers

    Marker origin_marker;
    Marker destination_marker;

    // Map Bounds

    LatLngBounds.Builder builder;

    LocationManager locationManager;
    LocationListener locationListener;

    // Polyline to draw line

    Polyline line = null;

    Formula formula;

    String distance_res_string = "";
    String distance_res_int = "";
    String time_res_string = "";
    String time_res_int = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        transitionsContainer = (RelativeLayout) findViewById(R.id.transitions_container);
        input_con = (RelativeLayout) findViewById(R.id.input_con);
        prices_display_con = (RelativeLayout) findViewById(R.id.prices_display_con);
        result_con = (ListView) findViewById(R.id.result_con);
        whitebg = (ImageView) findViewById(R.id.whitebg);
        taxi_icon_con = (LinearLayout) findViewById(R.id.taxi_icon_con);

        origin_input = (EditText) findViewById(R.id.origin_input);
        destination_input = (EditText) findViewById(R.id.destination_input);
        compute_butt = (Button) findViewById(R.id.compute_butt);
        get_location = (ImageButton) findViewById(R.id.get_location);
        distance_display = (TextView) findViewById(R.id.distance_display);
        taxi_cab_price = (TextView) findViewById(R.id.taxi_cab_price);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        editor.apply();

        SESSION_TOKEN = randomAlphaNumeric(15);

        Log.d("HTTP", "SESSION_TOKEN: " + SESSION_TOKEN);

        items = new ArrayList<Map<String, String>>();

        builder = new LatLngBounds.Builder();

        formula = new Formula();

        /* -------------------- ORIGIN AND DESTINATION LISTENERS (JANJAN) ----------------------- */

        origin_input.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                openSearchResults();
            }

        });

        destination_input.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                openSearchResults();
            }

        });

        origin_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                openSearchResults();
                origin_chosen = true;
            }

        });


        destination_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                openSearchResults();
                origin_chosen = false;
            }

        });

        compute_butt.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                if (origin_marker != null && destination_marker != null) {
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

                    builder.include(origin_marker.getPosition());
                    builder.include(destination_marker.getPosition());

                    LatLngBounds bounds = builder.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);

                    TransitionManager.beginDelayedTransition(transitionsContainer);
                    taxi_icon_con.setVisibility(View.GONE);
                    compute_butt.setVisibility(View.GONE);

                    Log.d("HTTP", "Compute Button Clicked");

                    Log.d("HTTP", "Origin Name: " + final_origin_name);
                    Log.d("HTTP", "Origin Place ID: " + final_origin_placeid);
                    Log.d("HTTP", "Origin Latitude: " + origin_lat);
                    Log.d("HTTP", "Origin Longitude: " + origin_long);

                    Log.d("HTTP", "Destination Name: " + final_destination_name);
                    Log.d("HTTP", "Destination Place ID: " + final_destination_placeid);
                    Log.d("HTTP", "Destination Latitude: " + destination_lat);
                    Log.d("HTTP", "Destination Longitude: " + destination_long);

                    savePreferences();

                    String query = "origins=place_id:" + final_origin_placeid + "&destinations=place_id:" +
                            final_destination_placeid + "&key=" + API_KEY;

                    new APICall().execute("matrix", query);

                    createPolyline();

                } else {
                    Log.d("HTTP", "Origin and Destination are Required.");
                }

            }
        });

        get_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HTTP", "Setting User Location to Current Location");

                origin_lat = user_loc_lat;
                origin_long = user_loc_long;
                final_origin_name = user_loc_name;
                final_origin_placeid = user_loc_placeid;

                origin_latlng = new LatLng(origin_lat, origin_long);

                origin_marker = mMap.addMarker(new MarkerOptions().position(origin_latlng).title(final_origin_name));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(origin_latlng)      // Sets the center of the map to Mountain View
                        .zoom(17)                   // Sets the zoom\
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                origin_input.setText(final_origin_name);
            }
        });

        prices_display_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PricesActivity.class);
                intent.putExtra("origin", final_origin_name);
                intent.putExtra("destination", final_destination_name);
                intent.putExtra("distance_string", distance_res_string);
                intent.putExtra("distance_int", distance_res_int);
                intent.putExtra("time_string", time_res_string);
                intent.putExtra("time_int", time_res_int);

                startActivity(intent);
            }
        });

        /* --------------------------------------------------------------------------------- */

        /* ------------------------ LISTVIEW ADAPTER SETTINGS (JANJAN) --------------------- */

        adapter = new SimpleAdapter(this, items, R.layout.listview_item, from, to);
        result_con.setAdapter(adapter);

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                Log.d("HTTP", "Search Result Item Clicked");
                Log.d("HTTP", "Location Name: " + getLocName(item));
                Log.d("HTTP", "Location Place ID: " + getLocPlaceID(item));

                if (origin_chosen) {
                    final_origin_name = getLocName(item);
                    final_origin_placeid = getLocPlaceID(item);

                    origin_input.setText(final_origin_name);

                    new APICall().execute("search", "placeid=" + final_origin_placeid + "&key=" + API_KEY, "origin", "");

                    Log.d("HTTP", "Origin Name: " + final_origin_name);
                    Log.d("HTTP", "Origin Place ID: " + final_origin_placeid);
                } else {
                    final_destination_name = getLocName(item);
                    final_destination_placeid = getLocPlaceID(item);

                    destination_input.setText(final_destination_name);

                    new APICall().execute("search", "placeid=" + final_destination_placeid + "&key=" + API_KEY, "destination", "");

                    Log.d("HTTP", "Destination Name: " + final_destination_name);
                    Log.d("HTTP", "Destination Place ID: " + final_destination_placeid);
                }
            }
        };

        result_con.setOnItemClickListener(listener);

        /* ---------------------------------------------------------------------------------- */

        /* -------------------- 1 SECOND TEXT CHANGE LISTENER (JANJAN) ---------------------- */

        /* For 1 second text change listener for ORIGIN INPUT */

        origin_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                //You need to remove this to run only once
                handler.removeCallbacks(input_finish_checker_origin);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker_origin, delay);
                }
            }
        });

        /* For 1 second text change listener for DESTINATION INPUT */

        destination_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                //You need to remove this to run only once
                handler.removeCallbacks(input_finish_checker_destination);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker_destination, delay);
                }
            }
        });

        /* ------------------------------------------------------------------------------- */

        /* -------------------- GET USERS CURRENT LOCATION (JANJAN) ---------------------- */

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (user_loc_lat == 0.0f & user_loc_long == 0.0f) {
                    user_loc_lat = (float) location.getLatitude();
                    user_loc_long = (float) location.getLongitude();

                    Log.d("HTTP", "User Latitude Set");
                    Log.d("HTTP", "User Longitude Set");

                    new APICall().execute("geocode", "latlng=" + user_loc_lat + "," + user_loc_long + "&key=" + API_KEY);
                }

                Log.d("HTTP", "Latitude: " + (float) location.getLatitude());
                Log.d("HTTP", "Longitude: " + (float) location.getLongitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        /* -------------------------------------------------------------------------------- */
    }

    /* ---------------- RUNNABLES FOR 1 SECOND TEXT CHANGE LISTENER (JANJAN) -------------- */

    private Runnable input_finish_checker_origin = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                String input = origin_input.getText().toString();
                String query = "";

                if(!input.equalsIgnoreCase("")){
                    items.clear();
                    result_con.invalidateViews();

                    try{
                        query = "input=" + URLEncoder.encode(input,"UTF-8") + "&key=" + API_KEY + "&sessiontoken=" + SESSION_TOKEN;
                        new APICall().execute("autocomplete", query);
                    } catch (Exception e) {
                        Log.d("HTTP", e.getMessage());
                    }
                }

                Log.d("HTTP", "Initial Query for Origin: " + query);
            }
        }
    };


    private Runnable input_finish_checker_destination = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {

                String input = destination_input.getText().toString();
                String query = "";

                if(!input.equalsIgnoreCase("")){
                    items.clear();
                    result_con.invalidateViews();

                    try{
                        query = "input=" + URLEncoder.encode(input,"UTF-8") + "&key=" + API_KEY + "&sessiontoken=" + SESSION_TOKEN;
                        new APICall().execute("autocomplete", query);
                    } catch (Exception e) {
                        Log.d("HTTP", e.getMessage());
                    }
                }

                Log.d("HTTP", "Initial Query for Destination: " + query);
            }
        }
    };

    /* ----------------------------------------------------------------------------------------------------- */

    /* -------------------------------- OPEN AND CLOSE SEARCH RESULTS (ALEX) ---------------------------- */

    /** Call Function to open Search Results */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void openSearchResults() {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        RelativeLayout.LayoutParams input_con_params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        input_con_params.setMargins(0, 0, 0, 0);
        input_con_params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        input_con.setBackgroundResource(R.drawable.white_bg);
        input_con.setPaddingRelative(   30, 30, 30, 30);

        result_on = true; // Change variable value for next use
        compute_butt.setVisibility(View.GONE); // Hide Compute Button
        taxi_icon_con.setVisibility(View.GONE);
        whitebg.setVisibility(View.VISIBLE);

        ViewCompat.setElevation(input_con, 8); // Remove Elevation

        input_con.setLayoutParams(input_con_params);
    }

    /* Call Function to close Search Results */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void closeSearchResults() {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        closeKeyboard();

        RelativeLayout.LayoutParams input_con_params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        int margins = dpToPx(10);

        input_con_params.setMargins(margins, 0, margins, margins);
        input_con_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        input_con.setBackgroundResource(R.drawable.round_borders);

        result_on = false; // Change variable value for next use
        compute_butt.setVisibility(View.VISIBLE); // Show Compute Button
        taxi_icon_con.setVisibility(View.VISIBLE);
        whitebg.setVisibility(View.GONE);
        prices_display_con.setVisibility(View.GONE);

        input_con.setLayoutParams(input_con_params);

        ViewCompat.setElevation(input_con, 8); // Remove Elevation
    }

    /* ---------------------------------------------------------------------------------------------- */

    /* ------------------------------- IMPLEMENT GOOGLE MAPS (JANJAN) ------------------------------- */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Boolean status = preferences.getBoolean("saved_init", false);

        if(status) setDataPreferences(); else setupPreferences();

        Log.d("HTTP", "onMapReady running...");
    }

    /* ---------------------------------------------------------------------------------------------- */

    /* ----------------------------- SHAREDPREFERENCES FUNCTIONS (JANJAN) -------------------------- */

    public void setupPreferences() {
        editor.putBoolean("saved_init", true);

        editor.putString("saved_placename", "");
        editor.putString("saved_placeid", "");
        editor.putFloat("saved_lat", 0.0f);
        editor.putFloat("saved_long", 0.0f);

        editor.apply();
    }

    public void savePreferences() {
        editor.putString("saved_placename", final_origin_name);
        editor.putString("saved_placeid", final_origin_placeid);
        editor.putFloat("saved_lat", origin_lat);
        editor.putFloat("saved_long", origin_long);

        editor.apply();
    }

    public void setDataPreferences() {
        Float saved_lat = preferences.getFloat("saved_lat", 0.0f);
        Float saved_long = preferences.getFloat("saved_long", 0.0f);
        String saved_placename = preferences.getString("saved_placename", "");
        String saved_placeid = preferences.getString("saved_placeid", "");

        origin_lat = saved_lat;
        origin_long = saved_long;

        final_origin_name = saved_placename;
        final_origin_placeid = saved_placeid;

        origin_latlng = new LatLng(origin_lat, origin_long);

        origin_marker = mMap.addMarker(new MarkerOptions().position(origin_latlng).title(final_origin_name));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(origin_latlng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom\
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        origin_input.setText(saved_placename);
    }

    /* ---------------------------------------------------------------------------------------------- */

    /* ----------------------------------- SET PRICES DISPLAY --------------------------------------- */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setPrices(int dist_init, int time_init, String distance, String duration) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        prices_display_con.setVisibility(View.VISIBLE);
        get_location.setVisibility(View.GONE);
        taxi_icon_con.setVisibility(View.GONE);
        compute_butt.setVisibility(View.GONE);

        distance_res_string = distance;
        distance_res_int = dist_init + "";

        time_res_string = duration;
        time_res_int = time_init + "";

        float dist_final = formula.toKilometers(dist_init);
        int time_final = formula.toMinutes(time_init);

        distance_display.setText(distance + " in " + duration);
        taxi_cab_price.setText("₱" + formula.TaxiAirport(dist_final, time_final));
    }

    /* ----------------------------------------------------------------------------------------------- */

    /* ----------------------------------- CREATE POLYLINE (JANJAN) --------------------------------- */

    public void createPolyline() {
        if(line != null)
            line.remove();

        line = mMap.addPolyline(new PolylineOptions()
                .add(origin_latlng, destination_latlng)
                .width(5)
                .color(Color.RED));
    }

    /* ----------------------------------------------------------------------------------------------- */

    /* --------------------------------- HANDLE BACK BUTTON (JANJAN) --------------------------------- */

    /* Handle back button press to close search results. */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBackPressed() { // for API level 5 and greater
        if(result_on) closeSearchResults();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // older than API 5
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(result_on) closeSearchResults();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /* -------------------------------------------------------------------------------------------------- */

    /* ----------------------------------- FUNCTION TOOLS (JANJAN) -------------------------------------- */

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public String getLocName(String input) {
        String patternString = "location_name=([A-Za-z0-9\\s/-]*)";
        String result = "";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);

        while(matcher.find()) {
            if(result.equalsIgnoreCase("")){
                result = matcher.group(1);
            }
        }

        return result;
    }

    public String getLocPlaceID(String input) {
        String patternString = "location_placeid=([A-Za-z0-9_/-]*)";
        String result = "";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);

        while(matcher.find()) {
            if(result.equalsIgnoreCase("")){
                result = matcher.group(1);
            }
        }

        return result;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private HashMap<String, String> putData(String loc, String address, String placeid) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("location_name", loc);
        item.put("location_address", address);
        item.put("location_placeid", placeid);
        return item;
    }

    /* ---------------------------------------------------------------------------------------------- */

    /* ----------------------------- ASYNCTASK FOR API CALLS (JANJAN) ------------------------------- */

    private class APICall extends AsyncTask<String, String, String> {

        String request_type = "";
        String search_option = "";
        Boolean from_geocode = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("HTTP", "APICall onPreExecute");
        }

        @Override
        protected String doInBackground(String... params) {

            // implement API in background and store the response in current variable
            String current = "";
            String useURL = "";

            request_type = params[0];

            switch(request_type) {
                case "search":
                    useURL = search_url;
                    search_option = params[2];

                    if(!params[3].equalsIgnoreCase(""))
                        if(params[3].equalsIgnoreCase("FROM GEOCODE"))
                            from_geocode = true;

                    break;
                case "matrix":
                    useURL = dist_url;

                    break;
                case "autocomplete":
                    useURL = autocomp_url;

                    break;
                case "geocode":
                    useURL = geocode_url;

                    break;
            }

            try {

                URL url;
                HttpURLConnection urlConnection = null;

                try {
                    url = new URL(useURL + "?" + params[1]);
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

//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                Log.d("HTTP", "Error: " + e.getMessage());
                return "Exception: " + e.getMessage();
            }
            return current;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String s) {
            try {
                // JSON Parsing of data
                JSONObject jsonobj = new JSONObject(s);

                if(request_type.equalsIgnoreCase("autocomplete")) {

                    JSONArray predictions = jsonobj.getJSONArray("predictions");
                    Log.d("HTTP", "Total Predictions: " + predictions.length());

                    for(int a = 0; a < predictions.length(); a++) {
                        JSONObject predict = predictions.getJSONObject(a);

                        JSONObject structured = predict.getJSONObject("structured_formatting");

                        items.add(putData(structured.getString("main_text"),
                                structured.getString("secondary_text"),
                                predict.getString("place_id")));
                    }

                    result_con.setAdapter(adapter);

                } else if(request_type.equalsIgnoreCase("geocode")) {

                    JSONArray results = jsonobj.getJSONArray("results");

                    JSONObject first_result = results.getJSONObject(0); // Get First Result

                    String formatted_address = first_result.getString("formatted_address");
                    String place_id = first_result.getString("place_id");

                    user_loc_name = formatted_address;
                    user_loc_placeid = place_id;

                    Log.d("HTTP", "Geocode Result Name: " + formatted_address);
                    Log.d("HTTP", "Geocode Result Address: " + place_id);

                    JSONObject geometry = first_result.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");

                    user_loc_lat = Float.parseFloat(location.getString("lat"));
                    user_loc_long = Float.parseFloat(location.getString("lng"));

                    TransitionManager.beginDelayedTransition(transitionsContainer);

                    get_location.setVisibility(View.VISIBLE);

                    new APICall().execute("search", "placeid=" + place_id + "&key=" + API_KEY, "origin", "FROM GEOCODE");

                } else if(request_type.equalsIgnoreCase("search")) {

                    JSONObject result = jsonobj.getJSONObject("result");

                    JSONObject geometry = result.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");

                    if(search_option.equalsIgnoreCase("origin")) {

                        origin_lat = Float.parseFloat(location.getString("lat"));
                        origin_long = Float.parseFloat(location.getString("lng"));

                        if(!from_geocode) {
                            if(origin_marker != null) {
                                origin_marker.remove();
                            }

                            origin_latlng = new LatLng(origin_lat, origin_long);

                            origin_marker = mMap.addMarker(new MarkerOptions().position(origin_latlng).title(final_origin_name));

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin_latlng, 15));
                        }

                    } else if(search_option.equalsIgnoreCase("destination")) {

                        if(destination_marker != null) {
                            destination_marker.remove();
                        }

                        destination_lat = Float.parseFloat(location.getString("lat"));
                        destination_long = Float.parseFloat(location.getString("lng"));

                        destination_latlng = new LatLng(destination_lat, destination_long);

                        destination_marker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(destination_latlng)
                                        .title(final_destination_name)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination_latlng, 15));

                    }
                } else if(request_type.equalsIgnoreCase("matrix")) {
                    JSONArray rows = jsonobj.getJSONArray("rows");

                    JSONObject first_result = rows.getJSONObject(0);
                    JSONArray first_element = first_result.getJSONArray("elements");
                    JSONObject first_element_obj = first_element.getJSONObject(0);

                    JSONObject distance = first_element_obj.getJSONObject("distance");
                    JSONObject duration = first_element_obj.getJSONObject("duration");

                    int dist_init = Integer.parseInt(distance.getString("value"));
                    int time_init = Integer.parseInt(duration.getString("value"));

                    setPrices(dist_init, time_init, distance.getString("text"), duration.getString("text"));
                }

                Log.d("HTTP", "Result: " + s);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("HTTP", "JSONException: " + e.getMessage());
            }
        }
    }

    /* ---------------------------------------------------------------------------------------------- */
}
