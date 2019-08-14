package com.example.chesstree;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class Knight extends Piece {

    public Knight(boolean isWhite, ChessBoardActivity activity, int rank, int column, float density) {

        super(isWhite, activity, rank, column, density);
    }

    public Knight(boolean isWhite, int rank, int column) {

        super(isWhite, rank, column);
    }

    public void draw(ConstraintLayout cl) {

        if(color) iv.setImageResource(R.drawable.white_knight);
        else iv.setImageResource(R.drawable.black_knight);
        iv = super.draw(iv);

        class KnightOnClick implements View.OnClickListener {

            private KnightOnClick() {

            }

            @Override
            public void onClick(View v) {

                if(a.getShowingMoves()) a.hideAvailable();

                if((color && a.getMove() == 'w') || (!color && a.getMove() == 'b')) {

                    checkMove(r+2, c+1);
                    checkMove(r+2, c-1);
                    checkMove(r+1, c+2);
                    checkMove(r+1, c-2);
                    checkMove(r-1, c+2);
                    checkMove(r-1, c-2);
                    checkMove(r-2, c+1);
                    checkMove(r-2, c-1);
                }
            }

            private void checkMove(int rank, int column) {

                if(rank > 8 || rank < 1 || column > 8 || column < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+column-1);

                if(p == null) {a.markAsAvailable(r, c, rank, column); return;}

                if(p.getColor() == color) return;

                a.markAsAvailable(r, c, rank, column);
            }
        }

        class KnightOnDrag implements View.OnDragListener {

            private KnightOnClick n;

            private KnightOnDrag(KnightOnClick noc) {

                n = noc;
            }

            @Override
            public boolean onDrag(View v, DragEvent event) {

                //TODO
                return true;
            }
        }
        KnightOnClick noc = new KnightOnClick();
        KnightOnDrag nod = new KnightOnDrag(noc);
        iv.setOnClickListener(noc);
        iv.setOnDragListener(nod);
        cl.addView(iv);
    }

    public char getPieceType() {

        return 'N';
    }

    public boolean canMoveTo(ArrayList<Piece> bs, String fen, int rank, int column) {

        boolean isWhiteTurn = true;
        if(fen.split(" ")[1].equals("b")) isWhiteTurn = false;

        if(color != isWhiteTurn) return false;

        if(Math.abs(r - rank) + Math.abs(c - column) != 3) return false;
        if(rank > 8 || column > 8 || rank < 1 || column < 1) return false;

        if(r < 8 && c < 7 && r+1==rank && c+2==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
            bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        if(r < 8 && c > 2 && r+1==rank && c-2==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
                    bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        if(r < 7 && c < 8 && r+2==rank && c+1==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
                    bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        if(r < 7 && c > 1 && r+2==rank && c-1==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
                    bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        if(r > 2 && c < 8 && r-2==rank && c+1==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
                    bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        if(r > 2 && c > 1 && r-2==rank && c-1==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
                    bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        if(r > 1 && c < 7 && r-1==rank && c+2==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
                    bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        if(r > 1 && c > 2 && r-1==rank && c-2==column)
            if(bs.get(8*(8-rank)+column-1) == null ||
                    bs.get(8*(8-rank)+column-1).getColor() != color)
                return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

        return false;
    }

    public boolean canMove(ArrayList<Piece> bs, String fen) {

        if(canMoveTo(bs, fen,r+2, c+1)) return true;
        if(canMoveTo(bs, fen,r+2, c-1)) return true;
        if(canMoveTo(bs, fen,r+1, c+2)) return true;
        if(canMoveTo(bs, fen,r+1, c-2)) return true;
        if(canMoveTo(bs, fen,r-1, c+2)) return true;
        if(canMoveTo(bs, fen,r-1, c-2)) return true;
        if(canMoveTo(bs, fen,r-2, c+1)) return true;
        return (canMoveTo(bs, fen,r-2, c-1));
    }
}