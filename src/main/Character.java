package main;

import java.util.Random;

public class Character {

	
	/*
	 * Get a BoardRep subtraction that approximates gamestate/Piece[][]/heuristics-state moving from eval to dir, individual h moves closer to eval, diff -> 0,
	 */
	
	public static double[][] mine(int[][][] b, int white, int heuristic, double eval, int maxDist) {
		
		Random r = new Random();
		int t,x,y;
		int count = 0;
		int tries = 90;
		double min = 0;
		double[][] start = MSystem.computeBoard(b, white);
		double[][] end = null;
		double[][] result = new double[8][8];
		int[][][] pos = MGameUtility.cloneArray(b);
		int[][][] minPos = pos;
		
		
		//new eval in terms of this h is closer to zero
		while(Math.abs(eval - GameLogic.MeasureHeuristic(pos, white, heuristic)) > Math.abs(eval)) {
		
			for (int i = 0; i < 8 && count < 16; i++) {
				for (int j = 0; j < 8 && count < 16; j++) {
					if(pos[i][j] == null) {
						continue;
					}
					t = r.nextInt(16);
					if(t < 8) {
						
						do {
							x = r.nextInt(8);
							y = r.nextInt(8);
						}
						while(MGameUtility.distance(pos, pos[i][j], i, j, x, y, false) > 5);
						pos[x][y] = pos[i][j];
						pos[i][j] = null;
						count++;
							
						
						if(t > 3) {
							
						}
						else if(t > 0) {
							
						}
	
						
					}
				}
			}
			
			if(Math.abs(eval - GameLogic.MeasureHeuristic(pos, white, heuristic)) - Math.abs(eval) < min) {
				min = Math.abs(eval - GameLogic.MeasureHeuristic(pos, white, heuristic)) - Math.abs(eval);
				minPos = pos;
			}
			
			if(tries == 0) {
				return MSystem.computeBoard(minPos, white);
			}
			
		}
		
		return MSystem.computeBoard(pos, white);

		
	}
	
	
	
}
