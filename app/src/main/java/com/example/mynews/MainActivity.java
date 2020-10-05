package com.example.mynews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //ARRAYLIST STORE THE NEWS OBJECTS
    ArrayList<NewsObject> newsList = new ArrayList<>();

    //CREATE ADAPTER TO VIEW NEWS OBJECTS
    NewsAdapter newsAdapter;

    //CREATE DATABASE
    SQLiteDatabase sqlDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //CREATING DATABASE
        sqlDatabase = this.openOrCreateDatabase("news", MODE_PRIVATE, null);

        //CREATING SCHEMA FOR ARTICLES TABLE
        sqlDatabase.execSQL("CREATE TABLE IF NOT EXISTS news (id INTEGER PRIMARY KEY, author VARCHAR, " +
                "title VARCHAR, description VARCHAR, url VARCHAR, urlToImage VARCHAR, publishedAt VARCHAR)");

        startBackgroundProcess();

    }


    /**
     * THIS METHOD CALLS ALL THE PROCESS FROM WITHIN
     */
    private void startBackgroundProcess() {

        try {
            DownloadTask task = new DownloadTask();
            task.execute("http://newsapi.org/v2/top-headlines?country=in&category=entertainment&apiKey=237f852c2ae8467f857e34660c2a18d8");
            updateDatabase();
        } catch (Exception e){
            e.printStackTrace();
        }

        ListView listView = findViewById(R.id.news_listview);
        newsAdapter = new NewsAdapter(this, newsList);
        listView.setAdapter(newsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //CREATE AN INTENT TO VIEW THE WEB PAGE IN SECOND ACTIVITY
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                intent.putExtra("url", newsList.get(position).getUrl());
                startActivity(intent);
            }
        });

    }


    /**
     * THIS METHOD UPDATES THE DATABASE FROM THE NEW NEWS
     */
    private void updateDatabase() {
        //CREATE A CURSOR TO RETRIEVE THE DATA FROM THE DATABASE ONE BY ONE
        Cursor c = sqlDatabase.rawQuery("SELECT * FROM news", null);
        int authorIndex = c.getColumnIndex("author");
        int titleIndex = c.getColumnIndex("title");
        int descriptionIndex = c.getColumnIndex("description");
        int urlIndex = c.getColumnIndex("url");
        int urlToImageIndex = c.getColumnIndex("urlToImage");
        int publishedAtIndex = c.getColumnIndex("publishedAt");

        if(c.moveToFirst()){
            newsList.clear();
            do {
                //ADD NEWS OBJECTS IN THE NEWS ARRAY LIST
                newsList.add(new NewsObject(c.getString(authorIndex), c.getString(titleIndex),
                        c.getString(descriptionIndex), c.getString(urlIndex),
                        c.getString(urlToImageIndex), c.getString(publishedAtIndex)));
            }
            while(c.moveToNext());
        }
        c.close();
    }


    /**
     * THIS CLASS RUNS THE PROCESS IN BACKGROUND
     */
    public class DownloadTask extends AsyncTask<String, Void, NewsObject>{

        @Override
        protected NewsObject doInBackground(String... urls) {

            //GET THE JSON STRING FROM GETJSONSTRING METHOD
            try {
                String jsonString = getJsonString(urls[0]);
                assert jsonString != null;
                JSONObject newsJSONObject = new JSONObject(jsonString);

                if(newsJSONObject.getString("status").equals("ok")){
                    sqlDatabase.execSQL("DELETE FROM news");
                    JSONArray newsJSONArray = new JSONArray(newsJSONObject.getString("articles"));
                    ArrayList<NewsObject> newsItemsList = new ArrayList<>();

                    for(int i = 0; i < newsJSONArray.length(); i++){
                        try {
                            JSONObject newsItemJSONObject = newsJSONArray.getJSONObject(i);

                            //GET THE VALUES OF THE REQUIRED FIELD
                            String newsAuthor = newsItemJSONObject.getString("author");
                            String newsTitle = newsItemJSONObject.getString("title");
                            String newsDescription = newsItemJSONObject.getString("description");
                            String newsUrl = newsItemJSONObject.getString("url");
                            String newsUrlToImage = newsItemJSONObject.getString("urlToImage");
                            String newsPublishedAt = newsItemJSONObject.getString("publishedAt");

                            //INSERT VALUES IN THE DATABASE
                            SQLiteStatement statement = sqlDatabase.compileStatement("INSERT INTO news(author, title, description, url, urlToImage, publishedAt) " +
                                    "VALUES(?, ?, ?, ?, ?, ?)");
                            statement.bindString(1, newsAuthor);
                            statement.bindString(2, newsTitle);
                            statement.bindString(3, newsDescription);
                            statement.bindString(4, newsUrl);
                            statement.bindString(5, newsUrlToImage);
                            statement.bindString(6, newsPublishedAt);
                            statement.execute();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    TextView titleForText = findViewById(R.id.news_title_textview);
                    titleForText.setText("");
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(NewsObject newsObject) {
            super.onPostExecute(newsObject);
        }

        /**
         * THIS METHOD GETS THE DATA FROM URL AND RETURN IT
         */
        private String getJsonString(String urls) throws IOException {
            URL url = null;
            HttpURLConnection httpURLConnection = null;

            //SETTING THE CONNECTION AND GETTING DATA
            try {
                url = new URL(urls);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                //STRINGBUILDER TO CREATE THE FINAL STRING
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}