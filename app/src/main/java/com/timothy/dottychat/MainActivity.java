package com.timothy.dottychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar tToolbar;

    private ViewPager mViewPager;
    private DottyTabsAdpater dottyTabsAdpater;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        tToolbar = findViewById(R.id.main_page_appbar);
        setSupportActionBar(tToolbar);
        getSupportActionBar().setTitle("DottyChat");

        mViewPager = findViewById(R.id.view_tab);
        dottyTabsAdpater = new DottyTabsAdpater(getSupportFragmentManager());
        mViewPager.setAdapter(dottyTabsAdpater);

        mTabLayout = findViewById(R.id.main_tablayout);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    //end of onCreate

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){

            sendToStart();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.log_out)
        {

            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if (item.getItemId() == R.id.profile_menu)
        {
            Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settings_intent);
        }

        if (item.getItemId() == R.id.friends)
        {
            Intent all_users_intent = new Intent(MainActivity.this, AllUsers.class);
            startActivity(all_users_intent);
        }
        return true;
    }

    private void sendToStart() {

        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }
}


