package de.gasthof_schnau.gasthofschnau.tab_fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.gasthof_schnau.gasthofschnau.Entry;
import de.gasthof_schnau.gasthofschnau.MoreInfoActivity;
import de.gasthof_schnau.gasthofschnau.R;
import de.gasthof_schnau.gasthofschnau.SettingsActivity;
import de.gasthof_schnau.gasthofschnau.lib.AnimatedExpandableListView;
import de.gasthof_schnau.gasthofschnau.lib.DownloadTask;
import de.gasthof_schnau.gasthofschnau.lib.Internet;
import de.gasthof_schnau.gasthofschnau.lib.Util;
import de.gasthof_schnau.gasthofschnau.lib.XmlParser;

public class EventsFragment extends Fragment {

    private Context c;
    private View v;

    private List<Entry> entries;
    private EventsAdapter adapter;
    private AnimatedExpandableListView listView;
    private List<GroupItem> groups;

    private boolean showNoConnectionWarningToast;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        c = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new EventsAdapter(c);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (v == null) v = inflater.inflate(R.layout.fragment_events, null);
        listView = (AnimatedExpandableListView) v.findViewById(R.id.listView);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        update(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_syncable, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                update(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private class DownloadEventsTask extends DownloadTask<List<Entry>> {

        @Override
        protected List<Entry> loadFromNetwork(String urlString) {
            Parser parser = new Parser();
            List<Entry> entries = null;
            InputStream input = null;

            try {

                input = downloadUrl(urlString);

                OutputStream output = c.openFileOutput("events.xml", Context.MODE_PRIVATE);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                output.write(total.toString().getBytes());
                output.close();

                input = c.openFileInput("events.xml");
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
            showList();
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(showNoConnectionWarningToast) {
                Util.makeToast(c, "Da momentan keine Verbindung zum Internet besteht, wurde die letzte verfügbare Sitzung wiederhergestellt.", 1);
                showNoConnectionWarningToast = false;
            }
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
                        entry.setTitle(readComponent(parser, "title").replace("\\n", "\n"));
                        break;
                    case "text":
                        entry.setText(readComponent(parser, "text").replace("\\n", "\n"));
                        break;
                    case "date":
                        entry.setDate(readComponent(parser, "date").replace("\\n", "\n"));
                        break;
                    case "time":
                        entry.setTime(readComponent(parser, "time".replace("\\n", "\n")));
                        break;
                    case "price":
                        entry.setPrice(readComponent(parser, "price").replace("\\n", "\n"));
                        break;
                    case "more_info":
                        entry.setMoreInfo(readComponent(parser, "more_info").replace("\\n", "\n"));
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
            return entry;
        }
    }

    public void update(boolean manuallyUpdated) {

        v.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        if (Internet.isOnline(c)) {

            if ((PreferenceManager.getDefaultSharedPreferences(c).getString(SettingsActivity.PREF_KEY_SYNC_EVENTS, "").equals("Automatisch")) || (!new File(c.getFilesDir().getPath(), "events.xml").exists()) || manuallyUpdated) {
                v.findViewById(R.id.noConnectionMessage).setVisibility(View.GONE);
                v.findViewById(R.id.retryButton).setVisibility(View.GONE);
                v.findViewById(R.id.stand).setVisibility(View.GONE);

                v.findViewById(R.id.listView).setVisibility(View.VISIBLE);

                new DownloadEventsTask().execute("http://gasthofschnau.github.io/events.xml");
            } else {
                try {
                    Parser parser = new Parser();
                    InputStream stream = c.openFileInput("events.xml");
                    entries = parser.parse(stream);
                    stream.close();

                    v.findViewById(R.id.noConnectionMessage).setVisibility(View.GONE);
                    v.findViewById(R.id.retryButton).setVisibility(View.GONE);

                    v.findViewById(R.id.listView).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.stand).setVisibility(View.VISIBLE);

                    showList();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            if (new File(c.getFilesDir().getPath(), "events.xml").exists()) {
                if(getUserVisibleHint()) Util.makeToast(c, "Da momentan keine Verbindung zum Internet besteht, wurde die letzte verfügbare Sitzung wiederhergestellt.", 1);
                else showNoConnectionWarningToast = true;
                try {
                    Parser parser = new Parser();
                    InputStream stream = c.openFileInput("events.xml");
                    entries = parser.parse(stream);
                    stream.close();

                    v.findViewById(R.id.noConnectionMessage).setVisibility(View.GONE);
                    v.findViewById(R.id.retryButton).setVisibility(View.GONE);

                    v.findViewById(R.id.listView).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.stand).setVisibility(View.VISIBLE);

                    showList();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                Button retryButton = (Button) v.findViewById(R.id.retryButton);
                retryButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update(true);
                    }
                });

                v.findViewById(R.id.listView).setVisibility(View.GONE);
                v.findViewById(R.id.stand).setVisibility(View.GONE);
                v.findViewById(R.id.progressBar).setVisibility(View.GONE);

                v.findViewById(R.id.noConnectionMessage).setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showList() {

        groups = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            GroupItem group = new GroupItem();
            if (entries.get(i).getTitle() != null)
                group.title = entries.get(i).getTitle().replace("\\n", "\n");

            ChildItem child = new ChildItem();
            if (entries.get(i).getText() != null)
                child.text = entries.get(i).getText().replace("\\n", "\n");
            else
                child.text = " ";

            if (entries.get(i).getDate() != null)
                child.date = entries.get(i).getDate().replace("\\n", "\n");
            else
                child.date = " ";

            if (entries.get(i).getTime() != null)
                child.time = entries.get(i).getTime().replace("\\n", "\n");
            else
                child.time = " ";

            if (entries.get(i).getPrice() != null)
                child.price = entries.get(i).getPrice().replace("\\n", "\n");
            else
                child.price = " ";

            group.items.append(group.items.size(), child);
            groups.add(group);

            adapter.setData(groups);
            listView.setAdapter(adapter);
        }

        ((TextView) v.findViewById(R.id.stand)).setText("Zuletzt aktualisiert:\n" + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new File(c.getFilesDir().getPath(), "events.xml").lastModified()) + " Uhr");

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
                    if (sharedPrefs.getBoolean("show_tutorial_events_more_info", true)) {
                        final Snackbar snackbar = Snackbar.make(v, "Sie möchten mehr Info über das Menü? Tippen Sie einfach auf einen bereits ausgeklappten Eintrag", Snackbar.LENGTH_INDEFINITE);
                        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setLines(3);
                        snackbar.setAction("Okay!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        }).show();
                        sharedPrefs.edit().putBoolean("show_tutorial_events_more_info", false).apply();
                    }
                }
                return true;
            }

        });

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String date = entries.get(groupPosition).getDate();
                date = date.replace(" Januar ", "1.");
                date = date.replace(" Februar ", "2.");
                date = date.replace(" März ", "3.");
                date = date.replace(" April ", "4.");
                date = date.replace(" Mai ", "5.");
                date = date.replace(" Juni ", "6.");
                date = date.replace(" Juli ", "7.");
                date = date.replace(" August ", "8.");
                date = date.replace(" September ", "9.");
                date = date.replace(" Oktober ", "10.");
                date = date.replace(" November ", "11.");
                date = date.replace(" Dezember ", "12.");
                Intent i = new Intent(c, MoreInfoActivity.class);
                i.putExtra("title", entries.get(groupPosition).getTitle().replace(" - AUSGEBUCHT", "") + " " + date);
                i.putExtra("moreInfo", entries.get(groupPosition).getMoreInfo());
                startActivity(i);
                return true;
            }
        });

        adapter.setData(groups);
        listView.setAdapter(adapter);

        v.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }


    private static class GroupItem {
        String title;
        SparseArray<ChildItem> items = new SparseArray<>();
    }

    private static class ChildItem {
        String text;
        String date;
        String time;
        String price;
    }

    private static class ChildHolder {
        TextView text;
        TextView date;
        TextView time;
        TextView price;
    }

    private static class GroupHolder {
        TextView title;
    }

    private class EventsAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        private LayoutInflater inflater;

        private List<GroupItem> items;

        private EventsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<GroupItem> items) {
            this.items = items;
        }

        @Override
        public ChildItem getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            ChildItem item = getChild(groupPosition, childPosition);
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.price = (TextView) convertView.findViewById(R.id.price);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }

            holder.text.setText(item.text);
            holder.date.setText(item.date);
            holder.time.setText(item.time);
            holder.price.setText(item.price);

            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return items.get(groupPosition).items.size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            GroupItem item = getGroup(groupPosition);
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.group_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.textTitle);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }

            holder.title.setText(item.title);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


    }

}
