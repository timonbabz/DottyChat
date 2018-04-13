package com.timothy.dottychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profImage;
    private TextView dispName;
    private TextView statusText;
    private Toolbar tToolbar;
    private Button btnchangeStatus, imageButton;
    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mcurrentUser;
    private static final int GALLERY_CHOOSE = 1;

    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profImage = findViewById(R.id.profile_image);
        dispName = findViewById(R.id.settings_display_name);
        statusText = findViewById(R.id.settings_status);
        btnchangeStatus = findViewById(R.id.set_status);
        imageButton = findViewById(R.id.set_image);
        mProgress = new ProgressDialog(this);

        tToolbar = findViewById(R.id.settings_bar);
        setSupportActionBar(tToolbar);
        getSupportActionBar().setTitle("DottyChat Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mcurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = mcurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("dottyUsers").child(user_id);
        mUserDatabase.keepSynced(true);
        userRef = FirebaseDatabase.getInstance().getReference().child("dottyUsers").child(mAuth.getCurrentUser().getUid());

        mStorage = FirebaseStorage.getInstance().getReference();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumbnail = dataSnapshot.child("thumb_image").getValue().toString();

                dispName.setText(name);
                statusText.setText(status);

                if (!image.equals("default")) {

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).
                            placeholder(R.drawable.myavatar).into(profImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.myavatar).into(profImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnchangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = statusText.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_CHOOSE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CHOOSE && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgress.setTitle("Uploading image...");
                mProgress.setMessage("please wait");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                String current_userId = mcurrentUser.getUid();


                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mStorage.child("profile_images").child(current_userId +".jpg");
                final StorageReference thumb_nailFilePath =  mStorage.child("profile_images").child("thumbs").child(current_userId +".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful())
                        {
                            final String download_url = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_nailFilePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()){

                                        Map upload_hashMap = new HashMap();
                                        upload_hashMap.put("image", download_url);
                                        upload_hashMap.put("thumb_image", thumb_downloadUrl);

                                        mUserDatabase.updateChildren(upload_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful())
                                                {
                                                    mProgress.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Uploaded", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }else{ Toast.makeText(SettingsActivity.this, "Error in uploading file", Toast.LENGTH_LONG).show();
                                        mProgress.dismiss();}

                                }
                            });

                        }else{
                            Toast.makeText(SettingsActivity.this, "Error in uploading file", Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRef.child("online").setValue(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        userRef.child("online").setValue(false);
    }

}
