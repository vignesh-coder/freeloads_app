package com.belikeprogrammer.freeloads;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewPostActivity extends AppCompatActivity {

    private static final String TAG = "View Post";
    private ImageSwitcher imageSwitcher;
    private TextView titleTV, descTV, categoryTV, nameTV, contactTV;
    private Button mCallBtn;
    private ImageView nextBtn, prevBtn;
    private Post post;
    private DatabaseReference databaseReference;
    private int cnt = 0, pos = 0;
    private String uri[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        getSupportActionBar().setTitle("View post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                post = dataSnapshot.getValue(Post.class);
                setData(post);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void init() {

        imageSwitcher = findViewById(R.id.view_post_image_switcher);
        prevBtn = findViewById(R.id.view_post_prev);
        nextBtn = (ImageView) findViewById(R.id.view_post_next);
        titleTV = (TextView) findViewById(R.id.view_post_title);
        descTV = (TextView) findViewById(R.id.view_post_desc);
        categoryTV = (TextView) findViewById(R.id.view_post_category);
        nameTV = (TextView) findViewById(R.id.view_post_name);
        contactTV = (TextView) findViewById(R.id.view_post_number);
        mCallBtn = (Button) findViewById(R.id.view_post_call_btn);
        String key = getIntent().getStringExtra("Key");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(key);
    }

    public void setData(final Post data) {


        if (data.getImage0() != null) cnt++;
        if (data.getImage1() != null) cnt++;
        if (data.getImage2() != null) cnt++;
        if (data.getImage3() != null) cnt++;

        uri = new String[cnt];
        int i = 0;
        if (data.getImage0() != null) uri[i++] = data.getImage0();
        if (data.getImage1() != null) uri[i++] = data.getImage1();
        if (data.getImage2() != null) uri[i++] = data.getImage2();
        if (data.getImage3() != null) uri[i++] = data.getImage3();

        if (cnt > 0) {
            imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    ImageView imageView = new ImageView(getApplicationContext());
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                    return imageView;
                }
            });

            Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

            imageSwitcher.setInAnimation(in);
            imageSwitcher.setOutAnimation(out);

            prevBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (pos == 0) pos = cnt - 1;
                    else if (pos > 0) pos--;
                    Glide.with(ViewPostActivity.this).load(uri[pos]).into((ImageView) imageSwitcher.getCurrentView());
                }
            });
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    pos++;
                    if (pos == cnt) pos = 0;
                    Glide.with(ViewPostActivity.this).load(uri[pos]).into((ImageView) imageSwitcher.getCurrentView());
                }
            });

            Glide.with(ViewPostActivity.this)
                    .load(uri[pos])
                    .asBitmap()
                    .placeholder(R.drawable.ic_image)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            ProgressBar pb = (ProgressBar) findViewById(R.id.view_post_progress_bar);
                            pb.setVisibility(View.GONE);
                            pos++;
                            if (pos == uri.length) {
                                pos = 0;
                            }
                            imageSwitcher.setImageDrawable(new BitmapDrawable(getResources(), resource));
                            return true;
                        }
                    }).into((ImageView) imageSwitcher.getCurrentView());

        } else {
            imageSwitcher.setVisibility(View.GONE);
            prevBtn.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
            ProgressBar pb = (ProgressBar) findViewById(R.id.view_post_progress_bar);
            pb.setVisibility(View.GONE);
        }

        titleTV.setText(data.getTitle());
        descTV.setText(data.getDesc());
        categoryTV.setText("Category : " + data.getCategory());
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users").child(post.getUid());
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                String flag = data.getFlag();
                if (flag.equalsIgnoreCase("Donate"))
                    flag += "d";
                else
                    flag += "ed";
                nameTV.setText(flag + " by " + user.getName());
                nameTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(ViewPostActivity.this, ProfileActivity.class);
                        intent.putExtra("uid", post.getUid());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (!data.getContactNo().contains("+"))
            contactTV.setText("+91 " + data.getContactNo());
        else contactTV.setText(data.getContactNo());
        mCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeCall(data.getContactNo().trim());
            }
        });
    }

    private void makeCall(String number) {

        try {
            Intent callIntent = new Intent(Intent.ACTION_VIEW);
            callIntent.setData(Uri.parse("tel:" + number));
            startActivity(callIntent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();

        return super.onOptionsItemSelected(item);
    }
}
