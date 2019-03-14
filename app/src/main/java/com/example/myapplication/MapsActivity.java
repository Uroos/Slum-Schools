package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.School;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.myapplication.utils.Utils.isOnline;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final int RC_SIGN_IN = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = MapsActivity.class.getName();
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewKey";

    // To limit the calls to get current location
    private static int once = 0;

    private List<School> schools;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng currentLatLng;
    private double currentLatitude;
    private double currentLongitude;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @BindView(R.id.tv_error_message)
    TextView tvErrorMessage;

    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

       // AnimatedVectorDrawable menuToBack = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_menu_to_back);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        // This arraylist will contain all the markers downloaded from the database.
        schools = new ArrayList<>();

        // The toolbar appears in the layout but it's not functioning as the app bar.
        // To apply the toolbar as the app bar, call setSupportActionBar() and pass the Toolbar object from your layout.
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);
        // Now add the button that opens the navigation drawer.
        // Enable the app bar's "home" button by calling setDisplayHomeAsUpEnabled(true)
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
       actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        //actionbar.setHomeAsUpIndicator(menuToBack);
        //menuToBack.start();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // To resolve the error of not getting instance of database
        // Solution from: https://stackoverflow.com/questions/40081539/default-firebaseapp-is-not-initialized
        // Version com.google.gms:google-services:4.1.0 was crashing but com.google.gms:google-services:4.0.1 worked
        // so it was changed in the build.gradle file.
        FirebaseApp.initializeApp(MapsActivity.this);
        // Main access point for the database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Using the access point, get access to a specific place in database
        databaseReference = firebaseDatabase.getReference().child("location");

        firebaseAuth = FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
        }

        // Run this part only when activity starts for the first time. This sets the camera on user
        // location when app is first opened and permission is given. We don't want this code running
        // at every rotation of the phone, as we want to preserve the state of the map.
        if (once == 0) {
            once = 1;
            if (isOnline(this)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();
                                currentLatLng = new LatLng(currentLatitude, currentLongitude);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10));
                                //Log.v(TAG, "On current latitude is: " + location.getLatitude());
                            }
                        }
                    });
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
                } else {
                    Log.v(TAG, "On permission not granted");
                    currentLatitude = 0;
                    currentLongitude = 0;
                    currentLatLng = new LatLng(currentLatitude, currentLongitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10));
                }
            }
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (isOnline(this)) {
            tvErrorMessage.setVisibility(View.INVISIBLE);
            mapFragment.getView().setVisibility(View.VISIBLE);
            mapFragment.onCreate(mapViewBundle);
            mapFragment.getMapAsync(this);
        } else {
            tvErrorMessage.setVisibility(View.VISIBLE);
            mapFragment.getView().setVisibility(View.GONE);
        }
        Log.v(TAG, "On create called");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v(MapsActivity.this.getClass().getName(), "On MapReady is called");

        mMap = googleMap;
        // Clear the map of any markers already present and only show logged in user's marker
        mMap.clear();
        loadUserMarkers();
        enableMyLocation();
        // Setting click listener on the marker of the map
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //1: World 5: Landmass/continent 10: City 15: Streets 20: Buildings
        mMap.setMinZoomPreference(2);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                /**
                 * If user is authenticated then add a marker on the map and open
                 * input screen. Then save marker info to firebase database.
                 */
                if (user != null) {
                    // When marker is added then input screen is launched to save additional details.
                    mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    MapsActivity.this.launchInputScreen(latLng);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            if (once == 1) {
                once = 2;
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            currentLatLng = new LatLng(currentLatitude, currentLongitude);
                            // once ensures that only the first time camera zooms in to the current location
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10));
                            mMap.setMyLocationEnabled(true);
                            //Log.v(TAG, "On current latitude is: " + location.getLatitude());
                        }
                    }
                });
            }
            Log.v(MapsActivity.this.getClass().getName(), "On Location is allowed");

        } else {
            mMap.setMyLocationEnabled(false);
        }
    }

    // Attaching Auth State listener
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.v(this.getClass().getName(), "On resume called");
        if (mMap != null) {
            enableMyLocation();
            loadUserMarkers();
        }
        if (authStateListener != null) {
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

    // Input Screen is opened for user to enter details about the school.
    private void launchInputScreen(LatLng latLng) {
        Intent intent = new Intent(this, InputActivity.class);
        String id = user.getUid();
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        intent.putExtra(getString(R.string.key_uid), id);
        intent.putExtra(getString(R.string.key_latitude), latitude);
        intent.putExtra(getString(R.string.key_longitude), longitude);
        startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Read from stored arraylist at the marker position and show info in a new activity
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        findSchoolDetail(latitude, longitude);
        return true;
    }

    private void findSchoolDetail(final double latitude, final double longitude) {
        // Looping through the list of schools to find the one clicked at
        for (int i = 0; i < schools.size(); i++) {
            if (latitude == schools.get(i).getLatitude() && longitude == schools.get(i).getLongitude()) {
                if (user == null) {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra(getString(R.string.key_bundle_school), schools.get(i));
                    startActivity(intent);
                } else {
                    //Open input activity with details pre filled
                    DatabaseReference userReference = databaseReference.child(user.getUid());
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            School school = dataSnapshot.getValue(School.class);
                            Log.v(TAG, "Getting value from database. School name: " + school.getName());
                            Intent intent = new Intent(MapsActivity.this, InputActivity.class);
                            String id = user.getUid();
                            intent.putExtra(getString(R.string.key_uid), id);
                            intent.putExtra(getString(R.string.key_latitude), latitude);
                            intent.putExtra(getString(R.string.key_longitude), longitude);
                            intent.putExtra("school", school);
                            //put extras
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        }
    }

    /**
     * This method loads the markers from the database depending upon whether the user is
     * logged in or not.
     * If user is logged in then it only displays the user's own saved marker.
     * If user is not logged in then it displays all the markers in the database.
     */
    private void loadUserMarkers() {
        // Clear all markers from the map before proceeding to add more from database.
        // If not cleared then old markers persist.
        mMap.clear();
        String userId;

        if (user != null) {
            userId = user.getUid();
            Log.v(MapsActivity.this.getClass().getName(), "user id is: " + userId);
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            /**
             * This method will be invoked any time the data on the database changes.
             * It will also get invoked as soon as we connect the listener, so that we can get an initial
             * snapshot of the data.
             *
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get all the children under the node "locations"
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // Iterate over all the children
                for (DataSnapshot child : children) {
                    // Get the userId stored under locations node
                    String nameofnode = child.getKey();
                    School school = child.getValue(School.class);
                    schools.add(school);

                    LatLng currentLatLng = new LatLng(school.getLatitude(), school.getLongitude());
                    if (user != null) {
                        //Log.v(MapsActivity.this.getClass().getName(), "user is not null");
                        if (nameofnode.equals(user.getUid())) {

                            // Only add the marker that is in the logged in user's database record
                            mMap.addMarker(new MarkerOptions().position(currentLatLng)
                                    .title(school.getName()));
                            schools.add(school);
                        }
                    } else {
                        //If user is not logged in then show all the markers stored in database
                        mMap.addMarker(new MarkerOptions().position(currentLatLng).title(school.getName()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (bundle == null) {
            bundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, bundle);
        }
        mapFragment.onSaveInstanceState(bundle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "On Start called");
        mapFragment.onStart();
    }

    // Removing Auth State listener
    @Override
    protected void onPause() {
        super.onPause();
        mapFragment.onPause();
        Log.v(this.getClass().getName(), "On pause called");
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapFragment.onStop();
        Log.v(this.getClass().getName(), "On stop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment.onDestroy();
        Log.v(this.getClass().getName(), "On destroy called");
    }

    // When hamburger icon is pressed it opens the navigation drawer.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implements the behavior of navigation drawer.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // set item as selected to persist highlight
        menuItem.setChecked(true);

        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_add_marker:
                //start login screen if user is not logged in else dismiss to go back to maps
                //Toast.makeText(this, "add marker is pressed", Toast.LENGTH_SHORT).show();
                //Timber.v("add marker is pressed");
                once = 0;
                if (isOnline(this)) {
                    if (user == null) {
                        startSignInFlow();
                    } else {
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        loadUserMarkers();
                    }
                } else {
                    Toast.makeText(this, "Internet is not connected", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nav_about:
                Intent aboutIntent = new Intent(MapsActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                break;

            case R.id.log_out:
                if (user != null) {
                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MapsActivity.this, "You are now logged out", Toast.LENGTH_SHORT).show();
                                    MapsActivity.this.loadUserMarkers();
                                    // Start current activity again after logging out, because pressing
                                    // the back button just exits the activity and shows the login screen again.
                                    Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                                    MapsActivity.this.startActivity(intent);
                                }
                            });
                } else {
                    Toast.makeText(this, "You are logged out already.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        // close drawer when item is tapped
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSignInFlow() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());

                if (user != null) {
                    // user is signed in
                    // Toast.makeText(MapsActivity.this, "You are signed in.", Toast.LENGTH_SHORT).show();
                    MapsActivity.this.loadUserMarkers();
                } else {
                    // User is not signed in
                    // Create and launch sign-in intent. When it is successful it calls onActivityResult()
                    // with the request code = RC_SIGN_IN.
                    MapsActivity.this.startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .setLogo(R.drawable.logo_with_name)
                                    .setTheme(R.style.LoginTheme)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        //Originally it was uncommented. commented on 8 feb. have to check behaviour
        firebaseAuth.addAuthStateListener(authStateListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            //IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                loadUserMarkers();
                //Toast.makeText(this, "You are now logged in with email: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                // ...
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                // If you don't call finish(), it will result in infinite loop where activity won't exit
                // on pressing the back button.
                // finish() ends the signin flow activity. So when it is cancelled we want to launch Maps Activity again.
                finish();
                Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        }
    }
}
