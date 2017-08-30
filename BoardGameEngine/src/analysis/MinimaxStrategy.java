package analysis;

import java.util.List;

import analysis.search.MoveWithResult;
import game.IPosition;

public class MinimaxStrategy<M, P extends IPosition<M, P>> extends AbstractDepthBasedStrategy<M, P> {
	private final IPositionEvaluator<M, P> positionEvaluator;

	public MinimaxStrategy(IPositionEvaluator<M, P> positionEvaluator) {
		this.positionEvaluator = positionEvaluator;
	}

	@Override
	public double evaluate(P position, int player, int plies) {
		return minimax(position, player, plies);
	}

	private double minimax(P position, int player, int plies) {
		if (searchCanceled) {
			return 0;
		}

		if (plies == 0) {
			return positionEvaluator.evaluate(position, player);
		}

		List<M> possibleMoves = position.getPossibleMoves();

		if (possibleMoves.size() == 0) {
			return positionEvaluator.evaluate(position, player);
		}

		boolean max = player == position.getCurrentPlayer();

		double bestScore = max ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

		for (M move : possibleMoves) {
			if (searchCanceled) {
				return 0;
			}
			position.makeMove(move);
			double score = minimax(position, player, plies - 1);
			position.unmakeMove(move);

			if (max) {
				if (score > bestScore) {
					bestScore = score;
				}
			} else {
				if (score < bestScore) {
					bestScore = score;
				}
			}
		}

		return bestScore;
	}

	@Override
	public AnalysisResult<M> join(P position, int player, List<MoveWithScore<M>> movesWithScore, List<MoveWithResult<M>> movesWithResults) {
		AnalysisResult<M> joinedResult = new AnalysisResult<>(movesWithScore);
		boolean min = player == position.getCurrentPlayer();
		for (MoveWithResult<M> moveWithResult : movesWithResults) {
			if (min) {
				joinedResult.addMoveWithScore(moveWithResult.move, moveWithResult.result.getMin());
			} else {
				joinedResult.addMoveWithScore(moveWithResult.move, moveWithResult.result.getMax());
			}
		}
		return joinedResult;
	}

	@Override
	public IDepthBasedStrategy<M, P> createCopy() {
		return new MinimaxStrategy<>(positionEvaluator);
	}
}
