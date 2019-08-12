package com.example.Fragment;


import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.Models.Article;
import com.example.fastnews.FragmentActionListener;
import com.example.fastnews.R;
import com.example.fastnews.Util;

import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsWebViewFragment extends Fragment {

    View rootView;
    Article article = null;

    private ImageView newsImageIV;
    private TextView date, timeTV, titleTV;


    public NewsWebViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_news_web_view, container, false);
        Bundle bundle = getArguments();
        article = (Article) bundle.getSerializable(FragmentActionListener.KEY_SELECTED_NEWS);

        initWebView(article.getUrl());
        initView();

        return rootView;
    }
    private void initView(){
        newsImageIV = rootView.findViewById(R.id.backdrop);
        timeTV = rootView.findViewById(R.id.tv_time_webview);
        titleTV = rootView.findViewById(R.id.tv_title_webview);


        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(Util.getRandomDrawbleColor());

        Glide.with(this)
                .load(article.getUrlToImage())
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(newsImageIV);
        timeTV.setText(Util.getFormattedDate(article.getPublishedAt()));
        titleTV.setText(article.getTitle());
    }

    private void initWebView(String url){
        WebView webView = rootView.findViewById(R.id.webView);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }

}
