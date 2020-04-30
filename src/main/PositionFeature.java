package main;

import java.util.ArrayList;
import java.util.Arrays;

public class PositionFeature {

	
	private static Piece[][] board;
	private static ArrayList<Piece> ownPieces;
	private static ArrayList<Piece> enemyPieces;
	private static ArrayList<Piece> defendingPieces;
	private static int turnNumber;
	private static boolean whitesTurn;
	private static int branching = 0;
	
	private static double f10;
	private static double f11;
	private static ArrayList<int[]> allLegalSquares = new ArrayList<int[]>();
	private static ArrayList<int[]> allFreeSquares = new ArrayList<int[]>();
	private static ArrayList<int[]> allSafeSquares = new ArrayList<int[]>();
	private static ArrayList<int[]> threatenedByEnemy = new ArrayList<int[]>();
	private static double[] t;
	private static int[] t2;
	private static int[] t3;
	private static int complexity;
	
	
	/*
	 * pass the board, turn number and current turn
	 * init local variables
	 * handle frequently needed arrays of pieces
	 */
	

	public static void initFeatures(Piece[][] b, boolean currentTurn, int turnN) {
		board = b;
		turnNumber = turnN;
		whitesTurn = currentTurn;
		
		if(whitesTurn) {
			enemyPieces = MGameUtility.ReturnAllPieces(board, false);
			ownPieces = MGameUtility.ReturnAllPieces(board, true);
		}
		else {
			ownPieces = MGameUtility.ReturnAllPieces(board, false);
			enemyPieces = MGameUtility.ReturnAllPieces(board, true);
		}
		
		System.out.println("#1");
		t = HelperFunction1();
		System.out.println("#2");
		HelperFunction2();
		System.out.println("#3");
		t2 = HelperFunction3(ownPieces);
		System.out.println("#4");
		t3 = HelperFunction3(enemyPieces);
		System.out.println("#5");
	}
	
	
	private static double[] HelperFunction1() {
		double squares = 0;
		double maxSquares = 0;
		double freeSquares = 0;
		double maxFreeSquares = 0;
		double safeSquares = 0;
		double maxSafeSquares = 0;
		boolean safe = true;
		
		System.out.println("start");
		
		for (Piece p : ownPieces) {
			
			for(int i = 0; i<8; i++) {
				for(int j = 0; j<8; j++) {
					
					if(MGameUtility.distance(board, p, p.getX(), p.getY(), i, j, false) == 1) {
						System.out.println("#");
						squares++;
						allLegalSquares.add(new int[] {i,j});
						
						if(board[i][j] == null) {
							freeSquares++;
							allFreeSquares.add(new int[] {i,j});
						}
						
						for(int[] threatenedSquare : threatenedByEnemy) {
							if(threatenedSquare[0] == i && threatenedSquare[1] == j) {
								safe = false;
							}
						}
						if(safe) {
							safeSquares++;
						}
						
					}
				}
			}
			if(squares > maxSquares) {
				maxSquares = squares;
			}
			if(freeSquares > maxFreeSquares) {
				maxFreeSquares = freeSquares;
			}
			if(safeSquares > maxSafeSquares) {
				maxSafeSquares = safeSquares;
			}
			squares = 0;
			freeSquares = 0;
			safeSquares = 0;
			safe = true;
			
		}
		
		System.out.println("return");
		return new double[] {maxSquares,maxFreeSquares,maxSafeSquares};
	}
	
	private static void HelperFunction2() {
		for (Piece e : enemyPieces) {
			for(int i = 0; i<8; i++) {
				for(int j = 0; j<8; j++) {
					if(MGameUtility.distance(board, e, e.getX(), e.getY(), i, j, false) == 1) {
						threatenedByEnemy.add(new int[] {i,j});
					}
				}
			}
		}
	}
	
