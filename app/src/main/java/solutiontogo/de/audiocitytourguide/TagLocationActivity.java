package solutiontogo.de.audiocitytourguide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by maheshkandhari on 1/16/2017.
 */

public class TagLocationActivity extends NavigationHeader {

    private static String TAG = TagLocationActivity.class.getSimpleName();

    private Button btTagLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.tag_location, linearLayout);

        init();

        btTagLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UploadLocationDetails.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void init() {
        btTagLocation = (Button) findViewById(R.id.btTagLocation);
    }

}
