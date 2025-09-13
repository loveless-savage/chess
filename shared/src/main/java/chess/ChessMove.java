package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private ChessPosition startPosition, endPosition;
    private ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition inStartPosition, ChessPosition inEndPosition,
                     ChessPiece.PieceType inPromotionPiece) {
        startPosition = inStartPosition;
        endPosition = inEndPosition;
        promotionPiece = inPromotionPiece;
    }
    public ChessMove(ChessPosition inStartPosition, ChessPosition inEndPosition) {
        this(inStartPosition,inEndPosition,null);
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    /**
     * overrides for equality and hashcodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove that = (ChessMove) o;
        return startPosition.equals(that.startPosition) &&
                endPosition.equals(that.endPosition) &&
                promotionPiece.equals(that.promotionPiece);
        /*
        return Objects.equals(startPosition, chessMove.startPosition) &&
               Objects.equals(endPosition, chessMove.endPosition) &&
                promotionPiece == chessMove.promotionPiece;
         */
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }
}
