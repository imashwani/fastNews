package com.example.fastnews;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.Fragment.LocalNewsFragment;
import com.example.Fragment.NewsFragment;
import com.example.Fragment.NewsWebViewFragment;
import com.example.Fragment.SavedArticlesFragment;
import com.example.Models.Article;
import com.example.RoomDb.AppExecutors;
import com.example.RoomDb.ArticlesDatabase;


public class MainActivity extends AppCompatActivity implements FragmentActionListener {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment active;
    NewsFragment newsFragment;
    LocalNewsFragment localNewsFragment;
    NewsWebViewFragment newsWebViewFragment;
    SavedArticlesFragment savedArticlesFragment;
    private ArticlesDatabase articlesDatabase;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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


}
