package com.example.chesstree;

import android.support.constraint.ConstraintLayout;
import android.widget.ImageView;

import java.util.ArrayList;

public class Piece {

    protected ChessBoardActivity a;
    protected float left;
    protected float top;
    protected float d;
    protected int dim;
    protected boolean color;
    protected int r;
    protected int c;
    protected ImageView iv;

    public Piece(boolean isWhite, ChessBoardActivity activity, int rank, int column, float density) {

        a = activity;
        d = density;
        left = (-11 + 39 * column - (float) (column / 4)) * d;
        if(a.getRotated()) top = (21 + 39 * (rank - 1) - (float)((rank - 1) / 4)) * d;
        else top = (21 + 39 * (8 - rank) - (float) ((8 - rank) / 4)) * d;
        dim = (int) (34 * d);
        color = isWhite;
        r = rank;
        c = column;
        iv = new ImageView(a);
    }

    public Piece(boolean isWhite, int rank, int column) {

        color = isWhite;
        r = rank;
        c = column;
    }

    protected ImageView draw(ImageView iv) {

        iv.setTranslationX(left);
        iv.setTranslationY(top);
        iv.setLayoutParams(new android.view.ViewGroup.LayoutParams(dim, dim));
        return iv;
    }

    public int getRank() {

        return r;
    }

    public void setRank(int rank) {

        r = rank;
        if(a.getRotated()) top = (21 + 39 * (rank - 1) - (float)((rank - 1) / 4)) * d;
        else top = (21 + 39 * (8 - rank) - (float) ((8 - rank) / 4)) * d;
    }

    public int getColumn() {

        return c;
    }

    public void setColumn(int column) {

        c = column;
        left = (-11 + 39 * column - (float) (column / 4)) * d;
    }

    public boolean getColor() {

        return color;
    }

    public char getPieceType() {

        return 'Z';
    }

    public boolean canMoveTo(ArrayList<Piece> bs, String fen, int rank, int column) {

        return false;
    }

    public boolean canMove(ArrayList<Piece> bs, String fen) {

        return false;
    }
}