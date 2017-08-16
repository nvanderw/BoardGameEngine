package game.forkjoinexample;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import game.forkjoinexample.ForkJoinExampleThreadTracker.ForkJoinExampleNodeInfo;
import gui.GameGuiManager;
import gui.gamestate.GameState.UserInput;
import gui.gamestate.IGameRenderer;

public class ForkJoinExampleGameRenderer implements IGameRenderer<ForkJoinExampleNode, ForkJoinExampleTree> {
	private static final Queue<Color> COLOR_QUEUE = new LinkedList<>(Arrays.asList(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA));

	private static final int BREDTH = (int) Math.round(Math.pow(ForkJoinExampleGame.DEPTH, ForkJoinExampleGame.BRANCHING_FACTOR));

	private final Map<String, Color> threadColorMap = new HashMap<>();

	private double nodeRadius = 0;

	@Override
	public void initializeAndDrawBoard(Graphics2D g) {
		int padding = 20; // pixels on either side
		nodeRadius = (((double) GameGuiManager.getComponentWidth() - 2 * padding) / BREDTH) / 4;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, GameGuiManager.getComponentWidth(), GameGuiManager.getComponentHeight());
	}

	@Override
	public void drawPosition(Graphics2D g, ForkJoinExampleTree position) {
		int width = GameGuiManager.getComponentWidth();
		int height = GameGuiManager.getComponentHeight();
		List<List<ForkJoinExampleNode>> nodesByBredth = ForkJoinExampleThreadTracker.getNodesByBredth();
		for (List<ForkJoinExampleNode> nodes : nodesByBredth) {
			for (ForkJoinExampleNode node : nodes) {
				ForkJoinExampleNodeInfo nodeInfo = ForkJoinExampleThreadTracker.getForkJoinExampleNodeInfo(node);
				double nodeX = nodeInfo.fractionX * width;
				double nodeY = nodeInfo.fractionY * height;
				// draw lines to children
				g.setColor(Color.BLACK);
				for (ForkJoinExampleNode child : node.getChildren()) {
					ForkJoinExampleNodeInfo childInfo = ForkJoinExampleThreadTracker.getForkJoinExampleNodeInfo(child);
					g.drawLine(round(nodeX), round(nodeY), round(childInfo.fractionX * width), round(childInfo.fractionY * height));
				}
				// draw node
				drawCircle(g, nodeX, nodeY, nodeRadius);
				// maybe fill in node
				if (nodeInfo.getThreadName() != null) {
					g.setColor(getColorFromThreadName(nodeInfo.getThreadName()));
					fillCircle(g, nodeX, nodeY, nodeRadius);
				}
			}
		}
	}

	private Color getColorFromThreadName(String threadName) {
		Color color = threadColorMap.get(threadName);
		if (color == null) {
			color = COLOR_QUEUE.poll();
			threadColorMap.put(threadName, color);
		}
		return color;
	}

	@Override
	public ForkJoinExampleNode maybeGetUserMove(UserInput input, ForkJoinExampleTree position) {
		return null; // only the computer plays this game
	}
}
