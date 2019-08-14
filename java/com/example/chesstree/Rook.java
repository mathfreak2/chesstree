package com.example.chesstree;

import android.support.constraint.ConstraintLayout;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class Rook extends Piece {

    public Rook(boolean isWhite, ChessBoardActivity activity, int rank, int column, float density) {

        super(isWhite, activity, rank, column, density);
    }

    public Rook(boolean isWhite, int rank, int column) {

        super(isWhite, rank, column);
    }

    public void draw(ConstraintLayout cl) {

        if(color) iv.setImageResource(R.drawable.white_rook);
        else iv.setImageResource(R.drawable.black_rook);
        iv = super.draw(iv);

        class RookOnClick implements View.OnClickListener {

            private RookOnClick() {

            }

            @Override
            public void onClick(View v) {

                if(a.getShowingMoves()) a.hideAvailable();

                if((color && a.getMove() == 'w') || (!color && a.getMove() == 'b')) {

                    up(r+1);
                    down(r-1);
                    right(c+1);
                    left(c-1);
                }
            }

            public void up(int rank) {

                if(rank > 8) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+c-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, c);
                    up(rank+1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, c);
            }

            public void down(int rank) {

                if(rank < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+c-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, c);
                    down(rank-1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, c);
            }

            public void left(int column) {

                if(column < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-r)+column-1);

                if(p == null) {

                    a.markAsAvailable(r, c, r, column);
                    left(column-1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, r, column);
            }

            public void right(int column) {

                if(column > 8) return;

                Piece p = a.getPositionAsArray().get(8*(8-r)+column-1);

                if(p == null) {

                    a.markAsAvailable(r, c, r, column);
                    right(column+1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, r, column);
            }
        }

        class RookOnDrag implements View.OnDragListener {

            private RookOnClick r;

            private RookOnDrag(RookOnClick roc) {

                r = roc;
            }

            @Override
            public boolean onDrag(View v, DragEvent event) {

                //TODO
                return true;
            }
        }

        RookOnClick roc = new RookOnClick();
        RookOnDrag rod = new RookOnDrag(roc);
        iv.setOnClickListener(roc);
        iv.setOnDragListener(rod);
        cl.addView(iv);
    }

    public char getPieceType() {

        return 'R';
    }

    public boolean canMoveTo(ArrayList<Piece> bs, String fen, int rank, int column) {

        boolean isWhiteTurn = true;
        if(fen.split(" ")[1].equals("b")) isWhiteTurn = false;

        if(color != isWhiteTurn) return false;

        if(r-rank!=0 && c-column!=0) return false;
        if(r==rank && c==column) return false;
        if(rank > 8 || column > 8 || rank < 1 || column < 1) return false;

        // Up
        for(int i=1; ;i++) {

            if(r + i > 8) break;
            if(bs.get(8*(8-r-i)+c-1) == null ||
                    bs.get(8*(8-r-i)+c-1).getColor() != color)
                if(r + i == rank)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r-i)+c-1) != null) break;
        }

        // Down
        for(int i=1; ;i++) {

            if(r - i < 1) break;
            if(bs.get(8*(8-r+i)+c-1) == null ||
                    bs.get(8*(8-r+i)+c-1).getColor() != color)
                if(r - i == rank)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r+i)+c-1) != null) break;
        }

        // Right
        for(int i=1; ;i++) {

            if(c + i > 8) break;
            if(bs.get(8*(8-r)+c-1+i) == null ||
                    bs.get(8*(8-r)+c-1+i).getColor() != color)
                if(c + i == column)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r)+c-1+i) != null) break;
        }

        // Left
        for(int i=1; ;i++) {

            if(c - i < 1) break;
            if(bs.get(8*(8-r)+c-1-i) == null ||
                    bs.get(8*(8-r)+c-1-i).getColor() != color)
                if(c - i == column)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r)+c-1-i) != null) break;
        }

        return false;
    }

    public boolean canMove(ArrayList<Piece> bs, String fen) {

        for(int i=1;i<=8;i++) {

            if(canMoveTo(bs, fen,r+i,c)) return true;
            if(canMoveTo(bs, fen,r-i,c)) return true;
            if(canMoveTo(bs, fen,r,c+i)) return true;
            if(canMoveTo(bs, fen,r,c-i)) return true;
        }

        return false;
    }
}