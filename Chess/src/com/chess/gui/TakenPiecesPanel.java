package com.chess.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.gui.Table.MoveLog;
import com.google.common.primitives.Ints;

public class TakenPiecesPanel extends JPanel {

    private final JPanel whitePanel;
    private final JPanel blackPanel;
    private static final Color PANEL_COLOR = Color.decode("0xFDF36");
    private static final Dimension TAKEN_PIECES_DIMENSION = new Dimension(400, 80);
    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);

    public TakenPiecesPanel() {
        super(new BorderLayout());
        setBackground(PANEL_COLOR);
        setBorder(PANEL_BORDER);
        this.whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        this.blackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        this.whitePanel.setBackground(PANEL_COLOR);
        this.blackPanel.setBackground(PANEL_COLOR);
        this.add(this.whitePanel, BorderLayout.NORTH);
        this.add(this.blackPanel, BorderLayout.SOUTH);
        setPreferredSize(TAKEN_PIECES_DIMENSION);
    }

    public void redo(final MoveLog moveLog) {
        this.blackPanel.removeAll();
        this.whitePanel.removeAll();

        final List<Piece> whiteTakenPieces = new ArrayList<>();
        final List<Piece> blackTakenPieces = new ArrayList<>();

        for (final Move move : moveLog.getMoves()) {
            if (move.isAttack()) {
                final Piece takenPiece = move.getAttackedPiece();
                if (takenPiece.getPieceAlliance().isWhite()) {
                    whiteTakenPieces.add(takenPiece);
                } else if (takenPiece.getPieceAlliance().isBlack()) {
                    blackTakenPieces.add(takenPiece);
                } else {
                    throw new RuntimeException("Should Not Reach Here!");
                }
            }
        }
        Collections.sort(whiteTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(Piece o1, Piece o2) {
                return Ints.compare(o1.getPieceValue(), o2.getPieceValue());
            }
        });
        Collections.sort(blackTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(Piece o1, Piece o2) {
                return Ints.compare(o1.getPieceValue(), o2.getPieceValue());
            }
        });

        for(final Piece takenPiece : whiteTakenPieces){
            try {
                final BufferedImage image = ImageIO.read(new File("art/pieces/" +
                                                            takenPiece.getPieceAlliance().toString().substring(0,1) + ""
                                                          + takenPiece + ".gif"));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(
                        icon.getIconWidth() - 15, icon.getIconWidth() - 15, Image.SCALE_SMOOTH)));
                this.blackPanel.add(imageLabel);
            } catch (final IOException e){
                e.printStackTrace();
            }
        }

        for(final Piece takenPiece : blackTakenPieces){
            try {
                final BufferedImage image = ImageIO.read(new File("art/pieces/" +
                        takenPiece.getPieceAlliance().toString().substring(0,1) + ""
                        + takenPiece.toString() + ".gif"));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(
                        icon.getIconWidth() - 15, icon.getIconWidth() - 15, Image.SCALE_SMOOTH)));
                this.whitePanel.add(imageLabel);
            } catch (final IOException e){
                e.printStackTrace();
            }
        }
        validate();
    }
}
