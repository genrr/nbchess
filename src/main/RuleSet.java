package main;

import java.util.ArrayList;

public class RuleSet {
	private static int limit;
	private static Piece[][] grid = null;
	private static Piece[][] tempGrid = new Piece[8][8];
	private static int[] pattern;
	private static boolean bKingMoved = false;
	private static boolean wKingMoved = false;
	private static int[] rooksMoved = {0,0,0,0};
	private static int bkingX = 0;
	private static int bkingY = 0;
	private static int wkingX = 0;
	private static int wkingY = 0;
	private static boolean wcastlingk = false;
	private static boolean wcastlingq = false;
	private static boolean bcastlingk = false;
	private static boolean bcastlingq = false;

	/**
	 * 
	 * @param rgrid
	 * @param white
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @return 
	 * 
	 * 0 legal move
	 * 1 if not legal for a given piece or own piece in the way, or trying to move other players piece
	 * 2 if king in check 
	 * 3 if checkmate for white
	 * 4 if checkmate for black
	 * 
	 */
	
	public static int validate(Piece[][] rgrid, boolean white, int startX, int startY, int endX, int endY) {
		grid = rgrid;
		limit = 0;
		int checkResult = -1;
		
		piecePositions();
		
		/*
		System.out.println("piece: "+!(parsePattern(grid[startX][startY],startX,startY,endX,endY).equals("error")));
		pattern = null;
		System.out.println("blocking: "+!blocking(grid[startX][startY],startX,startY,endX,endY));
		pattern = null;
		System.out.println("check: "+!check_mate());
		pattern = null;
		*/
		
		//trying to move other players piece
		
		System.out.println(grid[startX][startY] != null);
		
		if(grid[startX][startY] != null && ((white && !grid[startX][startY].getColor()) || (!white && grid[startX][startY].getColor()))) {
			return 1;
		}
		
		if(grid[startX][startY] != null && !(parsePattern(grid[startX][startY],startX,startY,endX,endY).equals("error")) && 
				!blocking(grid[startX][startY],startX,startY,endX,endY))
		{
			if(bcastlingk) {
				Piece p = grid[0][7]; 
				grid[0][7] = null;
				grid[0][5] = p;
			}
			else if(bcastlingq){
				Piece p = grid[0][0]; 
				grid[0][0] = null;
				grid[0][3] = p;
			}
			else if(wcastlingk) {
				Piece p = grid[7][7]; 
				grid[7][7] = null;
				grid[7][5] = p;
			}
			else if(wcastlingq) {
				Piece p = grid[7][0]; 
				grid[7][0] = null;
				grid[7][3] = p;
			}
			
			
			
			checkResult = king_in_check(white,grid[startX][startY],startX,startY,endX,endY);

			//check delivered by opposing piece
			if(checkResult == 1) {
				return 5;
			}
			//illegal move, check not resolved
			else if (checkResult == 2){
				return 2;
			}
			if(checkResult == 3) {
				return 3;
			}
			else if(checkResult == 4) {
				return 4;
			}
			return 0;
		}
		return 1;
	}
	
	private static String parsePattern(Piece p,int x, int y,int tX, int tY){
		System.out.println("tY: "+tY+"y: "+y);
		
		
		pattern = new int[] {(y-tY),(x-tX)};
		
		while(limit < 8){
			//rotate(p);
			if(verifyPattern(p,p.getName(),pattern,x,grid[tX][tY])){
				return p.getName();
			}

		limit++;
		}

		return "error";
	}

