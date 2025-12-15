package com.chess;
import com.chess.engine.board.Board;
import com.chess.gui.Table;

import javax.swing.*;

public class JChess {

    public static void main (String[] args){
        SwingUtilities.invokeLater(() -> {
            Table.get().show();
        });
    }
}
