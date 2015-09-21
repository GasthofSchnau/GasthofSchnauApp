package de.gasthof_schnau.gasthofschnau.tab_fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.gasthof_schnau.gasthofschnau.Entry;
import de.gasthof_schnau.gasthofschnau.R;
import de.gasthof_schnau.gasthofschnau.lib.DownloadTask;
import de.gasthof_schnau.gasthofschnau.lib.Internet;
import de.gasthof_schnau.gasthofschnau.lib.XmlParser;

public class NewsFragment extends Fragment {

    private Context c;

    private List<Entry> entries;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        c = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, null);
        ((TextView) view.findViewById(R.id.text)).setMovementMethod(ScrollingMovementMethod.getInstance());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private class DownloadNewsTask extends DownloadTask<List<Entry>> {

        @Override
        protected List<Entry> loadFromNetwork(String urlString) {
            Parser parser = new Parser();
            List<Entry> entries = null;
            InputStream input = null;

            try {
                input = downloadUrl(urlString);

                OutputStream output = c.openFileOutput("news.xml", Context.MODE_PRIVATE);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                output.write(total.toString().getBytes());
                output.close();

                input = c.openFileInput("news.xml");
                entries = parser.parse(input);

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return entries;
        }

        @Override
        protected void onPostExecute(List<Entry> fertigeEntries) {
            entries = fertigeEntries;
            showResult(true);
        }

    }

    private class Parser extends XmlParser {

        @Override
        protected Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, null, "entry");
            Entry entry = new Entry();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                switch (parser.getName()) {
                    case "title":
                        entry.setTitle(readComponent(parser, "title"));
                        break;
                    case "text":
                        entry.setText(readComponent(parser, "text"));
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
            return entry;
        }
    }

    public void update() {

        if (Internet.isOnline(c)) {

            getView().findViewById(R.id.noConnectionMessage).setVisibility(View.GONE);
            getView().findViewById(R.id.retryButton).setVisibility(View.GONE);
            getView().findViewById(R.id.stand).setVisibility(View.GONE);

            getView().findViewById(R.id.textTitle).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.text).setVisibility(View.VISIBLE);

            new DownloadNewsTask().execute("http://gasthofschnau.github.io/news.xml");

        } else {
            if (new File(c.getFilesDir(), "news.xml").exists()) {
                try {
                    Parser parser = new Parser();
                    InputStream stream = c.openFileInput("news.xml");
                    entries = parser.parse(stream);
                    stream.close();

                    getView().findViewById(R.id.noConnectionMessage).setVisibility(View.GONE);
                    getView().findViewById(R.id.retryButton).setVisibility(View.GONE);

                    getView().findViewById(R.id.textTitle).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.text).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.stand).setVisibility(View.VISIBLE);

                    showResult(false);
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                Button retryButton = (Button) getView().findViewById(R.id.retryButton);
                retryButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update();
                    }
                });

                getView().findViewById(R.id.textTitle).setVisibility(View.GONE);
                getView().findViewById(R.id.text).setVisibility(View.GONE);
                getView().findViewById(R.id.stand).setVisibility(View.GONE);

                getView().findViewById(R.id.noConnectionMessage).setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showResult(boolean isOnline) {

        ((TextView) getView().findViewById(R.id.textTitle)).setText(entries.get(0).getTitle().replace("\\n", "\n"));
        ((TextView) getView().findViewById(R.id.text)).setText(entries.get(0).getText().replace("\\n", "\n"));

        if(!isOnline) {
            TextView stand = (TextView) getView().findViewById(R.id.stand);
            stand.setText("Zuletzt aktualisiert:\n"+ new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new File(c.getFilesDir().getPath(), "events.xml").lastModified()) + " Uhr");
        }

    }

}