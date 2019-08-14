package com.example.chesstree;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.TouchDelegate;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.support.v7.widget.GridLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chesstree.ui.main.NoteEditorFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ChessBoardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    public static final char FIRST_MOVE = 'w';
    public static final String INITIAL_CASTLES = "KQkq";
    public static final String INITIAL_EN_PASSANT = "-";
    public static final int INITIAL_HALF_MOVES_FROM_PAWN_CAPTURES = 0;
    public static final int INITIAL_TURN = 0;
    public static final HashMap<Character, Integer> COLUMN_CODE = new HashMap<Character, Integer>() {
        {
            put('a', 1);
            put('b', 2);
            put('c', 3);
            put('d', 4);
            put('e', 5);
            put('f', 6);
            put('g', 7);
            put('h', 8);
        }};
    public static final HashMap<Integer, Character> INVERSE_COLUMN_CODE = new HashMap<Integer, Character>() {
        {
            put(1, 'a');
            put(2, 'b');
            put(3, 'c');
            put(4, 'd');
            put(5, 'e');
            put(6, 'f');
            put(7, 'g');
            put(8, 'h');
        }};

    /* Counts the number of new imageviews upon clicking any given piece and uses that number as each
     * imageview's identity so when the piece is moved or something else is clicked, these dots which
     * show the user where any selected piece has the potential to move can be removed upon deselection. */
    public static int showMoveID = 0;

    private String position = "";
    private ArrayList<Piece> positionAsArray = new ArrayList<>(64);
    private char move = ' ';
    private String castles = "";
    private String en_passant = "";
    private int half_moves_from_pawn_capture = -1;
    private int turns = -1;
    private String fen = position + " " + move + " " + castles + " " + en_passant + " " + half_moves_from_pawn_capture + " " + turns;
    private int screenX;
    private int screenY;
    private float density;

    private LinkedList<Node> movesList = new LinkedList<>();
    private int movesListIndex = 0;

    private ArrayList<ChessMoveItem> boardHistory = new ArrayList<>();
    private int currentMoveItem = -1;

    // Stores chess game notation to then display to the user upon making a move.
    private String moveString = "";

    // Keeps a list of moves performed this game, which allows the program to compile this list
    // into a readable PGN (Portable Game Notation)
    private ArrayList<Pair<String, String>> pgn = new ArrayList<>();

    /* Determines whether or not any piece is selected such that it's moves are showing. this is
     * here to increase the efficiency of touch events. In other words, when removing the imageviews
     * that show possible moves, it first checks if it is necessary to remove anything. */
    private boolean showingMoves = false;

    /* In the case of pawn promotions during a chess game, this value stores whether or not a menu
     * item in the promotion menu has been selected. If none has been selected upon dismissal of the
     * menu, then it goes to a default option, namely promotion to a queen. */
    private boolean menuItemClicked = false;

    /* In the case of pawn promotions during a chess game, this value stores whether or not a menu
     * is currently being shown. This is necessary if the menu is showing to require an item selection
     * before the game can continue, as it prevents certain things from being checked or permanently
     * changed that would break the game in the case of happening during a pawn promotion. */
    private boolean promotionMenuShowing = false;

    // Variable sent from Popup.onDismissListener to changeBoardState to tell it that it's the one
    // that is calling it
    private boolean promotion = false;

    // Whether or not this activity should restore a previous board state
    private boolean restoreState = true;

    /* Controls the point of view of the board. The default state is white's point of view
     * so that is when rotated is false. When rotated is true, black's pieces are displayed
     * at the botton of the screen and white's pieces are displayed at the top. */
    private boolean rotated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /* Determines hardware specifications such as screen size and pixel density to be able to
         * properly display each image on the screen according to such specifications. */
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenX = size.x;
        screenY = size.y;

        // Autogenerated code provided when the activity file was created
        setContentView(R.layout.activity_chess_board);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        ArrayList<String> str = intent.getStringArrayListExtra("Node");
        if (str != null) {

            position = str.get(0);
            if(str.get(1).equals("Black")) {move = 'b'; turns++;}
            else move = 'w';
            restoreState = false;

            updateBoard();

            StringBuilder castle = new StringBuilder();

            Piece whiterookk = positionAsArray.get(63);
            Piece whiteking = positionAsArray.get(60);
            Piece whiterookq = positionAsArray.get(56);
            Piece blackrookk = positionAsArray.get(7);
            Piece blackking = positionAsArray.get(4);
            Piece blackrookq = positionAsArray.get(0);

            if(whiteking != null && whiteking.getPieceType() == 'K' && whiteking.getColor()) {

                if(whiterookk != null)
                    if(whiterookk.getPieceType() == 'R')
                        if(whiterookk.getColor())
                            castle.append("K");

                if(whiterookq != null)
                    if(whiterookq.getPieceType() == 'R')
                        if(whiterookq.getColor())
                            castle.append("Q");
            }

            if(blackking != null && blackking.getPieceType() == 'K' && !blackking.getColor()) {

                if(blackrookk != null)
                    if(blackrookk.getPieceType() == 'R')
                        if(!blackrookk.getColor())
                            castle.append("k");

                if(blackrookq != null)
                    if(blackrookq.getPieceType() == 'R')
                        if(!blackrookq.getColor())
                            castle.append("q");
            }

            if(castle.length() == 0) castle.append("-");

            castles = new String(castle);
        }

        // Variable initialization if a savedInstanceState has not been loaded
        if(position.equals("")) position = STARTING_POSITION;
        if(move == ' ') move = FIRST_MOVE;
        if(castles.equals("")) castles = INITIAL_CASTLES;
        if(en_passant.equals("")) en_passant = INITIAL_EN_PASSANT;
        if(half_moves_from_pawn_capture == -1) half_moves_from_pawn_capture = INITIAL_HALF_MOVES_FROM_PAWN_CAPTURES;
        if(turns == -1) turns = INITIAL_TURN;

        fen = position + " " + move + " " + castles + " " + en_passant + " " + half_moves_from_pawn_capture + " ";
        if(position.equals(STARTING_POSITION)) fen += "1";
        else fen += turns;

        // Adds a listener to the screen to allow for deselection of pieces by clicking somewhere else
        ConstraintLayout layout = findViewById(R.id.chessBoardScreen);
        ScreenListener sl = new ScreenListener();
        layout.setOnClickListener(sl);

        // Draws the board for the first time
        updateBoard();

        movesList.add(MainActivity.findNode(position, move));
        pgn.add(Pair.create(fen, "Base"));

        // Initialize click listeners for menu displayed directly beneath the board
        initializeBoardMenu();
    }

    private void initializeBoardMenu() {

        ImageButton options = findViewById(R.id.more_options);
        ImageButton note_edit = findViewById(R.id.note_edit);
        ImageButton flip_board = findViewById(R.id.flip_board);
        ImageButton analyze = findViewById(R.id.analyze);
        ImageButton rewind = findViewById(R.id.rewind);
        ImageButton go_forward = findViewById(R.id.go_forward);

        options.setOnClickListener(new OptionsListener());
        note_edit.setOnClickListener(new NoteEditListener());
        flip_board.setOnClickListener(new FlipListener());
        analyze.setOnClickListener(new AnalyzeListener());
        rewind.setOnClickListener(new RewindListener());
        go_forward.setOnClickListener(new ForwardListener());
    }

    private void setBoardState(String fen) {

        this.fen = fen;
        position = fen.split(" ")[0];
        move = fen.split(" ")[1].charAt(0);
        castles = fen.split(" ")[2];
        en_passant = fen.split(" ")[3];

        String half = fen.split(" ")[4];
        half_moves_from_pawn_capture = 0;

        String t = fen.split(" ")[5];
        turns = 0;

        for(int i=0; i<half.length(); i++)
            half_moves_from_pawn_capture += Math.pow(10, half.length()-i-1) *
                    Character.getNumericValue(half.charAt(i));

        for(int i=0; i<t.length(); i++)
            turns += Math.pow(10, t.length()-i-1) *
                    Character.getNumericValue(t.charAt(i));

        positionAsArray = new ArrayList<>(64);
        updateBoard();
    }

    // What happens when the options menu on the board menu is clicked on
    private class OptionsListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            final AlertDialog.Builder adb = new AlertDialog.Builder(ChessBoardActivity.this);
            adb.setTitle("Options");
            String[] items = new String[3];
            items[0] = "Share PGN";
            items[1] = "View Nodes";
            items[2] = "To Be Continued";
            adb.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {

                        case 0:
                            Intent intent = new Intent(ChessBoardActivity.this, ImportExportActivity.class);
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putStringArrayListExtra("PGN", writePGNToStringArrayList());
                            startActivity(intent);
                            break;
                        case 1:
                            Intent intent2 = new Intent(ChessBoardActivity.this, NodeActivity.class);
                            intent2.putStringArrayListExtra("Node", movesList.get(movesListIndex).writeContentsToStringArray());
                            startActivity(intent2);
                            break;
                        case 2:
                        default:
                    }
                }
            });

            adb.show();
        }
    }

    // What happens when the note editor on the board menu is clicked on
    private class NoteEditListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            NoteEditorFragment fragment = NoteEditorFragment.newInstance(position, move);
            fragment.setShowsDialog(true);
            fragment.setCancelable(true);
            fragment.showNow(getSupportFragmentManager(), null);
        }
    }

    // What happens when the board flipper on the board menu is clicked on
    private class FlipListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            setRotated(!rotated);
            updateBoard();
        }
    }

    // What happens when the analyzer button on the board menu is clicked on
    private class AnalyzeListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(ChessBoardActivity.this, AnalysisActivity.class);
            intent.putStringArrayListExtra("PGN", writePGNToStringArrayList());
            startActivity(intent);
        }
    }

    // What happens when the rewind button on the board menu is clicked on
    private class RewindListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(movesListIndex == 0) return;
            Node n = movesList.get(movesListIndex-1);

            String fen = findFENInPGN(n.getPosition(), n.getMove());

            if(fen == null) {
                Log.e("NullBoardState", "FEN Cannot be null when setting the board");
                return;
            }

            setBoardState(fen);
            movesListIndex--;
        }
    }

    //  What happens when the fast forward button on the board menu is clicked on
    private class ForwardListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(movesList.size() <= movesListIndex + 1) return;

            Node n = movesList.get(movesListIndex+1);
            fen = findFENInPGN(n.getPosition(), n.getMove());

            if(fen == null) {
                Log.e("NullBoardState", "FEN Cannot be null when setting the board");
                return;
            }

            setBoardState(fen);
            movesListIndex++;
        }
    }

    @Override
    protected void onStop() {

        super.onStop();

        if(pgn.isEmpty()) return;

        File directory = getCacheDir();
        String filename = "boardstate";
        try {
            File file = File.createTempFile(filename, null, directory);
            FileOutputStream fos = new FileOutputStream(file);
            ArrayList<String> fileContents = writePGNToStringArrayList();

            for(int i=0; i<fileContents.size(); i++) {
                fos.write(fileContents.get(i).getBytes());
            }
            fos.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        restoreState = true;
    }

    @Override
    protected void onResume() {

        super.onResume();

        if(!restoreState) return;

        File directory = getCacheDir();
        String filename = "boardstate";

        try {

            File file = new File(directory, filename);
            if(!file.exists()) return;
            FileInputStream fis = openFileInput(filename);
            int content;
            StringBuilder sb = new StringBuilder();

            while((content = fis.read()) != -1) {

                sb.append((char)content);
            }

            String fileContents = new String(sb);
            StringBuilder moves = new StringBuilder();
            StringBuilder fens = new StringBuilder();
            String mo = "";
            String fe = "";
            boolean recording_moves = false;
            boolean recording_fens = false;

            for(int i=0; i<fileContents.length(); i++) {

                char x = fileContents.charAt(i);

                if(x == ' ') continue;

                if(x == ',') {

                    mo = new String(moves);
                    moves = new StringBuilder();
                    recording_moves = false;
                    recording_fens = true;
                    continue;
                }

                if(x == ']') {

                    fe = new String(fens);
                    fens = new StringBuilder();
                    NestedScrollView nsv = findViewById(R.id.move_view);
                    ChessMoveFormatterLayout ml = findViewById(R.id.move_list);
                    ChessMoveItem cmi;
                    if(currentMoveItem == -1) cmi = new ChessMoveItem(this, fen);
                    else {
                        boardHistory.get(currentMoveItem).setIsSelected(false);
                        cmi = new ChessMoveItem(this, boardHistory.get(currentMoveItem), fen);
                    }
                    currentMoveItem = boardHistory.size();
                    boardHistory.add(cmi);

                    cmi.setTextSize(16);
                    cmi.setMinimumHeight((int)(18*density));
                    cmi.setMaxHeight((int)(30*density));
                    cmi.setText(moveString);
                    cmi.setOnClickListener(new MoveStringListener(fe, cmi));

                    cmi.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    ml.addChessMove(cmi);
                    nsv.invalidate();
                    nsv.requestLayout();
                    pgn.add(Pair.create(fen, moveString));
                    recording_fens = false;
                    continue;
                }

                if(recording_moves) moves.append(x);
                if(recording_fens) fens.append(x);

                if(x == '[') recording_moves = true;
            }

            position = fe.split(" ")[0];
            move = fe.split(" ")[1].charAt(0);
            castles = fe.split(" ")[2];
            en_passant = fe.split(" ")[3];
            for(int i=0; i<fen.split(" ")[4].length(); i++)
                half_moves_from_pawn_capture += Math.pow(10, Character.getNumericValue(fen.split(" ")[4].charAt(i)))
                        *Character.getNumericValue(fen.split(" ")[4].charAt(i));
            for(int i=0; i<fen.split(" ")[5].length(); i++)
                turns += Math.pow(10, Character.getNumericValue(fen.split(" ")[5].charAt(i)))
                        *Character.getNumericValue(fen.split(" ")[5].charAt(i));

            updateBoard();
        }
        catch(Exception e) {

            e.printStackTrace();
        }
    }

    /* The job of the PromotionListener class is to deal with a user's choice of pawn promotions and modify the
     * board state accordingly. If the user selects Knight, turn the pawn into a knight. If the user selects
     * Bishop, turn the pawn into a bishop. If the user selects rook, turn the pawn into a rook. If the user
     * selects Queen, turn the pawn into a queen. The other job of the PromotionListener class is to give a
     * default option in case the menu is dismissed without any selection. As such, this class also implements
     * the popup menu dismissing interface to handle such an occurrence. In the case of no menu item being selected,
     * it defaults to promoting the pawn into a queen. PromotionListener uses the onMenuClicked boolean to determine
     * whether or not an item from the menu has been clicked. */

    private class PromotionListener implements PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener {

        private Piece piece;
        private Piece t;
        private float d;

        private PromotionListener(Piece p, float density) {

            piece = p;
            d = density;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            // Detect which button was pressed when a menu item has been clicked and modify the board state accordingly
            if(item.getTitle().equals("Knight")) t = new Knight(piece.getColor(), ChessBoardActivity.this, piece.getRank(), piece.getColumn(), d);
            else if(item.getTitle().equals("Rook")) t = new Rook(piece.getColor(), ChessBoardActivity.this, piece.getRank(), piece.getColumn(), d);
            else if(item.getTitle().equals("Bishop")) t = new Bishop(piece.getColor(), ChessBoardActivity.this, piece.getRank(), piece.getColumn(), d);
            else t = new Queen(piece.getColor(), ChessBoardActivity.this, piece.getRank(), piece.getColumn(), d);

            positionAsArray.set(8*(8-t.getRank())+t.getColumn()-1, t);
            moveString += t.getPieceType();
            promotionMenuShowing = false;
            menuItemClicked = true;
            return true;
        }

        @Override
        public void onDismiss(PopupMenu menu) {

            // If a menu item was clicked, reset values and return without doing anything else
            if(menuItemClicked) {

                menuItemClicked = false;
                promotion = true;
                changeBoardState(t, t.getRank(), t.getColumn());
                return;
            }

            // Default to promoting the pawn to a queen if no menu option was chosen
            t = new Queen(piece.getColor(), ChessBoardActivity.this, piece.getRank(), piece.getColumn(), d);

            positionAsArray.set(8*(8-t.getRank())+t.getColumn()-1, t);
            moveString += t.getPieceType();
            promotionMenuShowing = false;
            promotion = true;
            changeBoardState(t, t.getRank(), t.getColumn());
        }
    }


    /* This is the method that does all the hard work of changing the board state, including special
     * rules such as en passant and castling as well as pawn promoting. It's called whenever the
     * board state requires changing (usually from a click event on a piece's possible move option),
     * and it updates the board with the new state once it is done. */

    private void changeBoardState(Piece p, int rank, int column) {

        // Counts half moves from pawn capture to detect a draw game if it reaches 100
        if(positionAsArray.get(8*(8-rank)+column-1) != null && positionAsArray.get(8*(8-rank)+column-1).getPieceType() == 'P')
            half_moves_from_pawn_capture = 0;
        else half_moves_from_pawn_capture++;

        // Check special rules for pawns, mainly en passant and promotions
        if(p.getPieceType() == 'P') {

            // If a white pawn moves in such a way to make en passant possible, record
            // where en passant is possible in the en passant variable.
            if(p.getColor() && p.getRank() + 2 == rank) setEnPassant("" + INVERSE_COLUMN_CODE.get(p.getColumn()) + (rank-1));

            // If a black pawn moves in such a way to make en passant possible, record
            // where en passant is possible in the en passant variable.
            if(!p.getColor() && p.getRank() - 2 == rank) setEnPassant("" + INVERSE_COLUMN_CODE.get(p.getColumn()) + (rank+1));

            // If a pawn moved but not in such a way to make en passant possible, record
            // the lack of an en passant possibility in the en passant variable
            if(!(p.getColor() && p.getRank() + 2 == rank) && !(!p.getColor() && p.getRank() - 2 == rank)) setEnPassant("-");

            // Remove captured pawn if captured through en passant
            if(p.getColumn() + 1 == column || p.getColumn() - 1 == column) {

                if(p.getRank() + 1 == rank && p.getColor() && positionAsArray.get(8*(8-rank)+column-1) == null)
                    positionAsArray.set(8*(9-rank)+column-1, null);

                if(p.getRank() - 1 == rank && !p.getColor() && positionAsArray.get(8*(8-rank)+column-1) == null)
                    positionAsArray.set(8*(7-rank)+column-1, null);
            }

            // Deals with pawn promotions
            if((p.getColor() && rank == 8) || (!p.getColor() && rank == 1)) {

                // Creates an empty view with which to anchor the popup menu
                View v = new View(this);
                v.setLayoutParams(new android.view.ViewGroup.LayoutParams((int)(100*density),(int)(34*density)));
                ConstraintLayout cl = findViewById(R.id.cl);
                cl.addView(v);

                // Creates a popup menu and populates it with the possible options
                // and adds a listener to it such that when clicked or dismissed, it will
                // promote the pawn as desired.
                PopupMenu pm = new PopupMenu(this, v);
                pm.getMenu().add("Queen");
                pm.getMenu().add("Rook");
                pm.getMenu().add("Bishop");
                pm.getMenu().add("Knight");
                PromotionListener pl = new PromotionListener(p, density);
                pm.setOnMenuItemClickListener(pl);
                pm.setOnDismissListener(pl);
                pm.show();
                promotionMenuShowing = true;

                // Sets the new promoted piece to the new piece value
                p = positionAsArray.get(8*(8-p.getRank())+p.getColumn()-1);
            }
        }
        // If it was not a pawn that moved, record the lack of en passant
        // possibility in the en passant variable
        else setEnPassant("-");

        if(!promotionMenuShowing) {

            // Get the notation for the move just made if not in the middle of promoting a pawn
            if(!promotion) moveString = moveToString(p, rank, column);
            else promotion = false;

            // Flips move variable to keep track of whose turn it is
            if(move == 'b') move = 'w';
            else {move = 'b'; turns++;}
        }
        else {

            // Get the notation for the move just made if updating the board just before a pawn promotion
            Piece k = positionAsArray.get(8*(8-rank)+column-1);
            if(k != null) moveString = INVERSE_COLUMN_CODE.get(p.getColumn()) + "x" +
                    INVERSE_COLUMN_CODE.get(column) + rank + "=";
            else moveString = "" + INVERSE_COLUMN_CODE.get(column) + "=";
        }

        //Remove corresponding castling opportunity if rook is taken
        if(castles.contains("K") || castles.contains("Q") || castles.contains("k") || castles.contains("q")) {

            Piece r = positionAsArray.get(8*(8-rank)+column-1);

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
        positionAsArray.set(8*(8-p.getRank())+p.getColumn()-1, null);
        positionAsArray.set(8*(8-rank)+column-1, p);

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

            Piece r = positionAsArray.get(8*(8-p.getRank())+7);
            positionAsArray.set(8*(8-r.getRank())+7, null);
            positionAsArray.set(8*(8-rank)+5, r);
            r.setColumn(6);
        }

        // Queenside castle
        if (p.getPieceType() == 'K' && p.getColumn()-2 == column) {

            Piece r = positionAsArray.get(8*(8-p.getRank()));
            positionAsArray.set(8*(8-r.getRank()), null);
            positionAsArray.set(8*(8-rank)+3, r);
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

        for(int i=0; i<positionAsArray.size(); i++) {

            Piece c = positionAsArray.get(i);

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

        position = new String(po);

        // Removes dots indicating where possible moves are
        hideAvailable();

        fen = position + " " + move + " " + castles + " " + en_passant + " " + half_moves_from_pawn_capture + " " + turns;

        if(!promotionMenuShowing) displayMove();
        // Redraws board to account for the new board state
        updateBoard();
    }

    /* This function is much like the previous changing of board states, except it has an extra
     * argument to modify a potential (but not yet realized) board state. It is critical in
     * determining whether any given move leaves that player in check and is thus illegal. Changing
     * a dummy board state allows the computer to check the whole board for checks without having
     * to change the actual board state, which also allows for the detection of discovered checks as
     * well as preventing castling through or out of check. */

    public static ArrayList<Piece> testBoardState(ArrayList<Piece> bs, Piece p, int rank, int column) {

        ArrayList<Piece> boardstate = new ArrayList<>(64);
        for(int i=0; i<bs.size(); i++) boardstate.add(i, bs.get(i));

        // En Passant
        if (p.getPieceType() == 'P') {

            // Remove captured pawn if captured through en passant
            if (p.getColumn() + 1 == column || p.getColumn() - 1 == column) {

                if (p.getRank() + 1 == rank && p.getColor() && boardstate.get(8 * (8 - rank) + column - 1) == null)
                    boardstate.set(8 * (9 - rank) + column - 1, null);

                if (p.getRank() - 1 == rank && !p.getColor() && bs.get(8 * (8 - rank) + column - 1) == null)
                    boardstate.set(8 * (7 - rank) + column - 1, null);
            }
        }

        // Update boardstate
        boardstate.set(8 * (8 - p.getRank()) + p.getColumn() - 1, null);
        boardstate.set(8 * (8 - rank) + column - 1, p);

        // Kingside castle
        if (p.getPieceType() == 'K' && p.getColumn() + 2 == column) {

            Piece r = boardstate.get(8 * (8 - p.getRank()) + 7);
            boardstate.set(8 * (8 - p.getRank()) + 7, null);
            boardstate.set(8 * (8 - rank) + 6, r);
        }

        // Queenside castle
        if (p.getPieceType() == 'K' && p.getColumn() - 2 == column) {

            Piece r = boardstate.get(8 * (8 - p.getRank()));
            boardstate.set(8 * (8 - p.getRank()), null);
            boardstate.set(8 * (8 - rank) + 4, r);
        }

        return boardstate;
    }

    private String moveToString(Piece p, int r, int c) {

        String m = "";
        Piece k = positionAsArray.get(8*(8-r)+c-1);

        // Pawns don't need to check if other pawns can get to where they are going
        // because of how they move as well as the standard way of notating their movement
        if(p.getPieceType() == 'P') {

            if(k != null) m += INVERSE_COLUMN_CODE.get(p.getColumn()) + "x";
            if(k == null && p.getRank()+1==r && (p.getColumn()+1==c || p.getColumn()-1==c))
                m += INVERSE_COLUMN_CODE.get(p.getColumn()) + "x";

            // Kingside castle notation
        } else if(p.getPieceType() == 'K' && p.getColumn() + 2 == c) {

            m = "O-O";
            return m;

            // Queenside castle notation
        } else if(p.getPieceType() == 'K' && p.getColumn() - 2 == c) {

            m = "O-O-O";
            return m;

            // Kings don't need to check if other kings can get to where they are going
            // because there is only ever one king of a color on the board at a time
        } else if(p.getPieceType() == 'K') {

            m += "K";
            if(k != null) m += "x";

            // All other pieces have to check to see if another piece of the same kind
            // can also get to where they are going
        } else {

            m += p.getPieceType();

            for(int i=0; i<positionAsArray.size(); i++) {

                // Searches through the board state for a different piece of the same color and type
                // as the one being moved to see if it can reach the same spot as where the piece being
                // moved is going.
                Piece t = positionAsArray.get(i);

                // If the space in question is vacant or of the opposite color, do not check
                if(t == null || t.getColor() != p.getColor()) continue;

                // If the space in question is the same space that contains the piece being moved,
                // then pieces t and p are the same piece and there is no need to check
                if(t.getRank() == p.getRank() && t.getColumn() == p.getColumn()) continue;

                // If piece t is not of the same king of piece as piece p, then even if it can reach
                // the same square as piece p, it would not affect notation
                if(t.getPieceType() != p.getPieceType()) continue;

                // If there's a piece of the same type and color that can reach the same square as
                // the piece that is being moved, then additional information is needed to provide
                // unique notation.
                if(t.canMoveTo(positionAsArray, fen, r, c)) {

                    // If column numbers are the same, then provide the unique rank to the move string
                    // otherwise the default for if there's more than one piece of the same type and
                    // color that can reach the same square is to provide the unique column identifier
                    if(t.getColumn() == p.getColumn()) m += p.getRank();
                    else m += INVERSE_COLUMN_CODE.get(p.getColumn());
                }
            }

            if(k != null) m += "x";
        }

        m += "" + INVERSE_COLUMN_CODE.get(c) + r;
        return m;
    }

    public ArrayList<Piece> getPositionAsArray() {

        return positionAsArray;
    }

    public String getEnPassant() {

        return en_passant;
    }

    public void setEnPassant(String ep) {

        en_passant = ep;
    }

    public char getMove() {

        return move;
    }

    public String getCastles() {

        return castles;
    }

    public boolean getShowingMoves() {

        return showingMoves;
    }

    // Checks all possible moves a player can make in case of checkmate
    private boolean checkForEndGame() {

        // Match color boolean with whose move it is
        boolean color = true;
        if(move == 'b') color = false;

        // Check all pieces
        for(int i=0; i<positionAsArray.size(); i++) {

            // If the square at this place on the board is either null or the other
            // player's piece, go to the next spot. This only checks to see if there
            // is any place at all to move that doesn't land this player in check.
            Piece t = positionAsArray.get(i);
            if (t == null) continue;
            if (t.getColor() != color) continue;

            if(t.canMove(positionAsArray, fen)) return false;
        }
        return true;
    }

    /* Given a dummy board state produced by the testBoardState function,
     * this function determines whether or not a king is still in check after a
     * potential move. It is called during the markAsAvailable function such that
     * if move that was ordered to be marked as available would leave the king in
     * check, it instead returns immediately without having marked is as available. */

    public static boolean checkChecks(ArrayList<Piece> boardstate, boolean isWhiteTurn) {

        for(int i=0; i<boardstate.size(); i++) {

            // Checks for threats against the kings, so if there is not a piece
            // in any given place, it iterates until it finds a piece to check
            Piece c = boardstate.get(i);
            if(c == null) continue;

            // Matches a local boolean variable with whose turn it is to compare
            // with the color of the piece found, since a piece of king's own color
            // is not a threat to it.
            boolean color = true;
            if(c.getColor()) color = false;

            if(color != isWhiteTurn) continue;

            // If there is a piece and it is of the opposite color of whose turn it is,
            // the column and rank of the piece is put into local variables to make
            // expressions easier to write
            int cl = c.getColumn();
            int rk = c.getRank();

            // Determines what kind of piece of the opposite color was found and acts accordingly
            if(c.getPieceType() == 'P') {

                // The color of the pawn in question is used here to account for the directional
                // nature of pawns, since black pawns move down in rank while white pawns move up
                if(c.getColor()) {

                    // checks if white pawn attacks black king to the right
                    if(cl < 8 && boardstate.get(8*(7-rk)+cl) != null)
                        if(boardstate.get(8*(7-rk)+cl).getPieceType() == 'K')
                            if(!boardstate.get(8*(7-rk)+cl).getColor()) return true;

                    // checks if white pawn attacks black king to the left
                    if(cl > 1 && boardstate.get(8*(7-rk)+cl-2) != null)
                        if(boardstate.get(8*(7-rk)+cl-2).getPieceType() == 'K')
                            if(!boardstate.get(8*(7-rk)+cl-2).getColor()) return true;
                }
                else {

                    // checks if black pawn attacks white king to the right
                    if(cl < 8 && boardstate.get(8*(9-rk)+cl) != null)
                        if(boardstate.get(8*(9-rk)+cl).getPieceType() == 'K')
                            if(boardstate.get(8*(9-rk)+cl).getColor()) return true;

                    // checks if black pawn attacks white king to the left
                    if(cl > 1 && boardstate.get(8*(9-rk)+cl-2) != null)
                        if(boardstate.get(8*(9-rk)+cl-2).getPieceType() == 'K')
                            if(boardstate.get(8*(9-rk)+cl-2).getColor()) return true;
                }

            } else if(c.getPieceType() == 'N') {

                // checks if knight attacks opposing king by moving one up and two right
                if(rk < 8 && cl < 7 && boardstate.get(8*(7-rk)+cl+1) != null)
                    if(boardstate.get(8*(7-rk)+cl+1).getPieceType() == 'K')
                        if(boardstate.get(8*(7-rk)+cl+1).getColor() == color) return true;

                // checks if knight attacks opposing king by moving one up and two left
                if(rk < 8 && cl > 2 && boardstate.get(8*(7-rk)+cl-3) != null)
                    if(boardstate.get(8*(7-rk)+cl-3).getPieceType() == 'K')
                        if(boardstate.get(8*(7-rk)+cl-3).getColor() == color) return true;

                // checks if knight attacks opposing king by moving two up and one right
                if(rk < 7 && cl < 8 && boardstate.get(8*(6-rk)+cl) != null)
                    if(boardstate.get(8*(6-rk)+cl).getPieceType() == 'K')
                        if(boardstate.get(8*(6-rk)+cl).getColor() == color) return true;

                // checks if knight attacks opposing king by moving two up and one left
                if(rk < 7 && cl > 1 && boardstate.get(8*(6-rk)+cl-2) != null)
                    if(boardstate.get(8*(6-rk)+cl-2).getPieceType() == 'K')
                        if(boardstate.get(8*(6-rk)+cl-2).getColor() == color) return true;

                // checks if knight attacks opposing king by moving one down and two right
                if(rk > 1 && cl < 7 && boardstate.get(8*(9-rk)+cl+1) != null)
                    if(boardstate.get(8*(9-rk)+cl+1).getPieceType() == 'K')
                        if(boardstate.get(8*(9-rk)+cl+1).getColor() == color) return true;

                // checks if knight attacks opposing king by moving one down and two left
                if(rk > 1 && cl > 2 && boardstate.get(8*(9-rk)+cl-3) != null)
                    if(boardstate.get(8*(9-rk)+cl-3).getPieceType() == 'K')
                        if(boardstate.get(8*(9-rk)+cl-3).getColor() == color) return true;

                // checks if knight attacks opposing king by moving two down and one right
                if(rk > 2 && cl < 8 && boardstate.get(8*(10-rk)+cl) != null)
                    if(boardstate.get(8*(10-rk)+cl).getPieceType() == 'K')
                        if(boardstate.get(8*(10-rk)+cl).getColor() == color) return true;

                // checks if knight attacks opposing king by moving two down and one left
                if(rk > 2 && cl > 1 && boardstate.get(8*(10-rk)+cl-2) != null)
                    if(boardstate.get(8*(10-rk)+cl-2).getPieceType() == 'K')
                        if(boardstate.get(8*(10-rk)+cl-2).getColor() == color) return true;

            } else if(c.getPieceType() == 'B') {

                // checks if bishop attacks opposing king by moving up and right
                for(int j=1; ;j++) {

                    if(rk+j > 8 || cl+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk-j)+cl-1+j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if bishop attacks opposing king by moving up and left
                for(int j=1; ;j++) {

                    if(rk+j > 8 || cl-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk-j)+cl-1-j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if bishop attacks opposing king by moving down and right
                for(int j=1; ;j++) {

                    if(rk-j < 1 || cl+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk+j)+cl-1+j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if bishop attacks opposing king by moving down and left
                for(int j=1; ;j++) {

                    if(rk-j < 1 || cl-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk+j)+cl-1-j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

            } else if(c.getPieceType() == 'R') {

                // checks if rook attacks opposing king by moving up
                for(int j=1; ;j++) {

                    if(rk+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk-j)+cl-1);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if rook attacks opposing king by moving down
                for(int j=1; ;j++) {

                    if(rk-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk+j)+cl-1);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if rook attacks opposing king by moving right
                for(int j=1; ;j++) {

                    if(cl+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk)+cl-1+j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if rook attacks opposing king by moving left
                for(int j=1; ;j++) {

                    if(cl-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk)+cl-1-j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

            } else if(c.getPieceType() == 'Q') {

                // checks if queen attacks opposing king by moving up and right
                for(int j=1; ;j++) {

                    if(rk+j > 8 || cl+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk-j)+cl-1+j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if queen attacks opposing king by moving up and left
                for(int j=1; ;j++) {

                    if(rk+j > 8 || cl-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk-j)+cl-1-j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if queen attacks opposing king by moving down and right
                for(int j=1; ;j++) {

                    if(rk-j < 1 || cl+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk+j)+cl-1+j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if queen attacks opposing king by moving down and left
                for(int j=1; ;j++) {

                    if(rk-j < 1 || cl-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk+j)+cl-1-j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if queen attacks opposing king by moving up
                for(int j=1; ;j++) {

                    if(rk+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk-j)+cl-1);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if queen attacks opposing king by moving down
                for(int j=1; ;j++) {

                    if(rk-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk+j)+cl-1);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if queen attacks opposing king by moving right
                for(int j=1; ;j++) {

                    if(cl+j > 8) break;

                    Piece l = boardstate.get(8*(8-rk)+cl-1+j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

                // checks if queen attacks opposing king by moving left
                for(int j=1; ;j++) {

                    if(cl-j < 1) break;

                    Piece l = boardstate.get(8*(8-rk)+cl-1-j);
                    if(l == null) continue;
                    if(l.getColor() != color) break;
                    if(l.getPieceType() == 'K') return true;
                    break;
                }

            } else {

                // checks if king attacks opposing king by moving one up and one right
                if(rk < 8 && cl < 8 && boardstate.get(8*(7-rk)+cl) != null)
                    if(boardstate.get(8*(7-rk)+cl).getPieceType() == 'K')
                        if(boardstate.get(8*(7-rk)+cl).getColor() == color) return true;

                // checks if king attacks opposing king by moving one up
                if(rk < 8 && boardstate.get(8*(7-rk)+cl-1) != null)
                    if(boardstate.get(8*(7-rk)+cl-1).getPieceType() == 'K')
                        if(boardstate.get(8*(7-rk)+cl-1).getColor() == color) return true;

                // checks if king attacks opposing king by moving one up and one left
                if(rk < 8 && cl > 1 && boardstate.get(8*(7-rk)+cl-2) != null)
                    if(boardstate.get(8*(7-rk)+cl-2).getPieceType() == 'K')
                        if(boardstate.get(8*(7-rk)+cl-2).getColor() == color) return true;

                // checks if king attacks opposing king by moving one left
                if(cl > 1 && boardstate.get(8*(8-rk)+cl-2) != null)
                    if(boardstate.get(8*(8-rk)+cl-2).getPieceType() == 'K')
                        if(boardstate.get(8*(8-rk)+cl-2).getColor() == color) return true;

                // checks if king attacks opposing king by moving one right
                if(cl < 8 && boardstate.get(8*(8-rk)+cl) != null)
                    if(boardstate.get(8*(8-rk)+cl).getPieceType() == 'K')
                        if(boardstate.get(8*(8-rk)+cl).getColor() == color) return true;

                // checks if king attacks opposing king by moving one down and one right
                if(rk > 1 && cl < 8 && boardstate.get(8*(9-rk)+cl) != null)
                    if(boardstate.get(8*(9-rk)+cl).getPieceType() == 'K')
                        if(boardstate.get(8*(9-rk)+cl).getColor() == color) return true;

                // checks if king attacks opposing king by moving one down
                if(rk > 1 && boardstate.get(8*(9-rk)+cl-1) != null)
                    if(boardstate.get(8*(9-rk)+cl-1).getPieceType() == 'K')
                        if(boardstate.get(8*(9-rk)+cl-1).getColor() == color) return true;

                // checks if king attacks opposing king by moving one down and one left
                if(rk > 1 && cl > 1 && boardstate.get(8*(9-rk)+cl-2) != null)
                    if(boardstate.get(8*(9-rk)+cl-2).getPieceType() == 'K')
                        if(boardstate.get(8*(9-rk)+cl-2).getColor() == color) return true;
            }
        }

        // if the king is not in check after the potential move has been made, return false and
        // allow the move
        return false;
    }

    /* markAsAvailable is a function called upon detection of a click of a piece, which allows
     * the user to see what moves are possible and move correspondingly. The individual pieces
     * call this function without regard to the rest of the board, so it is this function's job
     * to also check if the potential move that has been passed to mark as available does not put
     * or leave the friendly king in check, for which it creates a dummy board state, modifies it
     * as if the move being marked as available as already been made, and then checks to see if
     * the friendly king is in check. It also prevents castling out of and through check in the
     * same manner. After it has determined that the move being passed to it is, in fact, legal,
     * it creates an image of a small gray dot to place on the board where the selected piece
     * can move. Since the small gray dot is too small to click on, it also creates a secondary
     * and bigger parent imageview in the same place relative to the board squares and assigns a
     * click listener to it which then delegates to the gray dot such that is has been clicked,
     * effectively expanding the hitbox of the small gray dot drawn. When clicked, it moves the
     * piece to the corresponding square and modifies the board state. */

    public void markAsAvailable(int piecerank, int piececolumn, int rank, int column) {

        // If this move would put or keep their own color king in check, do not mark as available
        Piece t = positionAsArray.get(8*(8-piecerank)+piececolumn-1);

        if(positionAsArray.get(8*(8-piecerank)+piececolumn-1).getPieceType() == 'K') {

            // Check kingside castle through check and away from check
            if(column - 2 == piececolumn) {

                if(checkChecks(positionAsArray, t.getColor())) return;
                if(checkChecks(testBoardState(positionAsArray, t, rank, column-1), t.getColor())) return;
            }

            // Check queenside castle through check and away from check
            if(column + 2 == piececolumn) {

                if(checkChecks(positionAsArray, t.getColor())) return;
                if(checkChecks(testBoardState(positionAsArray, t, rank, column+1), t.getColor())) return;
            }
        }

        // Modify dummy board state and check to see if own color king is still
        // in check after move has occurred.
        if(checkChecks(testBoardState(positionAsArray, t, rank, column), t.getColor())) return;

        // After confirming move does not put or keep own color king in check,
        // mark this move as legal by placing a gray dot where the piece would go

        // Creates imageviews, parent will contain the touch delegate which allows
        // the expansion of the gray dot's hitbox while iv will contain the gray dot
        final ImageView parent = new ImageView(this);
        final ImageView iv = new ImageView(this);

        // Find the container that these images will be present in and iterate the move ID
        // to be a unique identifier such that these imageviews can be removed later
        ConstraintLayout cl = findViewById(R.id.cl);
        showMoveID++;

        // Sets the position, dimension, and identifier of the parent imageview
        float pleft = (-11 + 39 * column - (float) (column / 4)) * density;
        float ptop = (21 + 39 * (8 - rank) - (float) ((8 - rank) / 4)) * density;
        parent.setTranslationX(pleft);
        parent.setTranslationY(ptop);
        parent.setLayoutParams(new android.view.ViewGroup.LayoutParams((int)(34*density), (int)(34*density)));
        parent.setId(showMoveID);

        // Iterate move ID, set the imageview's identifier, and add the image to the imageview
        showMoveID++;
        iv.setImageResource(R.drawable.circlegray);
        iv.setId(showMoveID);

        // Increase hit box to the image of a small blue circle to the dimension of a full chess square
        final Rect rect = new Rect();
        iv.getHitRect(rect);
        rect.top -= (int)(13*density);    // increase top hit area
        rect.left -= (int)(13*density);   // increase left hit area
        rect.bottom += (int)(13*density); // increase bottom hit area
        rect.right += (int)(13*density);  // increase right hit area
        parent.setTouchDelegate(new TouchDelegate(rect, iv));

        // Set position of the gray dot
        float left = (2+39*column-(float)(column/4))*density;
        float top = (34+39*(8-rank)-(float)((8-rank)/4))*density;
        iv.setTranslationX(left);
        iv.setTranslationY(top);

        // Set the click listener to the parent holding the gray dot to allow the moving of pieces
        ChessBoardActivity.BoardListener bl = new ChessBoardActivity.BoardListener(piecerank, piececolumn, rank, column);
        parent.setOnClickListener(bl);

        // Add both imageviews to the container to draw the gray dot
        cl.addView(parent);
        cl.addView(iv);

        // Mark the moves as currently being shown
        showingMoves = true;
    }

    // Function that allows for the removal of the gray dots associated with available moves of a piece
    // upon navigation away from the piece or the successful moving of the piece in question
    public void hideAvailable() {

        // Find view holding the gray dots
        ConstraintLayout cl = findViewById(R.id.cl);
        int children = cl.getChildCount();
        int numviews = showMoveID;

        // Removes all views that were assigned with the markAsAvailable method
        for(int i=0; i<numviews; i++) {

            for(int j=0; j<children; j++) {

                if(cl.getChildAt(j).getId() == showMoveID) {

                    cl.removeView(cl.getChildAt(j));
                    showMoveID--;
                    break;
                }
            }
        }

        // Once all the moves have been hidden, reset the move ID to 0
        // and mark the moves as no longer showing
        showMoveID = 0;
        showingMoves = false;
    }

    // If a user touches anywhere on the screen while a piece is selected and moves are
    // being shown, deselect the piece and hide the moves
    public class ScreenListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(showingMoves) hideAvailable();
        }
    }

    // If one of the gray dots is clicked, move the selected piece to the selected square
    public class BoardListener implements View.OnClickListener {

        private int pr;
        private int pc;
        private int r;
        private int c;

        // Constructor requires current location of the piece as well as where the piece is going
        public BoardListener(int piecerank, int piececolumn, int rank, int column) {

            pr = piecerank;
            pc = piececolumn;
            r = rank;
            c = column;
        }

        @Override
        public void onClick(View v) {

            // Get piece at specified location on the board
            Piece p = positionAsArray.get(8*(8-pr)+pc-1);

            // Save previous position and move to find parent node after board updates
            String po = position;
            char mo = move;

            // Changes the board state to reflect the move just made
            changeBoardState(p, r, c);

            // If the user is currently in the process of promoting a pawn,
            // do not check the board or add the node
            if(promotionMenuShowing) return;

            // Adds a node to the main activity with the current position and move
            Node parent = new Node();
            for(int i=0; i<MainActivity.nodesList.size(); i++) {

                Node j = MainActivity.nodesList.get(i);
                if(j.equals(po, mo)) parent = j;
            }

            if(move == 'b') moveString = moveString.substring(2);
            else moveString = moveString.substring(4);
            Node n = new Node(position, parent, moveString);
            MainActivity.addNode(n);
            movesListIndex++;
            while(movesList.size() > movesListIndex) movesList.removeLast();
            movesList.add(n);

            boolean isWhiteTurn = true;
            if(move == 'w') isWhiteTurn = false;

            boolean inCheck = checkChecks(positionAsArray, isWhiteTurn);

            if(checkForEndGame()) {

                if(inCheck) {

                    if(move == 'w')
                        Toast.makeText(ChessBoardActivity.this, "Black wins by checkmate. 0-1", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(ChessBoardActivity.this, "White wins by checkmate. 1-0", Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(ChessBoardActivity.this, "Stalemate. 0.5-0.5", Toast.LENGTH_LONG).show();
            }
            else {
                if(half_moves_from_pawn_capture == 100)
                    Toast.makeText(ChessBoardActivity.this, "50 moves since pawn taken: Stalemate. 0.5-0.5", Toast.LENGTH_LONG).show();
            }

            // Resets movement string to allow for rebuilding according to which pieces move
            if(!promotionMenuShowing) moveString = "";
        }
    }

    /* MoveStringListener listens for any clicks detected from the moveString textview displayed
     * at the bottom of the screen which will allow the user to return to previous board states. */
    private class MoveStringListener implements View.OnClickListener {

        private String f;
        private ChessMoveItem cmi;

        private MoveStringListener(String f, ChessMoveItem cmi) {

            this.f = f;
            this.cmi = cmi;
        }

        @Override
        public void onClick(View v) {

            boardHistory.get(currentMoveItem).setIsSelected(false);
            for(int i=0; i<boardHistory.size(); i++) {
                if(cmi.equals(boardHistory.get(i))) {
                    currentMoveItem = i;
                    break;
                }
            }
            fen = f;
            cmi.setIsSelected(true);
            String[] bs = fen.split(" ");
            position = bs[0];
            move = bs[1].charAt(0);
            castles = bs[2];
            en_passant = bs[3];
            String hf = bs[4];
            String tn = bs[5];
            half_moves_from_pawn_capture = 0;
            turns = 0;
            for(int i=0;i<hf.length();i++)
                half_moves_from_pawn_capture +=
                        Math.pow(10, hf.length()-i-1)*Character.getNumericValue(hf.charAt(i));
            for(int i=0;i<tn.length();i++)
                turns += Math.pow(10, tn.length()-i-1)*Character.getNumericValue(tn.charAt(i));
            positionAsArray = new ArrayList<>(64);
            if(move == 'w') movesListIndex = 2*turns-2;
            else movesListIndex = 2*turns-1;
            updateBoard();
        }
    }

    // Displays move to the user and also adds it to the game's PGN so the game may be exported
    private void displayMove() {

        // Displays turn number and whose turn it was in the move
        if(move == 'w') moveString = turns + "..." + moveString;
        else moveString = turns + "." + moveString;

        boolean isWhiteTurn = true;
        if(move == 'w') isWhiteTurn = false;

        // If a player is in check or checkmate, indicate as such in the string
        if(checkForEndGame() && checkChecks(positionAsArray, isWhiteTurn)) moveString += "#";
        else if(checkChecks(positionAsArray, isWhiteTurn)) moveString += "+";

        // If the move has already been made, do not add move to the screen
        for(int i=0; i<pgn.size(); i++) {
            if(pgn.get(i).equals(Pair.create(fen, moveString))) {

                ChessMoveItem cmi = new ChessMoveItem(this, boardHistory.get(currentMoveItem), fen);
                for(int j=0; j<boardHistory.size(); j++) {
                    if(boardHistory.get(j).equals(cmi)) {
                        boardHistory.get(currentMoveItem).setIsSelected(false);
                        currentMoveItem = j;
                        boardHistory.get(currentMoveItem).setIsSelected(true);
                        break;
                    }
                }
                return;
            }
        }

        // Display move on the screen and add move to the PGN
        NestedScrollView nsv = findViewById(R.id.move_view);
        ChessMoveFormatterLayout ml = findViewById(R.id.move_list);

        ChessMoveItem cmi;
        if(currentMoveItem == -1) cmi = new ChessMoveItem(this, fen);
        else {
            boardHistory.get(currentMoveItem).setIsSelected(false);
            cmi = new ChessMoveItem(this, boardHistory.get(currentMoveItem), fen);
        }
        currentMoveItem = boardHistory.size();
        boardHistory.add(cmi);

        cmi.setTextSize(16);
        cmi.setText(moveString);
        cmi.setOnClickListener(new MoveStringListener(fen, cmi));

        cmi.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        ml.addChessMove(cmi);
        nsv.invalidate();
        nsv.requestLayout();
        pgn.add(Pair.create(fen, moveString));
    }

    public boolean getRotated() {

        return rotated;
    }

    public void setRotated(boolean rotated) {

        this.rotated = rotated;
    }

    // Draws the board according to the board state
    public void updateBoard() {

        // Sets the initial values of rank and column to be able to read the position correctly
        int rank = 8;
        int column = 1;

        // Find container where all images of pieces are being placed and remove them all
        // to be able to redraw the board according to the new board state
        ConstraintLayout cl = findViewById(R.id.cl);
        cl.removeAllViews();

        // If positionAsArray has not been populated yet, populate it
        if(positionAsArray == null) positionAsArray = new ArrayList<>(64);

        // Iterates through the whole board to draw it
        for(int i = 0; i<position.length(); i++) {

            if(position.charAt(i) == 'r') {

                // Black rook
                Rook r = new Rook(false, this, rank, column, density);
                r.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, r);
                column++;

            } else if(position.charAt(i) == 'n') {

                // Black knight
                Knight n = new Knight(false, this, rank, column, density);
                n.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, n);
                column++;

            } else if(position.charAt(i) == 'b') {

                // Black bishop
                Bishop b = new Bishop(false, this, rank, column, density);
                b.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, b);
                column++;

            } else if(position.charAt(i) == 'q') {

                // Black queen
                Queen q = new Queen(false, this, rank, column, density);
                q.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, q);
                column++;

            } else if(position.charAt(i) == 'k') {

                // Black king
                King k = new King(false, this, rank, column, density);
                k.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, k);
                column++;

            } else if(position.charAt(i) == 'p') {

                // Black pawn
                Pawn p = new Pawn(false, this, rank, column, density);
                p.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, p);
                column++;

            } else if(position.charAt(i) == '1') {

                // Single open square
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, null);
                column += 1;

            } else if(position.charAt(i) == '2') {

                // Two open squares next to each other on the same rank
                if(positionAsArray.size() != 64)
                    for(int j=0; j<Character.getNumericValue(position.charAt(i)); j++)
                    positionAsArray.add(8*(8-rank)+column-1+j, null);
                column += 2;

            } else if(position.charAt(i) == '3') {

                // Three open squares next to each other on the same rank
                if(positionAsArray.size() != 64)
                    for(int j=0; j<Character.getNumericValue(position.charAt(i)); j++)
                    positionAsArray.add(8*(8-rank)+column-1+j, null);
                column += 3;

            } else if(position.charAt(i) == '4') {

                // Four open squares next to each other on the same rank
                if(positionAsArray.size() != 64)
                    for(int j=0; j<Character.getNumericValue(position.charAt(i)); j++)
                    positionAsArray.add(8*(8-rank)+column-1+j, null);
                column += 4;

            } else if(position.charAt(i) == '5') {

                // Five open squares next to each other on the same rank
                if(positionAsArray.size() != 64)
                    for(int j=0; j<Character.getNumericValue(position.charAt(i)); j++)
                    positionAsArray.add(8*(8-rank)+column-1+j, null);
                column += 5;

            } else if(position.charAt(i) == '6') {

                // Six open squares next to each other on the same rank
                if(positionAsArray.size() != 64)
                    for(int j=0; j<Character.getNumericValue(position.charAt(i)); j++)
                    positionAsArray.add(8*(8-rank)+column-1+j, null);
                column += 6;

            } else if(position.charAt(i) == '7') {

                // Seven open squares next to each other on the same rank
                if(positionAsArray.size() != 64)
                    for(int j=0; j<Character.getNumericValue(position.charAt(i)); j++)
                    positionAsArray.add(8*(8-rank)+column-1+j, null);
                column += 7;

            } else if(position.charAt(i) == '8') {

                // All squares on this rank are open
                if(positionAsArray.size() != 64)
                    for(int j=0; j<Character.getNumericValue(position.charAt(i)); j++)
                    positionAsArray.add(8*(8-rank)+column-1+j, null);
                column += 8;

            } else if(position.charAt(i) == '/') {

                // Move one rank down and reset column to draw the next rank
                rank--;
                column = 1;

            } else if(position.charAt(i) == 'R') {

                // White rook
                Rook r = new Rook(true, this, rank, column, density);
                r.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, r);
                column++;

            } else if(position.charAt(i) == 'N') {

                // White knight
                Knight n = new Knight(true, this, rank, column, density);
                n.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, n);
                column++;

            } else if(position.charAt(i) == 'B') {

                // White bishop
                Bishop b = new Bishop(true, this, rank, column, density);
                b.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, b);
                column++;

            } else if(position.charAt(i) == 'Q') {

                // White queen
                Queen q = new Queen(true, this, rank, column, density);
                q.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, q);
                column++;

            } else if(position.charAt(i) == 'K') {

                // White king
                King k = new King(true, this, rank, column, density);
                k.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, k);
                column++;

            } else if(position.charAt(i) == 'P') {

                // White pawn
                Pawn p = new Pawn(true, this, rank, column, density);
                p.draw(cl);
                if(positionAsArray.size() != 64) positionAsArray.add(8*(8-rank)+column-1, p);
                column++;

            }
        }
    }

    private ArrayList<String> writePGNToStringArrayList() {

        ArrayList<String> portable = new ArrayList<>();
        if(pgn.isEmpty()) return null;

        for(int i=0; i<pgn.size(); i++) {

            StringBuilder text = new StringBuilder();

            text.append("[");
            text.append(pgn.get(i).second);
            text.append(", ");
            text.append(pgn.get(i).first);
            text.append("]");
            portable.add(new String(text));
        }

        return portable;
    }

    private String findFENInPGN(String pos, char m) {

        if(pgn.isEmpty()) return null;

        for(int i=0; i<pgn.size(); i++) {

            String f = pgn.get(i).first;
            String p = f.split(" ")[0];
            char mo = f.split(" ")[1].charAt(0);
            if(pos.equals(p) && m == mo) return f;
        }

        return null;
    }

    // Save instance in case activity is interrupted
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("position", position);
        savedInstanceState.putString("castles", castles);
        savedInstanceState.putString("en passant", en_passant);
        savedInstanceState.putChar("move", move);
        savedInstanceState.putInt("half moves", half_moves_from_pawn_capture);
        savedInstanceState.putInt("turn number", turns);
    }

    // Restore saved instance data when activity is restored after being interrupted
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        position = savedInstanceState.getString("position");
        castles = savedInstanceState.getString("castles");
        en_passant = savedInstanceState.getString("en passant");
        move = savedInstanceState.getChar("move");
        half_moves_from_pawn_capture = savedInstanceState.getInt("half moves");
        turns = savedInstanceState.getInt("turn number");

        fen = position + " " + move + " " + castles + " " + en_passant + " " + half_moves_from_pawn_capture + " " + turns;
        updateBoard();
    }

    // User presses the back button
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
        getMenuInflater().inflate(R.menu.chess_board, menu);
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