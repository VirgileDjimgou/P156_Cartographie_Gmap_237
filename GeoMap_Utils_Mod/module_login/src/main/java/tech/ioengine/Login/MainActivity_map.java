package tech.ioengine.Login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ioengine.geo_map.LekuPoi;
import com.ioengine.geo_map.LocationPicker;
import com.ioengine.geo_map.LocationPickerActivity;
import com.ioengine.geo_map.tracker.LocationPickerTracker;
import com.ioengine.geo_map.tracker.TrackEvents;
import com.irozon.sneaker.Sneaker;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import tech.ioengine.Login.activity.EmailLoginActivity;
import tech.ioengine.Login.activity.SplaschScreen;
import tech.ioengine.Login.fotopicker.CardPack.MainActivity_card;
import tech.ioengine.Login.model.CustomPoint;

public class MainActivity_map extends AppCompatActivity {

  public static final int MAP_BUTTON_REQUEST_CODE = 1;
  public static final int MAP_POIS_BUTTON_REQUEST_CODE = 2;
  private Context context;

  public String NewGooglePlace = "";

  // location...
  private double lat;
  private double lng;

  public static FirebaseUser user;
  private DatabaseReference mDriverDatabase;
  public static FirebaseAuth mAuth;
  public static  FirebaseAuth.AuthStateListener mAuthListener;
  public static String requestIdUpload= "";
  private int COMPRESSION; // not a big diff eh?
  DatabaseReference PointsRef;
  List<String> listfilePath = new ArrayList<String>();
  List<LekuPoi> CustomPoints = new ArrayList<>();



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StrictMode.setThreadPolicy(
            new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    setContentView(R.layout.activity_map);
    View mapButton = findViewById(R.id.map_button);
    context = this.getApplicationContext();


    // init firebase  ...
    PointsRef = FirebaseDatabase.getInstance().getReference().child("Points");
    mapButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent locationPickerIntent = new LocationPickerActivity.Builder()
                // .withLocation(48.8712585, 10.0159448)
                .withGeolocApiKey("AIzaSyAr1zz4D73tgDLmZ4R4GGIqB0HGR8CyizY")
                .withSearchZone("es_ES")
                //.shouldReturnOkOnBackPressed()
                //.withStreetHidden()
                //.withCityHidden()
                //.withZipCodeHidden()
                //.withSatelliteViewHidden()
                .withGooglePlacesEnabled()
                .build(getApplicationContext());

        //this is optional if you want to return RESULT_OK if you don't set the latitude/longitude and click back button
        locationPickerIntent.putExtra("test", "this is a test");

        startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE);
      }
    });

    View mapPoisButton = findViewById(R.id.map_button_with_pois);
    mapPoisButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {



        try {
        // reset all list and empty
        CustomPoints.clear();
        CustomPoints.removeAll(CustomPoints);

          //getAllSavedOnFirebasePoints();
          // Thread.sleep(5000);
        } catch (Exception e) {
          e.printStackTrace();
        }

        Intent locationPickerIntent = new LocationPickerActivity.Builder()
                // .withLocation(41.4036299, 2.1743558)
                .withPois(getAllSavedOnFirebasePoints())
                .withGooglePlacesEnabled()
                .build(getApplicationContext());

        startActivityForResult(locationPickerIntent, MAP_POIS_BUTTON_REQUEST_CODE);
      }
    });

    initializeLocationPickerTracker();
  }

  private List<LekuPoi> getLekuPois() {
    List<LekuPoi> pois = new ArrayList<>();

    Location locationPoi1 = new Location("leku");
    locationPoi1.setLatitude(41.4036339);
    locationPoi1.setLongitude(2.1721618);
    LekuPoi poi1 = new LekuPoi(UUID.randomUUID().toString(), "Los bellota", locationPoi1);
    pois.add(poi1);

    Location locationPoi2 = new Location("leku");
    locationPoi2.setLatitude(41.4023265);
    locationPoi2.setLongitude(2.1741417);
    LekuPoi poi2 = new LekuPoi(UUID.randomUUID().toString(), "Starbucks", locationPoi2);
    poi2.setAddress("Plaça de la Sagrada Família, 19, 08013 Barcelona");
    pois.add(poi2);

    return pois;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      Log.d("RESULT****", "OK");
      if (requestCode == 1) {
        String latitude = String.valueOf(data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0));
        Log.d("LATITUDE****", String.valueOf(latitude));
        String longitude = String.valueOf(data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0));
        Log.d("LONGITUDE****", String.valueOf(longitude));
        String address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);
        Log.d("ADDRESS****", String.valueOf(address));
        String postalcode = data.getStringExtra(LocationPickerActivity.ZIPCODE);
        Log.d("POSTALCODE****", String.valueOf(postalcode));
        Bundle bundle = data.getBundleExtra(LocationPickerActivity.TRANSITION_BUNDLE);
        Log.d("BUNDLE TEXT****", bundle.getString("test"));
        Address fullAddress = data.getParcelableExtra(LocationPickerActivity.ADDRESS);
        if (fullAddress != null) {
          Log.d("FULL ADDRESS****", fullAddress.toString());
        }

        new LovelyTextInputDialog(this, R.style.EditTextTintTheme)
                .setTopColorRes(R.color.leku_app_blue)
                .setTitle("new google place name")
                .setMessage("enter a new google place Name")
                .setIcon(R.drawable.about_location_icon)
                // .setInputFilter("invalid name ! ")
                //new LovelyTextInputDialog.TextFilter() {
                //@Override
                //public boolean check(String text) {
                //  return text.matches("\\w+");
                //}
                // })
                .setCancelable(true)
                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                  @Override
                  public void onTextInputConfirmed(String text) {

                    Intent intent = new Intent(getBaseContext(), MainActivity_card.class);
                    NewGooglePlace = text.toString();
                    intent.putExtra("Place_name", NewGooglePlace);
                    intent.putExtra("Lat", latitude);
                    intent.putExtra("Long", longitude);


                    /*
                    Sneaker.with(getParent())
                            .setTitle("Success!!")
                            .setMessage(" infos ... " + latitude + longitude + NewGooglePlace)
                            .sneakSuccess();

*/
                    startActivity(intent);

                  }
                })
                .show();
        // MainActivity_map.this.finish();


      } else if (requestCode == 2) {
        double latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0);
        Log.d("LATITUDE****", String.valueOf(latitude));
        double longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0);
        Log.d("LONGITUDE****", String.valueOf(longitude));
        String address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);
        Log.d("ADDRESS****", String.valueOf(address));
        LekuPoi lekuPoi = data.getParcelableExtra(LocationPickerActivity.LEKU_POI);
        Log.d("LekuPoi****", String.valueOf(lekuPoi));

        // nichts machen  ...
      }
    }
    if (resultCode == RESULT_CANCELED) {
      Log.d("RESULT****", "CANCELLED");
    }
  }


  private void initializeLocationPickerTracker() {
    LocationPicker.setTracker(new LocationPickerTracker() {
      @Override
      public void onEventTracked(TrackEvents event) {
        Toast.makeText(MainActivity_map.this, "Event: " + event.getEventName(), Toast.LENGTH_SHORT)
                .show();
      }
    });
  }


  // Fetch all Points on the DB  ....

  /// history  ...

  private List<LekuPoi>  getAllSavedOnFirebasePoints() {
    List<LekuPoi> pois = new ArrayList<>();
    DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Points");
    userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.exists()){
          for(DataSnapshot history : dataSnapshot.getChildren()){
            LekuPoi Point = FetchRideInformation(history.getKey());
            if(Point != null){
              pois.add(Point);
            }
          }
        }
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
      }
    });

    return pois;
  }

  private  LekuPoi FetchRideInformation(String PointsKey) {

    final LekuPoi[] PointGlobal = {null};
    DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("Points").child(PointsKey);
    historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
      @SuppressLint("SetTextI18n")
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.exists()){
          String PointId = dataSnapshot.getKey();
          String timestamp = "";
          String PlaceName = "";
          String lat = "";
          String lng = "";
          Location location_point = new Location("Bee Fire");

          if(dataSnapshot.child("timestamp").getValue() != null){
            timestamp = dataSnapshot.child("timestamp").getValue().toString();
          }

          if(dataSnapshot.child("lng").getValue() != null){
            lng = dataSnapshot.child("lng").getValue().toString();
            location_point.setLongitude(2.1741417);
          }

          if(dataSnapshot.child("lat").getValue() != null){
            lat = dataSnapshot.child("lat").getValue().toString();
            location_point.setLatitude(41.4036339);

          }

          LekuPoi Point = new LekuPoi(UUID.randomUUID().toString(), "Los bellota", location_point);


          Point = new LekuPoi(UUID.randomUUID().toString(), "Starbucks", location_point);

          if(dataSnapshot.child("Place").getValue() != null){
            PlaceName = dataSnapshot.child("Place").getValue().toString();
            Point.setAddress(PlaceName);
          }


          Point.setLocation(location_point);
          Point.setTitle("echoo Test");

          PointGlobal[0] = Point;

        }
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
      }
    });

    return PointGlobal[0];
  }




  private String getDate(Long time) {
    Calendar cal = Calendar.getInstance(Locale.getDefault());
    cal.setTimeInMillis(time*1000);
    String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
    return date;
  }

  private ArrayList allSavedPoint = new ArrayList<CustomPoint>();
  private ArrayList<CustomPoint> getallSavedPoints() {

    return allSavedPoint;
  }

}