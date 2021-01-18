package com.nickinfinity.toprssfeeddownloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listapps;
    String feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    int feedLimit = 10;
    public String feedCachedURL = "INVALIDATED";
    public static final String STATE_URL = "feedURL";
    public static final String STATE_LIMIT = "feedLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listapps = findViewById(R.id.xmlListView);

        if(savedInstanceState != null){
            feedURL = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }

        // entry point for downloading xml
        downloadURL(String.format(feedURL,feedLimit));
    }


    // Menu Code

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu,menu);
        if(feedLimit==10)
        {
            menu.findItem(R.id.menu10).setChecked(true);
        }
        else if(feedLimit== 25){
            menu.findItem(R.id.menu25).setChecked(true);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menuFree:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menuPaid:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.menuSongs:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.menu10:
            case R.id.menu25:
                if(!item.isChecked())
                {
                    item.setChecked(true);
                    feedLimit = 35-feedLimit;
                    Log.d(TAG, "onOptionsItemSelected:"+item.getTitle()+"changing feed limit to "+feedLimit);
                }else{
                    Log.d(TAG, "onOptionsItemSelected: "+item.getTitle()+"feed limit is unchanged"+feedLimit);
                }
                break;
            case R.id.menuRefresh:
                feedCachedURL="INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        downloadURL(String.format(feedURL,feedLimit));
        return true;

    }


    // Saving Data between State change

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL,feedURL);
        outState.putInt(STATE_LIMIT,feedLimit);
        super.onSaveInstanceState(outState);
    }

    public void downloadURL(String feedURL){
        if(!feedURL.equalsIgnoreCase(feedCachedURL)) {
            Log.d(TAG, "downloadURL: starting Async Task");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedURL);
            feedCachedURL=feedURL;
            Log.d(TAG, "downloadURL: async task is done");
        } else{
            Log.d(TAG, "downloadURL: Feed url not changed");
        }
    }

    // Downloaddata class for async downloading of xml files
    private class  DownloadData extends AsyncTask<String,Void,String> //url format, no progressbar, output is string
    {
        private static final String TAG = "DownloadData";
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is" + s);
            ParseApplications parseApplications = new ParseApplications();

            //parsing data from xml (XMLPullParse)
            parseApplications.parse(s);

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this,R.layout.list_record,parseApplications.getApplications());

            // set adapter to listView
            listapps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with"+ strings[0]);

            String rssFeed = downlaodXML(strings[0]);
            if(rssFeed == null)
            {
                Log.e(TAG, "doInBackground: ERROR Downlaoding XML");
            }
            return rssFeed; // from downloadXML method to onPostExecute Method
        }

        private String downlaodXML(String urlPath) // storing data to a string by reading the xml inputStream
        {
            StringBuilder xmlResult = new StringBuilder();
            try{
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downlaodXML: response code is"+response);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true)
                {
                    charsRead = bufferedReader.read(inputBuffer);
                    if(charsRead<0)
                    {
                        break;
                    }
                    if(charsRead>0)
                    {
                        xmlResult.append(String.copyValueOf(inputBuffer,0,charsRead));
                    }
                }
                bufferedReader.close(); // closing the buffered reader will close all the io objects such as inputStream reader
                return xmlResult.toString();
            }catch (MalformedURLException e)
            {
                Log.e(TAG, "downlaodXML: Invalid URL"+e.getMessage());
            }catch (IOException e)
            {
                Log.e(TAG, "downlaodXML: IO Exception reading Data" + e.getMessage());
            }
            catch (SecurityException e) // for permissions
            {
                Log.e(TAG, "downlaodXML: SECURITY EXCEPTION needsPermission?" +e.getMessage() );
            }
            return null;
        }
    }
}
