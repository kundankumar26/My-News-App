package com.example.mynews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //CREATING DATABASE
        sqlDatabase = this.openOrCreateDatabase("news", MODE_PRIVATE, null);

        //CREATING SCHEMA FOR ARTICLES TABLE
        sqlDatabase.execSQL("CREATE TABLE IF NOT EXISTS news (id INTEGER PRIMARY KEY, author VARCHAR, " +
                "title VARCHAR, description VARCHAR, url VARCHAR, urlToImage VARCHAR, publishedAt VARCHAR)");

        //CALL THE NAVIGATION DRAWER
        navigationViewCreate();

        //START THIS METHOD FOR DEFAULT NEWS ITEMS SO NO BLANK PAGE
        startBackgroundProcess("business");
    }

    private void navigationViewCreate() {
        //FIND THE DRAWER LAYOUT ID
        drawerLayout = findViewById(R.id.activity_main);

        //FIND NAVIGATION VIEW ID
        final NavigationView navigationView = findViewById(R.id.nav_menu);

        //HAMBURGER MENU TOGGLE BUTTON
        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.Open, R.string.Close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //TOGGLING BETWEEN HOME AND BACK BUTTON
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TO SHOW THE IMAGES WITH COLOURS ELSE IT GETS SHOWED IN BLACK AND WHITE
        navigationView.setItemIconTintList(null);

        //MARK THE FIRST MENU ITEM SELECTED
        navigationView.getMenu().getItem(0).setChecked(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                int index = 0;
                String category = "";
                switch(id)
                {
                    case R.id.business:
                        category = "business";
                        break;
                    case R.id.entertainment:
                        category = "entertainment"; index = 1; break;
                    case R.id.health:
                        category = "health"; index = 2; break;
                    case R.id.science:
                        category = "science"; index = 3; break;
                    case R.id.sports:
                        category = "sports"; index = 4; break;
                    case R.id.technology:
                        category = "technology"; index = 5; break;
                    default:
                        return true;
                }

                //MARK ALL OTHER MENU ITEM UNSELECTED
                int size = navigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    navigationView.getMenu().getItem(i).setChecked(false);
                }

                //MARK CURRENT MENU ITEM SELECTED
                navigationView.getMenu().getItem(index).setChecked(true);

                //CLOSE THE DARWER WHEN AN ITEM IS SELECTED
                drawerLayout.closeDrawers();

                //CHECK IF INTERNET CONNECTION IS THERE
                if(isNetworkStatusAvailable (getApplicationContext())) {
                    startBackgroundProcess(category);
                } else {
                    //SHOW INTERNET IS NOT AVAILABLE
                    Snackbar.make(findViewById(R.id.snackbar_textView), "Internet is not available", Snackbar.LENGTH_LONG).show();
                }
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

    private void updateRecyclerViewAdapter(){
        recyclerView = findViewById(R.id.news_recyclerview);
        recyclerView.invalidate();
        recyclerViewAdapter = new RecyclerviewAdapter(this, newsList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * THIS METHOD CALLS ALL THE PROCESS FROM WITHIN
     */
    private void startBackgroundProcess(String category) {

        try {
            DownloadTask task = new DownloadTask();
            String newUrl = "http://newsapi.org/v2/top-headlines?country=in&category=" + category + "&apiKey=237f852c2ae8467f857e34660c2a18d8";
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
            }
            while(c.moveToNext());
        }
        c.close();
        updateRecyclerViewAdapter();
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