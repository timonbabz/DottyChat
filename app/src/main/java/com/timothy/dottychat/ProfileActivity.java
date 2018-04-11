package com.timothy.dottychat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {


    private ImageView proImage;
    private TextView statusView;
    private TextView usersNumber;
    private TextView profDispName;
    private Button requestButton;
    private Button declineBtn;
    private ProgressDialog loadProgress;

    private DatabaseReference usersDatabase;
    private DatabaseReference friendsRequestDb;
    private DatabaseReference friendsDatabase;
    private DatabaseReference mNotificationDb;
    private FirebaseUser mUser_current;

    private String current_state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("dottyUsers").child(user_id);
        friendsRequestDb = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationDb = FirebaseDatabase.getInstance().getReference().child("notifications");
        mUser_current = FirebaseAuth.getInstance().getCurrentUser();

        profDispName = findViewById(R.id.profile_disp_name);
        statusView = findViewById(R.id.profile_status_name);
        usersNumber = findViewById(R.id.number_friends);
        requestButton = findViewById(R.id.button_request);
        proImage = findViewById(R.id.other_profImage);
        declineBtn = findViewById(R.id.decline_btn);

        current_state = "not_friends";

        loadProgress = new ProgressDialog(this);
        loadProgress.setTitle("Loading DottyUser details");
        loadProgress.setMessage("please wait while loading data");
        loadProgress.setCanceledOnTouchOutside(false);
        loadProgress.show();

        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status_display = dataSnapshot.child("status").getValue().toString();
                String image_profile = dataSnapshot.child("image").getValue().toString();

                profDispName.setText(display_name);
                statusView.setText(status_display);
                Picasso.with(ProfileActivity.this).load(image_profile).placeholder(R.drawable.profile).into(proImage);

                friendsRequestDb.child(mUser_current.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")){

                                requestButton.setText("Accept chat request");
                                current_state = "req_received";

                                declineBtn.setVisibility(View.VISIBLE);
                                declineBtn.setEnabled(true);

                            }else if (req_type.equals("sent")){

                                requestButton.setText("Cancel request");
                                current_state = "req_sent";

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        } else {
                            friendsDatabase.child(mUser_current.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        requestButton.setText("Unfriend!");
                                        current_state = "friends";

                                        declineBtn.setVisibility(View.INVISIBLE);
                                        declineBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                loadProgress.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestButton.setEnabled(false);

                //----------------send request [NOT FRIENDS] and notification-----------------------
                if(current_state.equals("not_friends"))
                {
                    friendsRequestDb.child(mUser_current.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                friendsRequestDb.child(user_id).child(mUser_current.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationMap = new HashMap<>();
                                        notificationMap.put("from", mUser_current.getUid());
                                        notificationMap.put("type", "request");

                                        mNotificationDb.child(user_id).push().setValue(notificationMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                requestButton.setEnabled(true);
                                                requestButton.setText("Cancel request");
                                                current_state = "req_sent";

                                                declineBtn.setVisibility(View.INVISIBLE);
                                                declineBtn.setEnabled(false);

                                                Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_LONG).show();

                                            }
                                        });

                                    }
                                });

                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //--------------cancel request [STOP REQUEST]-------------------------
                if(current_state.equals("req_sent"))
                {
                    friendsRequestDb.child(mUser_current.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendsRequestDb.child(user_id).child(mUser_current.getUid()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    requestButton.setEnabled(true);
                                    requestButton.setText("Send chat Request");
                                    current_state = "not_friends";

                                    declineBtn.setVisibility(View.INVISIBLE);
                                    declineBtn.setEnabled(false);

                                    Toast.makeText(ProfileActivity.this, "Request cancelled", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }

                //-----------------request received [ACCEPT FRIEND REQUEST]---------------------
                if (current_state.equals("req_received")){

                    final String current_date = DateFormat.getDateTimeInstance().format(new Date());
                    friendsDatabase.child(mUser_current.getUid()).child(user_id).setValue(current_date)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendsDatabase.child(user_id).child(mUser_current.getUid()).setValue(current_date)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    friendsRequestDb.child(mUser_current.getUid()).child(user_id).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    friendsRequestDb.child(user_id).child(mUser_current.getUid()).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    requestButton.setEnabled(true);
                                                                    requestButton.setText("Unfriend!");
                                                                    current_state = "friends";

                                                                    declineBtn.setVisibility(View.INVISIBLE);
                                                                    declineBtn.setEnabled(false);

                                                                    Toast.makeText(ProfileActivity.this, "You are now DottyChat Friends", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                        }
                    });
                }

                //-----------------------unFriend person---------------------------------------

            }
        });
    }
}
