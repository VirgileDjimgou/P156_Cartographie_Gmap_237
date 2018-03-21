package tech.ioengine.Login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.irozon.sneaker.Sneaker;

import java.util.ArrayList;
import java.util.List;

import tech.ioengine.Login.data.StaticConfig;
import tech.ioengine.Login.model.CustomPoint;

public class MapAllPoints extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    MapView mMapView;
    private GoogleMap googleMap;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;


    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;
    private Boolean isLoggingOut = false;
    private SupportMapFragment mapFragment;
    private LinearLayout mCustomerInfo;
    private ImageView mCustomerProfileImage;
    private DatabaseReference userDB;
    private FirebaseAuth mAuth;
    ImageView avatar;
    private Context context;
    private ProgressBar progressBar;
    private String ID_FCM ="";
    private String Customer_ID_FCM ="";
    PlaceAutocompleteFragment Depart_autocompleteFragment;
    private LatLng  position_depart_LatLng;
    private String  position_depart ="";
    public static  List<CustomPoint> AllPoints = new ArrayList<CustomPoint>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(StaticConfig.UID);
        setContentView(R.layout.activity_driver_map);


        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        context = getApplicationContext();

        mMapView.onResume(); // needed to get the map to display immediately

        if (ActivityCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapAllPoints.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapAllPoints.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }else{
            mMapView.getMapAsync(this);
        }

        try {
            MapsInitializer.initialize(MapAllPoints.this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        // init recherche google

        Depart_autocompleteFragment = (PlaceAutocompleteFragment)
                MapAllPoints.this.getFragmentManager().findFragmentById(R.id.place_depart);
        Depart_autocompleteFragment.setHint("Start point ...");
        Depart_autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                position_depart =  place.getName().toString();
                position_depart_LatLng = place.getLatLng();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });


        mCustomerInfo = (LinearLayout) findViewById(R.id.PointInfos);
        mCustomerInfo.setVisibility(View.GONE);

        mCustomerProfileImage = (ImageView) findViewById(R.id.images1);
        // reset point list

        try {
            // reset all list and empty
            AllPoints.clear();
            AllPoints.removeAll(AllPoints);

            //getAllSavedOnFirebasePoints();
            // Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;


                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //User has previously accepted this permission
                    if (ActivityCompat.checkSelfPermission(MapAllPoints.this.getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                    //Not in api-23, no need to prompt
                    googleMap.setMyLocationEnabled(true);
                }


                // googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                // LatLng sydney = new LatLng(-34, 151);
                // googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                pickupLatLng = new LatLng(41.4036339,2.1741417);
                Marker pickupMarker;
                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).
                        title("Custom Bee Pickup location").
                        icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bee_round)));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(pickupLatLng ).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                // get all saved Points and add this Points on the Maps
                getAllSavedOnFirebasePoints();
            }
        });

    }



    private void getAllSavedOnFirebasePoints() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Points");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private  void  FetchRideInformation(String PointsKey) {
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
                    CustomPoint NewPoint = new CustomPoint();


                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = dataSnapshot.child("timestamp").getValue().toString();
                        NewPoint.TimeSpan = timestamp;
                    }

                    if(dataSnapshot.child("lng").getValue() != null){
                        lng = dataSnapshot.child("lng").getValue().toString();
                        NewPoint.longitude = lng;
                    }

                    if(dataSnapshot.child("lat").getValue() != null){
                        lat = dataSnapshot.child("lat").getValue().toString();
                        NewPoint .latitude = lat;
                        // location_point.setLatitude(41.4036339);

                    }

                    if(dataSnapshot.child("Place").getValue() != null){
                        PlaceName = dataSnapshot.child("Place").getValue().toString();
                        NewPoint.namePoint = PlaceName;
//            Point.setAddress(PlaceName);
                    }


                    if(dataSnapshot.child("Image0")!=null){
                        Glide.with(MapAllPoints.this).
                                load("                        https://firebasestorage.googleapis.com/v0/b/beewallet-114e7.appspot.com/o/profile_images%2Fp7GZA1zyn4fnBWGf4b4io6M2IBA3?alt=media&token=1b6152c0-6dcc-407a-833a-9a4f9c63ec38\n").
                                into(mCustomerProfileImage);
                        // Glide.with(MapAllPoints.this).load(dataSnapshot.child("Image0").getValue().toString()).into(mCustomerProfileImage);
                    }


                    pickupLatLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    Marker NewMarker;

                    NewMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).
                            title(PlaceName).
                            icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bee_round)));

                    AllPoints.add(NewPoint);

                    //        Point.setLocation(location_point);
                    //        Point.setTitle("echoo Test");


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(MapAllPoints.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MapAllPoints.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                Log.d("arg0", arg0.latitude + "-" + arg0.longitude);

                Sneaker.with(MapAllPoints.this)
                        .setTitle("Success!!")
                        .setMessage(" SUcces  new google Point added !!!" )
                        .sneakSuccess();

                mCustomerInfo.setVisibility(View.GONE);
            }

        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String locAddress = marker.getTitle();
                // fillTextViews(locAddress);

                marker.showInfoWindow();
                mCustomerInfo.setVisibility(View.VISIBLE);

                return true;
            }
        });


    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(MapAllPoints.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(MapAllPoints.this!=null){

        }

        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (ActivityCompat.checkSelfPermission(MapAllPoints.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapAllPoints.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapAllPoints.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Toast.makeText(MapAllPoints.this, "Driver connected with Firebase db  ...." , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                } else{
                    Toast.makeText(MapAllPoints.this, "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }



}
