package com.threemoji.threemoji;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.threemoji.threemoji.service.RegistrationIntentService;
import com.threemoji.threemoji.utility.SvgUtils;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ViewPager mViewPager;

    // ================================================================
    // Initialisation methods
    // ================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPrefs().getBoolean(getString(R.string.pref_has_seen_start_page_key), false)) {
            startRegistrationIntentServiceIfNeeded();
        } else {
            updateVersionInPrefs(BuildConfig.VERSION_CODE);
            startService(RegistrationIntentService.createIntent(this,
                                                                RegistrationIntentService.Action.CREATE_TOKEN));
            startStartPage();
        }

        setContentView(R.layout.activity_main);
        initActionBar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        initNavigationDrawer();

        initViewPagerAndTabs();
    }

    private void startStartPage() {
        Intent intent = new Intent(this, StartPageActivity.class);
        startActivity(intent);
        finish();
    }


    // ================================================================
    // Methods to check if registration is needed
    // ================================================================
    private void startRegistrationIntentServiceIfNeeded() {
        if (hasGooglePlayServices()) {
            // hasVersionChanged is always checked first to ensure shared preferences is updated
            if (hasVersionChanged() || !hasToken()) {
                startService(RegistrationIntentService.createIntent(this,
                                                                    RegistrationIntentService.Action.UPDATE_TOKEN));
            }
        }
    }

    private boolean hasGooglePlayServices() {
        // https://developers.google.com/android/guides/setup#ensure_devices_have_the_google_play_services_apk
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        Log.i(TAG, "checking for Google Play Services");
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                Dialog dialog = googleApiAvailability.getErrorDialog(this, resultCode, 1);
                dialog.show();
            } else {
                Log.i(TAG, "This device is not supported");
                finish();
            }
            return false;
        }
        return true;
    }

    private boolean hasVersionChanged() {
        int registeredVersion = getPrefs().getInt("appVersion", Integer.MIN_VALUE);
        int currentVersion = BuildConfig.VERSION_CODE;
        if (registeredVersion != currentVersion) {
            Log.v(TAG, "App version changed " + registeredVersion + " vs " + currentVersion);
            updateVersionInPrefs(currentVersion);
            return true;
        }
        return false;
    }

    private boolean hasToken() {
        String token = getPrefs().getString(getString(R.string.pref_token_key), "");
        if (token != null && token.length() == 0) {
            Log.v(TAG, "Registration not found.");
            return false;
        }
        Log.v(TAG, "Current token " + token);
        return true;
    }

    private void updateVersionInPrefs(int currentVersion) {
        getPrefs().edit().putInt("appVersion", currentVersion).apply();
    }


    // ================================================================
    // ActionBar initialisation methods
    // ================================================================
    private void initActionBar() {
        // Set a Toolbar to replace the ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.i(TAG, "added action bar");

        // Set the menu icon instead of the launcher icon.
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    // ================================================================
    // Navigation drawer initialisation methods
    // ================================================================
    private void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        initProfile();
        initDrawerListeners(navigationView);
    }

    private void initProfile() {
        if (getPrefs().getBoolean(getString(R.string.pref_has_seen_start_page_key), false)) {
            initProfileEmoji();
            initProfileGeneratedName();
        }
    }

    private void initProfileEmoji() {
        SharedPreferences prefs = getPrefs();
        int mSizeOfEmojiIcon = 72;

        ImageView userEmoji1 = (ImageView) findViewById(R.id.profile_emoji1);
        ImageView userEmoji2 = (ImageView) findViewById(R.id.profile_emoji2);
        ImageView userEmoji3 = (ImageView) findViewById(R.id.profile_emoji3);

        String emoji1ResourceName = prefs.getString(getString(R.string.profile_emoji_one_key),
                                                    null);
        String emoji2ResourceName = prefs.getString(getString(R.string.profile_emoji_two_key),
                                                    null);
        String emoji3ResourceName = prefs.getString(getString(R.string.profile_emoji_three_key),
                                                    null);

        Drawable emoji1DrawableResource =
                SvgUtils.getSvgDrawable(emoji1ResourceName, mSizeOfEmojiIcon, getPackageName());
        Drawable emoji2DrawableResource =
                SvgUtils.getSvgDrawable(emoji2ResourceName, mSizeOfEmojiIcon, getPackageName());
        Drawable emoji3DrawableResource =
                SvgUtils.getSvgDrawable(emoji3ResourceName, mSizeOfEmojiIcon, getPackageName());

        userEmoji1.setImageDrawable(emoji1DrawableResource);
        userEmoji2.setImageDrawable(emoji2DrawableResource);
        userEmoji3.setImageDrawable(emoji3DrawableResource);
    }

    private void initProfileGeneratedName() {
        String name = getPrefs().getString(getString(R.string.profile_generated_name_key), "");
        ((TextView) findViewById(R.id.profile_generated_name)).setText("You: " + name);
    }

    private void initDrawerListeners(NavigationView navigationView) {
        View view = findViewById(R.id.nav_header);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navDrawerChangeEmoji();
            }
        });
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void navDrawerChangeEmoji() {
        Intent intent = new Intent(this, StartPageActivity.class);
        startActivity(intent);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.nav_people_nearby:
                mViewPager.setCurrentItem(1);
                break;

            case R.id.nav_chats:
            default:
                mViewPager.setCurrentItem(0);
                break;
        }
    }


    // ================================================================
    // ViewPager and Tabs initialisation methods
    // ================================================================
    private void initViewPagerAndTabs() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        CustomAdapter adapter = new CustomAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatListFragment(), "Chats");
        adapter.addFragment(new PeopleNearbyFragment(), "People nearby");
        viewPager.setAdapter(adapter);
    }


    // ================================================================
    // Overridden methods
    // ================================================================
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }


    // ================================================================
    // Utility methods
    // ================================================================
    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }


    // ================================================================
    // Inner adapter class for viewpager
    // ================================================================
    static class CustomAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public CustomAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
