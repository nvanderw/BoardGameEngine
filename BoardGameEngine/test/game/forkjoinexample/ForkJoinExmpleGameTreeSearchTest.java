package game.forkjoinexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import analysis.search.IterativeDeepeningTreeSearcher;
import game.MoveListFactory;

public class ForkJoinExmpleGameTreeSearchTest {
	@BeforeClass
	public static void clear() {
		ForkJoinExampleThreadTracker.searchStarted();
		ForkJoinExampleThreadTracker.setSleepTimes(0, 0, 0);
	}

	@Test
	public void testSearch_TwoNodesOneWorkers() {
		assertSearch(2, 2, 1, 1, 2, 1);
	}

	@Test
	public void testSearch_TwoNodesTwoWorkers() {
		assertSearch(2, 2, 1, 2, 2, 2);
	}

	@Test
	public void testSearch_FourNodesThreeWorkers() {
		assertSearch(3, 3, 2, 3, 9, 3);
	}

	private static void assertSearch(int treeDepth, int branchingFactor, int searchDepth, int numWorkers, int expectedNodes, int expectedWorkers) {
		MoveListFactory<ForkJoinExampleNode> moveListFactory = new MoveListFactory<>(ForkJoinExampleGame.MAX_MOVES);
		IterativeDeepeningTreeSearcher<ForkJoinExampleNode, ForkJoinExampleTree> treeSearcher = new IterativeDeepeningTreeSearcher<>(new ForkJoinExampleStraregy(moveListFactory),
				moveListFactory, numWorkers);
		ForkJoinExampleTree position = new ForkJoinExampleTree(treeDepth, branchingFactor);
		ForkJoinExampleThreadTracker.init(position);
		treeSearcher.startSearch(position, searchDepth, false);
		List<ForkJoinExampleNode> nodesByDepth = ForkJoinExampleThreadTracker.nodesByDepth().get(searchDepth);
		assertEquals(expectedNodes, nodesByDepth.size());
		Set<String> threadNames = new HashSet<>();
		for (ForkJoinExampleNode node : nodesByDepth) {
			String threadName = ForkJoinExampleThreadTracker.getForkJoinExampleNodeInfo(node).getThreadName();
			assertNotNull("Thread name shoud not be null", threadName);
			threadNames.add(threadName);
		}
		assertEquals(expectedWorkers, threadNames.size());
		treeSearcher.stopSearch(true);
	}
}
