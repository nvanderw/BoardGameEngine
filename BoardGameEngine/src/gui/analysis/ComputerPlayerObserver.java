package gui.analysis;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import analysis.AnalysisResult;
import analysis.ComputerPlayer;
import analysis.search.ThreadNumber;
import game.TwoPlayers;
import gui.DrawingMethods;
import main.BoardGameEngineMain;

public class ComputerPlayerObserver implements DrawingMethods {
	private static final int MS_PER_UPDATE = DrawingMethods.roundS(1000.0 / 60);

	private final int playerNum;

	private ComputerPlayerResult currentResult = new ComputerPlayerResult(null, Collections.emptyList(), 0);

	private volatile boolean keepObserving = true;

	public ComputerPlayerObserver(ComputerPlayer computerPlayer, int playerNum, Consumer<String> nameConsumer, Consumer<String> currentDepthConsumer) {
		this.playerNum = playerNum;
		new Thread(() -> {
			nameConsumer.accept(computerPlayer.toString() + "...");
			do {
				currentResult = computerPlayer.getCurrentResult();
				currentDepthConsumer.accept(String.format("depth = %-3d", currentResult.depth));
				synchronized (this) {
					try {
						wait(MS_PER_UPDATE);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			} while (keepObserving && !currentResult.isDecided);
			nameConsumer.accept(computerPlayer.toString());
			currentDepthConsumer.accept(String.format("depth = %-3d", currentResult.depth));
		}, "Computer_Observation_Thread_" + ThreadNumber.getThreadNum(getClass())).start();
	}

	public void drawOn(Graphics2D graphics) {
		List<ObservedMoveWithScore> currentMoves = currentResult.moves;
		if (currentMoves == null) {
			return;
		}
		graphics.setFont(BoardGameEngineMain.DEFAULT_FONT_SMALL);
		FontMetrics metrics = graphics.getFontMetrics();
		int stringHeight = metrics.getHeight() + 2;
		int i = 0;
		int startY = stringHeight;
		while (i < currentMoves.size()) {
			int y = startY + i * stringHeight;
			ObservedMoveWithScore moveWithScore = currentMoves.get(i);
			graphics.setColor(moveWithScore.isPartial || AnalysisResult.isGameOver(moveWithScore.score) ? BoardGameEngineMain.FOREGROUND_COLOR : BoardGameEngineMain.LIGHTER_FOREGROUND_COLOR);
			graphics.drawString(i < 9 ? (i + 1) + ".   " : (i + 1) + ". ", 20, y);
			graphics.drawString(String.format("%-13s", getScoreString(moveWithScore.score, playerNum == TwoPlayers.PLAYER_1)), 45, y);
			graphics.drawString(moveWithScore.moveString, 100, y);
			++i;
		}
	}

	private static String getScoreString(double score, boolean isPlayerOne) {
		if (AnalysisResult.isDraw(score)) {
			return "(Draw)";
		} else if (AnalysisResult.WIN == score) {
			return "(Win)";
		} else if (AnalysisResult.LOSS == score) {
			return "(Loss)";
		} else {
			long playerScore = Math.round(100 * (isPlayerOne ? score : -score));
			double roundScore = playerScore / 100.0;
			return String.format("(%.2f)", Double.valueOf(roundScore));
		}
	}

	public synchronized void stopObserving() {
		keepObserving = false;
		notify();
	}
}
