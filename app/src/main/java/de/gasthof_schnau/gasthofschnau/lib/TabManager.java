package de.gasthof_schnau.gasthofschnau.lib;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import de.gasthof_schnau.gasthofschnau.R;

import java.util.Locale;

public class TabManager implements ActionBar.TabListener {

    private AppCompatActivity c;

    private ViewPager mViewPager;

    private SparseArray<String> tabs = new SparseArray<>();
    private SparseArray<Fragment> fragments = new SparseArray<>();

    public TabManager(AppCompatActivity activity) {
        this.c = activity;
    }

    private int tabPosition = 0;
    private int fragmentPosition = 0;

    public void addTab(String name, Fragment tabFragment) {
        tabs.put(tabPosition, name);
        tabPosition++;

        fragments.put(fragmentPosition, tabFragment);
        fragmentPosition++;
    }

    public void init() {

        final ActionBar actionBar = c.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(c.getSupportFragmentManager());

        mViewPager = (ViewPager) c.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) { mViewPager.setCurrentItem(tab.getPosition()); }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}


    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            final String ARG_OBJECT = "object";
            Bundle args = new Bundle();
            args.putInt(ARG_OBJECT, position + 1);
            fragment = fragments.get(position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position).toUpperCase(Locale.getDefault());
        }

    }

}
