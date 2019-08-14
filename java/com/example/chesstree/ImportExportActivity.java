package com.example.chesstree;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Stack;

public class ImportExportActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public int screenX;
    public int screenY;
    public float density;
    public static boolean importerror = false;
    private String importString = "";

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

        setContentView(R.layout.activity_import_export);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        initializeUI();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
            if(intent.hasExtra("PGN")) {

                ArrayList<String> pgn = intent.getStringArrayListExtra("PGN");
                EditText et = findViewById(R.id.import_export);
                StringBuilder text = new StringBuilder();
                int count = 0;
                boolean turn = true;

                for(int i=0; i<pgn.size(); i++) {

                    for(int j=0; j<pgn.size(); j++) {

                        if(pgn.get(j).contains("Base") && count == 0) {
                            text.append(pgn.get(j));
                            text.append(":\n");
                            count++;
                            continue;
                        }
                        if(pgn.get(j).contains(count+".") && turn) {

                            String move = pgn.get(j).substring(1,pgn.get(j).indexOf(","));
                            if(move.contains("...")) continue;
                            turn = false;
                            text.append(move);
                            text.append(" ");
                            continue;
                        }
                        if(pgn.get(j).contains(count+"...") && !turn) {

                            String move = pgn.get(j).substring(1,pgn.get(j).indexOf(","));
                            move = move.substring(move.indexOf("...")+3);
                            count++;
                            turn = true;
                            text.append(move);
                            text.append(" ");
                        }
                    }
                }

                et.setText(new String(text));
            }
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {

            EditText et = findViewById(R.id.import_export);
            et.setText(sharedText);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        FileInputStream fileInputStream;
        JSONArray fileContents;

        try {
            fileInputStream = openFileInput("autosave");
            int content;
            StringBuilder sb = new StringBuilder();

            while((content = fileInputStream.read()) != -1) {

                sb.append((char)content);
            }

            fileContents = new JSONArray(new String(sb));
        }
        catch (Exception e) {

            Log.i("ImportActivity", e.toString());
            return;
        }

        new Node(fileContents, MainActivity.nodesList);
        NodeView.dim = 34*density;
    }

    @Override
    protected void onStop() {

        super.onStop();

        Log.i("SaveFile", "Auto-saving tree");

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

            fos = openFileOutput("autosave", Context.MODE_PRIVATE);
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

    private void initializeUI() {

        Button export = findViewById(R.id.export);
        Button imp = findViewById(R.id.imp);
        ImageButton copy = findViewById(R.id.copy);
        EditText et = findViewById(R.id.import_export);

        export.setOnClickListener(new ExportListener(et));
        imp.setOnClickListener(new ImportButtonListener());
        copy.setOnClickListener(new CopyListener());
        et.setOnLongClickListener(new ImportListener());
        et.addTextChangedListener(new ImportTextWatcher());

    }

    private class ImportTextWatcher implements TextWatcher {

        private ImportTextWatcher() {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            importString = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class ExportListener implements View.OnClickListener {

        private EditText editText;

        private ExportListener(EditText editText) {

            this.editText = editText;
        }

        @Override
        public void onClick(View v) {

            int count = fileList().length;

            PopupMenu pm = new PopupMenu(ImportExportActivity.this, v);
            pm.getMenu().add("this tree");

            for(int i=1; i<count; i++) {

                pm.getMenu().add(fileList()[i]);
            }

            PMListener pml = new PMListener();

            pm.setOnMenuItemClickListener(pml);
            pm.show();
        }

        private class PMListener implements PopupMenu.OnMenuItemClickListener {

            private PMListener() {

            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getTitle().equals("this tree")) {

                    String text = ImportExportActivity.this.Export(MainActivity.nodesList);
                    editText.setText(text);
                    return true;
                }
                else {

                    String filename = "";

                    for(int i=1; i<fileList().length; i++) {

                        if(item.getTitle().toString().equals(fileList()[i])) {

                            filename = fileList()[i];
                            break;
                        }
                    }

                    if(filename.equals("")) {

                        Log.wtf("ExportTree", "Could not find selected file in the file list");
                        return true;
                    }

                    ArrayList<Node> exporting = new ArrayList<>();
                    FileInputStream fileInputStream;
                    JSONArray fileContents;

                    try {
                        fileInputStream = openFileInput(filename);
                        int content;
                        StringBuilder sb = new StringBuilder();

                        while((content = fileInputStream.read()) != -1) {

                            sb.append((char)content);
                        }

                        fileContents = new JSONArray(new String(sb));
                    }
                    catch (Exception e) {

                        Log.wtf("ExportTree", e.toString() + ": Could not find selected file in the file list");
                        return true;
                    }

                    new Node(fileContents, exporting);
                    String text = ImportExportActivity.this.Export(exporting);
                    editText.setText(text);
                    return true;
                }
            }
        }
    }

    private class ImportButtonListener implements View.OnClickListener {

        private ImportButtonListener() {}

        @Override
        public void onClick(View v) {

            ImportExportActivity.this.Import(importString);
            importerror = false;
            Log.i("SaveFile", "Auto-saving tree");

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

                fos = openFileOutput("autosave", Context.MODE_PRIVATE);
                fos.write(fileContentsAsString.getBytes());
                fos.close();
            }
            catch (Exception e) {

                e.printStackTrace();
            }

            Toast.makeText(ImportExportActivity.this, "Game added to current tree!", Toast.LENGTH_SHORT).show();
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
    }

    private class CopyListener implements View.OnClickListener {

        private CopyListener() {}

        @Override
        public void onClick(View v) {

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("exported game", importString));
            Toast.makeText(ImportExportActivity.this, "Text copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }

    private class ImportListener implements View.OnLongClickListener {

        private ImportListener() {

        }

        @Override
        public boolean onLongClick(View v) {

            PopupMenu pm = new PopupMenu(ImportExportActivity.this, v);
            pm.getMenu().add("Paste");

            PMListener pml = new PMListener(v);

            pm.setOnMenuItemClickListener(pml);
            pm.show();

            return true;
        }

        private class PMListener implements PopupMenu.OnMenuItemClickListener {

            private EditText editText;

            private PMListener(View v) {

                editText = (EditText) v;
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                try {
                    editText.setText(clipboard.getPrimaryClip().getItemAt(0).getText().toString());
                } catch(NullPointerException npe) {

                    Log.i("ClipBoard", "Nothing is on the clipboard");
                    return true;
                }

                return true;
            }
        }
    }

    private void Import(String s) {

        s += " ";

        ArrayList<Piece> bs = new ArrayList<>(64);
        String boardstate = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        char m = 'w';
        String en_passant = "-";
        String castles = "KQkq";
        int half_moves = 0;
        int turn = 1;
        String fen = boardstate + " " + m + " " + castles + " " + en_passant + " " + half_moves + " " + turn;

        // Initialize starting board state
        bs.add(new Rook(false, 8, 1));
        bs.add(new Knight(false, 8, 2));
        bs.add(new Bishop(false, 8, 3));
        bs.add(new Queen(false, 8, 4));
        bs.add(new King(false, 8, 5));
        bs.add(new Bishop(false, 8, 6));
        bs.add(new Knight(false, 8, 7));
        bs.add(new Rook(false, 8, 8));
        for(int i=0; i<8; i++) bs.add(new Pawn(false, 7, i+1));
        for(int i=0; i<32; i++) bs.add(null);
        for(int i=0; i<8; i++) bs.add(new Pawn(true, 2, i+1));
        bs.add(new Rook(true, 1, 1));
        bs.add(new Knight(true, 1, 2));
        bs.add(new Bishop(true, 1, 3));
        bs.add(new Queen(true, 1, 4));
        bs.add(new King(true, 1, 5));
        bs.add(new Bishop(true, 1, 6));
        bs.add(new Knight(true, 1, 7));
        bs.add(new Rook(true, 1, 8));

        // sets default value for loop uses to determine whether or not to ignore
        // a part of the given imported text
        boolean ignore = false;

        // Tells the loop whether or not it is currently reading a move notation
        boolean moving = false;

        // Tells the loop whether or not a note is being taken and stores the note in the note String
        // If the first move has not been written, though, it will not record the note
        boolean start = false;
        boolean n = false;
        StringBuilder note = new StringBuilder();
        Node f = new Node();

        // In the case of branching in an imported file, this allows the loop to restore a boardstate after
        // having included a branch of the main line
        Stack<ArrayList<Piece>> alternate_board_states = new Stack<>();
        Stack<String> alternate_fens = new Stack<>();

        String move = "";

        for(int i=0; i<s.length(); i++) {

            if(ignore && s.charAt(i) != ']') continue;
            if(ignore && s.charAt(i) == ']') {ignore = false; continue;}
            if(s.charAt(i) == '[') {ignore = true; continue;}

            if(s.charAt(i) == '{' && start) {n = true; continue;}
            if(s.charAt(i) == '}' && start) {

                f.setNote(new String(note));
                note = new StringBuilder();
                n = false;
                continue;
            }

            if(n && start) {

                note.append(s.charAt(i));
                continue;
            }

            if(s.charAt(i) == '(') {

                alternate_board_states.push(bs);
                alternate_fens.push(fen);
                continue;
            }

            if(s.charAt(i) == ')') {

                bs = alternate_board_states.pop();
                fen = alternate_fens.pop();
                Log.i("BackToFen", fen);
                continue;
            }

            switch(s.charAt(i)) {

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                    if(!moving) continue;
                    break;
                case '.':
                    moving = true;
                    continue;
                case 'N':
                case 'B':
                case 'R':
                case 'O':
                case 'Q':
                case 'K':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                    moving = true;
            }

            if(s.charAt(i) == ' ' && moving && move.equals("")) continue;

            if(s.charAt(i) == ' ' && moving) {

                boardstate = fen.split(" ")[0];
                m = fen.split(" ")[1].charAt(0);

                fen = readMove(bs, fen, move, findViewById(R.id.import_export));
                if(importerror) {

                    Log.e("ImportError", fen);
                    return;
                }

                // Adds a node to the main activity with the current position and move
                Node parent = new Node();
                for(int k=0; k<MainActivity.nodesList.size(); k++) {

                    Node j = MainActivity.nodesList.get(k);
                    if(j.equals(boardstate, m)) parent = j;
                }

                f = new Node(fen.split(" ")[0], parent, move);
                MainActivity.addNode(f);
                Log.i("NodeCreation", "Node added from import: " + move);

                move = "";
                moving = false;
                start = true;
                continue;
            }

            if(moving) {

                switch(s.charAt(i)) {

                    case 'R':
                    case 'N':
                    case 'B':
                    case 'Q':
                    case 'K':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'g':
                    case 'h':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '=':
                    case '+':
                    case '#':
                    case 'O':
                    case '-':
                    case 'x':
                        move += s.charAt(i);
                }
            }
        }
    }

    public static String readMove(ArrayList<Piece> bs, String fen, String s, View v) {

        String move = fen.split(" ")[1];
        boolean isWhiteTurn = true;
        if(move.equals("b")) isWhiteTurn = false;
        int rank = 0, piecerank = 0;
        char piecetype = ' ';
        int countlower = 0; int countnumber = 0;
        char lower1 = 'z', lower2 = 'z';
        int number1 = 0, number2 = 0;
        String castles = fen.split(" ")[2];

        // Read castling moves
        if(s.contains("O")) {

            if(s.contains("O-O-O")) {

                // Queen's castle black
                if(!isWhiteTurn && castles.contains("q")) {

                    try {

                        return changeBoardState(bs, fen, bs.get(4), 8, 3);
                    }
                    catch(NullPointerException npe) {

                        Snackbar.make(v, "PGN not recognized at " + s, Snackbar.LENGTH_LONG);
                        Log.e("ReadImportedMove", "498: PGN not recognized at " + s);
                        importerror = true;
                        return ("PGN not recognized at " + s);
                    }
                }

                // Queen's castle white
                if(isWhiteTurn && castles.contains("Q")) {

                    try {

                        return changeBoardState(bs, fen, bs.get(60), 1, 3);
                    }
                    catch(NullPointerException npe) {

                        Snackbar.make(v, "PGN not recognized at " + s, Snackbar.LENGTH_LONG);
                        Log.e("ReadImportedMove", "514: PGN not recognized at " + s);
                        importerror = true;
                        return ("PGN not recognized at " + s);
                    }
                }
            }

            if(s.contains("O-O")) {

                // King's castle black
                if(!isWhiteTurn && castles.contains("k")) {

                    try {

                        return changeBoardState(bs, fen, bs.get(4), 8, 7);
                    }
                    catch(NullPointerException npe) {

                        Snackbar.make(v, "PGN not recognized at " + s, Snackbar.LENGTH_LONG);
                        Log.e("ReadImportedMove", "533: PGN not recognized at " + s);
                        importerror = true;
                        return ("PGN not recognized at " + s);
                    }
                }

                // King's castle white
                if(isWhiteTurn && castles.contains("K")) {

                    try {

                        return changeBoardState(bs, fen, bs.get(60), 1, 7);
                    }
                    catch(NullPointerException npe) {

                        Snackbar.make(v, "PGN not recognized at " + s, Snackbar.LENGTH_LONG);
                        Log.e("ReadImportedMove", "549: PGN not recognized at " + s);
                        importerror = true;
                        return ("PGN not recognized at " + s);
                    }
                }

                Snackbar.make(v, "PGN not recognized at " + s, Snackbar.LENGTH_LONG);
                Log.e("ReadImportedMove", "556: PGN not recognized at " + s);
                importerror = true;
                return ("PGN not recognized at " + s);
            }
        }

        for(int i=0; i<s.length(); i++) {

            switch(s.charAt(i)) {

                case 'R':
                    piecetype = 'R';
                    break;
                case 'B':
                    piecetype = 'B';
                    break;
                case 'N':
                    piecetype = 'N';
                    break;
                case 'Q':
                    piecetype = 'Q';
                    break;
                case 'K':
                    piecetype = 'K';
                    break;
                case 'a':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'a';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'a';
                        if(countlower == 2) lower2 = 'a';
                        break;
                    }
                case 'b':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'b';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'b';
                        if(countlower == 2) lower2 = 'b';
                        break;
                    }
                case 'c':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'c';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'c';
                        if(countlower == 2) lower2 = 'c';
                        break;
                    }
                case 'd':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'd';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'd';
                        if(countlower == 2) lower2 = 'd';
                        break;
                    }
                case 'e':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'e';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'e';
                        if(countlower == 2) lower2 = 'e';
                        break;
                    }
                case 'f':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'f';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'f';
                        if(countlower == 2) lower2 = 'f';
                        break;
                    }
                case 'g':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'g';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'g';
                        if(countlower == 2) lower2 = 'g';
                        break;
                    }
                case 'h':
                    if(s.contains("x") && lower1 == 'z' && piecetype == ' ') {

                        piecetype = 'P';
                        countlower++;
                        lower1 = 'h';
                        break;
                    } else {

                        if(piecetype == ' ') piecetype = 'P';
                        countlower++;
                        if(countlower == 1) lower1 = 'h';
                        if(countlower == 2) lower2 = 'h';
                        break;
                    }
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                    countnumber++;
                    if(countnumber == 1) number1 = Character.getNumericValue(s.charAt(i));
                    if(countnumber == 2) number2 = Character.getNumericValue(s.charAt(i));

            }
        }

        if(piecetype == ' ' || lower1 == 'z' || number1 == 0) {

            Snackbar.make(v, "PGN not recognized at " + s, Snackbar.LENGTH_LONG);
            Log.e("ReadImportedMove", "711: PGN not recognized at " + s);
            importerror = true;
            return ("PGN not recognized at " + s);
        }

        int piececolumn = 0, column = 0;

        if(lower2 != 'z') {

            piececolumn = ChessBoardActivity.COLUMN_CODE.get(lower1);
            column = ChessBoardActivity.COLUMN_CODE.get(lower2);
        }
        else column = ChessBoardActivity.COLUMN_CODE.get(lower1);

        if(number2 != 0) {

            piecerank = number1;
            rank = number2;
        }
        else rank = number1;

        for (int i=0;i<bs.size();i++) {

            Piece p = bs.get(i);

            if(p == null) continue;
            if(p.getColor() != isWhiteTurn) continue;
            if (p.getPieceType() != piecetype) continue;
            if(!p.canMoveTo(bs, fen, rank, column)) continue;
            if(piecerank != 0 && p.getRank() != piecerank) continue;
            if(piececolumn != 0 && p.getColumn() != piececolumn) continue;

            if(s.contains("=")) {

                if(s.contains("N")) return changeBoardState(bs, fen, p, rank, column, 1);
                if(s.contains("B")) return changeBoardState(bs, fen, p, rank, column, 2);
                if(s.contains("R")) return changeBoardState(bs, fen, p, rank, column, 3);
            }

            return changeBoardState(bs, fen, p, rank, column);
        }

        // In the case where the program finds no viable pieces, log an error and display to screen and
        // stop recording moves from the imported text
        Snackbar.make(v, "PGN not recognized at " + s, Snackbar.LENGTH_LONG);
        Log.e("ReadImportedMove", "756: PGN not recognized at " + s);
        importerror = true;
        return ("PGN not recognized at " + s);
    }

    public static String changeBoardState(ArrayList<Piece> bs, String fen, Piece p, int rank, int column) {

        return changeBoardState(bs, fen, p, rank, column, 0);
    }

    public static String changeBoardState(ArrayList<Piece> bs, String fen, Piece p, int rank, int column, int promotion) {

        char move;
        if(fen.split(" ")[1].equals("b")) move = 'b';
        else move = 'w';

        String castles = fen.split(" ")[2];

        String en_passant = fen.split(" ")[3];

        String half = fen.split(" ")[4];
        int half_moves_from_pawn_capture = 0;

        for(int i=0; i<half.length(); i++)
            half_moves_from_pawn_capture += Math.pow(10, half.length()-i-1)*Character.getNumericValue(half.charAt(i));

        int turns = 0;
        for(int i=0; i<fen.split(" ")[5].length(); i++)
            turns += Math.pow(10, fen.split(" ")[5].length()-i-1)*Character.getNumericValue(fen.split(" ")[5].charAt(i));

        if(bs.get(8*(8-rank)+column-1) != null && bs.get(8*(8-rank)+column-1).getPieceType() == 'P')
            half_moves_from_pawn_capture = 0;
        else half_moves_from_pawn_capture++;

        // Check special rules for pawns, mainly en passant and promotions
        if(p.getPieceType() == 'P') {

            // If a white pawn moves in such a way to make en passant possible, record
            // where en passant is possible in the en passant variable.
            if(p.getColor() && p.getRank() + 2 == rank) en_passant = "" + ChessBoardActivity.INVERSE_COLUMN_CODE.get(p.getColumn()) + (rank-1);

            // If a black pawn moves in such a way to make en passant possible, record
            // where en passant is possible in the en passant variable.
            if(!p.getColor() && p.getRank() - 2 == rank) en_passant = "" + ChessBoardActivity.INVERSE_COLUMN_CODE.get(p.getColumn()) + (rank+1);

            // If a pawn moved but not in such a way to make en passant possible, record
            // the lack of an en passant possibility in the en passant variable
            if(!(p.getColor() && p.getRank() + 2 == rank) && !(!p.getColor() && p.getRank() - 2 == rank)) en_passant = "-";

            // Remove captured pawn if captured through en passant
            if(p.getColumn() + 1 == column || p.getColumn() - 1 == column) {

                if(p.getRank() + 1 == rank && p.getColor() && bs.get(8*(8-rank)+column-1) == null)
                    bs.set(8*(9-rank)+column-1, null);

                if(p.getRank() - 1 == rank && !p.getColor() && bs.get(8*(8-rank)+column-1) == null)
                    bs.set(8*(7-rank)+column-1, null);
            }

            // Deals with pawn promotions
            if((p.getColor() && rank == 8) || (!p.getColor() && rank == 1)) {

                if(promotion == 1) {

                    p = new Knight(p.getColor(), p.getRank(), p.getColumn());
                } else if(promotion == 2) {

                    p = new Bishop(p.getColor(), p.getRank(), p.getColumn());
                } else if(promotion == 3) {

                    p = new Rook(p.getColor(), p.getRank(), p.getColumn());
                } else {

                    p = new Queen(p.getColor(), p.getRank(), p.getColumn());
                }

                bs.set(8*(8-p.getRank())+p.getColumn()-1, p);
            }
        }
        // If it was not a pawn that moved, record the lack of en passant
        // possibility in the en passant variable
        else en_passant = "-";

            // Flips move variable to keep track of whose turn it is
            if(move == 'w') move = 'b';
            else {move = 'w'; turns++;}

        //Remove corresponding castling opportunity if rook is taken
        if(castles.contains("K") || castles.contains("Q") || castles.contains("k") || castles.contains("q")) {

            Piece r = bs.get(8*(8-rank)+column-1);

            if(r != null) {

                if(r.getPieceType() == 'R') {

                    if(castles.contains("K") && rank == 1 && column == 8)
                        castles = castles.replace("K", "");
                    if(castles.contains("Q") && rank == 1 && column == 1)
                        castles = castles.replace("Q", "");
                    if(castles.contains("k") && rank == 8 && column == 8)
                        castles = castles.replace("k", "");
                    if(castles.contains("q") && rank == 8 && column == 1)
                        castles = castles.replace("q", "");
                }
            }
        }

        // Update board state
        bs.set(8*(8-p.getRank())+p.getColumn()-1, null);
        bs.set(8*(8-rank)+column-1, p);

        // If a king moves, remove all corresponding castling opportunities from that
        // king's player if such castling opportunities still exist.
        if(p.getPieceType() == 'K') {
            if(p.getColor()) {
                if(castles.contains("K")) castles = castles.replace("K", "");
                if(castles.contains("Q")) castles = castles.replace("Q", "");
            }
            else {
                if(castles.contains("k")) castles = castles.replace("k", "");
                if(castles.contains("q")) castles = castles.replace("q", "");
            }
        }

        // Kingside castle
        if(p.getPieceType() == 'K' && p.getColumn()+2 == column) {

            Piece r = bs.get(8*(8-p.getRank())+7);
            bs.set(8*(8-r.getRank())+7, null);
            bs.set(8*(8-rank)+5, r);
            r.setColumn(6);
        }

        // Queenside castle
        if (p.getPieceType() == 'K' && p.getColumn()-2 == column) {

            Piece r = bs.get(8*(8-p.getRank()));
            bs.set(8*(8-r.getRank()), null);
            bs.set(8*(8-rank)+3, r);
            r.setColumn(4);
        }

        // Remove corresponding castle opportunity if any of the rooks move
        if(p.getPieceType() == 'R' && !castles.equals("-")) {

            if(p.getColor() && p.getRank() == 1 && p.getColumn() == 8 && castles.contains("K"))
                castles = castles.replace("K", "");
            if(p.getColor() && p.getRank() == 1 && p.getColumn() == 1 && castles.contains("Q"))
                castles = castles.replace("Q", "");
            if(!p.getColor() && p.getRank() == 8 && p.getColumn() == 8 && castles.contains("k"))
                castles = castles.replace("k", "");
            if(!p.getColor() && p.getRank() == 8 && p.getColumn() == 1 && castles.contains("q"))
                castles = castles.replace("q", "");
        }

        // if there are no castling opportunities, set variable as such
        if(castles.equals("")) castles = "-";

        // Tells the piece where it is on the board
        p.setColumn(column); p.setRank(rank);

        // Update position string
        int count = 0;
        StringBuilder po = new StringBuilder();

        for(int i=0; i<bs.size(); i++) {

            Piece c = bs.get(i);

            if(c == null) count++;
            else {

                if(count > 0) po.append(count);
                count = 0;
                if(c.getColor()) po.append(c.getPieceType());
                else po.append(Character.toLowerCase(c.getPieceType()));
            }

            if(i % 8 == 7) {
                if(count > 0) po.append(count);
                po.append('/');
                count = 0;
            }
        }

        String position = new String(po);

        return (position + " " + move + " " + castles + " " + en_passant + " " + half_moves_from_pawn_capture + " " + turns);
    }

    private String Export(ArrayList<Node> tree) {

        Node n = null;

        for(int i=0; i<tree.size(); i++) {

            if(tree.get(i).isBaseNode()) {n = tree.get(i); break;}
        }

        if(n == null) {

            Log.e("ExportTree", "Base node does not exist, cannot find tree");
            return "Base node does not exist, cannot find tree";
        }

        StringBuilder export = new StringBuilder();
        int turn = 1;

        for(int i=0; i<n.getChildren().size(); i++) {

            if(i != n.getChildren().size()-1) export.append("(");
            export.append(turn);
            export.append(".");
            export.append(n.getChildMove(n.getChildren().get(i)));
            export.append(" ");
            exportTextGenerator(n.getChildren().get(i), export, turn);
            if(i != n.getChildren().size()-1) export.append(") ");
        }

        return (new String(export));
    }

    private void exportTextGenerator(Node n, StringBuilder x, int turn) {

        if(n.getMove() == 'b') turn++;
        if(n.getNote().length() > 0) x.append("(");
        x.append(n.getNote());
        if(n.getNote().length() > 0) x.append(") ");

        for(int i=0; i<n.getChildren().size(); i++) {

            if(i != n.getChildren().size()-1) x.append("(");
            if(n.getMove() == 'b') {

                x.append(turn);
                x.append(".");
            }
            x.append(n.getChildMove(n.getChildren().get(i)));
            x.append(" ");
            exportTextGenerator(n.getChildren().get(i), x, turn);
            if(i != n.getChildren().size()-1) x.append(") ");
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
        getMenuInflater().inflate(R.menu.import_export, menu);
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

            Intent intent = new Intent(this, SaveActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_importexport) {

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