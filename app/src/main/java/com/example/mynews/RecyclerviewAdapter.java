package com.example.mynews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<NewsObject> newsObjectArrayList;

    //DEFAULT CONSTRUCTOR FOR THE CURRENT CLASS
    public RecyclerviewAdapter(Context context, ArrayList<NewsObject> newsList){
        mContext = context;
        newsObjectArrayList = newsList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.news_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final NewsObject currentNewsObject = newsObjectArrayList.get(position);

        //GET IMAGES OF EVERY ARTICLE IN BACKGROUND AND DISPLAY IT
        Glide.with(mContext)
                .load(currentNewsObject.getUrlToImage())
                .placeholder(R.drawable.ic_tv)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.newsImageview);

        //PARSE PUBLISHED DATE TO SHOW ONLY DAY AND MONTH ANS SET IT IN TEXTVIEW
        String newsPublishedDate = parseNewsPublishedDate(currentNewsObject.getPublishedAt());
        holder.newsPublishedDateTextview.setText(newsPublishedDate);

        //PARSE TITLE TO REMOVE NEWSPAPER NAME FROM TITLE AND SET IT
        String newsTitle = parseNewsTitle(currentNewsObject.getTitle());
        holder.newsTitleTextview.setText(newsTitle);

        //DONT SHOW IF AUTHOR IS NULL
        String newsAuthor = ParseNewsAuthor(currentNewsObject.getAuthor());
        holder.newsAuthorTextview.setText(newsAuthor);

        String newsDescription = parseNewsDescription(currentNewsObject.getDescription());
        holder.newsDescriptionTextview.setText(newsDescription);


        holder.newsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CHECK IF INTERNET CONNECTION IS THERE THEN ONLY OPEN INTENT
                if(isNetworkStatusAvailable (mContext.getApplicationContext())) {
                    Intent intent = new Intent(mContext, SecondActivity.class);
                    intent.putExtra("url", newsObjectArrayList.get(position).getUrl());
                    mContext.startActivity(intent);
                } else {
                    //SHOW INTERNET IS NOT AVAILABLE
                    Snackbar.make(v, "Internet nahi hai, Baad me dekhiye", Snackbar.LENGTH_LONG).show();
                }
            }

            //METHOD TO CHECK IF INTERNET IS THERE
            public boolean isNetworkStatusAvailable(Context context) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
                    if(netInfos != null && netInfos.isConnected())
                        return true;
                }
                return false;
            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, newsObjectArrayList.get(position).getUrl());
                //shareIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                mContext.startActivity(Intent.createChooser(shareIntent, "Share File Using!"));

            }

        });

    }



    @Override
    public int getItemCount() {
        return newsObjectArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImageview;
        TextView newsTitleTextview;
        TextView newsAuthorTextview;
        TextView newsPublishedDateTextview;
        LinearLayout newsLinearLayout;
        TextView newsDescriptionTextview;
        ImageButton shareButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            //GET ALL THE TEXTVIEWS AND IMAGEVIEWS
            newsImageview = itemView.findViewById(R.id.news_imageview);
            newsTitleTextview = itemView.findViewById(R.id.news_title_textview);
            newsAuthorTextview = itemView.findViewById(R.id.news_author_textview);
            newsPublishedDateTextview = itemView.findViewById(R.id.news_published_date_textview);
            newsLinearLayout = itemView.findViewById(R.id.news_linear_layout);
            newsDescriptionTextview = itemView.findViewById(R.id.news_description_textview);
            shareButton = itemView.findViewById(R.id.share_button);
        }
    }



    /**
     * VAROIUS METHODS DEFINITIONS
     */
    private String ParseNewsAuthor(String newsAuthorString) {
        int newsAuthorLength = newsAuthorString.length();
        if(newsAuthorLength > 4) {
            int maxLengthOfAuthor = 20;
            if(newsAuthorLength > maxLengthOfAuthor) {
                //maxLengthOfAuthor = newsAuthorString.length();
                return "Author: " + newsAuthorString.substring(0, maxLengthOfAuthor) + "...";
            }
            return "Author: " + newsAuthorString;
        }
        return "";
    }

    private String parseNewsDescription(String description) {
        if(description.length() < 5){
            return "";
        }
        for(int i = description.length() - 1; i >= 0; i--){
            if(description.charAt(i) == '['){
                return description.substring(0, i);
            }
        }
        return description;
    }

    private String parseNewsTitle(String newsTitle) {
        String parsedNewsTitle = "";
        for(int i = newsTitle.length() - 1; i >= 0; i--){
            if(newsTitle.charAt(i) == '-'){
                parsedNewsTitle = newsTitle.substring(0, i);
                break;
            }
        }
        return parsedNewsTitle;
    }

    private String parseNewsPublishedDate(String dateString) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdfParseDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date getParsedDate = sdfParseDate.parse(dateString);

            if(getParsedDate != null){
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdfFormat = new SimpleDateFormat("dd MMM");
                return sdfFormat.format(getParsedDate);
            }
            return "";
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}
