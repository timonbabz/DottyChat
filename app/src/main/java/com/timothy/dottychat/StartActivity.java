package com.timothy.dottychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button btnNewAccount;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnLogin = findViewById(R.id.log_in);
        btnNewAccount = findViewById(R.id.new_user);
        btnNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent create_account = new Intent(StartActivity.this, CreateAccount.class);
                startActivity(create_account);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(login_intent);
            }
        });
    }
}
