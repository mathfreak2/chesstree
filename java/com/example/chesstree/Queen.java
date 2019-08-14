package com.example.chesstree;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class Queen extends Piece {

    public Queen(boolean isWhite, ChessBoardActivity activity, int rank, int column, float density) {

        super(isWhite, activity, rank, column, density);
    }

    public Queen(boolean isWhite, int rank, int column) {

        super(isWhite, rank, column);
    }

    public void draw(ConstraintLayout cl) {

        if(color) iv.setImageResource(R.drawable.white_queen);
        else iv.setImageResource(R.drawable.black_queen);
        iv = super.draw(iv);

        class QueenOnClick implements View.OnClickListener {

            private QueenOnClick() {

            }

            @Override
            public void onClick(View v) {

                if(a.getShowingMoves()) a.hideAvailable();

                if((color && a.getMove() == 'w') || (!color && a.getMove() == 'b')) {

                    upRight(r+1, c+1);
                    upLeft(r+1, c-1);
                    downRight(r-1, c+1);
                    downLeft(r-1, c-1);
                    up(r+1);
                    down(r-1);
                    right(c+1);
                    left(c-1);
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

            private void up(int rank) {

                if(rank > 8) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+c-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, c);
                    up(rank+1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, c);
            }

            private void down(int rank) {

                if(rank < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+c-1);

                if(p == null) {

                    a.markAsAvailable(r, c, rank, c);
                    down(rank-1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, rank, c);
            }

            private void left(int column) {

                if(column < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-r)+column-1);

                if(p == null) {

                    a.markAsAvailable(r, c, r, column);
                    left(column-1);
                    return;
                }

                if(p.getColor() != color) a.markAsAvailable(r, c, r, column);
            }

            private void right(int column) {

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

        class QueenOnDrag implements View.OnDragListener {

            private QueenOnClick q;

            private QueenOnDrag(QueenOnClick qoc) {

                q = qoc;
            }

            @Override
            public boolean onDrag(View v, DragEvent event) {

                //TODO
                return true;
            }
        }

        QueenOnClick qoc = new QueenOnClick();
        QueenOnDrag qod = new QueenOnDrag(qoc);
        iv.setOnClickListener(qoc);
        iv.setOnDragListener(qod);
        cl.addView(iv);
    }

    public char getPieceType() {

        return 'Q';
    }

    public boolean canMoveTo(ArrayList<Piece> bs, String fen, int rank, int column) {

        boolean isWhiteTurn = true;
        if(fen.split(" ")[1].equals("b")) isWhiteTurn = false;

        if(color != isWhiteTurn) return false;

        if(rank - r != column - c && rank - r != c - column)
            if(rank - r != 0 && column - c != 0) return false;
        if(rank == r && column == c) return false;
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

            if(canMoveTo(bs, fen, r+i, c+i)) return true;
            if(canMoveTo(bs, fen,r+i, c-i)) return true;
            if(canMoveTo(bs, fen,r-i, c+i)) return true;
            if(canMoveTo(bs, fen,r-i, c-i)) return true;
            if(canMoveTo(bs, fen,r+i,c)) return true;
            if(canMoveTo(bs, fen,r-i,c)) return true;
            if(canMoveTo(bs, fen, r,c+i)) return true;
            if(canMoveTo(bs, fen, r,c-i)) return true;
        }

        return false;
    }
}