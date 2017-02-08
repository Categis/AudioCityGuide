package solutiontogo.de.audiocitytourguide;

/**
 * Created by maheshkandhari on 1/17/2017.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import solutiontogo.de.audiocitytourguide.utils.PropertyReader;

public class NavigationHeader extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static String TAG = NavigationHeader.class.getSimpleName();

    public static Boolean isLaunched = Boolean.TRUE;
    public PropertyReader propertyReader;
    public Button btnLoginInHeader;
    public NavigationView navigationView;
    public LinearLayout linearLayout;
    public LinearLayout mapLayout;
    public DrawerLayout drawerLayout;
    public ImageButton btClearSearchLocationText;
    public AutoCompleteTextView autocompleteView;

    private GooglePlacesAdapter mAdapter;
    HandlerThread mHandlerThread;
    Handler mThreadHandler;

    private GoogleMap mMap;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;

    public ImageView ivLocation;
    public TextView textView;
    public static String description = null;
    public static Bitmap bitmap;
    public SupportMapFragment mapFragment;

    public NavigationHeader() {
        if (mThreadHandler == null) {
            // Initialize and start the HandlerThread
            // which is basically a Thread with a Looper
            // attached (hence a MessageQueue)
            mHandlerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();

            // Initialize the Handler
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(final Message msg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (msg.what == 1) {
                                ArrayList<GooglePlacesAdapter.PlaceAutocomplete> results = mAdapter.resultList;
                                if (results != null && results.size() > 0) {
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    mAdapter.notifyDataSetInvalidated();
                                }
                            }
                        }
                    });
                }
            };
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        propertyReader = new PropertyReader(getBaseContext());

        linearLayout = (LinearLayout) findViewById(R.id.layout_content);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        btnLoginInHeader = (Button) header.findViewById(R.id.btnLoginInHeader);
        btnLoginInHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
                return false;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        // below code is only working when it is pasted here. Please check why? (later)
        btClearSearchLocationText = (ImageButton) findViewById(R.id.btClearSearchLocationText);
        btClearSearchLocationText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //int btId = v.getId();
                if (null != autocompleteView)
                    autocompleteView.setText("");
                return false;
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

        autocompleteView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        autocompleteView.setThreshold(1);
        mAdapter = new GooglePlacesAdapter(getApplicationContext(), R.layout.autocomplete_list_item, null, null);
        autocompleteView.setAdapter(mAdapter);

        autocompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                GooglePlacesAdapter.PlaceAutocomplete placeAutocomplete = (GooglePlacesAdapter.PlaceAutocomplete) parent.getItemAtPosition(position);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeAutocomplete.placeId.toString());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                placePhotosAsync(placeAutocomplete.placeId.toString());

            }
        });

        autocompleteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String value = s.toString();
                // Remove all callbacks and messages
                mThreadHandler.removeCallbacksAndMessages(null);
                // Now add a new one
                mThreadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (null != value && !"".equals(value)) {
                            // Background thread
                            mAdapter.resultList = mAdapter.getPredictions(value);
                        }
                        // Post to Main Thread
                        mThreadHandler.sendEmptyMessage(1);
                    }
                }, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "doAfterTextChanged");
            }
        });

        if (isLaunched) {
            isLaunched = Boolean.FALSE;
            openActivity(R.id.nav_explore);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Get rid of our Place API Handlers
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        openActivity(item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openActivity(int id) {
        Intent intent;
        switch (id) {
            case R.id.nav_editor_choice:
                // Handle the camera action
                intent = new Intent(this, CameraDemoActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_explore:
                intent = new Intent(this, ExploreActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.nav_tag:
                intent = new Intent(this, TagLocationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.nav_bookmarks:
                break;

            case R.id.nav_share:
                break;

            case R.id.nav_history:
                break;

            case R.id.nav_settings:
                break;

            case R.id.fb_share:
                break;

            case R.id.google_share:
                break;

            case R.id.instragam_share:
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (null != drawerLayout && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    private void placePhotosAsync(String placeId) {
        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {

                    @Override
                    public void onResult(PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess()) {
                            return;
                        }

                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        if (photoMetadataBuffer.getCount() > 0) {
                            // Display the first bitmap in an ImageView in the size of the view
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(mGoogleApiClient, ivLocation.getWidth(), ivLocation.getHeight())
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        } else {
                            ivLocation.setImageResource(R.drawable.image1);
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                return;
            }
            bitmap = placePhotoResult.getBitmap();
            ivLocation.setImageBitmap(bitmap);
        }
    };


    @Override
    public void onConnected(Bundle bundle) {
        mAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mAdapter.setGoogleApiClient(null);
        Log.e(TAG, "Google Places API connection suspended.");
    }

    private ResultCallback<? super PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            LatLng latLng = place.getLatLng();
            changeMapLocation(latLng);
            description = place.getName() + "\n" + place.getAddress();
            textView.setText(description);
            places.release();
        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        double latitude = 12.9720810;
        double longitude = 77.6472364;

        @SuppressWarnings("MissingPermission")
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        LatLng defaultLocation = new LatLng(latitude, longitude);
        changeMapLocation(defaultLocation);
    }

    public void changeMapLocation(LatLng latLng) {
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
    }
}