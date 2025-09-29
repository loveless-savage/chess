import chess.*;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        //System.out.println("♕ 240 Chess Client: " + piece);
        var pos = new ChessPosition(5,5);
        var target = new ChessPosition(7,5);

        var board = new ChessBoard();
        board.resetBoard();
        board.addPiece(target,null);
        var kingPos = board.getKing(ChessGame.TeamColor.BLACK);
        System.out.println(board);
        var moves = piece.pieceMoves(board,pos);
        System.out.println(moves.stream().anyMatch(move -> move.getEndPosition().equals(kingPos)));

        ChessGame game = new ChessGame();
        System.out.println(game);
    }
}