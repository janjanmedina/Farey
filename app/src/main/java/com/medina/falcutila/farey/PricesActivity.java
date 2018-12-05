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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    // ListView Items

    ArrayList<Map<String, String>> items;

    // For Listview Item Details

    String[] from = {"rideshare", "price"};
    int[] to = {R.id.rideshare_text, R.id.price_text};

    ArrayList<String> packages;
    SimpleAdapter adapter;

    Formula formula;
    RideShare rideShare;

    FirebaseDatabase db;
    DatabaseReference reference;

    float dist_final;
    int time_final;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        price_list = (ListView) findViewById(R.id.price_listview);
        chosen_origin_display = (TextView) findViewById(R.id.chosen_origin_display);
        chosen_destination_display = (TextView) findViewById(R.id.chosen_destination_display);

        db = FirebaseDatabase.getInstance();
        reference = db.getReference("RideShare");

        formula = new Formula();

        items = new ArrayList<>();
        packages = new ArrayList<>();

        adapter = new SimpleAdapter(this, items, R.layout.pricelist_item, from, to);
        price_list.setAdapter(adapter);

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startNewActivity(getApplicationContext(), packages.get(position));
            }
        };

        price_list.setOnItemClickListener(listener);

        setChosenDisplay();
        setPriceDisplay();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();

                Log.d("HTTP", "ON CHANGE");
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    rideShare = ds.getValue(RideShare.class);

                    items.add(putData(ds.getKey(), compute(rideShare.getBaseFare(), rideShare.getPerKM(), rideShare.getPerMin())));

                    if(rideShare.getPkg() != null) {
                        packages.add(rideShare.getPkg());
                    } else {
                        packages.add("NULL");
                    }
                }

                price_list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private HashMap<String, String> putData(String rideshare, String price) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("rideshare", rideshare);
        item.put("price", price);
        return item;
    }

    public void setChosenDisplay() {
        Log.d("HTTP", "SET CHOSEN DISPLAY");
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");

        chosen_origin_display.setText(origin);
        chosen_destination_display.setText(destination);
    }

    public String compute(int baseFare, float perKM, int perMin) {
        float compute_price = formula.Formula(dist_final, time_final, baseFare, perMin, perKM);
        Log.d("HTTP", baseFare + " " + perKM + " " + perMin + " " + compute_price);

        return "₱ " + compute_price;
    }

    public void setPriceDisplay() {
        Log.d("HTTP", "SET PRICE DISPLAY");

        String distance_int = getIntent().getStringExtra("distance_int");
        String time_int = getIntent().getStringExtra("time_int");

        int dist_init = Integer.parseInt(distance_int);
        int time_init = Integer.parseInt(time_int);

        dist_final = formula.toKilometers(dist_init);
        time_final = formula.toMinutes(time_init);
    }

    public void startNewActivity(Context context, String packageName) {
        if(!packageName.equalsIgnoreCase("NULL")) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent == null) {
                // Bring user to the market or let them choose an app?
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + packageName));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
