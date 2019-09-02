package bge.game.photosynthesis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

import bge.game.photosynthesis.PhotosynthesisPosition.Buy;
import bge.game.photosynthesis.PhotosynthesisPosition.EndTurn;
import bge.game.photosynthesis.PhotosynthesisPosition.MainBoard;
import bge.game.photosynthesis.PhotosynthesisPosition.PlayerBoard;
import bge.game.photosynthesis.PhotosynthesisPosition.Seed;
import bge.game.photosynthesis.PhotosynthesisPosition.Setup;
import bge.game.photosynthesis.PhotosynthesisPosition.Upgrade;
import bge.igame.Coordinate;
import bge.igame.IPosition;
import bge.igame.MoveList;

public class TrimmedPhotosynthesisPosition implements IPosition<IPhotosynthesisMove> {
    private final PhotosynthesisPosition position;

    private final Stack<IPhotosynthesisMove> cursors;

    public TrimmedPhotosynthesisPosition(PhotosynthesisPosition position) {
        this(position, ((Supplier<Stack<IPhotosynthesisMove>>) (() -> {
            Stack<IPhotosynthesisMove> initialCursors = new Stack<>();
            initialCursors.push(null);
            return initialCursors;
        })).get());
    }

    private TrimmedPhotosynthesisPosition(PhotosynthesisPosition position, Stack<IPhotosynthesisMove> cursors) {
        this.position = position;
        this.cursors = cursors;
    }

    public int[] getResult() {
        return position.getResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public IPosition<IPhotosynthesisMove> createCopy() {
        return new TrimmedPhotosynthesisPosition(
                (PhotosynthesisPosition) position.createCopy(),
                (Stack<IPhotosynthesisMove>) cursors.clone());
    }

    @Override
    public void getPossibleMoves(MoveList<IPhotosynthesisMove> moveList) {
        List<IPhotosynthesisMove> moves = new ArrayList<>();
        IPhotosynthesisMove cursor = cursors.peek();

        position.getPossibleMoves(new MoveList<IPhotosynthesisMove>() {
            @Override
            public MoveList<IPhotosynthesisMove> subList(int beginIndex) {
                return null;
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
            public IPhotosynthesisMove get(int index) {
                return null;
            }

            @Override
            public boolean contains(IPhotosynthesisMove move) {
                return false;
            }

            @Override
            public void clear() {
            }

            @Override
            public void addQuietMove(IPhotosynthesisMove move, IPosition<IPhotosynthesisMove> position) {
                moves.add(move);
            }

            @Override
            public void addDynamicMove(IPhotosynthesisMove move, IPosition<IPhotosynthesisMove> position) {
            }

            @Override
            public void addAllQuietMoves(IPhotosynthesisMove[] moves, IPosition<IPhotosynthesisMove> position) {
            }

            @Override
            public void addAllDynamicMoves(IPhotosynthesisMove[] moves, IPosition<IPhotosynthesisMove> position) {
            }
        });

        for (IPhotosynthesisMove move : moves) {
            if (PhotosynthesisMoveComparator.INSTANCE.compare(cursor, move) <= 0) {
                moveList.addQuietMove(move, position);
            }
        }
    }

    @Override
    public int getCurrentPlayer() {
        return position.getCurrentPlayer();
    }

    @Override
    public void makeMove(IPhotosynthesisMove move) {
        int player = position.getCurrentPlayer();

        if (move instanceof Setup || move instanceof EndTurn) {
            // Another player's turn has begun; clear the cursor
            cursors.push(null);
        }
        else {
            cursors.push(move);
        }

        position.makeMove(move);
    }

    @Override
    public void unmakeMove(IPhotosynthesisMove move) {
        position.unmakeMove(move);

        cursors.pop();
    }

    private enum PhotosynthesisMoveComparator implements Comparator<IPhotosynthesisMove> {
        INSTANCE;

        @Override
        public int compare(IPhotosynthesisMove a, IPhotosynthesisMove b) {
            if (a == null) {
                if (b == null) {
                    return 0;
                }

                return -1;
            }
            if (b == null) {
                return 1;
            }

            if (a instanceof Setup) {
                if (b instanceof Setup) {
                    Setup as = (Setup) a;
                    Setup bs = (Setup) b;

                    return coordinateToInt(as.coordinate) - coordinateToInt(bs.coordinate);
                }

                return -1;
            }

            if (b instanceof Setup) {
                return 1;
            }

            if (a instanceof Buy) {
                if (b instanceof Buy) {
                    Buy ab = (Buy) a;
                    Buy bb = (Buy) b;

                    // Reverse the usual ordering here, constraining buys to be from high to low point values.
                    // I think this will reduce the branching more.
                    return bb.buyColumn - ab.buyColumn;
                }

                return -1;
            }

            if (b instanceof Buy) {
                return 1;
            }

            if (a instanceof Upgrade) {
                if (b instanceof Upgrade) {
                    Upgrade au = (Upgrade) a;
                    Upgrade bu = (Upgrade) b;

                    return coordinateToInt(au.coordinate) - coordinateToInt(bu.coordinate);
                }

                return -1;
            }

            if (b instanceof Upgrade) {
                return 1;
            }

            if (a instanceof Seed) {
                if (b instanceof Seed) {
                    Seed as = (Seed) a;
                    Seed bs = (Seed) b;

                    return coordinateToInt(as.source) - coordinateToInt(bs.source);
                }

                return -1;
            }

            if (b instanceof Seed) {
                return 1;
            }

            // a and b are both EndTurn
            return 0;
        }

        private int coordinateToInt(Coordinate coordinate) {
            return coordinate.x + coordinate.y * PhotosynthesisPosition.MainBoard.AXIS_LENGTH;
        }
    }

    public int getSunPosition() {
        return position.getSunPosition();
    }

    public int getPlayerRoundsRemaining() {
        return position.playerRoundsRemaining;
    }

    public MainBoard getMainBoard() {
        return position.mainBoard;
    }

    public int getNumPlayers() {
        return position.numPlayers;
    }

    public PlayerBoard getPlayerBoard(int i) {
        return position.playerBoards[i];
    }
}