package com.example.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.Adapter.NewsAdapter;
import com.example.Api.ApiClient;
import com.example.Api.ApiInterface;
import com.example.Models.Article;
import com.example.Models.Constants;
import com.example.Models.NewsResponse;
import com.example.fastnews.FragmentActionListener;
import com.example.fastnews.R;
import com.example.fastnews.Util;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment implements NewsAdapter.OnItemListener {
    public static final String ARG_CATEGORY = "category";

    FragmentActionListener fragmentActionListener;
    View rootView;
    ArrayList<Article> articleStructure = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    NewsAdapter newsAdapter;
    ProgressBar progressBar;
    Button retryBt;
    String category = null;

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_news, container, false);
        progressBar = rootView.findViewById(R.id.trending_progress_bar);
        recyclerView = rootView.findViewById(R.id.news_recycler_view);
        retryBt = rootView.findViewById(R.id.bt_retry_load_news);
        retryBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                retryBt.setVisibility(View.INVISIBLE);
                if (category != null) loadCategoryJSON();
                else loadJSON();
            }
        });

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
            if (category != null) loadCategoryJSON();
            else loadJSON();
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

        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("data", articleStructure);
    }


    private void loadJSON() {
        ApiInterface request = ApiClient.getClient().create(ApiInterface.class);
        Call<NewsResponse> call = request.getTopCountryHeadlines(Util.getPrefCountry(getActivity()), Constants.API_KEY);
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
                retryBt.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "ERROR IN GETTING RESPONSE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategoryJSON() {
        ApiInterface request = ApiClient.getClient().create(ApiInterface.class);
        if (category != null) {
            Call<NewsResponse> call = request.getCategoryHeadlines(category, "in", Constants.API_KEY);
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
                    retryBt.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "ERROR IN GETTING RESPONSE, please retry !", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "ERROR IN Category args", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClickListener(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(FragmentActionListener.KEY_SELECTED_NEWS, articleStructure.get(position));
        if (fragmentActionListener != null) {
            fragmentActionListener.onActionPerformed(bundle);
        }
    }

    @Override
    public void saveNewsOffline(Article article) {
        if (fragmentActionListener != null) {
            Toast.makeText(getActivity(), "saving news offline"
                    + article.getTitle(), Toast.LENGTH_SHORT).show();
            fragmentActionListener.saveNewsOffline(article);
        }
    }

    @Override
    public void deleteNews(Article article) {

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
