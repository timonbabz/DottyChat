package com.timothy.dottychat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar tToolbar;
    private TextInputLayout statusInput;
    private Button btnUpdate;
    private ProgressDialog mProgress;

    private DatabaseReference mStatusDb;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = currentUser.getUid();
        mStatusDb = FirebaseDatabase.getInstance().getReference().child("dottyUsers").child(user_id);

        btnUpdate = findViewById(R.id.btn_updatestatus);
        statusInput = findViewById(R.id.status_update);
        mProgress = new ProgressDialog(this);

        tToolbar = findViewById(R.id.status_activity_bar);
        setSupportActionBar(tToolbar);
        getSupportActionBar().setTitle("DottyChat: Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        statusInput.getEditText().setText(status_value);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress.setTitle("Updating status");
                mProgress.show();

                String status = statusInput.getEditText().getText().toString().trim();
                mStatusDb.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            mProgress.dismiss();
                            Toast.makeText(StatusActivity.this, "Status updated successfully", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
