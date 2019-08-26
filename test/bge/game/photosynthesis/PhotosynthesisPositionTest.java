package bge.game.photosynthesis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;

import bge.game.Coordinate;
import bge.game.IPosition;
import bge.game.MoveList;
import bge.game.photosynthesis.PhotosynthesisPosition.Buy;
import bge.game.photosynthesis.PhotosynthesisPosition.EndTurn;
import bge.game.photosynthesis.PhotosynthesisPosition.PlayerBoard;
import bge.game.photosynthesis.PhotosynthesisPosition.Seed;
import bge.game.photosynthesis.PhotosynthesisPosition.Setup;
import bge.game.photosynthesis.PhotosynthesisPosition.Tile;
import bge.game.photosynthesis.PhotosynthesisPosition.Upgrade;

public class PhotosynthesisPositionTest {
    @Test
    public void TestFirstMove() {
        final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

        position.makeMove(new Setup(Coordinate.valueOf(0, 0)));
        assertEquals(1, position.currentPlayer);
    }

    @Test
    public void TestSetup() {
        final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

        position.makeMove(new Setup(Coordinate.valueOf(0, 0)));
        position.makeMove(new Setup(Coordinate.valueOf(0, 1)));
        position.makeMove(new Setup(Coordinate.valueOf(0, 2)));
        position.makeMove(new Setup(Coordinate.valueOf(0, 3)));

        assertEquals(0, position.currentPlayer);
        assertEquals(0, position.setupPlayerRoundsRemaining);
        assertEquals(2 * 6 * 3, position.playerRoundsRemaining);

        for (int p = 0; p < 2; p++) {
            assertEquals(0, position.playerBoards[p].victoryPoints);
            assertEquals(2, position.playerBoards[p].lightPoints);
        }

        Arrays.stream(PhotosynthesisPosition.ALL_COORDS).forEach(coord -> {
            final Tile tile = position.mainBoard.grid[coord.x][coord.y];
            assertEquals(Integer.MAX_VALUE, tile.lastTouchedPlayerRoundsRemaining);
        });

        assertEquals(Integer.MAX_VALUE, position.mainBoard.grid[0][0].lastTouchedPlayerRoundsRemaining);
    }

    @Test
    public void UnapplySetup() {
        final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

        final IPhotosynthesisMove[] moves = new IPhotosynthesisMove[] {
                new Setup(Coordinate.valueOf(0, 0)),
                new Setup(Coordinate.valueOf(1, 0)),
                new Setup(Coordinate.valueOf(2, 0)),
                new Setup(Coordinate.valueOf(3, 0))
        };

        for (final IPhotosynthesisMove move : moves) {
            position.makeMove(move);
        }

        for (int i = moves.length - 1; i >= 0; i--) {
            position.unmakeMove(moves[i]);
        }

        assertEquals(new PhotosynthesisPosition(2), position);
    }

    @Test
    public void ApplyAndUnapplyUpgrade() {
        // Helper to get starting positions with a "seeded" tile
        final Supplier<PhotosynthesisPosition> getStartingPosition = () -> {
            final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

            final Tile tile = position.mainBoard.grid[0][0];
            tile.level = 0;
            tile.player = 0;
            return position;
        };

        final PhotosynthesisPosition position = getStartingPosition.get();

        // Apply a series of upgrades until the tree is collected
        final IPhotosynthesisMove[] moves = new IPhotosynthesisMove[] {
                new Upgrade(Coordinate.valueOf(0, 0)),
                new Upgrade(Coordinate.valueOf(0, 0)),
                new Upgrade(Coordinate.valueOf(0, 0)),
                new Upgrade(Coordinate.valueOf(0, 0))
        };

        for (final IPhotosynthesisMove move : moves) {
            position.makeMove(move);
        }

        final Tile tile = position.mainBoard.grid[0][0];
        assertEquals(position.playerRoundsRemaining, tile.lastTouchedPlayerRoundsRemaining);
        assertEquals(-1, tile.level);
        assertEquals(-1, tile.player);
        assertEquals(14, position.playerBoards[0].victoryPoints);

        // Unmake all the moves and verify that the position is the same as we started with
        for (int i = moves.length - 1; i >= 0; i--) {
            position.unmakeMove(moves[i]);
        }

        final PhotosynthesisPosition expected = new PhotosynthesisPosition(2);
        assertEquals(getStartingPosition.get(), position);
    }

