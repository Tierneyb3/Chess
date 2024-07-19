package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.*;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;

public class Board {

    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;

    private final Pawn enPassantPawn;

    private Board(Builder builder) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(this.gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, Alliance.BLACK);
        this.enPassantPawn = builder.enPassantPawn;
        final Collection<Move> whiteStandardLegalMoves = calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackStandardLegalMoves = calculateLegalMoves(this.blackPieces);
        this.whitePlayer = new WhitePlayer (this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.blackPlayer = new BlackPlayer (this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.currentPlayer = builder.nextMoveMaker.choosePlayer(this.whitePlayer, this.blackPlayer);
    }

    public Player whitePlayer(){
        return this.whitePlayer;
    }

    public Player blackPlayer(){
        return this.blackPlayer;
    }

    public Player currentPlayer(){
        return this.currentPlayer;
    }

    @Override
    public String toString (){
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < BoardUtils.NUM_TILES; i++){
            final String tileText = this.gameBoard.get(i).toString();
            builder.append(String.format("%3s", tileText));
            if((i+1) % BoardUtils.NUM_TILES_PER_ROW == 0){
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public Pawn getEnPassantPawn (){
        return this.enPassantPawn;
    }

    public Collection<Piece> getBlackPieces(){
        return this.blackPieces;
    }

    public Collection<Piece> getWhitePieces() {
        return this.whitePieces;
    }
    private Collection<Move> calculateLegalMoves(Collection<Piece> pieces) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final Piece piece : pieces){
            legalMoves.addAll(piece.calculateLegalMoves(this));
            //this line adds all the elements to an arraylist of legal moves
            //the elements are all the possible legal moves a piece can make for every type of piece
        }
        return ImmutableList.copyOf(legalMoves);
    }

    public Tile getTile(int tileCoordinate){
        return gameBoard.get(tileCoordinate);//retrives a tile (most likely to be used for capturing pieces))
    }

    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard, Alliance alliance){
        //keeps track of the active pieces of each alliance
        final List<Piece> activePieces = new ArrayList<>();

        for(final Tile tile : gameBoard){
            if(tile.isTileOccupied()){ //if a tile is occupied in the current gameBoard
                final Piece piece = tile.getPiece();//get the piece
                if(piece.getPieceAlliance() == alliance){ //if the pieces alliance is equal to the alliance passed
                    //the instance variables above both take in different alliances depending on the variable
                    activePieces.add(piece); //if the piece is part of that alliance, add the arraylist
                }
            }
        }
        return ImmutableList.copyOf(activePieces); //return the immutable copy of the arrayList of active pieces
    }

