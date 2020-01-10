package main;

import java.util.ArrayList;

public class GameLogic {
	
	
	

	
	/*
	 * Generates move, returns it as a vector: {startX,startY,targetX,targetY}
	 * 
	 * 
	 */
	
	public static int[] generate(Piece[][] board, int turnNumber, boolean white, int[][][][][] stateVector) {
		
		ArrayList<int[]> candidateMoves = new ArrayList<int[]>();
		
		//generate moves
		computeRealtimeFunctions(board, white, turnNumber, stateVector);
		
		//fill arraylist, if generated list has a % sum lower than certain %, post resign flag
		
		
		//each move is in vector format of size 5: {sX,sY,tX,tY,%}
		
		
		//sort arraylist according to % (ascending)

		
		int[] bestMove = new int[4];
		bestMove[0] = candidateMoves.get(0)[0];
		bestMove[1] = candidateMoves.get(0)[1];
		bestMove[2] = candidateMoves.get(0)[2];
		bestMove[3] = candidateMoves.get(0)[3];
		 
		
		return bestMove; 
		
	}
	
	
	private static void computeRealtimeFunctions(Piece[][] board, boolean white, int turnNumber, int[][][][][] stateVector) {
		
		PositionFeature.initFeatures(board, white, turnNumber);
		
		//RealtimeFunction.computeVsTime(PositionFeature.AllAvailableSquares(false),turnNumber);
		
		//WRITE states
		PositionFeature.writeAll();
		
		//READ states
		
		
	}
	
	
}
