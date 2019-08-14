package com.example.chesstree;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class NodeViewGroup extends ViewGroup {

    // Nodeview to draw
    private NodeView nodeView;

    // Button which allows expansion and contractions
    private ImageView iv;

    // Position on screen
    private float x;
    private float y;

    // Children nodes
    private ArrayList<NodeViewGroup> children = new ArrayList<>();

    private NodeViewGroup parent = null;

    // Screen on which this Nodeviewgroup is being drawn
    private MainActivity ma;

    public NodeViewGroup(Context c, NodeView nv) {

        super(c);
        ma = (MainActivity) c;
        initialize(nv, null);
    }

    public NodeViewGroup(Context c, NodeView nv, NodeViewGroup nvg) {

        super(c);
        ma = (MainActivity) c;
        initialize(nv, nvg);
    }

    public NodeViewGroup(Context c, AttributeSet attrs, NodeView nv) {

        super(c, attrs);
        ma = (MainActivity) c;
        initialize(nv, null);
    }

    // Listener on the imageview (iv) to determine whether or not to expand or
    // contract any given node
    private class IBListener implements View.OnClickListener {

        private ImageView iv;

        private IBListener() {

            iv = NodeViewGroup.this.getButton();
        }

        @Override
        public void onClick(View v) {

            // Creates a toggle such that it will contract if the node is currently expanded
            // and expand if the node is currently contracted
            if(NodeViewGroup.this.getExpanded()) {

                iv.setImageResource(android.R.drawable.ic_menu_add);
                NodeViewGroup.this.setExpanded(false);
            }
            else {

                iv.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                NodeViewGroup.this.setExpanded(true);
            }
        }
    }

    private void initialize(NodeView nv, NodeViewGroup p) {

        // Upon creation, save the passed nodeview and create a new button which will
        // act as a toggle to expand and contract the node
        nodeView = nv;
        parent = p;
        if(p != null) parent.addChild(this);
        iv = new ImageView(ma);
        IBListener fl = new IBListener();

        // Set background and foreground image resources of the expansion button
        iv.setBackgroundResource(R.drawable.circleblue);
        if(nodeView.getExpanded()) iv.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        else iv.setImageResource(android.R.drawable.ic_menu_add);

        // Set dimension and click listener
        iv.setLayoutParams(new android.view.ViewGroup.LayoutParams((int)(NodeView.dim/3), (int)(NodeView.dim/3)));
        iv.setOnClickListener(fl);

        // Set original screen position of nodeView and button relative to this group's position on the screen
        nodeView.setX(0);
        nodeView.setY(0);
        iv.setX(NodeView.dim/2);
        iv.setY(NodeView.dim/6);

        // Add both the nodeview and the button to this group's views
        addView(nodeView);
        addView(iv);

        invalidate();
        requestLayout();
    }

    public boolean getExpanded() {

        return nodeView.getExpanded();
    }

    // Does the work of adding or removing nodeviewgroups to or from the NodeLayout based
    // on whether or not the node is expanded or contracted
    public void setExpanded(boolean expanded) {

        // If the value is not changing, then no work needs to be done
        if(expanded != getExpanded()) {

            NodeLayout nl = ma.findViewById(R.id.node_layout);
            int count = children.size();

            // If the node is currently being expanded, add any children nodes to the screen
            if(expanded) {

                iv.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

                for(int i=0; i<count; i++) {

                    nl.addNodeViewGroup(children.get(i));
                }
            }

            // If the node is currently being contracted, remove any children nodes from the screen
            else {

                iv.setImageResource(android.R.drawable.ic_menu_add);

                for(int i=0; i<count; i++) {

                    nl.removeNodeViewGroup(children.get(i));
                }
            }
        }

        nodeView.setExpanded(expanded);
    }

    // Returns the button and nodeview associated with this group
    public ImageView getButton() {

        return iv;
    }

    public NodeView getNodeView() {

        return nodeView;
    }

    // Add a child if it is not already present in the children arraylist
    public void addChild(NodeViewGroup nvg) {

        if(getChildIndex(nvg) == -1) children.add(nvg);
    }

    // Find where a child is in the children arraylist. If it is not present, return -1
    public int getChildIndex(NodeViewGroup nvg) {

        for(int i=0; i<children.size(); i++) {

            if(nvg.equals(children.get(i))) return i;
        }

        return -1;
    }

    // Get the list of children associated with this group
    public ArrayList<NodeViewGroup> getChildren() {

        return children;
    }

    // If the nodes of these two groups are equal, then these two groups are equal
    public boolean equals(NodeViewGroup nvg) {

        if(nvg == null) return false;

        if(getNode().equals(nvg.getNode())) {

            if(parent == null && nvg.getParentView() == null) return true;
            return parent.equals(nvg.getParentView());
        }
        else return false;
    }

    // Returns the node associated with this group
    public Node getNode() {

        return nodeView.getNode();
    }

    public NodeViewGroup getParentView() {

        return parent;
    }

    // Get where this group is placed on the screen
    public float getX() {

        return x;
    }

    public float getY() {

        return y;
    }

    // Set where this is placed on the screen
    public void setX(float x) {

        this.x = x;

        invalidate();
        requestLayout();
    }

    public void setY(float y) {

        this.y = y;

        invalidate();
        requestLayout();
    }

    public boolean isParentOf(NodeViewGroup nvg) {

        int count = children.size();

        for(int i=0; i<count; i++) {

            NodeViewGroup nodeViewGroup = children.get(i);

            if(nodeViewGroup.equals(nvg)) return true;
        }

        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // Resets the placement of the nodeview and the expansion button as well
        // as resizing the text and recalculating the text positioning in case of zooming
        nodeView.setX(0);
        nodeView.setY(0);

        float fontSize = NodeView.dim/8 - NodeView.dim*nodeView.getText().length()/80;
        nodeView.setTextSize(fontSize);
        nodeView.setPadding((int)NodeView.dim/3-nodeView.getText().length()*(int)(fontSize/3),
                (int)NodeView.dim/3-(int)(fontSize/2), 0, 0);

        // Place nodeview and expansion button on screen
        nodeView.layout(0,0,(int)NodeView.dim, (int)NodeView.dim);
        iv.layout((int)iv.getX(), (int)iv.getY(), (int)(iv.getX()+NodeView.dim/3), (int)(iv.getY()+NodeView.dim/3));

        /* For some reason, the numbers required to set the original placement of the button are different
         * than the numbers required to set the button in the same position upon layout, so this is placed
         * here after the button has been laid out to account for that such that it will have the correct
         * numbers next time it places the button on the screen. */

        iv.setX(NodeView.dim);
        iv.setY(NodeView.dim/3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension((int)(NodeView.dim + NodeView.dim), (int)(NodeView.dim + NodeView.dim/2));
    }

    // Saves having to write out invalidate and requestlayout every time I need to draw the group again
    public void invalidateAndRequestLayout() {

        nodeView.invalidate();
        nodeView.requestLayout();

        iv.invalidate();
        iv.requestLayout();

        invalidate();
        requestLayout();
    }
}