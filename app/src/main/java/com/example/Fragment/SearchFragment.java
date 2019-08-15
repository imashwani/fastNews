package com.example.Fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.Api.ApiClient;
import com.example.Api.ApiInterface;
import com.example.Models.Article;
import com.example.Models.Constants;
import com.example.Models.NewsResponse;
import com.example.fastnews.FragmentActionListener;
import com.example.fastnews.NewsAdapter;
import com.example.fastnews.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements NewsAdapter.OnItemListener {

    private View rootView;
    private SearchView searchView;
    private List<Article> articles;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private NewsAdapter newsAdapter;
    private FragmentActionListener fragmentActionListener;
    private ProgressBar progressBar;
    private TextView resultTextView;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        searchView = rootView.findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        progressBar = rootView.findViewById(R.id.progress_bar_search_rv);
        resultTextView = rootView.findViewById(R.id.result_search_tv);
        resultTextView.setText("");

        recyclerView = rootView.findViewById(R.id.search_recycler_view);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        articles = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                resultTextView.setText("");
                progressBar.setVisibility(View.VISIBLE);
                loadSearchJSON(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.requestFocus();
    }


    private void loadSearchJSON(String query) {
        ApiInterface request = ApiClient.getClient().create(ApiInterface.class);

        Call<NewsResponse> call = request.getSearchResults(query, "en", Constants.API_KEY);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {

                if (response.isSuccessful() && response.body().getArticles() != null) {
                    if (!articles.isEmpty()) {
                        articles.clear();
                    }
                    articles = response.body().getArticles();
                    if (articles.size() == 0) {
                        resultTextView.setText(getResources().getString(R.string.no_result_found));
                    }
                    setData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                resultTextView.setText("Error In Getting response");
            }
        });
    }

    private void setData() {
        newsAdapter = new NewsAdapter((ArrayList<Article>) articles, getContext());
        recyclerView.setAdapter(newsAdapter);
        newsAdapter.notifyDataSetChanged();
        newsAdapter.setOnItemClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);
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
        if (fragmentActionListener != null) {
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
