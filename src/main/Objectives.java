package main;

import java.util.Random;

/**
 * 
 * Generates a set of objectives
 * 
 * of the form
 * 
 * 1: (dont prefer /prefer)				(own pieces) 			(attack/keep-in-place)
 * 2: (favor/neglect) 				 	(pieces) 						
 * 3: (minimize/neutralize/maximize) 	(feature)		
 * 4: (clear/fill/guard/attack) square 	(x) (y)
 * 5: (sacrifice/guard) 				(own piece)
 * 6: (move backwards / forwards)		(own piece)	 			(n squares)
 * 7: prefer (equal/winning) trades
 * 8: (dont prefer/prefer) equalizing (threats/safe squares)
 * 9: (ignore/attack)  					(enemy pieces)
 * 
 * weight = [0,100]
 * count = [0,8]
 * 
 * 
 * 
 * @author shiftzero
 *
 */


public class Objectives {

	private static int[][] objectiveList;
	private static int[] piecesOwn = {11,12,13,14,15,16};
	private static int[] piecesOpponent = {18,19,20,21,22,23};
	private static int[] counters = {1,1,10,5,64};//{drawCounter, captureCounter, confusionCounter,
	//objectiveFailCounter, resignCounter}
	
	public static int[][] init(int k) {
		int temp;
		Random r = new Random();
		
		objectiveList = new int[k][3];
	
		for(int i = 0; i<k; i++) {
			temp = r.nextInt(9) + 1;
			
			if(temp == 1) {
				objectiveList[i] = new int[] {piecesOwn[r.nextInt(6)], r.nextInt(2), piecesOpponent[r.nextInt(6)]};
			}
			else if(temp == 2) {
				objectiveList[i] = new int[] {r.nextInt(2), piecesOwn[r.nextInt(6)], 0};
			}
			else if(temp == 3){
				objectiveList[i] = new int[] {r.nextInt(3)-1, r.nextInt(26)+1};
			}
			else if(temp == 4) {
				objectiveList[i] = new int[] {r.nextInt(4)-1, r.nextInt(8), r.nextInt(8)};
			}
			else if(temp == 5) {
				objectiveList[i] = new int[] {r.nextInt(2), piecesOwn[r.nextInt(6)],0};
			}
			else if(temp == 6) {
				objectiveList[i] = new int[] {r.nextInt(2), piecesOwn[r.nextInt(6)], r.nextInt(8)};
			}
			else if(temp == 7) {
				objectiveList[i] = new int[] {r.nextInt(2), 0,0};
			}
			else if(temp == 8) {
				objectiveList[i] = new int[] {r.nextInt(2), r.nextInt(2),0};
			}
			else if(temp == 9) {
				objectiveList[i] = new int[] {r.nextInt(2), piecesOpponent[r.nextInt(6)]};
			}
		}
		
		return objectiveList;
	}
	
	public static double[] computeWeights() {
		
	}
	
	public static int[] computeCounts() {
		
	}
	
	public int[] getCounters() {
		return counters;
	}
	
}
