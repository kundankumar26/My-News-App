package com.example.mynews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String TAG = "Main activity se";

    //ARRAYLIST STORE THE NEWS OBJECTS
    ArrayList<NewsObject> newsList = new ArrayList<>();

    //CREATE RECYCLERVEIW
    RecyclerView recyclerView;

    //CREATE CUSTOM RECYCLER VEIW ADAPTER
    RecyclerviewAdapter recyclerViewAdapter;

    //CREATE DATABASE
    SQLiteDatabase sqlDatabase;

    //CREATE DRAWER LAYOUT FOR SIDEBAR MENU
    private DrawerLayout drawerLayout;

    //CREATE ACTIONBARTOGGLE FOR LEFT SIDE BUTTON
    private ActionBarDrawerToggle toggle;

    //CREATE SWIPE UP REFRESH LAYOUT
    SwipeRefreshLayout swipeRefreshLayout;

    //NO INTERNET TEXTLABEL
    TextView internetErrorTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        //CREATING DATABASE
        sqlDatabase = this.openOrCreateDatabase("news", MODE_PRIVATE, null);

        //CREATING SCHEMA FOR ARTICLES TABLE
        sqlDatabase.execSQL("CREATE TABLE IF NOT EXISTS news (id INTEGER PRIMARY KEY, author VARCHAR, " +
                "title VARCHAR, description VARCHAR, url VARCHAR, urlToImage VARCHAR, publishedAt VARCHAR)");

        //CALL THE NAVIGATION DRAWER
        navigationViewCreate();

        //START THIS METHOD FOR DEFAULT NEWS ITEMS SO NO BLANK PAGE
        startBackgroundProcess("business");

        //CALL REFRESH ON RECYCLERVIEW
        RefreshRecyclerViewDataset();
    }

    /**
     * METHOD TO REFRESH THE RECYCLERVIEW
     */
    private void RefreshRecyclerViewDataset() {

        //CREATE A HANDLER TO UPDATE THE RECYCLER VIEW AFTER 1 SECOND WITH SWIPE ANIMATION
        swipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDatabase();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }


    /**
     * THIS METHOD CREATE THE NAVIGATION DRAWER IN THE MAIN LAYOUT
     */
    private void navigationViewCreate() {
        //FIND THE INTERNET PROBLEM TEXTVIEW
        internetErrorTextview = findViewById(R.id.nothing_to_show_textview);

        //FIND THE DRAWER LAYOUT ID
        drawerLayout = findViewById(R.id.activity_main);

        //FIND NAVIGATION VIEW ID
        final NavigationView navigationView = findViewById(R.id.nav_menu);

        //HAMBURGER MENU TOGGLE BUTTON
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        //TOGGLING BETWEEN HOME AND BACK BUTTON
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        //TO SHOW THE IMAGES WITH COLOURS ELSE IT GETS SHOWED IN BLACK AND WHITE
        navigationView.setItemIconTintList(null);

        //MARK CURRENT MENU ITEM SELECTED
        SpannableString spanString = new SpannableString(navigationView.getMenu().getItem(0).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark)), 0, spanString.length(), 0);
        navigationView.getMenu().getItem(0).setTitle(spanString);

        //METHOD TO FIND WHICH MENU ITEM WAS SELECTED
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                Log.i(TAG, "clicked in navigation itemselected");

                //CLOSE THE DRAWER WHEN AN ITEM IS SELECTED
                drawerLayout.closeDrawers();

                //CHECK IF INTERNET CONNECTION IS THERE
                if(!isNetworkStatusAvailable (getApplicationContext())) {
                    Snackbar.make(findViewById(R.id.snackbar_textView), "Internet nahi hai, Baad me try kejeye", Snackbar.LENGTH_LONG).show();

                    String internetErrorMessage = "Internet nahi hai";

                    internetErrorTextview.setVisibility(View.VISIBLE);
                    internetErrorTextview.setText(internetErrorMessage);
                    return true;
                }

                //STORE THE MENU ITEM INDEX WHEN ONE IS SELECTED
                String category = "";
                switch(id) {
                    case R.id.business:
                        category = "business";
                        break;
                    case R.id.entertainment:
                        category = "entertainment"; break;
                    case R.id.health:
                        category = "health"; break;
                    case R.id.science:
                        category = "science"; break;
                    case R.id.sports:
                        category = "sports"; break;
                    case R.id.technology:
                        category = "technology"; break;
                    case R.id.about:
                        category = "about"; break;
                    default:
                        return true;
                }
                if(category.equals("about")){
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                    return true;
                }

                //MARK ALL OTHER MENU ITEM UNSELECTED
                int size = navigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    SpannableString spanString = new SpannableString(navigationView.getMenu().getItem(i).getTitle().toString());
                    spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.title_text_color)), 0, spanString.length(), 0);
                    navigationView.getMenu().getItem(i).setTitle(spanString);
                }

                //MARK CURRENT MENU ITEM SELECTED
                SpannableString spanString = new SpannableString(item.getTitle().toString());
                spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark)), 0, spanString.length(), 0);
                item.setTitle(spanString);

                //START SYNCING NEWS FOR CURRENT CATEGORY
                startBackgroundProcess(category);

                //CALL REFRESH ON RECYCLERVIEW WHEN NEW MENU ITEM IS SELECTED
                RefreshRecyclerViewDataset();

                return true;
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

    }

    //TOGGLE BETWEEN HOME AND BACK BUTTON
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }


    /**
     * THIS METHOD CALLS ALL THE PROCESS FROM WITHIN
     */
    private void startBackgroundProcess(String category) {

        try {
            DownloadTask task = new DownloadTask();
            String apiKey = "237f852c2ae8467f857e34660c2a18d8";
            //http://newsapi.org/v2/top-headlines?country=in&category=entertainment&pageSize=100&apiKey=237f852c2ae8467f857e34660c2a18d8
            String newUrl = "https://newsapi.org/v2/top-headlines?country=in&category=" + category + "&pageSize=100&apiKey=" + apiKey;
            task.execute(newUrl);
            updateDatabase();

        } catch (Exception e){
            e.printStackTrace();
        }
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
                //Log.i("database se", c.getString(titleIndex));
            }
            while(c.moveToNext());
        }
        c.close();
        updateRecyclerViewAdapter();
    }


    /**
     * CREATE THE RECYCLERVIEW AND UPDATE THE CONTENTS
     */
    private void updateRecyclerViewAdapter(){
        //FIND THE VIEW FOR EACH ONE
        recyclerView = findViewById(R.id.news_recyclerview);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        recyclerView.invalidate();
        recyclerViewAdapter = new RecyclerviewAdapter(this, newsList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //SHOW THE ITEMS IN VERTICAL LAYOUT AND ONLY 1 ITEM AT A TIME
        //SNAP HELPER CENTER THE ITEM
        SnapHelper snapHelper = new PagerSnapHelper();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(recyclerView);

        //UPDATE THE RECYCLERVIEW IF DATASET IS CHANGED
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDatabase();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
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
                            String newsDescription = newsItemJSONObject.getString("content");
                            String newsUrl = newsItemJSONObject.getString("url");
                            String newsUrlToImage = newsItemJSONObject.getString("urlToImage");
                            String newsPublishedAt = newsItemJSONObject.getString("publishedAt");

                            if(newsDescription.length() < 15){
                                continue;
                            }

                            Log.i(TAG, newsAuthor);

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

            } catch (IOException | JSONException e) {
                String errorMessage = "Kuch problem hua API me, Baad me try kejeyega";
                Snackbar.make(findViewById(R.id.snackbar_textView), errorMessage, Snackbar.LENGTH_LONG).show();

                String internetErrorMessage = "Error accessing Api";
                internetErrorTextview = findViewById(R.id.nothing_to_show_textview);
                internetErrorTextview.setVisibility(View.VISIBLE);
                internetErrorTextview.setText(internetErrorMessage);

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
                httpURLConnection.setReadTimeout(10000 /* milliseconds */);
                httpURLConnection.setConnectTimeout(15000 /* milliseconds */);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                // If the request was successful (response code 200),
                // then read the input stream and parse the response.
                if (httpURLConnection.getResponseCode() == 200) {
                    Log.i("every thin", "everyhtin gifnfien");
                } else {
                    Log.e("LOG_TAG", "Error response code: " + httpURLConnection.getResponseCode());
                }

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