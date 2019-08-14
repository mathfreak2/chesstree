package com.example.chesstree;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.HashMap;

public class Node implements Parcelable {

    // variable containing the board state
    private String position;

    // variable containing any notes this node may hold
    private String note = "";

    // variable containing whose move it is, which is part of the board state
    private char move;

    private boolean expanded = false;

    // Holds its children and parents to be able to form a forwards and backwards tree of nodes
    private ArrayList<Node> children = new ArrayList<>();
    private ArrayList<Node> parents = new ArrayList<>();

    // Hash variables to allow the storage of moves which results in other nodes, this allows the
    // node to store the chess moves while still also allowing for transpositions
    private HashMap<String, Node> childrenHash = new HashMap<>();
    private HashMap<String, Node> parentHash = new HashMap<>();
    private HashMap<Node, String> childrenHash_I = new HashMap<>();
    private HashMap<Node, String> parentHash_I = new HashMap<>();

    // Stores whether this node is the base node of the tree
    private boolean isBaseNode = false;

    public enum MoveQuality {

        BRILLIANT,
        GOOD,
        INTERESTING,
        DUBIOUS,
        MISTAKE,
        BLUNDER,
        EMPTY;

        public static int getNumber(MoveQuality mq) {

            switch(mq) {

                case BRILLIANT: return 1;
                case GOOD: return 2;
                case INTERESTING: return 3;
                case DUBIOUS: return 4;
                case MISTAKE: return 5;
                case BLUNDER: return 6;
                default: return 0;
            }
        }

        public static MoveQuality getQuality(int i) {

            switch(i) {

                case 1: return BRILLIANT;
                case 2: return GOOD;
                case 3: return INTERESTING;
                case 4: return DUBIOUS;
                case 5: return MISTAKE;
                case 6: return BLUNDER;
                default: return EMPTY;
            }
        }
    }

    public enum MoveAnalysis {

        EQUAL,
        SLIGHT_ADVANTAGE_WHITE,
        SLIGHT_ADVANTAGE_BLACK,
        CLEAR_ADVANTAGE_WHITE,
        CLEAR_ADVANTAGE_BLACK,
        DECISIVE_ADVANTAGE_WHITE,
        DECISIVE_ADVANTAGE_BLACK,
        UNCLEAR,
        COMPENSATION,
        EMPTY;

        public static int getNumber(MoveAnalysis mq) {

            switch(mq) {

                case EQUAL: return 1;
                case SLIGHT_ADVANTAGE_WHITE: return 2;
                case SLIGHT_ADVANTAGE_BLACK: return 3;
                case CLEAR_ADVANTAGE_WHITE: return 4;
                case CLEAR_ADVANTAGE_BLACK: return 5;
                case DECISIVE_ADVANTAGE_WHITE: return 6;
                case DECISIVE_ADVANTAGE_BLACK: return 7;
                case UNCLEAR: return 8;
                case COMPENSATION: return 9;
                default: return 0;
            }
        }

        public static MoveAnalysis getAnalysis(int i) {

            switch(i) {

                case 1: return EQUAL;
                case 2: return SLIGHT_ADVANTAGE_WHITE;
                case 3: return SLIGHT_ADVANTAGE_BLACK;
                case 4: return CLEAR_ADVANTAGE_WHITE;
                case 5: return CLEAR_ADVANTAGE_BLACK;
                case 6: return DECISIVE_ADVANTAGE_WHITE;
                case 7: return DECISIVE_ADVANTAGE_BLACK;
                case 8: return UNCLEAR;
                case 9: return COMPENSATION;
                default: return EMPTY;
            }
        }
    }

    public enum MoveOther {

        BETTER,
        ONLY,
        IDEA,
        COUNTER,
        NOVELTY,
        EMPTY;

        public static int getNumber(MoveOther mq) {

            switch(mq) {

                case BETTER: return 1;
                case ONLY: return 2;
                case IDEA: return 3;
                case COUNTER: return 4;
                case NOVELTY: return 5;
                default: return 0;
            }
        }

