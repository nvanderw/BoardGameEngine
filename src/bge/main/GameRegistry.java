package bge.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bge.analysis.IPositionEvaluator;
import bge.gui.gamestate.IGameRenderer;
import bge.igame.ArrayMoveList;
import bge.igame.IGame;
import bge.igame.IPosition;
import bge.igame.MoveList;
import bge.igame.MoveListFactory;
import bge.igame.player.ComputerPlayer;
import bge.igame.player.GuiPlayer;
import bge.igame.player.PlayerInfo;
import bge.igame.player.PlayerOptions;
import bge.igame.player.PlayerOptions.CPOptionIntRange;
import bge.igame.player.PlayerOptions.CPOptionStringArray;
import bge.strategy.ts.wrapped.IPositionWrapper;
import gt.component.IMouseTracker;
import gt.gameentity.IGameImageDrawer;

public class GameRegistry {
    private static final Map<String, GameRegistryItem<?, ?>> gameMap = new LinkedHashMap<>();

    @SuppressWarnings("rawtypes")
    public static <M, P extends IPosition<M>> GameRegistryItem<M, P> registerGame(IGame<M, P> game) {
        return registerGame(game, (Class<? extends MoveList>) ArrayMoveList.class);
    }

    @SuppressWarnings("rawtypes")
    public static <M, P extends IPosition<M>> GameRegistryItem<M, P> registerGame(IGame<M, P> game, Class<? extends MoveList> moveListClass) {
        GameRegistryItem<M, P> gameRegistryItem = new GameRegistryItem<>(game, moveListClass);
        gameMap.put(game.getName(), gameRegistryItem);
        return gameRegistryItem;
    }

    public static Set<String> getGameNames() {
        return gameMap.keySet();
    }

    @SuppressWarnings("unchecked")
    public static <M, P extends IPosition<M>> IGame<M, P> getGame(String gameName) {
        return (IGame<M, P>) gameMap.get(gameName).game;
    }

    @SuppressWarnings("unchecked")
    public static <M, P extends IPosition<M>> IGameRenderer<M, P> getGameRenderer(String gameName, IMouseTracker mouseTracker, IGameImageDrawer imageDrawer) {
        return (IGameRenderer<M, P>) gameMap.get(gameName).game.newGameRenderer(mouseTracker, imageDrawer);
    }

    @SuppressWarnings("unchecked")
    public static <M> MoveListFactory<M> getMoveListFactory(String gameName) {
        return (MoveListFactory<M>) gameMap.get(gameName).moveListFactory;
    }

    public static String[] getPlayerNames(String gameName) {
        return gameMap.get(gameName).playerNames.toArray(new String[0]);
    }

    public static String[] getPositionEvaluatorNames(String gameName) {
        return gameMap.get(gameName).positionEvaluators.keySet().toArray(new String[0]);
    }

    public static String[] getConstraintNames(String gameName) {
        return gameMap.get(gameName).positionWrappers.keySet().toArray(new String[0]);
    }

    public static PlayerOptions getPlayerOptions(String gameName, String playerName) {
        return gameMap.get(gameName).playerOptions.get(playerName);
    }

    @SuppressWarnings("unchecked")
    public static <M, P extends IPosition<M>> IPositionEvaluator<M, P> getPositionEvaluator(String gameName, String evaluatorName) {
        return (IPositionEvaluator<M, P>) gameMap.get(gameName).positionEvaluators.get(evaluatorName);
    }

    @SuppressWarnings("unchecked")
    public static <M, P extends IPosition<M>, W extends IPosition<M>> IPositionWrapper<M, P, W> getConstraint(String gameName, String constraintName) {
        return (IPositionWrapper<M, P, W>) gameMap.get(gameName).positionWrappers.get(constraintName);
    }

    public static class GameRegistryItem<M, P extends IPosition<M>> {
        final IGame<M, P> game;
        final MoveListFactory<M> moveListFactory;
        final List<String> playerNames = new ArrayList<>();
        final Map<String, PlayerOptions> playerOptions = new HashMap<>();
        final Map<String, IPositionEvaluator<M, P>> positionEvaluators = new LinkedHashMap<>();
        final Map<String, IPositionWrapper<M, P, ?>> positionWrappers = new LinkedHashMap<>();

