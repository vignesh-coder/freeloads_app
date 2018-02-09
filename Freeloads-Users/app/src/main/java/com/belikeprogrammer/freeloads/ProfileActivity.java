package com.belikeprogrammer.freeloads;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements TextWatcher {


    private RecyclerAdapter recyclerAdapter;
    private RecyclerView mRecyclerView;
    private EditText mNameET, mBioET;
    private Button mUpdateBtn;
    private Query query;
    private FirebaseAuth mAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        uid = getIntent().getStringExtra("uid");

        init();
        final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mNameET.setText(user.getName());
                mBioET.setText(user.getBio());
                mNameET.addTextChangedListener(ProfileActivity.this);
                mBioET.addTextChangedListener(ProfileActivity.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = mNameET.getText().toString();
                String bio = mBioET.getText().toString();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(bio)) {
                    userDatabase.setValue(new User(name, bio));
                    Toast.makeText(ProfileActivity.this, "Updated!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(ProfileActivity.this, "Fields should not be left blank.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();

        mNameET = findViewById(R.id.name_et);
        mBioET = findViewById(R.id.bio_et);
        mUpdateBtn = findViewById(R.id.update_btn);
        mUpdateBtn.setVisibility(View.GONE);

        if (uid.equals(mAuth.getCurrentUser().getUid())) {

            query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uid")
                    .equalTo(uid);
        } else {

            query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("uid_verified")
                    .equalTo(uid + "_" + true);
            mNameET.setKeyListener(null);
            mBioET.setKeyListener(null);
        }
        recyclerAdapter = new RecyclerAdapter(Post.class, R.layout.post_card, RecyclerAdapter.MyViewHolder.class, query, this);
        mRecyclerView = findViewById(R.id.dashboard_recycler_view);


        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(ProfileActivity.this);
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

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        if (mUpdateBtn.getVisibility() == View.GONE)
            mUpdateBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