    /** Tests for effects of Upgrade moves on the player board state */
    @Test
    public void Upgrade_PlayerBoard() {
        final PhotosynthesisPosition position = new PhotosynthesisPosition(2);
        final PlayerBoard playerBoard = position.playerBoards[0];

        final Tile tile = position.mainBoard.grid[0][0];
        tile.level = 0;
        tile.player = 0;
        playerBoard.buy[0] = 3;

        position.makeMove(new Upgrade(Coordinate.valueOf(0, 0)));

        assertEquals(1, playerBoard.available[1]);
        assertEquals(4, playerBoard.buy[0]);

        position.makeMove(new Upgrade(Coordinate.valueOf(0, 0)));
        assertEquals(0, playerBoard.available[2]);
    }

    @Test
    public void ApplyUnapplyBuy() {
        final int priceOfEverything = Arrays.stream(PhotosynthesisPosition.PRICES)
                .flatMapToInt(x -> Arrays.stream(x))
                .sum();

        final Supplier<PhotosynthesisPosition> getPosition = () -> {
            final PhotosynthesisPosition position = new PhotosynthesisPosition(2);
            final PlayerBoard playerBoard = position.playerBoards[0];

            playerBoard.lightPoints = priceOfEverything;
            return position;
        };

        final PhotosynthesisPosition position = getPosition.get();

        final PlayerBoard playerBoard = position.playerBoards[0];
        final List<IPhotosynthesisMove> moves = new ArrayList<>();

        for (int i = 0; i < PhotosynthesisPosition.PRICES.length; i++) {
            for (int j = 0; j < PhotosynthesisPosition.PRICES[i].length; j++) {
                final Buy move = new Buy(i);
                moves.add(move);
                position.makeMove(move);
            }
        }

        assertEquals(0, playerBoard.lightPoints);

        Collections.reverse(moves);

        for (final IPhotosynthesisMove move : moves) {
            position.unmakeMove(move);
        }

        assertEquals(getPosition.get(), position);
    }

    @Test
    public void ApplyUnapplyEndTurn() {
        int expectedPlayerRounds = 2 * 6 * 3;

        final IPhotosynthesisMove[] setupMoves = new IPhotosynthesisMove[] {
                new Setup(Coordinate.valueOf(0, 0)),
                new Setup(Coordinate.valueOf(0, 1)),
                new Setup(Coordinate.valueOf(0, 2)),
                new Setup(Coordinate.valueOf(0, 3))
        };

        final PhotosynthesisPosition startingPosition = new PhotosynthesisPosition(2);
        for (final IPhotosynthesisMove move : setupMoves) {
            move.applyMove(startingPosition);
        }

        final PhotosynthesisPosition position = (PhotosynthesisPosition) startingPosition.createCopy();

        assertEquals(2, position.playerBoards[0].lightPoints);
        assertEquals(2, position.playerBoards[1].lightPoints);

        position.makeMove(EndTurn.getInstance());
        expectedPlayerRounds--;

        assertEquals(1, position.currentPlayer);
        assertEquals(expectedPlayerRounds, position.playerRoundsRemaining);
        assertEquals(2, position.playerBoards[0].lightPoints);
        assertEquals(2, position.playerBoards[1].lightPoints);

        position.makeMove(EndTurn.getInstance());
        expectedPlayerRounds--;

        assertEquals(expectedPlayerRounds, position.playerRoundsRemaining);
        assertEquals(1, position.currentPlayer);
        assertEquals(4, position.playerBoards[0].lightPoints);
        assertEquals(4, position.playerBoards[1].lightPoints);

        position.unmakeMove(EndTurn.getInstance());
        position.unmakeMove(EndTurn.getInstance());

        assertEquals(startingPosition, position);
    }

