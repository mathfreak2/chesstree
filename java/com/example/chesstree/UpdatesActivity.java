package com.example.chesstree;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.chesstree.ui.main.SectionsPagerAdapter;
import com.example.chesstree.ui.main.UpdatesFragment;

public class UpdatesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_chessboard) {

            Intent intent = new Intent(this, ChessBoardActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_analysis) {

            Intent intent = new Intent(this, AnalysisActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_save) {

            Intent intent = new Intent(this, SaveActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_importexport) {

            Intent intent = new Intent(this, ImportExportActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_setting) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.updates) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}