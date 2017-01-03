package solutiontogo.de.audiocitytourguide;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    public static String LOCATION_IMAGE_URL = "location_image_url";
    public static String LOCATION_IMAGE_INFO = "location_image_info";
    public CustomAdapter adapter;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected String latitude,longitude;

    public Button btnLoginInHeader;
    public TextView tvLocationDescription;
    public ListView lvInExplorerActivity;
    public ImageView ivInListItem;
    public ImageView ivLocationImage;
    public Fragment map;
    public ImageView ivLocationAudioListItem;
    public TextView tvLocationAudeioInfo;

    HashMap<String, Object> temp = new HashMap<String, Object>();
    ArrayList<HashMap<String, Object>> originalValues = new ArrayList<HashMap<String, Object>>();
    Integer locationsAudioInteger[] = {R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        init();

        tvLocationDescription.setText("      The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.  The description of the location displayed in the left window. The description of the location displayed in the left window.");
        tvLocationDescription.setTypeface(getFontType());
        tvLocationDescription.scrollTo(0, 0);
        tvLocationDescription.setMovementMethod(new ScrollingMovementMethod());

        ivInListItem = (ImageView) findViewById(R.id.ivLocationAudioListItem);

        temp = new HashMap<String, Object>();
        String locations[] = {"Hyderabad", "Secuderabad", "Bangalore", "Tirupati", "Delhi"};
        for (String location:locations) {
            temp.put(LOCATION_IMAGE_URL, R.drawable.image1);
            temp.put(LOCATION_IMAGE_INFO, location);
            originalValues.add(temp);
        }

        adapter = new CustomAdapter(this, R.layout.simplerow, originalValues);
        lvInExplorerActivity = (ListView) findViewById(R.id.ivLocationAudioList);
        lvInExplorerActivity.setAdapter(adapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        btnLoginInHeader  = (Button) header.findViewById(R.id.btnLoginInHeader);
        btnLoginInHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent login = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(login);
                return false;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

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
        int id = item.getItemId();
        System.out.println(id);

        if (id == R.id.nav_editor_choice) {

        } else if (id == R.id.nav_explore) {
            // Handle the camera action
            Intent intent = new Intent("solutiontogo.de.audiocitytourguide.CameraDemoActivity");
            startActivity(intent);

        } else if (id == R.id.nav_tag) {

        } else if (id == R.id.nav_bookmarks) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_history) {

        }else if (id == R.id.nav_settings) {

        }else if (id == R.id.fb_share) {

        }else if (id == R.id.google_share) {

        }else if (id == R.id.instragam_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        LatLng sydney = new LatLng(12.9720810, 77.6472364);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Categis"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    public void onLocationChanged(Location loc) {
        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                + cityName;
        //editLocation.setText(s);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void setMenuItemCustomTextStyle(){
        tvLocationDescription.setTypeface(getFontType());
    }

    private void init(){

        //map = (Fragment) findViewById(R.id.map);
        ivLocationImage = (ImageView) findViewById(R.id.ivLocationImage);
        tvLocationDescription =  (TextView) findViewById(R.id.tvLocationDescription);
        //ivLocationAudioListItem = (ImageView) findViewById(R.id.ivLocationAudioListItem);
        //tvLocationAudeioInfo = (TextView) findViewById(R.id.tvLocationAudeioInfo);
    }


    // define your custom adapter
    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {
        LayoutInflater inflater;

        public CustomAdapter(Context context, int textViewResourceId,
                             ArrayList<HashMap<String, Object>> Strings) {
            super(context, textViewResourceId, Strings);
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // class for caching the views in a row
        private class ViewHolder {

            TextView tvLocationAudioInfo;
            ImageView ivInListItem;
            int[] imageIds = new int[]{R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1, R.drawable.image1};


        }

        ViewHolder viewHolder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.simplerow, null);
                viewHolder = new ViewHolder();
                viewHolder.ivInListItem = (ImageView) convertView.findViewById(R.id.ivLocationAudioListItem);

                viewHolder.tvLocationAudioInfo = (TextView) convertView.findViewById(R.id.tvLocationAudeioInfo);
                viewHolder.tvLocationAudioInfo.scrollTo(0, 0);
                viewHolder.tvLocationAudioInfo.setMovementMethod(new ScrollingMovementMethod());

                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.ivInListItem.setImageResource(viewHolder.imageIds[0]);

            viewHolder.tvLocationAudioInfo.setText(originalValues.get(position).get(LOCATION_IMAGE_INFO).toString());
            viewHolder.tvLocationAudioInfo.setTypeface(getFontType());
            viewHolder.tvLocationAudioInfo.scrollTo(0, 0);
            viewHolder.tvLocationAudioInfo.setMovementMethod(new ScrollingMovementMethod());

            // return the view to be displayed
            return convertView;
        }
    }

    public Typeface getFontType(){
        Typeface droidSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        return droidSans;
    }

}