	public static int[] HelperFunction3(ArrayList<Piece> pieces) {
		int length = 0;
		int maxLength = 0;
		int maxBranches = 0;
		int visible = 0;
		int ownComplexity = 0;
		ArrayList<Piece> array;
		
		
		for(int i = 0; i<pieces.size(); i++) {
			
			array = MGameUtility.cloneArrayList(pieces);
			System.out.println("calling on "+pieces.get(i).getName());
			length = DefenseTree(array,pieces.get(i).getGid(),pieces.get(i).getGid());
			System.out.println(length);
			
			if(length > maxLength) {
				maxLength = length; 
			}
			if(branching > maxBranches) {
				maxBranches = branching;
			}
			branching  = 0;
		}
		ownComplexity = complexity;
		
		return new int[] {maxLength,maxBranches,visible,ownComplexity};
	}
	
	
	//#1 Relative material amount (pieces on the board)
	
	public static double RelM() {
		
		return ownPieces.size() - enemyPieces.size();
		
	}
	
	//#2 Relative material AVG values (ratio of whiteAVGValue and blackAVGValue)
	
	public static double RelMAVG() {
		
		double ownPieceValuesSum = 0;
		double enemyPieceValuesSum = 0;

		for(int i = 0; i<ownPieces.size(); i++) {
			ownPieceValuesSum += MGameUtility.unitValue(ownPieces.get(i));
		}
		for(int i = 0; i<enemyPieces.size(); i++) {
			enemyPieceValuesSum += MGameUtility.unitValue(enemyPieces.get(i));
		}
		
		return (double)(ownPieceValuesSum/ownPieces.size()) - (double)(enemyPieceValuesSum/enemyPieces.size());
		
	}
	
	//#3 Relative material values
	
	public static double RelMV() {
		
		double ownPieceValuesSum = 0;
		double enemyPieceValuesSum = 0;

		for(int i = 0; i<ownPieces.size(); i++) {
			ownPieceValuesSum += MGameUtility.unitValue(ownPieces.get(i));
		}
		for(int i = 0; i<enemyPieces.size(); i++) {
			enemyPieceValuesSum += MGameUtility.unitValue(enemyPieces.get(i));
		}
		
		return ownPieceValuesSum - enemyPieceValuesSum;
		
	}

	
	//#4 AVG piece value
	
	public static double RelPVAVG() {
		double sum = 0;
		
		for(Piece p : ownPieces) {
			sum += MSystem.RelPV(p);
		}
		return sum / ownPieces.size();

	}
	
	
	//#5 highest piece value
	
	public static double BestPiece() {
		double best = 0;
		
		for(Piece p : ownPieces) {
			if(MSystem.RelPV(p) > best) {
				best = MSystem.RelPV(p);
			}
		}
		return best;
	}
	
	//#6 Avg amount of threats to kings squares
	
	public static double KingSquaresSafetyMetric() {
		
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
		
		return (count*1.0) / t.size(); 
		
		
	}
	
	//#7  
	
	public static double RelDistanceFromDefault() {
		
		double ownDiff = 0;
		double enemyDiff = 0;
		
		for(Piece p : MGameUtility.ReturnAllPieces(board, whitesTurn)) {
			if(p.getGid() < 19) {
				if(!whitesTurn) {
					ownDiff += MGameUtility.distance(board, p, 0, p.getGid()%11, p.getX(), p.getY(), false);
				}
				else {
					enemyDiff += MGameUtility.distance(board, p, 0, p.getGid()%11, p.getX(), p.getY(), false);
				}
				
			}
			else if(p.getGid() < 27) {
				if(!whitesTurn) {
					ownDiff += MGameUtility.distance(board, p, 1, p.getGid()%19, p.getX(), p.getY(), false);
				}
				else {
					enemyDiff += MGameUtility.distance(board, p, 1, p.getGid()%19, p.getX(), p.getY(), false);
				}
				
			}
			else if(p.getGid() < 35) {
				if(whitesTurn) {
					ownDiff += MGameUtility.distance(board, p, 7, p.getGid()%27, p.getX(), p.getY(), false);
				}
				else {
					enemyDiff += MGameUtility.distance(board, p, 7, p.getGid()%27, p.getX(), p.getY(), false);
				}
				
			}
			else if(p.getGid() < 43){
				if(whitesTurn) {
					ownDiff += MGameUtility.distance(board, p, 6, p.getGid()%35, p.getX(), p.getY(), false);
				}
				else {
					enemyDiff += MGameUtility.distance(board, p, 6, p.getGid()%35, p.getX(), p.getY(), false);
				}
				
			}
		}
		
		return ownDiff / enemyDiff;
		
	}
	
	//#8 minimal enemy distance to own king 
	
