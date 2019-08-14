package com.example.chesstree;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class SaveActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public int screenX;
    public int screenY;
    public float density;

    private TextListener tl;
    private String s;

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

        setContentView(R.layout.activity_save);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        populate();
    }

    private void populate() {

        Button button = findViewById(R.id.save_button);
        EditText editText = findViewById(R.id.save_as);

        tl = new TextListener();
        editText.addTextChangedListener(tl);

        ButtonListener buttonListener = new ButtonListener();
        button.setOnClickListener(buttonListener);

        repopulate();
    }

    private void repopulate() {

        LinearLayout linearLayout = findViewById(R.id.load_viewer);
        int count = fileList().length;
        linearLayout.removeAllViews();

        for(int i=1; i<count; i++) {

            TextView tv = new TextView(this);
            tv.setText(fileList()[i]);
            tv.setTextSize(18);
            tv.setBackgroundResource(R.drawable.rectangle);
            tv.setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(50*density)));
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setPadding(50,0,0,0);
            LoadListener loadListener = new LoadListener();
            tv.setOnClickListener(loadListener);
            ConstraintLayout ll = new ConstraintLayout(this);
            ll.addView(tv);
            ImageButton ib = new ImageButton(this);
            ib.setImageResource(android.R.drawable.ic_menu_delete);
            ib.setBackgroundColor(Color.parseColor("#49628a"));
            ib.setOnClickListener(new DeleteSaveListener(tv));
            ib.setLayoutParams(new android.view.ViewGroup.LayoutParams((int)(30*density), (int)(30*density)));
            ib.setTranslationX(screenX-50*density);
            ib.setTranslationY(10*density);
            ll.addView(ib);
            linearLayout.addView(ll);
            View padding = new View(this);
            padding.setMinimumHeight(5);
            linearLayout.addView(padding);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        EditText editText = findViewById(R.id.save_as);
        editText.removeTextChangedListener(tl);
    }

    private class DeleteSaveListener implements View.OnClickListener {

        private TextView textView;
        private String text;

        private DeleteSaveListener(TextView textView) {

            this.textView = textView;
            text = textView.getText().toString();
        }

        @Override
        public void onClick(View v) {

            AlertDialog.Builder adb = new AlertDialog.Builder(SaveActivity.this);
            adb.setCancelable(false);
            adb.setTitle("Delete Save File");
            adb.setMessage("Are you sure you want to delete this file? This may cause the tree to be inaccessible or unrecoverable.");

            adb.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    SaveActivity.this.deleteFile(text);
                    Toast.makeText(SaveActivity.this, "\"" + text + "\" deleted.", Toast.LENGTH_SHORT).show();
                    Log.println(Log.ASSERT, "DeleteFile", "The file named \"" + text + "\" was deleted.");
                    SaveActivity.this.repopulate();
                }
            });

            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });

            adb.show();
        }
    }

    private class TextListener implements TextWatcher {

        private TextListener() {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            SaveActivity.this.s = s.toString();
            Log.v("EditText", "Text changed to " + SaveActivity.this.s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class ButtonListener implements View.OnClickListener {

        private String text;
        boolean confirm_needed = false;
        boolean confirmed = false;

        private ButtonListener() {}

        @Override
        public void onClick(View v) {

            text = SaveActivity.this.s;
            if(text.length() == 0) {

                Toast.makeText(SaveActivity.this, "Name is too short", Toast.LENGTH_SHORT).show();
                return;
            }

            int count = fileList().length;
            boolean name_collision = false;

            for(int i=0; i<count; i++) {

                if(text.equals(fileList()[i])) name_collision = true;
            }

            if(name_collision) {

                confirm_needed = true;
                displayConfirmDialog();
            }

            if(confirm_needed && !confirmed) {

                confirm_needed = false;
                return;
            }

            String fileContentsAsString;
            JSONArray fileContents = new JSONArray();
            FileOutputStream fos;

            for(int i=0; i<MainActivity.nodesList.size(); i++) {

                Node n = MainActivity.nodesList.get(i);
                if(n.isBaseNode()) {
                    fileContents = generatefileContents(n, fileContents);
                    break;
                }
            }

            fileContentsAsString = fileContents.toString();

            try {

                fos = openFileOutput(text, Context.MODE_PRIVATE);
                fos.write(fileContentsAsString.getBytes());
                fos.close();
            }
            catch (Exception e) {

                e.printStackTrace();
            }

            Toast.makeText(SaveActivity.this, "Save successful!", Toast.LENGTH_SHORT).show();
            Log.i("SaveFile", "A file named \"" + text + "\" was saved to the list of trees.");
            SaveActivity.this.repopulate();
        }

        private void displayConfirmDialog() {

            SaveActivity context = SaveActivity.this;

            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setCancelable(false);
            adb.setTitle("Confirm Overwrite");
            adb.setMessage("Are you sure you want to overwrite the file named " + text + "?");

            adb.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ButtonListener.this.confirmed = true;
                }
            });

            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            adb.show();
        }

        private JSONArray generatefileContents(Node n, JSONArray ja) {

            JSONArray currentNode = new JSONArray();

            currentNode = currentNode.put(n.getPosition());
            currentNode = currentNode.put(n.getMove());
            currentNode = currentNode.put(n.isBaseNode());
            currentNode = currentNode.put(n.getNote());
            currentNode = currentNode.put(n.getQuality());
            currentNode = currentNode.put(n.getMoveOther());
            currentNode = currentNode.put(n.getAnalysis());
            currentNode = currentNode.put(n.getPositionOther());
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
    }

    private class LoadListener implements View.OnClickListener {

        private boolean confirmed = false;

        private LoadListener() {}

        @Override
        public void onClick(View v) {

            displayConfirmDialog();
            if(!confirmed) return;

            MainActivity.nodesList = new ArrayList<>();
            FileInputStream fileInputStream;
            JSONArray fileContents;

            try {
                fileInputStream = openFileInput(new String(new StringBuilder(((TextView)v).getText())));
                int content;
                StringBuilder sb = new StringBuilder();

                while((content = fileInputStream.read()) != -1) {

                    sb.append((char)content);
                }

                fileContents = new JSONArray(new String(sb));
            }
            catch (Exception e) {

                Log.i("LoadListener", e.toString());
                return;
            }

            new Node(fileContents, MainActivity.nodesList);
            NodeView.dim = 34*density;
        }

        private void displayConfirmDialog() {

            SaveActivity context = SaveActivity.this;

            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setCancelable(false);
            adb.setTitle("Confirm Load");
            adb.setMessage("Are you sure you want to load a new tree? Any existing unsaved work may be lost.");

            adb.setPositiveButton("Load", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LoadListener.this.confirmed = true;
                }
            });

            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            adb.show();
        }
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
        getMenuInflater().inflate(R.menu.save, menu);
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

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_chessboard) {

            Intent intent = new Intent(this, ChessBoardActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_analysis) {

            Intent intent = new Intent(this, AnalysisActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_save) {

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