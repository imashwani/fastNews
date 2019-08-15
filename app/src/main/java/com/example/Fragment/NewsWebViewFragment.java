package com.example.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.Models.Article;
import com.example.fastnews.FragmentActionListener;
import com.example.fastnews.R;
import com.example.fastnews.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsWebViewFragment extends Fragment {

    private View rootView;
    private Article article = null;

    private ImageView newsImageIV;
    private TextView timeTV, titleTV, descriptionTv;
    private ImageButton shareBt;


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

    private void initView() {
        newsImageIV = rootView.findViewById(R.id.backdrop);
        timeTV = rootView.findViewById(R.id.tv_time_webview);
        titleTV = rootView.findViewById(R.id.tv_title_webview);
        descriptionTv = rootView.findViewById(R.id.web_view_description);
        shareBt = rootView.findViewById(R.id.share_imgbtn);

        shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = article.getTitle() + "\n\n" + article.getDescription() + "\n\n" + article.getUrl();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sharing News from fastNEWS app");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "FastNEWS, share the News"));
            }
        });

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(Util.getRandomDrawbleColor());

        Glide.with(this)
                .load(article.getUrlToImage())
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(newsImageIV);
        timeTV.setText(Util.getFormattedDate(article.getPublishedAt()));
        titleTV.setText(article.getTitle());
        descriptionTv.setText(article.getDescription());
    }

    private void initWebView(String url) {
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
