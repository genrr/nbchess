package main;

public class Engine extends Thread {

	Piece[][] position = null;
	int turnNumber = 0;
	boolean turn = false;
	
	int[] move = null;
	
	public void setState(Piece[][] p, int turnNumber, boolean turn) {
		position = p;
		this.turnNumber = turnNumber;
		this.turn = turn;
	}
	
	public int[] getMove() {
		return move;
	}
	
	
	public void run() {
		
		while(Board.getGameRunning()) {
			try {
				System.out.println("running..");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("called!");
				generateMove(position,turnNumber,turn);
			}
			
		}
	}

	private void generateMove(Piece[][] board, int turn, boolean botColor) {
		move = GameLogic.generate(board,turn,botColor);
	}
	
}
