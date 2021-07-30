package main;

import java.util.ArrayList;

public class GameState {
	
	//change gamestate params
	
	public static void changeStateParams() {
		
	}

	/*
	public static int getStatePos(Piece[][] board, Piece p) {
		
	}
*/
	public static ArrayList<Integer> getStateRel1(Piece[][] board, Piece p) {
		boolean color = p.getColor();
		ArrayList<Piece> pieces = MGameUtility.ReturnAllPieces(board, color);
		ArrayList<Integer> rel1 = new ArrayList<>(pieces.size());
		
		int temp;
		
		
		for (Piece q : pieces) {
			temp = MGameUtility.distance(board, q, q.getX(), q.getY(), p.getX(), p.getY(), false);
			rel1.add(temp);
		}
		
		return rel1;
		
	}

	public static ArrayList<Integer> getStateRel2(Piece[][] board, Piece p) {
		boolean color = p.getColor();
		ArrayList<Piece> pieces = MGameUtility.ReturnAllPieces(board, !color);
		ArrayList<Integer> rel2 = new ArrayList<>(pieces.size());
		
		int temp;
		
		
		for (Piece q : pieces) {
			temp = MGameUtility.distance(board, q, q.getX(), q.getY(), p.getX(), p.getY(), false);
			rel2.add(temp);
		}
		
		return rel2;
	}
	
	/*
	 * {0,1,4,1,1,3,4}
	 * {0,0,0,5,4,3,2}
	 * 
	 * {0,1,4,1,1,3,4}
	 * 
	 * change 
	 * 
	 * 
	 * 
	 */
	
	
	

}
