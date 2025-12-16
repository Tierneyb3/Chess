♟️ Java Chess Engine with GUI, AI & Analysis

A fully-featured Java chess application built from scratch, combining a custom chess engine with a Swing-based GUI, AI opponents, move animation, and analysis tools inspired by modern online chess platforms.

This project emphasizes clean architecture, correct rule enforcement, and interactive analysis, making it suitable both as a learning project and a portfolio showcase.

🚀 Features
Gameplay

Human vs Human

Human vs Computer

Computer vs Computer

Game setup dialog (player type & AI difficulty)

Full rule support:

Check, checkmate, stalemate

Threefold repetition

Insufficient material detection

Castling (king & queen side)

Pawn promotion

En passant (engine-level)

Artificial Intelligence

Multiple difficulty levels:

Greedy (1-ply)

Shallow Minimax

Deeper Minimax with alpha-beta pruning

Adjustable search depth via UI

Board evaluation considers:

Material balance

King safety

Endgame draw conditions

AI avoids trivial repetition when a winning position exists

Analysis & Navigation

Clickable move history

Keyboard navigation:

← / → step backward or forward through moves

Navigation respects the currently selected historical position

Evaluation bar:

Updates per position

Shows numeric evaluation or M:White / M:Black for forced mates

Appears automatically when the game ends (analysis mode)

Animation & UI

Smooth animated piece movement

Animated history stepping

Legal move highlighting

Taken pieces panel

Evaluation bar

Swing-based layered rendering system

🧠 Architecture

Engine/UI separation
Game logic is independent of rendering and user input.

Immutable board states
Each move generates a new Board, simplifying undo/redo and analysis.

Observer pattern
AI players automatically respond to human moves.

Modular design
AI, evaluation, GUI, and game state tracking are cleanly separated.

📁 Project Structure
com.chess

├── engine

│   ├── board        # Board, Move, Tile, transitions

│   ├── pieces       # All chess pieces

│   ├── player       # Human & AI players

│   └── ai           # Minimax, evaluators, difficulty logic



├── gui

│   ├── Table                # Central controller

│   ├── BoardPanel           # Board rendering & animation

│   ├── TilePanel            # Individual squares

│   ├── GameHistoryPanel     # Move list & navigation

│   ├── TakenPiecesPanel

│   ├── EvaluationPanel

│   └── GameSetup            # Setup dialog

│
└── JChess.java               # Entry point

▶️ Running the Project
Requirements

Java JDK 14+

IntelliJ IDEA (recommended)

Run
com.chess.JChess

Use Game → Setup Game to configure players and AI difficulty.

🛠️ In Progress / Future Improvements

Full SAN/PGN import/export (parser partially implemented and preserved)

Opening book support

Time controls (blitz / rapid / classical)

Further AI evaluation tuning

Engine performance optimizations

📌 Notes

SAN/PGN parsing code is intentionally retained (commented/partial) for future expansion.

This project was built incrementally with a focus on correctness, maintainability, and real-world chess behavior.

📄 License

This project is for educational and personal use.

