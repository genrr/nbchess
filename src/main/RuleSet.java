package main;

public class RuleSet {
	private static int limit = 0;
	static String[][] grid = null;
	private static int[] pattern;
	
	public static String[][] validate(String[][] rgrid, int startX, int startY, int endX, int endY) {
		grid = rgrid;
		if(parsePattern(grid[startX][startY],startX,startY,endX,endY) != "error" && !blocking(grid[startX][startY],startX,startY,endX,endY) && !check_mate()){
			makeMove(rgrid[startX][startY],startX,startY,endX,endY);
			return rgrid;
		}
		return null;
	}
	
	private static String parsePattern(String p,int x, int y,int tX, int tY){
		pattern = new int[] {(tX-x),(tY-y)};
		
		while(limit < 8){
			rotate(p);
			if(verifyPattern(p,pattern,y,grid[tX][tY])){
				return p;
			}

		limit++;
		}
		return "error";
	}

	static boolean verifyPattern(String piece, int[] pattern, int y, String targetSquareContent){
		boolean result = false;

		if (piece.equals("pawn_w")){
				//white pawn capture OR white pawn moves one square up OR pawn is situated at (x,2) and can move two squares up
				if((targetSquareContent.charAt(targetSquareContent.length()-1) == 'b' && (Math.abs(pattern[0]) == 1 && pattern[1] == 1)) || (pattern[0] == 0 && pattern[1] == 1) || 
						(y == 2 && pattern[0] == 0 && pattern[1] == 2)){
					result = true;
				}
			
		}
		else if (piece.equals("pawn_b")){ 
			//black pawn capture OR white pawn moves one square down OR pawn is situated at (x,7) and can move two squares down
			if((targetSquareContent.charAt(targetSquareContent.length()-1) == 'w' && (Math.abs(pattern[0]) == 1 && pattern[1] == -1)) || (pattern[0] == 0 && pattern[1] == -1) || 
					(y == 7 && pattern[0] == 0 && pattern[1] == -2)){
				result = true;
			}
			
		}
		else if (piece.substring(0,6).equals("knight") && pattern[0] == 1 && pattern[1] == 2){
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
	
		if(piece.equals("Pawn_w") || piece.equals("Pawn_b")) {
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

	static void makeMove(String piece, int x, int y, int tX, int tY){
		if(grid[tX][tY] != " "){
			//scoreboard()
		}
		grid[tX][tY] = piece;
		grid[x][y] = " ";
	}

	static boolean blocking(String piece,int x,int y,int tX, int tY){
		//theres something in the way between (exclusive)(x,y) and (tX,tY)(inclusive) while piece is not a knight
		int tempX = 0;
		int tempY = 0;
		int start;
		int end;
		
		if(piece.substring(0,6).equals("knight")) {
			if((piece.charAt(piece.length()-1) != grid[tX][tY].charAt(grid[tX][tY].length()-1))) {
				return true;	
			}
		}
		else {
			if(grid[x][y] != " ") {
				return false;
			}
			
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
				start = x;
				end = tX;
			}
			
			for(int i = start; i <= end; i++) {
				
				if(tY < y) {
					tempY = -i;
				}
				else if(tY > y){
					tempY = i;
				}
				
				if(tX < x) {
					tempX = -i;
				}
				else if(tX > x){
					tempX = i;
				}

				
				if(!grid[tempX][tempY].equals(" ")) {
					return false;
				}
			}

		}
			
		return true;
	}

	static boolean king_in_check(){
		//exists different-colored piece with legal next move, with targetX,targetY = kings (x,y)
		return false;
	}

	static boolean check_mate(){
		/*
		if(
		king_in_check() == true FOR all x,y king can move &&
		blocking() = false &&
		kings color cant capture a piece to escape check){
		

		return true;
		}
		 */
		return false;
	}





	
	
}
