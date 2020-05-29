package main;

import java.util.ArrayList;

public class ValueFunction {

	
	public static int avgSq = 2;
	public static int avgAtt = 1;
	public static int avgDef = 2;
	
	/*
	 * #1 safe squares around king (safety)
	 * #2 sum of all distances to enemy king (attack)
	 * #3 sum of all dist == 1, (exploration)
	 * #4 move complexity ratio (damping)
	 * #5  (position) 

	 * 
	 *
	 */
	
	public static double computeValue(int index,Piece[][] board,boolean whitesTurn) {
		
		double n = 0.0;
		double threatMultSum = 0.0;
		int[] t = RuleSet.ReturnKingAndRookPositions(board, whitesTurn);
		ArrayList<Piece> enemyPieces = MGameUtility.ReturnAllPieces(board, !whitesTurn);
		ArrayList<Piece> ownPieces = MGameUtility.ReturnAllPieces(board, whitesTurn);
		ArrayList<int[]> ks;
		switch(index) {
		case 1: //kings safe squares
			int temp;
			ks = RuleSet.GetKingsSquares(t[0], t[1]);
			n = ks.size();
			//iterate kings squares
			for(int[] kingsSquare : ks){
				//iterate all enemyPieces
				for(Piece q : enemyPieces) {
					temp = MGameUtility.distance(board, q, q.getX(), q.getY(), 
							kingsSquare[0], kingsSquare[1], false);

					if(temp != 0) {
						threatMultSum += 1 / temp;	
					}
					// 1/1 + 1/3 + 1/2 (skipped zeros of 6 kings squares,
					//found 3 pieces with 1 with dist 1, 1 dist 3, and 1 with dist 2) = 22/12 ~ 1.83
				}
			}
			//e.g 6 - (1.83)² ~ 2.63
			return n - Math.pow(threatMultSum,2);
			
			
			
		case 2: //sum of all own pieces' distance to enemy king
			
			int eKingX = t[2];
			int eKingY = t[3];
			
			int sum = 0;
			
			for (Piece piece : ownPieces) {
				sum += MGameUtility.distance(board, piece, piece.getX(), piece.getY(), t[2], t[3], false);
			}
			
			return sum;
			
			
			
			
		case 3: //sum of all squares within 1
			int count = 0;
			
			for(int i = 0; i<8; i++) {
				for(int j = 0; j<8;j++) {
					for (Piece piece : ownPieces) {
						if(MGameUtility.distance(board, piece, piece.getX(), piece.getY(),
								i,j, false) == 1) {
							count++;
						}
					}
				}
			}
			
			
			return count;
		case 4:
			return MoveEntropy(board,whitesTurn) / MoveEntropy(board,!whitesTurn);
			
		case 5:
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
			
			for(Piece p : ownPieces) {
				for(int i = 0; i < 8; i++) {
					for(int j = 0; j < 8; j++) {
						dist = MGameUtility.distance(board, p, p.getX(), p.getY(),
								i, j, false);
						if(dist == 1){
							sqCount++;

							if(board[i][j].getColor() != whitesTurn) {
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
			return maxSqCount + sumAdjSqCount + sumAdjAtt + sumAdjDef; 
			
			
		}
		
		return 0;
		
		
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
	
	private static double MoveEntropy(Piece[][] board, boolean white) {
		
		ArrayList<int[]> list1 = MGameUtility.getAllMoves(board, white);
		
		Piece[][] newBoard = MGameUtility.cloneArray(board);

		double valueSum = 0;
		
		
		for (int i = 0; i < list1.size(); i++) {
			newBoard[list1.get(i)[2]][list1.get(i)[3]] = newBoard[list1.get(i)[0]][list1.get(i)[1]];
			newBoard[list1.get(i)[0]][list1.get(i)[1]] = null;
			
			double v1 = computeValue(1, newBoard, white) / 8.0;
			double v2 = computeValue(2, newBoard, white) / list1.size();
			double v3 = computeValue(3, newBoard, white) / 6.0;
			double v5 = computeValue(5, newBoard, white) / 5.5;
			
			valueSum += (v1+v2+v3+v5) / 4.0;
		}
		
		//return avg 
		return valueSum / list1.size();
		
		
	}
	
	
	public static boolean valuesCheck(Piece[][] pos, boolean white, double[] qualityValues) {
		double v1 = computeValue(1, pos, white);
		
		v1 /= 8.0;
		
		double v2 = computeValue(2,pos,white);
		
		v2 /= MGameUtility.ReturnAllPieces(pos, white).size();
		
		double v3 = computeValue(3,pos,white);
		
		v3 /= 6.0;
		
		double v4 = computeValue(4,pos,white);
		
		double v5 = computeValue(4,pos,white);
		
		v5 /= 5.5;
		
		
		v1 = Math.abs(1 - v1);
		v2 = Math.abs(1 - v2);
		v3 = Math.abs(1 - v3);
		v4 = Math.abs(1 - v4);
		v5 = Math.abs(1 - v5);
		
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
		
		if(sum < 0.5) {
			return true;
		}
		else {
			return false;
		}
		
		
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
	
	
	
}
