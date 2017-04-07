package solutiontogo.de.audiocitytourguide;

/**
 * Created by shivaramak on 02/01/2017.
 */

import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;


public class ProfileActivity extends NavigationHeader {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.profile, linearLayout);
        tvAppBarTitle.setText("Profile Details...");

    }
}