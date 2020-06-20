package main;

import java.util.ArrayList;
import java.util.Arrays;


public class PositionFeature {
	
	private static Piece[][] board = null;
	private static ArrayList<Piece> ownPieces;
	private static ArrayList<Piece> enemyPieces;
	private static boolean whitesTurn;
	private static int branching = 0;
	private static ArrayList<int[]> allLegalSquares;
	private static ArrayList<int[]> allFreeSquares;
	private static ArrayList<int[]> allSafeSquares;
	private static ArrayList<int[]> threatenedByEnemy;
	private static double[] t = null;
	private static double[] t2 = null;
	private static double[] t3 = null;
	private static double[] t4 = null;
	private static int complexity = 1;
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
	

	public PositionFeature(Piece[][] b, boolean currentTurn) {
		board = b;
		whitesTurn = currentTurn;
		enemyPieces = MGameUtility.ReturnAllPieces(board, !currentTurn);
		ownPieces = MGameUtility.ReturnAllPieces(board, currentTurn);
		allLegalSquares = new ArrayList<int[]>();
		allFreeSquares = new ArrayList<int[]>();
		allSafeSquares = new ArrayList<int[]>();
		threatenedByEnemy = new ArrayList<int[]>();
		
		t = HelperFunction1();
		HelperFunction2();System.out.println("%");
		t2 = HelperFunction3(ownPieces);System.out.println("%");
		t3 = HelperFunction3(enemyPieces);System.out.println("%");
		t4 = HelperFunction4();
		
	}
	
