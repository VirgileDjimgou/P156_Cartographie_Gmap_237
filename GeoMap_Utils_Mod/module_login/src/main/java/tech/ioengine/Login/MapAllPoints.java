package tech.ioengine.Login;

import android.Manifest;
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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import java.util.List;

import tech.ioengine.Login.data.StaticConfig;

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

    FloatingActionMenu materialDesignFAM;
    FloatingActionButton Dasboard , History , Adv_Settings, Road_Simulation, logout_disconnect;

    private Button mRideStatus;
    private Switch mWorkingSwitch;
    private int status = 0;
    private String customerId = "", destination;
    private String customer_Fcm_ID ="";
    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;
    private Boolean isLoggingOut = false;
    private SupportMapFragment mapFragment;
    private LinearLayout mCustomerInfo;
    private ImageView mCustomerProfileImage;
    private TextView mtrip_cost , mnbOfPassengers , mcustomer_Service ,
                     mServicesOptions, mCustomerName, mCustomerDestination;
    private Button mButtonDeclineDrive , mButtonAcceptDrive ;
    private DatabaseReference userDB;
    private FirebaseAuth mAuth;
    ImageView avatar;
    private Context context;
    private ProgressBar progressBar;
    private String ID_FCM ="";
    private String Customer_ID_FCM ="";


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

        // add old Activity
        //counter to take  a decisions ...
        progressBar = (ProgressBar) findViewById(R.id.CounterDriver);
        mCustomerInfo = (LinearLayout) findViewById(R.id.PassengerInfoCustom);
        // always mask this Layout  after the creation ....
        mCustomerInfo.setVisibility(View.GONE);

        mtrip_cost = (TextView) findViewById(R.id.trip_cost);
        mcustomer_Service = (TextView) findViewById(R.id.customer_Service);
        mnbOfPassengers = (TextView) findViewById(R.id.nbOfPassengers);
        mServicesOptions = (TextView) findViewById(R.id.ServicesOptions);
        mButtonDeclineDrive  = (Button) findViewById(R.id.btn_decline_drive) ;
        mButtonDeclineDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Passenger refused ", Toast.LENGTH_SHORT).show();
                // mask the  Dasboard ...

            }
        });



        mButtonAcceptDrive = (Button)  findViewById(R.id.btn_accept_passenger) ;
        mButtonAcceptDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Passenger accepted ", Toast.LENGTH_SHORT).show();

                try{

                }catch(Exception ex){
                    ex.printStackTrace();
                }


                // Start  the Triggers  to begin with the Trips  ...
                // getAssignedCustomer();

            }
        });


        mCustomerProfileImage = (ImageView) findViewById(R.id.profil_img_customer);

        mCustomerName = (TextView) findViewById(R.id.name_customer);
        // mCustomerPhone = (TextView) rootView .findViewById(R.id.customer_phone);
        mCustomerDestination = (TextView) findViewById(R.id.customer_destination);

        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    connectDriver();
                }else{
                    disconnectDriver();
                }
            }
        });



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
                LatLng sydney = new LatLng(-34, 151);
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });


        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        logout_disconnect = (FloatingActionButton) findViewById(R.id.menu_item_logout);


        logout_disconnect .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu first item clicked

                // launch new intent instead of loading fragment

                if(mCustomerInfo.isActivated() == true){
                    mCustomerInfo.setVisibility(View.GONE);
                    mCustomerInfo.setActivated(false);

                }else{


                    mCustomerInfo.setVisibility(View.VISIBLE);
                    mCustomerInfo.setActivated(true);

                }
            }
        });

    }

    public void quit() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startMain);
    }

    private void InitViewDashboard(){
        mButtonAcceptDrive.setEnabled(true);
        mButtonDeclineDrive.setEnabled(true);
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

    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation(){
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
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

            try{

                customerId = customerId;

                if(!customerId.equals("")){
                    rideDistance += mLastLocation.distanceTo(location)/1000;
                }


            }catch(Exception ex){
                mCustomerInfo.setVisibility(View.GONE);
                Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }


                mLastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        }
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

    private void connectDriver(){
        if (ActivityCompat.checkSelfPermission(MapAllPoints.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapAllPoints.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapAllPoints.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Toast.makeText(MapAllPoints.this, "Driver connected with Firebase db  ...." , Toast.LENGTH_LONG).show();
    }

    private void disconnectDriver(){

        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        }catch (Exception ex ){
            ex.printStackTrace();
            Toast.makeText(MapAllPoints.this, ex.toString() , Toast.LENGTH_LONG).show();

        }

        Toast.makeText(MapAllPoints.this, "Driver disconnected with Firebase db ...." , Toast.LENGTH_LONG).show();

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
