package com.percicjan.doodling.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.percicjan.doodling.custom.CustomPageTransformer;
import com.percicjan.doodling.custom.CustomViewPager;
import com.percicjan.doodling.fragments.DrawFragment;
import com.percicjan.doodling.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by janpercic on 11. 01. 14.
 * DrawActivity is the activity that is called when the app opens. In this activity
 * a JSON file for colors is read and navigation tabs and fragments in viewpager are
 * created.
 */

public class DrawActivity extends FragmentActivity {

    private static Context mContext;
    private static CustomViewPager mViewPager;
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    // Array for colors that need to be parsed from JSON file
    private String[] mColorsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_activity);
        mContext = this;

        // Method for getting colors from JSON file
        readColorsFromJson();

        // Specify that tabs should be displayed below the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adapter for fragments in viewpager
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Viewpager
        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        // Optional for transitions between fragments
//        mViewPager.setPageTransformer(true, new CustomPageTransformer());
        mViewPager.setOnPageChangeListener(new CustomViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between pages, select the corresponding tab.
                getActionBar().setSelectedNavigationItem(position);
            }
        });


        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // Show the given tab
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // Hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // Ignore this event
            }
        };


        // Add 2 tabs, specifying the tab's text and TabListener
        for (int i = 0; i < 2; i++) {
            actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.navigation_tab_title) + " " + (i + 1)).setTabListener(tabListener));
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.draw_activity_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // List of active fragments
        List<Fragment> fragmentsList = getSupportFragmentManager().getFragments();

        switch (item.getItemId()) {
            case R.id.draw_activity_menu_save:
                // Save the canvas of the current fragment
                ((DrawFragment) fragmentsList.get(mViewPager.getCurrentItem())).saveDrawing();
                break;

            case R.id.draw_activity_menu_discard:
                // Discard the canvas of the current fragment
                ((DrawFragment) fragmentsList.get(mViewPager.getCurrentItem())).deleteDrawing();
                break;

            case android.R.id.home:
                break;

        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * Saves colors for foreground and background from JSON file in res/raw/ folder.
     */
    private void readColorsFromJson() {
        // Init colors array
        mColorsArray = new String[4];

        // JSON file in res/raw/ folder
        InputStream mColorsStream = getResources().openRawResource(R.raw.colors);

        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(mColorsStream, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            mColorsStream.close();
        } catch (Exception exc) {
            Log.e("BUREK", "Error reading file");
        }
        String jsonString = writer.toString();
        try {
            // Get JSONObject from string and then get colors
            JSONObject colorJson = new JSONObject(jsonString);
            mColorsArray[0] = colorJson.getString("background_1");
            mColorsArray[1] = colorJson.getString("foreground_1");
            mColorsArray[2] = colorJson.getString("background_2");
            mColorsArray[3] = colorJson.getString("foreground_2");
        } catch (JSONException ex) {
            Log.e("BUREK", "JSONException - " + ex);
        }
    }



    /**
     * Returns a fragment corresponding to one of the primary sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new DrawFragment(mContext, true, mColorsArray[0], mColorsArray[1]);

                default:
                    return new DrawFragment(mContext, false, mColorsArray[2], mColorsArray[3]);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getString(R.string.navigation_tab_title) + " " + (position + 1);
        }
    }
}


