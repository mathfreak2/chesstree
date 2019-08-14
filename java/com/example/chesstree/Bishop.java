package com.example.chesstree;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class Bishop extends Piece {

    public Bishop(boolean isWhite, ChessBoardActivity activity, int rank, int column, float density) {

        super(isWhite, activity, rank, column, density);
    }

    public Bishop(boolean isWhite, int rank, int column) {

        super(isWhite, rank, column);
    }

    public void draw(ConstraintLayout cl) {

        if(color) iv.setImageResource(R.drawable.white_bishop);
        else iv.setImageResource(R.drawable.black_bishop);
        iv = super.draw(iv);

        class BishopOnClick implements View.OnClickListener {

            private BishopOnClick() {

            }

            @Override
            public void onClick(View v) {

                if(a.getShowingMoves()) a.hideAvailable();

                if((color && a.getMove() == 'w') || (!color && a.getMove() == 'b')) {

                    upRight(r+1, c+1);
                    upLeft(r+1,c-1);
                    downRight(r-1, c+1);
                    downLeft(r-1, c-1);
                }
            }

            private void upRight(int rank, int column) {

                if(rank > 8 || column > 8) return; // If checker goes off board, stop recurring

                Piece p = a.getPositionAsArray().get(8*(8-rank)+column-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, column);
                    upRight(rank+1, column+1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, column);
            }

            private void upLeft(int rank, int column) {

                if(rank > 8 || column < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+column-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, column);
                    upLeft(rank+1, column-1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, column);
            }

            private void downRight(int rank, int column) {

                if(rank < 1 || column > 8) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+column-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, column);
                    downRight(rank-1, column+1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, column);
            }

            private void downLeft(int rank, int column) {

                if(rank < 1 || column < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+column-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, column);
                    downLeft(rank-1, column-1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, column);
            }
        }

        class BishopOnDrag implements View.OnDragListener {

            private BishopOnClick b;

            private BishopOnDrag(BishopOnClick boc) {

                b = boc;
            }

            @Override
            public boolean onDrag(View v, DragEvent event) {

                //TODO
                return true;
            }
        }

        BishopOnClick boc = new BishopOnClick();
        BishopOnDrag bod = new BishopOnDrag(boc);
        iv.setOnClickListener(boc);
        iv.setOnDragListener(bod);
        cl.addView(iv);
    }

    public char getPieceType() {

        return 'B';
    }

    public boolean canMoveTo(ArrayList<Piece> bs, String fen, int rank, int column) {

        boolean isWhiteTurn = true;
        if(fen.split(" ")[1].equals("b")) isWhiteTurn = false;

        if(color != isWhiteTurn) return false;

        if(rank - r != column - c && rank - r != c - column) return false;
        if(rank == r && column == r) return false;
        if(rank > 8 || column > 8 || rank < 1 || column < 1) return false;

        // Up and right
        for(int i=1; ;i++) {

            if(r + i > 8 || c + i > 8) break;
            if(bs.get(8*(8-r-i)+c-1+i) == null ||
                    bs.get(8*(8-r-i)+c-1+i).getColor() != color)
                if(r + i == rank && c + i == column)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r-i)+c-1+i) != null) break;
        }

        // Up and left
        for(int i=1; ;i++) {

            if(r + i > 8 || c - i < 1) break;
            if(bs.get(8*(8-r-i)+c-1-i) == null ||
                    bs.get(8*(8-r-i)+c-1-i).getColor() != color)
                if(r + i == rank && c - i == column)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r-i)+c-1-i) != null) break;
        }

        // Down and right
        for(int i=1; ;i++) {

            if(r - i < 1 || c + i > 8) break;
            if(bs.get(8*(8-r+i)+c-1+i) == null ||
                    bs.get(8*(8-r+i)+c-1+i).getColor() != color)
                if(r - i == rank && c + i == column)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r+i)+c-1+i) != null) break;
        }

        // Down and left
        for(int i=1; ;i++) {

            if(r - i < 1 || c - i < 1) break;
            if(bs.get(8*(8-r+i)+c-1-i) == null ||
                    bs.get(8*(8-r+i)+c-1-i).getColor() != color)
                if(r - i == rank && c - i == column)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            if(bs.get(8*(8-r+i)+c-1-i) != null) break;
        }

        return false;
    }

    public boolean canMove(ArrayList<Piece> bs, String fen) {

        for(int i=1;i<=8;i++) {

            if(canMoveTo(bs, fen,r+i, c+i)) return true;
            if(canMoveTo(bs, fen, r+i, c-i)) return true;
            if(canMoveTo(bs, fen,r-i, c+i)) return true;
            if(canMoveTo(bs, fen,r-i, c-i)) return true;
        }

        return false;
    }
}