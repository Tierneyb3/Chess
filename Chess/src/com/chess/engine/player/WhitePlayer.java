package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

import java.util.*;

public class WhitePlayer extends Player{
    public WhitePlayer(final Board board,
                       final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves); /* in the player class it takes in legal moves
        (your moves) and then the opponents moves. for the white pieces your moves are the white moves and the oppenent
        is the black moves*/
    }
    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals, final Collection<Move> opponentsLegals) {
        final List<Move> kingCastles = new ArrayList<>();
        if(this.playerKing.isFirstMove() && !this.isInCheck()) { //if it is the kings first move, and the king is not in check
            //whites king side castle
            if (!this.board.getTile(61).isTileOccupied() &&
                !this.board.getTile(62).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(63);/*creates a Tile object on the 63rd square so that you
                can use the isTileOccupied method in the next line to make sure that the tile that a rook should be on in a king
                side castle is occupied, therefore it will be a legal move*/
                if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(61, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(62, opponentsLegals).isEmpty() &&
                       rookTile.getPiece().getPieceType().isRook()){
                        //TODO add a castle move
                        kingCastles.add(new Move.KingSideCastleMove(this.board,
                                                                    this.playerKing,
                                                   62,
                                                                    (Rook)rookTile.getPiece(),
                                                                    rookTile.getTileCoordinate(),
                                                   61));

                    }
                }
            }
            //whites queen side castle
            if(!this.board.getTile(59).isTileOccupied() &&
               !this.board.getTile(58).isTileOccupied() &&
               !this.board.getTile(57).isTileOccupied()){
                final Tile rookTile = this.board.getTile(56);
                if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(58, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(59, opponentsLegals).isEmpty() &&
                       rookTile.getPiece().getPieceType().isRook()){
                        //TODO add a castle move
                        kingCastles.add(new Move.QueenSideCastleMove(this.board,
                                                                    this.playerKing,
                                                   58,
                                                                    (Rook)rookTile.getPiece(),
                                                                    rookTile.getTileCoordinate(),
                                                   59));
                    }
                }
            }
        }
        return ImmutableList.copyOf(kingCastles);
    }
}
