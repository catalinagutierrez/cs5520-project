package com.example.cs5520_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    String currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        selectedFriend = "";
        username = findViewById(R.id.userId);
        Bundle bundle = getIntent().getExtras();
        currentUser = bundle.getString("username");
        friendsList = new ArrayList<>();

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

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "Nothing sleected" ,Toast.LENGTH_SHORT).show();
            }
        });

        // Load Users
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference();
        reference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot datas : snapshot.getChildren()) {
                    String friendUsername = datas.child("username").getValue().toString();
                    if(!currentUser.equals(friendUsername)) {
                        friendsList.add(datas.child("name").getValue().toString());
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        recylerViewAdapter = new RecylerViewAdapter(arr,this);
        recyclerView.setAdapter(recylerViewAdapter);
        recyclerView.setHasFixedSize(true);

        username.setText(currentUser);
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
            holder.clickedView.setText("Sent " + 0 + " times");
        }

        @Override
        public int getItemCount() {
            return arr.length;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView imageView;
            TextView textView;
            TextView clickedView;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                textView = itemView.findViewById(R.id.textView);
                clickedView = itemView.findViewById(R.id.clickedView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,StickerInfoActivity.class);
                intent.putExtra("image",arr[getAbsoluteAdapterPosition()]);
                intent.putExtra("title","Emoji " + (getAbsoluteAdapterPosition() + 1));
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("selectedFriend", selectedFriend);
                context.startActivity(intent);
            }
        }

    }

}