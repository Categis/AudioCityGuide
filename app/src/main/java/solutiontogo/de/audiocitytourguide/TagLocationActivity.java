package solutiontogo.de.audiocitytourguide;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by maheshkandhari on 1/16/2017.
 */

public class TagLocationActivity extends Activity {

    Button btTagLocation;
    Button btUploadPicture;
    Button btUploadAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_location);
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

                return false;
            }
        });

        btUploadPicture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    public void init(){
        btTagLocation = (Button) findViewById(R.id.btTagLocation);
        btUploadAudio = (Button) findViewById(R.id.btUploadAudio);
        btUploadPicture = (Button) findViewById(R.id.btUploadPicture);
    }



}
