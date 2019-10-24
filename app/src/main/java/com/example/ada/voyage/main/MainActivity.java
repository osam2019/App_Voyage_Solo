package com.example.ada.voyage.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.ada.voyage.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EditText et;
    Button bt;
    ListView lv;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;


    private BackPressCloseHandler backPressCloseHandler;
    View header;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    ImageButton setting;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backPressCloseHandler = new BackPressCloseHandler(this);


        et = (EditText) findViewById(R.id.event_title);
        bt = (Button) findViewById(R.id.submitt);
        lv = (ListView) findViewById(R.id.event_list);

        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
        lv.setAdapter(adapter);



        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("/users");
        mAuth = FirebaseAuth.getInstance();
        myRef.push().setValue("users");
        FirebaseUser currentUser =  mAuth.getCurrentUser();
        String personName = (String) currentUser.getDisplayName();
        String personEmail = (String) currentUser.getEmail();
        String personId = (String) currentUser.getUid();
        Uri photoUrl = currentUser.getPhotoUrl();
        DatabaseReference childRef = myRef.child(personId);
        DatabaseReference child1 = childRef.child("Email");
        DatabaseReference child2 = childRef.child("Name");

        child1.setValue(personEmail);
        child2.setValue(personName);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);


        header = navigationView.getHeaderView(0);
        TextView username = (TextView) header.findViewById(R.id.username);
        TextView userEmail = (TextView) header.findViewById(R.id.userAccount);
        ImageView profile = (ImageView) header.findViewById(R.id.imageView);
        username.setText(personName);
        userEmail.setText(personEmail);
        Picasso.with(MainActivity.this).load(photoUrl).into(profile);

        navigationView.setNavigationItemSelectedListener(this);
        displaySelectedScreen(R.id.nav_blog);

        onBtnClick();
    }

    public  void onBtnClick(){
        bt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String result = et.getText().toString();
                //arrayList.add(result);
                //adapter.notifyDataSetChanged();

                adapter.add(result);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backPressCloseHandler.onBackPressed();
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

        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(int itemId) {
        ActionBar actionBar = getSupportActionBar();
        //creating fragment object
        Fragment fragment = null;

        setting = (ImageButton) header.findViewById(R.id.settings);

        //initializing the fragment object which is selected
        switch (itemId) {

            case R.id.nav_map:

                String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if (!provider.contains("gps")) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        Toast.makeText(getApplicationContext(), "Please Turn on GPS", Toast.LENGTH_LONG).show();
                    }

                break;


        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    public void onClick(View view) {
        /** If the user clicks the settings icon, go to the settings activity after closing the drawer */
        if(view.getId()==(header.findViewById(R.id.settings)).getId()){
            Intent i= new Intent(this, SettingsActivity.class);
            startActivity(i);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    public void gotoCal(View view) {
        //setContentView(R.layout.calendar_activity_event);
        setContentView(R.layout.fragment_calendar);
    }

    public void gotoList(View view) {
        setContentView(R.layout.fragment_calendar);
    }

    public void gotoSelect(View view) {
        setContentView(R.layout.activity_money_detail);
    }

    public void openCamera(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);

    }




}