	private static boolean verifyPattern(Piece p, String name, int[] pattern, int y, Piece targetSquareContent){
		boolean result = false;
		System.out.println(pattern[0]+" "+pattern[1]);
		
		if (name.equals("pawn_w")){
			System.out.println(pattern[0]+" "+pattern[1] + "y_"+y);
				//white pawn capture OR white pawn moves one square up OR pawn is situated at (x,6) and can move two squares up
				if((targetSquareContent != null && !targetSquareContent.getColor() && (Math.abs(pattern[0]) == 1 && pattern[1] == 1)) || 
						(pattern[0] == 0 && pattern[1] == 1) || 
						(y == 6 && pattern[0] == 0 && pattern[1] == 2)){
					result = true;
				}
			
		}
		else if (name.equals("pawn_b")){
			System.out.println(y);
			System.out.println(pattern[0]+" "+pattern[1]);
			//black pawn capture OR white pawn moves one square down OR pawn is situated at (x,1) and can move two squares down
			if((targetSquareContent != null && targetSquareContent.getColor() && (Math.abs(pattern[0]) == 1 && pattern[1] == -1)) ||
					(pattern[0] == 0 && pattern[1] == -1) || 
					(y == 1 && pattern[0] == 0 && pattern[1] == -2)){
				result = true;
			}
			
		}
		else if (name.substring(0,6).equals("knight") && (Math.abs(pattern[0]) == 1 && Math.abs(pattern[1]) == 2) || (Math.abs(pattern[0]) == 2 && Math.abs(pattern[1]) == 1)){
			result = true;
		}
		else if (name.substring(0,6).equals("bishop") && Math.abs(pattern[0]) == Math.abs(pattern[1])){
			result = true;
		}
		else if (name.substring(0,4).equals("rook") && (pattern[0] == 0 || pattern[1] == 0)) {
			result = true;
		}
		else if (name.substring(0,5).equals("queen") && ((pattern[0] == 0 || pattern[1] == 0) || (Math.abs(pattern[0]) == Math.abs(pattern[1])))){
			result = true;
		}
		else if (name.substring(0,4).equals("king") && 
				((pattern[0] == 0 && Math.abs(pattern[1]) == 1 || (pattern[1] == 0 && Math.abs(pattern[0]) == 1)) ||
				(Math.abs(pattern[0]) == 1 && Math.abs(pattern[1]) == 1)) || 
				((pattern[0] == 2 || pattern[0] == -2) && pattern[1] == 0 && castlingAllowed(pattern, p.getColor()))){
			result = true;
		}
		else{
			result = false;
		}
		return result;

	}

