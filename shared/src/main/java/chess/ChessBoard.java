package chess;

import java.util.Map;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] pieces;

    public ChessBoard() {
        pieces = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param pos where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition pos, ChessPiece piece) {
        if(pos.getRow()>8 || pos.getRow()<1 ||
                pos.getColumn()>8 || pos.getColumn()<1) {
            throw new RuntimeException("Cannot add piece: position "+pos+" out of bounds");
        }
        pieces[pos.getRow()-1][pos.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param pos The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition pos) {
        if(pos.getRow()>8 || pos.getRow()<1 ||
                pos.getColumn()>8 || pos.getColumn()<1) return null;

        return pieces[pos.getRow()-1][pos.getColumn()-1];
    }

    /**
     * Locates the king of given color
     *
     * @param team Which king are we looking for?
     * @return position of the king
     */
    public ChessPosition getKing(ChessGame.TeamColor team) {
        for(int row=1; row<=8; row++) {
            for (int col=1; col<=8; col++) {
                ChessPiece piece = pieces[row-1][col-1];
                if(piece != null && // is there a piece here?
                        piece.getPieceType() == ChessPiece.PieceType.KING && // is it a king?
                        piece.getTeamColor() == team){ // is it on the team we're asking for?
                    return new ChessPosition(row,col);
                }
            }
        }
        return null;
    }

    /**
     * Sets the board to the default starting board
     */
    public void resetBoard() {
        //pieces.clear();
        loadBoard("""
                |r|n|b|q|k|b|n|r|
                |p|p|p|p|p|p|p|p|
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                |P|P|P|P|P|P|P|P|
                |R|N|B|Q|K|B|N|R|
                """);
    }

    /**
     * overrides
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return toString().equals(that.toString());
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public String toString() {
        return printBoard();
    }

////////////////////////////////////////////////////////////////////////////////

    /**
     * mappings between ascii representation of pieces to object representation
     */
    private static final Map<Character, ChessPiece.PieceType> CHAR_TO_TYPE_MAP = Map.of(
            'p', ChessPiece.PieceType.PAWN,
            'n', ChessPiece.PieceType.KNIGHT,
            'r', ChessPiece.PieceType.ROOK,
            'q', ChessPiece.PieceType.QUEEN,
            'k', ChessPiece.PieceType.KING,
            'b', ChessPiece.PieceType.BISHOP);
    private static final Map<ChessPiece.PieceType, Character> TYPE_TO_CHAR_MAP = Map.of(
            ChessPiece.PieceType.PAWN, 'p',
            ChessPiece.PieceType.KNIGHT, 'n',
            ChessPiece.PieceType.ROOK, 'r',
            ChessPiece.PieceType.QUEEN, 'q',
            ChessPiece.PieceType.KING, 'k',
            ChessPiece.PieceType.BISHOP, 'b');

    /**
     * convert ascii representation of board to object representation
     */
    private void loadBoard(String boardText) {
        char[] boardIn = boardText.toCharArray();
        int row = 8;
        int col = 1;
        for (var c : boardIn) {
            if (c == '|') continue; // skip delimiters
            if (c == '\n') { // new row
                row--;
                col = 1;
                continue;
            }
            if (c == ' ') { // empty spaces
                pieces[row-1][col-1] = null;
                continue;
            }

            ChessGame.TeamColor color = Character.isLowerCase(c) ?
                    ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            var type = CHAR_TO_TYPE_MAP.get(Character.toLowerCase(c));

            if(pieces[row-1][col-1] == null || // always write to null spaces
                    type != pieces[row-1][col-1].getPieceType() || // if piece type and color both match,
                    color != pieces[row-1][col-1].getTeamColor()) { // we can leave the piece unchanged
                pieces[row-1][col-1] = new ChessPiece(color,type);
            }
            col++;
        }
    }

    /**
     * convert object representation of board to ascii representation
     */
    private String printBoard() {
        char[] boardText = new char[144];
        int i,j;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                boardText[18*i+2*j] = '|';

                chess.ChessPiece currentPiece = pieces[7-i][j];
                if(currentPiece == null){ // blank space
                    boardText[18*i+2*j+1] = ' ';
                }else{ // non-empty character
                    char type = TYPE_TO_CHAR_MAP.get(currentPiece.getPieceType());
                    if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) type = Character.toUpperCase(type);
                    boardText[18*i+2*j+1] = type;
                }
            }
            boardText[18*i+16] = '|';
            boardText[18*i+17] = '\n';
        }
        return new String(boardText);
    }
}
