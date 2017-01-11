package solutiontogo.de.audiocitytourguide;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    public static String LOCATION_IMAGE_URL = "location_image_url";
    public static String LOCATION_IMAGE_INFO = "location_image_info";
    public CustomAdapter adapter;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected String latitude,longitude;
    private GoogleApiClient mGoogleApiClient;
    public Button btnLoginInHeader;
    public Button btClearSearchLocationText;
    public TextView tvLocationDescription;
    public ListView lvInExplorerActivity;
    public AutoCompleteTextView autocompleteView;
    public ImageView ivInListItem;
    public ImageView ivLocationImage;
    public Fragment map;
    public ImageView ivLocationAudioListItem;
    public TextView tvLocationAudeioInfo;

    HashMap<String, Object> temp = new HashMap<String, Object>();
    ArrayList<HashMap<String, Object>> originalValues = new ArrayList<HashMap<String, Object>>();
    Integer locationsAudioInteger[] = {R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1};

    private static String TAG = MainActivity.class.getSimpleName();

    private GooglePlacesAdapter mAdapter;

    HandlerThread mHandlerThread;
    Handler mThreadHandler;

    public MainActivity() {
        // Required empty public constructor

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

        ivInListItem = (ImageView) findViewById(R.id.ivLocationAudioListItem);

        temp = new HashMap<String, Object>();
        String locations[] = {"Hyderabad", "Secuderabad", "Bangalore", "Tirupati", "Delhi"};
        for (String location:locations) {
            temp.put(LOCATION_IMAGE_URL, R.drawable.image1);
            temp.put(LOCATION_IMAGE_INFO, location);
            originalValues.add(temp);
        }

        adapter = new CustomAdapter(this, R.layout.simplerow, originalValues);
        lvInExplorerActivity = (ListView) findViewById(R.id.ivLocationAudioList);
        lvInExplorerActivity.setAdapter(adapter);

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
        mAdapter = new GooglePlacesAdapter(getApplicationContext(), R.layout.autocomplete_list_item, null, null);
        autocompleteView.setAdapter(mAdapter);






        autocompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                GooglePlacesAdapter.PlaceAutocomplete placeAutocomplete = (GooglePlacesAdapter.PlaceAutocomplete) parent.getItemAtPosition(position);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeAutocomplete.placeId.toString());

                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);



                //Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
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
                        // Background thread
                        mAdapter.resultList = mAdapter.getPredictions(value);
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
            gotoLocation(latLng);
            /*CharSequence attributions = places.getAttributions();


            if (attributions != null) {

            }*/
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

        }else if (id == R.id.nav_settings) {

        }else if (id == R.id.fb_share) {

        }else if (id == R.id.google_share) {

        }else if (id == R.id.instragam_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        LatLng defaultLocation = new LatLng(12.9720810, 77.6472364);
        gotoLocation(defaultLocation);

    }

    public void gotoLocation(LatLng latLng){
        mMap.addMarker(new MarkerOptions().position(latLng).title("Categis"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
    }

    public void setMenuItemCustomTextStyle(){
        tvLocationDescription.setTypeface(getFontType());
    }

    private void init(){

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


    // define your custom adapter
    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {
        LayoutInflater inflater;

        public CustomAdapter(Context context, int textViewResourceId,
                             ArrayList<HashMap<String, Object>> Strings) {
            super(context, textViewResourceId, Strings);
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // class for caching the views in a row
        private class ViewHolder {

            TextView tvLocationAudioInfo;
            ImageView ivInListItem;
            int[] imageIds = new int[]{R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1};


        }

        ViewHolder viewHolder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.simplerow, null);
                viewHolder = new ViewHolder();
                viewHolder.ivInListItem = (ImageView) convertView.findViewById(R.id.ivLocationAudioListItem);

                viewHolder.tvLocationAudioInfo = (TextView) convertView.findViewById(R.id.tvLocationAudeioInfo);
                viewHolder.tvLocationAudioInfo.scrollTo(0, 0);
                viewHolder.tvLocationAudioInfo.setMovementMethod(new ScrollingMovementMethod());

                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.ivInListItem.setImageResource(viewHolder.imageIds[0]);

            viewHolder.tvLocationAudioInfo.setText(originalValues.get(position).get(LOCATION_IMAGE_INFO).toString());
            viewHolder.tvLocationAudioInfo.setTypeface(getFontType());
            viewHolder.tvLocationAudioInfo.scrollTo(0, 0);
            viewHolder.tvLocationAudioInfo.setMovementMethod(new ScrollingMovementMethod());

            // return the view to be displayed
            return convertView;
        }
    }

    public Typeface getFontType(){
        Typeface droidSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        return droidSans;
    }



}
