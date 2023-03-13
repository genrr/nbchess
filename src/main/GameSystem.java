package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;

public class GameSystem {
	
	public static int MAIN_ITERATIONS = 1;//32768;
	public static int NO_GP = 170;
	public static int RANDOMNESS_LEVEL = 4350;
	public static int RANDOMNESS_SOURCE = 0;
	public static int FUNCTION_CHOICE = 2;
	public static int SP_SIZE = 9;
	public static int RESIGN_THRESHOLD = 3;
	public static int DIST_THRESHOLD = 5;
	public static int NO_LINES = 16;
	public static int LINES_TOP = 3;
	
	static Random r = new Random();
	private static ArrayList<ArrayList<int[][][]>> lineStack = new ArrayList<ArrayList<int[][][]>>(NO_LINES);
	private static ArrayList<ArrayList<double[]>> moveStack = new ArrayList<ArrayList<double[]>>(NO_LINES);
	private static ArrayList<double[][]> SP;
	private static Objectives o;
	private static boolean resign = false;
	private static boolean drawOffered = false;
	private static boolean drawOfferedByPlayer = false;
	public static int positionsSPn = 16;
	public static double tldSP = 3.4;
	public static double tObj = 12.5;
	private static int hMax = 5;
	private static int tempLimit = 64;
	private static double dist = 0;
	private static int tries = 0;
	private static int depthMax = 5;
	private static int depthCounter = 0;
	private static int counter = 0;
	private static int totalPlies = 0;
	private static int maxPlies = 100;
	private static int ply = 0;
	private static int depth = 0;
	private static int localDepth = 0;
	private static int maxDepth = 3;
	private static int posValue;
	private static ArrayList<double[][]> repChanges = new ArrayList<>();
	private static ArrayList<double[]> diffTable = new ArrayList<>();
	private static ArrayList<double[][]> repList = new ArrayList<double[][]>();
	private static double[][] correlationTable;
	private static HashMap<int[],double[]> MCS;
	private static ArrayList<double[]> localH;
	private static ArrayList<double[]> localV;
	private static ArrayList<double[][]> localE;
	private static ArrayList<double[][][]> localR;
	private static ArrayList<double[][]> localHV;
	private static ArrayList<double[][][]> localEV;
	private static ArrayList<double[][][][]> localRV;
	private static ArrayList<String> posList = Board.getPosList();
	private static double badValueTld = 3;
	private static boolean init = true;
	/*
	 * convenience function for manual adjustment of algorithm constants
	 * 
	 * above constants:
	 * main_iterations = amount of cycles done in the progression mining phase
	 * randomness_level = amount of iterations in the fractal generator
	 * randomness_source = 
	 * function_choice = 
	 * resign_threshold = if candidateMoves has lower mean score than this, resign
	 */
	
	public static void SetConstants(int iter, int rndlvl, int fchoice, int size) {
		MAIN_ITERATIONS = iter;
		RANDOMNESS_LEVEL = rndlvl;
		FUNCTION_CHOICE = fchoice;
		SP_SIZE = size;
	}

	/*
	 * init features and
	 * init C & SP at the start of the game
	 */
	
	public static void InitData() {
		Random r = new Random();

		SP = new ArrayList<double[][]>();
		SP.add(new double[3][26]);

		for(int i = 0; i<26; i++) {
			for(int j = 0; j<3; j++) {
				SP.get(0)[j][i] = 6*r.nextDouble()-3;
			}
		}

		
	
	}
	
	
	
	
	/*
	 * Generates move, returns it as a vector: {startX,startY,targetX,targetY}
	 * 
	 * TODO: eval changing as part of the algorithm
	 * COTable implementation, Heuristic implementations
	 * 
	 */
	
