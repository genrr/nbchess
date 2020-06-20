package main;


import java.util.ArrayList;
import java.util.Random;

public class MSystem {
	
	private static double[][] relativeValues = new double[8][8];
	private static double[][] heightValues = new double[8][8];
	private static double[][] relationValues = new double[8][8];
	private static double[][] zValues = new double[8][8];
	
	private static double dampeningValue = 24.0;
	public static final int DIST_CALC_DEPTH = 8;

	
	/* RootSystemCall() / low level system function
	 * 
	 * Handles (low-level representation and) optimization
	 * 
	 * 
	 */
	
	public static void RootSystemCall() {
		
	}
	
		

	
	/*METRICS:
	 * 
	 * d = Piece Objects in a matrix
	 * v = Piece relative values, computed by RelPV(Piece p)
	 * r = Relative values in r[i][j] add(subtract if different color) the value v[i][j] (-=  
	 * h = degree of which random square is a valid square * r[i][j] * v[i][j]
	 * z = 16 largest height values
	 * 
	 */
	
	/*
	 * [pieces]
	 * 
	 * [2][1][0]
	 * 
	 * [][][]
	 * 
	 */

	public static double[][][] ComputeMetrics(Piece[][] board) {

		
		relativeValues = new double[8][8];
		heightValues = new double[8][8];
		relationValues = new double[8][8];
		zValues = new double[8][8];
		
		//first compute v (the relative piece values)
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(board[i][j] != null) {
					relativeValues[i][j] = RelPV(board, board[i][j]);
				}
				else {
					relativeValues[i][j] = 0;
				}
			}
		}
		
		//then compute r (the relations to other pieces)

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(board[i][j] != null) {
					for (int m = 0; m < 8; m++) {
						for (int n = 0; n < 8; n++) {
							if((m != i || n != j) &&  board[m][n] != null) {
								if(MGameUtility.distance(board, board[i][j], i, j, m, n, false) == 1) {
									//decremented at (m,n) by rel piece values of all the attackers
									relationValues[m][n] -= relativeValues[i][j]; 
								}
								else if(MGameUtility.distance(board, board[i][j], i, j, m, n, false) == -1) {
									//incremented at (m,n) by rel piece values of all its defenders
									relationValues[m][n] += relativeValues[i][j];
								}
							}
						}
					}
				}
				
				
			}
		}
		
		//then compute h (the potentials)
		int counter = 0;
		int targetX = -1;
		int targetY = -1;
		Random r = new Random();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(board[i][j] != null) {

					while(MGameUtility.distance(board, board[i][j], i, j, targetX, targetY, false) != 1) {
						targetX = r.nextInt(8);
						targetY = r.nextInt(8);
						counter++;
						if(counter > 63) {
							break;
						}
					}
					//relative piece values + relative attack/defend counts for a piece divided by amount of tries needed to find
					//adjusted exponentially
					heightValues[i][j] = (relationValues[i][j] + relativeValues[i][j]) / Math.exp((counter*1.0)/dampeningValue);
				}
				else {
					heightValues[i][j] = 0;
				}
				
			}
		}
		
		//finally, time for z values, the area complexity metric
		double temp1 = 0;
		double temp2 = 0;
		double temp3 = 0;
		int tempX1 = 0,tempY1 = 0,tempX2 = 0,tempY2 = 0,tempX3 = 0,tempY3 = 0;
		int l1 = 0, l2 = 0, l3 = 0, a1 = 0, a2 = 0, a3 = 0;
		ArrayList<Piece> post = new ArrayList<>();
		ArrayList<Piece> post2 = new ArrayList<>();
		ArrayList<Piece> post3 = new ArrayList<>();
		ArrayList<Piece> post4 = new ArrayList<>();
		ArrayList<Piece> post5 = new ArrayList<>();
		ArrayList<Piece> post6 = new ArrayList<>();
		ArrayList<Piece> lin1 = new ArrayList<>();
		ArrayList<Piece> lin2 = new ArrayList<>();
		ArrayList<Piece> lin3 = new ArrayList<>();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(heightValues[i][j] > temp1) {
					temp1 = heightValues[i][j];
					tempX1 = i;
					tempY1 = j;
				}
				if(relationValues[i][j] > temp2) {
					temp2 = relationValues[i][j];
					tempX2 = i;
					tempY2 = j;
				}
				if(relativeValues[i][j] > temp3) {
					temp3 = relativeValues[i][j];
					tempX3 = i;
					tempY3 = j;
				}
				
			}
		}
		
		/*
		 * max rel value
		 * max height value
		 * max relation value
		 */
		
		for(int k1 = 0; k1<8; k1++) {
			for(int k2 = 0; k2<8; k2++) {
				Piece p1 = board[tempX1][tempY1];
				Piece p2 = board[tempX2][tempY2];
				Piece p3 = board[tempX3][tempY3];
				if(MGameUtility.distance(board, board[k1][k2], k1, k2, tempX1,tempY1, false) == -1) {
					post.add(board[k1][k2]);
					l1++;
				}
				if(MGameUtility.distance(board, board[k1][k2], k1, k2, tempX2,tempY2, false) == -1) {
					post2.add(board[k1][k2]);
					l2++;
				}
				if(MGameUtility.distance(board, board[k1][k2], k1, k2, tempX3,tempY3, false) == -1) {
					post3.add(board[k1][k2]);
					l3++;
				}
				if(MGameUtility.distance(board, p1, p1.getX(), p1.getY(), k1, k2, false) == 1 &&  board[k1][k2] != null) {
					post4.add(p1);
					lin1.add(board[k1][k2]);
					a1++;
				}
				if(MGameUtility.distance(board, p2, p2.getX(), p2.getY(), k1, k2, false) == 1 &&  board[k1][k2] != null) {
					post5.add(p2);
					lin2.add(board[k1][k2]);
					a2++;
				}
				if(MGameUtility.distance(board, p3, p3.getX(), p3.getY(), k1, k2, false) == 1 &&  board[k1][k2] != null) {
					post6.add(p3);
					lin3.add(board[k1][k2]);
					a3++;
				}
			}
		}
		
		temp1 *= l1 + a1*a1;
		temp2 *= l2 + a2*a2;
		temp3 *= l3 + a3*a3;
		
		//defenders get the multiplier
		
		for(Piece p : post) {
			zValues[p.getX()][p.getY()] += temp1;
		}
		
		for(Piece p : post2) {
			zValues[p.getX()][p.getY()] += temp2;
		}
		
		for(Piece p : post3) {
			zValues[p.getX()][p.getY()] += temp3;
		}
		
		//attackers get max product unitValue*relPV of attacked opponent Piece
		double maxCValue = 0;
		
		for(Piece q : lin1) {
			if(MGameUtility.unitValue(q)*RelPV(board,q) > maxCValue) {
				maxCValue = MGameUtility.unitValue(q) * RelPV(board, q);
			}
		}
		
		for(Piece p : post4) {
			zValues[p.getX()][p.getY()] += maxCValue;
		}
		
		maxCValue = 0;
		
		for(Piece q : lin2) {
			if(MGameUtility.unitValue(q)*RelPV(board,q) > maxCValue) {
				maxCValue = MGameUtility.unitValue(q) * RelPV(board, q);
			}
		}
		
		for(Piece p : post5) {
			zValues[p.getX()][p.getY()] += maxCValue;
		}
		
		maxCValue = 0;
		
		for(Piece q : lin3) {
			if(MGameUtility.unitValue(q)*RelPV(board,q) > maxCValue) {
				maxCValue = MGameUtility.unitValue(q) * RelPV(board, q);
			}
		}
		
		for(Piece p : post6) {
			zValues[p.getX()][p.getY()] += maxCValue;
		}

		return new double[][][] {relativeValues,relationValues,heightValues,zValues};

	}
	
	
	
	
	
	//#4 Relative Piece Value
		/* 
		 * 
		 * very low to very high (0-10)
		 * 
		 * cases: 
		 * 
		 * unitvalue: high
		 * sum of pieces: high
		 * sum of piece values: high -> result: 4
		 * 
		 * unitvalue: low
		 * sum of pieces: high
		 * sum of piece values: high -> result: 2
		 * 
		 * unitvalue: high
		 * sum of pieces: low
		 * sum of piece values: high -> result: 8
		 * 
		 * unitvalue: low
		 * sum of pieces: low
		 * sum of piece values: high -> result: 7
		 * 
		 * unitvalue: high
		 * sum of pieces: high
		 * sum of piece values: low -> result: 8
		 * 
		 * unitvalue: low
		 * sum of pieces: high
		 * sum of piece values: low -> result: 5
		 * 
		 * unitvalue: high
		 * sum of pieces: low
		 * sum of piece values: low -> result: 10
		 * 
		 * unitvalue: low
		 * sum of pieces: low
		 * sum of piece values: low -> result: 8
		 * 
		 * 
		 * functions:
		 * 
		 * unitValue() * sumOfPieces * sumOfPieceValues() = 
		 * 
		 * 
		 * !!!!!!!!IMPORTANT !!!!!!!!!! 
		 * 
		 * flip sign when evaluating for BLACK!
		 * 
		 * 
		 * start game piece value sum:
		 * 
		 * 9 + 255 + 5 + 5 + 3 + 3 + 3 + 3 + 8 = 294
		 *
		 */
		
		public static double RelPV(Piece[][] board, Piece p) {
			
			boolean currentTurn = p.getColor(); 			
			double highTldUV = 4.5;
			double lowTldUV = 2.3;			
			double highTldPS = 8.0;
			double lowTldPS = -8.0;			
			double highTldRS = 4.5;
			double lowTldRS = -4.5;
			
			ArrayList<Piece> l1 = MGameUtility.ReturnAllPieces(board, currentTurn);
			ArrayList<Piece> l2 = MGameUtility.ReturnAllPieces(board, !currentTurn);
			int s1 = l1.size();
			int s2 = l2.size();
			double ownPieceValuesSum = 0;
			double enemyPieceValuesSum = 0;			
			double unitValue = MGameUtility.unitValue(p);
			double PiecesSum =  s1 - s2;

			for(int i = 0; i < s1; i++) {
				ownPieceValuesSum += MGameUtility.unitValue(l1.get(i));
			}
			for(int i = 0; i < s2; i++) {
				enemyPieceValuesSum += MGameUtility.unitValue(l2.get(i));
			}
			
			
			double ValuesSum = ownPieceValuesSum - enemyPieceValuesSum;
			int unitValueStr = 0;
			int pieceSumStr = 0;
			int pieceValueSumStr = 0;

			if(unitValue < lowTldUV) {
				unitValueStr = -1;
			}
			else if(lowTldUV < unitValue && unitValue <= highTldUV) {
				unitValueStr = 0;
			}
			else {
				unitValueStr = 1;
			}
			
			if(PiecesSum < lowTldPS) {
				pieceSumStr = -1;
			}
			else if(lowTldPS < PiecesSum && PiecesSum <= highTldPS) {
				pieceSumStr = 0;
			}
			else {
				pieceSumStr = 1;
			}
			
			if(ValuesSum < lowTldRS) {
				pieceValueSumStr = -1;
			}
			else if(lowTldRS < ValuesSum && ValuesSum <= highTldRS) {
				pieceValueSumStr = 0;
			}
			else {
				pieceValueSumStr = 1;
			}
			
			if(unitValueStr == 1 && pieceSumStr == 1 && pieceValueSumStr == 1) {
				return 4;
			}
			else if(unitValueStr == 1 && pieceSumStr == 0 && pieceValueSumStr == 1) {
				return 2;
			}
			else if(unitValueStr == 0 && pieceSumStr == 1 && pieceValueSumStr == 1) {
				return 8;
			}
			else if(unitValueStr == 0 && pieceSumStr == 0 && pieceValueSumStr == 1) {
				return 7;
			}
			else if(unitValueStr == 1 && pieceSumStr == 1 && pieceValueSumStr == 0) {
				return 8;
			}
			else if(unitValueStr == 1 && pieceSumStr == 0 && pieceValueSumStr == 0) {
				return 5;
			}
			else if(unitValueStr == 0 && pieceSumStr == 1 && pieceValueSumStr == 0) {
				return 10;
			}
			else if(unitValueStr == 0 && pieceSumStr == 0 && pieceValueSumStr == 0) {
				return 8;
			}
			return 0;
		}
	
		
		

}
