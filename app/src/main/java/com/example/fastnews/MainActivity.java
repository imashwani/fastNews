package com.example.fastnews;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.Fragment.LocalNewsFragment;
import com.example.Fragment.NewsFragment;
import com.example.Fragment.NewsWebViewFragment;
import com.example.Fragment.SavedArticlesFragment;
import com.example.Fragment.SearchFragment;
import com.example.Models.Article;
import com.example.RoomDb.AppExecutors;
import com.example.RoomDb.ArticlesDatabase;
import com.example.Worker.RepeatTopNewsNotifWork;

import java.io.IOException;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements FragmentActionListener {

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment active;
    private NewsFragment newsFragment;
    private LocalNewsFragment localNewsFragment;
    private NewsWebViewFragment newsWebViewFragment;
    private SavedArticlesFragment savedArticlesFragment;
    private SearchFragment searchFragment = null;
    private Location location;
    private double describeContents;
    private Geocoder geocoder;
    private String countryCode = "";
    private ArticlesDatabase articlesDatabase;
    private SharedPreferences sharedPreferences;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (fragmentManager.findFragmentById(R.id.mainFragmentContainer) instanceof NewsWebViewFragment) {
                fragmentManager.popBackStack();
            }
            switch (item.getItemId()) {
                case R.id.trending:
                    fragmentManager.beginTransaction().hide(active).show(newsFragment).commit();
                    active = newsFragment;
                    return true;

                case R.id.navigation_local_news:
                    fragmentManager.beginTransaction().hide(active).show(localNewsFragment).commit();
                    active = localNewsFragment;
                    return true;

                case R.id.navigation_notifications:
                    fragmentManager.beginTransaction().hide(active).show(savedArticlesFragment).commit();
                    active = savedArticlesFragment;
                    return true;
                case R.id.navigation_search:
                    if (searchFragment == null) {
                        searchFragment = new SearchFragment();
                        searchFragment.setFragmentActionListener(MainActivity.this);
                        fragmentManager.beginTransaction().hide(active)
                                .add(R.id.mainFragmentContainer, searchFragment)
                                .show(searchFragment).commit();

                    } else {
                        fragmentManager.beginTransaction().hide(active).show(searchFragment).commit();
                    }
                    active = searchFragment;
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        newsFragment = new NewsFragment();
        newsFragment.setFragmentActionListener(this);
        localNewsFragment = new LocalNewsFragment();
        localNewsFragment.setActivityContext(this);
        savedArticlesFragment = new SavedArticlesFragment();
        savedArticlesFragment.setFragmentActionListener(this);
//        searchFragment = new SearchFragment();
        active = newsFragment;

        fragmentManager = this.getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.mainFragmentContainer) == null) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.mainFragmentContainer, localNewsFragment).hide(localNewsFragment);
            fragmentTransaction.add(R.id.mainFragmentContainer, savedArticlesFragment).hide(savedArticlesFragment);
            fragmentTransaction.add(R.id.mainFragmentContainer, newsFragment);
            fragmentTransaction.commit();
        }

        articlesDatabase = ArticlesDatabase.getInstance(getApplicationContext());
        setupNotification();

        sharedPreferences = getApplicationContext().getSharedPreferences("fastnews", MODE_PRIVATE);
        countryCode = sharedPreferences.getString(Util.COUNTRY, null);

        if (countryCode == null || countryCode.length() == 0) {
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            }
            location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            geocoder = new Geocoder(this);
        }
    }


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

    @Override
    public void saveNewsOffline(Article article) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d("sql", "run: inserting the data: " + article.getTitle());
                try {
                    article.setSaved(true);
                    articlesDatabase.articlesDao().insertArticle(article);
                } catch (SQLiteConstraintException sqlConstExct) {
                    sqlConstExct.printStackTrace();
                    Log.d("sql", "run: data already present" + article.getTitle());
                }
            }
        });
    }

    @Override
    public void deleteNewsFromDb(Article article) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d("sql", "run: deleting the data: " + article.getTitle());
                try {
                    articlesDatabase.articlesDao().deleteArticle(article);
                } catch (SQLiteConstraintException sqlConstExct) {
                    sqlConstExct.printStackTrace();
                }
            }
        });
    }

    void setupNotification() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        dueDate.set(Calendar.HOUR, 8);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        OneTimeWorkRequest oneTimeWorkRequest
                = new OneTimeWorkRequest
                .Builder(RepeatTopNewsNotifWork.class)
                .setConstraints(constraints)
//                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);


        Log.d("MainActivity", "setupNotification: Setting up Notification");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        countryCode = geocoder.getFromLocation(location.getLatitude(),
                                location.getLongitude(), 10).get(0).getCountryCode();
                        updateCountryPref();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            countryCode = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 10).get(0).getCountryCode();
            updateCountryPref();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateCountryPref() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Util.COUNTRY, countryCode.toLowerCase());
        editor.commit();
    }

}
