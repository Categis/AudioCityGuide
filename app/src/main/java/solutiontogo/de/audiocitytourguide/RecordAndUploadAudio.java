package solutiontogo.de.audiocitytourguide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import solutiontogo.de.audiocitytourguide.recording.AudioRecordingHandler;
import solutiontogo.de.audiocitytourguide.recording.AudioRecordingThread;
import solutiontogo.de.audiocitytourguide.utils.AmazonS3Constants;
import solutiontogo.de.audiocitytourguide.utils.AmazonS3Utility;

/**
 * Created by shivaramak on 17/01/2017.
 */

public class RecordAndUploadAudio extends ListActivity {

    // Indicates that no upload is currently selected
    private static final int INDEX_NOT_CHECKED = -1;

    // TAG for logging;
    private static final String TAG = RecordAndUploadAudio.class.getSimpleName();

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    // The SimpleAdapter adapts the data about transfers to rows in the UI
    private SimpleAdapter simpleAdapter;

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

    private Button startBtn;
    private Button stopBtn;
    private Button playBtn;
    private Button deleteRecordedAudio;
    private Button stopPlayBtn;
    private Button btUploadAudio;

    String outputFile;
    private MediaPlayer myPlayer;
    private AudioRecordingThread recordingThread;
    VisualizerView visualizerView;
    private static final int FILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_and_upload_audio);

        visualizerView = (VisualizerView) findViewById(R.id.visualizer);

        // recorded file storage path
        outputFile = "/audioRecording" + "_" + System.currentTimeMillis() + ".mp3";

        String sdCardState = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(sdCardState) &&
                !Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdCardState)) {
            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + outputFile;
        } else {
            outputFile = getFilesDir() + outputFile;
        }

        startBtn = (Button) findViewById(R.id.start_recording);
        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                start(v);
            }
        });

        stopBtn = (Button) findViewById(R.id.stop_recording);
        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                stop(v);
            }
        });

        playBtn = (Button) findViewById(R.id.play_recording);
        playBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                play(v);
            }
        });

        stopPlayBtn = (Button) findViewById(R.id.stop_playing);
        stopPlayBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                stopPlay(v);
            }
        });

        deleteRecordedAudio = (Button) findViewById(R.id.delete_recording);

        deleteRecordedAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File file = new File(outputFile);
                    boolean deleted = file.delete();
                    Toast.makeText(getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "File Not Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Initializes TransferUtility, always do this before using it.
        transferUtility = AmazonS3Utility.getTransferUtility(this);
        checkedIndex = INDEX_NOT_CHECKED;
        transferRecordMaps = new ArrayList<HashMap<String, Object>>();
        initUI();

        btUploadAudio = (Button) findViewById(R.id.btUploadAudio);
        btUploadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= 19) {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                } else {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                }
                intent.setType("audio/*");
                startActivityForResult(intent, FILE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get the data from any transfer's that have already happened,
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear transfer listeners to prevent memory leak, or
        // else this activity won't be garbage collected.
        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }
    }

    /**
     * Gets all relevant transfers from the Transfer Service for populating the
     * UI
     */
    private void initData() {
        transferRecordMaps.clear();
        // Use TransferUtility to get all upload transfers.
        observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        if(observers.size() > 3){
            observers = observers.subList(observers.size() - 3, observers.size());
        }
        TransferListener listener = new UploadListener();
        for (TransferObserver observer : observers) {

            // For each transfer we will will create an entry in
            // transferRecordMaps which will display
            // as a single row in the UI
            HashMap<String, Object> map = new HashMap<String, Object>();
            AmazonS3Utility.fillMap(map, observer, false);
            transferRecordMaps.add(map);

            // Sets listeners to in progress transfers
            if (TransferState.WAITING.equals(observer.getState())
                    || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                    || TransferState.IN_PROGRESS.equals(observer.getState())) {
                observer.setTransferListener(listener);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    private void initUI() {
        /**
         * This adapter takes the data in transferRecordMaps and displays it,
         * with the keys of the map being related to the columns in the adapter
         */
        simpleAdapter = new SimpleAdapter(this, transferRecordMaps,
                R.layout.upload_item, new String[]{
                "fileName", "progress", "bytes", "state", "percentage"
        },
                new int[]{
                        R.id.textFileName, R.id.progressBar1, R.id.textBytes,
                        R.id.textState, R.id.textPercentage
                });
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.textFileName:
                        TextView fileName = (TextView) view;
                        fileName.setText((String) data);
                        return true;
                    case R.id.progressBar1:
                        ProgressBar progress = (ProgressBar) view;
                        progress.setProgress((Integer) data);
                        return true;
                    case R.id.textBytes:
                        TextView bytes = (TextView) view;
                        bytes.setText((String) data);
                        return true;
                    case R.id.textState:
                        TextView state = (TextView) view;
                        state.setText(((TransferState) data).toString());
                        return true;
                    case R.id.textPercentage:
                        TextView percentage = (TextView) view;
                        percentage.setText((String) data);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

    }

    public void start(View view) {
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        playBtn.setEnabled(false);
        stopPlayBtn.setEnabled(false);
        deleteRecordedAudio.setEnabled(false);

        recordingThread = new AudioRecordingThread(outputFile, new AudioRecordingHandler() {
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
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        playBtn.setEnabled(true);
        stopPlayBtn.setEnabled(true);
        deleteRecordedAudio.setEnabled(true);

        if (recordingThread != null) {
            recordingThread.stopRecording();
            recordingThread = null;
        }

        Toast.makeText(getApplicationContext(), "Stop recording...", Toast.LENGTH_SHORT).show();

        try {
            Uri uri = Uri.fromFile(new File(outputFile));
            String path = getPath(uri);
            beginUpload(path);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Unable to upload file from the given uri", e);
        }
    }

    public void play(View view) {
        try {
            if (myPlayer == null)
                myPlayer = new MediaPlayer();
            myPlayer.setDataSource(outputFile);
            myPlayer.prepare();
            myPlayer.start();

            playBtn.setEnabled(false);
            stopPlayBtn.setEnabled(true);
            deleteRecordedAudio.setEnabled(true);
            startBtn.setEnabled(true);

            Toast.makeText(getApplicationContext(), "Start play the recording...",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopPlay(View view) {
        try {
            if (myPlayer != null) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
                playBtn.setEnabled(true);
                startBtn.setEnabled(true);
                stopPlayBtn.setEnabled(false);


                Toast.makeText(getApplicationContext(), "Stop playing the recording...",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                try {
                    String path = getPath(uri);
                    beginUpload(path);
                } catch (URISyntaxException e) {
                    Toast.makeText(this,
                            "Unable to get the file from the given URI.  See error log for details",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Unable to upload file from the given uri", e);
                }
            }
        }
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(AmazonS3Constants.BUCKET_NAME, file.getName(),
                file);
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
//        observer.setTransferListener(new UploadListener());
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
}
