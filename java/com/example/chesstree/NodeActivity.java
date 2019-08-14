package com.example.chesstree;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.ArrayList;

public class NodeActivity extends AppCompatActivity {

    private ArrayList<String> children;
    private ArrayList<String> parents;
    private Node node;
    public int screenX;
    public int screenY;
    public float density;
    private String note;
    private ArrayList<String> tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenX = size.x;
        screenY = size.y;

        ArrayList<String> str = getIntent().getStringArrayListExtra("Node");
        String position = str.get(0);
        String move = str.get(1);
        children = new ArrayList<>();
        parents = new ArrayList<>();
        char m;
        if(move.equals("Black")) m = 'b';
        else m = 'w';

        for(int i=0; i<MainActivity.nodesList.size(); i++) {

            Node n = MainActivity.nodesList.get(i);
            if(n.equals(position, m)) {

                node = n;
                break;
            }
        }

        if(node == null) {

            Log.e("NodeActivity", "Node provided does not exist");
            throw new NullPointerException("Node does not exist");
        }

        int count = node.getChildren().size();

        for(int i=0; i<count; i++) {

            Node no = node.getChildren().get(i);
            children.add(node.getChildMove(no));
        }

        count = node.getParents().size();

        for(int i=0; i<count; i++) {

            Node no = node.getParents().get(i);
            parents.add(node.getParentMove(no));
        }

