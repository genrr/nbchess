package main;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Platform;

public class RuleSet {
	private static int limit;
	private static int[][][] board = null;
	private static Piece[][] tempGrid = new Piece[8][8];
	private static int[] pattern;
	private static int[][] pieceMovesInfo = new int[8][];
	private static boolean wcastlingq;
	private static boolean bcastlingq;
	private static boolean wcastlingk;
	private static boolean bcastlingk;
	private static boolean draw;
	private static int turn = 0;
	private static int oTurn = 0;

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
	 * 12 promotion
	 * 13 white moves pawn by two
	 * 14 black moves pawn by two
	 * 
	 */
	
	public static int validate(int[][][] rgrid, int t, int startX, int startY,
			int targetX, int targetY) {
		
		turn = t;
		oTurn = (turn + 1) % 2;
		
		//System.out.println("validate: "+startX+","+startY+" -> "+targetX+","+targetY);
		//System.out.println("en passant: "+s[6]+" "+s[7]);
		
		bcastlingk = false;
		wcastlingk = false;
		bcastlingq = false;
		wcastlingq = false;
		
		draw = false;
		board = rgrid;
		int multX = 0;
		int multY = 0;
		MGameUtility.UpdateCoords(board);
		int specialMove;
		
		pieceMovesInfo = board[8];
		
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
		
		//System.out.println("special "+specialMove);
		
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
		
		
		//increment counter in Board class if validated current move is not a pawn move or capture(for 50/75- move rules)
		if(board[startX][startY][0] == 17 || board[startX][startY][0] == 24 || board[targetX][targetY] != null) {
			Board.setWOPMORC(0);
		}
		else {
			Board.incrementWOPMORC();
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
		
		ArrayList<int[]> opponentsAttackingPieces = CheckingPieces(turn, testBoard);
		ArrayList<int[]> ownAttackingPieces = CheckingPieces(oTurn, testBoard);
		
		//test for drawn position (3-fold/5-fold repetition, in sufficient material endgames, stalemate)
		if(checkDraw(testBoard, MGameUtility.ReturnAllPieces(board, turn),MGameUtility.ReturnAllPieces(board, oTurn))) {
			return 6;
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
		if(draw) {
			return 6;
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
		else if ((p[0] == 16 || p[0] == 23) &&
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


	private static boolean lineIsUnderAttack(int[][][] grid, int sx, int sy, int tx, int ty, int whitesTurn, boolean inclusive, boolean hostile) {
		int tempX = sx;
		int tempY = sy;
		
		
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
		boolean notDefended = true;

		//System.out.println("is line from"+sx+","+sy+" to "+tx+","+ty+" under attack?");
		
		amount = Math.max(Math.abs(sx-tx), Math.abs(sy-ty));
		
		ArrayList<int[]> allPieces = MGameUtility.ReturnAllPieces(grid, hostile ? oTurn : turn);
		
		//System.out.println("list contains "+ allPieces.get(0).getColor()+" pieces");
		
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
			
			for(int[] p : allPieces) {
				
				//System.out.println("X,Y : "+tempX+", "+tempY);
				
				//own piece can move to (tempX,tempY)
				if(MGameUtility.distance(grid, p, p[3], p[4], tempX, tempY, false) == 1) {
					
					//we're somewhere along the line, not at the end yet
					if((tempX != tx || tempY != ty)) {
						//..but king can't block the check now, obviously
						if(p[0] != 16 && p[0] != 23){
							//System.out.println("line blocked/attacked");
							return true;
						}
					}
					//tempX = tx && tempY = ty at the end point(piece itself)
					else {
						//if king can capture the piece at the end, check if the piece is defended
						if(p[0] == 16 || p[0] == 23){
							ArrayList<int[]> enemyPieces = MGameUtility.ReturnAllPieces(grid, oTurn);
							
							for (int[] q : enemyPieces) {
								//System.out.println("D: "+MGameUtility.distance(grid, q, q.getX(), q.getY(), tx, ty, false));
								if(MGameUtility.distance(grid, q, q[3], q[4], tx, ty, false) == -1) {
									//System.out.println(tx+ty+"defended by: "+q.getX()+" "+q.getY());
									//piece was defended, king cant capture,
									notDefended = false;
								}
							}
							//piece at the end is not defended, can be captured by king
							if(notDefended) {
								return true;
							}
							
							
						}
						//normal capture by some own piece
						else {
							//System.out.println("line blocked/attacked");
							return true;
						}
					}
					

					/*
					if(p.getName().contains("pawn") && p.getY() == tempY) {
						return false;
					}*/

				}
				
			}
			

		}
		//System.out.println("line free");
		
	return false;
	
	}

	private static boolean castlingAllowed(int[] pattern, int turn) {
		
		//Queenside
		if(pattern[1] == -2) {
			if(turn == 1) {
				if(board[7][1] == null && board[7][2] == null && board[7][3] == null) {
					if(pieceMovesInfo[4][0] == 0 && pieceMovesInfo[6][0] == 0 && !lineIsUnderAttack(board,7,0,7,4,turn,false,true)) {
						System.out.println("white queenside");
						wcastlingq = true;
						return true;
					}
				}
			}
			else {
				//System.out.println(rooksMoved[0]+" "+!bKingMoved+" "+!lineIsUnderAttack(board,0,0,0,4,whitesTurn,false,true));
				if(board[0][1] == null && board[0][2] == null && board[0][3] == null) {
					
					if(pieceMovesInfo[2][0] == 0 && pieceMovesInfo[7][0] == 0 && !lineIsUnderAttack(board,0,0,0,4,turn,false,true)) {
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
					if(pieceMovesInfo[5][0] == 0 && pieceMovesInfo[6][0] == 0 && !lineIsUnderAttack(board,7,4,7,7,turn,false,true)) {
						System.out.println("white kingside");
						wcastlingk = true;
						return true;
					}
				}
			}
			else {
				if(board[0][5] == null && board[0][6] == null) {
					if(pieceMovesInfo[3][0] == 0 && pieceMovesInfo[7][0] == 0 && !lineIsUnderAttack(board,0,4,0,7,turn,false,true)) {
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
				if(MGameUtility.distance(board, board[kingX][kingY], kingX, kingY, i, j, false) == 1 && 
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

			
			if(MGameUtility.distance(board, opponentsPieces.get(index), opponentsPieces.get(index)[3], 
					opponentsPieces.get(index)[4], kingX, kingY, false) == 1) {
				System.out.println("check: "+opponentsPieces.get(index)[1]
						+" in "+opponentsPieces.get(index)[3]+","+opponentsPieces.get(index)[4]);
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
		ArrayList<int[]> kingsMoves;
		//System.out.println("checkmate testing started at turn of "+turn);
		
		ArrayList<int[]> c;
		
	
		//opponents king x,y
		int kingX = MGameUtility.getKingPos(board,oTurn)[0];
		int kingY = MGameUtility.getKingPos(board,oTurn)[1];
		
		
		
		//if this variable reaches 0 after initializing it with kingsMoves size, there are no legal moves for the king
		int availableSquares = 0;
		
		//models threat
		int threat = 0;
		
		//return own pieces
		ArrayList<int[]> ownPieces = MGameUtility.ReturnAllPieces(board, turn);
		ArrayList<int[]> enemyPieces = MGameUtility.ReturnAllPieces(board, oTurn);
		//store opponent kings possible moves in here
		kingsMoves = GetKingsSquares(board,kingX,kingY);

		
		//now initializing availableSquares with the size of the array
		availableSquares = kingsMoves.size();
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
		for (int[] is : kingsMoves) {
			for(int[] p : ownPieces) {
				boolean pIsPawn = (p[0] == 17 || p[0] == 24) ? true : false; 
				//System.out.println("testing: "+p.getName()+" at ("+p.getX()+","+p.getY()+
				//		") for distance 1 to kings square ("+is[0]+","+is[1]+")");
				
				if(p[3] == is[0] && p[4] == is[1]) {
					for(int[] q : ownPieces) {
						threat = MGameUtility.distance(board, q, q[3], q[4], p[3], p[4], false);
						//piece at kings area, is defended(-1) and cannot be captured
						if(threat == -1) {
							availableSquares--;
							break;
						}
					}
					continue;
				}
				
				threat = MGameUtility.distance(board, p, p[3], p[4], is[0], is[1], false);
				
				//either enemy piece can move to kings square is[n] || pawn threatens the square(cannot move to it, though)
				if((pIsPawn && threat == -2) || !pIsPawn && threat == 1) {
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
		
		//return opponents pieces that cause check for white(for dual checks)
		 
		c = CheckingPieces(oTurn, board);		
		
		//king has no squares
		if(availableSquares == 0) {
			//two or more checking pieces while king cannot move -> checkmate
			if(c.size() > 1) {
				return true;
			}
			else if(c.size() == 1) {
				//this piece checks
				int[] p = c.get(0);
				
				//is it a knight?
				if(p[0] == 12 || p[0] == 19) {
					//is a knight, can it be captured?
					for(int[] q : enemyPieces) {
						if(MGameUtility.distance(board, q, q[3], q[4], p[3], p[4], false) == 1) {
							return false;
						}
					}
					//knight couldn't be captured -> checkmate
					return true;
				}

				//piece is not a knight, if line cannot be blocked(includes capturing the piece itself) -> checkmate
				else if(!lineIsUnderAttack(board, kingX, kingY, p[3], p[4], oTurn, true, false)) {
					System.out.println("checkmate");
					return true;
				}
			}
			//no check, if no squares for king test for any legal move, if not found -> stalemate
			else if(c.size() == 0) {
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
						draw = true;
					}
			}
			
		}
		


		//something could be done, not checkmate
		return false;
	}


	//Draws:
	//3-fold(optional) 5-fold(absolute), 50-moves without pawn moves/captures(optional draw) 75-moves(absolute draw), 
	//insufficient material:
	//king & king
	//king, bishop & king
	//king, knight & king
	//king, bishop & king, bishop (bishops same color)
	

	private static boolean checkDraw(int[][][] board, ArrayList<int[]> currentPlayersPieces, ArrayList<int[]> opponentsPieces) {
		
		ArrayList<String> pos = Board.getPosList();
		int appearances = 1;
		
		//repetition: 3-fold repetition, 5-fold repetition
		for(int i = 0; i < pos.size(); i++) {
			for(int j = 0; j < pos.size(); j++) {
				if(i != j && pos.get(i).equals(pos.get(j))) {
					//System.out.println("match found "+appearances);
					//System.out.println(pos.toString());
					appearances++;
					if(appearances == 3) {
						System.out.println("three-fold repetition");
						return true;
					}
					else if(appearances == 5) {
						System.out.println("five-fold repetition");
						return true;
					}
				}
			}
			appearances = 0;
		}

		//50-moves, 75-moves rules
		if(Board.getWOPMORC() == 50) {
			return true;
		}
		
		//test for insufficient material: 
		//king vs king
		if(currentPlayersPieces.size() == 1 && opponentsPieces.size() == 1) {
			return true;
			
		}
		//king vs king & bishop, king vs king & knight
		else if(currentPlayersPieces.size() == 1 && opponentsPieces.size() == 2 ) {
			int oid = opponentsPieces.get(0)[0];
			int oid2 = opponentsPieces.get(1)[0];
			
			if((oid == 13 || oid == 14 || oid == 20 || oid == 21) || oid == 12 || oid == 19
			 || oid2 == 13 || oid2 == 14 || oid2 == 20 || oid2 == 21 || oid2 == 12 || oid2 == 19) {
				return true;
			}
		}
		else if(opponentsPieces.size() == 1 && currentPlayersPieces.size() == 2 ) {
			int oid = currentPlayersPieces.get(0)[0];
			int oid2 = currentPlayersPieces.get(1)[0];
			
			if((oid == 13 || oid == 14 || oid == 20 || oid == 21) || oid == 12 || oid == 19
			 || oid2 == 13 || oid2 == 14 || oid2 == 20 || oid2 == 21 || oid2 == 12 || oid2 == 19) {
				return true;
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
				return true;
			}
		
		}
		

		

		
		return false;
	}


	
	
}
