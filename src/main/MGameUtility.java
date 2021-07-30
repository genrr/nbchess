package main;

import java.util.ArrayList;
import java.util.Random;

import javafx.application.Platform;


public class MGameUtility {

	private static int distance = 0;
	public static int[] distVector = new int[4];
	private static Path[] pathVector = new Path[10];
	private static int[][][] board = null;
	static double[][] evalMatrix = new double[8][8];
	private static boolean defend;




	public static int distance(int[][][] b, int[] piece, int startX, int startY, int targetX, int targetY,boolean eval) {

		defend = false;
		boolean canMoveDiagonally = false;
		board = b;
		int dist = 0;
		int id = piece[0]; 

		if(startX == -1 && startY == -1) {
			return 0;
		}

		//System.out.println("testing distance between: "+piece.getName()+" at ("+piece.getX()+","+piece.getY()+") -> ("+targetX+","+targetY+")");

		// sX == tX && sY == tY, same square, return 0
		if((startX == targetX && startY == targetY) || (targetX == -1 && targetY == -1)) {
			return 0; 
		}


		if(id == 12 || id == 19) {
			dist = KnightMoveDist(startX,startY,targetX,targetY);
			if(dist == 1 && board[targetX][targetY] != null && board[targetX][targetY][2] == piece[2]) {
				return -1;
			}
		}
		else if(id == 13 || id == 14 || id == 20 || id == 21) {
			dist = BishopMoveDist(startX,startY,targetX,targetY);
		}
		else if(id == 11 || id == 18) {
			dist = RookMoveDist(startX,startY,targetX,targetY);
		}
		else if(id == 17 || id == 24) {

			int[] p = RuleSet.nettyPater(startX, startY, targetX, targetY);

			//target square is diagonal
			if((piece[2] == 1 && ((p[0] == -1 && p[1] == 1) || (p[0] == -1 && p[1] == -1))) ||
					(piece[2] == 0 && ((p[0] == 1 && p[1] == -1) || (p[0] == 1 && p[1] == 1)))) {

				if(board[targetX][targetY] != null) {
					// 0  p  0		0  p  0
					// 1  1  1		1  1  1
					// 2  2  2		2  1  2
					canMoveDiagonally = true;

					//pawn defends a piece
					if(board[targetX][targetY][2] == piece[2]) {
						//defending own piece, return -1
						return -1;

					}

				}
				else {
					//canMoveDiagonally == false
					// 0  p  0		0  p  0
					//-2  1 -2	   -2  1 -2
					// 2  2  2		2  1  2
					return -2;
				}

			}

			dist = PawnMoveDist(piece,startX,startY,targetX,targetY,canMoveDiagonally);

			//pawns cannot capture forwards
			if(dist == 1 && board[targetX][targetY] != null && targetY == startY) {
				// 0  p  0		0  p  0
				// 1  @  1		1  @  1
				// 0  0  0		0  0  0
				return 0;
			}


		}
		else if(id == 15 || id == 22) {
			dist = QueenMoveDist(startX,startY,targetX,targetY);
		}
		else if(id == 16 || id == 23) {
			dist = KingMoveDist(startX,startY,targetX,targetY);
			if(dist == 1 && board[targetX][targetY] != null && board[targetX][targetY][2] == piece[2]) {
				return -1;
			}
		}

		if(eval) {
			//distanceMatrix(piece[2]);
		}
		//	
		//	System.out.println("dist for "+piece.getName()+"from ("+startX+ ","+ startY +
		//			") -> ("+targetX+","+targetY+") = "+dist);

		if(defend && dist == 1) {
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

	private static int PawnMoveDist(int[] piece, int X, int Y,int tX,int tY, boolean capture) {


		/*
		 * black pawn move matrix
		 * if capturing, return distance from point (1,7)
		 * else, add 1 to (1,7) and return distance from point (0,7)
		 */
		if(piece[2] == 0) {
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

				if(board[t1][t2][2] != board[X][Y][2] && (board[t1][t2][0] == 16 || board[t1][t2][0] == 23)) {
					continue;
				}

				if(board[X][Y][2] == board[t1][t2][2]) {
					//own blocking piece along the line
					if((t1 != tX || t2 != tY)) {
						//System.out.println("own piece between");
						return true;
					}
					//own piece at end, can move, set defending flag
					else {
						defend = true;
						//System.out.println("defend");
						return true;
					}

				}
				else {
					//enemy piece blocking the way
					if(t1 != tX || t2 != tY){
						//System.out.println("enemy blocking between");
						return true;
					}
					//enemy piece at the end, can capture
					else {
						//System.out.println("capture at the end");
						return false;
					}
				}

			}

		}

		return false;

	}

	public static ArrayList<int[]> PaintLastMove(int[][][] board, int X, int Y, int tX, int tY){
		System.out.println("hi");

		ArrayList<int[]> l = new ArrayList<>();
		//set local variables:

		int t1; //local variable to hold temporary X value
		int t2; //local variable to hold temporary Y value
		int t = Math.max(Math.abs(tX-X),Math.abs(tY-Y)); //Complete movement, absolute value	
		t1 = X; //set start temp x as X
		t2 = Y; //set start temp y as Y
		int i = 0; //init iterator i	
		int multX = (int) Math.signum(tX-X);
		int multY = (int) Math.signum(tY-Y);	

		System.out.println("X:"+X+" Y:"+Y+" tX:"+tX+" tY:"+tY+" t: "+t);

		if(board[X][Y][0] == 12 || board[X][Y][0] == 19) {
			l.add(new int[] {X,Y});
			l.add(new int[] {tX,tY});
			return l;
		}


		//iterate t times
		while(i < t){	
			//either decrease or increase both temp vars by one, depending on the direction (multX, multY)
			t1 += multX;
			t2 += multY;
			i++;

			System.out.println("t1: "+t1+" t2: "+t2);		
			l.add(new int[] {t1,t2});

		}

		return l;

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


	public static double pathBranchingEvaluation(ArrayList<int[]> p1, ArrayList<int[]> p2, int x, int y, boolean color) {



		for(int[] piece1 : p1) {
			//enemy piece attacks the turning point (temp,j)
			if(distance(board, piece1, piece1[3], piece1[4], x, y,false) == 1) {

				for(int[] piece2 : p2) {
					//our own piece protects the turning point (temp,j)
					if(distance(board, piece2, piece2[3], piece2[4], x, y,false) == 1) {
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



	public static ArrayList<int[]> getAllMoves(int[][][] board, int white) {

		ArrayList<int[]> list = new ArrayList<int[]>();
		ArrayList<int[]> pieces = ReturnAllPieces(board, white);
		char promotion = 0;
		int capture;

		for (int[] p : pieces) {

			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if(p[3] == i && p[4] == j) {
						continue;
					}

					
					int t = RuleSet.validate(board, white, p[3], p[4], i, j);


					if(!(t == 1 || t == 2)) {

						capture = 0;

						if(board[i][j] != null || t == 11) {
							capture = 1;
						}



						if(t == 12) {
							list.add(new int[] {p[3],p[4],i,j,t,'B',board[8][0][0],board[8][1][0],capture});
							list.add(new int[] {p[3],p[4],i,j,t,'N',board[8][0][0],board[8][1][0],capture});
							list.add(new int[] {p[3],p[4],i,j,t,'R',board[8][0][0],board[8][1][0],capture});
							list.add(new int[] {p[3],p[4],i,j,t,'Q',board[8][0][0],board[8][1][0],capture});
						}
						list.add(new int[] {p[3],p[4],i,j,t,promotion,board[8][0][0],board[8][1][0],capture});

//						System.out.println(list.get(list.size()-1)[0]+ " "+
//								list.get(list.size()-1)[1]+ " "+
//								list.get(list.size()-1)[2]+ " "+
//								list.get(list.size()-1)[3]+ " "+list.get(list.size()-1)[4]);
					}

				}
			}
		}



		return list;
	}


	public static int[] getKingPos(int[][][] board,int turn) {
		int[] t = new int[2];

		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(board[i][j] != null && (board[i][j][0] == 16 || board[i][j][0] == 23)) {
					if(board[i][j][2] == turn) {
						t[0] = i;
						t[1] = j;
					}
				}
			}
		}

		return t;
	}

	public static int[][][] generatePos(int[][][] board, ArrayList<int[]> pieces1, ArrayList<int[]> pieces2, int k, double[] diff){

		Random r = new Random();
		int tempX;
		int tempY;
		int t = 36;
		int[][][] pos = new int[8][8][];

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				pos[i][j] = null;
			}
		}


		for (int[] p : pieces1) {

			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					do {
						tempX = r.nextInt(8);
						tempY = r.nextInt(8);
						t--;

						if(t < 0) {
							pos[p[3]][p[4]] = p;
							break;
						}

					} while (distance(board,p,p[3],p[4],tempX,tempY,false) > k || board[tempX][tempY][2] == p[2]);

					pos[tempX][tempY] = p;
				}
			}
		}

