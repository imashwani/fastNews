package com.example.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.Api.ApiClient;
import com.example.Api.ApiInterface;
import com.example.Models.Article;
import com.example.Models.Constants;
import com.example.Models.NewsResponse;
import com.example.fastnews.FragmentActionListener;
import com.example.fastnews.NewsAdapter;
import com.example.fastnews.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrendingFragment extends Fragment implements NewsAdapter.OnItemListener {
    FragmentActionListener fragmentActionListener;
    View rootView;
    ArrayList<Article> articleStructure = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    NewsAdapter newsAdapter;
    ProgressBar progressBar;

    public TrendingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_trending, container, false);
        progressBar = rootView.findViewById(R.id.trending_progress_bar);
        recyclerView = rootView.findViewById(R.id.news_recycler_view);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            loadJSON();
        } else {
            articleStructure = (ArrayList<Article>) savedInstanceState.getSerializable("data");
            setData();
        }
    }

    private void setData() {
        newsAdapter = new NewsAdapter(articleStructure, getContext());
        recyclerView.setAdapter(newsAdapter);
        newsAdapter.notifyDataSetChanged();
        newsAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("data", articleStructure);

    }

    private void loadJSON() {
        ApiInterface request = ApiClient.getClient().create(ApiInterface.class);

        Call<NewsResponse> call = request.getTopHeadlines("in", Constants.API_KEY);
        call.enqueue(new Callback<NewsResponse>() {

            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {

                if (response.isSuccessful() && response.body().getArticles() != null) {

                    if (!articleStructure.isEmpty()) {
                        articleStructure.clear();
                    }
                    articleStructure = response.body().getArticles();
                    setData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "ERROR IN GETTING RESPONSE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClickListener(View view, int position) {
//        Toast.makeText(getContext(), "clicked " + (position), Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putSerializable(FragmentActionListener.KEY_SELECTED_NEWS, articleStructure.get(position));
        if (fragmentActionListener != null) {
            fragmentActionListener.onActionPerformed(bundle);
        }
    }

    public void setFragmentActionListener(FragmentActionListener fal) {
        fragmentActionListener = fal;
    }
}
