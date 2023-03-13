package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Platform;

/*
 * Implementation for the rules of chess, uses helper functions from MGameUtility
 * 
 * 
 */

public class RuleSet {
	private static int[][][] board = null;
	private static int[] pattern;
	private static int[][] pieceMovesInfo = new int[8][];
	private static boolean wcastlingq;
	private static boolean bcastlingq;
	private static boolean wcastlingk;
	private static boolean bcastlingk;
	private static int drawResult = 0;
	private static int turn = 0;
	private static int enemyColor = 0;
	
	static ArrayList<int[]> opponentsAttackingPieces = null;
	static ArrayList<int[]> ownAttackingPieces = null;

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
	 * 1 if not legal for a given piece or own piece in the way, or trying to move other players piece, or castling conditions not met
	 * 2 if king in check 
	 * 3 if white wins by checkmate
	 * 4 if black wins by checkmate
	 * 5 if check
	 * 60 if drawn by insufficient material (absolute draw)
	 * 61 if draw can be requested after 3-fold repetition
	 * 62 if draw by 5-fold repetition (absolute draw)
	 * 63 if draw can be requested after 50 moves of no pawn moves or captures
	 * 64 if drawn by 75 moves after no pawn moves or captures 
	 * 65 if drawn by stalemate
	 * 7 castling black kingside
	 * 8 castling black queenside
	 * 9 castling white kingside
	 * 10 castling white queenside
	 * 11 en passant capture
	 * 12 promotion
	 * 13 white moves pawn by two
	 * 14 black moves pawn by two
	 * 
	 */
	
