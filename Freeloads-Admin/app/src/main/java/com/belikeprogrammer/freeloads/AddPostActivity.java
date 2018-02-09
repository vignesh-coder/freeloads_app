package com.belikeprogrammer.freeloads;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.belikeprogrammer.freeloads.util.MyTextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioGroup mRadioGroup;
    private RadioButton mDonateRBtn, mNeedBtn;
    private TextView mPhotosTv;
    private LinearLayout mPhotosLL;
    private ImageView mImages[];
    private Spinner mCategorySpinner;
    private EditText mTitleET, mDescET, mAddressET, mContactET;
    private Button mPostBtn;
    private String userChoosenTask;
    private ImageView ivImage;
    private int i;
    private Bitmap toUpload[];
    private FirebaseAuth mAuth;
    private int count = 0;
    private DatabaseReference mDatabasePosts;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute);
        getSupportActionBar().setTitle("Add Post");
        init();
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                if (checkedId == mDonateRBtn.getId()) {

                    mPhotosTv.setVisibility(View.VISIBLE);
                    mPhotosLL.setVisibility(View.VISIBLE);
                } else if (checkedId == mNeedBtn.getId()) {

                    mPhotosTv.setVisibility(View.GONE);
                    mPhotosLL.setVisibility(View.GONE);
                }
            }
        });
        List<String> category = new ArrayList<>();
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
        category.add(0, "Select a category");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, category);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(arrayAdapter);

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = mTitleET.getText().toString();
                String desc = mDescET.getText().toString();
                String address = mAddressET.getText().toString();
                String contactNo = mContactET.getText().toString();

                if (!new MyTextUtil().isEmpty(title, desc, address, contactNo) && !mCategorySpinner.getSelectedItem().equals("Select a category")) {
                    startPosting(title, desc, address, contactNo);
                }
            }
        });

    }

    private void startPosting(String title, String desc, String address, String contactNo) {

        final DatabaseReference currentpost = mDatabasePosts.push();


        for (int i = 0; i < 4; i++)
            if (toUpload[i] != null) count++;


        String flag = (mRadioGroup.getCheckedRadioButtonId() == mDonateRBtn.getId()) ? "Donate" : "Need";
        if (count > 0 || flag.equals("Need")) {


            progressDialog.setCancelable(false);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();


            String category = mCategorySpinner.getSelectedItem().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("uid", mAuth.getCurrentUser().getUid());
            map.put("title", title);
            map.put("category", category);
            map.put("desc", desc);
            map.put("flag", flag);
            map.put("contactNo", contactNo);
            map.put("address", address);
            map.put("timestamp", System.currentTimeMillis());
            map.put("verified", false);
            map.put("category_verified", category + "_" + false);
            map.put("uid_verified", map.get("uid") + "_" + false);
            currentpost.setValue(map);
            uploadImages(currentpost, 0);
        }
    }

    private void uploadImages(final DatabaseReference currentpost, final int c) {

        if (c == count) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Post Added!");
            builder.setMessage("Congrats. Your post will be live after the verification.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).show();

        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            toUpload[c].compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();


            final StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            storageReference.child(currentpost.getKey() + "" + c).putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        progressDialog.setMessage("Uploaded " + (c + 1) + " image(s) out of " + count);
                        currentpost.child("image" + c).setValue(task.getResult().getDownloadUrl().toString());
                    } else {
                        Toast.makeText(AddPostActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    }
                    uploadImages(currentpost, c + 1);
                }
            });
        }

    }

    private void init() {

        mRadioGroup = (RadioGroup) findViewById(R.id.contribute_radio_group);
        mDonateRBtn = (RadioButton) findViewById(R.id.contribute_donate_rbtn);
        mNeedBtn = (RadioButton) findViewById(R.id.contribute_need_rbtn);

        mPhotosTv = (TextView) findViewById(R.id.contribute_photos_TV);
        mPhotosLL = (LinearLayout) findViewById(R.id.contribute_photos_LL);

        mImages = new ImageView[4];

        mImages[0] = (ImageView) findViewById(R.id.contribute_add_image1);
        mImages[1] = (ImageView) findViewById(R.id.contribute_add_image2);
        mImages[2] = (ImageView) findViewById(R.id.contribute_add_image3);
        mImages[3] = (ImageView) findViewById(R.id.contribute_add_image4);

        for (ImageView iv : mImages)
            iv.setOnClickListener(this);

        mCategorySpinner = (Spinner) findViewById(R.id.contribute_category_spinner);

        mTitleET = (EditText) findViewById(R.id.contribute_title_ET);
        mDescET = (EditText) findViewById(R.id.contribute_desc_ET);
        mAddressET = (EditText) findViewById(R.id.contribute_address_ET);
        mContactET = (EditText) findViewById(R.id.contribute_contact_ET);

        mPostBtn = (Button) findViewById(R.id.contribute_post_btn);
        mDatabasePosts = FirebaseDatabase.getInstance().getReference().child("Posts");

        toUpload = new Bitmap[4];
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Remove Image"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(AddPostActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Remove Image")) {
                    ivImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_box));
                    toUpload[i] = null;
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 27);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), 28);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                    Toast.makeText(this, "Cannot choose image.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 28)
                onSelectFromGalleryResult(data);
            else if (requestCode == 27)
                onCaptureImageResult(data);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ivImage.setImageBitmap(bm);
        toUpload[i] = bm;
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ivImage.setImageBitmap(thumbnail);
        toUpload[i] = thumbnail;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == mImages[0].getId()) {
            ivImage = mImages[0];
            i = 0;
        } else if (v.getId() == mImages[1].getId()) {
            ivImage = mImages[1];
            i = 1;
        } else if (v.getId() == mImages[2].getId()) {
            ivImage = mImages[2];
            i = 2;
        } else if (v.getId() == mImages[3].getId()) {
            ivImage = mImages[3];
            i = 3;
        }
        selectImage();
    }
}
