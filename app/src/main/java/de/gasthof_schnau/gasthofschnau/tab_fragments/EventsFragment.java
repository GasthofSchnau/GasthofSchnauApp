package de.gasthof_schnau.gasthofschnau.tab_fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
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
import de.gasthof_schnau.gasthofschnau.lib.AnimatedExpandableListView;
import de.gasthof_schnau.gasthofschnau.lib.DownloadTask;
import de.gasthof_schnau.gasthofschnau.lib.Internet;
import de.gasthof_schnau.gasthofschnau.lib.Util;
import de.gasthof_schnau.gasthofschnau.lib.XmlParser;

public class EventsFragment extends Fragment {

    private Context c;

    private List<Entry> entries;
    private EventsAdapter adapter;
    private AnimatedExpandableListView listView;
    private SparseArray<GroupItem> groups;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        c = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EventsAdapter(c);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, null);
        listView = (AnimatedExpandableListView) view.findViewById(R.id.listView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
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
                while((line = r.readLine()) != null) {
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
            showList(true);
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
                    case "date":
                        entry.setDate(readComponent(parser, "date"));
                        break;
                    case "time":
                        entry.setTime(readComponent(parser, "time"));
                        break;
                    case "price":
                        entry.setPrice(readComponent(parser, "price"));
                        break;
                    case "more_info":
                        entry.setMoreInfo(readComponent(parser, "more_info"));
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

            getView().findViewById(R.id.listView).setVisibility(View.VISIBLE);

            new DownloadEventsTask().execute("http://gasthofschnau.github.io/events.xml");

        } else {
            if (new File(c.getFilesDir().getPath(), "events.xml").exists()) {
                try {
                    Parser parser = new Parser();
                    InputStream stream = c.openFileInput("events.xml");
                    entries = parser.parse(stream);
                    stream.close();

                    getView().findViewById(R.id.noConnectionMessage).setVisibility(View.GONE);
                    getView().findViewById(R.id.retryButton).setVisibility(View.GONE);

                    getView().findViewById(R.id.listView).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.stand).setVisibility(View.VISIBLE);

                    showList(false);
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

                getView().findViewById(R.id.listView).setVisibility(View.GONE);
                getView().findViewById(R.id.stand).setVisibility(View.GONE);

                getView().findViewById(R.id.noConnectionMessage).setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showList(boolean isOnline) {

        groups = new SparseArray<>();

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
            groups.append(groups.size(), group);

            adapter.setData(groups);
            listView.setAdapter(adapter);
        }

        if(!isOnline) {
            TextView stand = (TextView) getView().findViewById(R.id.stand);
            stand.setText("Zuletzt aktualisiert:\n"+ new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new File(c.getFilesDir().getPath(), "events.xml").lastModified()) + " Uhr");
        }


        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                }
                return true;
            }

        });

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Util.makeToast(c, "Hier muss Hauke noch was machen");
                return true;
            }
        });

        adapter.setData(groups);
        listView.setAdapter(adapter);

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

        private SparseArray<GroupItem> items;

        private EventsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(SparseArray<GroupItem> items) {
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
