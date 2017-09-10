package game.gomoku;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import game.Coordinate;

public class GomokuPositionTest {
	@Test
	public void testWinsHorizontal() {
		GomokuPosition position = new GomokuPosition();
		int numMoves = 361;
		for (int x = 18; x > 14; --x) {
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(x, 0));
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(x, 1));
		}
		position.makeMove(Coordinate.valueOf(14, 0));
		assertEquals(0, position.getPossibleMoves().size());
	}

	@Test
	public void testWinsVertical() {
		GomokuPosition position = new GomokuPosition();
		int numMoves = 361;
		for (int y = 18; y > 14; --y) {
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(0, y));
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(1, y));
		}
		position.makeMove(Coordinate.valueOf(0, 14));
		assertEquals(0, position.getPossibleMoves().size());
	}

	@Test
	public void testWinsDiagonalRight() {
		GomokuPosition position = new GomokuPosition();
		int numMoves = 361;
		for (int x = 0; x < 4; ++x) {
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(x, x));
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(x + 1, x));
		}
		position.makeMove(Coordinate.valueOf(4, 4));
		assertEquals(0, position.getPossibleMoves().size());
	}

	@Test
	public void testWinsDiagonalLeft() {
		GomokuPosition position = new GomokuPosition();
		int numMoves = 361;
		for (int x = 0; x < 4; ++x) {
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(x, 18 - x));
			assertEquals(numMoves--, position.getPossibleMoves().size());
			position.makeMove(Coordinate.valueOf(x + 1, 18 - x));
		}
		position.makeMove(Coordinate.valueOf(4, 14));
		assertEquals(0, position.getPossibleMoves().size());
	}
}