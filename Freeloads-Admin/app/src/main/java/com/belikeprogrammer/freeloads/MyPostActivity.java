package com.belikeprogrammer.freeloads;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MyPostActivity extends AppCompatActivity {


    private RecyclerAdapter recyclerAdapter;
    private RecyclerView mRecyclerView;
    private Query query;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        getSupportActionBar().setTitle("My Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();
        query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());
        recyclerAdapter = new RecyclerAdapter(Post.class, R.layout.post_card, RecyclerAdapter.MyViewHolder.class, query, this);
        mRecyclerView = (RecyclerView) findViewById(R.id.dashboard_recycler_view);

        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(MyPostActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();

        return super.onOptionsItemSelected(item);
    }
}