		t = 36;

		for (int[] p : pieces2) {

			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					do {
						tempX = r.nextInt(8);
						tempY = r.nextInt(8);
						t--;

						if(t < 0) {
							pos[p[3]][p[4]] = p;
							break;
						}

					} while (distance(board,p,p[3],p[4],tempX,tempY,false) > k || board[tempX][tempY][2] == p[2]);

					pos[tempX][tempY] = p;

				}
			}
		}

		return pos;

	}

	public static boolean checkCharacter(Piece[][] pos, int chr) {



	}


	public static double posDist(Piece[][] pos1, Piece[][] pos2) {

		double a = 0;
		int gid = 0;



		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(pos1[i][j] != null) {
					gid = pos1[i][j].getGid();

					for (int j1 = 0; j1 < 8; j1++) {
						for (int j2 = 0; j2 < 8; j2++) {

							if(pos2[j1][j2] != null && pos2[j1][j2].getGid() == gid) {
								a += distance(pos1, pos1[i][j], i, j, j1, j2, false);
							}
						}
					}
				}

			}
		}

		return a;

	}


	public static Piece[][][] lineConnect(Piece[][] pos1, Piece[][] pos2, ComplexSystem c){




	}

	public static boolean connectionSolver(Piece[][] pos1, Piece[][] pos2) {

	}



	public static double unitValue(int[] unit) {
		int v = 0;
		if(unit[0] == 12 || unit[0] == 19 || unit[0] == 13 || unit[0] == 14 || unit[0] == 20 || unit[0] == 21) {
			v = 3;
		}
		else if(unit[0] == 11 || unit[0] == 18) {
			v = 5;
		}
		else if(unit[0] == 15 || unit[0] == 22) {
			v = 9;
		}
		else if(unit[0] == 17 || unit[0] == 24) {
			v = 1;
		}
		else if(unit[0] == 16 || unit[0] == 23) {
			v = 255;
		}
		return v;
	}

	public static int[] GetByGid(ArrayList<int[]> list, int gid) {
		for(int[] p : list) {
			if(p[1] == gid) {
				return p;
			}
		}

		return new int[] {-1, -1, 0, -1, -1}; //not found
	}

	public static ArrayList<int[]> ReturnAllPieces(int[][][] board, int color) {
		ArrayList<int[]> returnTable = new ArrayList<int[]>();

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {				
				if(board[i][j] != null && board[i][j][2] == color) {
					returnTable.add(board[i][j]);

				}
			}
		}

		return returnTable;
	}

	public static int[][][] cloneArray(int[][][] p){
		int[][][] clone = new int[9][8][];
		int[] q;

		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 8; j++) {
				q = p[i][j];
				if(q != null) {
					if(q.length != 1) {
						clone[i][j] = new int[] {q[0],q[1],q[2],q[3],q[4]};
					}
					else {
						clone[i][j] = new int[] {q[0]};
					}
					
				}
				else {
					clone[i][j] = null;
				}
			}
		}

		return clone;
	}

	public static void UpdateCoords(int[][][] board) {
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(board[i][j] != null && (board[i][j][3] != i || board[i][j][4] != j)) {
					board[i][j][3] = i;
					board[i][j][4] = j;
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
	
	/*
	 * Pearsons correlation coefficient
	 */
	
	public static double correlation(double[] d1, double[] d2, int r) {
		double cov = 0;
		double stdDevD1 = 0;
		double stdDevD2 = 0;
		
		//compute avgs
		double avgd1 = 0;
		double avgd2 = 0;
		double sum = 0;
		
		for(int i = 0; i<d1.length; i++) {
			sum += d1[i];
		}
		avgd1 = sum / d1.length;
		
		sum = 0;
		
		for(int i = 0; i<d2.length; i++) {
			sum += d2[i];
		}
		avgd2 = sum / d2.length;
		
		
		for(int i = 0; i<d1.length; i++) {
			cov += (d1[i] - avgd1)*(d2[i] - avgd2);
			
		}
		
		for(int i = 0; i<d1.length; i++) {
			if(d1[i] == 0 || d2[i] == 0) {
				//System.out.println(d1[i]+"##"+d2[i]+" r "+r);
			}
			
			stdDevD1 += Math.pow(d1[i] - avgd1, 2);  
			stdDevD2 += Math.pow(d2[i] - avgd2, 2);
		}
		
		
		return (cov == 0 || stdDevD1 == 0 || stdDevD2 == 0) ? 0 : cov / Math.sqrt(stdDevD1 * stdDevD2); 
		
		
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
