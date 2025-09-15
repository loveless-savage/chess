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

    private final ChessGame.TeamColor color;
    private final PieceType type;

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
        var moves = new HashSet<ChessMove>();
        // which directions can the king step?
        int[] dx = {1, 1, 1, 0,-1,-1,-1, 0};
        int[] dy = {1, 0,-1,-1,-1, 0, 1, 1};
        // look at each possibility in turn
        for(int i=0;i<8;i++){
            ChessPosition newPos = new ChessPosition(pos.getRow()+dx[i],pos.getColumn()+dy[i]);
            // skip this position if is off the board
            if(newPos.getRow()>8 || newPos.getRow()<1 ||
                    newPos.getColumn()>8 || newPos.getColumn()<1) continue;
            // no friendly fire
            if(board.getPiece(newPos) != null && color == board.getPiece(newPos).color) continue;
            moves.add(new ChessMove(pos,newPos));
        }
        return moves;
    }
    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // march in each direction, accumulating end positions as far as we can go
        moves.addAll(moveMarch(board,pos, 1, 0)); // right
        moves.addAll(moveMarch(board,pos, 1, 1)); // up + right
        moves.addAll(moveMarch(board,pos, 0, 1)); // up
        moves.addAll(moveMarch(board,pos,-1, 1)); // up + left
        moves.addAll(moveMarch(board,pos,-1, 0)); // left
        moves.addAll(moveMarch(board,pos,-1,-1)); // down + left
        moves.addAll(moveMarch(board,pos, 0,-1)); // down
        moves.addAll(moveMarch(board,pos, 1,-1)); // down + right
        return moves;
    }
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // march in each direction, accumulating end positions as far as we can go
        moves.addAll(moveMarch(board,pos, 1, 1)); // up + right
        moves.addAll(moveMarch(board,pos,-1, 1)); // up + left
        moves.addAll(moveMarch(board,pos, 1,-1)); // down + right
        moves.addAll(moveMarch(board,pos,-1,-1)); // down + left
        return moves;
    }
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // which directions can the knight move relative to its current spot?
        int[] dx = {2, 2, 1,-1,-2,-2,-1, 1};
        int[] dy = {1,-1,-2,-2,-1, 1, 2, 2};
        // look at each possibility in turn
        for(int i=0;i<8;i++){
            ChessPosition newPos = new ChessPosition(pos.getRow()+dx[i],pos.getColumn()+dy[i]);
            // skip this position if is off the board
            if(newPos.getRow()>8 || newPos.getRow()<1 ||
                    newPos.getColumn()>8 || newPos.getColumn()<1) continue;
            // no friendly fire
            if(board.getPiece(newPos) != null && color == board.getPiece(newPos).color) continue;
            moves.add(new ChessMove(pos,newPos));
        }
        return moves;
    }
    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // march in each direction, accumulating end positions as far as we can go
        moves.addAll(moveMarch(board,pos, 1, 0)); // right
        moves.addAll(moveMarch(board,pos,-1, 0)); // left
        moves.addAll(moveMarch(board,pos, 0, 1)); // up
        moves.addAll(moveMarch(board,pos, 0,-1)); // down
        return moves;
    }
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition pos) {
        var moves = new HashSet<ChessMove>();
        // white pawns march up the board, black pawns march down
        int dx = (color == ChessGame.TeamColor.WHITE)? 1 : -1;
        ChessPosition newPos = new ChessPosition(pos.getRow()+dx,pos.getColumn());
        // is the path forward blocked? pawns cannot capture forward
        if(board.getPiece(newPos) == null) {
            moves.addAll(pawnPromotions(pos,newPos));
            // if the pawn is in its original row, it can also move two spaces
            if((color == ChessGame.TeamColor.WHITE && pos.getRow() == 2) ||
                    (color == ChessGame.TeamColor.BLACK && pos.getRow() == 7)) {
                newPos = new ChessPosition(pos.getRow()+2*dx,pos.getColumn());
                if(board.getPiece(newPos) == null) moves.add(new ChessMove(pos, newPos));
            }
        }
        // even if the path forward is blocked, the pawn can capture diagonally
        newPos = new ChessPosition(pos.getRow()+dx,pos.getColumn()+1); // to the right
        if(board.getPiece(newPos) != null && color != board.getPiece(newPos).color) {
            moves.addAll(pawnPromotions(pos,newPos));
        }
        newPos = new ChessPosition(pos.getRow()+dx,pos.getColumn()-1); // to the left
        if(board.getPiece(newPos) != null && color != board.getPiece(newPos).color) {
            moves.addAll(pawnPromotions(pos,newPos));
        }

        return moves;
    }

    /**
     * utility for marching along path in any direction until colliding with another piece
     */
    private Collection<ChessMove> moveMarch(ChessBoard board, ChessPosition posIn, int dx, int dy) {
        // collect all moves we find along this march
        var moves = new HashSet<ChessMove>();
        // where are we looking now?
        ChessPosition pos = new ChessPosition(posIn.getRow() + dx, posIn.getColumn() + dy);
        // keep stepping and adding moves until we step off the board
        while(pos.getRow()<=8 && pos.getRow()>=1 &&
                pos.getColumn()<=8 && pos.getColumn()>=1) {
            // did we run into another piece?
            ChessPiece collisionpiece = board.getPiece(pos);
            if(collisionpiece == null) { // no collision yet
                moves.add(new ChessMove(posIn, pos));
            } else if(color != collisionpiece.color){ // enemy team?
                moves.add(new ChessMove(posIn, pos));
                break;
            } else { // abort when we collide with our own team
                break;
            }
            pos = new ChessPosition(pos.getRow() + dx, pos.getColumn() + dy);
        }
        return moves;
    }

    /**
     * utility for expanding a pawn move into all possible promotions if it reaches the other end of the board
     */
    private Collection<ChessMove> pawnPromotions(ChessPosition pos, ChessPosition newPos) {
        var moves = new HashSet<ChessMove>();
        if((color == ChessGame.TeamColor.WHITE && newPos.getRow() == 8) ||
                (color == ChessGame.TeamColor.BLACK && newPos.getRow() == 1)) {
            for (var i : PieceType.values()) {
                // can't promote to a pawn or a king
                if(i == PieceType.PAWN || i == PieceType.KING) continue;
                moves.add(new ChessMove(pos,newPos,i));
            }
        } else { // false alarm! don't promote
            moves.add(new ChessMove(pos, newPos));
        }
        return moves;
    }
}