        public static MoveOther getMoveOther(int i) {

            switch(i) {

                case 1: return BETTER;
                case 2: return ONLY;
                case 3: return IDEA;
                case 4: return COUNTER;
                case 5: return NOVELTY;
                default: return EMPTY;
            }
        }
    }

    public enum PositionOther {

        INITIATIVE,
        ATTACK,
        COUNTERPLAY,
        DEVELOPMENT,
        SPACE,
        TIME_TROUBLE,
        ZUGZWANG,
        EMPTY;

        public static int getNumber(PositionOther mq) {

            switch(mq) {

                case INITIATIVE: return 1;
                case ATTACK: return 2;
                case COUNTERPLAY: return 3;
                case DEVELOPMENT: return 4;
                case SPACE: return 5;
                case TIME_TROUBLE: return 6;
                case ZUGZWANG: return 7;
                default: return 0;
            }
        }

        public static PositionOther getQuality(int i) {

            switch(i) {

                case 1: return INITIATIVE;
                case 2: return ATTACK;
                case 3: return COUNTERPLAY;
                case 4: return DEVELOPMENT;
                case 5: return SPACE;
                case 6: return TIME_TROUBLE;
                case 7: return ZUGZWANG;
                default: return EMPTY;
            }
        }
    }

    private MoveQuality quality = MoveQuality.EMPTY;
    private MoveAnalysis analysis = MoveAnalysis.EMPTY;
    private MoveOther moveOther = MoveOther.EMPTY;
    private PositionOther positionOther = PositionOther.EMPTY;

    private ArrayList<String> tags = new ArrayList<>();

    //The default constructor is reserved for the creation of a base node
    public Node() {

        isBaseNode = true;
        position = ChessBoardActivity.STARTING_POSITION;
        note = "Base Node";
        move = ChessBoardActivity.FIRST_MOVE;
        Log.v("Node", "Base node created.");
    }

    public Node(String pos, Node parent, String m) {

        if(parent.getMove() == 'w') move = 'b';
        else move = 'w';
        position = pos;

        for(int i=0; i<MainActivity.nodesList.size(); i++) {

            Node n = MainActivity.nodesList.get(i);

            if(equals(n)) {
                n.addParent(parent, m);
                return;
            }
        }

        addParent(parent, m);
        Log.v("Node", "Node created with position listed as " + pos);
    }

    public Node(JSONArray node, ArrayList<Node> list) {

        try {
            Log.v("Node created by JSON", node.toString());
            position = node.getString(0);
            if(node.get(1).equals(98)) move = 'b';
            else move = 'w';
            isBaseNode = node.getBoolean(2);
            note = node.getString(3);
            quality = MoveQuality.getQuality(node.getInt(4));
            moveOther = MoveOther.getMoveOther(node.getInt(5));
            analysis = MoveAnalysis.getAnalysis(node.getInt(6));
            positionOther = PositionOther.getQuality(node.getInt(7));

            expanded = node.getBoolean(8);
            String tags_string = node.getString(9);

            String[] str = tags_string.split(",");

            for(int i=0; i<str.length; i++) {

                tags.add(str[i]);
            }

            if(isBaseNode) Log.i("Node", "Base node created from save file");

            boolean exists = false;

            for(int i=0; i<list.size(); i++) {

                if(list.get(i).equals(this)) exists = true;
            }

            if(!exists) list.add(this);
            if(node.length() <= 10) return;

            for(int i=10; i<node.length(); i+=2) {

                String l = node.getString(i);
                JSONArray child = node.getJSONArray(i+1);

                String childp = child.getString(0);
                char mo;
                if(child.get(1).equals(98)) mo = 'b';
                else mo = 'w';

                for(int j=0; j<MainActivity.nodesList.size(); j++) {

                    Node y = MainActivity.nodesList.get(j);
                    if(y.equals(childp, mo)) {
                        y.addParent(this, l);
                        return;
                    }
                }

                Node no = new Node(child, list);

                addChild(no, l);
            }

        }
        catch (Exception e) {

            Log.e("Node", e.toString()+": Provided JSONArray is not compatible with Node: "+node.toString());
        }
    }

