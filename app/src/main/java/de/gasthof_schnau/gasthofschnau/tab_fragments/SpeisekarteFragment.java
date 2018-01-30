package de.gasthof_schnau.gasthofschnau.tab_fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.Xml;
import android.view.*;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import de.gasthof_schnau.gasthofschnau.R;
import de.gasthof_schnau.gasthofschnau.SettingsActivity;
import de.gasthof_schnau.gasthofschnau.lib.DownloadTask;
import de.gasthof_schnau.gasthofschnau.lib.Internet;
import de.gasthof_schnau.gasthofschnau.lib.Util;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeisekarteFragment extends Fragment {

    private Context c;
    private View v;

    private List<GroupItem> groups;
    private ExpandableListView listView;
    private SpeisekarteAdapter adapter;

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
        adapter = new SpeisekarteAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(v == null) v = inflater.inflate(R.layout.fragment_speisekarte, null);
        listView = (ExpandableListView) v.findViewById(R.id.listView);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true;
            }
        });
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if (showNoConnectionWarningToast) {
                Util.makeToast(c, "Da momentan keine Verbindung zum Internet besteht, wurde die letzte verfügbare Sitzung wiederhergestellt.", 1);
                showNoConnectionWarningToast = false;
            }
        }
    }

    private void update(boolean manuallyUpdated) {

        v.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        if (Internet.isOnline(c)) {

            if((PreferenceManager.getDefaultSharedPreferences(c).getString(SettingsActivity.PREF_KEY_SYNC_SPEISEKARTE, "").equals("Automatisch")) || (!new File(c.getFilesDir().getPath(), "speisekarte.xml").exists()) || manuallyUpdated) {
                v.findViewById(R.id.noConnectionMessage).setVisibility(View.GONE);
                v.findViewById(R.id.retryButton).setVisibility(View.GONE);
                v.findViewById(R.id.stand).setVisibility(View.GONE);

                v.findViewById(R.id.listView).setVisibility(View.VISIBLE);

                new DownloadSpeisekarteTask().execute("http://gasthofschnau.github.io/speisekarte.xml");
            } else {
                try {
                    Parser parser = new Parser();
                    InputStream stream = c.openFileInput("speisekarte.xml");
                    groups = parser.parse(stream);
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
            if (new File(c.getFilesDir().getPath(), "speisekarte.xml").exists()) {
                if(getUserVisibleHint()) Util.makeToast(c, "Da momentan keine Verbindung zum Internet besteht, wurde die letzte verfügbare Sitzung wiederhergestellt.", 1);
                else showNoConnectionWarningToast = true;
                try {
                    Parser parser = new Parser();
                    InputStream stream = c.openFileInput("speisekarte.xml");
                    groups = parser.parse(stream);
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

        adapter.setData(groups);
        listView.setAdapter(adapter);

        ((TextView) v.findViewById(R.id.stand)).setText("Zuletzt aktualisiert:\n" + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new File(c.getFilesDir().getPath(), "speisekarte.xml").lastModified()) + " Uhr");

        v.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    private class DownloadSpeisekarteTask extends DownloadTask<List<GroupItem>> {

        @Override
        protected List<GroupItem> loadFromNetwork(String urlString) {

            Parser parser = new Parser();
            InputStream input = null;

            try {

                input = downloadUrl(urlString);

                OutputStream output = c.openFileOutput("speisekarte.xml", Context.MODE_PRIVATE);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                output.write(total.toString().getBytes());
                output.close();

                input = c.openFileInput("speisekarte.xml");
                groups = parser.parse(input);
                input.close();

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

            return groups;
        }

        @Override
        protected void onPostExecute(List<GroupItem> fertigeGroups) {
            showList();
        }
    }

    private class Parser {

        private final String ns = null;

        public List<GroupItem> parse(InputStream in) throws XmlPullParserException, IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return read(parser);
            } finally {
                in.close();
            }
        }

        private List<GroupItem> read(XmlPullParser parser) throws XmlPullParserException, IOException {
            List<GroupItem> groups = new ArrayList<>();

            parser.require(XmlPullParser.START_TAG, ns, "feed");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equalsIgnoreCase("group")) {
                    groups.add(readGroup(parser));
                } else {
                    skip(parser);
                }

            }

            return groups;
        }

        private String readComponent(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, tag);
            String result = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, tag);
            return result;
        }

        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }

        private ChildItem readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, null, "entry");
            ChildItem child = new ChildItem();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                switch (parser.getName()) {
                    case "title":
                        child.title = readComponent(parser, "title").replace("\\n", "\n");
                        break;
                    case "text":
                        child.text = readComponent(parser, "text").replace("\\n", "\n");
                        break;
                    case "price":
                        child.price = readComponent(parser, "price").replace("\\n", "\n");
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }

            return child;
        }

        private GroupItem readGroup(XmlPullParser parser) throws IOException, XmlPullParserException {

            GroupItem group = new GroupItem();
            group.items = new SparseArray<>();

            parser.require(XmlPullParser.START_TAG, ns, "group");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                switch (parser.getName()) {
                    case "title":
                        group.title = readComponent(parser, "title");
                        break;
                    case "entry":
                        group.items.append(group.items.size(), readEntry(parser));
                        break;
                    default:
                        skip(parser);
                        break;
                }

            }
            return group;
        }

    }

    private static class GroupItem {
        String title;
        SparseArray<ChildItem> items = new SparseArray<>();
    }

    private static class ChildItem {
        String title;
        String text;
        String price;
    }

    private static class GroupHolder {
        TextView title;
    }

    private static class ChildHolder {
        TextView title;
        TextView text;
        TextView price;
    }

    private class SpeisekarteAdapter extends BaseExpandableListAdapter {

        private LayoutInflater inflater;

        private List<GroupItem> items;

        private SpeisekarteAdapter() {
            inflater = LayoutInflater.from(c);
        }

        public void setData(List<GroupItem> items) {
            this.items = items;
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return items.get(groupPosition).items.size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public ChildItem getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ((ExpandableListView) parent).expandGroup(groupPosition);
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
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            GroupItem group = getGroup(groupPosition);
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.speisekarte_list_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.price = (TextView) convertView.findViewById(R.id.price);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }

            holder.title.setText(group.items.get(childPosition).title);
            holder.text.setText(group.items.get(childPosition).text);
            holder.price.setText(group.items.get(childPosition).price);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

}
