package analysis.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import analysis.AnalysisResult;
import analysis.search.GameTreeSearch;
import analysis.search.GameTreeSearchJoin;
import analysis.search.IGameTreeSearchJoin;
import game.IPosition;
import game.MoveList;
import game.MoveListFactory;

public abstract class AbstractAlphaBetaSearch<M, P extends IPosition<M>> implements IForkable<M, P> {
	protected final M parentMove;
	protected final P position;
	protected final int player;
	protected final MoveListFactory<M> moveListFactory;
	protected final MoveList<M> movesToSearch;
	protected final AtomicInteger branchIndex;
	protected final int plies;

	protected final IAlphaBetaStrategy<M, P> strategy;

	protected boolean searchCanceled = false;

	@SuppressWarnings("unchecked")
	public AbstractAlphaBetaSearch(M parentMove, P position, MoveList<M> movesToSearch, MoveListFactory<M> moveListFactory, int plies, IDepthBasedStrategy<M, P> strategy) {
		this.parentMove = parentMove;
		this.position = (P) position.createCopy();
		this.player = this.position.getCurrentPlayer();
		this.movesToSearch = movesToSearch;
		branchIndex = new AtomicInteger(0);
		this.moveListFactory = moveListFactory;
		this.plies = plies;
		this.strategy = (IAlphaBetaStrategy<M, P>) strategy.createCopy();
	}

	protected abstract AbstractAlphaBetaSearch<M, P> newSearch(M parentMove, P position, MoveList<M> movesToSearch, MoveListFactory<M> moveListFactory, int plies, IDepthBasedStrategy<M, P> strategy);

	@Override
	public AnalysisResult<M> search() {
		return isForkable() ? searchWithStrategy() : searchNonForkable();
	}

	protected abstract AnalysisResult<M> searchNonForkable();

	protected abstract AnalysisResult<M> searchWithStrategy();

	@Override
	public void stopSearch() {
		searchCanceled = true;
		strategy.stopSearch();
	}

	@Override
	public int getPlayer() {
		return player;
	}

	@Override
	public M getParentMove() {
		return parentMove;
	}

	@Override
	public int getPlies() {
		return plies;
	}

	@Override
	public int getRemainingBranches() {
		return movesToSearch.size() - branchIndex.get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GameTreeSearch<M, P>> fork(IGameTreeSearchJoin<M> parentJoin, AnalysisResult<M> partialResult) {
		if (partialResult == null) {
			partialResult = new AnalysisResult<>();
		}

		MoveList<M> unanalyzedMoves = movesToSearch.subList(partialResult.getMovesWithScore().size());
		int expectedResults = unanalyzedMoves.size();

		if (strategy instanceof ForkJoinObserver<?>) {
			((ForkJoinObserver<M>) strategy).notifyForked(parentMove, unanalyzedMoves);
		}

		List<GameTreeSearch<M, P>> gameTreeSearches = new ArrayList<>();
		GameTreeSearchJoin<M, P> forkJoin = new GameTreeSearchJoin<>(parentJoin, parentMove, position, player, strategy, partialResult, expectedResults);

		int i = 0;
		do {
			M move = unanalyzedMoves.get(i);
			position.makeMove(move);
			MoveList<M> subMoves = moveListFactory.newAnalysisMoveList();
			position.getPossibleMoves(subMoves);
			gameTreeSearches.add(new GameTreeSearch<>(newSearch(move, position, subMoves, moveListFactory, plies - 1, strategy), forkJoin));
			position.unmakeMove(move);
			++i;
		} while (i < unanalyzedMoves.size());

		return gameTreeSearches;
	}
}
