package solutiontogo.de.audiocitytourguide;

/**
 * Created by maheshkandhari on 1/17/2017.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import solutiontogo.de.audiocitytourguide.utils.PropertyReader;

public class NavigationHeader extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static String TAG = NavigationHeader.class.getSimpleName();

    public String latlngStr = null;
    public static Boolean isLaunched = Boolean.TRUE;
    public PropertyReader propertyReader;
    public Button btnLoginInHeader;
    public NavigationView navigationView;
    public LinearLayout linearLayout;
    public DrawerLayout drawerLayout;
    public TextView tvAppBarTitle;
    public static Bitmap bitmap;
    public static String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        propertyReader = new PropertyReader(getBaseContext());

        linearLayout = (LinearLayout) findViewById(R.id.layout_content);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        btnLoginInHeader = (Button) header.findViewById(R.id.btnLoginInHeader);
        btnLoginInHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
                return false;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        tvAppBarTitle = (TextView) findViewById(R.id.tvAppBarTitle);

        if (isLaunched) {
            isLaunched = Boolean.FALSE;
            openActivity(R.id.nav_explore);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        openActivity(item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openActivity(int id) {
        Intent intent;
        switch (id) {
            case R.id.nav_editor_choice:
                // Handle the camera action
/*                intent = new Intent(this, CameraDemoActivity.class);
                startActivity(intent);*/
                break;

            case R.id.nav_explore:
                intent = new Intent(this, ExploreActivity.class);
                startActivity(intent);

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.nav_tag:
                intent = new Intent(this, UploadLocationDetails.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.nav_bookmarks:
                intent = new Intent(this, RecordAndUploadAudio.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.nav_share:
                break;

            case R.id.nav_history:
                break;

            case R.id.nav_settings:
                break;

            case R.id.fb_share:
                break;

            case R.id.google_share:
                break;

            case R.id.instragam_share:
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (null != drawerLayout && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}