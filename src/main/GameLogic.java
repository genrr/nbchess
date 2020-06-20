package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class GameLogic {
	
	private static boolean white;
	private static int[] info;
	private static double[] heuristics = new double[26];;
	static Objectives o;
	private static boolean resign = false;
	private static boolean drawOffered = false;
	private static boolean drawOfferedByPlayer = false;

	public static int positionsSPn = 16;
	public static double tldSP = 3.4;
	public static double tObj = 12.5;
	
	public static int MAIN_ITERATIONS = 32768;
	public static int RANDOMNESS_LEVEL = 4350;
	public static int RANDOMNESS_SOURCE = 0;
	public static int FUNCTION_CHOICE = 2;
	public static int RESIGN_THRESHOLD = 3;
	public static int DIST_THRESHOLD = 5;
	public static int LINES_AMOUNT = 16;
	public static int LINES_TOP = 3;
	
	private static ArrayList<double[]> localH;
	private static ArrayList<double[][]> localE;
	private static ArrayList<double[][][]> localR;
	private static ArrayList<double[][]> localHV;
	private static ArrayList<double[][][]> localEV;
	private static ArrayList<double[][][][]> localRV;
	
	private static ComplexSystem c;
	private static int[][] localO;
	private static boolean initialValues = false;
	
	
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
	
	public static void SetConstants(int iter, int rndlvl, int fchoice) {
		MAIN_ITERATIONS = iter;
		RANDOMNESS_LEVEL = rndlvl;
		FUNCTION_CHOICE = fchoice;
	}

	/*
	 * init features and
	 * init C & SP at the start of the game
	 */
	
	public static void InitData() {
		Random r = new Random();

		localE = new ArrayList<double[][]>();
		localE.add(new double[3][26]);

		for(int i = 0; i<26; i++) {
			for(int j = 0; j<3; j++) {
				localE.get(0)[j][i] = 6*r.nextDouble()-3;
			}
		}

		o = new Objectives();
		//c = new ComplexSystem(RANDOMNESS_SOURCE, RANDOMNESS_LEVEL, FUNCTION_CHOICE);
		//c.start();
	}
	
	
	
	/*
	 * Generates move, returns it as a vector: {startX,startY,targetX,targetY}
	 * 
	 * 
	 */
	
	public static Message Generate(Piece[][] board, int turnNumber, boolean w, int[] i, BlockingQueue<Message> storage,boolean init) {
		
		white = w;
		info = i;
		
		Message element = null;
		try {
			if(!init) {
				storage.put(new Message(null,"request P -> E"));
				System.out.println("taking");
				element = storage.take();
				
				if(element.getStatus().equals("draw requested by player")) {
					drawOfferedByPlayer = true;
				}
				
				System.out.println("element at generate: "+element);
				localH = element.hs;
				localE = element.es;
				localR = element.rs;
				localHV = element.hvs;
				localEV = element.evs;
				localRV = element.rvs;
			
				
			}
			else {
				InitData();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		//measure H
		MeasureAllHeuristics(board,white);	
		//get G
		//TODO: MSystem.ComputeMetrics(board);	
		//evaluate & find patterns
		//double[][][] patterns = new double[26][][];
		ArrayList<int[][]> lineStack = new ArrayList<int[][]>();
		lineStack.add(search(board,turnNumber));

		
		/*
		for(int z = 0; z < LINES_AMOUNT; z++) {
			lineStack.add(search(board,turnNumber));
		}
		*/

		
		/*
		//TODO: pattern search(phase 3)
		
		
			
		run lineConnect (possibly connectionSolver()) on localSPn
		
		adversary search
		
		deep search
		
		
		extrapolation
		
		
		pruning
		
		pattern evaluations
		
		zeta-limit
		
		zeta eval & output & store lines, data etc in pipeline
		p.interrupt();
		p.inputData(localSP, localO, localSPn, localEV, localR, localRV);
		
		final search, connections
		rank lines by function a*SP + b*H + c*O
		return first lines first move
		
		
		


		/* DATA STORING for the next turns
		 * 
		 * heuristics, evalValues, eval, 
		 */
		 
		
		try {
			storage.put(new Message(localH,localE,localR,localHV,localEV,localRV,"send E -> P"));
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
	
		
		/*
		 * TEMPORARY: just returns a random legal move
		 
		
		ArrayList<int[]> t = MGameUtility.getAllMoves(board, white, info);
		
		Random r = new Random();
		*/
		
		String decision = "ready";
		
		if(calculateDiff(lineStack) < RESIGN_THRESHOLD) {
			decision = "resign";
		}
		else if(drawOffered) {
			decision = "draw requested";
		}
		else if(drawOfferedByPlayer && drawOffered) {
			decision = "draw accepted";
		}
		
		
		return new Message(null,lineStack.get(0)[0],
				turnNumber,new int[] {-1,-1},decision);
		
		
		
		
		
		//fill arraylist, if generated list has a sum lower than certain %, post resign flag
		//each move is in vector format of size 5: {sX,sY,tX,tY,%}
		//sort arraylist according to % (ascending)

//		
//		
//		int[] bestMove = candidateMoves.get(0);
//		bestMove[0] = candidateMoves.get(0)[0];
//		bestMove[1] = candidateMoves.get(0)[1];
//		bestMove[2] = candidateMoves.get(0)[2];
//		bestMove[3] = candidateMoves.get(0)[3];

		
		
	}
	
	
	private static int[][] search(Piece[][] pos, int t){
		int depth = 1;
		int[][] lineStack = new int[depth][];
		ArrayList<int[]> moves;
		Piece[][] posTemp = MGameUtility.cloneArray(pos);
		Piece[][] posTemp2;
		double[] d;
		double sum = 0;
		double rating = 0;
		double maxRating = 0;
		double[] evaluatedValues = new double[26];
		int selected = 0;
		ArrayList<Double> ratingList = new ArrayList<Double>();
		ArrayList<Integer> ratedList = new ArrayList<Integer>();
		ArrayList<double[]> valueList = new ArrayList<double[]>();
		double[] evalC = new double[] {5.00, 1.5,  3.5, 3.20, 4.5, 3.0, 1.15, 5.2,  3.5,  0.1,
									   0.35, 0.0, 25.0, 0.75, 6.0, 3.2, 4.50, 3.5, 32.0, 20.0,
									   11,5,5,3,3,1};
		
		
		for(int j = 0; j<depth; j++) {
			//get moves for posTemp, initialized as pos, at the end, best move is made into posTemp
			moves = MGameUtility.getAllMoves(posTemp, white, info);
	
			//go through all moves
			for(int k = 0; k<moves.size(); k++) {
				//make the move in posTemp2
				posTemp2 = makeMove(moves.get(k),posTemp);
				System.out.println();
				//measure H
				d = MeasureAllHeuristics(posTemp2, white);
				//evaluate() gets three coefficient for a sin() function indexed by heuristic index (1..26), computes sin(), and subtracts
				//from actual heuristic value
				for(int i = 0; i<26; i++) {
					evaluatedValues[i] = Math.abs(evalC[i] - d[i]);
					//evalValues[i] = evaluate(i+1, t, localE.get(0)) - d[i];
					sum += evaluatedValues[i];
					System.out.println("sum "+sum);
					//patterns[i] = Character.mine(i+1, Math.signum(evalValues[i]),evalValues[i],element);
				}

				rating = sum/26.0;
				ratingList.add(rating);
				valueList.add(ValueFunction.computeValue(posTemp2, white, info));
				System.out.println("move: "+Arrays.toString(moves.get(k))+" rating "+rating);
				
				sum = 0;

			}
			
			maxRating = ratingList.get(0);
			
			for (int i = 0; i < ratingList.size(); i++) {
				
				//lower is better
				if(ratingList.get(i) < maxRating) {
					maxRating = ratingList.get(i);
					selected = i;
				}
				
				
			}
			ratedList.add(selected);

			
			
			
			System.out.println("best move:"+Arrays.toString(moves.get(ratedList.get(0))));
			//make the best move in posTemp
			posTemp = makeMove(moves.get(selected),posTemp);
			
			//store at lineStack
			lineStack[j] = moves.get(ratedList.get(0));
		}

		
		
		
		
		
		return lineStack;
		
	}
	
	
	public static Piece[][] makeMove(int[] move, Piece[][] b){
		Piece[][] board = MGameUtility.cloneArray(b);
		int result = move[4];
		int sx = move[0], sy = move[1], tx = move[2], ty = move[3];
		
		if(result == 7) {
			board[0][5] = board[0][7];
			board[0][5] = null;
			board[0][6] = board[0][4];
			board[0][4] = null;
		}
		else if(result == 8) {
			board[0][3] = board[0][0];
			board[0][0] = null;
			board[0][2] = board[0][4];
			board[0][4] = null;
		}
		else if(result == 9) {
			board[7][5] = board[7][7];
			board[7][7] = null;
			board[7][6] = board[7][4];
			board[7][4] = null;
		}
		else if(result == 10) {
			board[7][3] = board[7][0];
			board[7][0] = null;
			board[7][2] = board[7][4];
			board[7][4] = null;
		}
		else if(result == 11) {
			board[tx][ty] = board[sx][sy];
			board[sx][sy] = null;
			board[sx][ty] = null;
		}
		else if(result == 12) {
			//TODO: =?
		}
		else {
			board[tx][ty] = board[sx][sy];
			board[sx][sy] = null;
		}
		
		
		if(result == 13 || result == 14) {
			//TODO: enpassant square created
		}
		else if(result != 13 && result != 14) {
			//TODO: enpassant square cleared
		}
	
		return board;
		
	}
	
	/*
	 * measure position
	 */
	
	private static double[] MeasureAllHeuristics(Piece[][] board, boolean white) {
		
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
		
		PositionFeature.reset();
		
		
		System.out.println(Arrays.toString(heuristics));
		
		return heuristics;
	}
	
	
	public static double MeasureHeuristic(Piece[][] board, boolean white, int h) {
		
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
		
		double a = evalConst[0][heuristic-1];
		double b = evalConst[1][heuristic-1];
		double c = evalConst[2][heuristic-1];
		
		return a*Math.sin(b*turn + c);
		
		
		
	}
	

	
}
