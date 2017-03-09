package solutiontogo.de.audiocitytourguide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import solutiontogo.de.audiocitytourguide.recording.AudioRecordingHandler;
import solutiontogo.de.audiocitytourguide.recording.AudioRecordingThread;
import solutiontogo.de.audiocitytourguide.utils.AmazonS3Constants;
import solutiontogo.de.audiocitytourguide.utils.AmazonS3Utility;
import solutiontogo.de.audiocitytourguide.utils.Utility;


import static solutiontogo.de.audiocitytourguide.R.id.etLocationDescription;

/**
 * Created by shivaramak on 21/02/2017.
 */

public class UploadLocationDetails extends NavigationHeader implements View.OnTouchListener {

    /////////////////////////////////////////////
// Indicates that no upload is currently selected
    private static final int INDEX_NOT_CHECKED = -1;

    private Handler handler = new Handler();
    // TAG for logging;
    private static final String TAG = UploadLocationDetails.class.getSimpleName();

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    // The SimpleAdapter adapts the data about transfers to rows in the UI
    private SimpleAdapter simpleAdapter;

    private SeekBar seekBarInUploadPage;

    // A List of all transfers
    private List<TransferObserver> observers;
    /**
     * This map is used to provide data to the SimpleAdapter above. See the
     * fillMap() function for how it relates observers to rows in the displayed
     * activity.
     */
    private ArrayList<HashMap<String, Object>> transferRecordMaps;

    // Which row in the UI is currently checked (if any)
    private int checkedIndex;

