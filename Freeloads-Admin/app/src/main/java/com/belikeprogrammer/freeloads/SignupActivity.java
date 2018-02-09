package com.belikeprogrammer.freeloads;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.belikeprogrammer.freeloads.util.MyTextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText mNameET, mEmailET, mPassET, mConfrmET;
    private Button signUpBtn;
    private FirebaseAuth mAuth;
    private MyTextUtil textUtil;
    private DatabaseReference mDatabaseUsers;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getSupportActionBar().setTitle("Sign up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mNameET.getText().toString();
                String email = mEmailET.getText().toString();
                String password = mPassET.getText().toString();
                String cpassword = mConfrmET.getText().toString();

                if (password.length() < 6)
                    Toast.makeText(SignupActivity.this, "should contain atleast 6 characters.", Toast.LENGTH_SHORT).show();
                else if (!textUtil.isEmpty(name, email, password, cpassword))
                    signup(name, email, password, cpassword);
            }
        });
    }

    private void signup(final String name, String email, String password, String cpassword) {

        progressDialog.show();
        if (password.equals(cpassword)) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isComplete()) progressDialog.dismiss();
                    if (!task.isSuccessful())
                        Toast.makeText(SignupActivity.this, "Sign up failed.", Toast.LENGTH_LONG).show();
                    else {
                        DatabaseReference currentUser = mDatabaseUsers.child(mAuth.getCurrentUser().getUid());
                        User user = new User(name, "Hey there! I am using Freeloads");
                        currentUser.setValue(user);
                        Toast.makeText(SignupActivity.this, "Sign up success!", Toast.LENGTH_SHORT).show();
                        mAuth.getCurrentUser().sendEmailVerification();
                        mAuth.signOut();
                        Intent intent = new Intent(SignupActivity.this, VerifyEmailActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Confirm password does not match with password.", Toast.LENGTH_LONG).show();
        }
    }

    private void init() {

        mNameET = (EditText) findViewById(R.id.signup_name_Et);
        mEmailET = (EditText) findViewById(R.id.signup_email_Et);
        mPassET = (EditText) findViewById(R.id.signup_password_Et);
        mConfrmET = (EditText) findViewById(R.id.signup_cpassword_Et);
        signUpBtn = (Button) findViewById(R.id.signup_btn);
        textUtil = new MyTextUtil();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (!isTaskRoot()) super.onBackPressed();
            else startActivity(new Intent(this, LoginActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
