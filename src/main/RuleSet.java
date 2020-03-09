package main;

import java.util.ArrayList;

import javafx.application.Platform;

public class RuleSet {
	private static int limit;
	private static Piece[][] board = null;
	private static Piece[][] tempGrid = new Piece[8][8];
	private static int[] pattern;
	private static boolean bKingMoved = false;
	private static boolean wKingMoved = false;
	private static int[] rooksMoved = {0,0,0,0};
	private static int bKingX = 0;
	private static int bKingY = 0;
	private static int wKingY = 0;
	private static int wKingX = 0;
	private static boolean wcastlingk = false;
	private static boolean wcastlingq = false;
	private static boolean bcastlingk = false;
	private static boolean bcastlingq = false;
	private static Piece checkingPiece = null;
	private static Piece enPassantPiece = null;

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
	 * 5 if check
	 * 6 if draw
	 * 7 castling bk
	 * 8 castling bq
	 * 9 castling wk
	 * 10 castling wq
	 * 11 en passant
	 */
	
	public static int validate(Piece[][] rgrid, boolean white, int startX, int startY, int targetX, int targetY) {
		board = rgrid;
		boolean checkResult = false;
		boolean isDraw;
		int multX = 0;
		int multY = 0;
		ReturnKingAndRookPositions();
		int specialMove;
	
		

		if(targetX < startX && targetY < startY){
			multX = -1;
			multY = -1;
		}
		else if(targetX < startX && targetY > startY){
			multX = -1;
			multY = 1;
		}
		else if(targetX > startX && targetY < startY){
			multX = 1;
			multY = -1;
		}
		else if(targetX > startX || targetY > startY){
			multX = 1;
			multY = 1;
		}
		
		
		//trying to move other players piece
		if(board[startX][startY] != null && ((white && !board[startX][startY].getColor()) || (!white && board[startX][startY].getColor()))) {
			return 1;
		}


		
		pattern = new int[] {(startX-targetX),(startY-targetY)};
		
		specialMove = specialMoves(board[startX][startY],board[startX][startY].getName(),pattern,startX,targetX,targetY);
		
		//check if en passant, pawn initial 2-move or castling
		if(specialMove == 1 || specialMove == -1){
			Board.setEnPassantSquare(new int[] {-1,-1});
			return 11;
			
		}
		
		else if(specialMove == 3) {
			if(bcastlingk) {
				return 7;
			}
			else if(bcastlingq) {
				return 8;
			}
			else if(wcastlingk) {
				return 9;
			}
			else if(wcastlingq) {
				return 10;
			}
		}
		else if(specialMove == 4) {
			return 1;
		}

		
		
		//not legal move for given piece or something in the way
		if(MGameStuff.distance(board, board[startX][startY], startX, startY, targetX, targetY,false) != 1) {
			return 1;
		}	
		
		
		//is drawn position?
		if(checkDraw(board,white)) {
			return 6;
		}

		//is the king currently in check?
		checkResult = king_in_check(white,board);
		Piece[][] testBoard = MGameStuff.cloneArray(board);
		testBoard[startX][startY].setCoords(targetX,targetY);
		if(testBoard[targetX][targetY] != null) {
			testBoard[targetX][targetY].setCoords(-1,-1);
		}
		
		testBoard[targetX][targetY] = testBoard[startX][startY];
		testBoard[startX][startY] = null;

		
		//black checkmates
		if(check_mate(white) && !white) {
			return 3;
		}
		//white checkmates
		else if(check_mate(white) && white) {
			return 4;
		}
		//check not resolved or new move causes check?
		else if((checkResult && king_in_check(white,testBoard)) || !checkResult && king_in_check(white,testBoard)) {
			return 2;
		}
		//opponents king is in check after the move
		else if(king_in_check(!white,testBoard)) {
			return 5;
		}
		
		//en passant opportunity was not used, remove it
		if(Board.getEnPassantSquare()[0] != -1) {
			Board.setEnPassantSquare(new int[] {-1,-1});
		}

		return 0;
		
		
	
	}
	
	
	/*
	 * Returns integer signifying the "special" move:
	 *  0 = not a special move 
	 *  4 = move is not valid castling, pawn initial two-move, or en passant move and thus not legal
	 *  1 = move is en passant for white
	 *  2 = white moves pawn two squares
	 * -1 = move is en passant for black
	 * -2 = black moves pawn two squares
	 *  3 = castling for either player
	 */
	

