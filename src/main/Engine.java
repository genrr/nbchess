package main;

/**
 * High-level engine abstraction class. Runs in a new Thread, receives InterruptedException
 *  when asked to generate new move.
 */

public class Engine extends Thread {

	Piece[][] position = null;
	int turnNumber = 0;
	boolean turn = false;
	private static boolean alreadyStarted = true;
	
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
		
		//run once
		if(alreadyStarted) {
			GameLogic.InitData();
			GameLogic.InitSP();
			alreadyStarted = false;
		}
		
		
		while(Board.getGameRunning()) {
			try {
				System.out.println(Thread.currentThread());
				System.out.println("running..");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("called!");
				generateMove(position,turnNumber,turn);
			}
			
		}
	}

	private void generateMove(Piece[][] board, int turn, boolean botColor) {
		move = GameLogic.Generate(board,turn,botColor);
	}
	
}
