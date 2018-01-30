package de.gasthof_schnau.gasthofschnau.lib;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class DownloadTask<Result> extends AsyncTask<String, Void, Result> {

    @Override
    protected Result doInBackground(String... urls) {
        return loadFromNetwork(urls[0]);
    }

    protected abstract Result loadFromNetwork(String urlString);

    @Override
    protected abstract void onPostExecute(Result result);

    protected InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

}
