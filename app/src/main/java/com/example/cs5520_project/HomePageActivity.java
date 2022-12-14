package com.example.cs5520_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class HomePageActivity extends AppCompatActivity implements LocationListener, NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    Button findNewEventBtn;
    RecyclerView eventRecyler, friendEventRecyler;
    EventAdapterYourEvents adapter;
    EventAdapterYourEvents friendsAdapter;
    String uid, locationString = "Boston";
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    protected LocationManager locationManager;
    private static final int LOCATION_REFRESH_TIME = 5000; // 5 minutes to update
    private static final int LOCATION_REFRESH_DISTANCE = 5000; // 5000 meters to update

    ArrayList<String> friendsList = new ArrayList<>();
    ArrayList<EventHelperClass> eventList = new ArrayList<>();
    ArrayList<EventHelperClass> addedEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home_page);
        uid = getIntent().getStringExtra("uid");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //check if app has permission to use location and request it if not
        if (ContextCompat.checkSelfPermission(HomePageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(HomePageActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            //subscribe to updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Eventure Users").child(uid);
        ref.child("addedEventList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addedEventList.clear();
                ArrayList<String> checkDuplicates = new ArrayList<String>();
                for(DataSnapshot data : snapshot.getChildren()) {
                    String eventTitle = data.child("title").getValue().toString();
                    if(!checkDuplicates.contains(eventTitle)) {
                        EventHelperClass event = new EventHelperClass(data.child("image").getValue().toString(), data.child("description").getValue().toString(), data.child("title").getValue().toString(), data.child("link").getValue().toString());
                        addedEventList.add(event);
                        checkDuplicates.add(eventTitle);
                        adapter.setEvents(addedEventList);
                    }
                }
                showNoEventsMessage(addedEventList.isEmpty());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        ref.child("friendsList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsList.clear();
                for(DataSnapshot data : snapshot.getChildren()) {
                    String friend = data.getValue().toString();
                    friendsList.add(friend);
                }
                loadFriendEvents();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        findNewEventBtn = findViewById(R.id.findNewEventBtn);
        findNewEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePageActivity.this,FindEventActivity.class);
                intent.putExtra("uid",uid);
                intent.putExtra("location",locationString);
                startActivity(intent);
            }
        });

        eventRecyler = findViewById(R.id.yourEventsRecycler);
        eventRecyler();

        friendEventRecyler = findViewById(R.id.yourFriendsEventsRecycler);
        friendEventRecyler();

        drawerLayout = findViewById(R.id.drawer_view);
        navigationView = findViewById(R.id.nav_view);
        ImageView navPanel = findViewById(R.id.sidePanel);

        navPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(HomePageActivity.this);
        navigationView.setCheckedItem(R.id.nav_home);

    }

    private void eventRecyler() {
        eventRecyler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new EventAdapterYourEvents(addedEventList, this, uid, true, friendsList);
        eventRecyler.setAdapter(adapter);
    }

    private void friendEventRecyler() {
        friendEventRecyler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        friendsAdapter = new EventAdapterYourEvents(eventList, this, uid, false, friendsList);
        friendEventRecyler.setAdapter(friendsAdapter);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            locationString = addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_add_friends:
                Intent friendIntent = new Intent(HomePageActivity.this, FriendListActivity.class);
                friendIntent.putExtra("uid",uid);
                friendIntent.putExtra("friendsList", friendsList);
                startActivity(friendIntent);
                finish();
                break;
            case R.id.nav_find_events:
                Intent eventIntent = new Intent(HomePageActivity.this,FindEventActivity.class);
                eventIntent.putExtra("uid",uid);
                eventIntent.putExtra("location",locationString);
                startActivity(eventIntent);
                break;
            case R.id.nav_logout:
                Intent logoutIntent = new Intent(HomePageActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    public void loadFriendEvents(){
        ArrayList<String> checkDuplicates = new ArrayList<String>();
        eventList.clear();
        showNoFriendsMessage(friendsList.isEmpty());
        for (String friend:friendsList) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Eventure Users").child(friend);
            ref.child("addedEventList").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot data : snapshot.getChildren()) {
                        String eventTitle = data.child("title").getValue().toString();
                        if(!checkDuplicates.contains(eventTitle)) {
                            EventHelperClass event = new EventHelperClass(data.child("image").getValue().toString(), data.child("description").getValue().toString(), data.child("title").getValue().toString(), data.child("link").getValue().toString());
                            eventList.add(event);
                            checkDuplicates.add(eventTitle);
                            friendsAdapter.setEvents(eventList);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString("uid", uid);
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        this.uid = savedInstanceState.getString("uid");
    }

    public void showNoEventsMessage(boolean shouldShow) {
        TextView noEventsText = findViewById(R.id.noEventsText);
        if(shouldShow) {
            noEventsText.setVisibility(View.VISIBLE);
        } else {
            noEventsText.setVisibility(View.GONE);
        }
    }

    public void showNoFriendsMessage(boolean shouldShow) {
        TextView noFriendsText = findViewById(R.id.noFriendsText);
        if(shouldShow) {
            noFriendsText.setVisibility(View.VISIBLE);
        } else {
            noFriendsText.setVisibility(View.GONE);
        }
    }

}