    // Add a child to the list of this node's children
    public void addChild(Node child, String m) {

        int index = getChildIndex(child);

        // If the child does not already exist, add it as a child
        if(index == -1) {

            childrenHash.put(m, child);
            childrenHash_I.put(child, m);
            children.add(child);
            child.addParent(this, m);
        }
    }

    // Remove a child from the list of this node's children
    public Node removeChild(Node child) {

        int index = getChildIndex(child);
        if(index == -1) return (new Node());

        Node n = children.remove(index);
        String l = getChildMove(n);
        childrenHash.remove(l);
        childrenHash_I.remove(n);
        n.removeParent(this);
        return n;
    }

    // Add a parent to the list of this node's parents
    public void addParent(Node parent, String m) {

        int index = getParentIndex(parent);

        if(index == -1) {

            parentHash.put(m, parent);
            parentHash_I.put(parent, m);
            parents.add(parent);
            parent.addChild(this, m);
        }
    }

    // Remove a parent from the list of this node's parents
    public Node removeParent(Node parent) {

        if(isBaseNode) return this;
        int index = getParentIndex(parent);
        if(index == -1) return (new Node());

        Node n = parents.remove(index);
        String l = getParentMove(n);
        parentHash.remove(l);
        parentHash_I.remove(n);
        n.removeChild(this);

        if(isOrphan()) removeSelf();
        return n;
    }

    /* Removes this node from the tree of nodes by removing all references to this
     * node by calling removeChild and removeParent on all its parents and children respectively
     * this method is called when it is orphaned (i.e. it has no parents and is thus no longer
     * part of the tree). */
    private Node removeSelf() {

        // Remove this node from all its children
        for(int i=0; i<children.size(); i++) {

            Node n = children.get(i);
            n.removeParent(this);
        }

        // Remove this node from all its parents
        for(int i=0; i<parents.size(); i++) {

            Node n = parents.get(i);
            n.removeChild(this);
        }

        return this;
    }

    // Provides the code for what happens when the node is clicked on
    private class NodeListener implements View.OnClickListener {

        private Activity a;

        private NodeListener(Activity a) {

            this.a = a;
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(a, NodeActivity.class);
            intent.putStringArrayListExtra("Node", Node.this.writeContentsToStringArray());
            a.startActivity(intent);
        }
    }

    public ArrayList<String> writeContentsToStringArray() {

        ArrayList<String> str = new ArrayList<>();

        str.add(position);
        if(move == 'b') str.add("Black");
        else str.add("White");

        return str;
    }

    // Parcelable Methods implementation here
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        ArrayList<String> str = new ArrayList<>();
        str.add(position);
        if(move == 'b') str.add("Black");
        else str.add("White");
        str.add(note);
        if(children.size() != 0) str.add("children");

        for(int i=0; i<children.size(); i++) {

            Node n = children.get(i);
            String l = getChildMove(n);

            str.add(l);
            str.add(n.getPosition());
            if(n.getMove() == 'b') str.add("Black");
            else str.add("White");
            if(i != children.size()-1) str.add(",");
        }

        if(parents.size() != 0) str.add("parents");

        for(int i=0; i<parents.size(); i++) {

            Node n = parents.get(i);
            String l = getParentMove(n);

            str.add(l);
            str.add(n.getPosition());
            if(n.getMove() == 'b') str.add("Black");
            else str.add("White");
            if(i != parents.size()-1) str.add(",");
        }

