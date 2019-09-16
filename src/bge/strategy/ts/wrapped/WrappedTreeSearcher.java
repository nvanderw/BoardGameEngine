package bge.strategy.ts.wrapped;

import java.util.Collections;

import bge.analysis.AnalysisResult;
import bge.analysis.PartialResultObservable;
import bge.analysis.StrategyResult;
import bge.igame.IPosition;
import bge.strategy.ts.ITreeSearcher;

public class WrappedTreeSearcher<M, P extends IPosition<M>, W extends IPosition<M>> implements ITreeSearcher<M, P>, PartialResultObservable {
    private final ITreeSearcher<M, W> treeSearcher;
    private final IPositionWrapper<M, P, W> wrapper;

    public WrappedTreeSearcher(ITreeSearcher<M, W> treeSearcher, IPositionWrapper<M, P, W> wrapper) {
        this.treeSearcher = treeSearcher;
        this.wrapper = wrapper;
    }

    @Override
    public void searchForever(P position, boolean escapeEarly) {
        treeSearcher.searchForever(wrapper.wrap(position), escapeEarly);
    }

    @Override
    public boolean isSearching() {
        return treeSearcher.isSearching();
    }

    @Override
    public void stopSearch(boolean gameOver) {
        treeSearcher.stopSearch(gameOver);
    }

    @Override
    public AnalysisResult<M> getResult() {
        return treeSearcher.getResult();
    }

    @Override
    public StrategyResult getPartialResult() {
        if (treeSearcher instanceof PartialResultObservable) {
            PartialResultObservable pro = (PartialResultObservable) treeSearcher;
            return pro.getPartialResult();
        }
        else {
            return new StrategyResult((AnalysisResult<Object>) treeSearcher.getResult(), Collections.emptyList(), 0);
        }
    }
}
