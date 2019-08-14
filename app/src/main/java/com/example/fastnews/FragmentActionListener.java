package com.example.fastnews;

import android.os.Bundle;

import com.example.Models.Article;

//generic interface for interaction b/w fragment and activity
public interface FragmentActionListener {
    String ACTION_KEY = "action_key";
    int ACTION_VALUE_NEWS_SELECTED = 1;

    String KEY_SELECTED_NEWS = "KEY_SELECTED_NEWS";

    void onActionPerformed(Bundle bundle);

    void saveNewsOffline(Article article);
}
