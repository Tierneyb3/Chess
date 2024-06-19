package com.chess.engine.board;

import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public abstract class Tile{

    protected final int tileCoordinate; //we want the coordinate to be set only once, at construction time

    private static final Map<Integer, emptyTile> EMPTY_TILES_CACHE = createAllPossibleEmptyTiles();
    //map to find all the possible empty tiles on the board

    private static Map<Integer, emptyTile> createAllPossibleEmptyTiles() {
        final Map<Integer, emptyTile> emptyTileMap = new HashMap<>();

            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                emptyTileMap.put(i, new emptyTile(i));
            }
            return ImmutableMap.copyOf(emptyTileMap);/*needs to be immutable so that someone doesn't clear the hashmap
        and add their own tiles*/
        }

    public static Tile createTile (int tileCoordinate, Piece piece){
        return piece != null ? new occupiedTile(tileCoordinate, piece) : EMPTY_TILES_CACHE.get(tileCoordinate);
        /*if someone wants to create a new tile, they must use this method and create an empty tile on a given coordinate,
        or they must use an occupied tile with the given coordinate and the piece that goes there*/
    }

    private Tile(int tileCoordinate){
        this.tileCoordinate = tileCoordinate;//basic constructor
    }
    public abstract boolean isTileOccupied();//abstract functions being used in multiple other classes

    public abstract Piece getPiece();

    public int getTileCoordinate() {
        return this.tileCoordinate;
    }

    public static final class emptyTile extends Tile{

        @Override
        public String toString(){
            return "-"; //if tile is not occupied print out a hyphen to demonstrate that the tile is empty
        }

        emptyTile(final int tileCoordinate) {
            super(tileCoordinate);
        }

        @Override
        public boolean isTileOccupied(){//empty tile means the tile is not occupied
            return false;
        }

        @Override
        public Piece getPiece(){
            return null;
        }

    }

    public static final class occupiedTile extends Tile{
        private final Piece pieceOnTile;//no way to reference this piece without using the get method

        private occupiedTile(int tileCoordinate, final Piece pieceOnTile){
            super(tileCoordinate);/*super is used when you're calling an instance variable
            from a class you are extending from*/
            this.pieceOnTile = pieceOnTile;
        }

        @Override
        public String toString(){
            return getPiece().getPieceAlliance().isBlack() ? getPiece().toString().toLowerCase() :
                    getPiece().toString(); //if the tile is occupied print out the piece whether it be black or white
        }

        @Override
        public boolean isTileOccupied(){
            return true;
        }

        public Piece getPiece(){
            return this.pieceOnTile;
        }
    }
}