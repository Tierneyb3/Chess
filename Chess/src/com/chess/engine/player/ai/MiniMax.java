package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.gui.Table;
public final class MiniMax implements MoveStrategy {

    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;

    public MiniMax(final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
    }

    @Override
    public Move execute(final Board board) {

        if (this.searchDepth <= 1) {
            return executeOnePlyGreedy(board);   // EASY
        } else if (this.searchDepth == 2) {
            return executeTwoPly(board, 150);     // MEDIUM
        } else {
            return executeTwoPly(board, 50);      // HARD
        }
    }
    private Move executeOnePlyGreedy(final Board board) {

        final boolean isWhiteToMove = board.currentPlayer().getAlliance().isWhite();

        final int currentEval = this.boardEvaluator.evaluate(board, 0);
        final int currentScoreForMover = isWhiteToMove ? currentEval : -currentEval;

        Move bestNonDrawMove = null;
        int bestNonDrawScore = Integer.MIN_VALUE;

        Move bestDrawMove = null;
        int bestDrawScore = Integer.MIN_VALUE;

        Move anyLegalMove = null;

        for (final Move move : board.currentPlayer().getLegalMoves()) {

            if (anyLegalMove == null) {
                anyLegalMove = move;
            }

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (!moveTransition.getMoveStatus().isDone()) {
                continue;
            }

            final Board transitionBoard = moveTransition.getTransitionBoard();

            final int eval = this.boardEvaluator.evaluate(transitionBoard, 0);
            int scoreForMover = isWhiteToMove ? eval : -eval;

            // checkmate of opponent
            if (transitionBoard.currentPlayer().isInCheckMate()) {
                scoreForMover += 100_000;
                if (scoreForMover > bestNonDrawScore) {
                    bestNonDrawScore = scoreForMover;
                    bestNonDrawMove = move;
                }
                continue;
            }

            // threefold repetition?
            if (Table.get().wouldBeThreefold(transitionBoard)) {
                if (currentScoreForMover > 0) {
                    // if we're winning, don't repeat
                    continue;
                }
                if (scoreForMover > bestDrawScore) {
                    bestDrawScore = scoreForMover;
                    bestDrawMove = move;
                }
                continue;
            }

            // stalemate?
            if (transitionBoard.currentPlayer().isInStaleMate()) {
                if (scoreForMover > bestDrawScore) {
                    bestDrawScore = scoreForMover;
                    bestDrawMove = move;
                }
                continue;
            }

            // normal move
            if (scoreForMover > bestNonDrawScore) {
                bestNonDrawScore = scoreForMover;
                bestNonDrawMove = move;
            }
        }

        if (bestNonDrawMove != null) return bestNonDrawMove;
        if (bestDrawMove != null) return bestDrawMove;
        return anyLegalMove;
    }


    private Move executeTwoPly(final Board board, final int margin) {

        final boolean isWhiteToMove = board.currentPlayer().getAlliance().isWhite();

        final int currentEval = this.boardEvaluator.evaluate(board, 0);
        final int currentScoreForMover = isWhiteToMove ? currentEval : -currentEval;

        Move bestNonDrawMove = null;
        int bestNonDrawScore = Integer.MIN_VALUE;

        Move bestDrawMove = null;
        int bestDrawScore = Integer.MIN_VALUE;

        Move anyLegalMove = null;

        final java.util.Map<Move, Integer> nonDrawScores = new java.util.HashMap<>();

        for (final Move move : board.currentPlayer().getLegalMoves()) {

            if (anyLegalMove == null) {
                anyLegalMove = move;
            }

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (!moveTransition.getMoveStatus().isDone()) {
                continue;
            }

            final Board transitionBoard = moveTransition.getTransitionBoard();

            final int baseEval = this.boardEvaluator.evaluate(transitionBoard, 0);
            final int scoreForMover = isWhiteToMove ? baseEval : -baseEval;

            // 1) mate
            if (transitionBoard.currentPlayer().isInCheckMate()) {
                int mateScore = scoreForMover + 100_000;
                nonDrawScores.put(move, mateScore);
                if (mateScore > bestNonDrawScore) {
                    bestNonDrawScore = mateScore;
                    bestNonDrawMove = move;
                }
                continue;
            }

            // 2) threefold
            if (Table.get().wouldBeThreefold(transitionBoard)) {
                if (currentScoreForMover > 0) {
                    continue; // refuse repetition when better
                }
                if (scoreForMover > bestDrawScore) {
                    bestDrawScore = scoreForMover;
                    bestDrawMove = move;
                }
                continue;
            }

            // 3) stalemate
            if (transitionBoard.currentPlayer().isInStaleMate()) {
                if (scoreForMover > bestDrawScore) {
                    bestDrawScore = scoreForMover;
                    bestDrawMove = move;
                }
                continue;
            }

            // 4) simulate opponent reply (2-ply)
            int worstReplyScore = Integer.MAX_VALUE;

            for (final Move oppMove : transitionBoard.currentPlayer().getLegalMoves()) {

                final MoveTransition oppTransition =
                        transitionBoard.currentPlayer().makeMove(oppMove);

                if (!oppTransition.getMoveStatus().isDone()) {
                    continue;
                }

                final Board replyBoard = oppTransition.getTransitionBoard();

                final int replyEval = this.boardEvaluator.evaluate(replyBoard, 0);
                final int scoreForMoverIfThisReply =
                        isWhiteToMove ? replyEval : -replyEval;

                if (scoreForMoverIfThisReply < worstReplyScore) {
                    worstReplyScore = scoreForMoverIfThisReply;
                }
            }

            if (worstReplyScore == Integer.MAX_VALUE) {
                worstReplyScore = scoreForMover;
            }

            nonDrawScores.put(move, worstReplyScore);

            if (worstReplyScore > bestNonDrawScore) {
                bestNonDrawScore = worstReplyScore;
                bestNonDrawMove = move;
            }
        }

        if (bestNonDrawMove != null && !nonDrawScores.isEmpty()) {

            java.util.List<Move> candidates = new java.util.ArrayList<>();
            for (java.util.Map.Entry<Move, Integer> entry : nonDrawScores.entrySet()) {
                if (entry.getValue() >= bestNonDrawScore - margin) {
                    candidates.add(entry.getKey());
                }
            }

            if (!candidates.isEmpty()) {
                java.util.Random rng = new java.util.Random();
                return candidates.get(rng.nextInt(candidates.size()));
            }

            return bestNonDrawMove;
        }

        if (bestDrawMove != null) {
            return bestDrawMove;
        }

        return anyLegalMove;
    }
}