    private String imagePath = null;
    private String audioFilePath = null;
    private String selectedPlaceId = null;
    public HashMap<String, String> filesToUpload;
    private MediaPlayer myPlayer;
    private AudioRecordingThread recordingThread;
    VisualizerView visualizerView;
    private static final int FILE_REQUEST_CODE = 2;
    ////////////////////////////////////////////
    private ImageView locationPic1, locationPic2, locationPic3;
    private EditText locationTitle, edLocationDescription;
    private TextView fileName, addLocationPictureTitle;
    private ImageButton startRecord, stopRecord, deleteRecordedAudio, playPauseBtn;
    private Button btnUploadLocationDetails;
    private TextView tvAddLocation;
    private static int clickedId;
    private String userChosenTask;
    private File locPicDest1, locPicDest2, locPicDest3 = null;
    private View view;
    private final int REQUEST_CAMERA = 0;
    private final int SELECT_FILE = 1;
    private final int PLACE_PICKER_REQUEST = 3;
    private PlacePicker.IntentBuilder placepickerIntentBuilder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_location_details);
        init();

        tvAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlacePickerIntent();
            }
        });


        // recorded file storage path
        audioFilePath = "/audioRecording" + "_" + System.currentTimeMillis() + ".mp3";

        String sdCardState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdCardState) &&
                !Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdCardState)) {
            audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + audioFilePath;
        } else {
            audioFilePath = getFilesDir().getAbsolutePath() + audioFilePath;
        }
        filesToUpload.put("audioFilePath", audioFilePath);

        // Initializes TransferUtility, always do this before using it.
        transferUtility = AmazonS3Utility.getTransferUtility(this);


        locationPic1.setOnTouchListener(this);
        locationPic2.setOnTouchListener(this);
        locationPic3.setOnTouchListener(this);

        startRecord = (ImageButton) findViewById(R.id.start_recording);


        startRecord.setOnTouchListener(this);
        stopRecord.setOnTouchListener(this);
        playPauseBtn.setOnTouchListener(this);
        deleteRecordedAudio.setOnTouchListener(this);

        btnUploadLocationDetails.setOnTouchListener(this);


    }

    public void start(View view) {
        startRecord.setEnabled(false);
        stopRecord.setEnabled(true);
        playPauseBtn.setEnabled(false);
        //stopPlayBtn.setEnabled(false);
        deleteRecordedAudio.setEnabled(false);

        recordingThread = new AudioRecordingThread(audioFilePath, new AudioRecordingHandler() {
            @Override
            public void updateVisualizerView(final int amplitude) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        visualizerView.updateVisualizer(amplitude);
                    }
                });
            }
        });
        recordingThread.start();

        Toast.makeText(getApplicationContext(), "Start recording...", Toast.LENGTH_SHORT).show();
    }

    public void stop(View view) {
        startRecord.setEnabled(true);
        stopRecord.setEnabled(false);
        playPauseBtn.setEnabled(true);
        deleteRecordedAudio.setEnabled(true);

        if (recordingThread != null) {
            recordingThread.stopRecording();
            recordingThread = null;
        }

        Toast.makeText(getApplicationContext(), "Stop recording...", Toast.LENGTH_SHORT).show();
        Uri uri = Uri.fromFile(new File(filesToUpload.get("audioFilePath")));
        fileName.setText(uri.getLastPathSegment());
        deleteRecordedAudio.setVisibility(View.VISIBLE);
    }

    /*
   * Gets the file path of the given Uri.
   */
    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "Error during upload: " + id, e);
            updateList();
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            updateList();
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(TAG, "onStateChanged: " + id + ", " + newState);
            updateList();
        }
    }

    // Below code is used for uploading

    /*
     * Updates the ListView according to the observers.
     */
    private void updateList() {
        TransferObserver observer = null;
        HashMap<String, Object> map = null;
        for (int i = 0; i < observers.size(); i++) {
            observer = observers.get(i);
            map = transferRecordMaps.get(i);
            AmazonS3Utility.fillMap(map, observer, i == checkedIndex);
        }
        simpleAdapter.notifyDataSetChanged();

    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(filePath);
        String key = selectedPlaceId + "/" + file.getName();
        TransferObserver observer = transferUtility.upload(AmazonS3Constants.BUCKET_NAME, key, file);
//        observer.setTransferListener(new UploadListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        clickedId = v.getId();
        switch (clickedId) {
            case R.id.iv_location_pic1:
                selectImage(clickedId);
                break;
            case R.id.iv_location_pic2:
                selectImage(clickedId);

                break;
            case R.id.iv_location_pic3:
                selectImage(clickedId);

                break;
            case R.id.start_recording:
                start(v);
                break;
            case R.id.stop_recording:
                stop(v);
                break;
            case R.id.playPauseRecording:
                play(v);
                break;
            case R.id.delete_recording:
                delete(v);
                break;
            case R.id.btUploadLocationDetails:
                if (selectedPlaceId != null && filesToUpload != null && !filesToUpload.isEmpty()) {
                    uploadLocationDetailsToCloud();
                }
                break;
            default://do nothing
                break;
        }

        return false;
    }

    private void uploadLocationDetailsToCloud() {
        Uri uri;
        Iterator<String> iterator = filesToUpload.values().iterator();
        try {
            while (iterator.hasNext()) {
                uri = Uri.fromFile(new File(iterator.next()));
                String path = getPath(uri);
                beginUpload(path);
            }
            Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void getPlacePickerIntent() {
        if (placepickerIntentBuilder == null) {
            placepickerIntentBuilder = new PlacePicker.IntentBuilder();
        }
        try {
            startActivityForResult(placepickerIntentBuilder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        return;
    }

    public void delete(View view) {
        boolean deleted = false;


        stopPlayingMediaPlayer();
        File file = new File(audioFilePath);
        if (file.exists()) {
            deleted = file.delete();
        }


        if (deleted) {
            fileName.setText("");
            deleteRecordedAudio.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
        }






}

    private void stopPlayingMediaPlayer() {
        if (myPlayer != null) {
            if (myPlayer.isPlaying()) {
                myPlayer.stop();
                myPlayer.release();
            }

        }
    }


    public void startPlayProgressUpdater() {


        if (myPlayer != null)
            seekBarInUploadPage.setProgress(myPlayer.getCurrentPosition()); //IllegalStateException coming here


        if (myPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater(); //IllegalStateException coming here
                }
            };
            handler.postDelayed(notification, 1000);
        } else {
            myPlayer.pause();
            //buttonPlayStop.setText(getString(R.string.play_str));
            //seekBar.setProgress(0);
        }
    }

    // This is event handler thumb moving event
    private void seekChange(View v) {
        if (myPlayer.isPlaying()) {
            SeekBar sb = (SeekBar) v;
            myPlayer.seekTo(sb.getProgress());
        }
    }

    public void play(View view) {
        //stopPlayingMediaPlayer();

        playMedia(myPlayer);

        Toast.makeText(getApplicationContext(), "Start play the recording...",
                Toast.LENGTH_SHORT).show();

    }

    private void playMedia(MediaPlayer mediaPlayer) {


        if (myPlayer == null) {
            myPlayer = new MediaPlayer();
            try {
                myPlayer.setDataSource(filesToUpload.get("audioFilePath"));

                //seekBarInUploadPage.getProgress();
                myPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            myPlayer.start();
            seekBarInUploadPage.setProgress(0);
            startPlayProgressUpdater();
            seekBarInUploadPage.setMax(myPlayer.getDuration());
            seekBarInUploadPage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    seekChange(v);
                    return false;
                }
            });
        } else {

            myPlayer.start();
            seekBarInUploadPage.setProgress(0);
            startPlayProgressUpdater();
            seekBarInUploadPage.setMax(myPlayer.getDuration());

            seekBarInUploadPage.setMax(myPlayer.getDuration());
            seekBarInUploadPage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    seekChange(v);
                    return false;
                }
            });


        }
        /*if (myPlayer != null) {
            myPlayer.reset();
            myPlayer.start();
            seekBarInUploadPage.setProgress(0);
            startPlayProgressUpdater();
            playPauseBtn.setBackgroundResource(R.drawable.quantum_ic_pause_grey600_48);
            seekBarInUploadPage.setMax(myPlayer.getDuration());
            seekBarInUploadPage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    seekChange(v);
                    return false;
                }
            });
        } else {
            try {
                if(null==myPlayer)
                myPlayer = new MediaPlayer();
                myPlayer.setDataSource(filesToUpload.get("audioFilePath"));
                //seekBarInUploadPage.getProgress();
                myPlayer.prepare();
                myPlayer.start();
                seekBarInUploadPage.setProgress(0);
                startPlayProgressUpdater();
                seekBarInUploadPage.setMax(myPlayer.getDuration());
                seekBarInUploadPage.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        seekChange(v);
                        return false;
                    }
                });

                //ibPlayPauseRecording
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    public void init() {


        locationPic1 = (ImageView) findViewById(R.id.iv_location_pic1);
        locationPic2 = (ImageView) findViewById(R.id.iv_location_pic2);
        locationPic3 = (ImageView) findViewById(R.id.iv_location_pic3);
        tvAddLocation = (TextView) findViewById(R.id.tvAddLocation);
        locationTitle = (EditText) findViewById(R.id.etLocationTitle);
        edLocationDescription = (EditText) findViewById(etLocationDescription);
        seekBarInUploadPage = (SeekBar) findViewById(R.id.seekBarInUploadPage);

        fileName = (TextView) findViewById(R.id.tv_fileName);
        addLocationPictureTitle = (TextView) findViewById(R.id.tvAddLocationPicture);

        startRecord = (ImageButton) findViewById(R.id.start_recording);
        stopRecord = (ImageButton) findViewById(R.id.stop_recording);
        playPauseBtn = (ImageButton) findViewById(R.id.playPauseRecording);
        deleteRecordedAudio = (ImageButton) findViewById(R.id.delete_recording);

        btnUploadLocationDetails = (Button) findViewById(R.id.btUploadLocationDetails);
        visualizerView = (VisualizerView) findViewById(R.id.visualizer);

        filesToUpload = new HashMap<String, String>();
    }


    private void setImageViewData(Bitmap bitmap) {
        File imageFile = null;
        switch (clickedId) {
            case R.id.iv_location_pic1:
                locationPic1.setImageBitmap(bitmap);
                imageFile = getFileFromBitmap(bitmap, "Pic1");
                filesToUpload.put("locationPic1", imageFile.getAbsolutePath());
                break;
            case R.id.iv_location_pic2:
                locationPic2.setImageBitmap(bitmap);
                imageFile = getFileFromBitmap(bitmap, "Pic2");
                filesToUpload.put("locationPic2", imageFile.getAbsolutePath());
                break;
            case R.id.iv_location_pic3:
                locationPic3.setImageBitmap(bitmap);
                imageFile = getFileFromBitmap(bitmap, "Pic3");
                filesToUpload.put("locationPic3", imageFile.getAbsolutePath());
                break;
            default: // do nothing
                break;
        }
    }

    private File createEmptyFile() throws IOException {
        // Create an image file name
        String imageFileName = System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        imagePath = image.getAbsolutePath();
        return image;
    }

    public File getFileFromBitmap(Bitmap bitmap, String imageSuffix) {
        //create a file to write bitmap data
        File imageFile;
        ByteArrayOutputStream bos;
        //write the bytes in file
        FileOutputStream fos;

        imageFile = new File(getApplicationContext().getCacheDir(), System.currentTimeMillis() + "_" + imageSuffix + ".jpg");
        bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        return imageFile;
    }


    ///////////// Dialog Window /////////////////////////

    public void selectImage(final int eventId) {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadLocationDetails.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(UploadLocationDetails.this);

                if (items[item].equals("Take Photo")) {
                    userChosenTask = "Take Photo";
                    if (result)
                        cameraIntent(eventId);
                } else if (items[item].equals("Choose from Library")) {
                    userChosenTask = "Choose from Library";
                    if (result)
                        galleryIntent(eventId);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent(int eventId) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = createEmptyFile();
            Uri imageUri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void galleryIntent(int eventId) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap = null;
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_CAMERA) {
                    imageBitmap = BitmapFactory.decodeFile(imagePath);
                    setImageViewData(imageBitmap);
                } else if (requestCode == SELECT_FILE) {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    setImageViewData(imageBitmap);
                } else if (requestCode == PLACE_PICKER_REQUEST) {
                    Place place = PlacePicker.getPlace(data, this);
                    tvAddLocation.setText(place.getAddress());
                    selectedPlaceId = place.getId();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChosenTask.equals("Take Photo"))
                        cameraIntent(0);
                    else if (userChosenTask.equals("Choose from Library"))
                        galleryIntent(0);
                } else {
                    //code for deny
                }
                break;
        }
    }

}
