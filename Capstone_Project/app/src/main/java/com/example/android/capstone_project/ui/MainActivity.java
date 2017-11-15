package com.example.android.capstone_project.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.capstone_project.R;
import com.example.android.capstone_project.others.GetArticlesIdlingResource;
import com.example.android.capstone_project.others.NetworkChangeReceiver;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DataInterface{

    // For phone only
    @Nullable
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @Nullable
    @BindView(R.id.nav_view) NavigationView navigationView;

    // For both phone and tablet layout
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.listView) ListView listView;
    @BindView(R.id.spinner) Spinner spinner;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.adView) AdView adView;

    private String[] spinnerItems = new String[] {"All", "Business", "Entertainment", "Gaming", "General",
            "Music", "Politics","Science", "Sport", "Technology"};

    private ViewPager mPager;
    private MenuItem refreshList;
    private MyPagerAdapter mPagerAdapter;
    private ActionBarDrawerToggle toggle;

    private String spinnerSelection = "all";
    private String source_item = "";
    private String prev_source_item;

    private boolean syncFinished = false;
    private boolean isRefreshed = true;
    private boolean isNetworkChangeReceiverSet = false;
    private boolean isRotated = false;
    private boolean isSourceItemClicked = false;

    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;
    private NetworkChangeReceiver networkChangeReceiver;

    private GetArticlesIdlingResource mIdlingResource;
    private int mListViewSelectedItem = 999;

    private static final int ID_TOP_ARTICLES_LOADER = 156;
    private static final int ID_LATEST_ARTICLES_LOADER = 249;

    private int SPINNER_BUSINESS = 1;
    private int SPINNER_ENTERTAINMENT = 2;
    private int SPINNER_GAMING = 3;
    private int SPINNER_GENERAL = 4;
    private int SPINNER_MUSIC = 5;
    private int SPINNER_POLITICS = 6;
    private int SPINNER_SCIENCE_NATURE = 7;
    private int SPINNER_SPORT = 8;
    private int SPINNER_TECHNOLOGY = 9;

    @NonNull
    public GetArticlesIdlingResource getIdleResource(){
        if (mIdlingResource == null) {
            mIdlingResource = new GetArticlesIdlingResource(this);
        }
        return mIdlingResource;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // For phones only
        if(drawer != null) {
            toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(this);
        }

        tabLayout.addTab(tabLayout.newTab().setText(this.getString(R.string.top)));
        tabLayout.addTab(tabLayout.newTab().setText(this.getString(R.string.latest)));

        mPagerAdapter = new MyPagerAdapter(getFragmentManager(), tabLayout.getTabCount());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        // No re-creation of fragments
        mPager.setOffscreenPageLimit(1);

        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_activated_1, spinnerItems){

        };

        spinner.setAdapter(spinnerAdapter);

        if(savedInstanceState != null){
            source_item = savedInstanceState.getString(getString(R.string.source_item));
            spinnerSelection = savedInstanceState.getString(getString(R.string.spinnerSelection));
            isRefreshed = savedInstanceState.getBoolean(getString(R.string.isRefreshed));
            mListViewSelectedItem = savedInstanceState.getInt(getString(R.string.listViewSelected));
            isRotated = true;
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                // Only activate item selection when activity is not being refreshed
                if(!isRefreshed) {
                    spinnerSelection = adapterView.getItemAtPosition(pos).toString().toLowerCase();
                    if(spinnerSelection.equals(getString(R.string.science))) {
                        spinnerSelection = getString(R.string.science_and_nature).toLowerCase();
                    }
                    Toast.makeText(MainActivity.this, spinnerSelection, Toast.LENGTH_SHORT).show();

                    // Only activate from user spinner selection
                    if(!isRotated && !isSourceItemClicked) {
                        updateFragments(source_item);
                        NavigationAdapter adapter = (NavigationAdapter) listView.getAdapter();
                        mListViewSelectedItem = 999;
                        adapter.setSelectedItem(mListViewSelectedItem);
                        adapter.notifyDataSetChanged();
                    }

                    // Reset rotation state and item click state
                    isRotated = false;
                    isSourceItemClicked = false;

                    // Use another variable to keep track of current source chosen
                    prev_source_item = source_item;

                    // Source item needs to be kept empty for all times, so as to react to changes due
                    // to nav item clicks as well as spinner item clicks
                    source_item = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Test network connection
        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        networkChangeReceiver = new NetworkChangeReceiver();
        if(!isConnected){
            promptNetworkConnection();
        }

        // Admob
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isNetworkChangeReceiverSet && networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.spinnerSelection), spinnerSelection);
        outState.putString(getString(R.string.source_item), prev_source_item);
        outState.putBoolean(getString(R.string.isRefreshed), isRefreshed);
        outState.putInt(getString(R.string.listViewSelected), mListViewSelectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        // Phone only
        if(drawer != null){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else super.onBackPressed();
        } else
            // For tablet layout
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        refreshList = menu.findItem(R.id.refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.search){

            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);

        } else if(id == R.id.refresh){

            listView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            isRefreshed = true;
            Fragment fragment_1 = mPagerAdapter.getRegisteredFragment(0);
            Fragment fragment_2 = mPagerAdapter.getRegisteredFragment(1);

            ((MainActivityFragment) fragment_1).hideRecyclerView();
            ((MainActivityFragment) fragment_2).hideRecyclerView();

            cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if(isConnected) {
                ((MainActivityFragment) fragment_1).getArticlesList(this);
                ((MainActivityFragment) fragment_2).getArticlesList(this);
                mPagerAdapter.notifyDataSetChanged();
            } else {
                promptNetworkConnection();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        closeDrawer();
        return true;
    }

    private void promptNetworkConnection(){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
        isNetworkChangeReceiverSet = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please connect to the Internet.")
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 999);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView messageText = (TextView) alert.findViewById(android.R.id.message);
        messageText.setTextDirection(View.TEXT_DIRECTION_LOCALE);
    }

    // Called when spinner item or nav item selected
    public void updateFragments(String source) {
        Fragment fragment_1 = mPagerAdapter.getRegisteredFragment(0);
        Fragment fragment_2 = mPagerAdapter.getRegisteredFragment(1);
        if (fragment_1 instanceof MainActivityFragment) {
            ((MainActivityFragment) fragment_1).setSource(source);
            ((MainActivityFragment) fragment_1).restartLoader(ID_TOP_ARTICLES_LOADER, spinnerSelection);
        }
        if(fragment_2 instanceof MainActivityFragment) {
            ((MainActivityFragment) fragment_2).setSource(source);
            ((MainActivityFragment) fragment_2).restartLoader(ID_LATEST_ARTICLES_LOADER, spinnerSelection);
        }
    }

    @Override
    public void onItemClicked(String source, String category) {
        source_item = source;
        isRefreshed = false;
        // Decouple item click from spinner selection. Only link between adapter item and spinner
        // is that onClick changes the display of spinner selection
        isSourceItemClicked = true;
        spinnerSelection = category;
        switch (category) {
            case "business":
                spinner.setSelection(SPINNER_BUSINESS);
                break;
            case "entertainment":
                spinner.setSelection(SPINNER_ENTERTAINMENT);
                break;
            case "gaming":
                spinner.setSelection(SPINNER_GAMING);
                break;
            case "general":
                spinner.setSelection(SPINNER_GENERAL);
                break;
            case "music":
                spinner.setSelection(SPINNER_MUSIC);
                break;
            case "politics":
                spinner.setSelection(SPINNER_POLITICS);
                break;
            case "science-and-nature":
                spinner.setSelection(SPINNER_SCIENCE_NATURE);
                break;
            case "sport":
                spinner.setSelection(SPINNER_SPORT);
                break;
            case "technology":
                spinner.setSelection(SPINNER_TECHNOLOGY);
                break;
        }
        updateFragments(source);
        closeDrawer();
    }

    @Override
    public ListView getListView() {
        return listView;
    }

    @Override
    public int getListViewSelected() {
        return mListViewSelectedItem;
    }

    @Override
    public void updateListViewSelected(int position) {
        mListViewSelectedItem = position;
    }

    @Override
    public String getSourceName() {
        return source_item;
    }

    @Override
    public String getSpinnerSelection(){
        return spinnerSelection;
    }

    @Nullable
    @Override
    public Spinner getSpinner() {
        return spinner;
    }


    @Override
    public NetworkChangeReceiver getNetworkChangeReceiver() {
        return networkChangeReceiver;
    }

    // Call when network receiver is registered
    @Override
    public boolean isNetworkChangeReceiverSet() {
        return isNetworkChangeReceiverSet;
    }

    // Call after fragment cursor loading is finished
    @Override
    public void setNetworkChangeReceiverFalse() {
        isNetworkChangeReceiverSet = false;
    }

    @Override
    public ActionBarDrawerToggle getToggle() {
        return toggle;
    }

    @Override
    public void closeDrawer() {
        if(drawer != null)
            drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void updateRotationStatus() {
        isRotated = true;
    }

    @Override
    public boolean getRotationStatus() {
        return isRotated;
    }

    @Override
    public MenuItem getRefreshListButton() {
        return refreshList;
    }

    @Override
    public boolean getRefreshStatus() {
        return isRefreshed;
    }

    @Override
    public void setRefreshStatusFalse() {
        isRefreshed = false;
    }

    // For testing purposes
    public boolean isSyncFinished(){
        return syncFinished;
    }

    // For testing purposes
    @Override
    public void setSyncFinished(){
        syncFinished = true;
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        // Register and store fragments for access later on
        private SparseArray<Fragment> registeredFragments = new SparseArray<>();

        int tabCount;

        public MyPagerAdapter(FragmentManager fm, int mTabCount) {
            super(fm);
            this.tabCount = mTabCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = MainActivityFragment.newInstance(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return tabLayout.getTabCount();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        public Fragment getRegisteredFragment(int position){
            return registeredFragments.get(position);
        }
    }
}
