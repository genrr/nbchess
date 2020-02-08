package main;

import java.util.ArrayList;

public class RuleSet {
	private static int limit;
	private static Piece[][] board = null;
	private static Piece[][] tempGrid = new Piece[8][8];
	private static int[] pattern;
	private static boolean bKingMoved = false;
	private static boolean wKingMoved = false;
	private static int[] rooksMoved = {0,0,0,0};
	private static int enemyKingY = 0;
	private static int enemyKingX = 0;
	private static int ownKingY = 0;
	private static int ownKingX = 0;
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
	 * 7 if pawn moved two squares
	 */
	
	public static int validate(Piece[][] rgrid, boolean white, int startX, int startY, int targetX, int targetY) {
		board = rgrid;
		int checkResult = -1;
		boolean isDraw;
		int multX = 0;
		int multY = 0;
		ReturnKingAndRookPositions(white);
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
			if(enPassantPiece != null) {
				return 1;
			}
			else {
				return 0;
			}
		}
		
		else if(specialMove == 3) {
			
			//handle castlings
			if(bcastlingk) {
				Piece p = board[0][7]; 
				board[0][7] = null;
				board[0][5] = p;
			}
			else if(bcastlingq){
				Piece p = board[0][0]; 
				board[0][0] = null;
				board[0][3] = p;
			}
			else if(wcastlingk) {
				Piece p = board[7][7]; 
				board[7][7] = null;
				board[7][5] = p;
			}
			else if(wcastlingq) {
				Piece p = board[7][0]; 
				board[7][0] = null;
				board[7][3] = p;
			}
			
			//quit the method when everything done about the move(s)
			return 0;
		}
		else if(specialMove == 4) {
			return 1;
		}

		
		
		//not legal move for given piece or something in the way
		if(MGameStuff.distance(board, board[startX][startY], startX, startY, targetX, targetY) != 1) {
			return 1;
		}	
		
		
		//is drawn position?
		if(checkDraw(board,white)) {
			return 6;
		}
		
		
		//is the king currently in check?
		checkResult = king_in_check(white,board);
		Piece[][] testBoard = board;
		testBoard[targetX][targetY] = board[startX][startY];
		testBoard[startX][startY] = null;
		
		//check not resolved?
		if(checkResult == 1 && king_in_check(white,testBoard) == 1) {
			return 2;
		}
		//checkmate for white
		if(check_mate(true)) {
			return 3;
		}
		//checkmate for black
		else if(check_mate(false)) {
			return 4;
		}
		//check delivered by opposing player
		else if(checkResult == 0 && king_in_check(white,testBoard) == 1) {
			return 5;
		}
		
