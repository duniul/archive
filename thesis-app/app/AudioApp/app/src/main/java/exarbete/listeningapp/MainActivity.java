package exarbete.listeningapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import exarbete.listeningapp.database.SQLiteHelper;
import exarbete.listeningapp.recording.RecorderFragment;
import exarbete.listeningapp.recording.ListeningSessionsFragment;
import exarbete.listeningapp.recording.SettingsFragment;

/**
 * The MainActivity of the app. This is the activity that opens when the app launches.
 */
public class MainActivity extends AppCompatActivity {

    public static final int DRAWER_MODE_MAIN_MENU = 0;
    public static final int DRAWER_MODE_ACCOUNT_MENU = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static int[] drawerMainMenuIcons = {R.drawable.ic_drawer_item_sign_out};
    private static int[] drawerMainMenuLabels = {R.string.drawer_item_sign_out};
    private static int[] drawerAccountMenuIcons = {R.drawable.ic_drawer_item_sign_out};
    private static int[] drawerAccountMenuLabels = {R.string.drawer_item_sign_out};

    private AccountHandler accountHandler;

    private DrawerRecyclerAdapter drawerRecyclerAdapter;
    private RecorderFragment recorderFragment;
    private SettingsFragment settingsFragment;

    private int drawerMode;
    private ViewPager viewPager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!SharedPrefsHandler.getInstance().isUserLoggedIn()){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize database.
        SQLiteHelper.initialize(MainActivity.this);

        // Sets up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sets up the view pager.
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        // Sets up the tab layout along with the view pager.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // Sets up the recycler view to be used in the navigation drawer.
        RecyclerView drawerRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        drawerRecyclerView.setHasFixedSize(true);

        // Attaches adapter to the recycler view.
        drawerRecyclerAdapter = new DrawerRecyclerAdapter(MainActivity.this, drawerMainMenuIcons, drawerMainMenuLabels);
        drawerRecyclerView.setAdapter(drawerRecyclerAdapter);
        drawerMode = DRAWER_MODE_MAIN_MENU;

        // Adds a layout manager to the recycler view.
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        drawerRecyclerView.setLayoutManager(layoutManager);

        // Sets a drawer layout and adds an action drawer toggler.
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerMode = DRAWER_MODE_MAIN_MENU;
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Tries to sign in to Google.
        accountHandler = AccountHandler.getInstance(this);
        accountHandler.signInOnStart();
    }

    @Override
    public void onStart() {
        super.onStart();
        accountHandler.getGoogleApiClient().connect();
    }

    public void setViewPagerEnabled(boolean enabled){
        View tabLayout = findViewById(R.id.tabLayout);
        if(enabled){
            viewPager.setOnTouchListener(null);
            tabLayout.setVisibility(View.VISIBLE);
        }else{
            viewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    return true;
                }
            });
            tabLayout.setVisibility(View.INVISIBLE);
        }
    }

    // Sets up the view pager and adds the fragments to be paged.
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        recorderFragment = RecorderFragment.newInstanceOf();
        adapter.addFragment(recorderFragment, "Recorder");
        adapter.addFragment(ListeningSessionsFragment.newInstanceOf(), "Saved sessions");
        settingsFragment = SettingsFragment.newInstanceOf();
        adapter.addFragment(settingsFragment, "Audio Settings");
        viewPager.setAdapter(adapter);
    }

    public void setDrawerMode(int drawerMode) {
        this.drawerMode = drawerMode;
    }

    public void updateDrawerHeader() {
        drawerRecyclerAdapter.updateHeaderInfo();
        drawerRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == accountHandler.getSignInCode()) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            updateDrawerHeader();
            accountHandler.handleSignInResult(result);
        }
    }

    public void updateDrawerMenuItems() {

        switch (drawerMode) {
            case DRAWER_MODE_MAIN_MENU:
                drawerRecyclerAdapter.setHeaderExpandButtonIcon(R.drawable.ic_drawer_header_drop_up_button);
                drawerRecyclerAdapter.setDrawerItems(drawerAccountMenuIcons, drawerAccountMenuLabels);
                drawerMode = DRAWER_MODE_ACCOUNT_MENU;
                break;
            case DRAWER_MODE_ACCOUNT_MENU:
                drawerRecyclerAdapter.setHeaderExpandButtonIcon(R.drawable.ic_drawer_header_drop_down_button);
                drawerRecyclerAdapter.setDrawerItems(drawerMainMenuIcons, drawerMainMenuLabels);
                drawerMode = DRAWER_MODE_MAIN_MENU;
                break;
        }

        drawerRecyclerAdapter.notifyDataSetChanged();
    }

    public void onHeaderClick(View view) {

        if (!SharedPrefsHandler.getInstance().isUserLoggedIn()) {
            accountHandler.signIn();
        } else {
            updateDrawerMenuItems();
        }
    }

    public void onDrawerItemClick(View view) {
        String itemText = ((TextView) view).getText().toString();

        // Needs to be if-else rather than switch, because it's not using constant values.
        if (itemText.equals(getString(R.string.drawer_item_sign_out))) {
            recorderFragment.stopRecording();
            settingsFragment.stopTesting();
            accountHandler.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}