	public static Message generate(int[][][] board, int turnNumber, int w, BlockingQueue<Message> storage, StochasticSystem c, ArrayList<?>[] data) {
		
		ArrayList<int[]> instructions = (ArrayList<int[]>) data[0];
		ArrayList<int[]> triggers = (ArrayList<int[]>) data[1];
		ArrayList<double[]> heur_history = (ArrayList<double[]>) data[2];
		ArrayList<double[]> val_history = (ArrayList<double[]>) data[3];
		ArrayList<double[][]> br1_history = (ArrayList<double[][]>) data[4];
		ArrayList<double[][]> br2_history = (ArrayList<double[][]>) data[5];
		ArrayList<double[][]> br3_history = (ArrayList<double[][]>) data[6];
		ArrayList<int[][][]> board_history = (ArrayList<int[][][]>) data[7];
		
		br3_history.add(new double[32][4]);
		br3_history.add(new double[16][9]);
		
		moveStack.clear();
		int[] priorityList;
		double[] evaluatedValues = new double[26];
		
		if(init)
		{
			InitData();
			init = false;
		}
		

		System.out.println("starting generation");
		/*
		 * START Move Generation
		 * 
		 * 
		 * 
		 */
		
		
		

		//store things into stuff (<?!>hooray for efficient commenting<!?>)
		heur_history.add(MeasureAllHeuristics(board, w));
		val_history.add(ValueFunction.computeValue(board, w, true));
		board_history.add(board);
		
		for(int j = 0; j<26; j++) {
			evaluatedValues[j] = evaluate(j,turnNumber,SP.get(0)) - heur_history.get(0)[j];
		}
		
		//priorityList = MGameUtility2.prioritize(evaluatedValues, hMax); //[h1,h2,h3,..]		
		
		int targetX;
		int targetY;
		
		int[] move = null;
		boolean success = false;

		System.out.println("tno "+turnNumber);
		if(turnNumber % 3 == 1)
		{
			instructions.add(Instruction.generate(board, w));
		}
			
		
		if(instructions.size() != 0)
		{
			for (int i = 0; i < instructions.size(); i++) {
				//fetch instructions
				int ins_type = instructions.get(i)[0]; 
				
				System.out.println(Arrays.toString(instructions.get(0)));
				
				ArrayList<int[]> matchedMoves;
				
				int[] source = new int[] {0,4,-1,-1,2,2,5};
				int[] target = new int[] {1,1,2,1,10,0,2,11,5};
				
				//return moves that match the instruction type with conditions
				switch(ins_type) {
				case 1:
						matchedMoves = Instruction.move_st(board, turnNumber, source, target, null, null);
					break;
				case 2:
					
					break;
				}
				
				//prune matched moves
				
				
				
				//add matched moves to potential moves
			}
			
			
			
			
			targetX = instructions.get(0)[0];
			targetY = instructions.get(0)[1];
			
			System.out.println("target is "+targetX+" "+targetY);
			
			ArrayList<int[]> moves = MGameUtility.getAllMoves(board, w);
			
			for (int[] m : moves) {
				if(m[2] == targetX && m[3] == targetY)	//move target = target square
				{
					System.out.println("found target!");
					
					move = m;
					return new Message(null, move,
							turnNumber,"ready");
				}
			}
			
			if(move == null)
			{
				
				//find highest unit value of own piece and try to defend it
				
				ArrayList<int[]> pieces = MGameUtility.ReturnAllPieces(board, w);
				double highest = MGameUtility.unitValue(pieces.get(0));
				
				int[] piece = pieces.get(0);
				
				for (int i = 0; i < pieces.size(); i++) {
					if(MGameUtility.unitValue(pieces.get(i)) > highest && !(pieces.get(i)[0] == 16 || pieces.get(i)[0] == 23))
					{
						highest = MGameUtility.unitValue(pieces.get(i));
						piece = pieces.get(i);
					}
				}
				
				System.out.println("highest piece (unit value) at ("+piece[3]+ ", "+piece[4]+")");
				
				for (int[] m : moves) 
				{
					if(m[0] == piece[3] && m[1] == piece[4])	//move Source = piece location, not a King!
					{
						System.out.println("attacking with highest value piece!");
						move = m;
						success = true;
					}
				}
				if(!success) {
					//could not defend highest valued piece, take random pawn and push it 
					ArrayList<int[]> pawns = new ArrayList<int[]>();
					
					for (int[] p : pieces) 
					{
						if(p[0] == 17 || p[0] == 24)
						{
							pawns.add(p);
						}
					}
					
					System.out.println("selecting random pawn...");
					//select random pawn
					int[] selected = pawns.get(r.nextInt(pawns.size()));
					
					for (int[] m : moves) 
					{
						if(m[0] == selected[3] && m[1] == selected[4])	//moveStart = piece(pawn) location, select the first move with the selected pawn
						{
							System.out.println("moving pawn at ("+selected[3]+", "+selected[4]+")");
							move = m;
							success = true;
						}
					}
				}
				
				

			
			}
			if(success)
			{
				int result = RuleSet.validate(board, w, move[0], move[1], move[2], move[3]);
				
				if(result == 1 || result == 2)
				{
					return new Message(null, MGameUtility.getAllMoves(board, w).get(0),
							turnNumber,"ready");
				}
				else
				{
					return new Message(null, move,
					turnNumber,"ready");
				}
				
			}
			else
			{
				return new Message(null, MGameUtility.getAllMoves(board, w).get(0),
						turnNumber,"ready");
				
			}
			
			
		}
		else
		{
			move = MGameUtility.getAllMoves(board, w).get(0);
		}
		

		//for(int i = 0; i<MAIN_ITERATIONS; i++) {
			//moveStack.add(new ArrayList<double[]>()); // add a line of moves
			//search(board,w,w,moveStack.get(moveStack.size()-1));
			//posList.clear(); //clear the poslist in anticipation of a new round, new line
			//posList.add(Resources.calculateHash(board)); //add starting position
		//}

		
	
		
		String decision = "ready";
		
		if(resign) {
			decision = "resign";
		}
		else if(drawOffered) {
			decision = "draw requested";
		}
		else if(drawOfferedByPlayer && drawOffered) {
			decision = "draw accepted";
		}
		
		
		return new Message(null, move,
				turnNumber, decision);
		

	}


	
	
