package main;

public class Message {
	
	private Piece[][] pos;
	private int turnNo;
	private String status;
	private int[] move;
	private int[] piecesInfo;
	private Piece[][][] lines;
	
	public Message(Piece[][] pos,int[] move, int turnNo, int[] piecesInfo, String status) {
		this.pos = pos;
		this.move = move;
		this.turnNo = turnNo;
		this.status = status;
		this.piecesInfo = piecesInfo;
	}
	
	public Message(Piece[][][] lineStack, String status) {
		lines = lineStack;
		this.status = status;
	}

	
	public Piece[][] getBoardData() {
		return pos;
	}
	
	public int[] getMove() {
		return move;
	}
	
	public String getStatus() {
		return status;
	}
	
	public int getTurnNumber() {
		return turnNo;
	}
	
	public int[] getPiecesInfo() {
		return piecesInfo;
	}
	
	public Piece[][][] getLines(){
		return lines;
	}
}
