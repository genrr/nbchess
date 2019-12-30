package main;

public class MGameStuff {
	
	private static int distance = 0;


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
		*/
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
				 */
				
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
				*/
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
				*/
				
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
				*/
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
				*/
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
				*/
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
		 */
	}
	
		/*
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
	public static void resetDistance() {
		distance = 0;
	}
	
	
	public static int[][] DistanceMatrix(String ownership, String action){
		
		
	}
	
	public static int unitValue(String unit) {
		int v = 0;
		if(unit.contains("knight") || unit.contains("bishop")) {
			v = 3;
		}
		else if(unit.contains("rook")) {
			v = 5;
		}
		else if(unit.contains("queen")) {
			v = 9;
		}
		else if(unit.contains("pawn")) {
			v = 1;
		}
		return v;
	}
	
}
