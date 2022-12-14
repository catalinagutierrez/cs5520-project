package com.example.cs5520_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    TextView username;
    Spinner spinner;
    Button historyBtn;
    private RecyclerView recyclerView;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    RecyclerView.LayoutManager layoutManager;
    RecylerViewAdapter recylerViewAdapter;
    int arr[] = {R.drawable.emoji_1,R.drawable.emoji_10,R.drawable.emoji_3,
    R.drawable.emoji_4,R.drawable.emoji_5, R.drawable.emoji_6, R.drawable.emoji_7,
    R.drawable.emoji_8,R.drawable.emoji_9, R.drawable.emoji_2};
    List<String> friendsList;
    String selectedFriend;
    UserInfo currentUser;

    private static final String CHANNEL_ID = "CHANNEL_NO_1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        username = findViewById(R.id.userId);
        historyBtn = findViewById(R.id.historyBtn);
        currentUser = (UserInfo) getIntent().getSerializableExtra("CURRENT_USER");
        friendsList = new ArrayList<>();

        selectedFriend = "";

        // Add users to friends dropdown
        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(UserProfileActivity.this, android.R.layout.simple_list_item_1, friendsList);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedFriend = friendsList.get(i);
            }

            //Selects the first friend if none were selected
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedFriend = friendsList.get(0);
            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, HistoryActivity.class);
                intent.putExtra("CURRENT_USER", currentUser);
                startActivity(intent);
            }
        });

        // Load Users
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference();
        reference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsList.clear();
                for(DataSnapshot data : snapshot.getChildren()) {
                    String friendUsername = data.child("username").getValue().toString();
                    if(!currentUser.getUsername().equals(friendUsername)) {
                        friendsList.add(friendUsername);
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        String key = reference.child("Transactions").push().getKey();
        reference.child("Transactions").orderByKey().startAt(key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                    String receiver = snapshot.child("receiver").getValue().toString();
                    if (currentUser.getUsername().equals(receiver)) {
                        String sender = snapshot.child("sender").getValue().toString();
                        String stickerName = snapshot.child("sticker").getValue().toString();
                        currentUser.incrementStickerCount("received", stickerName);
                        reference.child("Users").child(currentUser.uid).setValue(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    sendNotifictaion(UserProfileActivity.this, stickerName + " sent by " + sender, stickerName);
                                } else {
                                    Toast.makeText(UserProfileActivity.this, "Failed to send, please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });



        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        recylerViewAdapter = new RecylerViewAdapter(arr,this);
        recyclerView.setAdapter(recylerViewAdapter);
        recyclerView.setHasFixedSize(true);
        username.setText(currentUser.username);
    }

    public void sendNotifictaion(Context context, String message, String stickerName){
        // Create your notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "myChannel", importance);
            channel.setDescription(message);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        int stickerId = Integer.valueOf(stickerName.split(" ")[1]) - 1;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(arr[stickerId])
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), arr[stickerId]))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(message);

        if (Build.VERSION.SDK_INT >= 21) mBuilder.setVibrate(new long[0]);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }


    public class RecylerViewAdapter extends RecyclerView.Adapter<RecylerViewAdapter.MyViewHolder> {

        int []arr;
        Context context;

        public RecylerViewAdapter(int[] arr, Context context) {
            this.arr = arr;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);

            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.imageView.setImageResource(arr[position]);
            holder.textView.setText("Emoji " + (position + 1));
        }

        @Override
        public int getItemCount() {
            return arr.length;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView imageView;
            TextView textView;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                textView = itemView.findViewById(R.id.friendNameText);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,StickerInfoActivity.class);
                intent.putExtra("IMAGE",arr[getAbsoluteAdapterPosition()]);
                intent.putExtra("TITLE","Emoji " + (getAbsoluteAdapterPosition() + 1));
                intent.putExtra("CURRENT_USER", currentUser);
                intent.putExtra("SELECTED_FRIEND", selectedFriend);
                context.startActivity(intent);
            }
        }

    }

}