package main;

import java.util.ArrayList;

public class MGameStuff {
	
	private static int distance = 0;
	public static int[] distVector = new int[4];
	private static Path[] pathVector = new Path[10];
	private static Piece[][] board = null;
	static double[][] evalMatrix = new double[8][8];
/*
 * i : length of route
j : first blocking piece at square S
k : density of pieces along
l : value of pieces along

 */


	
/*

	public static int distance(Piece[][] grid, Piece piece, int pieceStartX, int pieceStartY, int dx, int dy) {
		
		int startX,startY;
		int temp1,temp2;
		int t;
		
		
		
		System.out.println("piece: "+piece.getName()+" dx: "+dx+" dy: "+dy+" distance: "+distance);
		
		/*
		if(piece.getName().contains("knight")) {
			
			int[][] mm={{4,1,2,1,4},
						{1,2,3,2,1},
						{2,3,0,3,2},
						{1,2,3,2,1},
						{4,1,2,1,4}};
			
			startX = 2;
			startY = 2;

			
			
			if((startX-dx < 5 && startX-dx > -1 && startY-dy < 5 && startY-dy > -1)) {
				System.out.println("matrix achieved! distance: "+(mm[startY-dy][startX-dx] + distance));
				t =  mm[startY-dy][startX-dx] + distance;
				distance = 0;
				return t;
			}
			else {
				distance++;
				distance(piece,pieceStartX,pieceStartY,dx + Math.negateExact((int)Math.signum(dx)*1), dy + Math.negateExact((int)Math.signum(dy)*2));
				
				// -5,3 -> -4,1
				
				
				//-5 + 1 = -4, 5 - 1 = 4
			
				//temp2 = distance(piece,pieceStartX,pieceStartY,dx + Math.negateExact((int)Math.signum(dx)*2), dy + Math.negateExact((int)Math.signum(dy)*1));
				//return Math.min(temp1, temp2);
				
				
				
				compareTransform(piece, dx, dy, pieceStartX, pieceStartY, (int)Math.signum(dx)*1, (int)Math.signum(dy)*2, (int)Math.signum(dx)*2, (int)Math.signum(dy)*1);
				
				if(dx > 0 && dy > 0) {
					compareTransform(piece, dx, dy, pieceStartX, pieceStartY, 1, 2, 2, 1);
				}
				else if(dx > 0 && dy < 0){
					compareTransform(piece,dx,dy, pieceStartX, pieceStartY,1,-2,2,-1);
				}
				else if(dx < 0 && dy > 0){
					compareTransform(piece,dx,dy, pieceStartX, pieceStartY,-1,2,-2,1);
				}
				else if(dx < 0 && dy < 0){
					compareTransform(piece,dx,dy, pieceStartX, pieceStartY,-1,-2,-2,-1);
				}
				
			}
		}

		if(piece.getName().contains("bishop")) {
			int[][] mm={{1,0,2,0,2,0,1},
						{0,1,0,2,0,1,0},
						{2,0,1,0,1,0,2},
						{0,2,0,0,0,2,0},
						{2,0,1,0,1,0,2},
						{0,1,0,2,0,1,0},
						{1,0,2,0,2,0,1}};
			
			startX = 3;
			startY = 3;
			
			if(startX+dx < 7 && startX+dx > -1 && startY+dy < 7 && startY+dy > -1) {
				System.out.println("returning.."+(mm[startX+dx][startY+dy]));
				return mm[startX+dx][startY+dy];
			}
			else {
				/*
				 * (0,0) -> (7,5):
				 * 0,0 -> 6,6 -> 7,5
				 * (1,1) -> (7,5):
				 * 1,1 -> 2,0 -> 7,5
				 * 
				 * dx = 7
				 * dy = 5
				 * 
				 * start 0,0
				 * tempX = 1,2,3,4,5,6
				 * tempY = 1,2,3,4,5,6
				 * 
				 * tempDx = 7-6 = 1
				 * tempDy = 5-6 = -1
				 * 
				 * signum(dx) = 1 = signum(tempDx)
				 * signum(dy) = 1 != signum(tempDy) = -1
				 * 
				 * 7 + (0+1)(-1)
				 * 7 + (0+2)(-1)
				 * 
				 *...
				 * 
				 * 7 + (0+6)(-1)
				 * 
				 * 
				 * startX = 2
				 * 
				 * 
				 * 

				
				int tempX = 0;
				int tempY = 0;
				int tempDx = dx;
				int tempDy = dy;
				
				//check if there is something in the way, exits if blocking() == false
				
				System.out.println("blocking:"+RuleSet.blocking(piece,grid, pieceStartX,pieceStartY, pieceStartX+tempX, pieceStartY+tempY));
				
				while(pieceStartX+tempX>=0 && pieceStartY+tempY>=0 && pieceStartX+tempX<8 && pieceStartY+tempY<8 &&
						Math.signum(dx) == Math.signum(tempDx) && Math.signum(dy) == Math.signum(tempDy) && 
						!RuleSet.blocking(piece,grid, pieceStartX, pieceStartY, pieceStartX+tempX, pieceStartY+tempY)) {
					tempX += Math.negateExact((int)Math.signum(dx));
					tempY += (int)Math.signum(dy);
					System.out.println("tempX:"+tempX+" tempY:"+tempY);
					tempDx = dx + (pieceStartX+tempX)*Math.negateExact((int)Math.signum(dx));
					tempDy = dy + (pieceStartY+tempY)*Math.negateExact((int)Math.signum(dy));
				}

				//distance(piece,grid,pieceStartX,pieceStartY, tempDx, tempDy);

			}
			
			
		}
		else if(piece.getName().contains("pawn_w")) {
			int[][] mm = {{5,5,5,5,5,5,5},
						  {4,4,4,4,4,4,4},
						  {3,3,3,3,3,3,3},
						  {0,2,2,2,2,2,0},
						  {0,0,1,1,1,0,0},
						  {0,0,0,0,0,0,0},
						  {0,0,0,0,0,0,0}};
			
			startX = 4;
			startY = 3;
			
			if(startX+dx < 7 && startX+dx > -1 && startY+dy < 7 && startY+dy > -1) {
				return mm[startX+dx][startY+dy] + distance;
			}
			else {
				
				
				/*
				if(dx > 0 && dy > 0) {
					return 0;
				}
				else if(dx > 0 && dy < 0){
					return 0;
				}
				else if(dx < 0 && dy > 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-1,1,-1,1);
				}
				else if(dx < 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-1,-1,-1,-1);
				}
				else if(dx < 0 && dy == 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-1,0,-1,0);
				}

			}
		}
		else if(piece.getName().contains("pawn_b")) {
			int[][] mm = {{0,0,0,0,0,0,0},
						  {0,0,0,0,0,0,0},
						  {0,0,1,1,1,0,0},
						  {0,2,2,2,2,2,0},
						  {3,3,3,3,3,3,3},
						  {4,4,4,4,4,4,4},
						  {5,5,5,5,5,5,5}};
			
			startX = 1;
			startY = 3;
			
			if(startX+dx < 7 && startX+dx > -1 && startY+dy < 7 && startY+dy > -1) {
				return mm[startX+dx][startY+dy] + distance;
			}
			else {
				
				
				/*
				if(dx > 0 && dy > 0) {
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,1,1,1,1);
				}
				else if(dx > 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,1,-1,1,-1);
				}
				else if(dx < 0 && dy == 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,1,0,1,0);
				}
				else if(dx < 0 && dy > 0){
					return 0;
				}
				else if(dx < 0 && dy < 0){
					return 0;
				}
	
				
			}

		}
		else if(piece.getName().contains("rook")) {
			int[][] mm = {{2,2,2,1,2,2,2},
						  {2,2,2,1,2,2,2},
						  {2,2,2,1,2,2,2},
						  {1,1,1,0,1,1,1},
						  {2,2,2,1,2,2,2},
						  {2,2,2,1,2,2,2},
						  {2,2,2,1,2,2,2}};
			
			startX = 3;
			startY = 3;
			
			if(startX+dx < 7 && startX+dx > -1 && startY+dy < 7 && startY+dy > -1) {
				return mm[startX+dx][startY+dy] + distance;
			}
			else {
				distance(grid,piece,pieceStartX,pieceStartY,dx + Math.negateExact((int)Math.signum(dx)*3), dy + Math.negateExact((int)Math.signum(dy)*1));
				/*
				if(dx > 0 && dy > 0) {
					compareTransform(piece, dx, dy, startX, startY, pieceStartX, pieceStartY, -dx, 0, 0, -dy);
				}
				else if(dx > 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-dx,0,0,-dy);
				}
				else if(dx < 0 && dy > 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,0,-dy,-dx,0);
				}
				else if(dx < 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,0,-dy,-dx,0);
				}

			}

		}
		else if(piece.getName().contains("queen")) {
			int[][] mm =   {{1,2,2,1,2,2,1},
					  		{2,1,2,1,2,1,2},
					  		{2,2,1,1,1,2,2},
					  		{1,1,1,0,1,1,1},
					  		{2,2,1,1,1,2,2},
					  		{2,1,2,1,2,1,2},
					  		{1,2,2,1,2,2,1}};
			
			startX = 3;
			startY = 3;
			
			if(startX+dx < 7 && startX+dx > -1 && startY+dy < 7 && startY+dy > -1) {
				return mm[startX+dx][startY+dy];
			}
			else {
				distance(grid,piece,pieceStartX,pieceStartY,dx + Math.negateExact((int)Math.signum(dx)*3), dy + Math.negateExact((int)Math.signum(dy)*1));
				/*
				if(dx > 0 && dy > 0) {
					compareTransform(piece, dx, dy, startX, startY, pieceStartX, pieceStartY, -dx, 0, 0, -dy);
				}
				else if(dx > 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-dx,0,0,-dy);
				}
				else if(dx < 0 && dy > 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,0,-dy,-dx,0);
				}
				else if(dx < 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,0,-dy,-dx,0);
				}
	
			}

	
		}
		else if(piece.getName().contains("king")) {
			int[][] mm = {{4,4,4,4,4,4,4,4,4},
						  {4,3,3,3,3,3,3,3,4},
						  {4,3,2,2,2,2,2,3,4},
						  {4,3,2,1,1,1,2,3,4},
						  {4,3,2,1,0,1,2,3,4},
						  {4,3,2,1,1,1,2,3,4},
						  {4,3,2,2,2,2,2,3,4},
						  {4,3,3,3,3,3,3,3,4},
						  {4,4,4,4,4,4,4,4,4}};
			
			startX = 4;
			startY = 4;
			
			
			if(startX+dx < 9 && startX+dx > -1 && startY+dy < 9 && startY+dy > -1) {
				return mm[startX+dx][startY+dy] + distance;
			}
			else {
				/*
				if(dx > 0 && dy > 0) {
					compareTransform(piece, dx, dy, startX, startY, pieceStartX, pieceStartY, 1, 1, 1, 1);
				}
				else if(dx > 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,1,-1,1,-1);
				}
				else if(dx < 0 && dy > 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-1,1,-1,1);
				}
				else if(dx < 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-1,-1,-1,-1);
				}
				else if(dx == 0 && dy < 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,0,-1,0,-1);
				}
				else if(dx == 0 && dy > 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,0,1,0,1);
				}
				else if(dx < 0 && dy == 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,-1,0,-1,0);
				}
				else if(dx > 0 && dy == 0){
					compareTransform(piece,dx,dy,startX,startY, pieceStartX, pieceStartY,1,0,1,0);
				}

			}

		}

		return 0;
		
		/* Knight
		 * 
		 * 4 1 2 1 4
		 * 1 2 3 2 1
		 * 2 3 x 3 2
		 * 1 2 3 2 1
		 * 4 1 2 1 4 
		 */
		