	public static double MinDistKing_Enemy() {
		
		int minDistance = 100;
		int pieceDist = 0;
		
		int kingX = RuleSet.ReturnKingAndRookPositions(board,whitesTurn)[0];
		int kingY = RuleSet.ReturnKingAndRookPositions(board,whitesTurn)[1];
		
		
		for(Piece p : enemyPieces) {
			pieceDist = MGameUtility.distance(board, p, p.getX(), p.getY(), kingX, kingY, false);
			if(pieceDist < minDistance) {
				minDistance = pieceDist; 
			}

		}
		
		return minDistance;
	}
	
	//#9 minimal own distance to enemy king
	
	public static double MinDistKing_Own() {
		
		int minDistance = 100;
		int pieceDist = 0;
		
		int kingX = RuleSet.ReturnKingAndRookPositions(board,whitesTurn)[2];
		int kingY = RuleSet.ReturnKingAndRookPositions(board,whitesTurn)[3];

		
		for(Piece p : ownPieces) {
			pieceDist = MGameUtility.distance(board, p, p.getX(), p.getY(), kingX, kingY, false);
			if(pieceDist < minDistance) {
				minDistance = pieceDist; 
			}

		}
		
		return minDistance;
	}
	
	//#10 % of own pieces under threat
	
	public static double PercentThreat_Own() {
		
		double count = 0;

		for (Piece piece : whitePieces) {
			for(Piece enemyPiece : blackPieces) {
				if(MGameUtility.distance(board, enemyPiece, enemyPiece.getX(), enemyPiece.getY(), piece.getX(), piece.getY(), false) == 1){
					count++;
				}
			}
		}

			return count / whitePieces.length;

			for (Piece piece : blackPieces) {
				for(Piece enemyPiece : whitePieces) {
					if(MGameUtility.distance(board, enemyPiece, enemyPiece.getX(), enemyPiece.getY(), piece.getX(), piece.getY(), false) == 1){
						count++;
					}
				}
			}

			return count / blackPieces.length;
		
		
	}
	
	//#11 % of enemy pieces attacked
	
	public static double PercentThreat_Enemy() {
		
		double count = 0;
		
		if(whitesTurn) {
			for (Piece piece : whitePieces) {
				for(Piece enemyPiece : blackPieces) {
					if(MGameUtility.distance(board, piece, piece.getX(), piece.getY(), enemyPiece.getX(), enemyPiece.getY(), false) == 1){
						count++;
					}
				}
			}

			return count / whitePieces.length;
		}
		else {
			for (Piece piece : blackPieces) {
				for(Piece enemyPiece : whitePieces) {
					if(MGameUtility.distance(board, piece, piece.getX(), piece.getY(), enemyPiece.getX(), enemyPiece.getY(), false) == 1){
						count++;
					}
				}
			}

			return count / blackPieces.length;
		}
		
	}
	
	//#12 
	
	public static double AmountOfPieces() {
		return ownPieces.size();
	}
	

	
	//#13 no of adjacent empty squares
	
	public static double OpenSquareCount() {
		int count = 0;
		
		int x = 0;
		int y = 0;
		

		for ( x = 0; x < 8; x++) {
			for ( y = 0; y < 8; y++) {
				while(board[x][y] == null && count < 16) {
					count++;
					
					while(x + 1 < 8 && board[x + 1][y] == null) {
						x = (x + 1);
					}
					
					while(x - 1 > -1 && board[x - 1][y] == null) {
						x = (x - 1);
					}
					
					while(x + 1 < 8 &&board[x][y + 1] == null) {
						y = (y + 1);
					}
					
					while(y - 1 > -1 && board[x][y - 1] == null) {
						y = (y - 1);
					}
					
				}
			}
		}
		
		
		/*
		while(board[x][y] == null && x < 7) {
			while(board[x][y] != null && x < 7) {	
				x++;

			}
			count++;
			x++;
		}
		x--;
		while(board[x][y] == null) {
			
		}
			
			if(x-1 > -1) { 
				x--;
			}
			
			if(y+1 < 8 ) {
				y++;
			}
			
			if(y-1 > -1 ) {
				y--;
			}
		*/
			
		return count+1;
			
	}
	
	//#14 % of pieces defended
	
