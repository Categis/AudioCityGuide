package solutiontogo.de.audiocitytourguide;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by maheshkandhari on 4/7/2017.
 */

public class LocationDetailsActivity extends NavigationHeader {

    SeekBar ldSeekBar;
    ImageView ivLdLocationImage;
    RecyclerView ldRecyclerView;
    TextView tvLdLocationDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.full_location_details, linearLayout);

        tvAppBarTitle.setText("Location Details");

        ivLdLocationImage = (ImageView) findViewById(R.id.ivLdLocationImage);
        System.out.println(bitmap == null);
        ivLdLocationImage.setImageBitmap(bitmap);

        tvLdLocationDescription = (TextView) findViewById(R.id.tvLdLocationDescription);
        System.out.println("::::"+description);
        tvLdLocationDescription.setText(description);

        ldRecyclerView = (RecyclerView) findViewById(R.id.ld_recycler_view);
        ldRecyclerView.setHasFixedSize(true);

        ldSeekBar = (SeekBar) findViewById(R.id.ld_seek_bar);
    }
}
