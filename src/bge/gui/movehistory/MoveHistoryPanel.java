package bge.gui.movehistory;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import bge.game.GameRunner;
import bge.game.IPosition;
import bge.game.MoveHistory;
import bge.gui.GameMouseAdapter;
import bge.gui.ScrollableGamePanel;
import bge.gui.analysis.AnalysisPanel;
import bge.main.BoardGameEngineMain;

@SuppressWarnings("serial")
public class MoveHistoryPanel<M> extends JPanel {
    private static final String NAME = "Move History";

    private final MoveHistoryState<M> moveHistoryState;
    private final ScrollableGamePanel moveHistoryPanel;

    public MoveHistoryPanel() {
        setLayout(new BorderLayout());
        BoardGameEngineMain.initComponent(this);

        moveHistoryState = new MoveHistoryState<>();

        JScrollPane scrollPane = AnalysisPanel.createScrollPane(false);
        JViewport viewport = scrollPane.getViewport();
        moveHistoryPanel = new ScrollableGamePanel(viewport, moveHistoryState, g -> moveHistoryState.drawOn(g));
        viewport.setView(moveHistoryPanel);

        GameMouseAdapter mouseAdapter = new GameMouseAdapter(moveHistoryState.mouseTracker);
        moveHistoryPanel.addMouseMotionListener(mouseAdapter);
        moveHistoryPanel.addMouseListener(mouseAdapter);

        JPanel topPanel = BoardGameEngineMain.initComponent(new JPanel(new FlowLayout(FlowLayout.LEADING)));
        topPanel.add(BoardGameEngineMain.initComponent(new JLabel("Move History")), BorderLayout.NORTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setGameRunner(GameRunner<M, IPosition<M>> gameRunner) {
        moveHistoryState.setGameRunner(gameRunner);
    }

    public void setMoveHistory(MoveHistory<M> moveHistory) {
        moveHistoryState.setMoveHistory(moveHistory);
        moveHistoryPanel.checkResized();
    }

    public void startDrawing() {
        moveHistoryPanel.addToGameLoop(NAME);
    }

    public void stopDrawing() {
        moveHistoryPanel.removeFromGameLoop(NAME);
    }
}