	public static int[][][] makeMove(int[] move, int[][][] b){
		int id;
		int gid;
		int color;
		int[][][] board = MGameUtility.cloneArray(b);
		int result = move[4];
		int sx = move[0], sy = move[1], tx = move[2], ty = move[3];
		
		if(board[tx][ty] != null) {
			board[tx][ty][3] = -1;
			board[tx][ty][4] = -1;
		}
		
		

		board[sx][sy][3] = tx;
		board[sx][sy][4] = ty;
		
		if(result == 7) {
			board[0][7][3] = 0;
			board[0][7][4] = 5;
			board[0][5] = board[0][7];
			board[0][5] = null;
			board[0][6] = board[0][4];
			board[0][4] = null;
		}
		else if(result == 8) {
			board[0][0][3] = 0;
			board[0][0][4] = 3;
			board[0][3] = board[0][0];
			board[0][0] = null;
			board[0][2] = board[0][4];
			board[0][4] = null;
		}
		else if(result == 9) {
			board[7][7][3] = 7;
			board[7][7][4] = 5;
			board[7][5] = board[7][7];
			board[7][7] = null;
			board[7][6] = board[7][4];
			board[7][4] = null;
		}
		else if(result == 10) {
			board[7][0][3] = 7;
			board[7][0][4] = 3;
			board[7][3] = board[7][0];
			board[7][0] = null;
			board[7][2] = board[7][4];
			board[7][4] = null;
		}
		else if(result == 11) {
			board[sx][ty][3] = -1;
			board[sx][ty][4] = -1;
			board[tx][ty] = board[sx][sy];
			board[sx][sy] = null;
			board[sx][ty] = null;
		}
		else if(result == 12) {
			switch(move[5]) {
			case 'B':
				color = b[sx][sy][2];
				if(color == 1) {
					if(ty % 2 == 0) {
						id = 21;
					}
					else {
						id = 20;
					}
				
				}
				else {
					if(ty % 2 == 0) {
						id = 14;
					}
					else {
						id = 13;
					}
				}
				gid = Board.getMaxID() + 1;
				board[tx][ty] = new int[] {id,gid,color,tx,ty};
				break;
			case 'N':
				color = b[sx][sy][2];
				if(color == 1) {
					id = 19;
				}
				else {
					id = 12;
				}
				gid = Board.getMaxID() + 1;
				board[tx][ty] = new int[] {id,gid,color,tx,ty};
				break;
			case 'R':
				color = b[sx][sy][2];
				if(color == 1) {
					id = 18;
				}
				else {
					id = 11;
				}
				gid = Board.getMaxID() + 1;
				board[tx][ty] = new int[] {id,gid,color,tx,ty};
				break;
			case 'Q':
				color = b[sx][sy][2];
				if(color == 1) {
					id = 22;
				}
				else {
					id = 15; 
				}
				gid = Board.getMaxID() + 1;
				board[tx][ty] = new int[] {id,gid,color,tx,ty};
				break;
			}
			board[sx][sy] = null;

		}
		else {
			board[tx][ty] = board[sx][sy];
			board[sx][sy] = null;
		}
		
		
		if(result == 13) {
			move[6] = tx + 1;
			move[7] = ty;
		}
		else if(result == 14) {
			move[6] = tx - 1;
			move[7] = ty;
		}
		else{
			move[6] = -1;
			move[7] = -1;
		}
		
		
		posList.add(Resources.calculateHash(board));
	
		return board;
		
	}
	
