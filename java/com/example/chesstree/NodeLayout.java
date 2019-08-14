package com.example.chesstree;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class NodeLayout extends ViewGroup {

    // Number of levels of nodes in the tree to determine measurement
    private int numLevels = 0;

    // Screen the layout is on
    public MainActivity c;

    // Padding between children on screen
    public int pad = (int)(NodeView.dim*2)/3;
    private Paint mPaint;

    // List of NodeViewGroups which have been added to the tree even if they aren't
    // currently showing on screen
    private ArrayList<NodeViewGroup> children = new ArrayList<>();

    // Variables used in the positioning of nodes on screen. Reset upon finishing positioning
    private static int count = 0;
    private static float minY = 0;

    public NodeLayout(Context context) {

        super(context);
        c = (MainActivity) context;
        mPaint = new Paint();

        // Sets how thick the line is between nodes
        mPaint.setStrokeWidth(9);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public NodeLayout(Context context, AttributeSet attrs) {

        super(context, attrs);
        c = (MainActivity) context;
        mPaint = new Paint();

        // Sets how thick the line is between nodes
        mPaint.setStrokeWidth(9);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // Displays all NodeViewGroups currently on screen
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final NodeViewGroup child = (NodeViewGroup) getChildAt(i);
            if (child.getVisibility() != GONE) {

                child.layout((int)child.getX(), (int)child.getY(), (int)(child.getX()+NodeView.dim*4/3), (int)(child.getY()+NodeView.dim));
            }
        }
    }

    // Gets the index of a particular nodeviewgroup in the master list of all nodeviewgroups created
    public int getNodeViewGroupIndex(NodeViewGroup nvg) {

        for(int i=0; i<children.size(); i++) {

            if(nvg.equals(children.get(i))) return i;
        }

        return -1;
    }

    // Add a node to the screen, called when a node is expanded
    public void addNodeViewGroup(NodeViewGroup child) {

        addView(child);
        if(getNodeViewGroupIndex(child) == -1) children.add(child);

        // If this node is expanded, also display its children
        if(child.getExpanded()) {

            int count = child.getChildren().size();

            for(int i=0; i<count; i++) {

                NodeViewGroup nvg = child.getChildren().get(i);

                // Recursive call to display the children of this node
                addNodeViewGroup(nvg);

                nvg.invalidate();
                nvg.requestLayout();
            }
        }

        invalidate();
        requestLayout();
    }

    // Remove nodes from the screen, called when a node is contracted
    public void removeNodeViewGroup(NodeViewGroup child) {

        int count = getChildCount();

        for(int i=0; i<count; i++) {

            NodeViewGroup nvg = (NodeViewGroup) getChildAt(i);

            if(nvg.equals(child)) {

                removeView(nvg);
                int countchild = nvg.getChildren().size();

                for(int j=0; j<countchild; j++) {

                    NodeViewGroup nvg2 = nvg.getChildren().get(j);

                    // Recursive call to remove children of this node from the screen
                    removeNodeViewGroup(nvg2);

                    invalidate();
                    requestLayout();
                }

                break;
            }

            invalidate();
            requestLayout();
        }
    }

    // Returns the master list of all nodeviewgroups currently existing whether or not they are being displayed
    public ArrayList<NodeViewGroup> getChildren() {

        return children;
    }

    // Draws a line connecting every node with its children nodes on screen
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int count = getChildCount();

        for(int i=0; i<count; i++) {

            NodeViewGroup nvg = (NodeViewGroup) getChildAt(i);

            if(nvg.getExpanded()) {

                for(int j=0; j<count; j++) {

                    NodeViewGroup nvg2 = (NodeViewGroup) getChildAt(j);

                    if(nvg.isParentOf(nvg2)) {

                        canvas.drawLine(nvg.getX()+nvg.getWidth(),
                                nvg.getY()+nvg.getHeight()/(float)2,
                                nvg2.getX(),
                                nvg2.getY()+nvg2.getHeight()/(float)2,
                                mPaint);
                    }
                }
            }
        }
    }

    // Used to find the index of the parent in the master list of nodeviewgroups of the given nodeviewgroup
    public int findParentOf(NodeViewGroup nvg) {

        if(nvg.getNode().isBaseNode()) return -1;

        for(int i=0; i<children.size(); i++) {

            if(children.get(i).isParentOf(nvg)) return i;
        }

        return -1;
    }

    // Add a child to the master list if it is not already present
    public void addChild(NodeViewGroup nvg) {

        for(int i=0; i<children.size(); i++) {

            if(children.get(i).equals(nvg)) return;
        }

        children.add(nvg);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int count = getChildCount();
        setWillNotDraw(false);
        pad = (int)(NodeView.dim*3)/4;

        if(count == 0) {setMeasuredDimension(0,0); return;}

        int height = 0;
        int width = 0;

        for(int i=0; i<count; i++) {

            NodeViewGroup nvg = (NodeViewGroup) getChildAt(i);

            // Set the position of the base node and recurses through all other nodes from that
            if(nvg.getNode().isBaseNode())
                setPosition(nvg, (float)(c.screenX/3)-NodeView.dim/2, (float)(c.screenY/3)-NodeView.dim/2);

            numLevels = Math.max(numLevels, nvg.getNodeView().getLevel());
        }

        // Measures width and height based on the highest x and y of its children
        for(int i=0; i<count; i++) {

            NodeViewGroup nvg = (NodeViewGroup) getChildAt(i);
            height = Math.max(height, (int)(nvg.getY()));
            width = Math.max(width, (int)(nvg.getX()));
        }

        // Accounts for the dimension of the NodeView and adds an extra padding to make sure everything is drawn
        height += NodeView.dim + pad*NodeView.dim;
        width += (NodeView.dim*3)/2 + pad*NodeView.dim;

        // Increases the height and width of the parent layout to allow for content bigger than the screen is
        ConstraintLayout sl = c.findViewById(R.id.cl_main);
        sl.setMinWidth(width); sl.setMinHeight(height);

        // Finally, set the measurements
        setMeasuredDimension(width, height);
    }

    // Does the hard work of positioning the nodes on the screen
    public void setPosition(NodeViewGroup nvg, float left, float top) {

        if((nvg.getNode().getChildren().size() == 1) || nvg.getNode().isBaseNode()) nvg.setExpanded(true);

        // Get the amount to iterate the x and y coordinates by
        float dim = NodeView.dim + pad;

        // If this node is not a leaf node, then set the x and y coords of its children
        // and only then determine its own coords based on the median y of its children
        if(nvg.getExpanded() && nvg.getChildren().size() > 0) {

            int num = nvg.getChildren().size();

            for(int i=0; i<num; i++) {

                // Recursive call to determine the x and y coords of this node's children
                setPosition(nvg.getChildren().get(i), left+dim, top-dim*(num-1)/2+dim*i);
            }

            // Sets this node's position based on its children nodes' coordinates
            nvg.setX(left);
            nvg.setY((nvg.getChildren().get(num-1).getY()+nvg.getChildren().get(0).getY())/(float)2);
        }
        // If this node is a leaf node, then iterate count such that each subsequent leaf node's y coordinate
        // holds a linearly increasing pattern so that no two nodes can be too close or overlapping with each other
        else {

            // If this is the first leaf node, set the minimum y value to iterate the subsequent leaf nodes from
            if(minY == 0) minY = top;
            nvg.setX(left);

            // This insures no nodes overlap with each other since each leaf node will be in a different place
            nvg.setY(minY+count*dim);

            // Iterate count to determine the next leaf node position
            count++;
        }

        // Log the position and screen coords for informational purposes
        Log.v("Node Position", nvg.getNode().getPosition()+", "+nvg.getX()+", "+nvg.getY());

        /* Since this method is only ever called on the base node, the following line of code insures
         * that the rest of the code beyond it is only called once and not called during any recursive
         * instance of this method. */

        if(!nvg.getNode().isBaseNode()) return;

        /* If the minimum y value is below dim, then that means it is either too close to the edge
         * of the screen, or it will be cut off by the end of the screen. This loop will add a
         * a value to all nodes that will insure the top most node will occur at y = dim if such
         * a situation occurs. */

        if(minY < dim) {

            for(int i=0; i<children.size(); i++) {

                children.get(i).setY(children.get(i).getY()+dim-minY);
            }
        }

        count = 0;
        minY = 0;
    }

    // Saves having to write the same code over and over
    public void invalidateAndRequestLayout() {

        int count = getChildCount();

        for(int i=0; i<count; i++) {

            ((NodeViewGroup)getChildAt(i)).invalidateAndRequestLayout();
        }

        invalidate();
        requestLayout();
    }
}