		/* Pawn-w
		 * 
		 * 
		 * 1 1 1
		 * 0 x 0
		 * 0 0 0
		 * 
		 * 
		 */
		
		
		/* Pawn-b
		 * 
		 * 
		 * 0 0 0
		 * 0 x 0
		 * 1 1 1
		 * 
		 * 
		 */
		
		/*
		 * Bishop
		 * 
		 * 1 0 2 0 2 0 1 0
		 * 0 1 0 2 0 1 0 2
		 * 2 0 1 0 1 0 2 0
		 * 0 2 0 x 0 2 0 2
		 * 2 0 1 0 1 0 2 0
		 * 0 1 0 2 0 1 0 2
		 * 1 0 2 0 2 0 1 0
		 * 0 2 0 2 0 2 0 1
		 * 
		 */
		
		/*
		 * Rook
		 * 
		 * 2 2 2 1 2 2 2
		 * 2 2 2 1 2 2 2
		 * 2 2 2 1 2 2 2
		 * 1 1 1 x 1 1 1
		 * 2 2 2 1 2 2 2
		 * 2 2 2 1 2 2 2
		 * 2 2 2 1 2 2 2
		 * 
		 */
		
		/*
		 * Queen
		 * 
		 * 1 2 2 1 2 2 1
		 * 2 1 2 1 2 1 2
		 * 2 2 1 1 1 2 2
		 * 1 1 1 X 1 1 1
		 * 2 2 1 1 1 2 2
		 * 2 1 2 1 2 1 2
		 * 1 2 2 1 2 2 1
		 * 
		 */
		
