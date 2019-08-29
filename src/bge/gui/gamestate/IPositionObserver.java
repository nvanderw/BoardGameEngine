package bge.gui.gamestate;

import bge.game.IPosition;
import bge.game.MoveList;

public interface IPositionObserver<M, P extends IPosition<M>> {
    public void notifyPositionChanged(P position, MoveList<M> possibleMoves);
}