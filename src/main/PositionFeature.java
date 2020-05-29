package main;

import java.util.ArrayList;
import java.util.Arrays;


public class PositionFeature {

	
	private static Piece[][] board = null;
	private static ArrayList<Piece> ownPieces = new ArrayList<Piece>();
	private static ArrayList<Piece> enemyPieces = new ArrayList<Piece>();
	private static int turnNumber;
	private static boolean whitesTurn;
	private static int branching = 0;
	
	//private static double f10;
	//private static double f11;
	private static ArrayList<int[]> allLegalSquares = new ArrayList<int[]>();
	private static ArrayList<int[]> allFreeSquares = new ArrayList<int[]>();
	private static ArrayList<int[]> allSafeSquares = new ArrayList<int[]>();
	private static ArrayList<int[]> threatenedByEnemy = new ArrayList<int[]>();
	private static double[] t = null;
	private static double[] t2 = null;
	private static double[] t3 = null;
	private static double[] t4 = null;
	private static int complexity = 0;
	private static int loops = 0;
	
	private static ArrayList<Integer> line = new ArrayList<Integer>();
	private static int init = 0;
	
	
	/*
	 * pass the board, turn number and current turn
	 * init local variables
	 * compute frequently needed arrays of pieces
	 * 
	 * 
	 * do we need
	 * 	 * q = amount of pieces / amount of pieces(opponent)
	 * p = piece value sum / piece value sum(opponent)
	 * r = relative piece value sum / relative piece value sum(opponent)
	 * 
	 * some feature of qpr?
	 */
	

