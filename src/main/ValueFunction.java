package main;

import java.util.ArrayList;

public class ValueFunction {


	public static int avgSq = 2;
	public static int avgAtt = 1;
	public static int avgDef = 2;
	private static double d0 = 2.9;
	private static double d1 = 0.138;
	private static double D = 1000;
	private static double f = 1.25;
	private static double k = 1560;

	/*
	 * #1 safe squares around king (safety)
	 * #2 sum of all distances to enemy king (attack)
	 * #3 sum of all dist == 1, (exploration)
	 * #4 move complexity ratio (damping)
	 * #5 maxSqCount + sumAdjSqCount + sumAdjAtt + sumAdjDef
	 * 
	 * Av1 + Bv2 + Cv3 + Dv4 + Ev5
	 * 
	 * A = 20
	 * B = 9
	 * C = 9
	 * D = 5
	 * E = 11
	 */

	public static double[] computeValue(int[][][] board,int whitesTurn, boolean computeEntropy) {
		double[] values = new double[5];

		double n = 0.0;
		double threatMultSum = 0.0;
		int[] t = MGameUtility.getKingPos(board, whitesTurn);
		ArrayList<int[]> enemyPieces = MGameUtility.ReturnAllPieces(board, (whitesTurn+1)%2);
		ArrayList<int[]> ownPieces = MGameUtility.ReturnAllPieces(board, whitesTurn);
		ArrayList<int[]> ks;

		
		
		//1) kings safe squares ***
		int distance;
		ks = RuleSet.GetKingsSquares(board, t[0], t[1]);
		n = ks.size();
		//iterate kings squares
		for(int[] kingsSquare : ks){
			//iterate all enemyPieces
			for(int[] q : enemyPieces) {
				distance = MGameUtility.distance(board, q, q[3], q[4], 
						kingsSquare[0], kingsSquare[1], false);

				if(distance != 0) {
					//System.out.println("dist "+temp);
					threatMultSum += 3 / distance;	
					//System.out.println("king threat sum "+threatMultSum);
				}
				// 1/1 + 1/3 + 1/2 (skipped zeros of 6 kings squares,
				//found 3 pieces with 1 with dist 1, 1 dist 3, and 1 with dist 2) = 22/12 ~ 1.83
			}
		}
		//e.g 6 - (1.83)² ~ 2.63
		values[0] = (n - Math.pow(threatMultSum,2)) / n;



		//2) sum of all own pieces' distance to enemy king ***
		int[] ekt = MGameUtility.getKingPos(board, (whitesTurn+1) % 2);
		int eKingX = ekt[0];
		int eKingY = ekt[1];

		int sum = 0;

		for (int[] piece : ownPieces) {
			sum += MGameUtility.distance(board, piece, piece[3], piece[4], eKingX, eKingY, false);
		}

		values[1] = sum / MGameUtility.ReturnAllPieces(board, whitesTurn).size();




		//3) sum of all squares within 1 ***
		int count = 0;

		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8;j++) {
				for (int[] piece : ownPieces) {
					//castling not counted
					if(MGameUtility.distance(board, piece, piece[3], piece[4],
							i,j, false) == 1) {
						//enpassant square found
						if(board[8][0][0] != -1) {
							count++;
						}
						count++;
					}
				}
			}
		}
		values[2] = count / 6.0;

		
		//4) enttropu ***
		if(computeEntropy) {
			values[3] = 5;//MoveEntropy(board,whitesTurn) / MoveEntropy(board,!whitesTurn);
		}
		
		
		
		//5) general position, piece activity
		int sqCount = 0;
		int maxSqCount = 0;
		int adjSqCount = 0;
		int sumAdjSqCount = 0;
		int attack = 0;
		int defence = 0;
		int dist;
		int adjAtt = 0;
		int adjDef = 0;
		int sumAdjAtt = 0;
		int sumAdjDef = 0;

		for(int[] p : ownPieces) {
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					dist = MGameUtility.distance(board, p, p[3], p[4],
							i, j, false);
					if(dist == 1){
						sqCount++;
						if(board[i][j] != null && board[i][j][2] != whitesTurn) {
							attack++;
						}
					}
					else if(dist == -1) {
						defence++;
					}
				}
			}
			adjSqCount += (-avgSq + sqCount);
			adjAtt += (-avgAtt + attack);
			adjDef += (-avgDef + defence);

			if(sqCount > maxSqCount) {
				maxSqCount = sqCount;
			}
			sumAdjSqCount += adjSqCount;
			sumAdjAtt += adjAtt;
			sumAdjDef += adjDef;

			sqCount = 0;
			adjSqCount = 0;
			adjAtt = 0;
			adjDef = 0;
			attack = 0;
			defence = 0;
		}
		//return value (emphasizing on) max squares
		//+ adjusted squares + adjusted attacks + adjusted defences
		values[4] = maxSqCount + sumAdjSqCount + sumAdjAtt + sumAdjDef; 
		values[4] /= 5.5; 

		return values;
	}

	/*
	 * Here we calculate mean entropy:
	 * 
	 * measure of how good next moves in terms of other valuefunctions are in given position
	 * 
	 * example would be a position, where there is  a couple of neutral moves and 2-3 massive blunders
	 * -> big differences
	 * 
	 */

	private static double MoveEntropy(int[][][] board, int white) {
		System.out.println("computing entropies for "+white);
		ArrayList<int[]> list1 = MGameUtility.getAllMoves(board, white);

		int[][][] newBoard = MGameUtility.cloneArray(board);
		int[][][] newBoard2 = MGameUtility.cloneArray(board);

		double diff = 0;


		for (int i = 0; i < list1.size(); i++) {
			GameSystem.makeMove(list1.get(i), newBoard2);


			double[] d = computeValue(newBoard2, white,false);
			double[] v = normalize(newBoard2, white, d, new double[] {16,16,16,16,16});

			for(int j = 0; j<5; j++) {
				for(int k = 0; k<5; k++) {
					if(j < k) {
						diff += v[j] - v[k];
					}
				}
			}
			
			/*
			 * 	 1 2 3 4 5
			 * 1 x
			 * 2 @ x
			 * 3 @ @ x
			 * 4 @ @ @ x
			 * 5 @ @ @ @ x
			 */ 

			newBoard2 = newBoard;

		}

		System.out.println("diff"+diff);
		System.out.println("list1"+list1.size());
		
		//return avg 
		return diff / list1.size();


	}


	public static double[] normalize(int[][][] pos, int white, double[] v, double[] qualityValues) {
		double v1 = v[0];
		double v2 = v[1];		
		double v3 = v[2];
		double v4 = v[3];
		double v5 = v[4];

		/*
		 * 0.1
		 * 0.4
		 * 0.25
		 * 0.33
		 * 
		 * 0.15
		 * 0.15
		 * 0.0
		 * 0.08
		 * 
		 * 0.38
		 * 
		 */


		double sum = Math.abs(v1 - 1/qualityValues[0]) + 
				Math.abs(v2 - 1/qualityValues[1]) +
				Math.abs(v3 - 1/qualityValues[2]) + 
				Math.abs(v4 - 1/qualityValues[3]) +
				Math.abs(v5 - 1/qualityValues[4]);

		return new double[] {v1,v2,v3,v4,v5,sum};


	}

	/*
	 * 	public static double KingSquaresSafetyMetric() {

		int x = RuleSet.ReturnKingAndRookPositions(board,whitesTurn)[0];
		int y = RuleSet.ReturnKingAndRookPositions(board,whitesTurn)[1];

		ArrayList<int[]> t = RuleSet.GetKingsSquares(x, y);

		int count = 0;

		for(int[] d : t) {
			for(Piece e : enemyPieces) {
				if(MGameUtility.distance(board, e, e.getX(), e.getY(), d[0], d[1], false) == 1) {
					count++;
				}
			}
		}

		if(t.size() == 0) {
			return 0;
		}
		else {
			return (count*1.0) / t.size(); 
		}

	}
	 */


	/*
	 * get valuation as a single double using coefficients A-E
	 * 
	 * terrible: < 1
	 * bad: 1-3
	 * inefficient: 3-10
	 * negligible: 10-27
	 * ok: 27-64
	 * efficient: 64-96
	 * good: 96-128
	 * excellent: 128-136
	 * flawless: 136-141
	 * 
	 */

	public static double crunch(double[] values, double[] coeff) {

		//0.001 -> 138
		double mul = 0;

		for(int i = 0; i<5; i++) {
			mul += (1/coeff[i]) * (d1*k*f)/(d0*values[i]*Math.exp(values[i]/D));
		}

		return mul / 5;
	}

	public static double[] direction(double[] v, boolean b) {
		
		double[] d = new double[5];
		
		
		for(int c = 0; c<5; c++) {
			if(v[c] == 0) {
				d[c] = 255;
			}
			else {
				d[c] = Math.abs(1.0 / v[c]);
			}
		}
		
		if(b) {
			
		}
		else {
			
		}
		
		return d;
	}


}
