package com.nickinfinity.toprssfeeddownloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

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
    String feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listapps = findViewById(R.id.xmlListView);
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
