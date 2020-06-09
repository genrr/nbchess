package main;

import java.util.ArrayList;
import java.util.Random;

import javafx.application.Platform;


public class MGameUtility {
	
	private static int distance = 0;
	public static int[] distVector = new int[4];
	private static Path[] pathVector = new Path[10];
	private static Piece[][] board = null;
	static double[][] evalMatrix = new double[8][8];


	
	
public static int distance(Piece[][] b, Piece piece, int startX, int startY, int targetX, int targetY,boolean eval) {
	
	boolean canMoveDiagonally = false;
	board = b;
	int dist = 0;
	
	//System.out.println("testing distance between: "+piece.getName()+" at ("+piece.getX()+","+piece.getY()+") -> ("+targetX+","+targetY+")");
	
	// sX == tX && sY == tY, same square, return 1 for use in other functions, although technically,
	// distance != 1
	if(startX == targetX && startY == targetY) {
		return 1; 
	}
	

	
	if(piece.getName().contains("knight")) {
		dist = KnightMoveDist(startX,startY,targetX,targetY);
	}
	else if(piece.getName().contains("bishop")) {
		dist = BishopMoveDist(startX,startY,targetX,targetY);
	}
	else if(piece.getName().contains("rook")) {
		dist = RookMoveDist(startX,startY,targetX,targetY);
	}
	else if(piece.getName().contains("pawn")) {
		
			
		int[] p = RuleSet.nettyPater(startX, startY, targetX, targetY);
		
		
		//target square is diagonal
		if((piece.getColor() && ((p[0] == -1 && p[1] == 1) || (p[0] == -1 && p[1] == -1))) ||
		  (!piece.getColor() && ((p[0] == 1 && p[1] == -1) || (p[0] == 1 && p[1] == 1)))) {
			
			if(board[targetX][targetY] != null) {
				canMoveDiagonally = true;
			}
			else {
				//canMoveDiagonally == false
				//
				// 0  0  0
				//-2  1 -2
				// 2  2  2
				
				return -2;
			}
			
			
		}
			
			
			
		
		
		dist = PawnMoveDist(piece,startX,startY,targetX,targetY,canMoveDiagonally);
		
		//if distance is one && target square is not empty && not diagonal -> pawn cannot move to target, return 0
		if(dist == 1 && board[targetX][targetY] != null && targetY == startY) {

			return 0;

		}
		
	}
	else if(piece.getName().contains("queen")) {
		dist = QueenMoveDist(startX,startY,targetX,targetY);
	}
	else if(piece.getName().contains("king")) {
		dist = KingMoveDist(startX,startY,targetX,targetY);
	}
	
	if(eval) {
		distanceMatrix(piece.getColor());
	}
	
	//System.out.println("dist for "+piece.getName()+"from ("+startX+ ","+ startY +
	//		") -> ("+targetX+","+targetY+") = "+dist);
	
	//defending own piece, return -1 (only 1-distance moves)
	if(dist == 1 && b[targetX][targetY] != null && b[targetX][targetY].getColor() == piece.getColor()){
		return -1;
	}
	
	
	return dist;

}


//knight move distances

private static int KnightMoveDist(int X, int Y,int tX,int tY) {

	int[][] mm =   {{6,5,4,5,4,5,4,5,4,5,4,5,4,5,6},
					{5,4,5,4,3,4,3,4,3,4,3,4,5,4,5},
			  		{4,5,4,3,4,3,4,3,4,3,4,3,4,5,4},
			  		{5,4,3,4,3,2,3,2,3,2,3,4,3,4,5},
			  		{4,3,4,3,2,3,2,3,2,3,2,3,4,3,4},
			  		{5,4,3,2,3,4,1,2,1,4,3,2,3,4,5},
			  		{4,3,4,3,2,1,2,3,2,1,2,3,4,3,4},
			  		{5,4,3,2,3,2,3,0,3,2,3,2,3,4,5},
			  		{4,3,4,3,2,1,2,3,2,1,2,3,4,3,4},
			  		{5,4,3,2,3,4,1,2,1,4,3,2,3,4,5},
			  		{4,3,4,3,2,3,2,3,2,3,2,3,4,3,4},
			  		{5,4,3,4,3,2,3,2,3,2,3,4,3,4,5},
			  		{4,5,4,3,4,3,4,3,4,3,4,3,4,5,4},
			  		{5,4,5,4,3,4,3,4,3,4,3,4,5,4,5},
			  		{6,5,4,5,4,5,4,5,4,5,4,5,4,5,6}};
	

	return mm[7+(X-tX)][7+(Y-tY)];
	
	
  
}

//pawn move distances

private static int PawnMoveDist(Piece piece, int X, int Y,int tX,int tY, boolean capture) {

	
	/*
	 * black pawn move matrix
	 * if capturing, return distance from point (1,7)
	 * else, add 1 to (1,7) and return distance from point (0,7)
	 */
	if(!piece.getColor()) {
		if(tX <= X) {
			return 0;
		}
		int[][] mm={{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
				  	{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
				  	{0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
				  	{0,0,0,0,0,2,2,2,2,2,0,0,0,0,0},
				  	{0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
				  	{0,0,0,4,4,4,4,4,4,4,4,4,0,0,0},
				  	{0,0,5,5,5,5,5,5,5,5,5,5,5,0,0},
				  	{0,6,6,6,6,6,6,6,6,6,6,6,6,6,0}};
		

		if(capture) {
			//use (1,7)
			return mm[1+(tX-X)][7+(tY-Y)];
		}
		else {
			mm[1][7] = 1;
			
			//handle cases, where possibly target dist not incremented (X == 1, forward square empty, moving in a straight line, target square not 
			//reachable only diagonal moves after first forward move
			if(mm[0+(tX-X)][7+(tY-Y)] != 0 && tX - X != 1) {
				
				if(X != 1 || board[X+1][Y] != null) {
					mm[0+(tX-X)][7+(tY-Y)]++;
				}
				else {
					if(X == 1 && Math.abs(tY-Y) == Math.abs(tX-X) - 1 && tX-X != 1) {
						mm[0+(tX-X)][7+(tY-Y)]++;
					}
				}	
			}
			
			//use (0,7)
			return mm[0+(tX-X)][7+(tY-Y)];
		}
	}
	/*
	 * white pawn move matrix
	 * if capturing, return distance from point (6,7)
	 * else, add 1 to (6,7) and return distance from point (7,7)
	 */
	else {
		if(tX >= X) {
			return 0;
		}
		int[][] mm = {{0,6,6,6,6,6,6,6,6,6,6,6,6,6,0},
					  {0,0,5,5,5,5,5,5,5,5,5,5,5,0,0},
				      {0,0,0,4,4,4,4,4,4,4,4,4,0,0,0},
					  {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
					  {0,0,0,0,0,2,2,2,2,2,0,0,0,0,0},
					  {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
					  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
					  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
		
		/*
		 *int[][] mm={{0,7,6,6,6,6,6,6,6,6,6,6,6,7,0},
					  {0,0,6,5,5,5,5,5,5,5,5,5,6,0,0},
				      {0,0,0,5,4,4,4,4,4,4,4,5,0,0,0},
					  {0,0,0,0,4,3,3,3,3,3,4,0,0,0,0},
					  {0,0,0,0,0,3,2,2,2,3,0,0,0,0,0},
					  {0,0,0,0,0,0,2,1,2,0,0,0,0,0,0},
					  {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
					  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
		 * 
		 * 
		 * 
		 */
		
		/*
		 * pawn is allowed to move diagonally initially, start at (6,7)
		 */
		if(capture) {
			return mm[6+(tX-X)][7+(tY-Y)];
		}
		else {
			/* pawn moves initially hor/ver
			 * (6,7) dist is 1
			 * 
			 */
			mm[6][7] = 1;
			/*
			 * requested target square is theoretically reachable &&
			 * not moving one square, possible to increase target dist
			 */
			if(mm[7+(tX-X)][7+(tY-Y)] != 0 && X - tX != 1) {
				/*
				 * either not in start pos or something in the way
				 * -> increase target dist
				 */
				if(X != 6 || board[X-1][Y] != null) {
					mm[7+(tX-X)][7+(tY-Y)]++;
				}
				else {
					/* dont increment unless:
					 * pawn is on the start pos && y change equals x change - 1(target square is reachable in only diagonal moves after 1 forward move)
					 * -> increment dist to target
					 */
					if(X == 6 && Math.abs(tY-Y) == Math.abs(tX-X) - 1 && tX - X != -1) {
						mm[7+(tX-X)][7+(tY-Y)]++;
		
						
					}
				}
				

			}
			return mm[7+(tX-X)][7+(tY-Y)];
		}
	}
	


}

//king move distance

private static int KingMoveDist(int X, int Y,int tX,int tY) {

	
	int a = Math.abs(tY-Y);
	int b = Math.abs(tX-X);
	
	/*
	 * if both diffs in range 0,1, return 1
	 * else, return 0
	 */
	if(a <= 1 && b <= 1) {
		return 1;
	}
	return 0;
}


//rook move distance


private static int RookMoveDist(int X, int Y,int tX,int tY) {

	int i;
	int t1 = 0;
	int t2 = 0;
	int multX = 0;
	int multY = 0;
	
	
	if(tX == X){
		t1 = tY;	
		t2 = Y;
	}
	else if(tY == Y){
		t1 = tX;
		t2 = X;
	}
	else {
		return 0;
	}
	
	if(tX < X){
		multX = -1;
	}
	else if(tX > X){
		multX = 1;
	}
	if(tY < Y) {
		multY = -1;
	}
	else if(tY > Y) {
		multY = 1;
	}

	
	/*
	 * case where tX == X OR tY == Y -> only need to check that nothing's in the way
	 * 
	 * else,
	 * two lines need to be checked
	 * 
	 */
	if(tX == X){
		if(!CheckInBetween(X,Y,tX,tY)) {
			return 1;
		}
	}	
	
	else if(tY == Y){
		if(!CheckInBetween(X,Y,tX,tY)) {
			return 1;
		}
	}	
	else {
		if(!CheckInBetween(X,Y,multX*Math.abs(tX-X),Y) &&
		   !CheckInBetween(multX*Math.abs(tX-X),Y,tX,tY)) {
			return 2;
		}
		else if(!CheckInBetween(X,Y,X,multY*Math.abs(tY-Y)) &&
				!CheckInBetween(X,multY*Math.abs(tY-Y),tX,tY)){
			return 2;
		}
	}
	
	return 0;
	
	

}


//bishop move distance


private static int BishopMoveDist(int X, int Y,int tX,int tY) {


	int multX = 0;
	int multY = 0;

	//checks for temp variables

	if(tX < X && tY < Y){
		multX = -1;
		multY = -1;
	}
	else if(tX < X && tY > Y){
		multX = -1;
		multY = 1;
	}
	else if(tX > X && tY < Y){
		multX = 1;
		multY = -1;
	}
	else if(tX > X || tY > Y){
		multX = 1;
		multY = 1;
	}



	if(Math.abs(tY-Y) == Math.abs(tX-X)){

		//targeted square is reachable by diagonal movement && nothing is blocking the movement
		if(!CheckInBetween(X,Y,tX,tY)){
			return 1;
		}
		else{
			//targeted square is reachable by diagonal movement && something is blocking the movement
			return 0;
		}
	}
	//target square is reachable in two moves(?)
	else if (Math.abs(tY-Y) % 2 == 0 && Math.abs(tX-X) % 2 == 0) {
		
		int i,j,t1,t2,d1,d2,h1,h2;
		i = X;
		j = Y;
		
		if(multX == 0) {
			h1 = X + multY/2;
			h2 = Y + multY/2;
			if(h1 > -1 && h1 < 8 && h2 > -1 && h2 < 8) {
				if(!CheckInBetween(X, Y, h1, h2) && !CheckInBetween(h1, h2, tX, tY)) {
					return 2;
				}
			}
			h1 = X - multY/2;
			h2 = Y + multY/2;
			if(h1 > -1 && h1 < 8 && h2 > -1 && h2 < 8) {
				if(!CheckInBetween(X, Y, h1, h2) && !CheckInBetween(h1, h2, tX, tY)) {
					return 2;
				}
			}
			return 0;
		}
		else if(multY == 0) {
			h1 = X + multX/2;
			h2 = Y + multX/2;
			if(h1 > -1 && h1 < 8 && h2 > -1 && h2 < 8) {
				if(!CheckInBetween(X, Y, h1, h2) && !CheckInBetween(h1, h2, tX, tY)) {
					return 2;
				}
			}
			h1 = X + multX/2;
			h2 = Y - multX/2;
			if(h1 > -1 && h1 < 8 && h2 > -1 && h2 < 8) {
				if(!CheckInBetween(X, Y, h1, h2) && !CheckInBetween(h1, h2, tX, tY)) {
					return 2;
				}
			}
			return 0;
		}
		
		
		//System.out.println("before "+i+" "+j);

		
		while(i < 8 && j < 8 && i > -1 && j > -1 && Math.abs(tX-i) != Math.abs(tY-j)) {
			//we're about to go over the board
			if(i+multX == 8 || j+multY == 8 || i+multX == -1 || j+multY == -1) {
				break;
			}
			i += multX;
			j += multY;
		}
		
		
		t1 = i - X;
		t2 = j - Y;
		
		d1 = tX-X + -1*t1;
		d2 = tY-Y + -1*t2;
		
		//System.out.println("after "+i+" "+j);
		
		if((!CheckInBetween(X,Y,i,j) && !CheckInBetween(i,j,tX,tY)) ||
		   X+d1 < 8 && X+d1 > -1 && Y+d2 < 8 && Y+d2 > -1 && 
		   (!CheckInBetween(X,Y,X+d1,Y+d2) && !CheckInBetween(X+d1,Y+d2,tX,tY))) {
			return 2;
		}
		
		return 0;

	}
	//targeted square is not reachable by diagonal movement
	return 0;
	
	
}


//Queens distances


private static int QueenMoveDist(int X, int Y,int tX,int tY) {

	int i,j,t1,t2,d1,d2;
	int multX = 0;
	int multY = 0;

	//checks for temp variables
	if(tX < X && tY < Y){
		multX = -1;
		multY = -1;
	}
	else if(tX < X && tY > Y){
		multX = -1;
		multY = 1;
	}
	else if(tX > X && tY < Y){
		multX = 1;
		multY = -1;
	}
	else if(tX > X || tY > Y){
		multX = 1;
		multY = 1;
	}
	
	//1-move: rook move or bishop move
	if(((X == tX || Y == tY) && !CheckInBetween(X,Y,tX,tY)) || 
	(Math.abs(tY-Y) == Math.abs(tX-X) && !CheckInBetween(X,Y,tX,tY))){
		return 1;
	}
		

	//2-move e.g. (1,2) -> (5,0)
	else if((X != tX && Y != tY) && Math.abs(tY-Y)/Math.abs(tX-X) != 1) {
		
		
		//PATTERN A: diagonal + diagonal/line
		i = X;
		j = Y;
		
		while(i < 8 && j < 8 && i > -1 && j > -1 && (Math.abs(tX-i) != Math.abs(tY-j) && i != tX && j != tY)) {
			if(i+multX == 8 || j+multY == 8 || i+multX == -1 || j+multY == -1) {
				break;
			}
			i += multX;
			j += multY;
		}
		
		if(!CheckInBetween(X,Y,i,j) && !CheckInBetween(i,j,tX,tY)) {
			return 2;
		}
		
		if(Math.abs(tX-i) == Math.abs(tY-j)) {
			t1 = i - X;
			t2 = j - Y;
			
			d1 = tX-X + -1*t1;
			d2 = tY-Y + -1*t2;
			
			if((!CheckInBetween(X,Y,i,j) && !CheckInBetween(i,j,tX,tY)) ||
					   X+d1 < 8 && X+d1 > -1 && Y+d2 < 8 && Y+d2 > -1 && 
					   (!CheckInBetween(X,Y,X+d1,Y+d2) && !CheckInBetween(X+d1,Y+d2,tX,tY))) {
						return 2;
					}
		}
		
		
		//PATTERN B: line + diagonal/line
		int n = 2;
		
		while(n != 0){
			i = X; 
			j = Y;
			
			while(i < 8 && j < 8 && i > -1 && j > -1 && (Math.abs(tX-i) != Math.abs(tY-j) && i != tX && j != tY)){
				if(i+multX == 8 || j+multY == 8 || i+multX == -1 || j+multY == -1) {
					break;
				}
				
				if(n == 2) {
					i += multX;
				}
				else {
					j += multY;
				}
				
			}
			
			if(!CheckInBetween(X,Y,i,j) && !CheckInBetween(i,j,tX,tY) ) {
				return 2;
			}
			

			n--;
		}
		
		

	}
	
	return 0;
	
	

	
}


//Checks if there is a piece in between (X,Y) and (tX,tY), (tX,tY) has to be reachable in one move 
//by the given piece from (X,Y)

public static boolean CheckInBetween(int X, int Y, int tX, int tY){
	
	
	
	//set local variables:
	
	int t1; //local variable to hold temporary X value
	int t2; //local variable to hold temporary Y value
	int t = Math.max(Math.abs(tX-X),Math.abs(tY-Y)); //Complete movement, absolute value
	
	t1 = X; //set start temp x as X
	t2 = Y; //set start temp y as Y

	int i = 0; //init iterator i
	
	int multX = (int) Math.signum(tX-X);
	int multY = (int) Math.signum(tY-Y);
	
	
	//System.out.println("X:"+X+" Y:"+Y+" tX:"+tX+" tY:"+tY+" t: "+t);
	
	
	//iterate t times
	while(i < t){
		
		
		//either decrease or increase both temp vars by one, depending on the direction (multX, multY)
		t1 += multX;
		t2 += multY;
		i++;
		
		//System.out.println("t1: "+t1+" t2: "+t2);
		
		if(board[X][Y] != null && board[t1][t2] != null){
			
			if(board[t1][t2].getColor() != board[X][Y].getColor() && board[t1][t2].getName().contains("king")) {
				continue;
			}
			
			//own blocking piece along the line
			if(board[X][Y].getColor() == board[t1][t2].getColor()) {
				return true;
			}
			//enemy piece blocking the way
			else if(t1 != tX || t2 != tY){
				return true;
			}
			//enemy piece at the end
			else {
				return false;
			}
		}
		
	}

	
	return false;

}

	
	
	/*
	 * Data structure for computing distances between pieces 
	 * 
	 * [3][][]
	 * [][][]
	 * [][][]
	 * 
	 */
	public static void distanceMatrix(boolean color){

		ArrayList<Piece >p1 = ReturnAllPieces(board, !color);
		ArrayList<Piece >p2 = ReturnAllPieces(board, color);
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				evalMatrix[i][j] = pathBranchingEvaluation(p1,p2,i,j,color);
			}
		}
		
		
		
	}
	
	
	public static double pathBranchingEvaluation(ArrayList<Piece> p1, ArrayList<Piece> p2, int x, int y, boolean color) {
		

		
		for(Piece piece1 : p1) {
			//enemy piece attacks the turning point (temp,j)
			if(distance(board, piece1, piece1.getX(), piece1.getY(), x, y,false) == 1) {
				
				for(Piece piece2 : p2) {
					//our own piece protects the turning point (temp,j)
					if(distance(board, piece2, piece2.getX(), piece2.getY(), x, y,false) == 1) {
						//enemy piece value is less than our own -> bad move
						//enemy piece value is more or equal to our own -> good
						return unitValue(piece2)/unitValue(piece1);
					}
				}
				return 0;
			}
		}
		return 1;
		
		
	}
	
	
	
	public static ArrayList<int[]> getAllMoves(Piece[][] board, boolean white, int[] info) {
		
		ArrayList<int[]> list = new ArrayList<int[]>();
		ArrayList<Piece> pieces = ReturnAllPieces(board, white);
		
		for (Piece p : pieces) {
	
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if(p.getX() == i && p.getY() == j) {
						continue;
					}
					
					int t = RuleSet.validate(board, white, p.getX(), p.getY(), i, j, info);
					
					if(!(t == 1 || t == 2)) {
						list.add(new int[] {p.getX(),p.getY(),i,j,t});
						System.out.println(list.get(list.size()-1)[0]+ " "+
								list.get(list.size()-1)[1]+ " "+
								list.get(list.size()-1)[2]+ " "+
								list.get(list.size()-1)[3]+ " "+list.get(list.size()-1)[4]);
					}

				}
			}
		}
		

		
		return list;
	}
	
	
	
	public static Piece[][] generatePos(Piece[][] board, ArrayList<Piece> pieces1, ArrayList<Piece> pieces2, int k0, double[] diff, Objectives O, int optimizationLevel){
		
		Random r = new Random();
		int k = 5;
		int tempX;
		int tempY;
		int t = 36;
		Piece[][] pos = new Piece[8][8];
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				pos[i][j] = null;
			}
		}
		
		
		for (Piece p : pieces1) {
			
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					do {
						tempX = r.nextInt(8);
						tempY = r.nextInt(8);
						t--;
						
						if(t < 0) {
							pos[p.getX()][p.getY()] = p;
							break;
						}
						
					} while (distance(board,p,p.getX(),p.getY(),tempX,tempY,false) > k || board[tempX][tempY].getColor() == p.getColor());
					
					pos[tempX][tempY] = p;
				}
			}
		}
		
