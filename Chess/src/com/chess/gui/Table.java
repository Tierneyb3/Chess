package com.chess.gui;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.MoveStrategy;
import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class Table extends Observable {

    private final JFrame gameFrame;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    private static Board chessBoard;
    private final EvaluationPanel evaluationPanel;
    private int historyCursor = -1;
    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;
    private Move computerMove;
    private boolean gameOver = false;
    private static final RepetitionTracker repetitionTracker = new RepetitionTracker();
    private boolean highlightLegalMoves;
    private static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private static Dimension BOARD_PANEL_DIMENSION = new Dimension(400,350);
    private static Dimension TILE_PANEL_DIMENSION = new Dimension(10,10);
    private static String defaultPieceImagesPath = "art/pieces/";
    private Color lightTileColor = Color.decode("#FFFACD");
    private Color darkTileColor = Color.decode("#593E1A");

    private static final Table INSTANCE = new Table();

    public Table(){
        this.gameFrame = new JFrame("JChess");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessBoard = Board.createStandardBoard();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.boardPanel = new BoardPanel();
        this.evaluationPanel = new EvaluationPanel();
        this.moveLog = new MoveLog();
        this.addObserver(new TableGameAiWatcher());
        this.gameSetup = new GameSetup(this.gameFrame, true);
        this.boardDirection = BoardDirection.NORMAL;
        this.highlightLegalMoves = false;
        // Panel: board + vertical eval bar
        final JPanel boardWithEvalPanel = new JPanel(new BorderLayout());
        boardWithEvalPanel.add(this.boardPanel, BorderLayout.CENTER);
        boardWithEvalPanel.add(this.evaluationPanel, BorderLayout.EAST);
        // Panel: (board+eval) on top, taken pieces underneath
        final JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(boardWithEvalPanel, BorderLayout.CENTER);
        centerPanel.add(this.takenPiecesPanel, BorderLayout.SOUTH);
        // Add to frame
        this.gameFrame.add(centerPanel, BorderLayout.CENTER);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.gameFrame.setVisible(true);
        final JRootPane root = this.gameFrame.getRootPane();
        // LEFT arrow = go back one move
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "historyBack");
        root.getActionMap().put("historyBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepHistory(-1);
            }
        });
        // RIGHT arrow = go forward one move (toward live)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "historyForward");
        root.getActionMap().put("historyForward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepHistory(1);
            }
        });
    }

    public static Table get(){
        return INSTANCE;
    }
    // =============================================================
    // UI bootstrap
    // =============================================================


    public void show(){
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().printMoveHistoryText(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
        this.evaluationPanel.reset();
        this.evaluationPanel.updateEvaluation(chessBoard);
    }

    public static boolean isThreefoldRepetition() {
        return repetitionTracker.isThreefold(chessBoard);
    }

    public boolean wouldBeThreefold(final Board nextBoard) {
        return repetitionTracker.wouldBeThreefold(nextBoard);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(final boolean gameOver) {
        this.gameOver = gameOver;
    }

    private GameSetup getGameSetup(){
        return this.gameSetup;
    }

    private Board getGameBoard(){
        return this.chessBoard;
    }
    // =============================================================
    // Menu bar
    // =============================================================


    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu(){
        final JMenu fileMenu = new JMenu("File");
        final JMenuItem loadPgnItem = new JMenuItem("Load PGN File");
        loadPgnItem.addActionListener(e -> Table.get().showLoadPGNDialog());
        fileMenu.add(loadPgnItem);
        fileMenu.addSeparator();

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createPreferencesMenu (){
        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);
        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("HighLight Legal Moves", false);
        legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected();
            }
        });
        preferencesMenu.add(legalMoveHighlighterCheckbox);

        return preferencesMenu;
    }

    private JMenu createOptionsMenu(){
        final JMenu optionsMenu = new JMenu("Options");
        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });
        optionsMenu.add(setupGameMenuItem);
        optionsMenu.addSeparator();
        final JMenuItem setupNewGameMenuItem = new JMenuItem("New Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });
        optionsMenu.add(setupNewGameMenuItem);
        return optionsMenu;
    }
    public void showLoadPGNDialog() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open PGN File");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PGN files", "pgn"));

        final int result = chooser.showOpenDialog(this.gameFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            final java.io.File file = chooser.getSelectedFile();
            try {
                final String pgnText = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                loadGameFromPGN(pgnText);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this.gameFrame,
                        "Failed to load PGN: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    private void loadGameFromPGN(final String pgnText) {
        // Reset game state
        this.gameOver = false;
        this.moveLog.clear();
        // Start from initial board
        Board board = Board.createStandardBoard();
        // Reset repetition tracker and record starting position
        this.repetitionTracker.reset();
        this.repetitionTracker.recordPosition(board);
        // Extract PGN moves
        final java.util.List<String> moveTokens = extractMoveTokens(pgnText);
        int index = 0;
        for (final String san : moveTokens) {
            System.out.println("Applying PGN move " + index + ": " + san);
            final Move move = createMoveFromNotation(board, san);
            if (move == null) {
                // 👇 Instead of stopping, just skip this move
                System.err.println("Skipping PGN move (no match): " + san +
                        " at index " + index);
                index++;
                continue;
            }
            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                // 👇 Same idea: log and skip
                System.err.println("Skipping illegal PGN move according to engine: "
                        + san + " at index " + index);
                index++;
                continue;
            }
            board = transition.getTransitionBoard();
            this.moveLog.addMove(move);
            this.repetitionTracker.recordPosition(board);
            index++;
        }
        // Commit final board as current game position
        this.chessBoard = board;
        // Update UI
        this.gameHistoryPanel.printMoveHistoryText(this.chessBoard, this.moveLog);
        this.takenPiecesPanel.redo(this.moveLog);
        this.boardPanel.drawBoard(this.chessBoard);
        this.evaluationPanel.reset();
        this.evaluationPanel.updateEvaluation(chessBoard);
    }
    //Need to import full San Library and matching parser
    /*private void loadGameFromPGN(final String pgnText) {

        // Reset game state
        this.gameOver = false;
        this.moveLog.clear();

        // Start from initial board
        Board board = Board.createStandardBoard();

        // Reset repetition tracker and record starting position
        this.repetitionTracker.reset();
        this.repetitionTracker.recordPosition(board);

        // Extract PGN moves
        final java.util.List<String> moveTokens = extractMoveTokens(pgnText);

        int index = 0;
        for (final String san : moveTokens) {

            System.out.println("Applying PGN move " + index + ": " + san);

            final Move move = createMoveFromNotation(board, san);
            if (move == null) {
                System.err.println("Failed to create move for SAN: " + san + " at index " + index);
                break;
            }

            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                System.err.println("Illegal PGN move according to engine: " + san + " at index " + index);
                break;
            }

            board = transition.getTransitionBoard();
            this.moveLog.addMove(move);
            this.repetitionTracker.recordPosition(board);

            index++;
        }


        // Update UI
        this.gameHistoryPanel.printMoveHistoryText(this.chessBoard, this.moveLog);
        this.takenPiecesPanel.redo(this.moveLog);
        this.boardPanel.drawBoard(this.chessBoard);
    }*/
    private java.util.List<String> extractMoveTokens(final String pgnText) {
        final java.util.List<String> tokens = new java.util.ArrayList<>();

        // Remove header tag pairs (lines starting with '[')
        final StringBuilder movetext = new StringBuilder();
        final String[] lines = pgnText.split("\\R");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("[")) continue; // skip tags
            movetext.append(line).append(" ");
        }

        // Split by whitespace
        for (String tok : movetext.toString().trim().split("\\s+")) {
            tok = tok.trim();
            if (tok.isEmpty()) continue;

            // Skip move numbers and results
            if (tok.matches("^[0-9]+\\.$")) continue;              // "1."
            if (tok.equals("1-0") || tok.equals("0-1")
                    || tok.equals("1/2-1/2") || tok.equals("*")) {
                continue;
            }

            tokens.add(tok);
        }

        return tokens;
    }

    private Move createMoveFromNotation(final Board board, final String notation) {

        // 1) Clean up notation: remove check/mate markers
        final String raw = notation.trim();
        final String clean = raw.replace("+", "").replace("#", "");

        // 2) First try: direct SAN match vs move.toString()
        Move sanMatch = matchByToString(board, clean);
        if (sanMatch != null) {
            return sanMatch;
        }

        // 3) Castling special cases (if toString() didn't match)
        if (clean.equals("O-O") || clean.equals("O-O-O")) {
            return findCastleMove(board, clean.equals("O-O")); // true = king-side
        }

        // 4) Parse SAN into piece type + dest square + disambiguation

        final Piece.PieceType pieceType;
        String remainder = clean;

        char first = remainder.charAt(0);
        if ("KQRBN".indexOf(first) >= 0) {
            pieceType = mapPieceLetter(first);
            remainder = remainder.substring(1); // strip 'B' from "Bxf4"
        } else {
            pieceType = Piece.PieceType.PAWN;
        }

        // strip capture marker
        remainder = remainder.replace("x", "");

        // handle promotion like e8=Q
        int promoIdx = remainder.indexOf('=');
        if (promoIdx >= 0) {
            // String promotionPart = remainder.substring(promoIdx + 1); // e.g. "Q"
            remainder = remainder.substring(0, promoIdx); // just the square/disambig part
        }

        if (remainder.length() < 2) {
            System.err.println("Cannot parse SAN: " + notation);
            return null;
        }

        String targetSquare = remainder.substring(remainder.length() - 2);   // e.g. "d4"
        String disambiguation = remainder.substring(0, remainder.length() - 2); // e.g. "b" in Nbd2

        // 5) Build candidates by comparing *algebraic* dest squares, not coordinates

        java.util.List<Move> candidates = new java.util.ArrayList<>();

        for (final Move move : board.currentPlayer().getLegalMoves()) {

            if (move.getMovedPiece().getPieceType() != pieceType) {
                continue;
            }

            // Convert this move's destination coordinate to algebraic (e.g. "d4")
            String moveDestSquare = coordinateToAlgebraic(move.getDestinationCoordinate());
            if (!moveDestSquare.equals(targetSquare)) {
                continue;
            }

            if (!disambiguationMatches(move, disambiguation)) {
                continue;
            }

            candidates.add(move);
        }

        if (candidates.size() == 1) {
            return candidates.get(0);
        } else if (candidates.isEmpty()) {
            System.err.println("No legal move matches SAN: " + notation);

            // Debug: dump all legal moves and their destinations
            System.err.println("Current player: " + board.currentPlayer().getAlliance());
            System.err.println("Legal moves at this point:");

            for (final Move m : board.currentPlayer().getLegalMoves()) {
                String san = m.toString();
                String destSq = coordinateToAlgebraic(m.getDestinationCoordinate());
                System.err.println("  " + san + "  -> " + destSq +
                        " (piece=" + m.getMovedPiece().getPieceType() + ")");
            }

            return null;
        }
        else {
            System.err.println("Ambiguous SAN for: " + notation + " candidates=" + candidates.size());
            return candidates.get(0); // or handle properly if you want
        }
    }

    private boolean disambiguationMatches(final Move move, final String disambiguation) {
        return true;
    }
        /*if (disambiguation == null || disambiguation.isEmpty()) {
            return true; // nothing to check
        }

        final int from = move.getCurrentCoordinate();
        final String fromSquare = coordinateToAlgebraic(from);
        final char fromFile = fromSquare.charAt(0); // 'a'..'h'
        final char fromRank = fromSquare.charAt(1); // '1'..'8'

        if (disambiguation.length() == 1) {
            char d = disambiguation.charAt(0);
            if (d >= 'a' && d <= 'h') {
                return fromFile == d;
            } else if (d >= '1' && d <= '8') {
                return fromRank == d;
            }
        } else if (disambiguation.length() == 2) {
            // both file and rank, e.g. "e4"
            return disambiguation.equals(fromSquare);
        }

        // If disambiguation is something unexpected, ignore it for now.
        return true;
    }*/

    private String coordinateToAlgebraic(final int coordinate) {
        int rankFromTop = coordinate / 8;          // 0..7
        int file = coordinate % 8;                 // 0..7

        char fileChar = (char) ('a' + file);
        char rankChar = (char) ('1' + (7 - rankFromTop));

        return "" + fileChar + rankChar;
    }

    private Piece.PieceType mapPieceLetter(final char c) {
        switch (c) {
            case 'K': return Piece.PieceType.KING;
            case 'Q': return Piece.PieceType.QUEEN;
            case 'R': return Piece.PieceType.ROOK;
            case 'B': return Piece.PieceType.BISHOP;
            case 'N': return Piece.PieceType.KNIGHT;
            default:  return Piece.PieceType.PAWN;
        }
    }


    private Move matchByToString(final Board board, final String cleanSAN) {

        // Normalize PGN SAN by ignoring captures
        final String normalizedSAN = cleanSAN.replace("x", "");

        for (final Move move : board.currentPlayer().getLegalMoves()) {
            String moveText = move.toString();
            if (moveText == null) continue;

            moveText = moveText.replace("+", "")
                    .replace("#", "")
                    .replace("x", "")
                    .trim();

            if (moveText.equals(normalizedSAN)) {
                return move;
            }
        }
        return null;
    }

    private Move findCastleMove(final Board board, final boolean kingSide) {
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            if (move.getMovedPiece().getPieceType() != Piece.PieceType.KING) {
                continue;
            }

            int dest = move.getDestinationCoordinate();
            String destSq = coordinateToAlgebraic(dest);

            if (board.currentPlayer().getAlliance().isWhite()) {
                if (kingSide && destSq.equals("g1")) return move;
                if (!kingSide && destSq.equals("c1")) return move;
            } else {
                if (kingSide && destSq.equals("g8")) return move;
                if (!kingSide && destSq.equals("c8")) return move;
            }
        }
        return null;
    }
    // =============================================================
    // Move history navigation (click / arrows)
    // =============================================================



    public void showPositionAfterPlies(final int plies) {
        // Start from initial board
        Board board = Board.createStandardBoard();

        final int max = Math.min(plies, this.moveLog.size());
        for (int i = 0; i < max; i++) {
            final Move move = this.moveLog.getMoves().get(i);
            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                board = transition.getTransitionBoard();
            } else {
                break;
            }
        }
        // Draw this historical position, but do NOT change chessBoard
        this.boardPanel.drawBoard(board);
    }

    void showPositionAtPly(final int plyIndex) {
        // safety
        if (plyIndex < 0 || plyIndex >= this.moveLog.size()) {
            return;
        }

        // Start from the initial position and replay moves up to plyIndex
        Board board = Board.createStandardBoard();
        for (int i = 0; i <= plyIndex; i++) {
            final Move move = this.moveLog.getMoves().get(i);
            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                break; // shouldn't happen, but be safe
            }
            board = transition.getTransitionBoard();
        }

        // Draw that board
        this.boardPanel.drawBoard(board);

        // Optional: update taken pieces & highlight that move in the history panel
        this.takenPiecesPanel.redo(this.moveLog);
        this.gameHistoryPanel.highlightMoveRow(plyIndex); // we'll add this next
    }

    private void showLivePosition() {
        this.boardPanel.drawBoard(this.chessBoard);
        this.takenPiecesPanel.redo(this.moveLog);
        this.gameHistoryPanel.printMoveHistoryText(this.chessBoard, this.moveLog);
    }

    public void stepHistory(final int delta) {
        final int moveCount = this.moveLog.size();
        if (moveCount == 0) {
            return;
        }

        if (this.historyCursor == -1) {
            // Currently at live board
            if (delta < 0) {
                // Go to last move
                this.historyCursor = moveCount - 1;
                showPositionAtPly(this.historyCursor);
            } else {
                // Can't go "forward" from live position
                return;
            }
        } else {
            // Already in history mode
            this.historyCursor += delta;

            if (this.historyCursor < 0) {
                this.historyCursor = 0;
            } else if (this.historyCursor >= moveCount) {
                // Past the end → go back to live board
                this.historyCursor = -1;
                showLivePosition();
                return;
            }

            showPositionAtPly(this.historyCursor);
        }
    }
    public void showFinalEvaluation() {
        // Evaluate the final position and show the bar
        this.evaluationPanel.updateEvaluation(this.chessBoard);
        this.evaluationPanel.setVisible(true);
        this.gameFrame.revalidate();
        this.gameFrame.repaint();
    }

    public void hideEvaluation() {
        this.evaluationPanel.setVisible(false);
    }

    public void newGame() {
        // Reset game-over flag
        this.gameOver = false;
        // Create a fresh starting board
        this.chessBoard = Board.createStandardBoard();
        // Reset repetition tracker and record the initial position
        this.repetitionTracker.reset();
        this.repetitionTracker.recordPosition(this.chessBoard);
        hideEvaluation();
        // Clear move log
        this.moveLog.clear();
        // Refresh side panels (history & captured pieces)
        this.gameHistoryPanel.printMoveHistoryText(this.chessBoard, this.moveLog);
        this.takenPiecesPanel.redo(this.moveLog);
        // Redraw the board
        this.boardPanel.drawBoard(this.chessBoard);
        this.evaluationPanel.reset();
        this.evaluationPanel.updateEvaluation(chessBoard);
    }


    private void setupUpdate(final GameSetup gameSetup){
        setChanged();
        notifyObservers(gameSetup);
    }

    public Board buildBoardAtMoveIndex(final int moveIndex) {
        // Start from the initial position
        Board board = Board.createStandardBoard();

        // Defensive clamp, in case of bad index
        int maxMoves = this.moveLog.size();
        int end = Math.max(0, Math.min(moveIndex, maxMoves));

        for (int i = 0; i < end; i++) {
            final Move move = this.moveLog.getMoves().get(i);
            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                board = transition.getTransitionBoard();
            } else {
                // If something goes weird (illegal move), stop there
                break;
            }
        }
        return board;
    }

    // =============================================================
    // AI
    // =============================================================


    private static class TableGameAiWatcher implements Observer {
        @Override
        public void update(final Observable o, final Object arg) {
            final Table table = Table.get();

            // If the game is already over, do nothing
            if (table.isGameOver()) {
                return;
            }

            if (InsufficientMaterial.isDraw(table.getGameBoard())) {
                table.setGameOver(true);
                table.showFinalEvaluation();
                JOptionPane.showMessageDialog(
                        Table.get().getBoardPanel(),
                        "Game drawn by insufficient mating material.",
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            // Normal AI trigger condition
            if (table.getGameSetup().isAIPlayer(table.getGameBoard().currentPlayer())
                    && !table.getGameBoard().currentPlayer().isInCheckMate()
                    && !table.getGameBoard().currentPlayer().isInStaleMate()) {

                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            // Existing checkmate/stalemate handling
            if (table.getGameBoard().currentPlayer().isInCheckMate()) {
                table.setGameOver(true);
                // show checkmate dialog...
                table.showFinalEvaluation();
            } else if (table.getGameBoard().currentPlayer().isInStaleMate()) {
                table.setGameOver(true);
                // show stalemate dialog...
                table.showFinalEvaluation();
            }
            // If threefold repetition reached, mark game over and show dialog
            if (table.isThreefoldRepetition()) {
                table.setGameOver(true);
                table.showFinalEvaluation();
                JOptionPane.showMessageDialog(
                        Table.get().getBoardPanel(),
                        "Game drawn by threefold repetition.",
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return; // IMPORTANT: do not start AI after this
            }
        }
    }


    private static class AIThinkTank extends SwingWorker<Move, String> {
        private AIThinkTank(){
        }

        @Override
        protected Move doInBackground() throws Exception {
            final MoveStrategy miniMax = new MiniMax(Table.get().gameSetup.getSearchDepth());
            final Move bestMove = miniMax.execute(Table.get().getGameBoard());
            return bestMove;
        }
        @Override
        protected void done() {
            try {
                final Move bestMove = get();
                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().printMoveHistoryText(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().moveLog);
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
            } catch(InterruptedException e) {
                e.printStackTrace();
            } catch(ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateGameBoard(final Board board){
        this.chessBoard = board;
        this.repetitionTracker.recordPosition(board);
        hideEvaluation();
    }

    public void updateComputerMove(final Move move){
        this.computerMove = move;
        hideEvaluation();
    }

    private MoveLog getMoveLog (){
        return this.moveLog;
    }

    private GameHistoryPanel getGameHistoryPanel(){
        return this.gameHistoryPanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel(){
        return this.takenPiecesPanel;
    }

    private BoardPanel getBoardPanel(){
        return this.boardPanel;
    }

    private void moveMadeUpdate (PlayerType playerType) {
        if (this.gameOver) {
            return; // don’t notify/watch once the game is over
        }
        this.historyCursor = -1;
        setChanged();
        notifyObservers(playerType);
        SwingUtilities.invokeLater(() -> this.evaluationPanel.updateEvaluation(this.chessBoard));
    }

    public enum BoardDirection{
        NORMAL {
            @Override
            List<TilePanel> traverse (final List<TilePanel> boardTiles){
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse (final List<TilePanel> boardTiles){
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };
        abstract List<TilePanel> traverse (final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    private class BoardPanel extends JPanel {
        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }
        public void drawBoard (final Board board) {
            removeAll();
            for (final TilePanel tilePanel : Table.this.boardDirection.traverse(this.boardTiles)){
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            revalidate();
            repaint();
        }
    }

    public static class MoveLog {
        private final List<Move> moves;

        MoveLog(){
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves (){
            return this.moves;
        }

        public void addMove(final Move move){
            this.moves.add(move);
        }

        public int size (){
            return this.moves.size();
        }

        public void clear (){
            this.moves.clear();
        }

        public boolean removeMove (final Move move){
            return this.moves.remove(move);
        }

        public Move removeMove (int index){
            return this.moves.remove(index);
        }

        public Move getLastMove(){return this.moves.get(this.moves.size()-1);}
    }

    public void showPositionAtMove(final int moveIndex) {
        final Board historicBoard = buildBoardAtMoveIndex(moveIndex);
        this.boardPanel.drawBoard(historicBoard);
    }

    enum PlayerType {
        HUMAN,
        COMPUTER
    }

    private class TilePanel extends JPanel{
        private final int tileId;

        TilePanel(final BoardPanel boardPanel,
                  final int tileId){
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if(isRightMouseButton(e)) {
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    } else if(isLeftMouseButton(e)) {
                        if (sourceTile == null) { //if you haven't selected a source tile...
                            sourceTile = chessBoard.getTile(tileId); //...get the tile and assign it to sourceTile...
                            humanMovedPiece = sourceTile.getPiece(); //...and get the piece and assign it to humanMovedPiece
                            if (humanMovedPiece == null) { //if the tile you select has no piece on it...
                                sourceTile = null; //...set the sourceTile to null and exit out of this code to run again
                            }
                        } else {
                            destinationTile = chessBoard.getTile(tileId); //if the humanMovedPiece is not null, it will create the move on the board
                            final Move move = Move.MoveFactory.createMove(chessBoard,
                                    sourceTile.getTileCoordinate(),
                                    destinationTile.getTileCoordinate());
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                chessBoard = chessBoard.currentPlayer().makeMove(move).getTransitionBoard();
                                moveLog.addMove(move);
                            }
                            sourceTile = null;
                            destinationTile = null;
                            humanMovedPiece = null;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                gameHistoryPanel.printMoveHistoryText(chessBoard, moveLog);
                                takenPiecesPanel.redo(moveLog);
                                if(gameSetup.isAIPlayer(chessBoard.currentPlayer())){
                                    Table.get().moveMadeUpdate(PlayerType.HUMAN);
                                }
                                boardPanel.drawBoard(chessBoard);
                            }
                        });
                    }
                }
                @Override
                public void mousePressed(final MouseEvent e) {

                }
                @Override
                public void mouseReleased(final MouseEvent e) {

                }
                @Override
                public void mouseEntered(final MouseEvent e) {

                }
                @Override
                public void mouseExited(final MouseEvent e) {

                }
            });
            validate();
        }

        public void drawTile (final Board board){
            assignTileColor();
            assignTilePieceIcon(board);
            highlightLegals(board);
            validate();
            repaint();
        }

        private void highlightLegals (final Board board) {
            if(highlightLegalMoves) {
                for(final Move move : pieceLegalMoves(board)){
                    if(move.getDestinationCoordinate() == this.tileId){
                        try{
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves (final Board board) {
            if(humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()){
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTilePieceIcon (final Board board){
            this.removeAll();
            if(board.getTile(this.tileId).isTileOccupied()){
                try {
                    final BufferedImage image =
                            ImageIO.read(new File(defaultPieceImagesPath +
                                    board.getTile(this.tileId).getPiece().getPieceAlliance().toString().charAt(0) +
                                    board.getTile(this.tileId).getPiece().toString() + ".gif"));
                    add(new JLabel(new ImageIcon(image)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void assignTileColor(){
            if(BoardUtils.EIGHTH_RANK[this.tileId] ||
                    BoardUtils.SIXTH_RANK[this.tileId] ||
                    BoardUtils.FOURTH_RANK[this.tileId] ||
                    BoardUtils.SECOND_RANK[this.tileId]) {
                setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            } else if(BoardUtils.SEVENTH_RANK[this.tileId] ||
                    BoardUtils.FIFTH_RANK[this.tileId] ||
                    BoardUtils.THIRD_RANK[this.tileId]  ||
                    BoardUtils.FIRST_RANK[this.tileId]) {
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
            }
        }
    }

}