	private static int specialMoves(Piece p, String name, int[] pattern, int y, int tX, int tY){
		System.out.println(pattern[0]+" "+pattern[1]);
		
		//moving two squares at the start + en passant
		if (name.equals("pawn_w")){
			System.out.println(pattern[0]+" "+pattern[1] + "y_"+y);
				//pawn is situated at (x,6) and can move two squares up, set en passant possibility for next move
				if(pattern[0] == 0 && pattern[1] == 2){
					if(y == 6) {
						Board.setEnPassantSquare(new int[] {p.getX()+1,p.getY()});
						return 2;						
					}
					else {
						return 4;
					}

				}
				//en passant theoretically possible, check if current pawn is at right X row for en passant
				//then check if target square is goes forward x-- to same column as the enpassant piece
				//if en passant square exists, return 1 (legal move)
				//else, return 4 (not valid move)

				if(p.getX() != Board.getEnPassantSquare()[0]) {
					return 0;
				}
				else {
					if(tX == Board.getEnPassantSquare()[0] && tY == Board.getEnPassantSquare()[1]) {
						if(Board.getEnPassantSquare()[0] != -1) {
							return 1;
						}
						else {
							return 4;
						}
					}
					
				}

		}
		else if (name.equals("pawn_b")){
			System.out.println(y);
			System.out.println(pattern[0]+" "+pattern[1]);
			//pawn is situated at (x,1) and can move two squares down, set en passant possibility for next move
			if(pattern[0] == 0 && pattern[1] == -2){
				if(y == 1) {
					Board.setEnPassantSquare(new int[] {p.getX()-1,p.getY()});
					return -2;
				}
				else {
					return 4;
				}
				
			}
			//en passant theoretically possible, check if current pawn is on right X row for en passant and
			//if target square is (x--,y--) OR (x--,y++)
			if(p.getX() == Board.getEnPassantSquare()[0] && tX == Board.getEnPassantSquare()[0] && tY == Board.getEnPassantSquare()[1]) {
				if(Board.getEnPassantSquare()[0] != -1) {
					return -1;
				}
				else {
					return 4;
				}
			}
			else {
				return 0;
			}
			
		}
		
		//if trying to castle, check if castling allowed
		else if (name.substring(0,4).equals("king") &&
				((pattern[0] == 2 || pattern[0] == -2) && pattern[1] == 0)) {
			if(castlingAllowed(pattern, p.getColor())){
				return 3;
			}
			else {
				return 4;
			}

		}
		
		//move is not special move
		return 0;

	}

	
	private static boolean squareIsUnderAttack(int x, int y, boolean whitesTurn, boolean hostile) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(hostile) {
					if(board[i][j] != null && (board[i][j].getColor() != whitesTurn) && MGameStuff.distance(board,board[i][j], j, i, y-i, x-j,false) == 1){
						return true;
					}	
				}
				else {
					if(board[i][j] != null && (board[i][j].getColor() == whitesTurn) && MGameStuff.distance(board,board[i][j], j, i, y-i, x-j,false) == 1){
						return true;
					}	
				}
			}
		}
		return false;
	}

	private static boolean lineIsUnderAttack(Piece[][] grid, int sx, int sy, int tx, int ty, boolean whitesTurn, boolean inclusive, boolean hostile) {
		int tempX = sx;
		int tempY = sy;
		
		//(4,7) -> (7,7) amount = 3; tx > sx, tempX = 4,5,6,7
		
		int amount;
		int counter = 0;

		System.out.println("is line from"+sx+","+sy+" to "+tx+","+ty+" under attack?");
		
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
						if(grid[i][j] != null && (grid[i][j].getColor() != whitesTurn) && MGameStuff.distance(grid,grid[i][j], i, j, tempX-i, tempY-j,false) == 1){
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
						
						if(grid[i][j] != null && (grid[i][j].getColor() == whitesTurn) && MGameStuff.distance(grid,grid[i][j], i, j, tempX-i, tempY-j,false) == 1){
							System.out.println(grid[i][j].getName());
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
				if(board[7][5] == null && board[7][6] == null) {
					if(rooksMoved[3] == 0 && !wKingMoved && !lineIsUnderAttack(board,4,7,7,7,whitesTurn,false,true)) {
						wcastlingk = true;
						return true;
					}
				}
			}
			else {
				if(board[0][5] == null && board[0][6] == null) {
					if(rooksMoved[1] == 0 && !bKingMoved && !lineIsUnderAttack(board,4,0,7,0,whitesTurn,false,true)) {
						bcastlingk = true;
						return true;
					}
				}
			}
		}
		else if(pattern[0] == 2) {
			if(whitesTurn ) {
				if(board[7][1] == null && board[7][2] == null && board[7][3] == null) {
					if(rooksMoved[2] == 0 && !wKingMoved && !lineIsUnderAttack(board,0,7,3,7,whitesTurn,false,true)) {
						wcastlingq = true;
						return true;
					}
				}
			}
			else {
				if(board[0][1] == null && board[0][2] == null && board[0][3] == null) {
					if(rooksMoved[0] == 0 && !bKingMoved && !lineIsUnderAttack(board,0,0,3,0,whitesTurn,false,true)) {
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



	
	public static int[] ReturnKingAndRookPositions() {
		
		int[] t = {0,0,0,0};
		
		for(int j = 0; j < 8; j++) {
			for(int i = 0; i < 8; i++) {
				
				if(board[i][j] == null) {
					continue;
				}
				else {
					if(board[i][j].getName().contains("king") && (board[i][j].getColor())) {
						System.out.println("white king x :"+j+" y: "+i);
						wKingX = i;
						wKingY = j;
						t[0] = i;
						t[1] = j;
						if(wKingX != 7 || wKingY != 4) {
							wKingMoved = true;
						}
					}	
					else if(board[i][j].getName().contains("king") && !board[i][j].getColor()) {
						System.out.println("black king x :"+j+" y: "+i);
						bKingX = i;
						bKingY = j;
						t[2] = i;
						t[3] = j;
						if(bKingX != 0 || bKingY != 4) {
							bKingMoved = true;
						}
					}
					
					if(board[i][j].getId() == 11) {
						if(i != 0 || j != 0) {
							rooksMoved[0] = 1;
						}
					}
					else if(board[i][j].getId() == 18) {
						if(i != 0 || j != 7) {
							rooksMoved[1] = 1;
						}
					}
					else if(board[i][j].getId() == 19){
						if(i != 7 || j != 0) {
							rooksMoved[2] = 1;
						}
					}
					else if(board[i][j].getId() == 26){
						if(i != 7 || j != 7) {
							rooksMoved[3] = 1;
						}
					}
				}
			}
		}	
		
		
		return t;
	}

	/**@param white
	 * @param Piece[][] board
	 * @return Return true if player specified by <b>white</b> is in check, false otherwise.
	 *
	 */
	
	private static boolean king_in_check(boolean white, Piece[][] board){
		
		
		
		int index = 0;
	
		ArrayList<Piece> l = MGameStuff.ReturnAllPieces(board, !white);
		
		//iterate the l array for enemy pieces which have distance of ONE to our own King!
		//return 1 if found
		
		int kingX,kingY;
		
		if(white) {
			kingX = wKingX;
			kingY = wKingY;
		}
		else {
			kingX = bKingX;
			kingY = bKingY;
		}
		
		while(index < l.size())
		{
			System.out.println(MGameStuff.distance(board, l.get(index), l.get(index).getX(), l.get(index).getY(), kingX, kingY, false));
			if(MGameStuff.distance(board, l.get(index), l.get(index).getX(), l.get(index).getY(), kingX, kingY, false) == 1) {
				System.out.println("check: "+l.get(index).getName()+"in "+l.get(index).getX()+","+l.get(index).getY());
				checkingPiece = l.get(index);
				return true;
			}
			index++;
		}
		
		return false;
	}

/*
 * if turn = true
 * 		white king is in checkmate
 * 
 * if turn = false
 * 		black king is in checkmate
 */
	
	private static boolean check_mate(boolean turn){
		
		
		int kingX,kingY;
		
		if(turn) {
			kingX = wKingX;
			kingY = wKingY;
		}
		else {
			kingX = bKingX;
			kingY = bKingY;
		}
		//if this variable reaches 0 after initializing it with kingsMoves size, there is no legal moves for the king
		int availableSquares = 0;
		
		//return opposing players pieces
		ArrayList<Piece> l = MGameStuff.ReturnAllPieces(board, !turn);
		//store kings possible moves in here
		ArrayList<int[]> kingsMoves = new ArrayList<int[]>();

		//find kings moves
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(MGameStuff.distance(board, board[kingX][kingY], kingX, kingY, i, j,false) == 1) {
					
					kingsMoves.add(new int[] {i,j});
					System.out.println("square ("+i+","+j+") available for "+turn);
				}
				
			}
		}
		
		//now initializing availableSquares with the size of the array
		availableSquares = kingsMoves.size();
		
		/*
		 * in the outer loop, go through kings moves
		 * in the inner, go through opponents pieces
		 *		once opponents piece threatens kings legal square, decrement availableSquares
		 */
		for (int[] is : kingsMoves) {
			for(Piece p : l) {
				System.out.println("testing: "+p.getName()+" at ("+p.getX()+","+p.getY()+") for distance 1 to kings square ("+is[0]+","+is[1]+")");
				if(MGameStuff.distance(board, p, p.getX(), p.getY(), is[0], is[1],false) == 1) {
					
					availableSquares--;
				}
			}
		}
		
		/*
		 * if there are no squares AND
		 * king is in check AND
		 * line through the king and the checking piece is not under attack = check cannot be blocked
		 * -> return true for checkmate
		 */
		if(availableSquares == 0 && king_in_check(turn,board) && !lineIsUnderAttack(board, kingX,kingY, checkingPiece.getX(), checkingPiece.getY(), turn, true, false)) {
			return true;
		}
		
		
		
		
		
		
		return false;
	}


	
	
	

	private static boolean checkDraw(Piece[][] board, boolean color) {
		
		//TODO: stalemate
		//3-fold(optional) 5-fold(absolute), 50-moves without pawn moves/captures(optional draw) 75-moves(absolute draw), 
		//insufficient material:
		//king & king
		//king, bishop & king
		//king, knight & king
		//king, bishop & king, bishop (bishops same color)
		
		//offered draw
		
		
		return false;
	}


	
	
}
