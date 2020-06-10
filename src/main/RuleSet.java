package main;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Platform;

public class RuleSet {
	private static int limit;
	private static Piece[][] board = null;
	private static Piece[][] tempGrid = new Piece[8][8];
	private static int[] pattern;
	private static int[] pieceMovesInfo = new int[7];
	private static boolean wcastlingq;
	private static boolean bcastlingq;
	private static boolean wcastlingk;
	private static boolean bcastlingk;


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
	
	public static int validate(Piece[][] rgrid, boolean white, int startX, int startY,
			int targetX, int targetY, int[] s) {
		
		//System.out.println("validate: "+startX+","+startY+" -> "+targetX+","+targetY);
		//System.out.println("en passant: "+s[6]+" "+s[7]);
		
		board = rgrid;
		int multX = 0;
		int multY = 0;
		MGameUtility.UpdateCoords(board);
		int specialMove;
		pieceMovesInfo = s;
		
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
		if(board[startX][startY] != null && ((white && !board[startX][startY].getColor()) || (!white && board[startX][startY].getColor()))) {
			return 1;
		}

			
		pattern = nettyPater(startX,startY,targetX,targetY);
		
		
		//test special moves(pawn two-move, en passant,castling)
		specialMove = specialMoves(board[startX][startY],board[startX][startY].getName(),pattern,startX,targetX,targetY);
		
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
		if(board[startX][startY].getName().contains("pawn") || board[targetX][targetY] != null) {
			Board.setWOPMORC(0);
		}
		else {
			Board.incrementWOPMORC();
		}
		

		//test board to test the effects of the new move
		Piece[][] testBoard = MGameUtility.cloneArray(board);
		if(specialMove == 1) {
			testBoard[startX][startY].setCoords(targetX,targetY);
			testBoard[targetX][targetY] = testBoard[startX][startY];
			testBoard[pieceMovesInfo[6]+1][pieceMovesInfo[7]] = null;
			testBoard[startX][startY] = null;
		}
		else if(specialMove == -1) {
			testBoard[startX][startY].setCoords(targetX,targetY);
			testBoard[targetX][targetY] = testBoard[startX][startY];
			testBoard[pieceMovesInfo[6]-1][pieceMovesInfo[7]] = null;
			testBoard[startX][startY] = null;
		}
		else {
			testBoard[startX][startY].setCoords(targetX,targetY);
			testBoard[targetX][targetY] = testBoard[startX][startY];
			testBoard[startX][startY] = null;
		}
		
		ArrayList<Piece> opponentsAttackingPieces = CheckingPieces(white, testBoard);
		ArrayList<Piece> ownAttackingPieces = CheckingPieces(!white, testBoard);
		
		//test for drawn position (3-fold/5-fold repetition, in sufficient material endgames, stalemate)
		if(checkDraw(testBoard, MGameUtility.ReturnAllPieces(board, white),MGameUtility.ReturnAllPieces(board, !white))) {
			return 6;
		}
		
		//test for for checkmate in this new position
		if(checkmateTest(white,testBoard)) {
			if(white) {
				return 3;
			}
			else {
				return 4;
			}
		}
		//test if check is not resolved or new move causes check
		else if(opponentsAttackingPieces.size() != 0) {
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
	

	private static int specialMoves(Piece p, String name, int[] pattern, int y, int tX, int tY){
		
		//moving two squares at the start + en passant
		if(name.equals("pawn_w")){
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

			if(p.getX() == pieceMovesInfo[6]+1 && (p.getY() == pieceMovesInfo[7]-1 || p.getY() == pieceMovesInfo[7]+1)
					&& tX == pieceMovesInfo[6] && tY == pieceMovesInfo[7]) {
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
			if(p.getX() == pieceMovesInfo[6]-1 && (p.getY() == pieceMovesInfo[7]-1 || p.getY() == pieceMovesInfo[7]+1)
					 && tX == pieceMovesInfo[6] && tY == pieceMovesInfo[7]) {
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
		
		System.out.println("list contains "+ allPieces.get(0).getColor()+" pieces");
		
		//loop through the "line" from (sx,sy) -> (tx,ty) with (tempX,tempY) being the points in between
		for(int z = 0; z <= amount-1; z++ ) {

			for(Piece p : allPieces) {
				
				System.out.println("X,Y : "+tempX+", "+tempY);
				
				if(!p.getName().contains("king") && MGameUtility.distance(grid, p, p.getX(), p.getY(), tempX, tempY, false) == 1) {

					/*
					if(p.getName().contains("pawn") && p.getY() == tempY) {
						return false;
					}
					*/
					
					return true;
				}
				
			}
			
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
		}
		
	return false;
	
	}

	private static boolean castlingAllowed(int[] pattern, boolean whitesTurn) {
		
		//Queenside
		if(pattern[1] == -2) {
			if(whitesTurn) {
				if(board[7][1] == null && board[7][2] == null && board[7][3] == null) {
					if(pieceMovesInfo[2] == 0 && pieceMovesInfo[4] == 0 && !lineIsUnderAttack(board,7,0,7,4,whitesTurn,false,true)) {
						wcastlingq = true;
						return true;
					}
				}
			}
			else {
				//System.out.println(rooksMoved[0]+" "+!bKingMoved+" "+!lineIsUnderAttack(board,0,0,0,4,whitesTurn,false,true));
				if(board[0][1] == null && board[0][2] == null && board[0][3] == null) {
					
					if(pieceMovesInfo[0] == 0 && pieceMovesInfo[5] == 0 && !lineIsUnderAttack(board,0,0,0,4,whitesTurn,false,true)) {
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
					System.out.println("checking");
					System.out.println((pieceMovesInfo[3] == 0)+" "+(pieceMovesInfo[4] == 0));
					if(pieceMovesInfo[3] == 0 && pieceMovesInfo[4] == 0 && !lineIsUnderAttack(board,7,4,7,7,whitesTurn,false,true)) {		
						wcastlingk = true;
						return true;
					}
				}
			}
			else {
				if(board[0][5] == null && board[0][6] == null) {
					if(pieceMovesInfo[1] == 0 && pieceMovesInfo[5] == 0 && !lineIsUnderAttack(board,0,4,0,7,whitesTurn,false,true)) {
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
	
	public static ArrayList<int[]> GetKingsSquares(int kingX, int kingY) {
		ArrayList<int[]> kingsMoves = new ArrayList<>();
		//find kings moves
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(MGameUtility.distance(board, board[kingX][kingY], kingX, kingY, i, j,false) == 1 && (i != kingX  || j != kingY)) {	
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
	
	
	
	private static ArrayList<Piece> CheckingPieces(boolean white, Piece[][] board){
		
		int index = 0;

		MGameUtility.UpdateCoords(board);
		ArrayList<Piece> opponentsPieces = MGameUtility.ReturnAllPieces(board, !white); //opponents pieces
		
		ArrayList<Piece> checkingPieces = new ArrayList<Piece>();
		
		//iterate the l (array of enemy pieces) which have distance of ONE to our own King!
		//return checkingPieces
		
		int kingX = 0,kingY = 0;
		
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(board[i][j] != null && board[i][j].getName().contains("king")) {
					
					if(board[i][j].getColor() == white) {
						kingX = i;
						kingY = j;				
					}

					
				}
				
			}
		}
		
		
		while(index < opponentsPieces.size())
		{

			
			if(MGameUtility.distance(board, opponentsPieces.get(index), opponentsPieces.get(index).getX(), 
					opponentsPieces.get(index).getY(), kingX, kingY, false) == 1) {
				System.out.println("check: "+opponentsPieces.get(index).getName()
						+" in "+opponentsPieces.get(index).getX()+","+opponentsPieces.get(index).getY());
				checkingPieces.add(opponentsPieces.get(index));
			}
			index++;
		}
		
		return checkingPieces;
	}

/*
 * "turn" king is in checkmate?
 */
	
	private static boolean checkmateTest(boolean turn, Piece[][] board){
		ArrayList<int[]> kingsMoves;
		//System.out.println("checkmate testing started at turn of "+turn);
		
		ArrayList<Piece> c;
		
		//opponents king x,y
		int kingX = 0,kingY = 0;
		
		for(int i = 0; i<8; i++) {
			for(int j = 0; j<8; j++) {
				if(board[i][j] != null && board[i][j].getName().contains("king")) {
					if(board[i][j].getColor() == !turn) {
						kingX = i;
						kingY = j;				
					}
				}
			}
		}
		
		//if this variable reaches 0 after initializing it with kingsMoves size, there are no legal moves for the king
		int availableSquares = 0;
		
		//models threat
		int threat = 0;
		
		//return own pieces
		ArrayList<Piece> ownPieces = MGameUtility.ReturnAllPieces(board, turn);
		//store opponent kings possible moves in here
		kingsMoves = GetKingsSquares(kingX,kingY);

		
		//now initializing availableSquares with the size of the array
		availableSquares = kingsMoves.size();
		//System.out.println("squares: "+availableSquares);
		
		/*
		 * in the outer loop, go through kings moves
		 * in the inner, go through opponents pieces
		 * once one own piece threatens kings legal square, decrement availableSquares
		 */
		for (int[] is : kingsMoves) {
			for(Piece p : ownPieces) {
				//System.out.println("testing: "+p.getName()+" at ("+p.getX()+","+p.getY()+
				//		") for distance 1 to kings square ("+is[0]+","+is[1]+")");
				
				if(p.getX() == is[0] && p.getY() == is[1]) {
					for(Piece q : ownPieces) {
						threat = MGameUtility.distance(board, q, q.getX(), q.getY(), p.getX(), p.getY(), false);
						//piece at kings area, is defended and cannot be captured
						if(threat == 1) {
							availableSquares--;
							break;
						}
					}
					continue;
				}
				
				threat = MGameUtility.distance(board, p, p.getX(), p.getY(), is[0], is[1], false);
				
				//either enemy piece can move to kings square is[n] || pawn threatens the square(cannot move to it, though)
				if(threat == 1 || threat == -2) {
					
					availableSquares--;
					//System.out.println("sq:"+availableSquares);
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
		//checking pieces give pieces that give check to current player(US), we want pieces that we're giving check OURSELVES, so hard.. 
		c = CheckingPieces(!turn, board);
		//System.out.println("squares: "+availableSquares);
		//System.out.println("checking pieces: "+c.size());
		//System.out.println("piece0: ("+c.get(0).getX()+","+c.get(0).getY()+")");
		
		
		if(availableSquares == 0) {
			//two or more checking pieces while king cannot move -> checkmate
			if(c.size() > 1) {
				return true;
			}
			
			for(Piece p : c) {

				// piece givin' a check is a knight
				if(p.getName().contains("knight")) {

					
					for(Piece q : MGameUtility.ReturnAllPieces(board, !turn)) {
						if(MGameUtility.distance(board, q, q.getX(), q.getY(), p.getX(), p.getY(), false) == 1) {
							return false;
						}
					}
					//knight couldn't be captured -> checkmate
					return true;
				}

				//one piece givin' check, that can't be blocked -> checkmate
				
				if(!lineIsUnderAttack(board, kingX, kingY, p.getX(), p.getY(), !turn, true, false)) {
					System.out.println("checkmate");
					return true;
				}
			}
		}

		return false;
	}


	//Draws:
	//stalemate
	//3-fold(optional) 5-fold(absolute), 50-moves without pawn moves/captures(optional draw) 75-moves(absolute draw), 
	//insufficient material:
	//king & king
	//king, bishop & king
	//king, knight & king
	//king, bishop & king, bishop (bishops same color)
	

	private static boolean checkDraw(Piece[][] board, ArrayList<Piece> currentPlayersPieces, ArrayList<Piece> opponentsPieces) {
		
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
			if(opponentsPieces.get(0).getName().contains("bishop") || opponentsPieces.get(0).getName().contains("knight")
					|| opponentsPieces.get(1).getName().contains("bishop") || opponentsPieces.get(1).getName().contains("knight")) {
				return true;
			}
		}
		else if(opponentsPieces.size() == 1 && currentPlayersPieces.size() == 2 ) {
			if(currentPlayersPieces.get(0).getName().contains("bishop") || currentPlayersPieces.get(0).getName().contains("knight")
					|| currentPlayersPieces.get(1).getName().contains("bishop") || currentPlayersPieces.get(1).getName().contains("knight")) {
				return true;
			}
		}
		//king & bishop vs king & bishop
		else if(currentPlayersPieces.size() == 2 && opponentsPieces.size() == 2) {
			int temp1 = 0;
			int temp2 = 0;
			if(currentPlayersPieces.get(0).getName().contains("bishop")) {
				temp1 = currentPlayersPieces.get(0).getId();
			}
			else if(currentPlayersPieces.get(1).getName().contains("bishop")) {
				temp1 = currentPlayersPieces.get(0).getId();
			}
			if(opponentsPieces.get(0).getName().contains("bishop")) {
				temp2 = opponentsPieces.get(0).getId();
			}
			else if(opponentsPieces.get(1).getName().contains("bishop")) {
				temp2 = opponentsPieces.get(0).getId();
			}
			
			//bishops are of the same color
			if(Math.abs(temp1-temp2) == 7) {
				return true;
			}
		
		}
		
		
		//stalemate test for opponents pieces, did this current move cause state in which
		//no legal moves are available for opponent?
		for(Piece p : opponentsPieces) {
			for(int i = 0; i<8; i++) {
				for(int j = 0; j<8; j++) {
					//a legal move is found
					if(MGameUtility.distance(board, p, p.getX(), p.getY(), i, j, false) == 1) {
						return false;
					}
				}
			}
		}
		

		
		return true;
	}


	
	
}