		/*
		 * King
		 * 
		 * 3 3 3 3 3 3 3
		 * 3 2 2 2 2 2 3
		 * 3 2 1 1 1 2 3
		 * 3 2 1 x 1 2 3
		 * 3 2 1 1 1 2 3 
		 * 3 2 2 2 2 2 3
		 * 3 3 3 3 3 3 3
		 * 

	}
	

	private static void compareTransform(Piece piece, int dx, int dy, int pointX, int pointY, int a1, int a2, int b1, int b2) {
		int transformDx = dx-Math.abs(a1);
		int transformDy = dy-Math.abs(a2);
		
		//generate new point from the information supplied by calling distance() method, if outside of the board, return
		//check if there exists something in between (inclusive)
		
		System.out.println(pointX+","+pointY+"->"+(pointX+a2)+","+(pointY+a1));
		if(pointX+a1 < 0 || pointX+a1 > 7 || pointY+a2 < 0 || pointY+a2 > 7){
			return;
		}
		else if(!RuleSet.blocking(piece, pointX, pointY, pointX+a1, pointY+a2)){
			pointX += a1;
			pointY += a2;
		}

		
		System.out.println("piece: "+piece.getName()+" starting (x,y) in board for piece: "+pointX+","+pointY);
		System.out.println("original (dx,dy): "+dx+","+dy);
		System.out.println("point (A) (dx,dy): "+transformDx+","+transformDy);
		System.out.println("point (A) in board: "+pointX+","+pointY);
		
		distance++;
		distance(piece,grid,pointX,pointY,transformDx,transformDy);
		
	
		
		
		double diff1 = (dx - 1.0*transformDx)/dx + (dy - 1.0*transformDy)/dy;
		double dist1 = Math.abs((dx - 1.0*transformDx)/dx - 0.5) * Math.abs((dy - 1.0*transformDy)/dy - 0.5);
		
		transformDx = dx-Math.abs(b1);
		transformDy = dy-Math.abs(b2); System.out.println("transformed: "+transformDx+","+transformDy);
		
		double diff2 = (dx - 1.0*transformDx)/dx + (dy - 1.0*transformDy)/dy;
		double dist2 = Math.abs((dx - 1.0*transformDx)/dx - 0.5) * Math.abs((dy - 1.0*transformDy)/dy - 0.5);
		
		
		if(diff1 <= diff2 && dist1 <= dist2) {
			distance++;
			distance(piece, pointX, pointY,dx-Math.abs(a1),dy-Math.abs(a2));			
		}
		else if(diff1 > diff2 && dist1 <= dist2) {
			distance++;
			distance(piece, pointX, pointY,dx-Math.abs(a1),dy-Math.abs(a2));
		}
		else if(diff1 <= diff2 && dist1 > dist2) {
			distance++;
			distance(piece, pointX, pointY,dx-Math.abs(b1),dy-Math.abs(b2));
		}
		else {
			distance++;
			distance(piece, pointX, pointY,dx-Math.abs(b1),dy-Math.abs(b2));
		}
		
		
		
	}
	*/
	


///###########################################################################################################

///Distance between pieces using log() vector:

	
	
public static int distance(Piece[][] b, Piece piece, int startX, int startY, int targetX, int targetY,boolean eval) {
	
	boolean capture = false;
	board = b;
	int dist = 0;
	
	System.out.println("testing distance between: "+piece.getName()+" at ("+piece.getX()+","+piece.getY()+") -> ("+targetX+","+targetY+")");
	
	//own piece in target square
	if(b[targetX][targetY] != null && b[targetX][targetY].getColor() == piece.getColor()){
		return 0;
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
		System.out.println("tX:"+targetX+" tY:"+targetY+" piece x,y: "+piece.getX()+","+piece.getY());
		if(board[targetX][targetY] != null && Math.abs(targetX-startX) == 1 && Math.abs(targetY-startY) == 1) {
			capture = true;
		}
		
		dist = PawnMoveDist(piece,startX,startY,targetX,targetY,capture);
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
		int[][] mm={{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
				  	{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
				  	{0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
				  	{0,0,0,0,0,2,2,2,2,2,0,0,0,0,0},
				  	{0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
				  	{0,0,0,4,4,4,4,4,4,4,4,4,0,0,0},
				  	{0,0,5,5,5,5,5,5,5,5,5,5,5,0,0},
				  	{0,6,6,6,6,6,6,6,6,6,6,6,6,6,0}};
		
		if(capture) {
			return mm[1+(tX-X)][7+(tY-Y)];
		}
		else {
			mm[1][7] = 1;
			return mm[0+(tX-X)][7+(tY-Y)];
		}
	}
	/*
	 * white pawn move matrix
	 * if capturing, return distance from point (6,7)
	 * else, add 1 to (6,7) and return distance from point (7,7)
	 */
	else {
		int[][] mm = {{0,6,6,6,6,6,6,6,6,6,6,6,6,6,0},
					  {0,0,5,5,5,5,5,5,5,5,5,5,5,0,0},
				      {0,0,0,4,4,4,4,4,4,4,4,4,0,0,0},
					  {0,0,0,0,3,3,3,3,3,3,3,0,0,0,0},
					  {0,0,0,0,0,2,2,2,2,2,0,0,0,0,0},
					  {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
					  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
					  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
		
		if(capture) {
			return mm[6+(tX-X)][7+(tY-Y)];
		}
		else {
			mm[6][7] = 1;
			System.out.println("7+(tX-X) = "+(7+(tX-X))+" tX: "+tX+" X: "+X);
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
		
		while(i < 8 && j < 8 && i > -1 && j > -1 && Math.abs(tX-i) != Math.abs(tY-j)) {
			if(i == 7 || j == 7 || i == 0 || j == 7) {
				break;
			}
			i += multX; 
			j += multY;
		}
		
		t1 = i - X;
		t2 = j - Y;
		
		d1 = tX-X + -1*t1;
		d2 = tY-Y + -1*t2;
		
		
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
	
	
	
	
	/* uhh..
	BishopMoveDist(X,Y,tX,tY);
	
	temp = distVector[0];
	
	RookMoveDist(X,Y,tX,tY);
	
	distVector[0] = Math.min(temp, distVector[0]);
	*/
	
	
}


//Checks if there is a piece in between (X,Y) and (tX,tY), (tX,tY) has to be reachable in one move 
//by the given piece from (X,Y)

static boolean CheckInBetween(int X, int Y, int tX, int tY){
	
	//set local variables:
	
	int t1; //local variable to hold temporary X value
	int t2; //local variable to hold temporary Y value
	int t = Math.max(Math.abs(tX-X),Math.abs(tY-Y)); //Complete movement, absolute value
	
	t1 = X; //set start temp x as X
	t2 = Y; //set start temp y as Y

	int i = 0; //init iterator i
	
	int multX = (int) Math.signum(tX-X);
	int multY = (int) Math.signum(tY-Y);
	
	//System.out.println("piece: "+board[X][Y].getName());
	System.out.println("checking line from ("+X+","+Y+") to ("+tX+","+tY+")");
	
	//iterate t times
	while(i < t){
		//either decrease or increase both temp vars by one, depending on the direction (multX, multY)
		t1 += multX;
		t2 += multY;
		i++;
		
		//own blocking piece along the line
		if(board[X][Y] != null && board[t1][t2] != null){
			if(board[X][Y].getColor() == board[t1][t2].getColor()) {
				System.out.println("At "+t1+", "+t2+" exists own blocking piece "+board[t1][t2].getName());
				return true;
			}
			else if(t1 != tX && t2 != tY){
				return true;
			}
			else {
				return false;
			}
		}
		
	}

	
	return false;

}

	
	
	public static void resetDistance() {
		distance = 0;
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
	
	public static ArrayList<Piece> ReturnAllPieces(Piece[][] board, boolean color) {
		ArrayList<Piece> returnTable = new ArrayList<Piece>();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(board[i][j] != null && board[i][j].getColor() == color) {
					//System.out.println(board[i][j].getName()+" at"+i+","+j);
					returnTable.add(board[i][j]);
					
				}
			}
		}
		
		return returnTable;
	}
	
	public static Piece[][] cloneArray(Piece[][] p){
		Piece[][] clone = new Piece[8][8];
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				clone[i][j] = p[i][j];
			}
		}
		
		return clone;
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