	private static boolean lineIsUnderAttack(int sx, int sy, int tx, int ty, boolean whitesTurn, boolean inclusive, boolean hostile) {
		int tempX = sx;
		int tempY = sy;
		
		//(4,7) -> (7,7) amount = 3; tx > sx, tempX = 4,5,6,7
		
		int amount;
		int counter = 0;

		
		if(sy == ty) {
			amount = Math.abs(sx-tx);
		}
		else {
			amount = Math.abs(sy-ty);
		}
		
		for(int z = 0; z <= amount; z++ ) {
			
			if(tx > sx) {
				tempX++;
			}
			else if(tx < sx) {
				tempX--;
			}
			if(ty > sy) {
				tempY++;
			}
			else if(ty < sy) {
				tempY--;
			}
			
			
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if(hostile) {
						if(grid[i][j] != null && (grid[i][j].getColor() != whitesTurn) && MGameStuff.distance(grid[i][j], j, i, tempY-i, tempX-j) == 1){
							if(!inclusive) {
								return true;
							}
							else if(counter == amount){
								return true;
							}
							counter++;
						}	
					}
					else {
						if(grid[i][j] != null && (grid[i][j].getColor() == whitesTurn) && MGameStuff.distance(grid[i][j], j, i, tempY-i, tempX-j) == 1){
							if(!inclusive) {
								return true;
							}
							else if(counter == amount){
								return true;
							}
							counter++;
						}	
					}
				}
			}		
		}
		return false;
	}

	private static boolean castlingAllowed(int[] pattern, boolean whitesTurn) {
		if(pattern[0] == -2) {
			if(whitesTurn) {
				if(grid[7][5] == null && grid[7][6] == null) {
					if(rooksMoved[3] == 0 && !wKingMoved && !lineIsUnderAttack(4,7,7,7,whitesTurn,false,true)) {
						wcastlingk = true;
						return true;
					}
				}
			}
			else {
				if(grid[0][5] == null && grid[0][6] == null) {
					if(rooksMoved[1] == 0 && !bKingMoved && !lineIsUnderAttack(4,0,7,0,whitesTurn,false,true)) {
						bcastlingk = true;
						return true;
					}
				}
			}
		}
		else if(pattern[0] == 2) {
			if(whitesTurn ) {
				if(grid[7][1] == null && grid[7][2] == null && grid[7][3] == null) {
					if(rooksMoved[2] == 0 && !wKingMoved && !lineIsUnderAttack(0,7,3,7,whitesTurn,false,true)) {
						wcastlingq = true;
						return true;
					}
				}
			}
			else {
				if(grid[0][1] == null && grid[0][2] == null && grid[0][3] == null) {
					if(rooksMoved[0] == 0 && !bKingMoved && !lineIsUnderAttack(0,0,3,0,whitesTurn,false,true)) {
						bcastlingq = true;
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	private static void rotate(String piece){
		int temp1 = 0;
		int temp2 = 0;
	
		if(piece.contains("pawn")) {
			return;
		}
		
		temp1 = pattern[0];
		temp2 = pattern[1];
		
		
		if((temp1 == -1 && temp2 == 2) || (temp1 == -1 && temp2 == -2)) {
			pattern[0] = temp2 - 2*temp2;
			pattern[1] = temp1 - 2*temp1;
		}
		else {
			pattern[0] = temp2;
			pattern[1] = temp1 - 2*temp1;
		}

	}
	*/


	protected static boolean blocking(Piece piece,int x,int y,int tX, int tY){
		//theres something in the way between (exclusive)(x,y) and (tX,tY)(inclusive) while piece is not a knight
		int tempX = 0;
		int tempY = 0;
		int start;
		int end;
		
		if(grid[tX][tY] != null && (piece.getColor() == grid[tX][tY].getColor())) {
			return true;	
		}
		
		if(piece.getName().substring(0,6).equals("knight")) {
			return false;
		}
		
		else{
			
			if(x == tX) {
				start = y;
				end = tY;
				tempX = 0;
			}
			else if(y == tY) {
				start = x;
				end = tX;
				tempY = 0;
			}
			else {
				start = Math.min(x,tX);
				end = Math.max(x, tX);
			}
			
			System.out.println("start "+start+" end "+end);
			
			for(int i = 1; i < end-start; i++) {
				
				
				if(tY < y) {
					tempY = y-i;
				}
				else if(tY > y){
					tempY = y+i;
				}
				else {
					tempY = y;
				}
				
				if(tX < x) {
					tempX = x-i;
					//System.out.println("i: "+i+" current x: "+tempX);
				}
				else if(tX > x){
					tempX = x+i;
				}
				else {
					tempX = x;
				}
				
				System.out.println(tempX+", "+tempY);

				
				if(grid[tempX][tempY] != null) {
					return true;
				}
			}

		}
			
		return false;
	}
	
	private static void piecePositions() {
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				
				if(tempGrid[i][j] == null) {
					continue;
				}
				else {
					if(tempGrid[i][j].getName().equals("king_w")) {
						wkingY = i;
						wkingX = j;
						if(wkingY != 7 || wkingX != 4) {
							wKingMoved = true;
						}
					}	
					else if(tempGrid[i][j].getName().equals("king_b")) {
						bkingY = i;
						bkingX = j;
						if(bkingY != 0 || bkingX != 4) {
							bKingMoved = true;
						}
					}
					if(tempGrid[i][j].getId() == 0) {
						if(i != 0 || j != 0) {
							rooksMoved[0] = 1;
						}
					}
					else if(tempGrid[i][j].getId() == 7) {
						if(i != 0 || j != 7) {
							rooksMoved[1] = 1;
						}
					}
					else if(tempGrid[i][j].getId() == 8){
						if(i != 7 || j != 0) {
							rooksMoved[2] = 1;
						}
					}
					else if(tempGrid[i][j].getId() == 15){
						if(i != 7 || j != 7) {
							rooksMoved[3] = 1;
						}
					}
				}
			}
		}	
	}

	private static int king_in_check(boolean whitesTurn, Piece piece, int sx, int sy, int tx, int ty){
		//exists different-colored piece with legal next move, with targetX,targetY = kings (x,y)
		
		//copy state so we can test for checks

		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(grid[i][j] == null) {
					tempGrid[i][j] = null;
				}
				else {
					tempGrid[i][j] = grid[i][j];
				}
			}
		}
		
		tempGrid[sx][sy] = null;
		tempGrid[tx][ty] = piece;
		
					
			
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				
				if(tempGrid[i][j] == null) {
					continue;
				}
				//first test for check, move causes opposite color king to be in check
				//then test for illegal move that doesn't resolve check
				if(whitesTurn) {
					if(tempGrid[i][j].getColor() && MGameStuff.distance(tempGrid[i][j],j,i,bkingX-j,bkingY-i) == 1) {
						if(tempGrid[i][j].getName().contains("knight")) {
							if(lineIsUnderAttack(j+1, i+1, j, i, whitesTurn, false, false) || !check_mate(whitesTurn)) {
								return 1;
							}
							else {
								return 3;
							}
						}
						else {
							if(lineIsUnderAttack(bkingX, bkingY, j, i, whitesTurn, false, false) || !check_mate(whitesTurn)) {
								return 1;
							}
							else {
								return 3;
							}
						}
					}
					else if(!tempGrid[i][j].getColor() && MGameStuff.distance(tempGrid[i][j],j,i,wkingX-j,wkingY-i) == 1) {
						return 2;
					}
				}
				else {				
					if(!tempGrid[i][j].getColor() && MGameStuff.distance(tempGrid[i][j],j,i,wkingX-j,wkingY-i) == 1) {
						if(tempGrid[i][j].getName().contains("knight")) {
							if(lineIsUnderAttack(j+1, i+1, j, i, whitesTurn, false, false) || !check_mate(whitesTurn)) {
								return 1;
							}
							else {
								return 4;
							}
							
						}
						else {
							if(lineIsUnderAttack(wkingX, wkingY, j, i, whitesTurn, false, false) || !check_mate(whitesTurn)) {
								return 1;
							}
							else {
								return 4;
							}
						}
					}
					else if(tempGrid[i][j].getColor() && MGameStuff.distance(tempGrid[i][j],j,i,bkingX-j,bkingY-i) == 1) {
						return 2;
					}
				}
			}
		}							
		
		return 0;
	}

	/**
	 * 
	 * @return 0 if no mate, 1 for white victory and 1 for black victory
	 */
	
	private static boolean check_mate(boolean turn){
		
		int startX;
		int startY;
		int endX;
		int endY;
		
		int booleanCounter = 0;
		
		//the adjacent square is either under attack or blocked by own piece for the counter to be incremented
		if(turn) {
			if((bkingX > 0 && lineIsUnderAttack(bkingX, bkingY, bkingX-1, bkingY, turn, true, true)) || !grid[bkingY][bkingX-1].getColor()){
				booleanCounter++;
			}
			if((bkingY > 0 && lineIsUnderAttack(bkingX, bkingY, bkingX, bkingY-1, turn, true, true)) || !grid[bkingY-1][bkingX].getColor()){
				booleanCounter++;
			}
			if((bkingX < 7 && lineIsUnderAttack(bkingX, bkingY, bkingX+1, bkingY, turn, true, true)) || !grid[bkingY][bkingX+1].getColor()){
				booleanCounter++;
			}
			if((bkingY < 7 && lineIsUnderAttack(bkingX, bkingY, bkingX, bkingY+1, turn, true, true)) || !grid[bkingY+1][bkingX].getColor()){
				booleanCounter++;
			}
			if((bkingX < 7 && bkingY < 7 && lineIsUnderAttack(bkingX, bkingY, bkingX+1, bkingY+1, turn, true, true)) || !grid[bkingY+1][bkingX+1].getColor()){
				booleanCounter++;
			}
			if((bkingX > 0 && bkingY > 0 && lineIsUnderAttack(bkingX, bkingY, bkingX-1, bkingY-1, turn, true, true)) || !grid[bkingY-1][bkingX-1].getColor()){
				booleanCounter++;
			}
			if((bkingX < 7 && bkingY > 0 && lineIsUnderAttack(bkingX, bkingY, bkingX+1, bkingY-1, turn, true, true)) || !grid[bkingY-1][bkingX+1].getColor()){
				booleanCounter++;
			}
			if((bkingX > 0 && bkingY < 7 && lineIsUnderAttack(bkingX, bkingY, bkingX-1, bkingY+1, turn, true, true)) || !grid[bkingY+1][bkingX-1].getColor()){
				booleanCounter++;
			}
		}
		
		if(booleanCounter == 8) {
			return true;
		}
		
		else {
			if((wkingX > 0 && lineIsUnderAttack(wkingX, wkingY, wkingX-1, wkingY, turn, true, true)) || !grid[wkingY][wkingX-1].getColor()){
				booleanCounter++;
			}
			if((wkingY > 0 && lineIsUnderAttack(wkingX, wkingY, wkingX, wkingY-1, turn, true, true)) || !grid[wkingY-1][wkingX].getColor()){
				booleanCounter++;
			}
			if((wkingX < 7 && lineIsUnderAttack(wkingX, wkingY, wkingX+1, wkingY, turn, true, true)) || !grid[wkingY][wkingX+1].getColor()){
				booleanCounter++;
			}
			if((wkingY < 7 && lineIsUnderAttack(wkingX, wkingY, wkingX, wkingY+1, turn, true, true)) || !grid[wkingY+1][wkingX].getColor()){
				booleanCounter++;
			}
			if((wkingX < 7 && bkingY < 7 && lineIsUnderAttack(wkingX, wkingY, wkingX+1, wkingY+1, turn, true, true)) || !grid[wkingY+1][wkingX+1].getColor()){
				booleanCounter++;
			}
			if((wkingX > 0 && bkingY > 0 && lineIsUnderAttack(wkingX, wkingY, wkingX-1, wkingY-1, turn, true, true)) || !grid[wkingY-1][wkingX-1].getColor()){
				booleanCounter++;
			}
			if((wkingX < 7 && bkingY > 0 && lineIsUnderAttack(wkingX, wkingY, wkingX+1, wkingY-1, turn, true, true)) || !grid[wkingY-1][wkingX+1].getColor()){
				booleanCounter++;
			}
			if((wkingX > 0 && bkingY < 7 && lineIsUnderAttack(wkingX, wkingY, wkingX-1, wkingY+1, turn, true, true)) || !grid[wkingY+1][wkingX-1].getColor()){
				booleanCounter++;
			}
		
			
			if(booleanCounter == 8) {
				return true;
			}
		
		}
		
		/*
		if(turn) {
		
			if(bkingX-1 < 0) {
				startX = bkingX;
			}
			else {
				startX = bkingX-1;
			}
			if(bkingX+1 > 7) {
				endX = bkingX;
			}
			
			else {
				endX = bkingX+1;
			}
			
			if(bkingY-1 < 0) {
				startY = bkingY;
			}
			else {
				startY = bkingY-1;
			}
			if(bkingY+1 > 7) {
				endY = bkingY;
			}
			else {
				endY = bkingY+1;
			}	
		}
			
	
		else {
			if(wkingX-1 < 0) {
				startX = wkingX;
			}
			else {
				startX = wkingX-1;
			}
			if(wkingX+1 > 7) {
				startX = wkingX;
			}
			else {
				startX = wkingX+1;
			}
			
			if(wkingY-1 < 0) {
				startY = wkingY;
			}
			else {
				startY = wkingY-1;
			}
			if(wkingY+1 > 7) {
				startY = wkingY;
			}
			else {
				startY = wkingY+1;
			}
		
		}
		*/
		/*
		if(bkingX == 0  && bkingY == 7) {
			if(lineIsUnderAttack(bkingX, bkingY, bkingX+1, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX, bkingY-1, bkingX+1, bkingY-1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else if(bkingX == 0 && bkingY == 0) {
			if(lineIsUnderAttack(bkingX, bkingY, bkingX+1, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX, bkingY+1, bkingX+1, bkingY+1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else if(bkingX == 0 && bkingY < 7) {
			if(lineIsUnderAttack(bkingX, bkingY, bkingX+1, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX, bkingY-1, bkingX+1, bkingY-1, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX, bkingY+1, bkingX+1, bkingY+1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else if(bkingX == 7  && bkingY == 0) {
			if(lineIsUnderAttack(bkingX-1, bkingY, bkingX, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY+1, bkingX, bkingY+1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else if(bkingX == 7 && bkingY == 7) {
			if(lineIsUnderAttack(bkingX-1, bkingY, bkingX, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY-1, bkingX, bkingY-1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else if(bkingX == 7 && bkingY > 0) {
			if(lineIsUnderAttack(bkingX-1, bkingY, bkingX, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY-1, bkingX, bkingY-1, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY+1, bkingX, bkingY+1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else if(bkingX == 0) {
			if(lineIsUnderAttack(bkingX-1, bkingY, bkingX+1, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY+1, bkingX+1, bkingY+1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else if(bkingX == 7) {
			if(lineIsUnderAttack(bkingX-1, bkingY, bkingX+1, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY-1, bkingX+1, bkingY-1, turn, true, true)) {
				booleanCounter++;
			}
		}
		else {
			if(lineIsUnderAttack(bkingX-1, bkingY, bkingX+1, bkingY, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY-1, bkingX+1, bkingY-1, turn, true, true)) {
				booleanCounter++;
			}
			if(lineIsUnderAttack(bkingX-1, bkingY+1, bkingX+1, bkingY+1, turn, true, true)) {
				booleanCounter++;
			}
		}
		*/
		
		
		return false;
	}





	
	
}
