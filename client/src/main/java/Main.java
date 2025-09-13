import chess.*;

public class Main {
    public static void main(String[] args) {
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        //System.out.println("♕ 240 Chess Client: " + piece);
        var board = new ChessBoard();
        board.resetBoard();
        System.out.println(board);

        /*
        board.loadBoard("""
                | |n|b|q|k|b|n|r|
                |p|p|p|p|p|p|p|p|
                | | | | | | | | |
                | |r| | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                |P|P|P|P|P|P|P|P|
                |R|N|B|Q|K|B|N|R|
                """);
        var pos = new ChessPosition(5,2);
        var piece = board.getPiece(pos);
        System.out.println(piece.toString());
        var place = piece.pieceMoves(board,pos);
        place.forEach((p) -> System.out.println(p));
        */
    }
}