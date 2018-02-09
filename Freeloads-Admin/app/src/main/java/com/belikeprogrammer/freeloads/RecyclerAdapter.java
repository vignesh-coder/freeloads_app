package com.belikeprogrammer.freeloads;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class RecyclerAdapter extends FirebaseRecyclerAdapter<Post, RecyclerAdapter.MyViewHolder> {
    ProgressBar progressBar;
    Activity activity;

    public RecyclerAdapter(Class modelClass, int modelLayout, Class viewHolderClass, Query ref, Activity activity) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.activity = activity;
        progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
    }

    @Override
    protected void populateViewHolder(MyViewHolder viewHolder, Post model, final int position) {

        progressBar.setVisibility(View.GONE);
        viewHolder.setTitle(model.getTitle());
        viewHolder.setFlag(model.getFlag());
        viewHolder.setCategory("Category : " + model.getCategory());
        viewHolder.setDelete(model.getUid());
        viewHolder.setImage(model.getImage0(), activity);
        viewHolder.setName(activity, model.getUid());
        viewHolder.setVerified(model.isVerified());
        viewHolder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Delete").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getRef(position).removeValue();
                    }
                }).setNegativeButton("cancel", null).show();
            }
        });
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, ViewPostActivity.class);
                intent.putExtra("Key", getRef(position).getKey());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    protected void onDataChanged() {

        super.onDataChanged();

        TextView noPosts = (TextView) activity.findViewById(R.id.no_posts);
        if (getItemCount() == 0) {

            noPosts.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

        } else {

            noPosts.setVisibility(View.GONE);
        }
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View view;
        FirebaseAuth mAuth;
        ImageView deleteImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            mAuth = FirebaseAuth.getInstance();


        }

        public void setVerified(boolean verified) {

            TextView verifiedText = view.findViewById(R.id.text_verified);
            if (verified)
                verifiedText.setVisibility(View.GONE);
            else
                verifiedText.setVisibility(View.VISIBLE);
        }

        public void setTitle(String title) {

            TextView titleTV = (TextView) view.findViewById(R.id.post_card_title);
            titleTV.setText(title);
        }

        public void setFlag(String flag) {

            TextView flagTV = (TextView) view.findViewById(R.id.post_card_flag);
            flagTV.setText(Html.fromHtml("<u>" + flag + "</u>"));
        }

        public void setImage(String image, Activity activity) {

            ImageView imageView = (ImageView) view.findViewById(R.id.post_card_image);
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.post_card_progress_bar);
            if (!TextUtils.isEmpty(image)) {
                imageView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                Glide.with(activity).load(image).placeholder(R.drawable.ic_image).into(imageView);
            } else {
                imageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        }

        public void setCategory(String category) {

            TextView categoryTV = (TextView) view.findViewById(R.id.post_card_category);
            categoryTV.setText(category);
        }

        public void setDelete(String uid) {
            deleteImage = (ImageView) view.findViewById(R.id.post_card_deleteBtn);
            if (mAuth.getCurrentUser() != null && uid.equals(mAuth.getCurrentUser().getUid())) {
                deleteImage.setVisibility(View.VISIBLE);
            } else {
                deleteImage.setVisibility(View.GONE);
            }
        }

        public void setName(final Activity activity, final String uid) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    User user = dataSnapshot.getValue(User.class);
                    TextView nameTV = (TextView) view.findViewById(R.id.post_card_name);
                    nameTV.setText(user.getName());
                    nameTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(activity, ProfileActivity.class);
                            intent.putExtra("uid", uid);
                            activity.startActivity(intent);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }
}
