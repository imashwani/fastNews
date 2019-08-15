package com.example.Fragment;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Adapter.NewsAdapter;
import com.example.Models.Article;
import com.example.RoomDb.AppExecutors;
import com.example.RoomDb.ArticlesDatabase;
import com.example.fastnews.FragmentActionListener;
import com.example.fastnews.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SavedArticlesFragment extends Fragment implements NewsAdapter.OnItemListener {

    private View rootView;
    private LifecycleOwner context;
    private ArticlesDatabase articlesDatabase;
    private LiveData<List<Article>> articlesLiveData;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private NewsAdapter newsAdapter;
    private ArrayList<Article> articles;
    private FragmentActionListener fragmentActionListener = null;

    public SavedArticlesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_saved_articles, container, false);

        articlesDatabase = ArticlesDatabase.getInstance(getActivity().getApplicationContext());

        recyclerView = rootView.findViewById(R.id.saved_news_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        articles = new ArrayList<>();

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = this;
        getSavedArticle();
    }

    private void setData(ArrayList<Article> articles) {
        newsAdapter = new NewsAdapter(articles, getContext());
        recyclerView.setAdapter(newsAdapter);
        newsAdapter.notifyDataSetChanged();
        newsAdapter.setOnItemClickListener(this);
    }

    void getSavedArticle() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                articlesLiveData = articlesDatabase.articlesDao().getArticleList();

                articlesLiveData.observe(context, new Observer<List<Article>>() {
                    @Override
                    public void onChanged(@Nullable List<Article> articlesLocal) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                articles = (ArrayList<Article>) articlesLocal;
                                //reversing the list so that the recently saved article comes at top
                                Collections.reverse(articles);
                                setData(articles);
                            }
                        });
                    }
                });

            }
        });
    }


    @Override
    public void onItemClickListener(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(FragmentActionListener.KEY_SELECTED_NEWS, articles.get(position));
        if (fragmentActionListener != null) {
            fragmentActionListener.onActionPerformed(bundle);
        }
    }

    @Override
    public void saveNewsOffline(Article article) {
    }

    @Override
    public void deleteNews(Article article) {
        fragmentActionListener.deleteNewsFromDb(article);
    }

    public void setFragmentActionListener(FragmentActionListener fal) {
        fragmentActionListener = fal;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        fragmentActionListener = null;
    }
}