    private static List<Tile> createGameBoard (final Builder builder){
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];
        for(int i =0; i < BoardUtils.NUM_TILES ; i++){
            tiles[i] = Tile.createTile(i, builder.boardConfig.get(i)); /*get the piece that is associated with tile i
            and create a tile from it*/
            /*for the create standard board method, it takes care of putting pieces on the correct tiles
            then the create game board method takes care of creating the tiles 0-63 and then the createTile()
            method will loop through and put the correct piece on the correct tile and if the tile is unoccupied
            the method will create an empty tile which can be retrieved through the EMPTY_TILE_CACHE*/
        }
        return ImmutableList.copyOf(tiles);
    }

    public static Board createStandardBoard (){
        final Builder builder = new Builder();
        //Black Pieces
        builder.setPiece(new Rook (Alliance.BLACK, 0)); //sets black rook to tile id 0
        builder.setPiece(new Knight(Alliance.BLACK, 1)); //sets black knight to tile id 1
        builder.setPiece(new Bishop(Alliance.BLACK, 2)); //sets black bishop to tile id 2
        builder.setPiece(new Queen(Alliance.BLACK, 3)); //sets black queen to tile id 3
        builder.setPiece(new King(Alliance.BLACK, 4)); //sets black king to tile id 4
        builder.setPiece(new Bishop(Alliance.BLACK, 5)); //sets black bishop to tile id 5
        builder.setPiece(new Knight(Alliance.BLACK, 6)); //sets black knight to tile id 6
        builder.setPiece(new Rook (Alliance.BLACK, 7)); //sets black rook to tile id 7
        builder.setPiece(new Pawn(Alliance.BLACK, 8)); //sets black pawn to tile id 8
        builder.setPiece(new Pawn(Alliance.BLACK, 9)); //sets black pawn to tile id 9
        builder.setPiece(new Pawn(Alliance.BLACK, 10)); //sets black pawn to tile id 10
        builder.setPiece(new Pawn(Alliance.BLACK, 11)); //sets black pawn to tile id 11
        builder.setPiece(new Pawn(Alliance.BLACK, 12)); //sets black pawn to tile id 12
        builder.setPiece(new Pawn(Alliance.BLACK, 13)); //sets black pawn to tile id 13
        builder.setPiece(new Pawn(Alliance.BLACK, 14)); //sets black pawn to tile id 14
        builder.setPiece(new Pawn(Alliance.BLACK, 15)); //sets black pawn to tile id 15
        //White Pieces
        builder.setPiece(new Pawn(Alliance.WHITE, 48)); //sets white pawn to tile id 48
        builder.setPiece(new Pawn(Alliance.WHITE, 49)); //sets white pawn to tile id 49
         builder.setPiece(new Pawn(Alliance.WHITE, 50)); //sets white pawn to tile id 50
        builder.setPiece(new Pawn(Alliance.WHITE, 51)); //sets white pawn to tile id 51
        builder.setPiece(new Pawn(Alliance.WHITE, 52)); //sets white pawn to tile id 52
        builder.setPiece(new Pawn(Alliance.WHITE, 53)); //sets white pawn to tile id 53
        builder.setPiece(new Pawn(Alliance.WHITE, 54)); //sets white pawn to tile id 54
        builder.setPiece(new Pawn(Alliance.WHITE, 55)); //sets white pawn to tile id 55
        builder.setPiece(new Rook(Alliance.WHITE, 56)); //sets white rook to tile id 56
        builder.setPiece(new Knight(Alliance.WHITE, 57)); //sets white knight to tile id 57
        builder.setPiece(new Bishop(Alliance.WHITE, 58)); //sets white bishop to tile id 58
        builder.setPiece(new Queen(Alliance.WHITE, 59)); //sets white queen to tile id 59
        builder.setPiece(new King(Alliance.WHITE, 60)); //sets white king to tile id 60
        builder.setPiece(new Bishop(Alliance.WHITE, 61)); //sets white bishop to tile id 61
        builder.setPiece(new Knight(Alliance.WHITE, 62)); //sets white knight to tile id 62
        builder.setPiece(new Rook(Alliance.WHITE, 63)); //sets white rook to tile id 63
        //white to move (The person who has the first turn in a game of chess)
        builder.setMoveMaker(Alliance.WHITE);

        return builder.build();
    }

    public Iterable<Move> getAllLegalMoves() {
        return this.currentPlayer.getLegalMoves();
    }

    public static class Builder {

        Map<Integer, Piece> boardConfig;
        Alliance nextMoveMaker;
        Pawn enPassantPawn;

        public Builder(){
            this.boardConfig = new HashMap<>();
        }

        public Builder setPiece (final Piece piece){/*sets the piece to the location immutablely so that it can only be
            moved when it is that players turn*/
            this.boardConfig.put(piece.getPiecePosition(), piece);// the position on the board of a piece is an integer
            return this;
        }

        public Builder setMoveMaker (final Alliance nextMoveMaker){
            this.nextMoveMaker = nextMoveMaker;
            return this;
        }
        public Board build (){ /*As we use build it will create an immutable board on the builder (something that
            cannot be manipulated) */
            return new Board (this);
        }

        public void setEnPassantPawn (Pawn enPassantPawn){
            this.enPassantPawn = enPassantPawn;
        }
    }
}
