package tech.ioengine.Login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.irozon.sneaker.Sneaker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import tech.ioengine.Login.data.StaticConfig;
import tech.ioengine.Login.fotopicker.CardPack.MainActivity_card;
import tech.ioengine.Login.model.CustomPoint;
import tech.ioengine.Login.model.InfoWindowData;

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
    private FusedLocationProviderClient mFusedLocationProviderClient;





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
        Depart_autocompleteFragment.setHint("google Search ");
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


        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(MapAllPoints.this);


        mCustomerInfo = (LinearLayout) findViewById(R.id.PointInfos);
        mCustomerInfo.setVisibility(View.GONE);

        mCustomerProfileImage = (ImageView) findViewById(R.id.images2);
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
                // googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").sni

                getDeviceLocation();
                // get all saved Points and add this Points on the Maps
                getAllSavedOnFirebasePoints();
            }
        });

    }


    private void getDeviceLocation() {
        try {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location location = task.getResult();
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            // For zooming automatically to the location of the marker
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLatLng ).zoom(12).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                });

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
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
                    String PlaceChar = "";
                    CustomPoint NewPoint = new CustomPoint();
                    List<Bitmap> Images_of_Point = new ArrayList<Bitmap>();


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
                    }

                    if(dataSnapshot.child("charac").getValue() != null){
                        PlaceChar = dataSnapshot.child("charac").getValue().toString();
                        NewPoint.Charact = PlaceChar;
                    }


                    if(dataSnapshot.child("Image0")!=null){
                        String ImageUrl0 = dataSnapshot.child("Image0").toString();



                        Bitmap theBitmap = null;
                        try {


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(MapAllPoints.this)
                                            .load(ImageUrl0 )
                                            .into(-1, -1);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();}
                        Images_of_Point.add(theBitmap);
                        // Glide.with(getApplication()).load(ImageUrl0).into(mCustomerProfileImage);
                    }


                    if(dataSnapshot.child("Image1")!=null){
                        String ImageUrl0 = dataSnapshot.child("Image1").toString();

                        Bitmap theBitmap = null;
                        try {


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(MapAllPoints.this)
                                            .load(ImageUrl0 )
                                            .into(-1, -1);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();}

                        Images_of_Point.add(theBitmap);
                        // Glide.with(getApplication()).load(ImageUrl0).into(mCustomerProfileImage);
                    }


                    if(dataSnapshot.child("Image2")!=null){
                        String ImageUrl0 = dataSnapshot.child("Image2").toString();


                        Bitmap theBitmap = null;
                        try {


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(MapAllPoints.this)
                                            .load(ImageUrl0 )
                                            .into(-1, -1);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();}

                        Images_of_Point.add(theBitmap);
                        // Glide.with(getApplication()).load(ImageUrl0).into(mCustomerProfileImage);
                    }


                    if(dataSnapshot.child("Image3")!=null){
                        String ImageUrl0 = dataSnapshot.child("Image3").toString();


                        Bitmap theBitmap = null;
                        try {


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(MapAllPoints.this)
                                            .load(ImageUrl0 )
                                            .into(-1, -1);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();}

                        Images_of_Point.add(theBitmap);
                        // Glide.with(getApplication()).load(ImageUrl0).into(mCustomerProfileImage);
                    }


                    if(dataSnapshot.child("Image4")!=null){
                        String ImageUrl0 = dataSnapshot.child("Image4").toString();


                        Bitmap theBitmap = null;
                        try {


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(MapAllPoints.this)
                                            .load(ImageUrl0 )
                                            .into(-1, -1);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();}


                        Images_of_Point.add(theBitmap);
                        // Glide.with(getApplication()).load(theBitmap).into(mCustomerProfileImage);
                    }


                    pickupLatLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));


                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(pickupLatLng)
                            .title(PlaceName)
                            .snippet("Custom Pickup Place  by Beetech.")
                            .icon(BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_BLUE));



                    InfoWindowData info = new InfoWindowData();
                    info.setImage("");
                    info.setHotel("Place Infos : " +PlaceChar);
                    info.setFood("Infos : excellent hotels and Restaurant available");
                    info.setTransport("Reach the site with Bee Service .");


                    CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(MapAllPoints.this);
                    mMap.setInfoWindowAdapter(customInfoWindow);



                    Marker m = mMap.addMarker(markerOptions);
                    m.setTag(info);
                    m.showInfoWindow();
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bee_round));
                    Marker NewMarker;


                    NewPoint.listImages = Images_of_Point;
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

                mCustomerInfo.setVisibility(View.GONE);
            }

        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String locAddress = marker.getTitle();
                // fillTextViews(locAddress);

                marker.showInfoWindow();
                marker.getId();

                Sneaker.with(MapAllPoints.this)
                        .setTitle("Marker id !!")
                        .setMessage(marker.getId().toString())
                        .sneakSuccess();
                CustomPoint NewPoint = new CustomPoint();
                // mCustomerInfo.setVisibility(View.VISIBLE);

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
