package com.example.cs5520_project;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;


public class EventAdapterFind extends RecyclerView.Adapter<EventAdapterFind.EventViewHolder> {

    ArrayList<EventHelperClass> eventList;
    EventHelperClass eventHelperClass;
    Context context;
    String uid;

    public EventAdapterFind(ArrayList<EventHelperClass> eventList, Context context, String uid) {
        this.eventList = eventList;
        this.context = context;
        this.uid = uid;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event,parent,false);
        EventViewHolder eventViewHolder = new EventViewHolder(view);
        return eventViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        eventHelperClass = eventList.get(position);
        holder.description.setText(eventHelperClass.getDescription());
        holder.eventTitle.setText(eventHelperClass.getTitle());

        // Fetch details
        String description = eventHelperClass.getDescription();
        String title = eventHelperClass.getTitle();
        String image = eventHelperClass.getImage();
        String link = eventHelperClass.getLink();

        Picasso.get().load(eventHelperClass.getImage()).placeholder(R.drawable.loading_screen).error(R.drawable.imagenotfound).into(holder.image);
        holder.eventMoreInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(browserIntent);
            }
        });
        holder.addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventHelperClass event = new EventHelperClass(image, description, title, link);
                addEvent(event);
            }
        });
    }

    public void addEvent(EventHelperClass event) {
        String eid = FirebaseDatabase.getInstance().getReference("Eventure Users").child(uid).child("addedEventList").push().getKey();
        FirebaseDatabase.getInstance().getReference("Eventure Users").child(uid).child("addedEventList").child(eid).setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, "Event Added!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context,HomePageActivity.class);
                    intent.putExtra("uid",uid);
                    context.startActivity(intent);
                }else{
                    Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView description;
        TextView eventTitle;
        Button addEvent;
        Button eventMoreInfoBtn;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.eventImage);
            description = itemView.findViewById(R.id.eventDescription);
            eventTitle = itemView.findViewById(R.id.eventCardTitle);
            addEvent = itemView.findViewById(R.id.eventRegisterBtn);
            eventMoreInfoBtn = itemView.findViewById(R.id.eventMoreInfoBtn);
        }
    }


}
