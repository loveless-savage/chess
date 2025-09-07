package chess;

import java.util.Map;
import java.util.HashMap;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private Map<ChessPosition,ChessPiece> pieces;

    public ChessBoard() {
        pieces = new HashMap<>();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        pieces.put(position,piece);
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return pieces.get(position);
    }

    /**
     * Sets the board to the default starting board
     */
    public void resetBoard() {
        pieces.clear();
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
     * convert ascii representation of board to object representation
     */
    private static final Map<Character, ChessPiece.PieceType> CHAR_TO_TYPE_MAP = Map.of(
            'p', ChessPiece.PieceType.PAWN,
            'n', ChessPiece.PieceType.KNIGHT,
            'r', ChessPiece.PieceType.ROOK,
            'q', ChessPiece.PieceType.QUEEN,
            'k', ChessPiece.PieceType.KING,
            'b', ChessPiece.PieceType.BISHOP);
    private void loadBoard(String boardText) {
        Map<ChessPosition, ChessPiece> piecesTemp = new HashMap<>();
        int row = 8;
        int column = 1;
        for (var c : boardText.toCharArray()) {
            switch (c) {
                case '\n' -> {
                    column = 1;
                    row--;
                }
                case ' ' -> column++;
                case '|' -> {
                }
                default -> {
                    ChessGame.TeamColor color = Character.isLowerCase(c) ? ChessGame.TeamColor.BLACK
                            : ChessGame.TeamColor.WHITE;
                    var type = CHAR_TO_TYPE_MAP.get(Character.toLowerCase(c));
                    var position = new ChessPosition(row, column);
                    var piece = new ChessPiece(color, type);
                    piecesTemp.put(position, piece);
                    column++;
                }
            }
        }
        pieces = piecesTemp;
    }

    /**
     * convert object representation of board to ascii representation
     */
    private static final Map<ChessPiece.PieceType, Character> TYPE_TO_CHAR_MAP = Map.of(
            ChessPiece.PieceType.PAWN, 'p',
            ChessPiece.PieceType.KNIGHT, 'n',
            ChessPiece.PieceType.ROOK, 'r',
            ChessPiece.PieceType.QUEEN, 'q',
            ChessPiece.PieceType.KING, 'k',
            ChessPiece.PieceType.BISHOP, 'b');
    public String printBoard() {
        char[] boardText = new char[144];
        int i,j;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                boardText[18*i+2*j] = '|';
                boardText[18*i+2*j+1] = ' ';
            }
            boardText[18*i+16] = '|';
            boardText[18*i+17] = '\n';
        }
        // fill in each piece
        for(var pos : pieces.keySet()) {
            ChessPiece currentPiece = pieces.get(pos);
            char pieceT = TYPE_TO_CHAR_MAP.get(currentPiece.getPieceType());
            if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE){
                pieceT = Character.toUpperCase(pieceT);
            }
            i = 8-pos.getRow();
            j =-1+pos.getColumn();
            boardText[18*i+2*j+1] = pieceT;
        }
        return new String(boardText);
    }

    /**
     * overrides for equality and hashcodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return pieces.hashCode()==that.hashCode();
    }
    @Override
    public int hashCode() {
        return pieces.hashCode();
    }
}
