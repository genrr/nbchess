package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameLogic {
	
	private static double[] heuristics;
	static Objectives o;
	private static double[][] eval = new double[3][26];
	
	private static boolean drawOffered = false;

	

	public static int positionsSPn = 16;
	public static double tldSP = 3.4;
	public static double tObj = 12.5;
	
	public static int MAIN_ITERATIONS = 32768;
	public static int RANDOMNESS_LEVEL = 4350;
	public static int RANDOMNESS_SOURCE = 0;
	public static int FUNCTION_CHOICE = 2;
	public static int RESIGN_THRESHOLD = 3;
	public static int DIST_THRESHOLD = 5;
	public static int LINES_AMOUNT = 42;
	public static int LINES_TOP = 16;
	
	
	private static double[] localSP;
	private static int[][] localO;
	private static double[] localEV;
	private static double[][] localR;
	private static double[][] localRV;
	private static ComplexSystem c;
	private static Piece[][][] localSPn;
	
	
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
		heuristics = new double[26];
		double[] initialValues = new double[26];
		
		c = new ComplexSystem(RANDOMNESS_SOURCE, RANDOMNESS_LEVEL, FUNCTION_CHOICE);
		c.start();
		
		for(int i = 0; i<26; i++) {
			initialValues[i] = 6*r.nextDouble()-3;
		}

		StandardPosition SP = new StandardPosition(initialValues);
		o = new Objectives();
	}
	
	
	
	/*
	 * Generates move, returns it as a vector: {startX,startY,targetX,targetY}
	 * 
	 * 
	 */
	
	public static int[] Generate(Piece[][] board, int turnNumber, boolean white, int[] info) {
		
		
		/*//measure
		MeasureAllHeuristics(board,white,turnNumber);
		
		//evaluate
		double[] evalValues = new double[26];
		for(int i = 0; i<26; i++) {
			evalValues[i] = evaluate(i+1, heuristics[i]);
		}
		
		
		GameSystem.optimize(board, white, evalValues, c, o, p);
		
		
		
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		//init board states
		MSystem.ComputeMetrics(board);
		
		double[] diff = new double[26];
		localSP = p.getSP();
		
		for(int i = 1; i<27; i++) {
			diff[i-1] = (features.get(turnNumber)[i-1] - localSP[i-1]);
		}
		
		localSPn = GameSystem.generateLines(diff,c,positionsSPn);
		
		//rank localSPn, get LINES_TOP amount of lines := localSPn, store rest in pipeline p, 
		
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
*/

		
		ArrayList<int[]> t = MGameUtility.getAllMoves(board, white, info);
		
		Random r = new Random();
		
		
		
		return t.get(r.nextInt(t.size()));
		/*
		
		ArrayList<int[]> candidateMoves = run(board,turnNumber,white);
		
		if(calculateDiff(candidateMoves) < RESIGN_THRESHOLD) {
			return new int[] {0};
		}
		if(drawOffered) {
			return new int[] {0,0};
		}
		
		//fill arraylist, if generated list has a sum lower than certain %, post resign flag
		//each move is in vector format of size 5: {sX,sY,tX,tY,%}
		//sort arraylist according to % (ascending)

		
		
		int[] bestMove = candidateMoves.get(0);
		bestMove[0] = candidateMoves.get(0)[0];
		bestMove[1] = candidateMoves.get(0)[1];
		bestMove[2] = candidateMoves.get(0)[2];
		bestMove[3] = candidateMoves.get(0)[3];
		 
		

		
		
		
		return bestMove; 
		
		
		
		
		*/
	}
	
	
	/*
	 * measure position
	 */
	
	private static void MeasureAllHeuristics(Piece[][] board, boolean white, int turnNumber) {
	
		
		PositionFeature p = new PositionFeature(board, white);

		
		heuristics[0] = p.RelM();
		heuristics[1] = p.RelMAVG();
		heuristics[2] = p.RelMV();
		heuristics[3] = p.RelPVAVG();
		heuristics[4] = p.BestPiece();
		//heuristics[5] = p.();
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
		
		System.out.println(Arrays.toString(heuristics));
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
		//case 6:
			
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
		case 13://TODO:
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
	
	

	private static double calculateDiff(ArrayList<int[]> candidateMoves) {
		
		int sum = 0;
		
		for (int i = 0; i < candidateMoves.size(); i++) {
			sum += candidateMoves.get(i)[4];
		}
		
		return (1.0*sum) / candidateMoves.size();
		
	}




	/**
	 * Evaluate the  board:
	 * 
	 * <li> Init PositionFeature, then call writeAll() to write all current feature states to disk
	 * <li> Use eval. functions in EvalutionFunction to compute distances from optimum state
	 * <li> Use StochasticSystem to generate optimal solution fulfilling criterias of <b>closeness to optimal state,
	 * closeness to current state</b>, output progressions
	 * <li> go through randomized data in progression to get graphs of features over time and relations between them
	 * <li> compare their connection against the value functions in ValueFunction, get tuning constants
	 * <li> apply fine.tuned eval. functions, relation importance constants, importances of features
	 * <li> save above to disk
	 * <li>
	 *
	 */

	
	
	
	/*
	 * Accepts draw if not in winning position
	 * 
	 */
	
	public static boolean DrawDecision(Piece[][] board, int turn, boolean b) {
		// TODO Auto-generated method stub
		return false;
	}
	
		

	
	
	
	private static double evaluate(int heuristic, double value) {
		
		//a*sin(b*x + c)
		
		double a = eval[0][heuristic];
		double b = eval[1][heuristic];
		double c = eval[2][heuristic];
		
		return a*Math.sin(b*value + c);
		
		
		
	}
	

	
}
