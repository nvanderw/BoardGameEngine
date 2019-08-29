package bge.analysis.strategy;

import bge.analysis.AnalysisResult;
import bge.game.IPosition;

public interface IAlphaBetaStrategy<M, P extends IPosition<M>> extends IDepthBasedStrategy<M, P> {
    @Override
    default double evaluate(P position, int plies) {
        return evaluate(position, plies, AnalysisResult.LOSS, AnalysisResult.WIN);
    }

    public double evaluate(P position, int plies, double alpha, double beta);
}