	public static double PercentDefended() {
		double perc = 0;
		
		for(Piece p : ownPieces) {
			for(Piece q : ownPieces) {
				if(MGameUtility.distance(board, q, q.getX(), q.getY(), p.getX(), p.getY(), false) == 1) {
					perc++;
				}
			}
		}
		return perc / ownPieces.size();
		
	}
	
	//#15 maximum squares available for a piece
	
	public static double MostSquaresAvailableForPiece() {
		return t[0];
	}
	
	//#16 maximum defenses for a piece
	
	public static double MostDefensesForPiece() {
		double defenses = 0;
		double maxDefenses = 0;
		
		for (Piece p : ownPieces) {
			for(Piece q : ownPieces) {
				if(MGameUtility.distance(board, q, q.getX(), q.getY(), p.getX(), p.getY(), false) == 1) {
					defenses++;
				}
			}
			if(defenses > maxDefenses) {
				maxDefenses = defenses;
			}
		}
		return maxDefenses;
		
	}
	
	//#17 maximal free squares for a piece
	
	public static double MostFreeSquaresForPiece() {
		return t[1];
	}
	
	
	
	//#18 maximum squares safe for a piece
	
	public static double MostSquaresSafeForPiece() {
		return t[2];
	}
	
	//#19 # of squares available for all pieces in total
	
	public static double CountAllAvailableSquares() {
		return allLegalSquares.size();
	}
	
	//#20 # of squares free for all pieces in total
	
	public static double CountAllFreeSquares() {
		return allFreeSquares.size();
	}
	
	//#21 # of squares safe for all pieces in total
	
	public static double CountAllSafeSquares() {
		return allSafeSquares.size();
	}
	
	
	//#22 move progression length = longest chain of defenses
	
	public static double MoveProgressionLength() {
		return t2[0];
	}

	//#23 move progression branching = 
	
	public static double MoveProgressionBranching() {
		return t2[1];
		
		
	}
	
	//#24 progression visible branches(PruningMethod, move) = amount of branches in the defending tree
	
	public static int CountProgressionVisibleBranches() {
		return t2[2];
	}
	
	//#25 complexity of moves ~ amount of computation needed to evaluate progression
	
	public static double PositionComplexity() {
		return t2[3] * t2[1];
	}
	
	//#26 complexity ratio
	
	public static double ComplexityRatio() {
		return (t2[3]*t2[1]) / (t3[3]*t3[1]);
	}
	

	
	public static int getFirstDefender(ArrayList<Piece> pieces,int init,int gid) {

		
		for(int j = 0; j<pieces.size(); j++) {
			
			
			
			if(		pieces.get(j).getGid() != gid &&
					MGameUtility.distance(board, pieces.get(j), pieces.get(j).getX(), pieces.get(j).getY(),
					MGameUtility.GetByGid(pieces, gid).getX(), MGameUtility.GetByGid(pieces, gid).getY(), false) == -1) {
				
				System.out.println(pieces.get(j).getName()+pieces.get(j).getGid()+" defends "+
					MGameUtility.GetByGid(pieces, gid).getName()+
					MGameUtility.GetByGid(pieces, gid).getGid());
				
				if(pieces.get(j).getGid() == init) {
					continue;
				}
				
				
				pieces.get(j).getGid();
			}

		}
		return -1; //not found
		
	}

	public static int DefenseTree(ArrayList<Piece> pieces,int initialGid,int defenderTarget) {

//		System.out.println(initialIndex+" "+defenderTarget);
//		System.out.println("defender target "+defenderTarget);
//		System.out.println("defender "+getFirstDefender(pieces,initialGid,defenderTarget));
//		System.out.println("init "+initialGid);
//		System.out.println("");
		
		
		int defender = getFirstDefender(pieces,initialGid,defenderTarget);
		
		
		if(defender != -1 && defender != initialGid) {
			if() {
				return DefenseTree(pieces,initialGid,defender) + 1;
			}
			else {
				initialGid = defender;
				defender = getFirstDefender(pieces,initialGid,defenderTarget) + 1;
				return DefenseTree(pieces,initialGid,defender) + 1;
				
			}
			
		}
		else if(defender == initialGid) {
			
		}
		else {
			return 0;
		}



		
		
	}
	
}

