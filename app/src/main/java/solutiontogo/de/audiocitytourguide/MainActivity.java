package solutiontogo.de.audiocitytourguide;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import solutiontogo.de.audiocitytourguide.utils.PropertyReader;

public class MainActivity extends NavigationHeader {

    private static String TAG = MainActivity.class.getSimpleName();

    public static String LOCATION_AUDIO_URL = "location_audio_url";
    public static String LOCATION_IMAGE_URL = "location_image_url";

    public static PropertyReader propertyReader;
    public TextView tvLocationDescription;
    public ImageView ivLocationImage;
    public ImageView ivPopupImage;
    public TextView tvPopupText;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter rvAdapter;
    ArrayList<String> locationAudioFiles;
    ArrayList<Integer> locationAudioThumbs;

    Dialog imageDescriptionDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.LLContentMain);
        getLayoutInflater().inflate(R.layout.content_main, linearLayout, false);

        propertyReader = new PropertyReader(getBaseContext());
        ivLocationImage = (ImageView) findViewById(R.id.ivLocationImage);
        tvLocationDescription =  (TextView) findViewById(R.id.tvLocationDescription);

        GoogleMapFragment googleMapFragment = new GoogleMapFragment();
        new GoogleMapFragment(ivLocationImage, tvLocationDescription);

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

        ivLocationImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                imageDescriptionDialog = new Dialog(MainActivity.this, R.style.PopupTheme);
                imageDescriptionDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                imageDescriptionDialog.setContentView(getLayoutInflater().inflate(R.layout.full_location_details, null));

                ivPopupImage = (ImageView) imageDescriptionDialog.findViewById(R.id.ivPopupImage);
                ivPopupImage.setImageBitmap(GoogleMapFragment.bitmap);

                tvPopupText = (TextView) imageDescriptionDialog.findViewById(R.id.tvPopupText);
                tvPopupText.setText(GoogleMapFragment.description);

                imageDescriptionDialog.show();

                return false;
            }
        });
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
