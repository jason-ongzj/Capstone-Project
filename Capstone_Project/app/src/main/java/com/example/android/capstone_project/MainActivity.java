package com.example.android.capstone_project;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DataInterface{

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.listView) ListView listView;
    @Nullable
    @BindView(R.id.spinner) Spinner spinner;

    private String[] spinnerItems = new String[] {"All", "Business", "Entertainment", "Gaming", "General",
            "Music", "Politics","Science-and-Nature", "Sport", "Technology"};

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private Context mContext;
    private Menu navMenu;
    public static final String TAG = "MainActivity";

    private String spinnerSelection = "all";
    private boolean itemSelected = false;

    private static final int ID_TOP_ARTICLES_LOADER = 156;
    private static final int ID_LATEST_ARTICLES_LOADER = 249;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mContext = this;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navMenu = navigationView.getMenu();

        tabLayout.addTab(tabLayout.newTab().setText("Top"));
        tabLayout.addTab(tabLayout.newTab().setText("Latest"));

        mPagerAdapter = new MyPagerAdapter(getFragmentManager(), tabLayout.getTabCount());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

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

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);

        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if(itemSelected) {
                    spinnerSelection = adapterView.getItemAtPosition(pos).toString().toLowerCase();
                    Toast.makeText(MainActivity.this, spinnerSelection, Toast.LENGTH_SHORT).show();


                    Fragment fragment_1 = mPagerAdapter.getRegisteredFragment(0);
                    Fragment fragment_2 = mPagerAdapter.getRegisteredFragment(1);
                    if (fragment_1 instanceof MainActivityFragment) {
                        ((MainActivityFragment) fragment_1).restartLoader(ID_TOP_ARTICLES_LOADER, spinnerSelection);
                    }
                    if(fragment_2 instanceof MainActivityFragment) {
                        ((MainActivityFragment) fragment_2).restartLoader(ID_LATEST_ARTICLES_LOADER, spinnerSelection);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if(itemSelected) {
                    Fragment fragment_1 = mPagerAdapter.getRegisteredFragment(0);
                    Fragment fragment_2 = mPagerAdapter.getRegisteredFragment(1);
                    if (fragment_1 instanceof MainActivityFragment) {
                        ((MainActivityFragment) fragment_1).restartLoader(ID_TOP_ARTICLES_LOADER, spinnerSelection);
                    }
                    if(fragment_2 instanceof MainActivityFragment) {
                        ((MainActivityFragment) fragment_2).restartLoader(ID_LATEST_ARTICLES_LOADER, spinnerSelection);
                    }
                }
            }
        });
    }

    @Override
    public Menu getMenu() {
        return navMenu;
    }

    @Override
    public ListView getListView() {
        return listView;
    }

    public void setItemSelectedTrue(){
        itemSelected = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public String getSpinnerSelection(){
        return spinnerSelection;
    }

    public Spinner getSpinner(){
        return spinner;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void closeDrawer() {
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
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
