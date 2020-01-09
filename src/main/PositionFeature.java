package main;

public class PositionFeature {

	
	
	
	//#1 Relative material amount
	
	public static double RelM() {
		
	}
	
	//#2 Relative material AVG values
	
	public static double RelMAVG() {
		
	}
	
	//#3 Relative material values
	
	public static double RelMV() {
		
	}

	//#4 Transient Material value for given piece 
	
	public static double TransMV_P() {
		
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
		
	}
	
	//#9 shortest own pieces distance to enemy king
	
	public static int MinDistKing_Own() {
		
	}
	
	//#10 % of own pieces under threat
	
	public static int PercentThreat_Own() {
		
	}
	
	//#11 % of own pieces safe 
	
	public static int PercentSafe() {
		
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
	
	
	
	public static Object[] callAll(int X, int Y, int tX, int tY, Piece p, boolean color, String move, Piece[][] board) {
		return new Object[] {RelM(), RelMAVG(), RelMV(), TransMV_P(),  Dist_Px2( X,  Y,  tX,  tY),
			 	 Dist_OK( X,  Y), Dist_EK( X,  Y),  MinDistKing_Enemy(), MinDistKing_Own(),
			 	 PercentThreat_Own(), PercentSafe(), PercentThreatK_Enemy(),
			 	 OwnVsEnemyThreatRatio(), PercentDefended(), SquaresAvailableForPiece(p) ,
			 	 SquaresFreeForPiece(p), SquaresSafeForPiece(p), AllAvailableSquares(color), 
			 	 AllFreeSquares(color), AllSafeSquares(color), MoveProgressionLength(move),
			 	 CountPrecedingMoves(move), MoveProgressionBranching(move), ProgressionVisibleBranches(1, move),
			 	 MoveComplexity(move), PositionComplexity(board)
		};
	}
	
	
	
	
}

