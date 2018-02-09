package com.belikeprogrammer.freeloads;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mNameTextView, mEmailTextView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;
    private RecyclerView mRecyclerView;
    private Spinner mCategorySpinner;
    private Query query;
    private RecyclerAdapter mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Dashboard");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new post...
                Intent intent = null;
                if (mAuth.getCurrentUser() == null) {

                    intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    intent.putExtra("intent", true);
                } else {

                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        intent = new Intent(DashboardActivity.this, AddPostActivity.class);
                    } else {
                        intent = new Intent(DashboardActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("intent", true);
                    }
                }
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        View view = navigationView.inflateHeaderView(R.layout.nav_header_dashboard);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mAuth.getCurrentUser() != null) {

                    Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                    intent.putExtra("uid", mAuth.getCurrentUser().getUid());
                    startActivity(intent);
                }
            }
        });

        mNameTextView = (TextView) view.findViewById(R.id.nav_header_name);
        mEmailTextView = (TextView) view.findViewById(R.id.nav_header_email);

        mCategorySpinner = (Spinner) findViewById(R.id.dashboard_category_spinner);

        query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("verified").equalTo(true);

        final List<String> category = new ArrayList<>();
        category.add("Blood");
        category.add("Food");
        category.add("Clothes");
        category.add("Books");
        category.add("Money");
        category.add("Electronic Items");
        category.add("Medicine");
        category.add("Automobile");
        category.add("Home Appliances");
        category.add("Other");

        Collections.sort(category);
        category.add(0, "All");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, category);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(arrayAdapter);

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!category.get(position).equals("All"))
                    query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("category_verified").equalTo(category.get(position) + "_" + true);
                else
                    query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("verified").equalTo(true);

                RecyclerAdapter mFirebaseAdapter = new RecyclerAdapter(Post.class, R.layout.post_card, RecyclerAdapter.MyViewHolder.class, query, DashboardActivity.this);
                mRecyclerView.setAdapter(mFirebaseAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(DashboardActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mFirebaseAdapter = new RecyclerAdapter(Post.class, R.layout.post_card, RecyclerAdapter.MyViewHolder.class, query, this);
        mRecyclerView = (RecyclerView) findViewById(R.id.dashboard_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


                if (firebaseAuth.getCurrentUser() != null) {

                    mDatabaseUsers.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);
                            mNameTextView.setText(user.getName());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mEmailTextView.setText(firebaseAuth.getCurrentUser().getEmail());

                    Menu menu = navigationView.getMenu();
                    menu.getItem(0).setVisible(true);
                    menu.getItem(1).setVisible(false);
                    menu.getItem(2).setVisible(true);

                } else {

                    mNameTextView.setText("Guest");
                    mEmailTextView.setVisibility(View.GONE);

                    Menu menu = navigationView.getMenu();
                    menu.getItem(0).setVisible(false);
                    menu.getItem(1).setVisible(true);
                    menu.getItem(2).setVisible(false);

                }
            }
        };
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_my_post) {

            startActivity(new Intent(this, MyPostActivity.class));
        } else if (id == R.id.nav_login) {

            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            mRecyclerView.setAdapter(mFirebaseAdapter);
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exit");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    System.exit(0);
                }
            }).setNegativeButton("No", null).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }
}