    @Test
    public void TestShadowMap() {
        // In this test we test a bunch of combinations of tree height + sun position to
        // see what shadows they create on the board. So bundle them into a class.
        final class TestCase {
            private final int level;

            private final int sunPosition;

            private final Coordinate[] expectedShadowCoords;

            public TestCase(int level, int sunPosition, Coordinate[] expectedShadowCoords) {
                this.level = level;
                this.sunPosition = sunPosition;
                this.expectedShadowCoords = expectedShadowCoords;
            }

            public void validate() {
                final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

                position.playerRoundsRemaining -= sunPosition;

                final Tile tile = position.mainBoard.grid[3][3];
                tile.level = level;
                tile.player = 0;

                final int[][] actualShadows = position.getShadowMap();

                final int[][] expectedShadows = new int[7][7];
                for (final Coordinate coord : expectedShadowCoords) {
                    expectedShadows[coord.x][coord.y] = level;
                }

                assertTrue(Arrays.deepEquals(expectedShadows, actualShadows));
            }
        }

        final TestCase[] testCases = new TestCase[] {
                // Sun at starting position, small tree, validate shadow at (4,3)
                new TestCase(1, 0, new Coordinate[] { Coordinate.valueOf(4, 3) }),
                new TestCase(2, 0, new Coordinate[] { Coordinate.valueOf(4, 3), Coordinate.valueOf(5, 3) }),
                // Height 3, validate shadows at (4,3), (5,3), (6,3)
                new TestCase(
                        3,
                        0,
                        new Coordinate[] {
                                Coordinate.valueOf(4, 3),
                                Coordinate.valueOf(5, 3),
                                Coordinate.valueOf(6, 3) }),

                // Test other sun positions
                new TestCase(1, 1, new Coordinate[] { Coordinate.valueOf(3, 4) }),
                new TestCase(1, 2, new Coordinate[] { Coordinate.valueOf(2, 4) }),
                new TestCase(1, 3, new Coordinate[] { Coordinate.valueOf(2, 3) }),
                new TestCase(1, 4, new Coordinate[] { Coordinate.valueOf(3, 2) }),
                new TestCase(1, 5, new Coordinate[] { Coordinate.valueOf(4, 2) })
        };

        for (final TestCase testCase : testCases) {
            testCase.validate();
        }
    }

    @Test
    public void ApplyUnapplySeed() {
        final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

        final Tile sourceTile = position.mainBoard.grid[3][3];
        final Tile destTile = position.mainBoard.grid[4][2];

        sourceTile.level = 1;
        sourceTile.player = 0;

        final PhotosynthesisPosition original = (PhotosynthesisPosition) position.createCopy();

        final IPhotosynthesisMove move = new Seed(Coordinate.valueOf(3, 3), Coordinate.valueOf(4, 2));
        move.applyMove(position);

        assertEquals(position.playerRoundsRemaining, sourceTile.lastTouchedPlayerRoundsRemaining);
        assertEquals(Integer.MAX_VALUE, destTile.lastTouchedPlayerRoundsRemaining);
        assertEquals(sourceTile.player, destTile.player);
        assertEquals(0, destTile.level);

        move.unapplyMove(position);
        assertEquals(original, position);
    }

    @Test
    public void TestPhotosynthesisPhase() {
        PhotosynthesisPosition position = new PhotosynthesisPosition(2);

        position.mainBoard.grid[0][0].player = 0;
        position.mainBoard.grid[0][0].level = 2;

        position.mainBoard.grid[1][0].player = 1;
        position.mainBoard.grid[1][0].level = 1;
        final PhotosynthesisPosition original = (PhotosynthesisPosition) position.createCopy();

        position.doPhotosynthesis();

        assertEquals(2, position.playerBoards[0].lightPoints);
        assertEquals(0, position.playerBoards[1].lightPoints);

        position = (PhotosynthesisPosition) original.createCopy();
        position.playerRoundsRemaining -= 3;

        position.doPhotosynthesis();

        assertEquals(2, position.playerBoards[0].lightPoints);
        assertEquals(1, position.playerBoards[1].lightPoints);
    }

    @Test
    public void TestEquals() {
        assertEquals(new PhotosynthesisPosition(2), new PhotosynthesisPosition(2));
        assertNotEquals(new PhotosynthesisPosition(3), new PhotosynthesisPosition(4));
    }

    @Test
    public void TestDeepCopy() {
        final PhotosynthesisPosition position = new PhotosynthesisPosition(2);
        final PhotosynthesisPosition copy = (PhotosynthesisPosition) position.createCopy();

        position.makeMove(new Setup(Coordinate.valueOf(0, 0)));
        position.makeMove(new Setup(Coordinate.valueOf(1, 0)));
        position.makeMove(new Setup(Coordinate.valueOf(2, 0)));
        position.makeMove(new Setup(Coordinate.valueOf(3, 0)));

        assertEquals(new PhotosynthesisPosition(2), copy);
    }

