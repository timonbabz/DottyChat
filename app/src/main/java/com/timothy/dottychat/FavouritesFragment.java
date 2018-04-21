package com.timothy.dottychat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouritesFragment extends Fragment {

    private RecyclerView mFavouritesList;

    private View mainView;
    private DatabaseReference mfavouritesDatabase;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private DatabaseReference mUsersDatabase;

    public FavouritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_request, container, false);

        mFavouritesList = mainView.findViewById(R.id.favourites_list);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        mfavouritesDatabase = FirebaseDatabase.getInstance().getReference().child("favourites").child(current_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("dottyUsers");
        mUsersDatabase.keepSynced(true);

        mFavouritesList.setHasFixedSize(true);
        mFavouritesList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

}
