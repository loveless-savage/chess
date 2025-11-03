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
    private final ChessPiece.PieceType type;
    private final MoveSet moveSet;

    public ChessPiece(ChessGame.TeamColor color, ChessPiece.PieceType type) {
        this.color = color;
        this.type = type;
        moveSet = new MoveSet();
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
        return moveSet.pieceMoves(board,pos);
    }

    /**
     * overrides for builtin functions
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
        return color + " " + type;
    }

////////////////////////////////////////////////////////////////////////////////
    /**
     * internal class that manages piece movement logic
     */
    private class MoveSet {
        private Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
            Collection<ChessMove> moves = new HashSet<>();
            switch (type) {
                case KING -> {
                    int[] dx = { 1, 1, 0,-1,-1,-1, 0, 1};
                    int[] dy = { 0, 1, 1, 1, 0,-1,-1,-1};
                    moves = batchMoves(board,pos,dx,dy,false);
                }
                case QUEEN -> {
                    int[] dx = { 1, 1, 0,-1,-1,-1, 0, 1};
                    int[] dy = { 0, 1, 1, 1, 0,-1,-1,-1};
                    moves = batchMoves(board,pos,dx,dy,true);
                }
                case BISHOP -> {
                    int[] dx = { 1,-1,-1, 1};
                    int[] dy = { 1, 1,-1,-1};
                    moves = batchMoves(board,pos,dx,dy,true);
                }
                case KNIGHT -> {
                    int[] dx = { 2, 1,-1,-2,-2,-1, 1, 2};
                    int[] dy = { 1, 2, 2, 1,-1,-2,-2,-1};
                    moves = batchMoves(board,pos,dx,dy,false);
                }
                case ROOK -> {
                    int[] dx = { 1, 0,-1, 0};
                    int[] dy = { 0, 1, 0,-1};
                    moves = batchMoves(board,pos,dx,dy,true);
                }
                case PAWN -> {
                    moves = pawnMoves(pos,board);
                }
                default -> {
                    System.out.println(type + " not a recognized piece type!");
                    return null;
                }
            }
            return moves;
        }

        /**
         * accumulate moves in an array of directions
         */
        private Collection<ChessMove> batchMoves(ChessBoard board, ChessPosition pos,
                                                    int[] dx, int[] dy, boolean doMarch) {
            var moves = new HashSet<ChessMove>();
            for (int i=0; i<dx.length; i++) {
                if (doMarch) {
                    moves.addAll(moveMarch(board,pos,dx[i],dy[i]));
                } else {
                    ChessPosition target = new ChessPosition(pos.getRow()+dx[i],pos.getColumn()+dy[i]);
                    if (!target.isInBounds()){
                        continue;
                    }
                    var collisionPiece = board.getPiece(target);
                    if(collisionPiece == null || color != collisionPiece.color) {
                        moves.add(new ChessMove(pos, target));
                    }
                }
            }
            return moves;
        }

        /**
         * march along a specified direction until hitting an obstacle
         */
        private Collection<ChessMove> moveMarch(ChessBoard board, ChessPosition pos, int dx, int dy) {
            var moves = new HashSet<ChessMove>();
            ChessPosition target = new ChessPosition(pos.getRow()+dx,pos.getColumn()+dy);
            while (target.isInBounds()) {
                var collisionPiece = board.getPiece(target);
                if(collisionPiece == null) {
                    moves.add(new ChessMove(pos, target));
                } else if (color != collisionPiece.color) {
                    moves.add(new ChessMove(pos, target));
                    break;
                } else {
                    break;
                }
                target = new ChessPosition(target.getRow()+dx,target.getColumn()+dy);
            }
            return moves;
        }

        /**
         * carry out pawn moving rules, including double-moving at start, diagonal capturing, and promotion
         */
        private Collection<ChessMove> pawnMoves(ChessPosition pos, ChessBoard board) {
            var moves = new HashSet<ChessMove>();
            int dx = (color == ChessGame.TeamColor.WHITE) ? 1:-1;
            ChessPosition target = new ChessPosition(pos.getRow()+dx,pos.getColumn());
            if (!target.isInBounds()){
                return null;
            }
            var collisionPiece = board.getPiece(target);
            if (collisionPiece == null) {
                moves.addAll(pawnPromoter(pos, target));
                if ( (color == ChessGame.TeamColor.WHITE && pos.getRow() == 2) ||
                        (color == ChessGame.TeamColor.BLACK && pos.getRow() == 7)) {
                    target = new ChessPosition(pos.getRow()+2*dx,pos.getColumn());
                    collisionPiece = board.getPiece(target);
                    if(collisionPiece == null) {
                        moves.add(new ChessMove(pos, target));
                    }
                }
            }
            if (pos.getColumn()<8) {
                target = new ChessPosition(pos.getRow() + dx, pos.getColumn() + 1);
                collisionPiece = board.getPiece(target);
                if (collisionPiece != null && color != collisionPiece.color) {
                    moves.addAll(pawnPromoter(pos, target));
                }
            }
            if (pos.getColumn()>1) {
                target = new ChessPosition(pos.getRow() + dx, pos.getColumn() - 1);
                collisionPiece = board.getPiece(target);
                if (collisionPiece != null && color != collisionPiece.color) {
                    moves.addAll(pawnPromoter(pos, target));
                }
            }
            return moves;
        }

        /**
         * convert a single pawn move into an array, each with a different promotion piece
         */
        private Collection<ChessMove> pawnPromoter(ChessPosition pos, ChessPosition target) {
            var moves = new HashSet<ChessMove>();
            if ( (color == ChessGame.TeamColor.WHITE && target.getRow() == 8) ||
                 (color == ChessGame.TeamColor.BLACK && target.getRow() == 1)) {
                for (var promo : PieceType.values()) {
                    if (promo == PieceType.KING || promo == PieceType.PAWN) {
                        continue;
                    }
                    moves.add(new ChessMove(pos, target, promo));
                }
            } else {
                moves.add(new ChessMove(pos, target));
            }
            return moves;
        }
    }
}
