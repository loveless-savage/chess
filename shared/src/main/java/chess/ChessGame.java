package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor turn;
    private boolean isOver = false;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param nextTurn the team whose turn it is
     */
    public void setTeamTurn(TeamColor nextTurn) {
        turn = nextTurn;
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver() {
        isOver = true;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = new HashSet<>();
        if(piece == null || isOver) {
            return moves;
        }
        // test each potential move to see if it endangers this piece's own king
        for(var move : piece.pieceMoves(board,startPosition)){
            // temporarily make move
            ChessPiece realEndPosState = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(),piece);
            board.addPiece(move.getStartPosition(),null);
            if(!isInCheck(piece.getTeamColor())) {
                moves.add(move);
            }
            // restore actual state of the board
            board.addPiece(move.getEndPosition(),realEndPosState);
            board.addPiece(move.getStartPosition(),piece);
        }
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(isOver) {
            throw new InvalidMoveException("This game is over, no pieces can be moved");
        }
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if(piece == null) { // is there a piece to move?
            throw new InvalidMoveException("No piece at "+move.getStartPosition());
        } else if(piece.getTeamColor() != turn) { // is it that piece's turn?
            throw new InvalidMoveException("Not "+piece.getTeamColor()+"'s turn!");
        } else if(!validMoves(move.getStartPosition()).contains(move)) { // is this move valid?
            throw new InvalidMoveException(piece+" cannot move to "+move.getEndPosition()+"!");
        }
        // promote pawn if applicable
        if(move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        // move piece
        board.addPiece(move.getEndPosition(),piece);
        board.addPiece(move.getStartPosition(),null);
        // switch turns
        setTeamTurn(turn == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK);
        // check for checkmate + stalemate
        if(isInCheckmate(turn) || isInStalemate(turn)) {
            isOver = true;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor enemyColor = (teamColor == TeamColor.BLACK) ? TeamColor.WHITE : TeamColor.BLACK;
        // for each piece on the enemy team, check its possible moveset
        TeamTestFunction checkTest = (pos, piece) -> {
            var moves = piece.pieceMoves(board,pos);
            // do any of this piece's moves land on our king?
            return moves.stream().anyMatch(
                    move -> move.getEndPosition().equals(board.getKing(teamColor))
            );
        };
        // if any of the enemy team's pieces can reach the king, return true
        return forAnyOnTeam(enemyColor,checkTest);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // we must be in check in order to be in checkmate
        if(!isInCheck(teamColor)) {
            return false;
        }
        // for each piece on teamColor, check whether it has any valid moves
        TeamTestFunction checkmateTest = (pos, piece) ->
                !validMoves(pos).isEmpty();
        // if any of our team's pieces have valid moves, return false
        return !forAnyOnTeam(teamColor,checkmateTest);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // if in check, abort
        if(isInCheck(teamColor)) {
            return false;
        }
        // for each piece on teamColor, check whether it has any valid moves
        TeamTestFunction stalemateTest = (pos, piece) ->
                !validMoves(pos).isEmpty();
        // if any of our team's pieces have valid moves, return false
        return !forAnyOnTeam(teamColor,stalemateTest);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * overrides
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame that = (ChessGame) o;
        return Objects.equals(board, that.board) && turn == that.turn;
    }
    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }
    @Override
    public String toString() {
        String gamestate = turn==TeamColor.WHITE ? "WHITE" : "BLACK ";
        return "   turn: " + gamestate + "   \n" + board;
    }

    /**
     * helper function: run a test for each piece of the specified team
     * e.g, see if moving this piece will free the king from check
     */
    private interface TeamTestFunction {
        boolean run(ChessPosition pos, ChessPiece piece);
    }
    private boolean forAnyOnTeam(TeamColor activeColor, TeamTestFunction test) {
        for(int row=1; row<=8; row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition pos = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(pos);
                if(piece != null && // is there a piece to move?
                        piece.getTeamColor() == activeColor) { // disregard friendly fire
                    if(test.run(pos,piece)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
