package com.example.fastnews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.Models.Article;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {
    private List<Article> articles;
    private Context context;
    private OnItemListener onItemListener;

    public NewsAdapter(ArrayList<Article> articleArrayList, Context context) {
        this.articles = articleArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_news_item, viewGroup, false);

        return new MyViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final MyViewHolder holder = myViewHolder;

        Article article = articles.get(i);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(Util.getRandomDrawbleColor());
        requestOptions.error(Util.getRandomDrawbleColor());
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.centerCrop();

        Glide.with(context)
                .load(article.getUrlToImage())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView);

        holder.title.setText(article.getTitle());
        holder.author.setText(article.getAuthor());
        holder.desc.setText(article.getDescription());
        holder.source.setText(article.getSource().getName());
        holder.time.setText(Util.getFormattedDate(article.getPublishedAt()));
        if (article.isSaved()) {
//            holder.saveImageBtn.setVisibility(View.GONE);
            holder.saveImageBtn.setImageResource(R.drawable.ic_delete_forever_black_24dp);
        }

        holder.saveImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (article.isSaved())
                    onItemListener.deleteNews(article);
                else
                    onItemListener.saveNewsOffline(article);
            }
        });

    }

    public void setOnItemClickListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public interface OnItemListener {
        void onItemClickListener(View view, int position);

        void saveNewsOffline(Article article);

        void deleteNews(Article article);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        OnItemListener onItemListener;
        TextView title, desc, author, published_ad, source, time;
        ImageView imageView;
        ProgressBar progressBar;
        ImageButton saveImageBtn;

        public MyViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.single_item_title);
            desc = itemView.findViewById(R.id.single_item_description);
            author = itemView.findViewById(R.id.author);
            source = itemView.findViewById(R.id.source);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.single_items_news_image);
            progressBar = itemView.findViewById(R.id.prograss_load_photo);
            saveImageBtn = itemView.findViewById(R.id.save_img_btn_single_item);

            this.onItemListener = onItemListener;
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClickListener(v, getAdapterPosition());
        }
    }
}
