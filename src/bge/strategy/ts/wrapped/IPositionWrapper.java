package bge.strategy.ts.wrapped;

import bge.igame.IPosition;

@FunctionalInterface
public interface IPositionWrapper<M, P extends IPosition<M>, W extends IPosition<M>> {
    W wrap(P position);
}
