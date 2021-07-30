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
	
	/*
	 * 
	 * 
	 * encode turn boolean, + castling rights + en passant square + king in check status
	 * 
	 * 

	 * 
	 * 
	 * 
	 */
	
	
	
	public static double[][] computeBoard(int[][][] board, int turn) {
		
		ArrayList<int[]> ownPieces = MGameUtility.ReturnAllPieces(board, turn == 1 ? 1 : 0);
		ArrayList<int[]> enemyPieces = MGameUtility.ReturnAllPieces(board, turn == 1 ? 0 : 1);
		ArrayList<int[]> ownMoves = MGameUtility.getAllMoves(board, turn);
		ArrayList<int[]> opponentMoves = MGameUtility.getAllMoves(board, (turn+1) % 2);

		double[][][] ownStack = new double[ownPieces.size()][8][8];
		double[][][] opponentStack = new double[enemyPieces.size()][8][8];
		double[][] table = new double[8][8];
		int x,y,tx,ty;
		
		for(int i = 0; i<enemyPieces.size();i++) {
			for (int[] move : opponentMoves) {
				x = move[0];
				y = move[1];
				tx = move[2];
				ty = move[3];
				opponentStack[i][tx][ty] = MGameUtility.unitValue(board[x][y]) + MSystem.RelPV(board, board[x][y]);
			}
		}
		
		for(int i = 0; i<ownPieces.size();i++) {
			for (int[] move : ownMoves) {
				x = move[0];
				y = move[1];
				tx = move[2];
				ty = move[3];
				ownStack[i][tx][ty] = MGameUtility.unitValue(board[x][y]) + MSystem.RelPV(board, board[x][y]);
			}
		}
		
		for(int w = 0; w<8; w++) {
			for(int q = 0; q<8; q++) {
				for(int i = 0; i<ownPieces.size();i++) {
					if(ownStack[i][q][w] != 0) {
						table[q][w] += ownStack[i][q][w];
					}
				}
			}
		}
		
		for(int w = 0; w<8; w++) {
			for(int q = 0; q<8; q++) {
				for(int i = 0; i<enemyPieces.size();i++) {
					if(opponentStack[i][q][w] != 0) {
						table[q][w] /= opponentStack[i][q][w];
					}
				}
			}
		}
		
		
		return table;
	}
	
	/*
	 * linear sum of boardRep unitValues
	 */
	
	public static double boardDistance(double[][] pos1, double[][] pos2) {
		
		double r = 0;
		
		for(int a = 0; a<8; a++) {
			for(int b = 0; b<8; b++) {
				r += 16*Math.sin(a / 8.0) + 16*Math.sin(b / 8.0) + (pos1[a][b] - pos2[a][b]);
			}
		}
		return r;
	}
	
	public static double[][] boardSubtraction(double[][] pos1, double[][] pos2){
		
		double[][] result = new double[8][8];
		
		for(int a = 0; a<8; a++) {
			for(int b = 0; b<8; b++) {
				result[a][b] = pos1[a][b] - pos2[a][b];
			}
		}
		
		
		return result;
		
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

	public static double[][][] ComputeMetrics(int[][][] board) {

		
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
		ArrayList<int[]> post = new ArrayList<>();
		ArrayList<int[]> post2 = new ArrayList<>();
		ArrayList<int[]> post3 = new ArrayList<>();
		ArrayList<int[]> post4 = new ArrayList<>();
		ArrayList<int[]> post5 = new ArrayList<>();
		ArrayList<int[]> post6 = new ArrayList<>();
		ArrayList<int[]> lin1 = new ArrayList<>();
		ArrayList<int[]> lin2 = new ArrayList<>();
		ArrayList<int[]> lin3 = new ArrayList<>();
		
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
				int[] p1 = board[tempX1][tempY1];
				int[] p2 = board[tempX2][tempY2];
				int[] p3 = board[tempX3][tempY3];
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
				if(MGameUtility.distance(board, p1, p1[3], p1[4], k1, k2, false) == 1 &&  board[k1][k2] != null) {
					post4.add(p1);
					lin1.add(board[k1][k2]);
					a1++;
				}
				if(MGameUtility.distance(board, p2, p2[3], p2[4], k1, k2, false) == 1 &&  board[k1][k2] != null) {
					post5.add(p2);
					lin2.add(board[k1][k2]);
					a2++;
				}
				if(MGameUtility.distance(board, p3, p3[3], p3[4], k1, k2, false) == 1 &&  board[k1][k2] != null) {
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
		
		for(int[] p : post) {
			zValues[p[3]][p[4]] += temp1;
		}
		
		for(int[] p : post2) {
			zValues[p[3]][p[4]] += temp2;
		}
		
		for(int[] p : post3) {
			zValues[p[3]][p[4]] += temp3;
		}
		
		//attackers get max product unitValue*relPV of attacked opponent Piece
		double maxCValue = 0;
		
		for(int[] q : lin1) {
			if(MGameUtility.unitValue(q)*RelPV(board,q) > maxCValue) {
				maxCValue = MGameUtility.unitValue(q) * RelPV(board, q);
			}
		}
		
		for(int[] p : post4) {
			zValues[p[3]][p[4]] += maxCValue;
		}
		
		maxCValue = 0;
		
		for(int[] q : lin2) {
			if(MGameUtility.unitValue(q)*RelPV(board,q) > maxCValue) {
				maxCValue = MGameUtility.unitValue(q) * RelPV(board, q);
			}
		}
		
		for(int[] p : post5) {
			zValues[p[3]][p[4]] += maxCValue;
		}
		
		maxCValue = 0;
		
		for(int[] q : lin3) {
			if(MGameUtility.unitValue(q)*RelPV(board,q) > maxCValue) {
				maxCValue = MGameUtility.unitValue(q) * RelPV(board, q);
			}
		}
		
		for(int[] p : post6) {
			zValues[p[3]][p[4]] += maxCValue;
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
		
		public static double RelPV(int[][][] board, int[] p) {
			
			int currentTurn = p[2]; 			
			double highTldUV = 4.5;
			double lowTldUV = 2.3;			
			double highTldPS = 8.0;
			double lowTldPS = -8.0;			
			double highTldRS = 4.5;
			double lowTldRS = -4.5;
			
			ArrayList<int[]> l1 = MGameUtility.ReturnAllPieces(board, currentTurn);
			ArrayList<int[]> l2 = MGameUtility.ReturnAllPieces(board, (currentTurn + 1) % 2);
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

		public static int[] generate() {
			
			Random r = new Random();
			int[] result = new int[6];
			
			for(int i = 0; i<4; i++) {
				result = new int[] {r.nextInt(10),r.nextInt(12),r.nextInt(10)-5,r.nextInt(12),r.nextInt(10)-5};
			}
			
			
			
			
			
			

			return result;
		}
	
		
		

}
