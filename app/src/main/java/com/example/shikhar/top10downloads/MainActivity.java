package com.example.shikhar.top10downloads;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listapps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    int feedLimit = 10;
    private String feedCachedUrl = "INVALIDATED";
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listapps = (ListView)findViewById(R.id.xmlListView);

        if(savedInstanceState != null){
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }
        downloadUrl(String.format(feedUrl,feedLimit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu,menu);
        if(feedLimit==10){
            menu.findItem(R.id.mnu10).setChecked(true);
        }else{
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.mnufree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.mnupaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.songs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnu10:
            case R.id.mnu25:
                if(!item.isChecked()){
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                }
                break;
            case R.id.refresh:
                feedCachedUrl = "INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(String.format(feedUrl,feedLimit));
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL,feedUrl);
        outState.putInt(STATE_LIMIT,feedLimit);
        super.onSaveInstanceState(outState);
    }


    private void downloadUrl(String feedUrl){
        if(!feedUrl.equalsIgnoreCase(feedCachedUrl)){
            Log.d(TAG, "downloadUrl: started");
            DownloadData downloaddata = new DownloadData();
            downloaddata.execute(feedUrl);
            feedUrl = feedCachedUrl;
            Log.d(TAG, "downloadUrl: ended");
        }else{
            Log.d(TAG, "downloadUrl: url not changed");
        }

    }

    private class DownloadData extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground: started with " + params[0]);
            String rssfeed = downloadXML(params[0]);
            if(rssfeed==null){
                Log.e(TAG, "doInBackground: error is rssfeed "+ rssfeed );
            }
            return rssfeed;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute: started with " + s);
            super.onPostExecute(s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(
//                    MainActivity.this,R.layout.textview,parseApplications.getAppplications());
//            listapps.setAdapter(arrayAdapter);
            FeedAdapter<FeedEntry> feedAdapter = new FeedAdapter<>(MainActivity.this,R.layout.list_record,parseApplications.getAppplications());
            listapps.setAdapter((feedAdapter));
            Log.d(TAG, "onPostExecute: ended");
        }
        private String downloadXML(String urlpath){
            StringBuilder xmlresult = new StringBuilder();

            try{
                URL url = new URL(urlpath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
//                InputStream inputstream = (InputStream)connection.getInputStream();
//                InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
//                BufferedReader reader = new BufferedReader(inputstreamreader);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                int charsRead;
                char[] inputbuffer = new char[500];
                while(true){
                    charsRead = reader.read(inputbuffer);
                    if(charsRead<0){
                        break;
                    }
                    if(charsRead>0){
                        xmlresult.append(String.copyValueOf(inputbuffer,0,charsRead));
                    }
                }
                reader.close();
            return xmlresult.toString();
            }catch(MalformedURLException m){
                Log.e(TAG, "downloadXML: url not found "+m.getMessage() );
            }catch(IOException e){
                Log.e(TAG, "downloadXML: io error" + e.getMessage() );
            }
            return null;
        }
    }
}
