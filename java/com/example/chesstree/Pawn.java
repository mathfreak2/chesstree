package com.example.chesstree;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class Pawn extends Piece {

    public Pawn(boolean isWhite, ChessBoardActivity activity, int rank, int column, float density) {

        super(isWhite, activity, rank, column, density);
    }

    public Pawn(boolean isWhite, int rank, int column) {

        super(isWhite, rank, column);
    }

    public void draw(ConstraintLayout cl) {

        if(color) iv.setImageResource(R.drawable.white_pawn);
        else iv.setImageResource(R.drawable.black_pawn);
        iv = super.draw(iv);

        class PawnOnClick implements View.OnClickListener {

            private PawnOnClick() {

            }

            @Override
            public void onClick(View v) {

                if(a.getShowingMoves()) a.hideAvailable();

                if(color && a.getMove() == 'w') {

                    attack(r+1, c-1);
                    attack(r+1, c+1);
                    forward(r+1);
                    enPassant();
                }

                if(!color && a.getMove() == 'b') {

                    attack(r-1, c-1);
                    attack(r-1, c+1);
                    forward(r-1);
                    enPassant();
                }
            }

            private void attack(int rank, int column) {

                if(column > 8 || column < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+column-1);

                if(p == null) return;
                if(p.getColor() != color) a.markAsAvailable(r, c, rank, column);
            }

            private void forward(int rank) {

                Piece p = a.getPositionAsArray().get(8*(8-rank)+c-1);

                if(p == null) a.markAsAvailable(r, c, rank, c);

                // The next two conditionals make it possible to move a pawn that
                // hasn't already moved yet two spaces forward.
                if(color && r == 2) {

                    Piece p2 = a.getPositionAsArray().get(8*(8-rank-1)+c-1);
                    if(p2 == null) a.markAsAvailable(r, c, rank+1, c);
                }

                if(!color && r == 7) {

                    Piece p2 = a.getPositionAsArray().get(8*(8-rank+1)+c-1);
                    if(p2 == null) a.markAsAvailable(r, c, rank-1, c);
                }
            }

            private void enPassant() {

                if((color && r == 5) || (!color && r == 4)) {

                    String ep = a.getEnPassant();
                    if(ep.equals("-")) return;
                    int epc = ChessBoardActivity.COLUMN_CODE.get(ep.charAt(0));
                    int epr = Character.getNumericValue(ep.charAt(1));

                    if(c == epc+1 || c == epc-1) {

                        if(color && epr == 6) a.markAsAvailable(r, c, r+1, epc);
                        if(!color && epr == 3) a.markAsAvailable(r, c, r-1, epc);
                    }

                }
            }
        }

        class PawnOnDrag implements View.OnDragListener {

            private PawnOnClick p;

            private PawnOnDrag(PawnOnClick poc) {

                p = poc;
            }

            @Override
            public boolean onDrag(View v, DragEvent event) {

                //TODO
                return true;
            }
        }

        PawnOnClick poc = new PawnOnClick();
        PawnOnDrag pod = new PawnOnDrag(poc);
        iv.setOnClickListener(poc);
        iv.setOnDragListener(pod);
        cl.addView(iv);
    }

    public char getPieceType() {

        return 'P';
    }

    public boolean canMoveTo(ArrayList<Piece> bs, String fen, int rank, int column) {

        boolean isWhiteTurn = true;
        if(fen.split(" ")[1].equals("b")) isWhiteTurn = false;

        if(color != isWhiteTurn) return false;

        if((!color && r-2 != rank && r-1 != rank) ||
                (color && r+2 != rank && r+1 != rank) ||
                Math.abs(c - column) > 1) return false;

        if(color) {

            // White pawn moves forward one
            if(r+1==rank && c==column)
                if(bs.get(8*(8-rank)+column-1) == null)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // White pawn moves forward two
            if(r==2 && rank==4 && c==column)
                if(bs.get(39+column) == null)
                    if(bs.get(31+column) == null)
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // White pawn attacks right
            if(c<8 && c+1==column)
                if(bs.get(8*(8-rank)+column-1) != null)
                    if(!bs.get(8*(8-rank)+column-1).getColor())
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // White pawn attacks left
            if(c>1 && c-1==column)
                if(bs.get(8*(8-rank)+column-1) != null)
                    if(!bs.get(8*(8-rank)+column-1).getColor())
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // White pawn attacks right en passant
            if(c<8 && c+1==column && r==5 && rank==6 && !fen.split(" ")[3].equals("-"))
                if(column==ChessBoardActivity.COLUMN_CODE.get(fen.split(" ")[3].charAt(0)))
                    if(rank==Character.getNumericValue(fen.split(" ")[3].charAt(1)))
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // White pawn attacks left en passant
            if(c>1 && c-1==column && r==5 && rank==6 && !fen.split(" ")[3].equals("-"))
                if(column==ChessBoardActivity.COLUMN_CODE.get(fen.split(" ")[3].charAt(0)))
                    if(rank==Character.getNumericValue(fen.split(" ")[3].charAt(1)))
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            return false;
        }
        else {

            // Black pawn moves forward one
            if(r-1==rank && c==column)
                if(bs.get(8*(8-rank)+column-1) == null)
                    return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // Black pawn moves forward two
            if(r==7 && rank==5 && c==column)
                if(bs.get(15+column) == null)
                    if(bs.get(23+column) == null)
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // Black pawn attacks right
            if(c<8 && c+1==column)
                if(bs.get(8*(8-rank)+column-1) != null)
                    if(bs.get(8*(8-rank)+column-1).getColor())
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // Black pawn attacks left
            if(c>1 && c-1==column)
                if(bs.get(8*(8-rank)+column-1) != null)
                    if(bs.get(8*(8-rank)+column-1).getColor())
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // Black pawn attacks right en passant
            if(c<8 && c+1==column && r==4 && rank==3 && !fen.split(" ")[3].equals("-"))
                if(column==ChessBoardActivity.COLUMN_CODE.get(fen.split(" ")[3].charAt(0)))
                    if(rank==Character.getNumericValue(fen.split(" ")[3].charAt(1)))
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            // Black pawn attacks left en passant
            if(c>1 && c-1==column && r==4 && rank==3 && !fen.split(" ")[3].equals("-"))
                if(column==ChessBoardActivity.COLUMN_CODE.get(fen.split(" ")[3].charAt(0)))
                    if(rank==Character.getNumericValue(fen.split(" ")[3].charAt(1)))
                        return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color));

            return false;
        }
    }

    public boolean canMove(ArrayList<Piece> bs, String fen) {

        int direction = 1;
        if(!color) direction = -1;

        if(canMoveTo(bs, fen,r+direction, c)) return true;
        if(canMoveTo(bs, fen,r+2*direction, c)) return true;
        if(canMoveTo(bs, fen,r+direction, c+1)) return true;
        return (canMoveTo(bs, fen,r+direction, c-1));
    }
}