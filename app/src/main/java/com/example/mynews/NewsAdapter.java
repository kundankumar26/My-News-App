package com.example.mynews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class NewsAdapter extends ArrayAdapter<NewsObject> {

    ImageView newsImageview;

    public NewsAdapter(@NonNull Context context, @NonNull ArrayList<NewsObject> newsObjects) {
        super(context, 0, newsObjects);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_item, parent, false);
        }
        View listview = convertView;

        NewsObject currentNewsObject = getItem(position);
        if(currentNewsObject == null){
            return listview;
        }

        //GET ALL THE TEXTVIEWS AND IMAGEVIEWS
        newsImageview = listview.findViewById(R.id.news_imageview);
        TextView newsTitleTextview = listview.findViewById(R.id.news_title_textview);
        TextView newsAuthorTextview = listview.findViewById(R.id.news_author_textview);
        TextView newsPublishedDateTextview = listview.findViewById(R.id.news_published_date_textview);

        //GET IMAGES OF EVERY ARTICLE IN BACKGROUND AND DISPLAY IT
        ImageTask imageTask = new ImageTask();
        imageTask.execute(currentNewsObject.getUrlToImage());

        //PARSE PUBLISHED DATE TO SHOW ONLY DAY AND MONTH ANS SET IT IN TEXTVIEW
        String newsPublishedDate = parseNewsPublishedDate(currentNewsObject.getPublishedAt());
        newsPublishedDateTextview.setText(newsPublishedDate);

        //PARSE TITLE TO REMOVE NEWSPAPER NAME FROM TITLE AND SET IT
        String newsTitle = parseNewsTitle(currentNewsObject.getTitle());
        newsTitleTextview.setText(newsTitle);

        //DONT SHOW IF AUTHOR IS NULL
        String newsAuthor = ParseNewsAuthor(currentNewsObject.getAuthor());
        newsAuthorTextview.setText(newsAuthor);

        return listview;
    }

    private String ParseNewsAuthor(String newsAuthorString) {
        int newsAuthorLength = newsAuthorString.length();
        if(newsAuthorLength > 4) {
            int maxLengthOfAuthor = 18;
            if(newsAuthorLength > maxLengthOfAuthor) {
                maxLengthOfAuthor = newsAuthorString.length();
                return "Author: " + newsAuthorString.substring(0, maxLengthOfAuthor) + "...";
            }
            return "Author: " + newsAuthorString;
        }
        return "";
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

    public class ImageTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            URL url = null;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            newsImageview.setImageBitmap(bitmap);
            super.onPostExecute(bitmap);
        }
    }
}
