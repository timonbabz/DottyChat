package com.timothy.dottychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateAccount extends AppCompatActivity {

    private Button btnCreate;
    private TextInputLayout displayName;
    private TextInputLayout email;
    private TextInputLayout mpassword;
    private ProgressDialog tProgres;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Toolbar tToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        tToolbar = findViewById(R.id.create_appbar);
        setSupportActionBar(tToolbar);
        getSupportActionBar().setTitle("Create account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tProgres = new ProgressDialog(this);
        btnCreate = findViewById(R.id.create_account);
        displayName =findViewById(R.id.display_name);
        email = findViewById(R.id.email_account);
        mpassword = findViewById(R.id.password_text);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mDisplayName = displayName.getEditText().getText().toString().trim();
                String mEmail = email.getEditText().getText().toString().trim();
                String mPassword = mpassword.getEditText().getText().toString().trim();

                if(TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPassword) || TextUtils.isEmpty(mDisplayName)){
                    Toast.makeText(CreateAccount.this, "Please fill all fields", Toast.LENGTH_LONG).show();
                }else{
                    tProgres.show();
                    tProgres.setTitle("Adding user");
                    tProgres.setMessage("Please wait while we register you");
                    tProgres.setCanceledOnTouchOutside(false);
                    registerUser(mDisplayName, mEmail, mPassword);}
            }
        });

    }

    private void registerUser(final String mDisplayName, String mEmail, String mPassword) {

        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String u_id = current_user.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("dottyUsers").child(u_id);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", mDisplayName);
                            userMap.put("status", "Hi there, DottyChat is awesome!");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful())
                                    {
                                        tProgres.dismiss();
                                        Intent mainIntent = new Intent(CreateAccount.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });

                        } else {
                            tProgres.hide();
                            Toast.makeText(CreateAccount.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


}