	public static void reset() {
		allLegalSquares.clear();
		allFreeSquares.clear();
		allSafeSquares.clear();
		threatenedByEnemy.clear();
		t = null;
		t2 = null;
		t3 = null;
		t4 = null;
		complexity = 1;
		branching = 0;
		loops = 0;
		init = 0;
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
						//System.out.println("squares:"+squares);
						squares++;
						allLegalSquares.add(new int[] {i,j});
						//System.out.println("size"+allLegalSquares.size());
						
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
		int ownComplexity ;
		
		
		for(int i = 0; i<pieces.size(); i++) {
			//System.out.println("calling on "+pieces.get(i).getName());
			
			//init = pieces.get(i).getGid();
			depth = DTree(pieces.get(i).getGid());
			System.out.println("depth of tree starting on "+i+": "+depth);
			
			if(depth > maxDepth) {
				maxDepth = depth; 
			}
			
			System.out.println("branching after recursive: "+branching+" maxbranches: "+maxBranches);
			
			if(branching > maxBranches) {
				maxBranches = branching;
			}
			branching  = 0;
		}
		ownComplexity = complexity;
		ownLoops = loops;
		
		System.out.println("complexity: "+ownComplexity);
		
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
	
	public double RelM() {
		
		return ownPieces.size() - enemyPieces.size();
		
	}
	
	//#2 Relative material AVG values
	
	public double RelMAVG() {
		
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
	
	public double RelMV() {
		
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
	
	public double RelPVAVG() {
		double sum = 0;
		
		for(Piece p : ownPieces) {
			sum += MSystem.RelPV(board,p);
		}
		return sum / ownPieces.size();

	}
	
	
	//#5 highest piece value in relative to other own pieces
	
	public double BestPiece() {
		double best = 0;
		
		for(Piece p : ownPieces) {
			if(MSystem.RelPV(board,p) > best) {
				best = MSystem.RelPV(board,p);
			}
		}
		return best;
	}
	
	//#6 longest pawn chain
	
	public double LongestPawnChain() {
		ArrayList<Piece> pawns = new ArrayList<Piece>();
		int maxLength = 0;
		int length = 0;
		
		
		for(Piece p : ownPieces) {
			if((whitesTurn && p.getId() == 24) || (!whitesTurn && p.getId() == 17)){
				pawns.add(p);
			}
		}
		
		if(!pawns.isEmpty()) {
			length = 1;
		}
		
		
		int i1 = 1;
		int i2 = 1;
		int tempX;
		int tempY;
		
		
		
		for (Piece p : pawns) {
			
			length += DTree(p.getGid());
			if(length > maxLength) {
				maxLength = length;
			}
		}
		return maxLength;
	}
	
	
	
	//#7 distance of own pieces from start vs enemy pieces from start
	
	public double DistanceFromDefaultRelativeToEnemy() {
		
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
	
	public double MinDistKing_Enemy() {
		
		int minDistance = 100;
		int pieceDist = 0;
		
		int[] t = MGameUtility.getKingPos(board,whitesTurn);
		
		int kingX = t[0];
		int kingY = t[1];
		
		for(Piece p : enemyPieces) {
			pieceDist = MGameUtility.distance(board, p, p.getX(), p.getY(), kingX, kingY, false);
			if(pieceDist != 0 && pieceDist < minDistance) {
				minDistance = pieceDist; 
			}

		}
		
		return minDistance;
	}
	
	//#9 minimal own distance to enemy king
	
	public double MinDistKing_Own() {
		
		int minDistance = 100;
		int pieceDist = 0;
		
		int[] t = MGameUtility.getKingPos(board,!whitesTurn);
		
		int kingX = t[0];
		int kingY = t[1];

		
		for(Piece p : ownPieces) {
			pieceDist = MGameUtility.distance(board, p, p.getX(), p.getY(), kingX, kingY, false);
			if(pieceDist != 0 && pieceDist < minDistance) {
				minDistance = pieceDist; 
			}

		}
		
		return minDistance;
	}
	
	//#10 % of own pieces under threat
	
	public double PercentThreat_Own() {
		return t4[0] / ownPieces.size();	
	}
	
	//#11 % of enemy pieces attacked
	
	public double PercentThreat_Enemy() {
		return t4[1] / enemyPieces.size();		
	}
	
	//#12 Degree of which the threats are answered, e.g. knight threatens bishop, count++, pawn protects bishop,  count-- count stays the same
	//e.g. return amount of all unanswered threats
	
	public double TradeEfficiency() {
		return t4[2];
	}
	
	
	//#13 longest line of adjacent, legal and empty squares, "size of empty lanes" TODO: fix horrible implementation 
	
	public double OpenSquareCount() {
		
		int tX;
		int tY;
		int l = 0;
		
		boolean white = whitesTurn ? true : false;
		
		for(int i = 0; i < ownPieces.size(); i++) {
			if(!whitesTurn) {
				//is this piece rook/bishop/queen?
				if(ownPieces.get(i).getId() == 11 || ownPieces.get(i).getId() == 13
						|| ownPieces.get(i).getId() == 14 || ownPieces.get(i).getId() == 15) {
					//iterate over the whole board for legal squares for this piece
					for(int j1 = 0; j1 < 8; j1++) {
						for(int j2 = 0; j2 < 8; j2++) {
							if(MGameUtility.distance(board, ownPieces.get(i), ownPieces.get(i).getX(),
									ownPieces.get(i).getY(), j1, j2, false) == 1) {
								l++;
							}
						}
					}
				}
			}
			else {
				if(ownPieces.get(i).getId() == 18 || ownPieces.get(i).getId() == 20 || 
						ownPieces.get(i).getId() == 21 || ownPieces.get(i).getId() == 22) {
					//iterate over the whole board for legal squares for this piece
					for(int j1 = 0; j1 < 8; j1++) {
						for(int j2 = 0; j2 < 8; j2++) {
							if(MGameUtility.distance(board, ownPieces.get(i), ownPieces.get(i).getX(),
									ownPieces.get(i).getY(), j1, j2, false) == 1) {
								l++;
							}
						}
					}
				}
			}
			
		}
		
		
		return l;
		
//		for(int i = 0; i < allFreeSquares.size(); i++) {
//			tX = allFreeSquares.get(i)[0];
//			tY = allFreeSquares.get(i)[1];
//			
//			//(0,4)
//			for(int j = 0; j < 8; j++) {
//				if(tX == j) {
//					for(int k = 0; k < allFreeSquares.size(); k++) {
//						//next (1,7), skip
//						//same (0,4), skip
//						if(allFreeSquares.get(k)[0] != j || i == k) {
//							continue;
//						}
//						//found either (0,5) or (0,3)
//						else if(allFreeSquares.get(k)[0] == j && Math.abs(allFreeSquares.get(k)[1]-tY) == 1) {
//							l++;
//						}
//						
//						
//					}
//				}
//			}
//			
//	
//		}
		
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
			
		//count+1;
			
	}
	
	//#14 % of pieces defended
	
	public double PercentDefended() {
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
	
	public double MostSquaresAvailableForPiece() {
		return t[0];
	}
	
	//#16 amount of defending pieces for the most defended piece
	
	public double MostDefensesForPiece() {
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
	
	public double MostFreeSquaresForPiece() {
		return t[1];
	}
	
	
	
	//#18 maximum squares safe for a piece
	
	public double MostSquaresSafeForPiece() {
		return t[2];
	}
	
	//#19 # of squares available for all pieces in total
	
	public double CountAllAvailableSquares() {
		return allLegalSquares.size();
	}
	
	//#20 # of squares free for all pieces in total
	
	public double CountAllFreeSquares() {
		return allFreeSquares.size();
	}
	
	//#21 # of squares safe for all pieces in total
	
	public double CountAllSafeSquares() {
		return allSafeSquares.size();
	}
	
	
	//#22 move progression length = longest chain of defenses
	
	public double LongestChainOfDefenses() {
		return t2[0];
	}

	//#23 move progression branching
	
	public double ChainBranching() {
		return t2[1];
	}
	
	//#24 amount of defending loops in the tree
	
	public double CountDefenseLoops() {
		return t2[2];
	}
	
	//#25 complexity of position ~ amount of computation needed to evaluate progression
	
	public double PositionComplexity() {
		return t2[3] * t2[1];
	}
	
	//#26 complexity ratio = (own maxBranches * own complexity) / 
	// (enemy maxBranches * enemy complexity)
	
	public double ComplexityRatio() {
		return (t2[3]*t2[1]) / (t3[3]*t3[1]);
	}
	

	
	
	public static ArrayList<Integer> getAllDefenders(int pieceGid) {
		ArrayList<Integer> L = new ArrayList<Integer>();
		boolean color = false;

		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(board[i][j] != null && board[i][j].getGid() == pieceGid) {
					color = board[i][j].getColor();
				}
			}
		}
		//own pieces
		ArrayList<Piece> pieces = MGameUtility.ReturnAllPieces(board, color);
		
		for(int j = 0; j<pieces.size(); j++) {
			
			if(!MGameUtility.GetByGid(pieces, pieceGid).getName().equals("temp") &&
			   MGameUtility.distance(board, pieces.get(j), pieces.get(j).getX(), pieces.get(j).getY(),
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
		System.out.println("branching:"+branching);
		
		int max = 0;
		
		if(defenders.size() == 0) {
			return 1;
		}

		for(int j : defenders) {
			if(!line.contains(j)) {
				complexity++;
				System.out.println(max);
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

