package bge.game.forkjoinexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bge.analysis.search.IterativeDeepeningTreeSearcher;
import bge.analysis.strategy.MinimaxStrategy;
import bge.analysis.strategy.MoveListProvider;
import bge.game.MoveListFactory;

public class ForkJoinExmpleGameTreeSearchTest {
    @BeforeClass
    public static void setSleepTimes() {
        ForkJoinExampleThreadTracker.setSleepTimes(1, 0, 0);
    }

    @Before
    public void clear() {
        ForkJoinExampleThreadTracker.searchStarted();
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
        assertSearch(3, 2, 2, 3, 4, 3);
    }

    private static void assertSearch(int treeDepth, int branchingFactor, int searchDepth, int numWorkers, int expectedNodes, int expectedWorkers) {
        MoveListFactory<ForkJoinExampleNode> moveListFactory = new MoveListFactory<>(ForkJoinExampleGame.MAX_MOVES);
        ForkJoinExampleStraregy strategy = new ForkJoinExampleStraregy(
                new MinimaxStrategy<>(new ForkJoinPositionEvaluator(), new MoveListProvider<>(moveListFactory)));
        IterativeDeepeningTreeSearcher<ForkJoinExampleNode, ForkJoinExampleTree> treeSearcher = new IterativeDeepeningTreeSearcher<>(strategy, moveListFactory,
                numWorkers);
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
        assertEquals("Unexpected num workers, reevaluated = " + ForkJoinExampleThreadTracker.getNodesReevaluated(), expectedWorkers, threadNames.size());
        treeSearcher.stopSearch(true);
    }
}