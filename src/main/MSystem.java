package main;

public class MSystem {
	
	
	public static final int DIST_CALC_DEPTH = 8;

	
	/* RootSystemCall() / low level system function
	 * 
	 * Handles low-level representation and optimization
	 * 
	 * 
	 */
	
	public static void RootSystemCall() {
		
	}
	
	
	
	/*
	 * Main function in the program logic:
	 * 
	 * receives data from Engine(board)
	 * queries current filtered state from GameStateFunction()
	 * calls generate() on GameLogic and then receives the move to be played
	 *
	 * 
	 */
	
	public static int[] MainFunction(Piece[][] board,int turn, boolean white) {
		int[][][][][] t;
		int[] r;
		
		t = GameStateFunction();
		
		r = GameLogic.generate(board,turn,white,t);

		return r;
	}
	
	
	/* 
	 * Main system function #2
	 * 
	 * 
	 * handles current objectives
	 * 
	 * and ValueFunctions
	 * 
	 * records histogram of previous moves
	 * 
	 */
	
	public static int[][][][][] GameStateFunction() {
		
	}


	/*
	 * Accepts draw if not in winning position
	 * 
	 */

	public static boolean DrawDecision(Piece[][] board, int turn, boolean b) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
