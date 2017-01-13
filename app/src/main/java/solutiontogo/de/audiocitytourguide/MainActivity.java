package solutiontogo.de.audiocitytourguide;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
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
import java.util.Arrays;

import solutiontogo.de.audiocitytourguide.utils.PropertyReader;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static String TAG = MainActivity.class.getSimpleName();

    public static String LOCATION_AUDIO_URL = "location_audio_url";
    public static String LOCATION_IMAGE_URL = "location_image_url";
    public Bitmap bitmap;
    public static PropertyReader propertyReader;

    private GoogleMap mMap;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private GooglePlacesAdapter mAdapter;

    public Button btnLoginInHeader;
    public Button btClearSearchLocationText;
    public TextView tvLocationDescription;
    public AutoCompleteTextView autocompleteView;
    public ImageView ivLocationImage;
    public ImageView ivPopupImage;
    public TextView tvPopupText;

    static String description = null;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter rvAdapter;
    ArrayList<String> locationAudioFiles;
    ArrayList<Integer> locationAudioThumbs;

    Dialog imageDescriptionDialog = null;

    HandlerThread mHandlerThread;
    Handler mThreadHandler;

    public MainActivity() {
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
                                }
                                else {
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
    public void onDestroy() {
        super.onDestroy();

        // Get rid of our Place API Handlers
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // below code is only working when it is pasted here. Please check why? (later)
        btClearSearchLocationText = (Button) findViewById(R.id.btClearSearchLocationText);
        btClearSearchLocationText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //int btId = v.getId();
                if(null!= autocompleteView)
                    autocompleteView.setText("");
                return false;
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        init();

        tvLocationDescription.setText("      The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.");
        tvLocationDescription.setTypeface(getFontType());
        tvLocationDescription.scrollTo(0, 0);
        tvLocationDescription.setMovementMethod(new ScrollingMovementMethod());

        locationAudioFiles = new ArrayList<>(Arrays.asList("Hyderabad", "Secuderabad", "Bangalore", "Tirupati", "Delhi"));
        locationAudioThumbs = new ArrayList<>(Arrays.asList(R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1));

        // Calling the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // The number of Columns
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        rvAdapter = new HLVAdapter(MainActivity.this, locationAudioFiles, locationAudioThumbs);
        mRecyclerView.setAdapter(rvAdapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        btnLoginInHeader  = (Button) header.findViewById(R.id.btnLoginInHeader);
        btnLoginInHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent login = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(login);
                return false;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                        if(null != value && !"".equals(value)) {
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

        ivLocationImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                imageDescriptionDialog = new Dialog(MainActivity.this, R.style.PopupTheme);
                imageDescriptionDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                imageDescriptionDialog.setContentView(getLayoutInflater().inflate(R.layout.full_location_details, null));

                ivPopupImage = (ImageView) imageDescriptionDialog.findViewById(R.id.ivPopupImage);
                ivPopupImage.setImageBitmap(bitmap);

                tvPopupText = (TextView) imageDescriptionDialog.findViewById(R.id.tvPopupText);
                tvPopupText.setText(description);

                imageDescriptionDialog.show();

                return false;
            }
        });
    }

    private ResultCallback<? super PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("MainActivity", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            LatLng latLng = place.getLatLng();
            changeMapLocation(latLng);
            description = place.getName() + "\n" + place.getAddress();
            tvLocationDescription.setText(description);
            places.release();
        }
    };

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
                                    .getScaledPhoto(mGoogleApiClient, ivLocationImage.getWidth(), ivLocationImage.getHeight())
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        } else {
                            ivLocationImage.setImageResource(R.drawable.image1);
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
            ivLocationImage.setImageBitmap(placePhotoResult.getBitmap());
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

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
        int id = item.getItemId();
        System.out.println(id);

        if (id == R.id.nav_editor_choice) {

        } else if (id == R.id.nav_explore) {
            // Handle the camera action
            Intent intent = new Intent("solutiontogo.de.audiocitytourguide.CameraDemoActivity");
            startActivity(intent);

        } else if (id == R.id.nav_tag) {

        } else if (id == R.id.nav_bookmarks) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.fb_share) {

        } else if (id == R.id.google_share) {

        } else if (id == R.id.instragam_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
        LatLng defaultLocation = new LatLng(12.9720810, 77.6472364);
        changeMapLocation(defaultLocation);
    }

    public void changeMapLocation(LatLng latLng){
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
    }

    public void setMenuItemCustomTextStyle(){
        tvLocationDescription.setTypeface(getFontType());
    }

    private void init(){

        propertyReader = new PropertyReader(getBaseContext());
        //map = (Fragment) findViewById(R.id.map);
        ivLocationImage = (ImageView) findViewById(R.id.ivLocationImage);
        tvLocationDescription =  (TextView) findViewById(R.id.tvLocationDescription);
        //ivLocationAudioListItem = (ImageView) findViewById(R.id.ivLocationAudioListItem);
        //tvLocationAudeioInfo = (TextView) findViewById(R.id.tvLocationAudeioInfo);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i("MainActivity", "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("MainActivity", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mAdapter.setGoogleApiClient(null);
        Log.e("MainActivity", "Google Places API connection suspended.");
    }

    public Typeface getFontType(){
        Typeface droidSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        return droidSans;
    }

    public void closeDialog(View view) {
        if (view.getId() == R.id.ib_close) {
            imageDescriptionDialog.hide();
        }
    }

}
