package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.MajorMove;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.*;

import static com.chess.engine.board.Move.*;


public class Pawn extends Piece{

    private final int[] CANDIDATE_MOVE_COORDINATE = {8, 16, 7, 9};

    public Pawn(final Alliance pieceAlliance,
                final int piecePosition) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, true);
    }

    public Pawn(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {

        final List<Move> legalMoves = new ArrayList<>();

        for(final int currentCandidateOffset: CANDIDATE_MOVE_COORDINATE){
            final int candidateDestinationCoordinate = this.piecePosition +
                    (this.pieceAlliance.getDirection() * currentCandidateOffset); /*this line uses the piece
                    alliance and direction functions so that the pawns go in the proper direction on the board*/
            if(!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                continue;
            }
            //major move method takes in a board, a piece, and a destination coordinate
            if(currentCandidateOffset == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                //if the offset of the pawn is 8 meaning it is moving up 1 square and there is no piece on that tile...
                if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)){
                    legalMoves.add(new PawnPromotion (new PawnMove(board,this,candidateDestinationCoordinate)));
                } else {
                    legalMoves.add(new PawnMove(board,this,candidateDestinationCoordinate));
                }

                //add this move to the list of legal moves
            } else if (currentCandidateOffset == 16 && this.isFirstMove() &&
                    ((BoardUtils.SEVENTH_RANK[this.piecePosition] && this.getPieceAlliance().isBlack()) ||
                    (BoardUtils.SECOND_RANK[this.piecePosition] && this.getPieceAlliance().isWhite()))){
                final int behindCandidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection()*8);
                //this line calculates the square behind the destination sqaure and then checks...
                if(!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                        /*this line makes sure that the 3rd or 6th row is not being occupied making it so that a double pawn
                        move would not be possible because you would be jumping over a piece*/
                   !board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    //also checks if the destination square is being occupied
                    legalMoves. add(new PawnJump(board,this,candidateDestinationCoordinate));
                }
            } else if (currentCandidateOffset == 7 &&
                    //this means the piece is attacking in the right direction if the board is flipped to your side
                      !((BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite()) ||
                      (BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack()))){
                if(board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    if(this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)){
                            legalMoves.add(new PawnPromotion (new PawnAttackingMove(board,this,
                                           candidateDestinationCoordinate, pieceOnCandidate)));
                        } else {
                            legalMoves.add(new PawnAttackingMove(board, this,
                                           candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                } else if(board.getEnPassantPawn() != null) {
                    if(board.getEnPassantPawn().getPiecePosition() ==
                      (this.piecePosition + (this.pieceAlliance.getOppositeDirection()))){
                        /*for black, if the piece is to the right of you, it is one less than the square you are on and therefore
                        it is the 7 offset which adds the get opposite direction which is -1, therefore the offset is -1
                        for white, if the piece is to the right, it is one more than the square you are on and therefore
                        it is the 7 offset which adds the get opposite direction which is 1, therefore, the offfset is 1
                        all of these calculations are in terms of if you have the pieces mentioned on the bottom of the board*/
                        final Piece pieceOnCandidate = board.getEnPassantPawn();
                        if(this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                            if(this.pieceAlliance != pieceOnCandidate.getPieceAlliance()){
                                legalMoves.add(new PawnEnPassantAttackMove(board,this, candidateDestinationCoordinate, pieceOnCandidate));
                            }
                        }
                    }
                }
            } else if (currentCandidateOffset == 9 &&
                    //this means the piece is attacking in the left direction if the board is flipped to your side
                      !((BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack()) ||
                      (BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite()))){
                if(board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    //these are the moves that do not work because they are illegal moves
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)){
                            legalMoves.add(new PawnPromotion (new PawnAttackingMove(board,
                                    this,candidateDestinationCoordinate, pieceOnCandidate)));
                        } else {
                            legalMoves.add(new PawnAttackingMove(board, this,
                                    candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                } else if(board.getEnPassantPawn() != null) {
                    if(board.getEnPassantPawn().getPiecePosition() ==
                            (this.piecePosition - (this.pieceAlliance.getOppositeDirection()))){
                        /*for white, if the piece is to the left of you, it is one less than the square you are on and therefore
                        it is the 9 offset which subtracts the get opposite direction which is 1 therefore the offset is -1
                        for black, if the piece is to the left of you, it is one more than the square you are on and therefore
                        is it the 9 offset which subtracts the get opposite direction which is -1 therefore the offset is 1
                        all of these calculations are in terms of if you have the pieces mentioned on the bottom of the board*/
                        final Piece pieceOnCandidate = board.getEnPassantPawn();
                        if(this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                            if(this.pieceAlliance != pieceOnCandidate.getPieceAlliance()){
                                legalMoves.add(new PawnEnPassantAttackMove(board,
                                        this, candidateDestinationCoordinate, pieceOnCandidate));
                            }
                        }
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }
    @Override
    public Pawn movePiece(Move move) {
        return new Pawn(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
    }

    //currentCandidateOffset = the amount of squares the piece can move legally
    //piecePosition = the location of the piece without any offset applied
    //candidateDestinationCoordinate = the coordinate that the piece will end up at after the offset is applied

    @Override
    public String toString(){
        return PieceType.PAWN.toString();
    }

    public Piece getPromotionPiece(){
        return new Queen (this.pieceAlliance, this.piecePosition, false);
    }
}

