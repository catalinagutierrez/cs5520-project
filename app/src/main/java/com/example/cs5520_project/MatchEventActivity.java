package com.example.cs5520_project;

import android.os.Bundle;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MatchEventActivity extends AppCompatActivity {

    RecyclerView eventRecyler;
    RecyclerView.Adapter adapter;
    ArrayList<EventHelperClass> eventList = new ArrayList<>();
    String uid;
    String URL1 = "https://serpapi.com/search.json?engine=google_events&q=Events+in+Austin&hl=en&gl=us\n" +
            "&api_key=5c030de392f00a9601c21416005ea917dabe08e5e8742d41cf8be0a4a566e7f1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_match_event);
        uid = getIntent().getStringExtra("uid");

        eventRecyler = findViewById(R.id.event_recycler);
        String URL = getIntent().getStringExtra("URL");
        parseApiData(URL);

    }

    private void eventRecyler(ArrayList eventList) {
        eventRecyler.setHasFixedSize(true);
        eventRecyler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new EventAdapterFind(eventList,MatchEventActivity.this, uid);
        eventRecyler.setAdapter(adapter);
    }

    public void parseApiData(String URL){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("events_results");
                    if(jsonArray.length() > 0) {
                        for(int i=0; i < jsonArray.length(); i++){
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String description = "";
                            if (jsonObject1.has("description")){
                                description = jsonObject1.getString("description");
                            }
                            String image = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQez2075rzHXB0DoVoojer29WCh0wPNxO-HNgfrYEawd3AU6c1_oXrKQkA&s";
                            if(jsonObject1.has("thumbnail")){
                                image = jsonObject1.getString("thumbnail");
                            }
                            String title = "";
                            if(jsonObject1.has("title")){
                                title = jsonObject1.getString("title");
                            }
                            eventList.add(new EventHelperClass(image,description,title));
                        }

                    }
                    eventRecyler(eventList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}