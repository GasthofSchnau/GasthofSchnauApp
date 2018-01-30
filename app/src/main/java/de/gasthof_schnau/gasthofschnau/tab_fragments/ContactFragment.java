package de.gasthof_schnau.gasthofschnau.tab_fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import de.gasthof_schnau.gasthofschnau.R;
import de.gasthof_schnau.gasthofschnau.lib.ExpandableHeightGridView;
import de.gasthof_schnau.gasthofschnau.lib.Util;

public class ContactFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, null);

        final Context c = getActivity();

        ExpandableHeightGridView gridView = (ExpandableHeightGridView) view.findViewById(R.id.gridView);
        gridView.setExpanded(true);
        gridView.setAdapter(new ContactAdapter(c));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                switch (position) {
                    case 0:
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:042082312"));
                        Util.makeToast(c, c.getString(R.string.calling_gasthof_schnau));
                        startActivity(callIntent);
                        break;
                    case 1:
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{c.getString(R.string.gasthof_schnau_email)});
                        startActivity(i);
                        break;
                    case 2:
                        Util.makeToast(c, c.getString(R.string.please_wait));
                        try {
                            c.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1533748086889460")));
                        } catch (Exception e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/GasthofSchnau")));
                        }
                        break;
                    case 3:
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=Gasthof+Schnau"));
                        startActivity(intent);
                        break;
                }
            }
        });

        return view;
    }

    private class ContactAdapter extends BaseAdapter {
        private Context mContext;

        public ContactAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(Util.convertToDip(150, mContext), Util.convertToDip(150, mContext)));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(Util.convertToDip(8, mContext), Util.convertToDip(8, mContext), Util.convertToDip(8, mContext), Util.convertToDip(8, mContext));
            } else {
                imageView = (ImageView) convertView;
            }

            Picasso.with(mContext).load(mThumbIds[position]).into(imageView);
            return imageView;
        }

        private Integer[] mThumbIds = {
                R.drawable.phone,
                R.drawable.email,
                R.drawable.facebook,
                R.drawable.google_maps_logo
        };
    }

}