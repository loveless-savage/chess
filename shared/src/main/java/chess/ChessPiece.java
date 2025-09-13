package chess;

import java.util.Collection;
import java.util.HashSet;
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
     * overrides
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

////////////////////////////////////////////////////////////////////////////////

    /**
     * movesets for different pieces
     */
    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("King moves not implemented");
    }
    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // define marching directions
        IncrementFunction stepup = (p) -> new ChessPosition( // up
                p.getRow()+1,p.getColumn());
        IncrementFunction stepdown = (p) -> new ChessPosition( // down
                p.getRow()-1,p.getColumn());
        IncrementFunction stepright = (p) -> new ChessPosition( // right
                p.getRow(),p.getColumn()+1);
        IncrementFunction stepleft = (p) -> new ChessPosition( // left
                p.getRow(),p.getColumn()-1);
        IncrementFunction stepur = (p) -> new ChessPosition( // up + right
                p.getRow()+1,p.getColumn()+1);
        IncrementFunction stepdr = (p) -> new ChessPosition( // down + right
                p.getRow()-1,p.getColumn()+1);
        IncrementFunction stepul = (p) -> new ChessPosition( // up + left
                p.getRow()+1,p.getColumn()-1);
        IncrementFunction stepdl = (p) -> new ChessPosition( // down + left
                p.getRow()-1,p.getColumn()-1);
        moves.addAll(moveMarch(board, pos, stepup));
        moves.addAll(moveMarch(board, pos, stepdown));
        moves.addAll(moveMarch(board, pos, stepright));
        moves.addAll(moveMarch(board, pos, stepleft));
        moves.addAll(moveMarch(board, pos, stepur));
        moves.addAll(moveMarch(board, pos, stepdr));
        moves.addAll(moveMarch(board, pos, stepul));
        moves.addAll(moveMarch(board, pos, stepdl));
        return moves;
    }
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // define marching directions
        IncrementFunction stepur = (p) -> new ChessPosition( // up + right
                p.getRow()+1,p.getColumn()+1);
        IncrementFunction stepdr = (p) -> new ChessPosition( // down + right
                p.getRow()-1,p.getColumn()+1);
        IncrementFunction stepul = (p) -> new ChessPosition( // up + left
                p.getRow()+1,p.getColumn()-1);
        IncrementFunction stepdl = (p) -> new ChessPosition( // down + left
                p.getRow()-1,p.getColumn()-1);
        moves.addAll(moveMarch(board, pos, stepur));
        moves.addAll(moveMarch(board, pos, stepdr));
        moves.addAll(moveMarch(board, pos, stepul));
        moves.addAll(moveMarch(board, pos, stepdl));
        return moves;
    }
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition pos) {
        throw new RuntimeException("Knight moves not implemented");
    }
    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // define marching directions
        IncrementFunction stepup = (p) -> new ChessPosition( // up
                p.getRow()+1,p.getColumn());
        IncrementFunction stepdown = (p) -> new ChessPosition( // down
                p.getRow()-1,p.getColumn());
        IncrementFunction stepright = (p) -> new ChessPosition( // right
                p.getRow(),p.getColumn()+1);
        IncrementFunction stepleft = (p) -> new ChessPosition( // left
                p.getRow(),p.getColumn()-1);
        moves.addAll(moveMarch(board, pos, stepup));
        moves.addAll(moveMarch(board, pos, stepdown));
        moves.addAll(moveMarch(board, pos, stepright));
        moves.addAll(moveMarch(board, pos, stepleft));
        return moves;
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
            ChessPiece collisionpiece = board.getPiece(pos);
            if(collisionpiece == null) { // no collision yet
                moves.add(new ChessMove(posIn, pos));
            } else if(getTeamColor() != collisionpiece.getTeamColor()){ // enemy team?
                moves.add(new ChessMove(posIn, pos));
                break;
            } else { // abort when we collide with our own team
                break;
            }
            pos = stepper.step(pos);
        }
        return moves;
    }
}
