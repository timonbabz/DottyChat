package com.timothy.dottychat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chat_user;
    private Toolbar tToolbar;
    private TextView dispName;
    private TextView lastSeen;
    private CircleImageView imageView;

    private DatabaseReference mRootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dispName = findViewById(R.id.bar_display_name);
        lastSeen =findViewById(R.id.last_seen);
        imageView = findViewById(R.id.custom_bar_image);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        chat_user = getIntent().getStringExtra("user_id");
        final String chat_user_name = getIntent().getStringExtra("user_name");

        tToolbar = findViewById(R.id.chat_bar);
        setSupportActionBar(tToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionBarView);

        dispName.setText(chat_user_name);

        mRootRef.child("dottyUsers").child(chat_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true"))
                {
                   // lastSeen.setText("Online");
                }else{
                   // lastSeen.setText(online);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
