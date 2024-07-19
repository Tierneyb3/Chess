package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

public class MiniMax implements MoveStrategy{

    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;

    public MiniMax(final int searchDepth){
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
    }

    @Override
    public String toString() {
        return "MiniMax";
    }

    @Override
    public Move execute(Board board) {

        final long startTime = System.currentTimeMillis();

        Move bestMove = null;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;
        System.out.println(board.currentPlayer() + " THINKING with Depth " + this.searchDepth);

        int numMoves = board.currentPlayer().getLegalMoves().size();
        for(final Move move : board.currentPlayer().getLegalMoves()){
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            //we want to use the board that we have transitioned to as the starting point for our min or max function
            if(moveTransition.getMoveStatus().isDone()){
                currentValue = board.currentPlayer().getAlliance().isWhite() ?
                        //if the current player is white, go into the minimizing function next due to that being the function
                        //we call for black, otherwise, if its not the white player next call the maximizing function
                        min(moveTransition.getTransitionBoard(), this.searchDepth -1):
                        max(moveTransition.getTransitionBoard(), this.searchDepth -1);

                if(board.currentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue){
                    highestSeenValue = currentValue;
                    bestMove = move;
                } else if(board.currentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue){
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;

        return bestMove;
    }

    public int min(final Board board,
                   final int depth) {
        if(depth == 0 /* || gameOver*/){
            return this.boardEvaluator.evaluate(board, depth);
        }
        int lowestSeenValue = Integer.MAX_VALUE;
        /*sets this value high at first so we never would have to worry about this number accidently not over writing*/
        for(final Move move : board.currentPlayer().getLegalMoves()){
            //finds the legal moves and then actually makes all those moves
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                //then checks the value of our board evaluator and sees what number its at,
                // and then loops through to find the lowest number at the interation
                final int currentValue = max(moveTransition.getTransitionBoard(), depth -1);
                if(currentValue <= lowestSeenValue){
                    lowestSeenValue = currentValue;//and then records that number here
                }
            }
        }
        return lowestSeenValue;
    }

    public static boolean isEndGameScenario (Board board){
        return board.currentPlayer().isInCheckMate() ||
               board.currentPlayer().isInStaleMate();
    }

    public int max(final Board board,
                   final int depth){
        if(depth == 0 || isEndGameScenario(board)){
            return this.boardEvaluator.evaluate(board, depth);
        }
        int highestSeenValue = Integer.MIN_VALUE;
        /*sets this value low at first so we never would have to worry about this number accidently not over writing*/
        for(final Move move : board.currentPlayer().getLegalMoves()){
            //finds the legal moves and then actually makes all of those moves
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                //then checks the value of our board evaluator and sees what number its at,
                // and then loops through to find the highest number at the interation
                final int currentValue = min(moveTransition.getTransitionBoard(), depth -1);
                if(currentValue >= highestSeenValue){
                    highestSeenValue = currentValue;//and the records that number here
                }
            }
        }
        return highestSeenValue;
    }
}
