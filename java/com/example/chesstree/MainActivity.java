package com.example.chesstree;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {

    public int screenX;
    public int screenY;
    public float density;
    public static ArrayList<Node> nodesList = new ArrayList<>();
    public static String autosavefilename = "autosave";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenX = size.x;
        screenY = size.y;

        loadConfig();
        if(nodesList.isEmpty()) loadAutoSave();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if(nodesList.isEmpty()) {

            Node baseNode = new Node();
            nodesList.add(baseNode);
            baseNode.display(this, 0, "", null);
        }
        else displayNodes();
    }

    public void displayNodes() {

        for(int i=0; i<nodesList.size(); i++) {

            Node n = nodesList.get(i);
            if(n.isBaseNode()) n.display(this, 0, "", null);
        }
    }

    @Override
    protected void onStop() {

        super.onStop();

        for(int i=0; i<nodesList.size(); i++) {

            Node n = nodesList.get(i);
        }

        autoSave();
    }

    private void loadConfig() {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        SettingsActivity.TWO_COLUMN = sharedPreferences.getBoolean("twocolumn", true);
    }

    public void autoSave() {

        if(nodesList.size() == 0) return;

        Log.i("SaveFile", "Auto-saving tree");

        String fileContentsAsString;
        JSONArray fileContents = new JSONArray();
        FileOutputStream fos;

        for(int i=0; i<nodesList.size(); i++) {

            Node n = nodesList.get(i);
            if(n.isBaseNode()) {
                fileContents = generatefileContents(n, fileContents);
                break;
            }
        }

        fileContentsAsString = fileContents.toString();

        try {

            fos = openFileOutput(autosavefilename, Context.MODE_PRIVATE);
            fos.write(fileContentsAsString.getBytes());
            fos.close();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    private JSONArray generatefileContents(Node n, JSONArray ja) {

        JSONArray currentNode = new JSONArray();

        currentNode = currentNode.put(n.getPosition());
        currentNode = currentNode.put(n.getMove());
        currentNode = currentNode.put(n.isBaseNode());
        currentNode = currentNode.put(n.getNote());
        currentNode = currentNode.put(Node.MoveQuality.getNumber(n.getQuality()));
        currentNode = currentNode.put(Node.MoveOther.getNumber(n.getMoveOther()));
        currentNode = currentNode.put(Node.MoveAnalysis.getNumber(n.getAnalysis()));
        currentNode = currentNode.put(Node.PositionOther.getNumber(n.getPositionOther()));
        currentNode = currentNode.put(n.getExpanded());

        StringBuilder tags = new StringBuilder();

        for(int i=0; i<n.getTags().size(); i++) {

            tags.append(n.getTags().get(i));
            tags.append(",");
        }

        currentNode = currentNode.put(tags);

        for(int i=0; i<n.getChildren().size(); i++) {

            Node child = n.getChildren().get(i);
            currentNode = currentNode.put(n.getChildMove(child));

            currentNode = currentNode.put(generatefileContents(child, ja));
        }

        return currentNode;
    }

    private void loadAutoSave() {

        FileInputStream fileInputStream;
        JSONArray fileContents;

        try {
            fileInputStream = openFileInput(autosavefilename);
            int content;
            StringBuilder sb = new StringBuilder();

            while((content = fileInputStream.read()) != -1) {

                sb.append((char)content);
            }

            fileContents = new JSONArray(new String(sb));
        }
        catch (Exception e) {

            Log.i("MainActivity", e.toString());
            return;
        }

        new Node(fileContents, nodesList);
        NodeView.dim = 34*density;
    }

    public static int getNodeIndex(Node n) {

        for(int i=0; i<nodesList.size(); i++) {

            Node a = nodesList.get(i);
            if(n.equals(a)) return i;
        }

        return -1;
    }

    public static Node addNode(Node n) {

        int index = getNodeIndex(n);
        if(index == -1) {

            nodesList.add(n);
            return n;
        }
        else return nodesList.get(index);
    }

    public static Node findNode(String pos, char m) {

        for(int i=0; i<nodesList.size(); i++) {

            if(nodesList.get(i).equals(pos, m)) return nodesList.get(i);
        }

        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        //TODO

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        //TODO
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

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

            Intent intent = new Intent(this, UpdatesActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}