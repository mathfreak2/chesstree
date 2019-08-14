package com.example.chesstree;

import android.support.constraint.ConstraintLayout;
import android.view.DragEvent;
import android.view.View;

import java.util.ArrayList;

public class King extends Piece {

    public King(boolean isWhite, ChessBoardActivity activity, int rank, int column, float density) {

        super(isWhite, activity, rank, column, density);
    }

    public King(boolean isWhite, int rank, int column) {

        super(isWhite, rank, column);
    }

    public void draw(ConstraintLayout cl) {

        if(color) iv.setImageResource(R.drawable.white_king);
        else iv.setImageResource(R.drawable.black_king);
        iv = super.draw(iv);

        class KingOnClick implements View.OnClickListener {

            private KingOnClick() {

            }

            @Override
            public void onClick(View v) {

                if(a.getShowingMoves()) a.hideAvailable();

                if((color && a.getMove() == 'w') || (!color && a.getMove() == 'b')) {

                    checkMove(r+1, c);
                    checkMove(r+1, c+1);
                    checkMove(r, c+1);
                    checkMove(r-1, c+1);
                    checkMove(r-1, c);
                    checkMove(r-1, c-1);
                    checkMove(r, c-1);
                    checkMove(r+1, c-1);
                    castle();
                }
            }

            private void checkMove(int rank, int column) {

                if(rank > 8 || column > 8 || rank < 1 || column < 1) return;

                Piece p = a.getPositionAsArray().get(8*(8-rank)+column-1);

                if(p == null) {a.markAsAvailable(r, c, rank, column); return;}

                if(p.getColor() == color) return;

                a.markAsAvailable(r, c, rank, column);
            }

            private void castle() {

                if(c != 5) return;

                String ca = a.getCastles();
                if(ca.equals("-")) return;
                int c1 = ca.indexOf("K");
                int c2 = ca.indexOf("Q");
                int c3 = ca.indexOf("k");
                int c4 = ca.indexOf("q");

                //Kingside
                Piece p1 = a.getPositionAsArray().get(8*(8-r)+c);
                Piece p2 = a.getPositionAsArray().get(8*(8-r)+c+1);

                //Queenside
                Piece p3 = a.getPositionAsArray().get(8*(8-r)+c-2);
                Piece p4 = a.getPositionAsArray().get(8*(8-r)+c-3);
                Piece p5 = a.getPositionAsArray().get(8*(8-r)+c-4);

                if(color && c1 != -1 && p1 == null && p2 == null)
                    a.markAsAvailable(r, c, r, c+2);
                if(color && c2 != -1 && p3 == null && p4 == null && p5 == null)
                    a.markAsAvailable(r, c, r, c-2);
                if(!color && c3 != -1 && p1 == null && p2 == null)
                    a.markAsAvailable(r, c, r, c+2);
                if(!color && c4 != -1 && p3 == null && p4 == null && p5 == null)
                    a.markAsAvailable(r, c, r, c-2);
            }
        }

        class KingOnDrag implements View.OnDragListener {

            private KingOnClick k;

            private KingOnDrag(KingOnClick koc) {

                k = koc;
            }

            @Override
            public boolean onDrag(View v, DragEvent event) {

                //TODO
                return true;
            }
        }

        KingOnClick koc = new KingOnClick();
        KingOnDrag kod = new KingOnDrag(koc);
        iv.setOnClickListener(koc);
        iv.setOnDragListener(kod);
        cl.addView(iv);
    }

    public char getPieceType() {

        return 'K';
    }

    public boolean canMoveTo(ArrayList<Piece> bs, String fen, int rank, int column) {

        boolean isWhiteTurn = true;
        if(fen.split(" ")[1].equals("b")) isWhiteTurn = false;

        if(color != isWhiteTurn) return false;

        if(rank > 8 || column > 8 || rank < 1 || column < 1) return false;
        String ca = fen.split(" ")[2];

        if(c == 5 && !ca.equals("-") && r==rank) {

            int c1 = ca.indexOf("K");
            int c2 = ca.indexOf("Q");
            int c3 = ca.indexOf("k");
            int c4 = ca.indexOf("q");

            //Kingside
            Piece p1 = bs.get(8*(8-r)+c);
            Piece p2 = bs.get(8*(8-r)+c+1);

            //Queenside
            Piece p3 = bs.get(8*(8-r)+c-2);
            Piece p4 = bs.get(8*(8-r)+c-3);
            Piece p5 = bs.get(8*(8-r)+c-4);

            if(c+2==column) {

                // Kingside castle option if king is white
                if(color && c1 != -1 && p1 == null && p2 == null) {

                    if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c), color))
                        if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c+1), color))
                            return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, column), color));

                    return false;
                }

                // Kingside castle option if king is black
                if(!color && c3 != -1 && p1 == null && p2 == null) {

                    if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c), color))
                        if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c+1), color))
                             return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c+2), color));

                    return false;
                }
            }
            if(c-2==column) {

                // Queenside castle option if king is white
                if(color && c2 != -1 && p3 == null && p4 == null && p5 == null) {

                    if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c), color))
                        if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c-1), color))
                            return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c-2), color));

                    return false;
                }

                // Queenside castle option if king is black
                if(!color && c4 != -1 && p3 == null && p4 == null && p5 == null) {

                    if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c), color))
                        if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c-1), color))
                            return (!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, r, c-2), color));

                    return false;
                }
            }
        }

        if(Math.abs(r - rank) > 1 || Math.abs(c - column) > 1) return false;
        if(r == rank && c == column) return false;

        if(r < 8 && c < 8 && r+1==rank && c+1==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        if(r < 8 && r+1==rank && c==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        if(r < 8 && c > 1 && r+1==rank && c-1==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        if(c < 8 && r==rank && c+1==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        if(c > 1 && r==rank && c-1==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        if(r > 1 && c < 8 && r-1==rank && c+1==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        if(r > 1 && r-1==rank && c==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        if(r > 1 && c > 1 && r-1==rank && c-1==column)
            if(!ChessBoardActivity.checkChecks(ChessBoardActivity.testBoardState(bs, this, rank, column), color))
                return true;

        return false;
    }

    public boolean canMove(ArrayList<Piece> bs, String fen) {

        if(canMoveTo(bs, fen,r+1, c+1)) return true;
        if(canMoveTo(bs, fen,r+1, c)) return true;
        if(canMoveTo(bs, fen,r+1, c-1)) return true;
        if(canMoveTo(bs, fen,r, c+1)) return true;
        if(canMoveTo(bs, fen,r, c-1)) return true;
        if(canMoveTo(bs, fen,r-1, c+1)) return true;
        if(canMoveTo(bs, fen,r-1, c)) return true;
        if(canMoveTo(bs, fen,r-1, c-1)) return true;
        if(canMoveTo(bs, fen,r, c+2)) return true;
        return (canMoveTo(bs, fen,r, c-2));
    }
}