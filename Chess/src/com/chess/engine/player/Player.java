package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;

public abstract class Player {

    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    Player(final Board board,
           final Collection<Move> legalMoves,
           final Collection<Move> opponentMoves){
        this.board = board;
        this.playerKing = establishKing();
        this.legalMoves = ImmutableList.copyOf(Iterables.concat(legalMoves, calculateKingCastles(legalMoves, opponentMoves)));
        //concatenates the list of legal moves with the list of castle moves made by the king, also taking into account opponentMoves
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentMoves).isEmpty();
        /*uses the method that takes in a piece's position and the moves an opponent can make, if this list is not empty
        there exists a legal move that attacks the king making him in check*/
    }

    protected static Collection<Move> calculateAttacksOnTile(int piecePosition, Collection<Move> moves) {
        final List<Move> attackMoves = new ArrayList<>();
        for(final Move move: moves){ /*goes through the enemy moves, and if there exists a legal move that the enemy can make
            that ends up on the same tile as the piece position this is considered an attacking move, and then adds that
            move to the list*/
            if(piecePosition == move.getDestinationCoordinate()){
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }

    private King establishKing() {
        for(final Piece piece: getActivePieces()){
            if(piece.getPieceType().isKing()){
                return (King) piece;
            }
        }
        throw new RuntimeException("Not a valid position in chess");
    }

    public King getPlayerKing(){
        return this.playerKing;
    }

    public Collection<Move> getLegalMoves(){
        return this.legalMoves;

    }
    public boolean isLegalMove (final Move move){
        return this.legalMoves.contains(move);
    }

    public boolean isInCheck(){
        return this.isInCheck;
    }

    public boolean isInCheckMate(){
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStaleMate(){
        return !this.isInCheck && !hasEscapeMoves();
    }

    public boolean isCastled(){
        return false;
    }

    protected boolean hasEscapeMoves() {
        for(final Move move : this.legalMoves){
            /* goes through the moves that you have with your king, and it makes those moves on an imaginary chess board
            if the move is legal and the get move status returns true then you have an escape move, if you go through all
            the moves and there isn't a move that makes this method return true, then you are effectively in checkmate */
            final MoveTransition transition = makeMove(move);
            if(transition.getMoveStatus().isDone()){
                return true;
            }
        }
        return false;
    }

    public MoveTransition makeMove(final Move move){

        if(!isLegalMove(move)){
            return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
        }

        final Board transitionBoard = move.execute();

        final Collection<Move> kingAttacks = Player.calculateAttacksOnTile(transitionBoard.currentPlayer().
                getOpponent().getPlayerKing().getPiecePosition(), transitionBoard.currentPlayer().getLegalMoves());
        /*After you make a move, you are no longer the current player, so the kingAttacks collection, calculates the
        opponents kings piece position (which is effectively you) and the current players (opponent's) legal moves using the
        attacksOnTile method which, if the position of your king and an opponents legal move is equal, you are in check
        (aka a king attack)*/

        if(!kingAttacks.isEmpty()){/*if the collection of attacks on your king is not empty there is a check so any move
            you make that leaves your king in check will go back to the same board you were at*/
            return new MoveTransition(this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }

        return new MoveTransition(transitionBoard, move, MoveStatus.DONE);
    }

    public abstract Collection<Piece> getActivePieces();
    public abstract Alliance getAlliance();
    public abstract Player getOpponent();
    protected abstract Collection<Move> calculateKingCastles (Collection<Move> playerLegals, Collection<Move> opponentsLegals);
}
