package main;


import java.util.Random;

public class MSystem {
	
	private static double[][] relativeValues = new double[8][8];
	private static double[][] heightValues = new double[8][8];
	private static double[][] relationValues = new double[8][8];
	private static double[] zValues = new double[16];
	
	
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

	public static void ComputeMetrics(Piece[][] board) {
		
		//first compute v (the relative piece values)
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(board[i][j] != null) {
					relativeValues[i][j] = RelPV(board[i][j]);
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
							if(board[m][n] != null) {
								if(MGameUtility.distance(board, board[i][j], i, j, m, n, false) == 1) {
									if(board[i][j].getColor() != board[m][n].getColor()) {
										relationValues[i][j] -= relativeValues[i][j]; 
									}
									else {
										relationValues[i][j] += relativeValues[i][j];
									}
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
					heightValues[i][j] = relationValues[i][j] * relativeValues[i][j] * (1.0/counter) * 64;
				}
			}
		}
		
		//finally, time for z values, a double array of 16 largest height values computed above, > for white, < for black piece
		double temp = 0;
		int index = 0;
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(board[i][j] != null) {
					if(Math.abs(heightValues[i][j]) > temp) {
						temp = Math.abs(heightValues[i][j]);
						if(index < 16) {
							zValues[index] = temp;
						}
						else {
							return;
						}
						
						index++;
					}
				}
			}
		}
		
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
		
		public static double RelPV(Piece p) {
			
			double highTldUV = 4.5;
			double lowTldUV = 2.3;
			
			double highTldPS = 8.0;
			double lowTldPS = -8.0;
			
			double highTldRS = 4.5;
			double lowTldRS = -4.5;
			
			double unitValue = MGameUtility.unitValue(p);
			double PiecesSum = PositionFeature.RelM();
			double ValuesSum = PositionFeature.RelMV();
			
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
