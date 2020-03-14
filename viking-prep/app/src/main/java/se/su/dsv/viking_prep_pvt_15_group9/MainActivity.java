package se.su.dsv.viking_prep_pvt_15_group9;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SessionManager;


public class MainActivity extends Activity {

    private CharSequence mTitle;
    private String[] mMenuItemTitles;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Bitmap userProfilePicture;

    private SQLiteManager localDatabase;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = getResources().getString(R.string.app_name);
        mMenuItemTitles = getResources().getStringArray(R.array.array_menu_content);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Lägger till en databashanterare.
        localDatabase = new SQLiteManager(getApplicationContext());

        // Lägger till en session manager.
        session = new SessionManager(getApplicationContext());

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, mMenuItemTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectDrawerItem(0);
        }
    }

    public Bitmap getUserProfilePicture() {
        return userProfilePicture;
    }

    private void selectDrawerItem(int position) {

        Fragment clickedFragment;
        FragmentManager fragmentManager = getFragmentManager();

        switch (position) {
            case 0:
                clickedFragment = new StartFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, clickedFragment).addToBackStack("").commit();
                break;
            case 1:
                clickedFragment = new MyProfileFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, clickedFragment).addToBackStack("").commit();
                break;
            case 2:
                clickedFragment = new MatchmakingFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, clickedFragment).addToBackStack("").commit();
                break;
            case 3:
                clickedFragment = new SearchPeopleFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, clickedFragment).addToBackStack("").commit();
                break;
            case 4:
                clickedFragment = new ConversationsFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, clickedFragment).addToBackStack("").commit();
                break;
            case 5:
                clickedFragment = new FriendsFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, clickedFragment).addToBackStack("").commit();
                break;
            case 6:
                clickedFragment = new ObstaclesFragment();
                fragmentManager.beginTransaction().replace(R.id.content_frame, clickedFragment).addToBackStack("").commit();
                break;
            case 7:
                Intent logout = new Intent(this, LoginActivity.class);
                session.setLoggedIn(false);
                startActivity(logout);
                break;
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuItemTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

    public SQLiteManager getLocalDatabase() {
        return localDatabase;
    }

    public void setActionBarTitle(String title) {
        getActionBar().setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if(count==0){
            super.onBackPressed();
        }else{
            getFragmentManager().popBackStack();
        }
    }
}
