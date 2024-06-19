package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.*;
import java.util.List;

import static com.chess.engine.board.Move.*;
import static com.chess.engine.board.BoardUtils.isValidTileCoordinate;

public class Knight extends Piece{

    private static final int[] CANDIDATE_MOVE_COORDINATE = {-17, -15, 10, -6, 6, 10, 15, 17};
    //the possible positions for a knight given that it can make any move possible (all 8);
    public Knight(final Alliance pieceAlliance,
                  final int piecePosition) {
        super(PieceType.KNIGHT ,piecePosition, pieceAlliance, true);
    }

    public Knight(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.KNIGHT, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Knight movePiece(Move move) {
        return new Knight(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
    }
    @Override
    public Collection<Move> calculateLegalMoves(final Board board){
        final List<Move> legalMoves = new ArrayList<>();

        for(final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATE) {
            int candidateDestinationCoordinate = this.piecePosition + currentCandidateOffset; /*the location of the
            current piece is the current position plus the current candidate aka the piece*/
            if (isValidTileCoordinate(candidateDestinationCoordinate)) {
                if(isFirstColumnException(this.piecePosition, currentCandidateOffset) ||
                        isSecondColumnException(this.piecePosition, currentCandidateOffset) ||
                isSeventhColumnException(this.piecePosition, currentCandidateOffset) ||
                isEighthColumnException(this.piecePosition, currentCandidateOffset)){
                    continue;
                }

                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate); /* the candidate destination
                tile is now the time retrieved from the board class at the candidate destination coordinate*/
            if (!candidateDestinationTile.isTileOccupied()) {//if the tile is valid and the tile is unoccupied, it's a legal move
                legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
            } else {
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();/*the piece at the current destination
                    is equal to the destination tile retrieving the get piece function*/
                    final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();//gets the alliance of a piece
                    if(this.pieceAlliance != pieceAlliance) { //capture of an enemy piece
                        legalMoves.add(new AttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public String toString(){
        return PieceType.KNIGHT.toString();
    }

    private static boolean isFirstColumnException (final int currentPosition, final int candidateOffset){
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -17 || candidateOffset == -10 ||
                candidateOffset == 6 || candidateOffset == 15);
        /*in the first column (the A file), these are all the moves that are 100% not legal no matter where you are on the
        first column*/
    }
    private static boolean isSecondColumnException (final int currentPosition, final int candidateOffset) {
        return BoardUtils.SECOND_COLUMN[currentPosition] && (candidateOffset == -10 || candidateOffset == 6);
        /*in the second column (the B file), these are all the moves that are 100% not legal no matter where you are on the
        second column*/
    }

    private static boolean isSeventhColumnException (final int currentPosition, final int candidateOffset) {
        return BoardUtils.SEVENTH_COLUMN[currentPosition] && (candidateOffset == 6 || candidateOffset == -10);
        /*in the seventh column (the G file), these are all the moves that are 100% not legal no matter where you are on the
        seventh column*/
    }

    private static boolean isEighthColumnException (final int currentPosition, final int candidateOffset) {
        return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateOffset == -15 || candidateOffset == -6||
                candidateOffset == 10 || candidateOffset == 17);
        /*in the Eighth column (the H file), these are all the moves that are 100% not legal no matter where you are on the
        Eighth column*/
    }
}
