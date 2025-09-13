package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor color;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor inColor, ChessPiece.PieceType inType) {
        color = inColor;
        type = inType;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        switch (type) {
            case PieceType.KING   -> { return kingMoves(board,pos);   }
            case PieceType.QUEEN  -> { return queenMoves(board,pos);  }
            case PieceType.BISHOP -> { return bishopMoves(board,pos); }
            case PieceType.KNIGHT -> { return knightMoves(board,pos); }
            case PieceType.ROOK   -> { return rookMoves(board,pos);   }
            case PieceType.PAWN   -> { return pawnMoves(board,pos);   }
            default -> throw new RuntimeException("Moves for piece of type " + type + " are not known");
        }
    }

    /**
     * overrides for equality and hashcodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
    @Override
    public String toString() {
        return color.toString() + " " + type.toString();
    }

    /**
     * movesets for different pieces
     */
    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("King moves not implemented");
    }
    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("Queen moves not implemented");
    }
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("Bishop moves not implemented");
    }
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("Knight moves not implemented");
    }
    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("Rook moves not implemented");
    }
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("Pawn moves not implemented");
    }

    /**
     * utility for marching along path in any direction until colliding with another piece
     */
    interface IncrementFunction{
        ChessPosition step(ChessPosition before);
    }
    public Collection<ChessMove> moveMarch(ChessBoard board, ChessPosition posIn, IncrementFunction stepper) {
        // collect all moves we find along this march
        var moves = new HashSet<ChessMove>();
        // where are we looking now?
        ChessPosition pos = stepper.step(posIn);
        // keep stepping and adding moves until we step off the board
        while(pos.getRow()<=8 && pos.getRow()>=1 &&
              pos.getColumn()<=8 && pos.getColumn()>=1) {
            // did we run into another piece?
            if(true) { //TODO
                moves.add(new ChessMove(posIn, pos));
            } else {
                break;
            }
            pos = stepper.step(pos);
        }
        return moves;
    }
}
