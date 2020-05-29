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
	
	public static int[] Generate(Pipeline p, Piece[][] board, int turnNumber, boolean white) {
		
		
		/*//measure
		MeasurePosition(board,white,turnNumber);
		
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
		
		localSPn = GameSystem.optimize(diff,c,positionsSPn);
		
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

		
		ArrayList<int[]> t = MGameUtility.getAllMoves(board, white);
		
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
	
	private static void MeasurePosition(Piece[][] board, boolean white, int turnNumber) {
	
		
		PositionFeature.initFeatures(board, white, turnNumber);

		
		heuristics[0] = PositionFeature.RelM();
		heuristics[1] = PositionFeature.RelMAVG();
		heuristics[2] = PositionFeature.RelMV();
		heuristics[3] = PositionFeature.RelPVAVG();
		heuristics[4] = PositionFeature.BestPiece();
		heuristics[5] = PositionFeature.KingSquaresSafetyMetric();
		heuristics[6] = PositionFeature.RelDistanceFromDefault();
		heuristics[7] = PositionFeature.MinDistKing_Enemy();
		heuristics[8] = PositionFeature.MinDistKing_Own();
		heuristics[9] = PositionFeature.PercentThreat_Own();
		heuristics[10] = PositionFeature.PercentThreat_Own();
		heuristics[11] = PositionFeature.TradeEfficiency();
		heuristics[12] = PositionFeature.OpenSquareCount();
		heuristics[13] = PositionFeature.PercentDefended();
		heuristics[14] = PositionFeature.MostSquaresAvailableForPiece();
		heuristics[15] = PositionFeature.MostDefensesForPiece();
		heuristics[16] = PositionFeature.MostFreeSquaresForPiece();
		heuristics[17] = PositionFeature.MostSquaresSafeForPiece();
		heuristics[18] = PositionFeature.CountAllAvailableSquares();
		heuristics[19] = PositionFeature.CountAllFreeSquares();
		heuristics[20] = PositionFeature.CountAllSafeSquares();
		heuristics[21] = PositionFeature.MoveProgressionLength();
		heuristics[22] = PositionFeature.MoveProgressionBranching();
		heuristics[23] = PositionFeature.CountProgressionVisibleBranches();
		heuristics[24] = PositionFeature.PositionComplexity();
		heuristics[25] = PositionFeature.ComplexityRatio();
		
		System.out.println(Arrays.toString(heuristics));
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
	
	private static ArrayList<int[]> run(Piece[][] board, int turnNumber, boolean turnOfWhite) {

		int[] progressions = GameSystem.generate();
		
		for(int returnBlockCount = 0; returnBlockCount < MAIN_ITERATIONS; returnBlockCount++) {
			GameSystem.ReEvaluate(progressions);
			GameSystem.mine();
		}
		
		Pipeline.dataCollection(GameSystem.returnOptimalSolutions());
		ArrayList<int[]> moveList = Pipeline.getData(); //get sorted moves
		Pipeline.flush();
		Pipeline.reset();
		
		Resources.memorize();
		Resources.patternize();
		
		EvaluationFunction.writeAll(); //eval. functions tuned
		Relation.writeAll(); //rel. importances tuned
		PositionFeature.writeImportances(); //feature importances tuned
		

		if(GameSystem.offerDraw()) {
			drawOffered = true; 
		}
		
		
		
		return moveList;
	
		
	}
	
	
	
	
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static void computeRealtimeFunctions(Piece[][] board, boolean white, int turnNumber, int[][][][][] stateVector) {
		
		PositionFeature.initFeatures(board, white, turnNumber);
		
		//RealtimeFunction.computeVsTime(PositionFeature.AllAvailableSquares(false),turnNumber);
		
		//WRITE states
		PositionFeature.writeAll();
		
		//READ states, compute importance
		/*
		 * for realtime value functions: "correlation" result determines "importance" of feature in question
		 * 
		 * for evaluation value functions: "correlation" result determines, whether to shift eval. func. parameters and in which way
		 * 
		 * for relation value functions: "correlation" resultt determines 
		 */
		
		
		ParameterizedValueFunction.compute(turnNumber);
		
		EvaluationValueFunction.compute(turnNumber);
		
		RelationValueFunction.compute(turnNumber);
		
	}
	

	
}