	public static int validate(int[][][] rgrid, int t, int startX, int startY, int targetX, int targetY) {
		
		turn = t;
		enemyColor = (turn + 1) % 2;
		//System.out.println("\nvalidate: "+startX+","+startY+" -> "+targetX+","+targetY);
		drawResult = 0;
		boolean notPawnMoveOrCapture = false;
		bcastlingk = false;
		wcastlingk = false;
		bcastlingq = false;
		wcastlingq = false;
		board = rgrid;
		MGameUtility.UpdateCoords(board);
		int specialMove;
		
		pieceMovesInfo = board[8];
		
		
		//test for moves in place
		if(startX == targetX && startY == targetY) {
			return 1;
		}
		
		//test for trying to move other players piece
		if(board[startX][startY] != null && turn != board[startX][startY][2]) {
			return 1;
		}
		
		pattern = nettyPater(startX,startY,targetX,targetY);

		//test special moves(pawn two-move, en passant,castling)
		specialMove = specialMoves(board[startX][startY],pattern,startX,targetX,targetY);
		
		
		//if castling
		if(specialMove == 3) {
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
		//test for not permitted castling/en passant
		else if(specialMove == 4) {
			return 1;
		}

	
		//test for move legality for given piece, while not castling/en passant
		if(MGameUtility.distance(board, board[startX][startY], startX, startY, targetX, targetY,false) != 1 && !(specialMove == 1 || specialMove == -1)) {
			return 1;
		}	
		
		if(board[startX][startY][5] != 1 && board[targetX][targetY] == null)
		{
			notPawnMoveOrCapture = true;
		}

		

		//test board to test the effects of the new move
		int[][][] testBoard = MGameUtility.cloneArray(board);
		testBoard[startX][startY][3] = targetX;
		testBoard[startX][startY][4] = targetY;
		
		if(specialMove == 1) {
			testBoard[targetX][targetY] = testBoard[startX][startY];
			testBoard[pieceMovesInfo[0][0]+1][pieceMovesInfo[1][0]] = null;
			testBoard[startX][startY] = null;
		}
		else if(specialMove == -1) {
			testBoard[targetX][targetY] = testBoard[startX][startY];
			testBoard[pieceMovesInfo[0][0]-1][pieceMovesInfo[1][0]] = null;
			testBoard[startX][startY] = null;
		}
		else {
			testBoard[targetX][targetY] = testBoard[startX][startY];
			testBoard[startX][startY] = null;
		}
		
		opponentsAttackingPieces = CheckingPieces(turn, testBoard);
		ownAttackingPieces = CheckingPieces(enemyColor, testBoard);
		
		checkDraw(testBoard, MGameUtility.ReturnAllPieces(testBoard, turn),MGameUtility.ReturnAllPieces(testBoard, enemyColor), notPawnMoveOrCapture);
		
		//test for drawn position (3-fold/5-fold repetition, in sufficient material endgames, stalemate)
		if(drawResult != 0) {
			return drawResult;
		}
		
		//test for for checkmate in this new position
		if(checkmateTest(turn,testBoard)) {
			if(turn == 1) {
				return 3;
			}
			else {
				return 4;
			}
		}
		//stalemate
		else if(drawResult != 0) {
			return drawResult;
		}
		
		//test if check is not resolved or new move causes check
		if(opponentsAttackingPieces.size() != 0) {
			return 2;
		}
		//test if opponents king is in check after the move, current player checks opponent
		else if(ownAttackingPieces.size() != 0) {
			return 5;
		}


		//test pawn promotion
		if(specialMove == 5) {
			return 12;
		}

		
		//dont remove en passant opportunity if pawn moved by two -> legal move
		//test for case where en passant square existed and pawn didn't move by two, en passant was not used, remove square
		else if(specialMove == 2 ){
			return 13;
		}
		else if(specialMove == -2) {
			return 14;
		}
		//if en passant move
		else if(specialMove == 1 || specialMove == -1){
			return 11;		
		}
		
		
		return 0;
		
		
	
	}
	
	public static int[] nettyPater(int sX, int sY, int tX, int tY) {
		//get a move pattern
		int[] p = new int[] {(tX-sX),(tY-sY)};
		return p;
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
	

	private static int specialMoves(int[] p, int[] pattern, int y, int tX, int tY){
		
		//moving two squares at the start + en passant
		//white pawn
		if(p[0] == 24){
			//System.out.println(pattern[0]+" "+pattern[1] + "y_"+y);
			//pawn is situated at (6,x) and moves two squares up, set en passant possibility for next move
			if(pattern[1] == 0 && pattern[0] == -2){
				if(y == 6) {
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

			if(pieceMovesInfo[0][0] != 5 && p[3] == pieceMovesInfo[0][0]+1 && (p[4] == pieceMovesInfo[1][0]-1 || p[4] == pieceMovesInfo[1][0]+1)
					&& tX == pieceMovesInfo[0][0] && tY == pieceMovesInfo[1][0]) {
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
		//black pawn
		else if (p[0] == 17){
			//System.out.println(pattern[0]+" "+pattern[1]+"y_"+y);
			//pawn is situated at (1,x) and moves two squares down, set en passant possibility for next move
			if(pattern[1] == 0 && pattern[0] == 2){
				if(y == 1) {
					return -2;
				}
				else {
					return 4;
				}
				
			}
			//en passant theoretically possible, check if current pawn is on right X row for en passant and
			//if target square is (x--,y--) OR (x--,y++)
			if(pieceMovesInfo[0][0] != 2 && p[3] == pieceMovesInfo[0][0]-1 && (p[4] == pieceMovesInfo[1][0]-1 || p[4] == pieceMovesInfo[1][0]+1)
					 && tX == pieceMovesInfo[0][0] && tY == pieceMovesInfo[1][0]) {
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
		else if ((p[5] == 6) &&
				((pattern[1] == 2 || pattern[1] == -2) && pattern[0] == 0)) {
			if(castlingAllowed(pattern, p[2])){
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




	private static boolean castlingAllowed(int[] pattern, int turn) {
		
		if(CheckingPieces(turn, board).size() != 0)
		{
			return false;
		}
		
		//Queenside
		if(pattern[1] == -2) {
			if(turn == 1) {
				if(board[7][1] == null && board[7][2] == null && board[7][3] == null) {
					if(pieceMovesInfo[4][0] == 0 && pieceMovesInfo[6][0] == 0 && !MGameUtility.lineCanBeMovedInto(board,7,0,7,4,turn,true,true)) {
						System.out.println("white queenside");
						wcastlingq = true;
						return true;
					}
				}
			}
			else {
				//System.out.println(rooksMoved[0]+" "+!bKingMoved+" "+!lineIsUnderAttack(board,0,0,0,4,whitesTurn,false,true));
				if(board[0][1] == null && board[0][2] == null && board[0][3] == null) {
					
					if(pieceMovesInfo[2][0] == 0 && pieceMovesInfo[7][0] == 0 && !MGameUtility.lineCanBeMovedInto(board,0,0,0,4,turn,true,true)) {
						System.out.println("black queenside");
						bcastlingq = true;
						return true;
					}
				}
			}
		}
		//Kingside
		else if(pattern[1] == 2) {
			if(turn == 1) {
				if(board[7][5] == null && board[7][6] == null) {
					if(pieceMovesInfo[5][0] == 0 && pieceMovesInfo[6][0] == 0 && !MGameUtility.lineCanBeMovedInto(board,7,4,7,7,turn,true,true)) {
						System.out.println("white kingside");
						wcastlingk = true;
						return true;
					}
				}
			}
			else {
				if(board[0][5] == null && board[0][6] == null) {
					if(pieceMovesInfo[3][0] == 0 && pieceMovesInfo[7][0] == 0 && !MGameUtility.lineCanBeMovedInto(board,0,4,0,7,turn,true,true)) {
						System.out.println("black kingside");
						bcastlingk = true;
						return true;
					}
				}
			}
		}
		return false;
	}




	
	
	/*
	 * Returns kings legal squares.
	 */
	
	public static ArrayList<int[]> GetKingsSquares(int[][][] board, int kingX, int kingY) {
		ArrayList<int[]> kingsMoves = new ArrayList<>();
		int opponentsColor = (board[kingX][kingY][2] + 1) % 2;
		//find kings moves
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(MGameUtility.attack(board, board[kingX][kingY], i, j) && 
						(board[i][j] == null || board[i][j][2] == opponentsColor)) {	
					//System.out.println("available for king: "+i+", "+j);
					kingsMoves.add(new int[] {i,j});
				}
				
			}
		}
		return kingsMoves;
	}

	/**@param white
	 * @param Piece[][] board
	 * @return Return the pieces that give check to <b>white</b> 
	 *
	 */
	
	
	
	private static ArrayList<int[]> CheckingPieces(int turn, int[][][] board){
		
		int index = 0;

		MGameUtility.UpdateCoords(board);
		ArrayList<int[]> opponentsPieces = MGameUtility.ReturnAllPieces(board, (turn +1) % 2); //opponents pieces
		
		ArrayList<int[]> checkingPieces = new ArrayList<int[]>();
		
		//iterate the l (array of enemy pieces) which have distance of ONE to our own King!
		//return checkingPieces
		
		int kingX = 0,kingY = 0;
		
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(board[i][j] != null && (board[i][j][0] == 16 || board[i][j][0] == 23)) {				
					if(board[i][j][2] == turn) {
						kingX = i;
						kingY = j;				
					}
				}				
			}
		}
		
		
		while(index < opponentsPieces.size())
		{
			if(MGameUtility.attack(board, opponentsPieces.get(index), kingX, kingY)) {
				checkingPieces.add(opponentsPieces.get(index));
			}
			index++;
		}
		
		return checkingPieces;
	}

/*
 * current player is checkmating?
 */
	
	private static boolean checkmateTest(int turn, int[][][] board){
		ArrayList<int[]> kingsSquares;
		//System.out.println("checkmate testing started at turn of "+turn);
		
		ArrayList<int[]> c;
		
		
		
	
		//opponents king x,y
		int kingX = MGameUtility.getKingPos(board,enemyColor)[0];
		int kingY = MGameUtility.getKingPos(board,enemyColor)[1];
		//System.out.println(board[kingX][kingY][2]);
		
		
		//if this variable reaches 0 after initializing it with kingsMoves size, there are no legal moves for the king
		int availableSquares = 0;
		
		//models threat
		int threat = 0;
		
		//return own pieces
		ArrayList<int[]> ownPieces = MGameUtility.ReturnAllPieces(board, turn);
		ArrayList<int[]> enemyPieces = MGameUtility.ReturnAllPieces(board, enemyColor);
		//store opponent kings possible moves in here
		kingsSquares = GetKingsSquares(board,kingX,kingY);

		
		//now initializing availableSquares with the size of the array
		availableSquares = kingsSquares.size();
		//System.out.println("squares: "+availableSquares);
		
		/*
		 * in the outer loop, go through kings moves array [x,y]
		 * go through own pieces, if found exact match, own piece is at kings square
		 * 		check if it is defended
		 * 		is so, decrement available squares
		 * assign to 'threat' dist between our piece and current square
		 * if result is 1(can move there) or -2(pawn attacks diagonally), decrement squares
		 * once one own piece threatens kings legal square, decrement availableSquares
		 */
		for (int[] is : kingsSquares) {
			for(int[] p : ownPieces) {
				boolean pIsPawn = (p[0] == 17 || p[0] == 24) ? true : false; 
				//our own piece p found at king square 'is'
				if(p[3] == is[0] && p[4] == is[1]) {
					for(int[] q : ownPieces) {
						//piece at kings area, is defended and cannot be captured
						if(MGameUtility.defended(board, q, p)) {
							availableSquares--;
							break;
						}
					}
					continue;
				}
				
				threat = MGameUtility.distance(board, p, p[3], p[4], is[0], is[1], false);
				
				//either enemy piece can move to kings square is[n] || pawn threatens the square(cannot move to it, though)
				if((pIsPawn && threat == -2) || !pIsPawn && threat == 1 && board[is[0]][is[1]] == null) {
					availableSquares--;
					//System.out.println("squares:"+availableSquares);
					break;
				}
			}
		}
		
		/*
		 * if there are no squares AND
		 * king is in check AND
		 * line through the king and the checking piece is not under attack = check cannot be blocked
		 * -> return true for checkmate
		 */
		
		
		 
		
		//king has no squares, so check must be blocked, or its checkmate
		if(availableSquares == 0) {
			//two or more checking pieces while king cannot move -> checkmate
			if(ownAttackingPieces.size() > 1) {
				return true;
			}
			else if(ownAttackingPieces.size() == 1) {
				//this piece checks
				int[] p = ownAttackingPieces.get(0);
				
				//is it a knight?
				if(p[5] == 3) {
					//is a knight, can it be captured?
					for(int[] q : enemyPieces) {
						if(MGameUtility.distance(board, q, q[3], q[4], p[3], p[4], false) == 1) {
							return false;
						}
					}
					//knight couldn't be captured -> checkmate
					return true;
				}
				
				else {
					//piece is not a knight, test if piece p can be captured by opponent?
					boolean defended = false;
					for(int[] h : enemyPieces)
					{
						if(MGameUtility.attack(board, h, p[3], p[4]))
						{
							defended = false;
							//is the enemy piece a king? if so, test if we are defending p by some piece q
							if(h[5] == 6)
							{
								for (int[] q : ownPieces) {
									//System.out.println("D: "+MGameUtility.distance(grid, q, q.getX(), q.getY(), tx, ty, false));
									if(MGameUtility.defended(board, q, p)) {
										//System.out.println(tx+ty+"defended by: "+q.getX()+" "+q.getY());
										//piece was defended, not legal for king
										defended = true;
										break;
									}
								}
								//p was not defended so king can capture it, not checkmate
								if(!defended) {
									return false;
								}
							}
							//enemy can capture p, so not checkmate
							else
							{
								return false;
							}
							
						}
					}
				}

				//piece is not a knight, if piece cannot be captured, then if line cannot be blocked -> checkmate
				if(!MGameUtility.lineCanBeMovedInto(board, kingX, kingY, p[3], p[4], turn, true, false)) {
					return true;
				}
			}
			//no check, if no squares for king test for any legal move, if not found -> stalemate
			else if(ownAttackingPieces.size() == 0) {
				boolean moveFound = false;
				//stalemate test for opponents pieces, did this current move cause state in which
				//no legal moves are available for opponent?
					for(int[] q : enemyPieces) {
						int i = -1;
						int j = 0;	
						
							while(!moveFound) {
								if(i < 7) {
									i++;
								}
								else {
									i = 0;
									j++;
								}
								if(j == 8) {
									break;
								}
								
								//System.out.println(q.getX()+" "+q.getY()+" to "+i+" "+j);
								//a legal move is found, draw = false
								if((q[0] != 16 && q[0] != 23) &&
									MGameUtility.distance(board, q, q[3], q[4], i, j, false) == 1) {
									moveFound = true;
								}
			
							}
	
					}
					if(!moveFound) {//STALEMATE
						//System.out.println("should not happen");
						drawResult = 65;
						return false;
					}
			}
			
		}

		return false;
	}


	//Draws:
	//3-fold(optional) 5-fold(absolute), 50-moves without pawn moves/captures(optional draw) 75-moves(absolute draw), 
	//insufficient material:
	//king & king
	//king, bishop & king
	//king, knight & king
	//king, bishop & king, bishop (bishops same color)
	

	private static void checkDraw(int[][][] board, ArrayList<int[]> currentPlayersPieces, ArrayList<int[]> opponentsPieces, boolean notPawnMoveOrCapture) {
		
		List<String> pos = Board.getPosList();
		int appearances = 1;
		
		//repetition: 3-fold repetition, 5-fold repetition
		for(int i = 0; i < pos.size(); i++) {
			for(int j = 0; j < pos.size(); j++) {
				//System.out.println(pos.toString());
				if(i != j && pos.get(i).equals(pos.get(j))) {
					//System.out.println("match found "+appearances);
					
					appearances++;
					if(appearances == 3) {
						drawResult = 61;
						
						System.out.println("three-fold repetition");
						return;
						
					}
					else if(appearances == 5) {
						drawResult = 62;
						System.out.println("five-fold repetition");
						return;
					}
				}
			}
			appearances = 0;
		}

		//50-moves, 75-moves rules
		//increment counter in Board class if validated current move is not a pawn move or capture(for 50/75- move rules)
		if(Board.getWOPMORC() == 49 && notPawnMoveOrCapture) {
			
			drawResult = 63;
			return;
		}
		else if(Board.getWOPMORC() == 74 && notPawnMoveOrCapture)
		{
			drawResult = 64;
			return;
		}
		
		//test for insufficient material: 
		//king vs king
		if(currentPlayersPieces.size() == 1 && opponentsPieces.size() == 1) {
			drawResult = 60;
			return;
		}
		//king vs king & bishop, king vs king & knight
		else if(currentPlayersPieces.size() == 1 && opponentsPieces.size() == 2 ) {
			int oid = opponentsPieces.get(0)[0];
			int oid2 = opponentsPieces.get(1)[0];
			
			if((oid == 13 || oid == 14 || oid == 20 || oid == 21) || oid == 12 || oid == 19
			 || oid2 == 13 || oid2 == 14 || oid2 == 20 || oid2 == 21 || oid2 == 12 || oid2 == 19) {
				drawResult = 60;
			}
		}
		else if(opponentsPieces.size() == 1 && currentPlayersPieces.size() == 2 ) {
			int oid = currentPlayersPieces.get(0)[0];
			int oid2 = currentPlayersPieces.get(1)[0];
			
			if((oid == 13 || oid == 14 || oid == 20 || oid == 21) || oid == 12 || oid == 19
			 || oid2 == 13 || oid2 == 14 || oid2 == 20 || oid2 == 21 || oid2 == 12 || oid2 == 19) {
				drawResult = 60;
			}
		}
		//king & bishop vs king & bishop
		else if(currentPlayersPieces.size() == 2 && opponentsPieces.size() == 2) {
			int temp1 = 0;
			int temp2 = 0;
			int id = currentPlayersPieces.get(0)[0];
			int id2 = currentPlayersPieces.get(1)[0];
			int oid = opponentsPieces.get(0)[0];
			int oid2 = opponentsPieces.get(1)[0];
			
			if(oid == 13 || oid == 14 || oid == 20 || oid == 21)  {
				temp1 = oid;
			}
			else if(oid2 == 13 || oid2 == 14 || oid2 == 20 || oid2 == 21) {
				temp1 = oid2;
			}
			if(id == 13 || id == 14 || id == 20 || id == 21)  {
				temp1 = id;
			}
			else if(id2 == 13 || id2 == 14 || id2 == 20 || id2 == 21) {
				temp1 = id2;
			}
			
			//bishops are of the same color
			if(Math.abs(temp1-temp2) == 7) {
				drawResult = 60;
			}
		
		}
		


	}


	
	
}