	public static void initFeatures(Piece[][] b, boolean currentTurn, int turnN) {
		board = b;
		turnNumber = turnN;
		whitesTurn = currentTurn;

		enemyPieces = MGameUtility.ReturnAllPieces(board, !currentTurn);
		ownPieces = MGameUtility.ReturnAllPieces(board, currentTurn);
		
		
		t = new double[] {2,1,1};//HelperFunction1();
		HelperFunction2();
		t2 = new double[] {1,1,1,1};//HelperFunction3(ownPieces);
		t3 = new double[] {1,1,1,1};//HelperFunction3(enemyPieces);
		t4 = HelperFunction4();
		
	}
	
	
	private static double[] HelperFunction1() {
		double squares = 0;
		double maxSquares = 0;
		double freeSquares = 0;
		double maxFreeSquares = 0;
		double safeSquares = 0;
		double maxSafeSquares = 0;
		boolean safe = true;
		
		
		for (Piece p : ownPieces) {
			
			for(int i = 0; i<8; i++) {
				for(int j = 0; j<8; j++) {
					
					if(MGameUtility.distance(board, p, p.getX(), p.getY(), i, j, false) == 1) {
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
	
	public static double[] HelperFunction3(ArrayList<Piece> pieces) {
		int depth = 0;
		int maxDepth = 0;
		int maxBranches = 0;
		int ownLoops = 0;
		int ownComplexity = 0;
		
		
		for(int i = 0; i<pieces.size(); i++) {
			//System.out.println("calling on "+pieces.get(i).getName());
			
			init = pieces.get(i).getGid();
			depth = DTree(pieces.get(i).getGid());
			System.out.println("depth of tree starting on "+i+": "+depth);
			
			if(depth > maxDepth) {
				maxDepth = depth; 
			}
			if(branching > maxBranches) {
				maxBranches = branching;
			}
			branching  = 0;
		}
		ownComplexity = complexity;
		ownLoops = loops;
		
		return new double[] {maxDepth,maxBranches,ownLoops,ownComplexity};
	}

	public static double[] HelperFunction4() {
		double tradeCount = 0;
		double attacked = 0;
		double underThreat = 0;
		
		for (Piece piece : ownPieces) {
			for(Piece enemyPiece : enemyPieces) {
				if(MGameUtility.distance(board, enemyPiece, enemyPiece.getX(), enemyPiece.getY(),
						piece.getX(), piece.getY(), false) == 1){
					underThreat++;
					tradeCount++;
					
					
					for (Piece piece2 : ownPieces) {
						
						if(MGameUtility.distance(board, piece2, piece2.getX(), piece2.getY(),
							piece.getX(), piece.getY(), false) == -1){
							tradeCount--;
						}
					}
				}
			}
		}
		
		for (Piece enemyPiece : enemyPieces) {
			for(Piece ownPiece : ownPieces) {
				if(MGameUtility.distance(board, ownPiece, ownPiece.getX(), ownPiece.getY(),
						enemyPiece.getX(), enemyPiece.getY(), false) == 1){
					attacked++;
				}
			}
		}
		
		
		
		return new double[] {underThreat,attacked,tradeCount};
		
	}
	
	
	//#1 Relative material amount (pieces on the board)
	
	public static double RelM() {
		
		return ownPieces.size() - enemyPieces.size();
		
	}
	
	//#2 Relative material AVG values
	
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

	
	//#4 AVG piece value relative to other own pieces
	
	public static double RelPVAVG() {
		double sum = 0;
		
		for(Piece p : ownPieces) {
			sum += MSystem.RelPV(p);
		}
		return sum / ownPieces.size();

	}
	
	
	//#5 highest piece value in relative to other own pieces
	
	public static double BestPiece() {
		double best = 0;
		
		for(Piece p : ownPieces) {
			if(MSystem.RelPV(p) > best) {
				best = MSystem.RelPV(p);
			}
		}
		return best;
	}
	
	//#6
	
	
	
	//#7 distance of own pieces from start vs enemy pieces from start
	
	public static double DistanceFromDefaultRelativeToEnemy() {
		
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
		
		if(ownDiff == 0) {
			ownDiff = 1;
		}
		if(enemyDiff == 0) {
			enemyDiff = 1;
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
		return t4[0] / ownPieces.size();	
	}
	
	//#11 % of enemy pieces attacked
	
	public static double PercentThreat_Enemy() {
		return t4[1] / enemyPieces.size();		
	}
	
	//#12 Degree of which the threats are answered, e.g. knight threatens bishop, count++, pawn protects bishop,  count-- count stays the same
	//e.g. return amount of all unanswered threats
	
	public static double TradeEfficiency() {
		return t4[2];
	}
	
	
	//#13 no of adjacent empty squares
	
	public static double OpenSquareCount() {
		/*
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
		
		*/
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
			
		return 1;//count+1;
			
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
	
	//#15 maximum legal squares available for a piece
	
	public static double MostSquaresAvailableForPiece() {
		return t[0];
	}
	
	//#16 amount of defending pieces for the most defended piece
	
	public static double MostDefensesForPiece() {
		double defenses = 0;
		double maxDefenses = 0;
		
		for (Piece p : ownPieces) {
			for(Piece q : ownPieces) {
				if(MGameUtility.distance(board, q, q.getX(), q.getY(), p.getX(), p.getY(), false) == -1) {
					defenses++;
				}
			}
			if(defenses > maxDefenses) {
				maxDefenses = defenses;
			}
			defenses = 0;
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
	
	//#24 amount of defending loops in the tree
	
	public static double CountProgressionVisibleBranches() {
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
	

	
	
	public static ArrayList<Integer> getAllDefenders(int pieceGid) {
		ArrayList<Integer> L = new ArrayList<Integer>();
		ArrayList<Piece> pieces = MGameUtility.ReturnAllPieces(board, whitesTurn);
		
		for(int j = 0; j<pieces.size(); j++) {
			
			if(MGameUtility.distance(board, pieces.get(j), pieces.get(j).getX(), pieces.get(j).getY(),
			   MGameUtility.GetByGid(pieces, pieceGid).getX(), MGameUtility.GetByGid(pieces, pieceGid).getY(), false) == -1) {
				L.add(pieces.get(j).getGid());
			}

		}
		return L;
		
	}

	/*
	 * Recursive function for computing "defense tree", where node n (=Piece p) has child node m, if m has distance of 1 to n
	 * 
	 * using DTree(), we compute the depth of the tree(22),
	 * sum of all branches across all pieces (23)
	 * amount of loops(24)
	 * sum of all steps taken when evaluating DTree() for all own pieces multiplied by sum of branches = complexity (25)
	 * ratio of complexity between ownPieces and enemyPieces (26)
	 */
	
	public static int DTree(int i) {
		ArrayList<Integer> defenders = getAllDefenders(i);
		line.add(i);
		branching += defenders.size();
		
		int max = 0;
		
		if(defenders.size() == 0) {
			return 1;
		}

		for(int j : defenders) {
			if(!line.contains(j)) {
				complexity++;
				max = Math.max(max, DTree(j));
				if(i == init) {
					line.clear();
					line.add(i);
				}
			}
			else {
				loops++;
			}

		}
		
		return max + 1;
		
	}
	
	/* DTree(0)
	 * defenders = [1,5,7,9];
	 * line = [0]
	 * max = 0; 
	 * size = 4 != 0
	 * loop1: max = math.max(0,DTree(1) = 1), max = 1
	 * loop5: max = math.max(1,DTree(5) = 2), max = 2
	 * loop7: max = math.max(2,DTree(7) = 2), max = 2
	 * loop9: max = math.max(2,DTree(9) = 3), max = 3
	 * return 4;
	 * 
	 * 
	 * DTree(1)
	 * defenders = [];
	 * line = [0,1];
	 * max = 0;
	 * size == 0, return 1
	 * 
	 * DTree(5)
	 * defenders = [2,3,4];
	 * line = [0,5]
	 * max = 0;
	 * size == 3 != 0
	 * loop2: max = math.max(0,DTree(2) = 1), max = 1
	 * loop3: max = math.max(1,DTree(3) = 1), max = 1
	 * loop4: max = math.max(1,DTree(4) = 1), max = 1
	 * return 2;
	 * 
	 * 
	 * DTree(2)
	 * defenders = [];
	 * line = [0,2]
	 * max = 0;
	 * size == 0, return 1
	 *
	 * DTree(3)
	 * defenders = [];
	 * line = [0,3]
	 * max = 0;
	 * size == 0, return 1
	 * 
	 * DTree(4)
	 * defenders = [];
	 * line = [0,4]
	 * max = 0;
	 * size == 0, return 1
	 * 
	 * DTree(7)
	 * defenders = [1]
	 * line = [0,7]
	 * max = 0
	 * size = 1 != 0
	 * loop1: max = math.max(0,DTree(1) = 1), max = 1
	 * return 2
	 * 
	 * DTree(9)
	 * defenders = [11,2,3]
	 * line = [0,9]
	 * max = 0
	 * size = 3 != 0
	 * loop11: max = math.max(0,DTree(11) = 2), max = 2
	 * loop2: max = math.max(2,DTree(2) = 1), max = 2
	 * loop3: max = math.max(2,DTree(3) = 1), max = 2
	 * return 3;
	 * 
	 * DTree(11)
	 * defenders = [4,9,6]
	 * line = [0,9,11]
	 * max = 0
	 * size = 3 != 0
	 * 4 not in [0,9,11], loop4: max = math.max(0,DTree(4) = 1), max = 1
	 * 9 found in [0,9,11], continue;
	 * 6 not in [0,9,11], loop6: max = math.max(1,DTree(6) = 1), max = 1
	 * return 2;
	 * 
	 * DTree(6)
	 * defenders = [11,0]
	 * line = [0,9,11,6]
	 * max = 0
	 * size = 2 != 0
	 * 11 found in [0,9,11,6], continue;
	 * 0 found in [0,9,11,6], continue;
	 * return 1;
	 * 
	 */
	
	
}

