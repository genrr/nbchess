package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Character {

	
	
	
	/*
	 * Get a BoardRep subtraction that approximates board Representation
	 * moving from eval to dir, individual h moves closer to eval
	 */
	
	public static ArrayList<double[][]> mine(int[][][] b, int white, int[] heuristicSet, double value, boolean rising, int maxDist) {
		boolean relation = false;
		int heuristic = heuristicSet[0];
		Random r = new Random();
		int t,x = 0,y = 0;
		int count = 0;
		int tries = 90;
		
		ArrayList<double[][]> list = new ArrayList<double[][]>();		
		double[][] start = BoardRepresentation.computeBoard(b, white);
		double[][] end = null;
		double[][] result = new double[8][8];
		int[][][] pos = MGameUtility.cloneArray(b);
		int[][][] pos2 = MGameUtility.cloneArray(b);
		int[][][] minPos = pos2;
		double min = 0;
		boolean kingInCheck = false;
		int kingsFound = 0;
		
		ArrayList<int[]> piecesQ = MGameUtility.ReturnAllPieces(pos2, (white+1)%2);
		ArrayList<int[]> piecesP = MGameUtility.ReturnAllPieces(pos2, white);
		double initialFillCounter = piecesQ.size() + piecesP.size();
		double fillCounter = initialFillCounter;
		
		if(heuristicSet.length == 1) {
			min = GameSystem.MeasureHeuristic(pos, white, heuristic);
		}
		else if(heuristicSet.length == 2) {
			relation = true;
			min = GameSystem.MeasureHeuristic(pos, white, heuristicSet[0])/GameSystem.MeasureHeuristic(pos, white, heuristicSet[1]);;
		}
		
				
		
		/*
		 * 
		 * 
		 * 
		 * 
		 */
	
		
		while(tries > 0){
			
			System.out.println(tries);
			//new eval in terms of this h is closer to zero
			
			do{
	
				for (int i = 0; i<8; i++) {
					for(int j = 0; j<8; j++) {
						if(pos[i][j] == null) {
							continue;
						}
		
						//i = piece[3] = 5, j = piece[4] = 8, kings coordinates
						//x = 1, y = 2
						//checks passed pos2[5][8] -> pos2[1][2], pos2[1][2] -> null
						
						//later, piece2 has coords of 1,2
						//when checks are passed, and x,y target square is found, 
						//nullpointer because pos2[1][2] was set to null before
						
						t = r.nextInt(16);
						if(t < 4) {
							do {
								x = r.nextInt(8);
								y = r.nextInt(8);
								
								if(pos2[x][y] != null) {
									fillCounter--;
								}
	//							
	//							if((pos2[x][y][0] == 16 || pos2[x][y][0] == 23) && MGameUtility.distance(pos2, q, x, y, kingX, kingY, false) == 1) {
	//								kingInCheck = true;
	//							}
	//							kingInCheck = false;
								
	

								
							}
							//iterate while distance is too high OR non-null target element is either king
							while(MGameUtility.distance(pos2, pos2[i][j], i, j, x, y, false) > maxDist ||
									(pos2[x][y] != null && (pos2[x][y][0] == 16 || pos2[x][y][0] == 23)));
	
							pos2[i][j][3] = x;
							pos2[i][j][4] = y;
							pos2[x][y] = pos2[i][j];
							pos2[i][j] = null;
							
		
							
						}
					}
				}
				
				if(GameSystem.MeasureHeuristic(pos2, white, heuristic) < min) {
					min = GameSystem.MeasureHeuristic(pos2, white, heuristic);
					minPos = pos2;
				}

				tries--;
				
				for (int c = 0; c < 8; c++) {
					for (int d = 0; d < 8; d++) {
						if(pos2[c][d] != null) {
							System.out.print(pos2[c][d][0]+" ");
							if(pos2[c][d][0] == 16) {
								System.out.println("king coords "+c+" "+d+" "+pos2[c][d][3]+" "+pos2[c][d][4]);
							}
						}
						else {
							System.out.print("   ");
						}
					}
					System.out.println();
					System.out.println();
				}
				
				if(tries == 0 || fillCounter / initialFillCounter < 0.22) {
					list.add(BoardRepresentation.computeBoard(pos2, white));
				}
				
				pos2 = pos;
			}
			while(heuristicSet.length == 2 && (((((GameSystem.MeasureHeuristic(pos, white, heuristicSet[0]) / 
					GameSystem.MeasureHeuristic(pos, white, heuristicSet[1])) > value && !rising) ||
					((GameSystem.MeasureHeuristic(pos2, white, heuristicSet[0]) / 
					GameSystem.MeasureHeuristic(pos2, white, heuristicSet[1])) < value && rising)) && relation)
					) || (GameSystem.MeasureHeuristic(pos2, white, heuristic) >= value && !rising) ||
					(GameSystem.MeasureHeuristic(pos2, white, heuristic) <= value) && rising);
			
			list.add(BoardRepresentation.computeBoard(pos2, white));
	
			
		}
	return list;
		
	}
	
	
	
}
