package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.Collection;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    Piece(final PieceType pieceType,
          final int piecePosition,
          final Alliance pieceAlliance,
          final boolean isFirstMove){
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

    private int computeHashCode() {
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals (final Object other){
        if(this == other){ //if the two objects are referentially equal, they will have object equality by defintion
            return true;
        }
        if(!(other instanceof Piece)){ //if the thing comparing against is not a piece
            return false;
        }
        final Piece otherPiece = (Piece) other;
        //casts other as piece, because if we made it through both if statements, it must be a piece
        return piecePosition == otherPiece.getPiecePosition() && pieceType == otherPiece.getPieceType() &&
               pieceAlliance == otherPiece.getPieceAlliance() && isFirstMove == otherPiece.isFirstMove();
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }
    /*HashCode is important for efficient retrieval and storage of objects in hash-based data structures
    it is also important to override both your hashcode and equals methods so that you can make sure that objects with
    the same logical value will produce the same hash code, or be objectively equal*/

    public int getPiecePosition (){
        return this.piecePosition;
    }

    public Alliance getPieceAlliance (){
        return this.pieceAlliance;
    }

    public boolean isFirstMove(){
        return this.isFirstMove;
    }

    public PieceType getPieceType(){
        return this.pieceType;
    }

    public abstract Piece movePiece (Move move);

    public abstract Collection<Move> calculateLegalMoves(final Board board);

    public int getPieceValue() {
        return this.pieceType.getPieceValue();
    }

    public enum PieceType {

        BISHOP("B", 300) {
            @Override
            public boolean isKing(){
                return false;
            }

            @Override
            public boolean isRook() {
                return false;
            }
        },
        KING("K", 10000) {
            @Override
            public boolean isKing() {
                return true;
            }
            @Override
            public boolean isRook() {
                return false;
            }
        },
        KNIGHT("N", 300) {
            @Override
            public boolean isKing() {
                return false;
            }
            @Override
            public boolean isRook() {
                return false;
            }
        },
        PAWN("P", 100) {
            @Override
            public boolean isKing() {
                return false;
            }
            @Override
            public boolean isRook() {
                return false;
            }
        },
        QUEEN("Q", 900) {
            @Override
            public boolean isKing() {
                return false;
            }
            @Override
            public boolean isRook() {
                return false;
            }
        },
        ROOK("R", 500) {
            @Override
            public boolean isKing() {
                return false;
            }
            @Override
            public boolean isRook() {
                return true;
            }
        };

        private String pieceName;
        private int pieceValue;

        PieceType(final String pieceName, final int pieceValue) {
            this.pieceName = pieceName;
            this.pieceValue = pieceValue;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        public int getPieceValue() {
            return this.pieceValue;
        }
        public abstract boolean isKing();

        public abstract boolean isRook();
    }

}

