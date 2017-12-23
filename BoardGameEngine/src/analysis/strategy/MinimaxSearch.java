package analysis.strategy;

import analysis.AnalysisResult;
import game.IPosition;
import game.MoveList;
import game.MoveListFactory;

public class MinimaxSearch<M, P extends IPosition<M>> extends AbstractAlphaBetaSearch<M, P> {
	public MinimaxSearch(M parentMove, P position, MoveList<M> movesToSearch, MoveListFactory<M> moveListFactory, int plies, IDepthBasedStrategy<M, P> strategy) {
		super(parentMove, position, movesToSearch, moveListFactory, plies, strategy);
	}

	@Override
	protected MinimaxSearch<M, P> newSearch(M parentMove, P position, MoveList<M> movesToSearch, MoveListFactory<M> moveListFactory, int plies, IDepthBasedStrategy<M, P> strategy) {
		return new MinimaxSearch<>(parentMove, position, movesToSearch, moveListFactory, plies, strategy);
	}

	@Override
	protected AnalysisResult<M> searchNonForkable() {
		AnalysisResult<M> result = new AnalysisResult<>(parentMove, strategy.evaluate(position, plies));
		result.searchCompleted();
		return result;
	}

	@Override
	protected AnalysisResult<M> searchWithStrategy() {
		AnalysisResult<M> analysisResult = new AnalysisResult<>();
		do {
			M move = movesToSearch.get(branchIndex.get());
			position.makeMove(move);
			double evaluate = strategy.evaluate(position, plies - 1);
			double score = searchCanceled ? 0 : player == position.getCurrentPlayer() ? evaluate : -evaluate;
			position.unmakeMove(move);
			if (searchCanceled) { // we need to check search canceled after making the call to evaluate
				break;
			} else {
				analysisResult.addMoveWithScore(move, score);
			}
		} while (branchIndex.incrementAndGet() < movesToSearch.size());

		if (branchIndex.get() == movesToSearch.size()) {
			analysisResult.searchCompleted();
		}

		return analysisResult;
	}
}
