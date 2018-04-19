package com.timothy.dottychat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chat_user;
    private String mCurrentUserId;
    private final List<Messages> messagesChat = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    private Toolbar tToolbar;
    private TextView dispName;
    private TextView lastSeen;
    private CircleImageView imageView;
    private ImageButton chatAddBtn;
    private ImageButton sendChatBtn;
    private EditText chatMessage;
    private RecyclerView messageRecycler;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private  MessageAdapter mAdapter;

    private int itemPos = 0;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private static final int GALLERY_PICK = 1;

    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tToolbar = findViewById(R.id.chat_bar);
        setSupportActionBar(tToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        chat_user = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        //---------------custom bar items-------------------
        dispName = findViewById(R.id.bar_display_name);
        lastSeen =findViewById(R.id.last_seen);
        imageView = findViewById(R.id.custom_bar_image);

        chatAddBtn = findViewById(R.id.add_button);
        sendChatBtn = findViewById(R.id.send_chat);
        chatMessage = findViewById(R.id.enter_chat_message);

        mAdapter = new MessageAdapter(messagesChat);

        messageRecycler = findViewById(R.id.message_list);
        linearLayoutManager = new LinearLayoutManager(this);
        messageRecycler.setHasFixedSize(true);
        messageRecycler.setLayoutManager(linearLayoutManager);
        messageRecycler.setAdapter(mAdapter);

        loadMessages();

        dispName.setText(userName);

        //----------------value for last seen-----------------------
        mRootRef.child("dottyUsers").child(chat_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true"))
                {
                    lastSeen.setText("Online");
                }else{

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    lastSeen.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //----------------values for the chat and create maps for the chat--------------------------
        mRootRef.child("chats").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(chat_user))
                {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chats/" + mCurrentUserId + "/" + chat_user, chatAddMap);
                    chatUserMap.put("chats/" + chat_user + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            //do nothing
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //----------------on click listener for send message button---------------------------------
        sendChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });

        //----------------on click listener for add buttton-----------------------------------------
        chatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(chat_user);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesChat.add(message);
                mAdapter.notifyDataSetChanged();
                messageRecycler.scrollToPosition(messagesChat.size() - 1);

               // mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = chatMessage.getText().toString();

        if (!TextUtils.isEmpty(message))
        {
            String current_user_ref = "messages/" + mCurrentUserId + "/" + chat_user;
            String chat_user_ref = "messages/" + chat_user + "/" + mCurrentUserId;

            DatabaseReference userMessageId= mRootRef.child("messages")
                    .child(mCurrentUserId).child(chat_user).push();

            String msgId = userMessageId.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map userMessageMap = new HashMap();
            userMessageMap.put(current_user_ref + "/" + msgId, messageMap);
            userMessageMap.put(chat_user_ref + "/" + msgId, messageMap);

            chatMessage.setText("");

            mRootRef.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                }
            });
        }
    }
}
