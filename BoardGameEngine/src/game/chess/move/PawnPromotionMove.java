package game.chess.move;

import game.Coordinate;
import game.chess.ChessFunctions;
import game.chess.ChessPosition;

public class PawnPromotionMove implements IChessMove {
	private final BasicChessMove basicMove;
	public final int promotion;
	private final int pawn;

	public PawnPromotionMove(BasicChessMove basicMove, int promotion, int pawn) {
		this.basicMove = basicMove;
		this.promotion = promotion;
		this.pawn = pawn;
	}

	@Override
	public void applyMove(ChessPosition position, boolean changeState) {
		position.squares[basicMove.to.y][basicMove.to.x] = promotion;
		position.squares[basicMove.from.y][basicMove.from.x] = UNPLAYED;
		if (changeState) {
			position.materialScore[position.otherPlayer] = position.materialScore[position.otherPlayer] - ChessFunctions.getPieceScore(basicMove.pieceCaptured);
			position.materialScore[position.currentPlayer] = position.materialScore[position.currentPlayer] + ChessFunctions.getPieceScore(promotion) - PAWN_SCORE;
		}
	}

	@Override
	public void unapplyMove(ChessPosition position, boolean changeState) {
		position.squares[basicMove.from.y][basicMove.from.x] = pawn;
		position.squares[basicMove.to.y][basicMove.to.x] = basicMove.pieceCaptured;
		if (changeState) {
			position.materialScore[position.otherPlayer] = position.materialScore[position.otherPlayer] + ChessFunctions.getPieceScore(basicMove.pieceCaptured);
			position.materialScore[position.currentPlayer] = position.materialScore[position.currentPlayer] - ChessFunctions.getPieceScore(promotion) + PAWN_SCORE;
		}
	}

	@Override
	public Coordinate getEnPassantSquare() {
		return basicMove.enPassantSquare;
	}

	@Override
	public int getPieceCaptured() {
		return basicMove.pieceCaptured;
	}

	@Override
	public Coordinate getFrom() {
		return basicMove.from;
	}

	@Override
	public Coordinate getTo() {
		return basicMove.to;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		return prime * (prime + basicMove.hashCode()) + promotion;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PawnPromotionMove other = (PawnPromotionMove) obj;
		return basicMove.equals(other.basicMove) && promotion == other.promotion;
	}

	@Override
	public String toString() {
		return basicMove.toString();
	}
}