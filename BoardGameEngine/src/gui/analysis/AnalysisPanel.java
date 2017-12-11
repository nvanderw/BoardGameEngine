package gui.analysis;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import analysis.ComputerPlayer;
import analysis.ComputerPlayerInfo;
import game.IPlayer;
import game.IPosition;
import game.PositionChangedInfo;
import gui.GameRegistry;
import gui.ScrollableGamePanel;
import main.BoardGameEngineMain;

@SuppressWarnings("serial")
public class AnalysisPanel<M, P extends IPosition<M, P>> extends JPanel {
	private static final String NAME = "Analysis";

	private final String gameName;
	private final ComputerPlayerInfo<M, P> computerPlayerInfo;
	private final ScrollableGamePanel analysisPanel;
	private IAnalysisState<M, P> analysisState;

	private P position;

	public AnalysisPanel(String gameName) {
		this.gameName = gameName;

		setLayout(new BorderLayout());
		BoardGameEngineMain.initComponent(this);

		computerPlayerInfo = GameRegistry.newDefaultComputerPlayerInfo(gameName);
		GameRegistry.updateComputerPlayerInfo(computerPlayerInfo, gameName, computerPlayerInfo.strategyName, computerPlayerInfo.numWorkers, Long.MAX_VALUE);

		analysisState = new InfiniteAnalysisState<>(gameName, position, computerPlayerInfo);

		JScrollPane scrollPane = BoardGameEngineMain.initComponent(new JScrollPane());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JViewport viewport = scrollPane.getViewport();
		analysisPanel = new ScrollableGamePanel(viewport, analysisState, g -> analysisState.drawOn(g));
		viewport.setView(analysisPanel);
		analysisState.setOnResize(analysisPanel::checkResized);

		add(analysisState.getTopPanel(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void startDrawing() {
		analysisPanel.addToGameLoop(NAME);
	}

	public void stopAnalysis() {
		analysisPanel.removeFromGameLoop(NAME);
		analysisState.stopAnalysis();
	}

	public void gamePaused() {
		if (!(analysisState instanceof InfiniteAnalysisState<?, ?>)) {
			analysisState.stopAnalysis();
			setAnalysisState(new InfiniteAnalysisState<>(gameName, position, computerPlayerInfo));
		}
	}

	private void setAnalysisState(IAnalysisState<M, P> newAnalysisState) {
		remove(analysisState.getTopPanel());
		add(newAnalysisState.getTopPanel(), BorderLayout.NORTH);

		analysisPanel.setSizable(newAnalysisState);
		newAnalysisState.setOnResize(analysisPanel::checkResized);

		revalidate();

		analysisState = newAnalysisState;
	}

	public void positionChanged(PositionChangedInfo<M, P> positionChangedInfo) {
		this.position = positionChangedInfo.position;
		IPlayer player = positionChangedInfo.currentPlayer;
		if (player != null && player instanceof ComputerPlayer) {
			analysisState.stopAnalysis();
			setAnalysisState(new ComputerPlayerObservationState<>((ComputerPlayer) player, position.getCurrentPlayer()));
		} else if (!(analysisState instanceof InfiniteAnalysisState<?, ?>)) {
			analysisState.stopAnalysis();
			setAnalysisState(new InfiniteAnalysisState<>(gameName, position, computerPlayerInfo));
			analysisState.setPosition(position);
		} else {
			analysisState.setPosition(position);
		}
	}
}
