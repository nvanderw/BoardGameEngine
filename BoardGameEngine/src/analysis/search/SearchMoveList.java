package analysis.search;

import java.util.HashSet;
import java.util.Set;

import analysis.MoveWithScore;
import game.IPosition;
import game.MoveList;

public class SearchMoveList<M> implements MoveList<M> {
	private final MoveList<M> moveList;
	private final Set<M> decidedMoves;

	public SearchMoveList(MoveList<M> moveList, Set<MoveWithScore<M>> decidedMovesWithScores) {
		this.moveList = moveList;
		this.decidedMoves = new HashSet<>();
		for (MoveWithScore<M> moveWithScore : decidedMovesWithScores) {
			decidedMoves.add(moveWithScore.move);
		}
	}

	@Override
	public void addDynamicMove(M move, IPosition<M> position) {
		if (!decidedMoves.contains(move)) {
			moveList.addDynamicMove(move, position);
		}
	}

	@Override
	public void addQuietMove(M move, IPosition<M> position) {
		if (!decidedMoves.contains(move)) {
			moveList.addQuietMove(move, position);
		}
	}

	@Override
	public void addAllQuietMoves(M[] moves, IPosition<M> position) {
		moveList.addAllQuietMoves(moves, position);
	}

	@Override
	public M get(int index) {
		return moveList.get(index);
	}

	@Override
	public boolean contains(M move) {
		return moveList.contains(move);
	}

	@Override
	public int size() {
		return moveList.size();
	}

	@Override
	public int numDynamicMoves() {
		return moveList.numDynamicMoves();
	}

	@Override
	public MoveList<M> subList(int beginIndex) {
		return moveList.subList(beginIndex);
	}

	@Override
	public void clear() {
		moveList.clear();
	}
}