		t = 36;
		
		for (Piece p : pieces2) {
			
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					do {
						tempX = r.nextInt(8);
						tempY = r.nextInt(8);
						t--;
						
						if(t < 0) {
							pos[p.getX()][p.getY()] = p;
							break;
						}
						
					} while (distance(board,p,p.getX(),p.getY(),tempX,tempY,false) > k || board[tempX][tempY].getColor() == p.getColor());
					
					pos[tempX][tempY] = p;
					
				}
			}
		}
		
		return pos;
		
	}
	
	public static boolean checkCharacter(Piece[][] pos, int chr) {
		
		
		
	}
	
	
	public static int posDist(Piece[][] pos1, Piece[][] pos2) {
		
		
	}
	
	
	public static Piece[][][] lineConnect(Piece[][] pos1, Piece[][] pos2, ComplexSystem c){
		
		
		
		
	}
	
	public static boolean connectionSolver(Piece[][] pos1, Piece[][] pos2) {
		
	}
	
	
	
	public static double unitValue(Piece unit) {
		int v = 0;
		if(unit.getName().contains("knight") || unit.getName().contains("bishop")) {
			v = 3;
		}
		else if(unit.getName().contains("rook")) {
			v = 5;
		}
		else if(unit.getName().contains("queen")) {
			v = 9;
		}
		else if(unit.getName().contains("pawn")) {
			v = 1;
		}
		else if(unit.getName().contains("king")) {
			v = 255;
		}
		return v;
	}
	
	public static Piece GetByGid(ArrayList<Piece> list, int gid) {
		for(Piece p : list) {
			if(p.getGid() == gid) {
				return p;
			}
		}
		
		return new Piece("temp", -1, -1, false, -1, -1); //not found
	}
	
	public static ArrayList<Piece> ReturnAllPieces(Piece[][] board, boolean color) {
		ArrayList<Piece> returnTable = new ArrayList<Piece>();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {				
				if(board[i][j] != null && board[i][j].getColor() == color) {
					returnTable.add(board[i][j]);
					
				}
			}
		}
		
		return returnTable;
	}
	
	public static Piece[][] cloneArray(Piece[][] p){
		Piece[][] clone = new Piece[8][8];
		Piece q;
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				q = p[i][j];
				if(q != null) {
					clone[i][j] = new Piece(q.getName(),q.getId(),q.getGid(),q.getColor(),q.getX(),q.getY());
				}
				else {
					clone[i][j] = null;
				}
			}
		}
		
		return clone;
	}
	
	public static void UpdateCoords(Piece[][] board) {
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(board[i][j] != null && (board[i][j].getX() != i || board[i][j].getY() != j)) {
					board[i][j].setCoords(i, j);
				}
			}
		}
	}


	public static ArrayList<Piece> cloneArrayList(ArrayList<Piece> pieces) {
		ArrayList<Piece> p = new ArrayList<Piece>(pieces.size());
		
		for(int i = 0; i<pieces.size(); i++) {
			p.add(i,pieces.get(i));
			
		}
		return p;
	}


	public static Piece[][] nearRandomSearch(Piece[][] pos2, int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}


	public static Piece[][][] generateOptimizedSolutions(Piece[][][] posList, int closenessTld) {
		// TODO Auto-generated method stub
		return null;
	}
	
}


class Path{
	
	private int[][] pointList = null;
	
	public Path(int cap) {
		pointList = new int[2][cap];
	}
	
	
	public void addNode(int i, int[] point) {
		pointList[i] = point;
	}
	
	public int getPathLength() {
		int i = 0;
		while(pointList[i] != null) {
			i++;
		}
		return i;
	}

	
}

class Move{
	
	Piece piece = null;
	int tX;
	int tY;
	
	public Move(Piece p, int tX, int tY) {
		piece = p;
		this.tX = tX;
		this.tY = tY;
	}
	
	public Piece getPiece() {
		return piece;
	}
	
	public int getX() {
		return tX;
	}
	
	
	public int getY() {
		return tY;
	}
}