        initializeUI();
    }

    private class NoteWatcher implements TextWatcher {

        private NoteWatcher() {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            note = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            node.setNote(note);
        }
    }

    private class TagWatcher implements TextWatcher {

        private TagWatcher() {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            tags = new ArrayList<>();
            String l = s.toString();
            String[] str = l.split("\\s|,");

            for(int i=0; i<str.length; i++) {

                tags.add(str[i]);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

            node.setTags(tags);
        }
    }

    private class ChildrenListener implements View.OnClickListener {

        private String text;

        private ChildrenListener(String text) {

            this.text = text;
        }

        @Override
        public void onClick(View v) {

            Node child = NodeActivity.this.node.getChild(text);
            ArrayList<String> str = child.writeContentsToStringArray();
            Intent intent = new Intent(NodeActivity.this, NodeActivity.class);
            intent.putStringArrayListExtra("Node", str);
            NodeActivity.this.startActivity(intent);
        }
    }

    private class ParentsListener implements View.OnClickListener {

        private String text;

        private ParentsListener(String text) {

            this.text = text;
        }

        @Override
        public void onClick(View v) {

            Node parent = NodeActivity.this.node.getParent(text);
            ArrayList<String> str = parent.writeContentsToStringArray();
            Intent intent = new Intent(NodeActivity.this, NodeActivity.class);
            intent.putStringArrayListExtra("Node", str);
            NodeActivity.this.startActivity(intent);
        }
    }

    private void initializeUI() {

        AppBarLayout abl = findViewById(R.id.appbar);
        TextView tv1 = new TextView(this);
        tv1.setGravity(Gravity.CENTER_VERTICAL);
        tv1.setPadding(20,0,0,0);
        String text1 = "FEN: " + node.getPosition() + " " + node.getMove();
        tv1.setText(text1);
        tv1.setTextSize(14);
        tv1.setBackgroundColor(Color.parseColor("#fccb8b"));
        tv1.setTextColor(Color.parseColor("#000000"));
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("fen", node.getPosition()+" "+node.getMove()));
                Toast.makeText(NodeActivity.this, "FEN copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        abl.addView(tv1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(node.isBaseNode()) {

            ConstraintLayout cl = findViewById(R.id.view_notes);
            ConstraintLayout.LayoutParams cllp = (ConstraintLayout.LayoutParams) cl.getLayoutParams();
            cllp.startToEnd = R.id.view_children;
        }


        Button b1 = findViewById(R.id.blunder);
        Button b2 = findViewById(R.id.mistake);
        Button b3 = findViewById(R.id.dubious);
        Button b4 = findViewById(R.id.interesting);
        Button b5 = findViewById(R.id.good);
        Button b6 = findViewById(R.id.brilliant);
        Button b7 = findViewById(R.id.remove1);

        Button b8 = findViewById(R.id.equal);
        Button b9 = findViewById(R.id.slight_white);
        Button b10 = findViewById(R.id.slight_black);
        Button b11 = findViewById(R.id.clear_white);
        Button b12 = findViewById(R.id.clear_black);
        Button b13 = findViewById(R.id.decisive_white);
        Button b14 = findViewById(R.id.decisive_black);
        Button b15 = findViewById(R.id.unclear);
        Button b16 = findViewById(R.id.compensation);
        Button b17 = findViewById(R.id.remove2);

        Button b18 = findViewById(R.id.better);
        Button b19 = findViewById(R.id.only);
        Button b20 = findViewById(R.id.idea);
        Button b21 = findViewById(R.id.counter);
        Button b22 = findViewById(R.id.novelty);
        Button b23 = findViewById(R.id.remove3);

        Button b24 = findViewById(R.id.initiative);
        Button b25 = findViewById(R.id.attack);
        Button b26 = findViewById(R.id.counterplay);
        Button b27 = findViewById(R.id.development);
        Button b28 = findViewById(R.id.space);
        Button b29 = findViewById(R.id.time_trouble);
        Button b30 = findViewById(R.id.zugzwang);
        Button b31 = findViewById(R.id.remove4);

        LinearLayout linear1 = findViewById(R.id.view_children_layout);
        LinearLayout linear2 = findViewById(R.id.view_parents_layout);

        EditText et1 = findViewById(R.id.tags);
        EditText et2 = findViewById(R.id.notes);

        et1.addTextChangedListener(new TagWatcher());
        et2.addTextChangedListener(new NoteWatcher());

        StringBuilder tagdisplay = new StringBuilder();

        for(int i=0; i<node.getTags().size(); i++) {

            if(node.getTags().get(i).equals("")) continue;
            tagdisplay.append(node.getTags().get(i));
            tagdisplay.append(", ");
        }

        et1.setText(new String(tagdisplay));
        et2.setText(node.getNote());

        for(int i=0; i<children.size(); i++) {

            TextView tv = new TextView(this);
            String text = children.get(i);

            ChildrenListener cl = new ChildrenListener(text);
            tv.setOnClickListener(cl);

            text = "Child: " + text;
            tv.setText(text);
            tv.setMinHeight((int)(50*density));
            tv.setBackgroundResource(R.drawable.rectangle);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setPadding(50,0,50,0);
            View v = new View(this);
            v.setMinimumHeight(5);
            linear1.addView(tv);
        }

        for(int i=0; i<parents.size(); i++) {

            TextView tv = new TextView(this);
            String text = parents.get(i);

            ParentsListener pl = new ParentsListener(text);
            tv.setOnClickListener(pl);

            text = "Parent: " + text;
            tv.setText(text);
            tv.setMinHeight((int)(50*density));
            tv.setBackgroundResource(R.drawable.rectangle);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setPadding(50,0,50,0);
            View v = new View(this);
            v.setMinimumHeight(5);
            linear2.addView(tv);
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                node.setQuality(Node.MoveQuality.BLUNDER);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setQuality(Node.MoveQuality.MISTAKE);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setQuality(Node.MoveQuality.DUBIOUS);
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setQuality(Node.MoveQuality.INTERESTING);
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setQuality(Node.MoveQuality.GOOD);
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setQuality(Node.MoveQuality.BRILLIANT);
            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setQuality(Node.MoveQuality.EMPTY);
            }
        });

        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.EQUAL);
            }
        });

        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.SLIGHT_ADVANTAGE_WHITE);
            }
        });

        b10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.SLIGHT_ADVANTAGE_BLACK);
            }
        });

        b11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.CLEAR_ADVANTAGE_WHITE);
            }
        });

        b12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.CLEAR_ADVANTAGE_BLACK);
            }
        });

        b13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.DECISIVE_ADVANTAGE_WHITE);
            }
        });

        b14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.DECISIVE_ADVANTAGE_BLACK);
            }
        });

        b15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.UNCLEAR);
            }
        });

        b16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.COMPENSATION);
            }
        });

        b17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setAnalysis(Node.MoveAnalysis.EMPTY);
            }
        });

        b18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setMoveOther(Node.MoveOther.BETTER);
            }
        });

        b19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setMoveOther(Node.MoveOther.ONLY);
            }
        });

        b20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setMoveOther(Node.MoveOther.IDEA);
            }
        });

        b21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setMoveOther(Node.MoveOther.COUNTER);
            }
        });

        b22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setMoveOther(Node.MoveOther.NOVELTY);
            }
        });

        b23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setMoveOther(Node.MoveOther.EMPTY);
            }
        });

        b24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.INITIATIVE);
            }
        });

        b25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.ATTACK);
            }
        });

        b26.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.COUNTERPLAY);
            }
        });

        b27.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.DEVELOPMENT);
            }
        });

        b28.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.SPACE);
            }
        });

        b29.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.TIME_TROUBLE);
            }
        });

        b30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.ZUGZWANG);
            }
        });

        b31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node.setPositionOther(Node.PositionOther.EMPTY);
            }
        });
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.node, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sendboard) {

            Intent intent = new Intent(this, ChessBoardActivity.class);
            ArrayList<String> str = node.writeContentsToStringArray();
            intent.putStringArrayListExtra("Node", str);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}