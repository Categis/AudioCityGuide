package solutiontogo.de.audiocitytourguide;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import solutiontogo.de.audiocitytourguide.utils.AmazonS3Constants;
import solutiontogo.de.audiocitytourguide.utils.AmazonS3Utility;

public class HLVAdapter extends RecyclerView.Adapter<HLVAdapter.ViewHolder> {

    private static String TAG = ExploreActivity.class.getSimpleName();

    Context context;
    SeekBar seekBar;
    File audioFile = null;
    MediaPlayer myPlayer = null;
    Integer itemPosition = null;
    ProgressDialog progressDialog;
    TransferUtility transferUtility;
    ArrayList<String> locationAudioFiles;
    ArrayList<Integer> locationAudioThumbs;
    private final Handler handler = new Handler();

    public HLVAdapter(Context context, ArrayList<String> locationAudioFiles, ArrayList<Integer> locationAudioThumbs, SeekBar seekBar) {
        super();
        this.context = context;
        this.locationAudioFiles = locationAudioFiles;
        this.locationAudioThumbs = locationAudioThumbs;
        this.seekBar = seekBar;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        viewHolder.tvSpecies.setText(locationAudioFiles.get(i));
        viewHolder.imgThumbnail.setImageResource(locationAudioThumbs.get(i));

        viewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                itemPosition = position;
                if(myPlayer != null){
                    myPlayer.stop();
                    myPlayer.release();
                    myPlayer = null;
                }
                displayProgressDialog(view);
                if (isLongClick) {
                    beginDownload(locationAudioFiles.get(itemPosition));
//                    Toast.makeText(context, "#" + position + " - " + locationAudioFiles.get(position) + " (Long click)", Toast.LENGTH_SHORT).show();
                } else {
                    beginDownload(locationAudioFiles.get(itemPosition));
//                    Toast.makeText(context, "#" + position + " - " + locationAudioFiles.get(position), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void displayProgressDialog(View view){
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setCancelable(true);
        progressDialog.setMessage("File downloading ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
    }

    @Override
    public int getItemCount() {
        return locationAudioFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView tvSpecies;
        public ImageView imgThumbnail;
        private ItemClickListener clickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.ivLocationAudioListItem);
            tvSpecies = (TextView) itemView.findViewById(R.id.tvLocationAudeioInfo);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onClick(view, getPosition(), true);
            return true;
        }
    }

    /*
     * Begins to download the file specified by the key in the bucket.
     */
    public void beginDownload(String key) {
        // Location to save the downloaded files from S3.
        audioFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + key);
        // Initiate the download
        transferUtility = AmazonS3Utility.getTransferUtility(this.context);
        TransferObserver observer = transferUtility.download(AmazonS3Constants.BUCKET_NAME, key, audioFile);
        observer.setTransferListener(new DownloadListener());
    }

    /*
     * A TransferListener class that can listen to a download task and be
     * notified when the status changes.
     */
    private class DownloadListener implements TransferListener {
        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "onError: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            int progress = (int) ((double) bytesCurrent * 100 / bytesTotal);
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            progressDialog.setProgress(progress);
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
            if (state.equals(TransferState.COMPLETED)) {
                try {
                    progressDialog.hide();
                    if (audioFile != null) {
                        if(myPlayer == null) {
                            myPlayer = new MediaPlayer();
                        }
                        myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        myPlayer.setDataSource(audioFile.getAbsolutePath());

                        myPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                seekBar.setMax(myPlayer.getDuration());
                                seekBar.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        seekChange(v);
                                        return false;
                                    }
                                });
                                seekBar.getProgress();
                            }
                        });

                        myPlayer.prepare();
                        myPlayer.start();
                        seekBar.setProgress(0);
                        startPlayProgressUpdater();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void seekChange(View v) {
        if (myPlayer != null && myPlayer.isPlaying()) {
            SeekBar sb = (SeekBar) v;
            myPlayer.seekTo(sb.getProgress());
        }
    }

    public void startPlayProgressUpdater() {
        if(myPlayer!=null) {
            seekBar.setProgress(myPlayer.getCurrentPosition()); //IllegalStateException coming here
            if (myPlayer.isPlaying()) {
                Runnable notification = new Runnable() {
                    public void run() {
                        startPlayProgressUpdater();
                    }
                };
                handler.postDelayed(notification, 1000);
            } else {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
            }
        }
    }

}

