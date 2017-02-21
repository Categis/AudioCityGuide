package solutiontogo.de.audiocitytourguide;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

import solutiontogo.de.audiocitytourguide.utils.AmazonS3Constants;
import solutiontogo.de.audiocitytourguide.utils.AmazonS3Utility;

public class ExploreActivity extends NavigationHeader {

    private static String TAG = ExploreActivity.class.getSimpleName();

    public static String LOCATION_AUDIO_URL = "location_audio_url";
    public static String LOCATION_IMAGE_URL = "location_image_url";

    public ImageView ivPopupImage;
    public TextView tvPopupText;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter rvAdapter;
    ArrayList<String> locationAudioFiles;
    ArrayList<Integer> locationAudioThumbs;
    SeekBar seekBar;

    Dialog imageDescriptionDialog = null;

    // The S3 client used for getting the list of objects in the bucket
    private AmazonS3Client s3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.content_main, linearLayout);

        ivLocation = (ImageView) findViewById(R.id.ivLocationImage);
        textView =  (TextView) findViewById(R.id.tvLocationDescription);

        textView.setText("      The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.");
        textView.setTypeface(getFontType());
        textView.scrollTo(0, 0);
        textView.setMovementMethod(new ScrollingMovementMethod());

        initData();

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
        new GetFileListTask().execute();
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
            s3ObjList = s3.listObjects(AmazonS3Constants.BUCKET_NAME).getObjectSummaries();
            locationAudioFiles.clear();
            for (S3ObjectSummary summary : s3ObjList) {
                if(summary.getKey().endsWith(".mp3")) {
                    locationAudioFiles.add(summary.getKey());
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

}