		//en passant opportunity was not used, remove it
		if(Engine.getEnPassantSquare()[0] != -1) {
			Engine.setEnPassantSquare(new int[] {-1,-1});
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
						Engine.setEnPassantSquare(new int[] {p.getX()+1,p.getY()});
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

				if(p.getX() != Engine.getEnPassantSquare()[0]) {
					return 0;
				}
				else {
					if(tX == Engine.getEnPassantSquare()[0] && tY == Engine.getEnPassantSquare()[1]) {
						if(Engine.getEnPassantSquare()[0] != -1) {
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
					Engine.setEnPassantSquare(new int[] {p.getX()-1,p.getY()});
					return -2;
				}
				else {
					return 4;
				}
				
			}
			//en passant theoretically possible, check if current pawn is on right X row for en passant and
			//if target square is (x--,y--) OR (x--,y++)
			if(p.getX() == Engine.getEnPassantSquare()[0] && tX == Engine.getEnPassantSquare()[0] && tY == Engine.getEnPassantSquare()[1]) {
				if(Engine.getEnPassantSquare()[0] != -1) {
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
					if(board[i][j] != null && (board[i][j].getColor() != whitesTurn) && MGameStuff.distance(board,board[i][j], j, i, y-i, x-j) == 1){
						return true;
					}	
				}
				else {
					if(board[i][j] != null && (board[i][j].getColor() == whitesTurn) && MGameStuff.distance(board,board[i][j], j, i, y-i, x-j) == 1){
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
						if(grid[i][j] != null && (grid[i][j].getColor() != whitesTurn) && MGameStuff.distance(grid,grid[i][j], j, i, tempY-i, tempX-j) == 1){
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
						
						if(grid[i][j] != null && (grid[i][j].getColor() == whitesTurn) && MGameStuff.distance(grid,grid[i][j], j, i, tempY-i, tempX-j) == 1){
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



	
	public static int[] ReturnKingAndRookPositions(boolean color) {
		
		int[] t = {0,0,0,0};
		
		for(int j = 0; j < 8; j++) {
			for(int i = 0; i < 8; i++) {
				
				if(board[i][j] == null) {
					continue;
				}
				else {
					if(board[i][j].getName().contains("king") && (board[i][j].getColor() == color)) {
						System.out.println("own king x :"+j+" y: "+i);
						ownKingX = i;
						ownKingY = j;
						t[0] = i;
						t[1] = j;
						if((ownKingX != 7 || ownKingY != 4) && board[i][j].getColor()) {
							wKingMoved = true;
						}
					}	
					else if(board[i][j].getName().contains("king") && (board[i][j].getColor() != color)) {
						System.out.println("enemy king x :"+j+" y: "+i);
						enemyKingX = i;
						enemyKingY = j;
						t[2] = i;
						t[3] = j;
						if((enemyKingX != 0 || enemyKingY != 4) && !board[i][j].getColor()) {
							bKingMoved = true;
						}
					}
					
					if(board[i][j].getId() == 0) {
						if(i != 0 || j != 0) {
							rooksMoved[0] = 1;
						}
					}
					else if(board[i][j].getId() == 7) {
						if(i != 0 || j != 7) {
							rooksMoved[1] = 1;
						}
					}
					else if(board[i][j].getId() == 8){
						if(i != 7 || j != 0) {
							rooksMoved[2] = 1;
						}
					}
					else if(board[i][j].getId() == 15){
						if(i != 7 || j != 7) {
							rooksMoved[3] = 1;
						}
					}
				}
			}
		}	
		
		
		return t;
	}

	/**
	 * 
	 * @param current turn
	 * @param piece
	 * @param start X
	 * @param start Y
	 * @param target X
	 * @param target Y
	 * @return 1 if a players move causes opponent to be in check, 0 if no check 
	 */
	
	private static int king_in_check(boolean whitesTurn, Piece[][] board){
		
		int index = 0;
	
		ArrayList<Piece>l = MGameStuff.ReturnAllPieces(board, !whitesTurn);
		
		//iterate the l array for enemy pieces which have distance of ONE to our own King!
		//return 1 if found
		
		
		while(index < l.size())
		{
			if(MGameStuff.distance(board, l.get(index), l.get(index).getX(), l.get(index).getY(), ownKingX, ownKingY) == 1) {
				System.out.println(l.get(index).getName());
				checkingPiece = l.get(index);
				return 1;
			}
			index++;
		}
		
		return 0;
	}

	/**
	 * 
	 * @return Returns true if checkmate for the current player.
	 */
	
	private static boolean check_mate(boolean turn){
		
		
		
		int availableSquares = 0;
		
		ArrayList<Piece>l = MGameStuff.ReturnAllPieces(board, !turn);
		ArrayList<int[]> kingsMoves = new ArrayList<int[]>();


		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(MGameStuff.distance(board, board[ownKingX][ownKingY], ownKingX, ownKingY, i, j) == 1) {
					kingsMoves.add(new int[] {i,j});
				}
				
			}
		}
		
		availableSquares = kingsMoves.size();
		
		for (int[] is : kingsMoves) {
			for(Piece p : l) {
				if(MGameStuff.distance(board, p, p.getX(), p.getY(), is[0], is[1]) == 1) {
					availableSquares--;
				}
			}
		}
		

		if(availableSquares == 0 && king_in_check(turn,board) == 1 && !lineIsUnderAttack(board, ownKingX, ownKingY, checkingPiece.getX(), checkingPiece.getY(), turn, true, false)) {
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