        @SuppressWarnings("rawtypes")
        public GameRegistryItem(IGame<M, P> game, Class<? extends MoveList> analysisMoveListClass) {
            this.game = game;
            moveListFactory = new MoveListFactory<>(game.getMaxMoves(), analysisMoveListClass);
            playerNames.add(GuiPlayer.NAME);
        }

        public GameRegistryItem<M, P> removeHumanPlayer() {
            playerNames.remove(0);
            return this;
        }

        public GameRegistryItem<M, P> addPlayer(String name) {
            playerNames.add(name);
            return this;
        }

        public GameRegistryItem<M, P> addPositionEvaluator(String name, IPositionEvaluator<M, P> evaluator) {
            positionEvaluators.put(name, evaluator);
            return this;
        }

        public GameRegistryItem<M, P> addPositionWrapper(String name, IPositionWrapper<M, P, ?> wrapper) {
            positionWrappers.put(name, wrapper);
            return this;
        }

        public GameRegistryItem<M, P> setPlayerOptions(String name, PlayerOptions options) {
            playerOptions.put(name, options);
            return this;
        }

        public GameRegistryItem<M, P> createComputerPlayerOptions(int minMs, int maxMs, int maxThreads, int maxSimulations) {
            PlayerOptions msPerMoveOption = new PlayerOptions("time", new CPOptionIntRange(PlayerInfo.KEY_MS_PER_MOVE, minMs, maxMs));
            PlayerOptions threadOption = new PlayerOptions("threads", new CPOptionIntRange(PlayerInfo.KEY_NUM_THREADS, 1, maxThreads));
            PlayerOptions simulationsOption = new PlayerOptions("sims", new CPOptionIntRange(PlayerInfo.KEY_NUM_SIMULATIONS, 1, maxSimulations));
            String gameName = game.getName();
            PlayerOptions evaluatorOption = new PlayerOptions("Evaluator", new CPOptionStringArray(PlayerInfo.KEY_EVALUATOR,
                    GameRegistry.getPositionEvaluatorNames(gameName)));
            PlayerOptions constraintsOption = new PlayerOptions("Constraints",
                    new CPOptionStringArray(PlayerInfo.KEY_CONSTRAINTS, GameRegistry.getConstraintNames(gameName)));

            PlayerOptions fjStrategyOptions = new PlayerOptions("Strategy",
                    new CPOptionStringArray(PlayerInfo.KEY_FJ_STRATEGY, PlayerInfo.ALL_FJ_STRATEGIES));
            for (String fjStrategy : PlayerInfo.ALL_FJ_STRATEGIES) {
                fjStrategyOptions.addSubOption(fjStrategy, constraintsOption);
                fjStrategyOptions.addSubOption(fjStrategy, evaluatorOption);
                fjStrategyOptions.addSubOption(fjStrategy, msPerMoveOption);
                fjStrategyOptions.addSubOption(fjStrategy, threadOption);
            }

            PlayerOptions mcStrategyOptions = new PlayerOptions("Strategy",
                    new CPOptionStringArray(PlayerInfo.KEY_MC_STRATEGY, PlayerInfo.ALL_MC_STRATEGIES));
            for (String mcStrategy : PlayerInfo.ALL_MC_STRATEGIES) {
                mcStrategyOptions.addSubOption(mcStrategy, constraintsOption);
                mcStrategyOptions.addSubOption(mcStrategy, evaluatorOption);
                mcStrategyOptions.addSubOption(mcStrategy, msPerMoveOption);
                mcStrategyOptions.addSubOption(mcStrategy, simulationsOption);
            }

            setPlayerOptions(ComputerPlayer.NAME, new PlayerOptions("Search Type",
                    new CPOptionStringArray(PlayerInfo.KEY_ISTRATEGY, PlayerInfo.ALL_TREE_SEARCHERS))
                    .addSubOption(PlayerInfo.TS_FORK_JOIN, fjStrategyOptions)
                    .addSubOption(PlayerInfo.TS_MONTE_CARLO, mcStrategyOptions));

            return this;
        }
    }
}
