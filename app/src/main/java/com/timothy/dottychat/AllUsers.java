package com.timothy.dottychat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsers extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView listView;
    private DatabaseReference mUsersDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mUsersDb = FirebaseDatabase.getInstance().getReference().child("dottyUsers");

        mToolbar = findViewById(R.id.all_users_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.users_list);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.users_list_layout,
                UsersViewHolder.class,
                mUsersDb

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setImage(model.getThumb_image(), getApplicationContext());
                viewHolder.setStatus(model.getStatus());

                final String user_id = getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profile_intent = new Intent(AllUsers.this, ProfileActivity.class);
                        profile_intent.putExtra("user_id", user_id);
                        startActivity(profile_intent);
                    }
                });
            }
        };

        listView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class  UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){

            TextView displayNameList = mView.findViewById(R.id.name_list);
            displayNameList.setText(name);
        }

        public void setStatus(String status){

            TextView statusList = mView.findViewById(R.id.status_list);
            statusList.setText(status);
        }

        public void setImage(String thumb_image, Context ctx){

            CircleImageView imageList = mView.findViewById(R.id.prof_avatar);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.newest_avatar).into(imageList);
        }

        public void setUserOnline(boolean online_status){

            ImageView iconOnline = mView.findViewById(R.id.imageViewOnline);

            if (online_status == true){

                iconOnline.setVisibility(View.VISIBLE);
            }else{
                iconOnline.setVisibility(View.INVISIBLE);
            }
        }
    }
}
