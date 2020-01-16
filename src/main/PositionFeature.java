package main;

public class PositionFeature {

	
	private static Piece[][] board;
	private static Piece[] blackPieces;
	private static Piece[] whitePieces;
	private static int turnNumber;
	private static boolean whitesTurn;
	
	
	/*
	 * pass the board, turn number and current turn
	 * init local variables
	 * handle frequently needed arrays of pieces
	 */
	
	public static void initFeatures(Piece[][] b, boolean currentTurn, int turnN) {
		board = b;
		turnNumber = turnN;
		whitesTurn = currentTurn;
		blackPieces = (Piece[]) MGameStuff.ReturnAllPieces(board, false).toArray();
		whitePieces = (Piece[]) MGameStuff.ReturnAllPieces(board, true).toArray();
	}
	
	
	
	
	
	
	
	//#1 Relative material amount (pieces on the board)
	
	public static int RelM() {
		
		return whitePieces.length - blackPieces.length;
		
	}
	
	//#2 Relative material AVG values (ratio of whiteAVGValue and blackAVGValue)
	
	public static double RelMAVG() {
		
		double blackPieceValuesSum = 0;
		double whitePieceValuesSum = 0;

		for(int i = 0; i<blackPieces.length; i++) {
			blackPieceValuesSum += MGameStuff.unitValue(blackPieces[i]);
		}
		for(int i = 0; i<whitePieces.length; i++) {
			whitePieceValuesSum += MGameStuff.unitValue(whitePieces[i]);
		}
		
		return (double)(whitePieceValuesSum/whitePieces.length) - (double)(blackPieceValuesSum/blackPieces.length);
		
	}
	
	//#3 Relative material values
	
	public static double RelMV() {
		
		double blackPieceValuesSum = 0;
		double whitePieceValuesSum = 0;

		for(int i = 0; i<blackPieces.length; i++) {
			blackPieceValuesSum += MGameStuff.unitValue(blackPieces[i]);
		}
		for(int i = 0; i<whitePieces.length; i++) {
			whitePieceValuesSum += MGameStuff.unitValue(whitePieces[i]);
		}
		
		return whitePieceValuesSum - blackPieceValuesSum;
		
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
		
		double unitValue = MGameStuff.unitValue(p);
		double PiecesSum = RelM();
		double ValuesSum = RelMV();
		
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
	
	//#5 Distance between two arbitrary pieces
	
	public static int Dist_Px2(int X, int Y, int tX, int tY) {
		
	}
	
	//#6 Distance from (X,Y) to own King
	
	public static int Dist_OK(int X, int Y) {
		
	}
	
	//#7 Distance from (X,Y) to enemy King
	
	public static int Dist_EK(int X, int Y) {
		
	}
	
	//#8 shortest enemy pieces distance to own king 
	
	public static int MinDistKing_Enemy() {
		
		int minDistance = 100;
		int pieceDist = 0;
		
		int[] kingPos = RuleSet.ReturnKingAndRookPositions(whitesTurn);
		
		Piece[] array;
		
		
		
		if(whitesTurn) {
			array = blackPieces;
		}
		else {
			array = whitePieces;
		}
		
		for(Piece p : array) {
			pieceDist = MGameStuff.distance(p, p.getX(), p.getY(), kingPos[0], kingPos[1]);
			if(pieceDist < minDistance) {
				minDistance = pieceDist; 
			}

		}
		
		return minDistance;
	}
	
	//#9 shortest own pieces distance to enemy king
	
	public static int MinDistKing_Own() {
		
		int minDistance = 100;
		int pieceDist = 0;
		
		int[] kingPos = RuleSet.ReturnKingAndRookPositions(whitesTurn);
		
		Piece[] array;

		if(whitesTurn) {
			array = whitePieces;
		}
		else {
			array = blackPieces;
		}
		
		for(Piece p : array) {
			pieceDist = MGameStuff.distance(p, p.getX(), p.getY(), kingPos[2], kingPos[3]);
			if(pieceDist < minDistance) {
				minDistance = pieceDist; 
			}

		}
		
		return minDistance;
	}
	
	//#10 % of own pieces under threat
	
	public static double PercentThreat_Own() {
		
		
		
	}
	
	//#11 % of own pieces safe 
	
	public static double PercentSafe() {
		
	}
	
	//#12 % of enemy pieces attacked after k moves
	
	public static int PercentThreatK_Enemy() {
		
	}
	
	//#13 % of own pieces under threat vs % of enemy pieces attacked 
	
	public static int OwnVsEnemyThreatRatio() {
		
	}
	
	//#14 % of pieces defended
	
	public static int PercentDefended() {
		
	}
	
	//#15 # of squares available for given piece P in point X,Y
	
	public static int SquaresAvailableForPiece(Piece p) {
		
		
	}
	
	//#16 # of squares free for given piece P in point X,Y
	
	public static int SquaresFreeForPiece(Piece p) {
		
	}
	
	//#17 # of squares safe for given piece P in point X,Y
	
	public static int SquaresSafeForPiece(Piece p) {
		
	}
	
	//#18 # of squares available for all pieces in total
	
	public static int AllAvailableSquares(boolean side) {
		
	}
	
	//#19 # of squares free for all pieces in total
	
	public static int AllFreeSquares(boolean side) {
		
	}
	
	//#20 # of squares safe for all pieces in total
	
	public static int AllSafeSquares(boolean side) {
		
	}
	
	//#21 move progression length
	
	public static int MoveProgressionLength(String move) {
	
	}
	
	//#22 amount of preceding moves +1 
	
	public static int CountPrecedingMoves(String move) {
		
	}
	
	//#23 move progression branching
	
	public static int MoveProgressionBranching(String move) {
		
	}
	
	//#24 progression visible branches(PruningMethod, move)
	
	public static int ProgressionVisibleBranches(int pruningMethod, String move) {
		
	}
	
	//#25 complexity of moves
	
	public static float MoveComplexity(String move) {
		
	}
	
	//#26 complexity of positions
	
	public static float PositionComplexity(Piece[][] board) {
		
	}
	
	
	
	public static void writeAll() {
		 RealtimeFunction.storeFeatureValues(1, RelM(), turnNumber);
		 RealtimeFunction.storeFeatureValues(2, RelMAVG(), turnNumber);
		 RealtimeFunction.storeFeatureValues(3, RelMV(), turnNumber);
		 RealtimeFunction.storeFeatureValues(8, MinDistKing_Enemy(), turnNumber);
		 RealtimeFunction.storeFeatureValues(9, MinDistKing_Own(), turnNumber);
		 RealtimeFunction.storeFeatureValues(10, PercentThreat_Own(), turnNumber);
		 RealtimeFunction.storeFeatureValues(11, PercentSafe(), turnNumber);
		 RealtimeFunction.storeFeatureValues(13, OwnVsEnemyThreatRatio(), turnNumber);
		 RealtimeFunction.storeFeatureValues(14, PercentDefended(), turnNumber);
		 RealtimeFunction.storeFeatureValues(18, AllAvailableSquares(whitesTurn), turnNumber);
		 RealtimeFunction.storeFeatureValues(19, AllFreeSquares(whitesTurn), turnNumber);
		 RealtimeFunction.storeFeatureValues(20, AllSafeSquares(whitesTurn), turnNumber);
		 RealtimeFunction.storeFeatureValues(26, PositionComplexity(board), turnNumber);
	}
	
	
	
	
}

