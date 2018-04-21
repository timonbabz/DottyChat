package com.timothy.dottychat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private View mainView;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference favouriteUsers;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = mainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("dottyUsers");
        favouriteUsers = FirebaseDatabase.getInstance().getReference().child("favourites").child(current_user_id);

        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));


        // Inflate the layout for this fragment
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdpaterView = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.users_list_layout,
                FriendsViewHolder.class,
                mFriendsDatabase

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String user_list_id = getRef(position).getKey();

                mUsersDatabase.child(user_list_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();


                        if (dataSnapshot.hasChild("online"))
                        {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.setName(userName);
                        viewHolder.setImage(userThumb, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"View profile", "Chat", "Add to Favourites"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (i == 0)
                                        {
                                            Intent profile_intent = new Intent(getContext(), ProfileActivity.class);
                                            profile_intent.putExtra("user_id", user_list_id);
                                            startActivity(profile_intent);
                                        }
                                        if (i == 1)
                                        {
                                            Intent chat_intent = new Intent(getContext(), ChatActivity.class);
                                            chat_intent.putExtra("user_name", userName);
                                            chat_intent.putExtra("user_id", user_list_id);
                                            startActivity(chat_intent);
                                        }
                                        if (i == 2)
                                        {
                                            favouriteUsers.child(user_list_id).setValue(userName);
                                        }

                                    }
                                });

                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mFriendsList.setAdapter(friendsRecyclerAdpaterView);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date){

            TextView statusList = mView.findViewById(R.id.status_list);
            statusList.setText(date);
        }

        public void setName(String name){

            TextView displayNameList = mView.findViewById(R.id.name_list);
            displayNameList.setText(name);
        }

        public void setImage(String thumb_image, Context ctx){

            CircleImageView imageList = mView.findViewById(R.id.prof_avatar);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.newest_avatar).into(imageList);
        }

        public void setUserOnline(String online_status) {

            ImageView iconOnline = mView.findViewById(R.id.imageViewOnline);

            if (online_status.equals("true")){

                iconOnline.setVisibility(View.VISIBLE);
            }else{
                iconOnline.setVisibility(View.INVISIBLE);
            }
        }
    }
}
