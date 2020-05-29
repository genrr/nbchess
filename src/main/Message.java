package main;

public class Message {
	
	private Piece[][] pos;
	private String status;
	private int[] move;
	
	public Message(Piece[][] pos,int[] move, String status) {
		this.pos = pos;
		this.move = move;
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
}
