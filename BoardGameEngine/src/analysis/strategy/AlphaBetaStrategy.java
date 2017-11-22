package analysis.strategy;

import java.util.List;

import analysis.AnalysisResult;
import analysis.IPositionEvaluator;
import game.IPosition;

public class AlphaBetaStrategy<M, P extends IPosition<M, P>> extends AbstractDepthBasedStrategy<M, P> {
	private boolean searchedAllPositions = true;

	private final IPositionEvaluator<M, P> positionEvaluator;

	public AlphaBetaStrategy(IPositionEvaluator<M, P> positionEvaluator) {
		this.positionEvaluator = positionEvaluator;
	}

	@Override
	public double evaluate(P position, int player, int plies) {
		searchedAllPositions = true;
		return alphaBeta(position, player, plies, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	private double alphaBeta(P position, int player, int plies, double alpha, double beta) {
		if (searchCanceled) {
			return 0;
		}

		if (plies == 0) {
			searchedAllPositions = false;
			return positionEvaluator.evaluate(position, player);
		}

		List<M> possibleMoves = position.getPossibleMoves();
		int numMoves = possibleMoves.size();

		if (numMoves == 0) {
			return positionEvaluator.evaluate(position, player);
		}

		boolean max = player == position.getCurrentPlayer();

		M move;
		double bestScore;
		int i = 0;
		if (max) { // Max
			bestScore = Double.NEGATIVE_INFINITY;
			do {
				move = possibleMoves.get(i);
				position.makeMove(move);
				double score = alphaBeta(position, player, plies - 1, alpha, beta);
				position.unmakeMove(move);

				if (score > bestScore) {
					bestScore = score;
					if (bestScore > alpha) {
						alpha = bestScore;
						if (beta <= alpha) {
							break;
						}
					}
				}

				++i;
			} while (i < numMoves);
		} else { // Min
			bestScore = Double.POSITIVE_INFINITY;
			do {
				move = possibleMoves.get(i);
				position.makeMove(move);
				double score = alphaBeta(position, player, plies - 1, alpha, beta);
				position.unmakeMove(move);

				if (score < bestScore) {
					bestScore = score;
					if (bestScore < beta) {
						beta = bestScore;
						if (beta <= alpha) {
							break;
						}
					}
				}

				++i;
			} while (i < numMoves);
		}

		return bestScore;
	}

	@Override
	public boolean searchedAllPositions() {
		return searchedAllPositions;
	}

	@Override
	public void notifyPlyStarted(AnalysisResult<M> lastResult) {
		// TODO Auto-generated method stub
	}

	@Override
	public IDepthBasedStrategy<M, P> createCopy() {
		return new AlphaBetaStrategy<>(positionEvaluator);
	}
}