        dest.writeStringList(str);
    }

    public static final Parcelable.Creator<Node> CREATOR
            = new Parcelable.Creator<Node>() {

        @Override
        public Node createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[0];
        }
    };

    /* This method provides the code to properly display the node ont he MainActivity screen,
     * including the creation of a nodeview and a nodeviewgroup to hold a fab which allows the
     * user to expand and contract its contents. */

    public void display(Activity a, int turn, String notation, NodeViewGroup paren) {

        NodeLayout nl = a.findViewById(R.id.node_layout);

        // Calculates tree level to pass to the nodeview created, which helps the NodeLayout
        // to determine where it should be placed on the screen
        int level;
        if(move == 'w') level = turn*2;
        else level = turn*2+1;
        NodeView nv = new NodeView(a, level, this);
        String text;

        // Change text displayed on the NodeView depending on the notation and whose turn it is
        if(move == 'b') {
            text = turn + "." + notation;
            nv.setTextColor(Color.parseColor("#464646"));
        }
        else {
            if(isBaseNode) text = "0.";
            else text = notation;
            nv.setTextColor(Color.parseColor("#c8c8c8"));
        }

        // Add user specified notation to the end of the displayed text
        switch(quality) {

            case BRILLIANT:
                text += a.getString(R.string.brilliant);
                break;

            case GOOD:
                text += a.getString(R.string.good);
                break;

            case INTERESTING:
                text += a.getString(R.string.interesting);
                break;

            case DUBIOUS:
                text += a.getString(R.string.dubious);
                break;

            case MISTAKE:
                text += a.getString(R.string.mistake);
                break;

            case BLUNDER:
                text += a.getString(R.string.blunder);
                break;
        }

        switch(analysis) {

            case EQUAL:
                text += a.getString(R.string.equal);
                break;

            case SLIGHT_ADVANTAGE_WHITE:
                text += a.getString(R.string.slight_advantage_white);
                break;

            case SLIGHT_ADVANTAGE_BLACK:
                text += a.getString(R.string.slight_advantage_white);
                break;

            case CLEAR_ADVANTAGE_WHITE:
                text += a.getString(R.string.clear_advantage_white);
                break;

            case CLEAR_ADVANTAGE_BLACK:
                text += a.getString(R.string.clear_advantage_black);
                break;

            case DECISIVE_ADVANTAGE_WHITE:
                text += a.getString(R.string.decisive_advantage_white);
                break;

            case DECISIVE_ADVANTAGE_BLACK:
                text += a.getString(R.string.decisive_advantage_black);
                break;

            case UNCLEAR:
                text += a.getString(R.string.unclear);
                break;

            case COMPENSATION:
                text += a.getString(R.string.compensation);
                break;
        }

        switch(moveOther) {

            case IDEA:
                text += a.getString(R.string.idea);
                break;

            case ONLY:
                text += a.getString(R.string.only);
                break;

            case BETTER:
                text += a.getString(R.string.better);
                break;

            case COUNTER:
                text += a.getString(R.string.counter);
                break;

            case NOVELTY:
                text += a.getString(R.string.novelty);
                break;
        }

        switch(positionOther) {

            case INITIATIVE:
                text += a.getString(R.string.initiative);
                break;

            case ATTACK:
                text += a.getString(R.string.attack);
                break;

            case COUNTERPLAY:
                text += a.getString(R.string.counterplay);
                break;

            case DEVELOPMENT:
                text += a.getString(R.string.development);
                break;

            case SPACE:
                text += a.getString(R.string.space);
                break;

            case TIME_TROUBLE:
                text += a.getString(R.string.time_trouble);
                break;

            case ZUGZWANG:
                text += a.getString(R.string.zugzwang);
                break;
        }

        nv.setText(text);

        // Determine text size and where the text is displayed on the node based on the current
        // dimension of the NodeView, which changes based on zoom scale factor
        float fontSize = NodeView.dim/8 - NodeView.dim*text.length()/100;
        nv.setTextSize(fontSize);
        nv.setGravity(Gravity.CENTER_VERTICAL);
        nv.setPadding(0,0,10,0);

        NodeListener li = new NodeListener(a);
        nv.setOnClickListener(li);
        NodeViewGroup nvg;
        if(isBaseNode) {

            nvg = new NodeViewGroup(a, nv);
            nl.addChild(nvg);
        }
        else {

            nvg = new NodeViewGroup(a, nv, paren);
            nl.addChild(nvg);
        }

        /* If this node has one or more children, add the option to display them
         * on MainActivity by adding their nodeviewgroups as children and displaying
         * them on screen when that node is expanded. */

        for(int i=0; i<children.size(); i++) {

            Node n = children.get(i);
            String l = getChildMove(n);
            int t = turn;
            if(move == 'w') t++;
            Log.v("NodeDisplay", t+"."+l);
            n.display(a, t, l, nvg);
        }

        if(isBaseNode) {
            nl.addNodeViewGroup(nvg);
            if(children.size() > 1) nvg.setExpanded(true);
        }
    }

    // Gets whether or not this node is the base node
    public boolean isBaseNode() {

        return isBaseNode;
    }

    // Gets whether or not this node is an orphan and should be removed
    private boolean isOrphan() {

        if(parentHash.isEmpty()) return true;
        else return false;
    }

    // Gets whether or not this node and parameter node are equal, which is defined as
    // having the same position and move. This allows for accounting for transpositions
    public boolean equals(Node n) {

        if(n.getPosition().equals(position) && n.getMove() == move) return true;
        else return false;
    }

    /* Same as above method, but this allows a position and a move to be compared independently
     * of being associated with a node, which is a memory optimization to prevent having to assign
     * too many extraneous nodes to compare each other. */

    public boolean equals(String p, char m) {

        if(position.equals(p) && move == m) return true;
        else return false;
    }

    // Gets the board state from this node
    public String getPosition() {

        return position;
    }

    // Gets the note associated with this node
    public String getNote() {

        return note;
    }

    public void setNote(String n) {

        note = n;
    }

    public boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    // Gets the children nodes of this node
    public ArrayList<Node> getChildren() {

        return children;
    }

    // Gets the parent nodes of this node
    public ArrayList<Node> getParents() {

        return parents;
    }

    // Get a child of this node based on the move string
    public Node getChild(String m) {

        return childrenHash.get(m);
    }

    // Get a child of this node based on where it is in the children arraylist
    public Node getChild(int index) {

        return children.get(index);
    }

    // Get the move string associated with a child
    public String getChildMove(Node n) {

        return childrenHash_I.get(n);
    }

    // Get a parent of this node based on its move string
    public Node getParent(String m) {

        return parentHash.get(m);
    }

    // Get a parent of this node based on where it is in the parent arraylist
    public Node getParent(int index) {

        return parents.get(index);
    }

    // Get the move string associated with a parent
    public String getParentMove(Node n) {

        return parentHash_I.get(n);
    }

    // Get the index of a child node
    public int getChildIndex(Node n) {

        for(int i=0; i<children.size(); i++) {

            if(children.get(i).equals(n)) return i;
        }

        return -1;
    }

    // Get the index of a parent node
    public int getParentIndex(Node n) {

        for(int i=0; i<parents.size(); i++) {

            if(parents.get(i).equals(n)) return i;
        }

        return -1;
    }

    // Get the move associated with this node (i.e., whose turn it is in this board state)
    public char getMove() {

        return move;
    }

    public MoveAnalysis getAnalysis() {
        return analysis;
    }

    public MoveOther getMoveOther() {
        return moveOther;
    }

    public MoveQuality getQuality() {
        return quality;
    }

    public PositionOther getPositionOther() {
        return positionOther;
    }

    public void setAnalysis(MoveAnalysis ma) {
        analysis = ma;
    }

    public void setQuality(MoveQuality mq) {
        quality = mq;
    }

    public void setMoveOther(MoveOther moveOther) {
        this.moveOther = moveOther;
    }

    public void setPositionOther(PositionOther po) {
        positionOther = po;
    }

    public ArrayList<String> getTags(){

        return tags;
    }

    public void setTags(ArrayList<String> tags) {

        this.tags = tags;
    }

    public void addTag(String tag) {

        tags.add(tag);
    }
}