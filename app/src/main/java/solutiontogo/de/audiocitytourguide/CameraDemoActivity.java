package solutiontogo.de.audiocitytourguide;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shivaramak on 30/12/2016.
 */

public class CameraDemoActivity extends Activity {
    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_picture);

        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        File newdir = new File(dir);
        newdir.mkdirs();

        Button capture = (Button) findViewById(R.id.btnCapture);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Here, the counter will be incremented each time, and the
                // picture taken by camera will be stored as 1.jpg,2.jpg
                // and likewise.
                count++;
                String file = dir+count+".jpg";
                File newfile = new File(file);
                try {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {
                }

                Uri outputFileUri = Uri.fromFile(newfile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Camera Pic Saved", Toast.LENGTH_SHORT).show();
            Log.d("CameraDemo", "Pic saved");
        }
    }
}