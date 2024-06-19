package com.chess.engine.board;

import com.google.common.collect.ImmutableMap;

import java.util.*;

public class BoardUtils {//class for basic rules of chess to keep the moves consistent
    public static final boolean[] FIRST_COLUMN = initColumn(0);
    public static final boolean[] SECOND_COLUMN = initColumn(1);
    public static final boolean[] SEVENTH_COLUMN = initColumn(6);
    public static final boolean[] EIGHTH_COLUMN = initColumn(7);

    public static final boolean[] EIGHTH_RANK = initRow(0);
    public static final boolean[] SEVENTH_RANK = initRow(8);
    public static final boolean[] SIXTH_RANK = initRow(16);
    public static final boolean[] FIFTH_RANK = initRow(24);
    public static final boolean[] FOURTH_RANK = initRow(32);
    public static final boolean[] THIRD_RANK = initRow(40);
    public static final boolean[] SECOND_RANK = initRow(48);
    public static final boolean[] FIRST_RANK = initRow(56);

    public static final String[] ALGEBRAIC_NOTATION = initializeAlgebraicNotation();
    public static final Map<String, Integer> POSITION_TO_COORDINATE = initializePositionCoordinateMap();

    public static final int NUM_TILES = 64;
    public static final int NUM_TILES_PER_ROW = 8;

    private static boolean[] initRow(int rowNumber) {
        final boolean[] row = new boolean[NUM_TILES];//creates a boolean array the size of the board
        do{
            row[rowNumber] = true;//as long as the number we give the method is contained within the squares of the board
            rowNumber++;//counts all the values in the row...
        } while(rowNumber % NUM_TILES_PER_ROW != 0); // ... until this condition is met
        /*for example, if the number you give is 8 (the second row), it would set 8 equal to true, and then 8-15, and then
        it would go to 16 and see that if divisible by NUM_TILES_PER_ROW, the remainder is 0
         */
        return row;
    }

    private static boolean[] initColumn(int columnNumber) {
        final boolean[] column = new boolean[NUM_TILES]; //first its going to declare an array of boolean of size 64
        do{
            column[columnNumber] = true;/*uses the math we have been doing to calculate all of the squares in a column
            first square is the 1st square the next square in the column will be the 9th, then the 17th and so on*/
            columnNumber+=NUM_TILES_PER_ROW; //adds enough squares to get back to the column
        } while (columnNumber < NUM_TILES);//while the column number is less than 64 (the amount of squares on a chess board)
        return column;
    }

    private BoardUtils (){
        throw new RuntimeException("You cannot Instantiate!");
    }

    private static String[] initializeAlgebraicNotation() {
        return new String[]{
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"
        };
    }

    private static Map<String, Integer> initializePositionCoordinateMap() {
        final Map<String, Integer> positionToCoordinate = new HashMap<>();

        for(int i = 0; i < NUM_TILES; i++){
            positionToCoordinate.put(ALGEBRAIC_NOTATION[i], i);
            //ALGEBREIC_NOTATION is a string array and at instance 0 the value is a8 A hashMap uses key and value mapping so the key is
            // a8 and the value is 0
            //Now we have a PositionCoordinateMap that takes in a position and spits out a tileID
        }
        return ImmutableMap.copyOf(positionToCoordinate);
    }

    public static boolean isValidTileCoordinate(final int tileCoordinate) {
        return tileCoordinate >= 0 && tileCoordinate < NUM_TILES;
    }

    public static int getCoordinateAtPosition (final String position){
        return POSITION_TO_COORDINATE.get(position);
    }

    public static String getPositionAtCoordinate(final int coordinate) {
        return ALGEBRAIC_NOTATION[coordinate];
    }
}
