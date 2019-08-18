package main;

public class RuleSet {
	private static int limit = 0;
	static String[][] grid = null;
	private static int[] pattern;
	private static int wkingX = 4;
	private static int wkingY = 0;
	private static int bkingX = 4;
	private static int bkingY = 7;
	
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
	 * 1 if not legal for a given pieceo or own piece in the way, or trying to move other players piece
	 * 2 if king in check 
	 * 3 if checkmate for white
	 * 4 if checkmate for black
	 * 
	 */
	
	public static int validate(String[][] rgrid, boolean white, int startX, int startY, int endX, int endY) {
		grid = rgrid;
		
		/*
		System.out.println("piece: "+!(parsePattern(grid[startX][startY],startX,startY,endX,endY).equals("error")));
		pattern = null;
		System.out.println("blocking: "+!blocking(grid[startX][startY],startX,startY,endX,endY));
		pattern = null;
		System.out.println("check: "+!check_mate());
		pattern = null;
		*/
		
		//trying to move other players piece
		
		if((white && (grid[startX][startY].charAt(grid[startX][startY].length()-1) != 'w')) || (!white && grid[startX][startY].charAt(grid[startX][startY].length()-1) != 'b')) {
			return 1;
		}
		
		if(grid[startX][startY] != " " && !(parsePattern(grid[startX][startY],startX,startY,endX,endY).equals("error")) && 
				!blocking(grid[startX][startY],startX,startY,endX,endY))
		{
			if(check_mate() == 1) {
				return 3;
			}
			else if(check_mate() == -1) {
				return 4;
			}
			
			if(king_in_check(white)) {
				return 2;
			}
			
			if(grid[startX][startY].contains("king_w")) {
				wkingX = endX;
				wkingY = endY;
			}
			else if(grid[startX][startY].contains("king_b")) {
				bkingX = endX;
				bkingY = endY;
			}
			return 0;
		}
		return 1;
	}
	
	private static String parsePattern(String p,int x, int y,int tX, int tY){
		System.out.println("tY: "+tY+"y: "+y);
		
		
		pattern = new int[] {(tX-x),(tY-y)};
		
		while(limit < 8){
			//rotate(p);
			if(verifyPattern(p,pattern,y,grid[tX][tY])){
				return p;
			}

		limit++;
		}

		return "error";
	}

	static boolean verifyPattern(String piece, int[] pattern, int y, String targetSquareContent){
		boolean result = false;
		System.out.println(pattern[0]+" "+pattern[1]);
		
		if (piece.equals("pawn_w")){
			System.out.println(pattern[0]+" "+pattern[1]);
				//white pawn capture OR white pawn moves one square up OR pawn is situated at (x,1) and can move two squares up
				if((targetSquareContent.charAt(targetSquareContent.length()-1) == 'b' && (Math.abs(pattern[0]) == 1 && pattern[1] == 1)) || 
						(pattern[0] == 0 && pattern[1] == 1) || 
						(y == 1 && pattern[0] == 0 && pattern[1] == 2)){
					result = true;
				}
			
		}
		else if (piece.equals("pawn_b")){
			System.out.println(y);
			System.out.println(pattern[0]+" "+pattern[1]);
			//black pawn capture OR white pawn moves one square down OR pawn is situated at (x,6) and can move two squares down
			if((targetSquareContent.charAt(targetSquareContent.length()-1) == 'w' && (Math.abs(pattern[0]) == 1 && pattern[1] == -1)) || (pattern[0] == 0 && pattern[1] == -1) || 
					(y == 6 && pattern[0] == 0 && pattern[1] == -2)){
				result = true;
			}
			
		}
		else if (piece.substring(0,6).equals("knight") && (Math.abs(pattern[0]) == 1 && Math.abs(pattern[1]) == 2) || (Math.abs(pattern[0]) == 2 && Math.abs(pattern[1]) == 1)){
			result = true;
		}
		else if (piece.substring(0,6).equals("bishop") && Math.abs(pattern[0]) == Math.abs(pattern[1])){
			result = true;
		}
		else if (piece.substring(0,4).equals("rook") && (pattern[0] == 0 || pattern[1] == 0)) {
			result = true;
		}
		else if (piece.substring(0,5).equals("queen") && ((pattern[0] == 0 || pattern[1] == 0) || (Math.abs(pattern[0]) == Math.abs(pattern[1])))){
			result = true;
		}
		else if (piece.substring(0,4).equals("king") && ((pattern[0] == 0 && Math.abs(pattern[1]) == 1 || (pattern[1] == 0 && Math.abs(pattern[0]) == 1)) || (Math.abs(pattern[0]) == 1 && Math.abs(pattern[1]) == 1))){
			result = true;
		}
		else{
			result = false;
		}
		return result;

	}

	static void rotate(String piece){
		int temp1 = 0;
		int temp2 = 0;
	
		if(piece.equals("pawn_w") || piece.equals("pawn_b")) {
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



	static boolean blocking(String piece,int x,int y,int tX, int tY){
		//theres something in the way between (exclusive)(x,y) and (tX,tY)(inclusive) while piece is not a knight
		int tempX = 0;
		int tempY = 0;
		int start;
		int end;
		
		if((piece.charAt(piece.length()-1) == grid[tX][tY].charAt(grid[tX][tY].length()-1))) {
			return true;	
		}
		
		if(piece.substring(0,6).equals("knight")) {
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
			
			for(int i = start; i < end-1; i++) {
				
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

				
				if(!grid[tempX][tempY].equals(" ")) {
					return true;
				}
			}

		}
			
		return false;
	}

	static boolean king_in_check(boolean whitesTurn){
		//exists different-colored piece with legal next move, with targetX,targetY = kings (x,y)
		
		String tempPiece = "";
		
		if(whitesTurn) {
			tempPiece = "knight";

			return true;
		}
		
		return false;
	}

	/**
	 * 
	 * @return 0 if no mate, 1 for white victory and 1 for black victory
	 */
	
	static int check_mate(){
		/*
		if(
		king_in_check() == true FOR all x,y king can move &&
		blocking() = false &&
		kings color cant capture a piece to escape check){
		

		return true;
		}
		 */
		return 0;
	}





	
	
}
