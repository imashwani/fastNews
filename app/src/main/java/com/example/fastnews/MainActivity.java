package com.example.fastnews;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.Fragment.LocalNewsFragment;
import com.example.Fragment.NewsWebViewFragment;
import com.example.Fragment.TrendingFragment;

public class MainActivity extends AppCompatActivity implements FragmentActionListener {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment active;
    TrendingFragment trendingFragment;
    LocalNewsFragment localNewsFragment;
    NewsWebViewFragment newsWebViewFragment;

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        trendingFragment = new TrendingFragment();
        trendingFragment.setFragmentActionListener(this);
        localNewsFragment = new LocalNewsFragment();
        active = trendingFragment;

        fragmentManager = this.getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.mainFragmentContainer) == null) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.mainFragmentContainer, localNewsFragment).hide(localNewsFragment);
            fragmentTransaction.add(R.id.mainFragmentContainer, trendingFragment);
            fragmentTransaction.commit();
        }
    }

    private void showTrending() {
        fragmentTransaction = fragmentManager.beginTransaction();
        trendingFragment = new TrendingFragment();
        fragmentTransaction.replace(R.id.mainFragmentContainer, trendingFragment);
        fragmentTransaction.addToBackStack(this.getClass().getName());
        fragmentTransaction.commit();
    }

    private void showLocalNews() {
        fragmentTransaction = fragmentManager.beginTransaction();
        localNewsFragment = new LocalNewsFragment();
        fragmentTransaction.replace(R.id.mainFragmentContainer, localNewsFragment);
        fragmentTransaction.addToBackStack(this.getClass().getName());
        fragmentTransaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.trending:
                    fragmentManager.beginTransaction().hide(active).show(trendingFragment).commit();
                    active = trendingFragment;
                    return true;

                case R.id.navigation_local_news:
                    fragmentManager.beginTransaction().hide(active).show(localNewsFragment).commit();
                    active = localNewsFragment;
                    return true;

                case R.id.navigation_notifications:

                    return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        if (fragmentManager.findFragmentById(R.id.mainFragmentContainer) instanceof NewsWebViewFragment) {
            // Removing web view frag. from back stack of fragment
            fragmentManager.popBackStack();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Fast News App")
                    .setMessage("Do you really want to Exit?")
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void showWebViewFragment(Bundle bundle) {
        fragmentTransaction = fragmentManager.beginTransaction();
        newsWebViewFragment = new NewsWebViewFragment();
        newsWebViewFragment.setArguments(bundle);

        fragmentTransaction.add(R.id.mainFragmentContainer, newsWebViewFragment);
        fragmentTransaction.addToBackStack(this.getClass().getName());
        fragmentTransaction.commit();

    }

    @Override
    public void onActionPerformed(Bundle bundle) {
        showWebViewFragment(bundle);
    }


}
