package solutiontogo.de.audiocitytourguide;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    File audioFile = null;
    TransferUtility transferUtility;
    ArrayList<String> locationAudioFiles;
    ArrayList<Integer> locationAudioThumbs;
    Context context;

    public HLVAdapter(Context context, ArrayList<String> locationAudioFiles, ArrayList<Integer> locationAudioThumbs) {
        super();
        this.context = context;
        this.locationAudioFiles = locationAudioFiles;
        this.locationAudioThumbs = locationAudioThumbs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.tvSpecies.setText(locationAudioFiles.get(i));
        viewHolder.imgThumbnail.setImageResource(locationAudioThumbs.get(i));

        viewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (isLongClick) {
                    beginDownload(locationAudioFiles.get(position));
//                    Toast.makeText(context, "#" + position + " - " + locationAudioFiles.get(position) + " (Long click)", Toast.LENGTH_SHORT).show();
                } else {
                    beginDownload(locationAudioFiles.get(position));
//                    Toast.makeText(context, "#" + position + " - " + locationAudioFiles.get(position), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationAudioFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public ImageView imgThumbnail;
        public TextView tvSpecies;
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
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
            if(state.equals(TransferState.COMPLETED)) {
                try {
                    if(audioFile != null) {
                        MediaPlayer myPlayer = new MediaPlayer();
                        myPlayer.setDataSource(audioFile.getAbsolutePath());
                        myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        myPlayer.prepare();
                        myPlayer.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}

