package bge.main;

import java.awt.Font;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bge.analysis.IPositionEvaluator;
import bge.game.chess.ChessGame;
import bge.game.chess.ChessPositionEvaluator;
import bge.game.gomoku.GomokuGame;
import bge.game.gomoku.GomokuPositionEvaluator;
import bge.game.papersoccer.PaperSoccerGame;
import bge.game.papersoccer.PaperSoccerPositionEvaluator;
import bge.game.photosynthesis.IPhotosynthesisMove;
import bge.game.photosynthesis.PhotosynthesisGame;
import bge.game.photosynthesis.PhotosynthesisPosition;
import bge.game.photosynthesis.PhotosynthesisPositionEvaluator;
import bge.game.photosynthesis.TrimmedPhotosynthesisPosition;
import bge.game.tictactoe.TicTacToeGame;
import bge.game.tictactoe.TicTacToePositionEvaluator;
import bge.game.ultimatetictactoe.UTTTProbabilityPositionEvaluator;
import bge.game.ultimatetictactoe.UltimateTicTacToeGame;
import bge.game.ultimatetictactoe.UltimateTicTacToePositionEvaluator;
import bge.gui.gamestate.MainMenuState;
import bge.igame.IGame;
import bge.igame.IPosition;
import bge.igame.player.ComputerPlayer;
import bge.main.GameRegistry.GameRegistryItem;
import bge.strategy.ts.wrapped.IPositionWrapper;
import gt.component.GamePanel;
import gt.component.MainFrame;
import gt.gamestate.GameStateManager;
import gt.util.Pair;

public class BoardGameEngineMain {
    private static final String PROJECT_NAME = "BoardGameEngine";
    private static final String TITLE = "Board Game Engine";

    public static final Font DEFAULT_FONT = new Font("consolas", Font.PLAIN, 24);
    public static final Font DEFAULT_SMALL_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
    public static final int DEFAULT_SMALL_FONT_HEIGHT = 18;

    private static <M, P extends IPosition<M>> GameRegistryItem<M, P> registerGame(IGame<M, P> game,
            List<Pair<String, IPositionEvaluator<M, P>>> positionEvaluators) {
        GameRegistryItem<M, P> gameRegistryItem = GameRegistry.registerGame(game).addPlayer(ComputerPlayer.NAME);
        for (Pair<String, IPositionEvaluator<M, P>> nameEvaluator : positionEvaluators) {
            gameRegistryItem.addPositionEvaluator(nameEvaluator.getFirst(), nameEvaluator.getSecond());
        }

        return gameRegistryItem;
    }

    public static void registerGames() {
        int maxThreads = Runtime.getRuntime().availableProcessors() - 1;

        // TODO ChessConstants.MAX_REASONABLE_DEPTH, etc
        registerGame(new ChessGame(),
                Collections.singletonList(Pair.valueOf("Evaluator1", new ChessPositionEvaluator())))
        .createComputerPlayerOptions(50, 10000, maxThreads, 20);

        registerGame(new TicTacToeGame(),
                Collections.singletonList(Pair.valueOf("Evaluator1", new TicTacToePositionEvaluator())))
        .createComputerPlayerOptions(50, 10000, maxThreads, 20);

        registerGame(new UltimateTicTacToeGame(),
                Arrays.asList(Pair.valueOf("Evaluator1", new UltimateTicTacToePositionEvaluator()),
                        Pair.valueOf("ProbGuess1", new UTTTProbabilityPositionEvaluator())))
        .createComputerPlayerOptions(50, 10000, maxThreads, 20);

        // TODO GomokuMoveList.class
        registerGame(new GomokuGame(),
                Collections.singletonList(Pair.valueOf("Evaluator1", new GomokuPositionEvaluator())))
                        .createComputerPlayerOptions(50, 10000, maxThreads, 20);

        registerGame(new PaperSoccerGame(),
                Collections.singletonList(Pair.valueOf("Evaluator1", new PaperSoccerPositionEvaluator())))
                        .createComputerPlayerOptions(50, 10000, maxThreads, 20);

        IPositionWrapper<IPhotosynthesisMove, PhotosynthesisPosition, TrimmedPhotosynthesisPosition> wrapper = TrimmedPhotosynthesisPosition::new;

        registerGame(new PhotosynthesisGame(),
                Collections.singletonList(Pair.valueOf("Evaluator1", new PhotosynthesisPositionEvaluator())))
        .addPositionWrapper("None", null)
                        .addPositionWrapper("Ordered", wrapper)
                        .createComputerPlayerOptions(50, 10000, maxThreads, 20);

        // TODO Sodoku
    }

    public static void main(String[] args) {
        registerGames();

        MainFrame mainFrame = new MainFrame(PROJECT_NAME, TITLE);
        GamePanel mainPanel = mainFrame.getGamePanel();

        GameStateManager gameStateManager = mainPanel.getGameStateManager();
        gameStateManager.setGameState(new MainMenuState(gameStateManager));

        mainFrame.show();
    }
}
