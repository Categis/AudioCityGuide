package solutiontogo.de.audiocitytourguide;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by maheshkandhari on 1/16/2017.
 */

public class TagLocationActivity extends NavigationHeader {

    Button btTagLocation;
    Button btUploadPicture;
    Button btUploadAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.tag_location, linearLayout);

        init();
        btTagLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        btUploadAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(getApplicationContext(), RecordAndUploadAudio.class);
                startActivity(intent);
                return false;
            }
        });
        btUploadPicture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(getApplicationContext(), CameraDemoActivity.class);
                startActivity(intent);
                return false;
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void init(){
        btTagLocation = (Button) findViewById(R.id.btTagLocation);
        btUploadAudio = (Button) findViewById(R.id.btUploadAudio);
        btUploadPicture = (Button) findViewById(R.id.btUploadPicture);
        imageView = (ImageView) findViewById(R.id.ivTLImage);
        textView =  (TextView) findViewById(R.id.tvTLDescription);
    }



}
