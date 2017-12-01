package game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import gui.GuiPlayer;

public class GameRunnerTest {
	@Test
	public void testStartStopGame() throws InterruptedException {
		AddToListTestGame game = new AddToListTestGame();
		GameRunner<?, ?> gameRunner = new GameRunner<>(game, new GameObserver<>(), new MoveListFactory<>(1));
		gameRunner.startNewGame(Collections.singletonList(game.player));
		Thread.sleep(10);// sleep a little to let the list populate
		gameRunner.endGame();
		assertTrue(game.list.size() > 0);
		assertEquals(2, game.numNewPositions);
	}

	@Test
	public void testStartTwice() throws InterruptedException {
		AddToListTestGame game = new AddToListTestGame();
		GameRunner<?, ?> gameRunner = new GameRunner<>(game, new GameObserver<>(), new MoveListFactory<>(1));
		gameRunner.startNewGame(Collections.singletonList(game.player));
		gameRunner.startNewGame(Collections.singletonList(game.player));
		Thread.sleep(10);// sleep a little to let the list populate
		gameRunner.endGame();
		assertTrue(game.list.size() > 0);
		assertEquals(3, game.numNewPositions);
	}

	@Test
	public void testEndTwice() throws InterruptedException {
		AddToListTestGame game = new AddToListTestGame();
		GameRunner<?, ?> gameRunner = new GameRunner<>(game, new GameObserver<>(), new MoveListFactory<>(1));
		gameRunner.startNewGame(Collections.singletonList(game.player));
		Thread.sleep(10);// sleep a little to let the list populate
		gameRunner.endGame();
		gameRunner.endGame();
		assertTrue(game.list.size() > 0);
		assertEquals(2, game.numNewPositions);
	}

	@Test
	public void testEndWhenWaitingOnPlayer() throws InterruptedException {
		AddToListTestGame game = new AddToListTestGame(GuiPlayer.HUMAN);
		GameRunner<?, ?> gameRunner = new GameRunner<>(game, new GameObserver<>(), new MoveListFactory<>(1));
		gameRunner.startNewGame(Collections.singletonList(game.player));
		Thread.sleep(10);
		gameRunner.endGame();
		assertEquals(2, game.numNewPositions);
	}

	@Test
	public void testStardAndEndWhenNoMoves() throws InterruptedException {
		AddToListTestGame game = new AddToListTestGame(new AddToListTestPlayer(), i -> Collections.emptyList());
		GameRunner<?, ?> gameRunner = new GameRunner<>(game, new GameObserver<>(), new MoveListFactory<>(1));
		gameRunner.startNewGame(Collections.singletonList(game.player));
		assertEquals(2, game.numNewPositions);
	}

	static class AddToListTestGame implements IGame<Integer, AddToListPosition> {
		final IPlayer player;
		final List<Integer> list = new ArrayList<>();
		final Function<Integer, List<Integer>> possibleMovesFunction;
		int numNewPositions = 0;

		public AddToListTestGame() {
			this(new AddToListTestPlayer());
		}

		public AddToListTestGame(IPlayer player) {
			this(player, i -> Collections.singletonList(i));
		}

		public AddToListTestGame(IPlayer player, Function<Integer, List<Integer>> possibleMovesFunction) {
			this.player = player;
			this.possibleMovesFunction = possibleMovesFunction;
		}

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public int getNumberOfPlayers() {
			return 1;
		}

		@Override
		public int getMaxMoves() {
			return 0; // Unused
		}

		@Override
		public AddToListPosition newInitialPosition() {
			numNewPositions++;
			list.clear();
			return new AddToListPosition(0, list, possibleMovesFunction);
		}
	}

	static class AddToListTestPlayer implements IPlayer {
		@Override
		public <M, P extends IPosition<M, P>> M getMove(P position) {
			MoveList<M> possibleMoves = new ArrayMoveList<>(1);
			position.getPossibleMoves(possibleMoves);
			return possibleMoves.get(0);
		}

		@Override
		public void notifyTurnEnded() {
		}

		@Override
		public void notifyGameEnded() {
		}
	}

	static class AddToListPosition implements IPosition<Integer, AddToListPosition> {
		int index;
		final List<Integer> list;
		final Function<Integer, List<Integer>> possibleMovesFunction;

		private AddToListPosition(int index, List<Integer> list, Function<Integer, List<Integer>> possibleMovesFunction) {
			this.index = index;
			this.list = list;
			this.possibleMovesFunction = possibleMovesFunction;
		}

		@Override
		public void getPossibleMoves(MoveList<Integer> moveList) {
			List<Integer> moves = possibleMovesFunction.apply(index);
			moveList.setQuietMoves(moves.toArray(new Integer[moves.size()]), this);
		}

		@Override
		public int getCurrentPlayer() {
			return 1;
		}

		@Override
		public void makeMove(Integer move) {
			list.add(move);
			++index;
		}

		@Override
		public void unmakeMove(Integer move) {
			list.remove(move);
			--index;
		}

		@Override
		public AddToListPosition createCopy() {
			return new AddToListPosition(index, new ArrayList<>(list), possibleMovesFunction);
		}
	}
}
