import chess.*;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        //System.out.println("♕ 240 Chess Client: " + piece);

        var board = new ChessBoard();
        board.resetBoard();
        System.out.println(board.printBoard());

        /*
        var piece2 = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        System.out.println("board init  = " + board);
        board.addPiece(new ChessPosition(1,2),piece);
        System.out.println("board 1     = " + board);
        board.addPiece(new ChessPosition(2,4),piece2);
        System.out.println("board 2     = " + board);
        board.resetBoard();
        System.out.println("board final = " + board);
        board.addPiece(new ChessPosition(2,4),piece2);
        board.addPiece(new ChessPosition(1,2),piece);
        System.out.println("board 2     = " + board);
        */
    }
}