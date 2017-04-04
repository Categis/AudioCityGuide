package solutiontogo.de.audiocitytourguide;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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

import java.util.ArrayList;
import java.util.List;

import solutiontogo.de.audiocitytourguide.utils.AmazonS3Constants;
import solutiontogo.de.audiocitytourguide.utils.AmazonS3Utility;

public class ExploreActivity extends NavigationHeader implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {

    private static String TAG = ExploreActivity.class.getSimpleName();

    public ImageView ivPopupImage;
    public TextView tvPopupText;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter rvAdapter;
    ArrayList<String> locationAudioFiles;
    ArrayList<Integer> locationAudioThumbs;
    SeekBar seekBar;
    public ImageButton btClearSearchLocationText;
    public AutoCompleteTextView autocompleteView;
    private GooglePlacesAdapter mAdapter;
    HandlerThread mHandlerThread;
    Handler mThreadHandler;

    private GoogleMap mMap;
    private Geocoder geocoder;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;

    public ImageView ivLocation;
    public TextView tvLocationDescription;
    public SupportMapFragment mapFragment;
    Bitmap bitmap;
    String description;
    double latitude;
    double longitude;
    Dialog imageDescriptionDialog = null;

    // The S3 client used for getting the list of objects in the bucket
    private AmazonS3Client s3;

    public ExploreActivity() {
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
        getLayoutInflater().inflate(R.layout.content_main, linearLayout);

        ivLocation = (ImageView) findViewById(R.id.ivExploreLocationImage);
        tvLocationDescription = (TextView) findViewById(R.id.tvLocationDescription);

        tvLocationDescription.setText("      The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.");
        tvLocationDescription.setTypeface(getFontType());
        tvLocationDescription.scrollTo(0, 0);
        tvLocationDescription.setMovementMethod(new ScrollingMovementMethod());

        initData();

        tvAppBarTitle.setVisibility(View.GONE);

        // below code is only working when it is pasted here. Please check why? (later)
        btClearSearchLocationText = (ImageButton) findViewById(R.id.btClearSearchLocationText);
        btClearSearchLocationText.setVisibility(View.VISIBLE);
        btClearSearchLocationText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //int btId = v.getId();
                if (null != autocompleteView)
                    autocompleteView.setText("");
                return false;
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        geocoder = new Geocoder(this);
        mAdapter = new GooglePlacesAdapter(getApplicationContext(), R.layout.autocomplete_list_item, null, null);

        autocompleteView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        autocompleteView.setVisibility(View.VISIBLE);
        autocompleteView.setText("");
        autocompleteView.setThreshold(1);
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
                new GetFileListTask().execute();

            }
        });


        this.autocompleteView.addTextChangedListener(new TextWatcher() {
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

        // Calling the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // The number of Columns
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        rvAdapter = new HLVAdapter(ExploreActivity.this, locationAudioFiles, locationAudioThumbs, seekBar);

        mRecyclerView.setAdapter(rvAdapter);

        ivLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                imageDescriptionDialog = new Dialog(ExploreActivity.this, R.style.PopupTheme);
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

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        btClearSearchLocationText.setVisibility(View.VISIBLE);
        new GetFileListTask().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
/*        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();*/

    }

    @Override
    protected void onPause() {
        super.onPause();
/*
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
*/


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
        // Get rid of our Place API Handlers
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
        }
    }

    public Typeface getFontType() {
        Typeface droidSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        return droidSans;
    }

    public void closeDialog(View view) {
        if (view.getId() == R.id.ib_close) {
            imageDescriptionDialog.hide();
        }
    }

    @Override
    public void onBackPressed() {
        if (null != drawerLayout && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();
            this.moveTaskToBack(true);
        }
    }

    private void initData() {
        // Gets the default S3 client.
        s3 = AmazonS3Utility.getS3Client(ExploreActivity.this);
        locationAudioFiles = new ArrayList<String>();
        locationAudioThumbs = new ArrayList<Integer>();
    }

    /**
     * This async task queries S3 for all files in the given bucket so that they
     * can be displayed on the screen
     */
    private class GetFileListTask extends AsyncTask<Void, Void, Void> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;
        // A dialog to let the user know we are retrieving the files
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mRecyclerView.getContext(),
                    getString(R.string.refreshing),
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            // Queries files in the bucket from S3.
            String filename = null;
            s3ObjList = s3.listObjects(AmazonS3Constants.BUCKET_NAME, latlngStr).getObjectSummaries();
            locationAudioFiles.clear();
            for (S3ObjectSummary summary : s3ObjList) {
                if (summary.getKey().endsWith(".mp3")) {
                    filename = summary.getKey();
                    if (filename.contains("/")) {
                        filename = filename.substring(filename.lastIndexOf("/") + 1);
                    }
                    locationAudioFiles.add(filename);
                    locationAudioThumbs.add(R.drawable.image1);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            rvAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    public void placePhotosAsync(String placeId) {
        if (isExploreActivity) {


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

    public ResultCallback<? super PlaceBuffer> mUpdatePlaceDetailsCallback
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
            latlngStr = latLng.toString();
            changeMapLocation(latLng);
            if (isExploreActivity) {
                description = place.getName() + "\n" + place.getAddress();
                tvLocationDescription.setText(description);
            }
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

        latitude = 17.385044;
        longitude = 78.486671;

        @SuppressWarnings("MissingPermission")
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        LatLng defaultLocation = new LatLng(latitude, longitude);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraIdleListener(this);
        changeMapLocation(defaultLocation);
    }


    public void changeMapLocation(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

    }

    @Override
    public void onCameraMove() {
        if (autocompleteView != null) {
            latitude = mMap.getCameraPosition().target.latitude;
            longitude = mMap.getCameraPosition().target.longitude;
        }

    }

    @Override
    public void onCameraIdle() {
        StringBuilder strAddress = new StringBuilder();
        try {
            latitude = mMap.getCameraPosition().target.latitude;
            longitude = mMap.getCameraPosition().target.longitude;
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                for (Address address : addresses) {
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        if (i != address.getMaxAddressLineIndex())
                            strAddress.append(address.getAddressLine(i)).append(", ");
                        else
                            strAddress.append(address.getAddressLine(i));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
