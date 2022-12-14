package com.example.cs5520_project;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn;
    TextView signUp;
    EditText username, password;
    String uid;
    RelativeLayout loadingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        loadingPanel = findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.GONE);

        username = findViewById(R.id.inputUsername1);
        password = findViewById(R.id.inputPassword1);

        loginBtn = findViewById(R.id.eventureLoginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInternetAvailable()) {
                    loadingPanel.setVisibility(View.VISIBLE);
                    String userName = username.getText().toString();
                    String userPass = password.getText().toString();
                    signIn(userName, userPass);
                } else {
                    Toast.makeText(LoginActivity.this, "Internet is required, please check your connection!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        signUp = findViewById(R.id.signUpText);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInternetAvailable()) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Internet is required, please check your connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void signIn(String username, String password){
        if(username.isEmpty() || username == null || username.trim().isEmpty() || username.contains(" ")){
            Toast.makeText(LoginActivity.this, "Please enter a valid username, no spaces!", Toast.LENGTH_SHORT).show();
            loadingPanel.setVisibility(View.GONE);
            return;
        }

        // Check with db records if user exists
        FirebaseDatabase.getInstance().getReference().child("Eventure Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExists = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    // if user exists, load their user profile
                    if (data.child("username").getValue().toString().equals(username)
                            && data.child("password").getValue().toString().equals(password)) {
                        uid = data.getKey();
                        loadUserProfile(uid);
                        userExists = true;
                        break;
                    }
                }
                // if user does not exist, create a new user
                if(!userExists){
                    Toast.makeText(LoginActivity.this, "Account not found. Please signup", Toast.LENGTH_SHORT).show();
                    loadingPanel.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){
                loadingPanel.setVisibility(View.GONE);
            }
        });
    }

    public void loadUserProfile(String uid){
        Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
        intent.putExtra("username",username.getText().toString());
        intent.putExtra("uid",uid);
        startActivity(intent);
        finish();
    }

    public boolean isInternetAvailable() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if( wifiInfo.getNetworkId() == -1 ){
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }
}