    /** Exhaustively check all neighbors of 3,3 in hex coords */
    @Test
    public void TestAreNeighbors() {
        final Coordinate[] positiveTestCases = new Coordinate[] {
                Coordinate.valueOf(2, 3),
                Coordinate.valueOf(4, 3),
                Coordinate.valueOf(3, 2),
                Coordinate.valueOf(3, 4),
                Coordinate.valueOf(2, 4),
                Coordinate.valueOf(4, 2)
        };

        final Set<Coordinate> negativeTestCases = new HashSet<>(Arrays.asList(PhotosynthesisPosition.ALL_COORDS));
        negativeTestCases.removeAll(Arrays.asList(positiveTestCases));

        final Coordinate source = Coordinate.valueOf(3, 3);
        for (final Coordinate dest : positiveTestCases) {
            assertTrue(PhotosynthesisPosition.MainBoard.areNeighbors(source, dest));
        }

        for (final Coordinate dest : negativeTestCases) {
            assertFalse(PhotosynthesisPosition.MainBoard.areNeighbors(source, dest));
        }
    }

    @Test
    public void TestAreNeighbors_Symmetry() {
        for (final Coordinate source : PhotosynthesisPosition.ALL_COORDS) {
            for (final Coordinate dest : PhotosynthesisPosition.ALL_COORDS) {
                assertEquals(
                        PhotosynthesisPosition.MainBoard.areNeighbors(source, dest),
                        PhotosynthesisPosition.MainBoard.areNeighbors(dest, source));

            }
        }
    }

    @Test
    public void TestGetPossibleMoves_Setup() {
        final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

        final Set<IPhotosynthesisMove> expectedMoves = new HashSet<>(
                Arrays.stream(PhotosynthesisPosition.ALL_TILES[0]).map(c -> new Setup(c)).collect(Collectors.toList()));

        final Set<IPhotosynthesisMove> quietMoves = new HashSet<>();
        position.getPossibleMoves(new MockMoveList(m -> quietMoves.add(m)));

        assertEquals(expectedMoves, quietMoves);
    }

    @Test
    public void TestApplyUnapplyRandomMovesUntilEnd_Sequential() {
        for (int seed = 0; seed < 100; seed++) {
            final Random random = new Random(seed);

            final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

            do {
                final List<IPhotosynthesisMove> quietMoves = new ArrayList<>();
                position.getPossibleMoves(new MockMoveList(m -> quietMoves.add(m)));

                final IPhotosynthesisMove move = quietMoves.get(random.nextInt(quietMoves.size()));
                final PhotosynthesisPosition copy = (PhotosynthesisPosition) position.createCopy();
                move.applyMove(position);
                move.unapplyMove(position);

                move.applyMove(position);
            } while (position.playerRoundsRemaining > 0);
        }
    }

    @Test
    public void TestApplyUnapplyRandomMovesUntilEnd_Stack() {
        for (int seed = 0; seed < 100; seed++) {
            final Random random = new Random(seed);

            final PhotosynthesisPosition position = new PhotosynthesisPosition(2);

            final Stack<IPhotosynthesisMove> stack = new Stack<>();

            do {
                final List<IPhotosynthesisMove> quietMoves = new ArrayList<>();
                position.getPossibleMoves(new MockMoveList(m -> quietMoves.add(m)));

                final IPhotosynthesisMove move = quietMoves.get(random.nextInt(quietMoves.size()));
                stack.push(move);
                move.applyMove(position);
            } while (position.playerRoundsRemaining > 0);

            while (!stack.empty()) {
                stack.pop().unapplyMove(position);
            }

            assertEquals(new PhotosynthesisPosition(2), position);
        }
    }

    private class MockMoveList implements MoveList<IPhotosynthesisMove> {
        private final Consumer<IPhotosynthesisMove> consumer;

        public MockMoveList(Consumer<IPhotosynthesisMove> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void addDynamicMove(IPhotosynthesisMove move, IPosition<IPhotosynthesisMove> position) {
        }

        @Override
        public void addAllDynamicMoves(IPhotosynthesisMove[] moves, IPosition<IPhotosynthesisMove> position) {

        }

        @Override
        public void addQuietMove(IPhotosynthesisMove move, IPosition<IPhotosynthesisMove> position) {
            consumer.accept(move);
        }

        @Override
        public void addAllQuietMoves(IPhotosynthesisMove[] moves, IPosition<IPhotosynthesisMove> position) {

        }

        @Override
        public IPhotosynthesisMove get(int index) {
            return null;
        }

        @Override
        public boolean contains(IPhotosynthesisMove move) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int numDynamicMoves() {
            return 0;
        }

        @Override
        public MoveList<IPhotosynthesisMove> subList(int beginIndex) {
            return null;
        }

        @Override
        public void clear() {
        }
    }
}