	/*
	 * measure position
	 */
	
	private static double[] MeasureAllHeuristics(int[][][] board, int white) {
		
		double[] heuristics = new double[26];
		
		PositionFeature p = new PositionFeature(board, white);

		heuristics[0] = p.RelM();
		heuristics[1] = p.RelMAVG();
		heuristics[2] = p.RelMV();
		heuristics[3] = p.RelPVAVG();
		heuristics[4] = p.BestPiece();
		heuristics[5] = p.LongestPawnChain();
		heuristics[6] = p.DistanceFromDefaultRelativeToEnemy();
		heuristics[7] = p.MinDistKing_Enemy();
		heuristics[8] = p.MinDistKing_Own();
		heuristics[9] = p.PercentThreat_Own();
		heuristics[10] = p.PercentThreat_Enemy();
		heuristics[11] = p.TradeEfficiency();
		heuristics[12] = p.OpenSquareCount();
		heuristics[13] = p.PercentDefended();
		heuristics[14] = p.MostSquaresAvailableForPiece();
		heuristics[15] = p.MostDefensesForPiece();
		heuristics[16] = p.MostFreeSquaresForPiece();
		heuristics[17] = p.MostSquaresSafeForPiece();
		heuristics[18] = p.CountAllAvailableSquares();
		heuristics[19] = p.CountAllFreeSquares();
		heuristics[20] = p.CountAllSafeSquares();
		heuristics[21] = p.LongestChainOfDefenses();
		heuristics[22] = p.ChainBranching();
		heuristics[23] = p.CountDefenseLoops();
		heuristics[24] = p.PositionComplexity();
		heuristics[25] = p.ComplexityRatio();
		
		//TODO: distance from start positions
		//longest chain gives 1 while loops gives e.g. 8??
		//is 23 loops, working?
		//is 24 branching, working?
		//define 25
		
//		System.out.println("relative piece amounts: "+heuristics[0]);
//		System.out.println("piece unit value avg relative to opponent : "+heuristics[1]);
//		System.out.println("piece unit value sum relative to opponent: "+heuristics[2]);
//		System.out.println("piece average relative value: "+heuristics[3]);
//		System.out.println("best relative value "+heuristics[4]);
//		System.out.println("longest string of connected pawns : "+heuristics[5]);
//		System.out.println("distance from starting positions compared to enemy : "+heuristics[6]);
//		System.out.println("closest enemy piece: has dist of "+heuristics[7]+" to our king");
//		System.out.println("closest own piece: has dist of "+heuristics[8]+" to enemy king");
//		System.out.println("% of own pieces under threat: "+heuristics[9]);
//		System.out.println("% of enemy pieces attacked: "+heuristics[10]);
//		System.out.println("degree of trading inequalities : "+heuristics[11]);
//		System.out.println("all legal squares of rooks/bishops/queens : "+heuristics[12]);
//		System.out.println("% of own pieces defended: "+heuristics[13]);
//		System.out.println("maximal legal squares : "+heuristics[14]);
//		System.out.println("maximal defenses : "+heuristics[15]);
//		System.out.println("maximal empty & legal squares : "+heuristics[16]);
//		System.out.println("maximal legal & not threatened squares : "+heuristics[17]);
//		System.out.println("count of all legal squares : "+heuristics[18]);
//		System.out.println("count of all empty & legal squares: "+heuristics[19]);
//		System.out.println("count of all legal & not threatened squares : "+heuristics[20]);
//		System.out.println("longest chain of defenses : "+heuristics[21]);
//		System.out.println("maximal defenders / branching : "+heuristics[22]);
//		System.out.println("count of loops (e.g. p1 defends p2 defends p3 defends p1 etc..): "+heuristics[23]);
//		System.out.println("amount of computation needed to get 22,23,24 -> complexity : "+heuristics[24]);
//		System.out.println("ratio of complexities : "+heuristics[25]);
//		System.out.println();
		
		
		PositionFeature.reset();
		
		
		return heuristics;
	}
	
	
	public static double MeasureHeuristic(int[][][] board, int white, int h) {
		
		PositionFeature p = new PositionFeature(board, white);
		
		switch(h) {
		case 1:
			return p.RelM();
		case 2:
			return p.RelMAVG();
		case 3:
			return p.RelMV();
		case 4:
			return p.RelPVAVG();
		case 5:
			return p.BestPiece();
		case 6:
			return p.LongestPawnChain();
		case 7:
			return p.DistanceFromDefaultRelativeToEnemy();
		case 8:
			return p.MinDistKing_Enemy();
		case 9:
			return p.MinDistKing_Own();
		case 10:
			return p.PercentThreat_Own();
		case 11:
			return p.PercentThreat_Enemy();
		case 12:
			return p.TradeEfficiency();
		case 13:
			return p.OpenSquareCount();
		case 14:
			return p.PercentDefended();
		case 15:
			return p.MostSquaresAvailableForPiece();
		case 16:
			return p.MostDefensesForPiece();
		case 17:
			return p.MostFreeSquaresForPiece();
		case 18:
			return p.MostSquaresSafeForPiece();
		case 19:
			return p.CountAllAvailableSquares();
		case 20:
			return p.CountAllFreeSquares();
		case 21:
			return p.CountAllSafeSquares();
		case 22:
			return p.LongestChainOfDefenses();
		case 23:
			return p.ChainBranching();
		case 24:
			return p.CountDefenseLoops();
		case 25:
			return p.PositionComplexity();
		case 26:
			return p.ComplexityRatio();
		}	
		return 0;
	}
	
	

	private static double calculateDiff(ArrayList<int[][]> candidateMoves) {
		
		int sum = 0;
		
		for (int i = 0; i < candidateMoves.size(); i++) {
			for(int j = 0; j < candidateMoves.get(i)[0].length; j++) {
				sum += candidateMoves.get(i)[j][5];
			}
		}
		
		return (1.0*sum) / candidateMoves.size();
		
	}

	
	
	/*
	 * Accepts draw if not in winning position
	 * 
	 */
	
	public static boolean DrawDecision(Piece[][] board, int turn, boolean b) {
		// TODO Auto-generated method stub
		return false;
	}
	

	private static double evaluate(int heuristic, int turn, double[][] evalConst) {
		
		//a*sin(b*x + c)
		
		double a = evalConst[0][heuristic];
		double b = evalConst[1][heuristic];
		double c = evalConst[2][heuristic];
		
		return a*Math.sin(b*turn + c);
		
		
		
	}
	
	private static double evaluate(int heuristic, double[][] evalConst) {
		
		//a*sin(b*x + c)
		
		double a = evalConst[0][heuristic];
		double b = evalConst[1][heuristic];
		double c = evalConst[2][heuristic];
		
		return a*Math.sin(b + c);
		
		
		
	}
	

	
}
