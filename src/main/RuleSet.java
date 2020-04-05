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
	 * 1 if not legal for a given piece or own piece in the way, or trying to move other players piece, castling conditions not met
	 * 2 if king in check 
	 * 3 if white wins by checkmate
	 * 4 if black wins by checkmate
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
		ReturnKingAndRookPositions(board);
		MGameUtility.UpdateCoords(board);
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


		
		pattern = new int[] {(targetX-startX),(targetY-startY)};
		
		
		//test special moves
		specialMove = specialMoves(board[startX][startY],board[startX][startY].getName(),pattern,startX,targetX,targetY);
		
		//test castling, en passant
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
		
		
		//not legal move for given piece or something in the way AND not a special move
		if(MGameUtility.distance(board, board[startX][startY], startX, startY, targetX, targetY,false) != 1 && (specialMove != 1 || specialMove != -1)) {
			return 1;
		}			
		

		//test board to test the effects of the new move
		Piece[][] testBoard = MGameUtility.cloneArray(board);
		testBoard[startX][startY].setCoords(targetX,targetY);
		if(testBoard[targetX][targetY] != null) {
			testBoard[targetX][targetY].setCoords(-1,-1);
		}
		testBoard[targetX][targetY] = testBoard[startX][startY];
		testBoard[startX][startY] = null;

		ArrayList<Piece> checkPiecesOwn = CheckingPieces(white, testBoard);
		ArrayList<Piece> checkPiecesOpponent = CheckingPieces(!white, testBoard);
		
		//does this hypothetical move cause check
		if(check_mate(white,testBoard)) {
			if(white) {
				return 3;
			}
			else {
				return 4;
			}
		}
		//check not resolved or new move causes check? = own king in check, illegal move
		else if(checkPiecesOwn.size() != 0) {
			return 2;
		}
		//opponents king is in check after the move, current player checks opponent
		else if(checkPiecesOpponent.size() != 0) {
			return 5;
		}
		
		//return original positions for both kings
		ReturnKingAndRookPositions(board);
		MGameUtility.UpdateCoords(board);

		//is drawn position?
		if(checkDraw(board,white)) {
			return 6;
		}

		System.out.println("sp"+specialMove);
		//check promotion
		if(specialMove == 5) {
			return 12;
		}
		
		//dont remove en passant opportunity if pawn moved by two, just quit 
		//if pawn didn't move by two, en passant opportunity was not used, remove it
		
		if(specialMove == 2 || specialMove == -2) {
			return 0;
		}
		else {
			if(Board.getEnPassantSquare()[0] != -1) {
				Board.setEnPassantSquare(new int[] {-1,-1});
			}
		}
		

		return 0;
		
		
	
	}
	
	
	/*
	 * Returns integer signifying the move:
	 *  0 = not a special move 
	 *  4 = move is not valid castling, pawn initial two-move, or en passant move and thus not legal
	 *  1 = move is en passant for white
	 *  2 = white moves pawn two squares
	 * -1 = move is en passant for black
	 * -2 = black moves pawn two squares
	 *  3 = castling for either player
	 *  5 = pawn promotion
	 */
	

	private static int specialMoves(Piece p, String name, int[] pattern, int y, int tX, int tY){
		System.out.println(pattern[0]+" "+pattern[1]);
		
		//moving two squares at the start + en passant
		if(name.equals("pawn_w")){
			System.out.println(pattern[0]+" "+pattern[1] + "y_"+y);
			//pawn is situated at (6,x) and moves two squares up, set en passant possibility for next move
			if(pattern[1] == 0 && pattern[0] == -2){
				if(y == 6) {
					Board.setEnPassantSquare(new int[] {tX+1,tY});
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
			if(p.getY() != Board.getEnPassantSquare()[1] && tX == Board.getEnPassantSquare()[0] && tY == Board.getEnPassantSquare()[1]) {
				return 1;
			}
			//promotion
			else if(tX == 0) {
				return 5;
			}
			else {
				return 0;
			}

			

		}
		else if (name.equals("pawn_b")){
			System.out.println(pattern[0]+" "+pattern[1]+"y_"+y);
			//pawn is situated at (1,x) and moves two squares down, set en passant possibility for next move
			if(pattern[1] == 0 && pattern[0] == 2){
				if(y == 1) {
					Board.setEnPassantSquare(new int[] {tX-1,tY});
					return -2;
				}
				else {
					return 4;
				}
				
			}
			//en passant theoretically possible, check if current pawn is on right X row for en passant and
			//if target square is (x--,y--) OR (x--,y++)
			if(p.getY() != Board.getEnPassantSquare()[1] && tX == Board.getEnPassantSquare()[0] && tY == Board.getEnPassantSquare()[1]) {
				return -1;
			}
			//promotion
			else if(tX == 7) {
				return 5;
			}
			else {
				return 0;
			}
			
		}
		
		//if trying to castle, check if castling allowed
		else if (name.substring(0,4).equals("king") &&
				((pattern[1] == 2 || pattern[1] == -2) && pattern[0] == 0)) {
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
	
	
	/**
	 * 
	 * 
	 * Method with running time l*R
	 * line length R = max(sX-tX,sY-tY)
	 * l = amount of pieces returned by returnAllPieces()
	 * 
	 * 
	 * @param grid
	 * @param sx
	 * @param sy
	 * @param tx
	 * @param ty
	 * @param whitesTurn
	 * @param inclusive
	 * @param hostile
	 * @return
	 */


	private static boolean lineIsUnderAttack(Piece[][] grid, int sx, int sy, int tx, int ty, boolean whitesTurn, boolean inclusive, boolean hostile) {
		int tempX = sx;
		int tempY = sy;
		
		System.out.println("#");
		//(4,7) -> (7,7) amount = 3; tx > sx, tempX = 4,5,6,7
		
		/*
		 * (4,7) -> (7,7)
		 * amount = 3
		 * tempX = 4
		 * tempY = 7
		 * 
		 * for z = 0
		 * tempX = 5
		 * tempY = 7
		 * if(distance p.getx, p.getY, tempX, tempY) == 1)
		 * 		if(counter == amount - 2) // counter = 0, amount -2 = 1
		 * 		if(counter == amount - 1) //counter = 0, amount - 1 = 2
		 * counter = 1
		 * 
		 * tempX = 6
		 * tempY = 7
		 * if(dist == 1)
		 * 		if(!inclusive && counter == amount - 2) // counter = 1, amount -2 = 1
		 * 			return true
		 * 		if(counter == amount - 1) // counter = 1, amount - 1 = 2)
		 * 			return true
		 * counter = 2
		 *
		 * tempX = 7
		 * tempY = 7
		 * if(dist == 1)
		 * 		if(!inclusive && counter == amount - 2) // counter = 2, amount -2 = 1
		 * 			return true
		 * 		if(counter == amount - 1) // counter = 2, amount - 1 = 2
		 * 			return true
		 * counter = 3
		 * 
		 * 
		 * 		
		 */
		
		int amount;

		System.out.println("is line from"+sx+","+sy+" to "+tx+","+ty+" under attack?");
		
		amount = Math.max(Math.abs(sx-tx), Math.abs(sy-ty));
		
		ArrayList<Piece> allPieces = MGameUtility.ReturnAllPieces(grid, hostile ? !whitesTurn : whitesTurn);
		
		//loop through the "line" from (sx,sy) -> (tx,ty) with (tempX,tempY) being the points in between
		for(int z = 0; z <= amount-1; z++ ) {
			
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
			
			for(Piece p : allPieces) {
				if(MGameUtility.distance(grid, p, p.getX(), p.getY(), tempX, tempY, false) == 1) {
					//if checking for blocking (non-hostile) the check, allow own king to capture a piece
					if(!p.getName().contains("king") && p.getColor() == whitesTurn) {
						if(!hostile && tempX == tx && tempY == ty) {
							return true;
						}
						else {
							return false;
						}
					}
					
					
					if(p.getName().contains("pawn") && p.getY() == tempY) {
						return false;
					}
					return true;
				}
				
			}
			
		}
		
	return false;
	
	}

	private static boolean castlingAllowed(int[] pattern, boolean whitesTurn) {
		System.out.println("castling?");
		
		//Queenside
		if(pattern[1] == -2) {
			if(whitesTurn) {
				if(board[7][1] == null && board[7][2] == null && board[7][3] == null) {
					if(rooksMoved[2] == 0 && !wKingMoved && !lineIsUnderAttack(board,7,0,7,4,whitesTurn,false,true)) {
						wcastlingq = true;
						return true;
					}
				}
			}
			else {
				//System.out.println(rooksMoved[0]+" "+!bKingMoved+" "+!lineIsUnderAttack(board,0,0,0,4,whitesTurn,false,true));
				if(board[0][1] == null && board[0][2] == null && board[0][3] == null) {
					if(rooksMoved[0] == 0 && !bKingMoved && !lineIsUnderAttack(board,0,0,0,4,whitesTurn,false,true)) {
						bcastlingq = true;
						return true;
					}
				}
			}
		}
		//Kingside
		else if(pattern[1] == 2) {
			if(whitesTurn) {
				if(board[7][5] == null && board[7][6] == null) {
					if(rooksMoved[3] == 0 && !wKingMoved && !lineIsUnderAttack(board,7,4,7,7,whitesTurn,false,true)) {
						wcastlingk = true;
						return true;
					}
				}
			}
			else {
				if(board[0][5] == null && board[0][6] == null) {
					if(rooksMoved[1] == 0 && !bKingMoved && !lineIsUnderAttack(board,0,4,0,7,whitesTurn,false,true)) {
						bcastlingk = true;
						return true;
					}
				}
			}
		}
		System.out.println("¤%¤");
		return false;
	}



	public static int[] ReturnKingAndRookPositions(Piece[][] board) {
		
		int[] t = {0,0,0,0};
		
		for(int j = 0; j < 8; j++) {
			for(int i = 0; i < 8; i++) {
				
				if(board[i][j] == null) {
					continue;
				}
				else {
					if(board[i][j].getName().contains("king") && (board[i][j].getColor())) {
						wKingX = i;
						wKingY = j;
						t[0] = i;
						t[1] = j;
						if(wKingX != 7 || wKingY != 4) {
							wKingMoved = true;
						}
					}	
					else if(board[i][j].getName().contains("king") && !board[i][j].getColor()) {
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
	
	private static ArrayList<Piece> CheckingPieces(boolean white, Piece[][] board){
		
		int index = 0;
	
		ReturnKingAndRookPositions(board);
		MGameUtility.UpdateCoords(board);
		ArrayList<Piece> l = MGameUtility.ReturnAllPieces(board, !white);
		ArrayList<Piece> checkingPieces = new ArrayList<Piece>();
		
		//iterate the l array for enemy pieces which have distance of ONE to our own King!
		//return true if found
		
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
			//System.out.println(MGameUtility.distance(board, l.get(index), l.get(index).getX(), l.get(index).getY(), kingX, kingY, false));
			
			if(MGameUtility.distance(board, l.get(index), l.get(index).getX(), l.get(index).getY(), kingX, kingY, false) == 1) {
				//System.out.println("check: "+l.get(index).getName()+"in "+l.get(index).getX()+","+l.get(index).getY());
				checkingPieces.add(l.get(index));
			}
			index++;
		}
		
		return checkingPieces;
	}

/*
 * "turn" king is in checkmate?
 */
	
	private static boolean check_mate(boolean turn, Piece[][] board){
		
		System.out.println("checkmate testing started at turn of "+turn);
		
		int kingX,kingY;
		ArrayList<Piece> c;
		
		if(!turn) {
			kingX = wKingX;
			kingY = wKingY;
		}
		else {
			kingX = bKingX;
			kingY = bKingY;
		}
		//if this variable reaches 0 after initializing it with kingsMoves size, there are no legal moves for the king
		int availableSquares = 0;
		
		//return own pieces
		ArrayList<Piece> opponentsPieces = MGameUtility.ReturnAllPieces(board, turn);
		//store opponents possible king moves in here
		ArrayList<int[]> kingsMoves = new ArrayList<int[]>();

		//find kings moves
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(MGameUtility.distance(board, board[kingX][kingY], kingX, kingY, i, j,false) == 1) {
					
					kingsMoves.add(new int[] {i,j});
					System.out.println("square ("+i+","+j+") available for "+!turn);
				}
				
			}
		}
		
		//now initializing availableSquares with the size of the array
		availableSquares = kingsMoves.size();
		
		/*
		 * in the outer loop, go through kings moves
		 * in the inner, go through opponents pieces
		 * once opponents piece threatens kings legal square, decrement availableSquares
		 */
		for (int[] is : kingsMoves) {
			for(Piece p : opponentsPieces) {
				System.out.println("testing: "+p.getName()+" at ("+p.getX()+","+p.getY()+") for distance 1 to kings square ("+is[0]+","+is[1]+")");
				if(MGameUtility.distance(board, p, p.getX(), p.getY(), is[0], is[1],false) == 1) {
					
					availableSquares--;
					System.out.println("sq:"+availableSquares);
				}
			}
		}
		
		/*
		 * if there are no squares AND
		 * king is in check AND
		 * line through the king and the checking piece is not under attack = check cannot be blocked
		 * -> return true for checkmate
		 */
		
		c = CheckingPieces(!turn, board);
		System.out.println("squares: "+availableSquares);
		System.out.println("checking pieces: "+c.size());
		//System.out.println("piece0: ("+c.get(0).getX()+","+c.get(0).getY()+")");
		
		if(availableSquares == 0) {
			for(Piece p : c) {
				if(!lineIsUnderAttack(board, kingX, kingY, p.getX(), p.getY(), !turn, true, false)) {
					return true;
				}
			}
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
