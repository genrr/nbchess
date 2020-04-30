package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic {
	
	private static ArrayList<double[]> features;
	
	private static boolean drawOffered = false;
	private static ArrayList<Double> features1;
	private static ArrayList<Double> features2;
	private static ArrayList<Double> features3;
	private static ArrayList<Double> features4;
	private static ArrayList<Integer> features5;
	private static ArrayList<Integer> features6;
	private static ArrayList<Integer> features7;
	private static ArrayList<Integer> features8;
	private static ArrayList<Integer> features9;
	private static ArrayList<Double> features10;
	private static ArrayList<Double> features11;
	private static ArrayList<Double> features12;
	private static ArrayList<Double> features13;
	private static ArrayList<Double> features14;
	private static ArrayList<Integer> features15;
	private static ArrayList<Integer> features16;
	private static ArrayList<Integer> features17;
	private static ArrayList<Integer> features18;
	private static ArrayList<Integer> features19;
	private static ArrayList<Integer> features20;
	private static ArrayList<Integer> features21;
	private static ArrayList<Double> features22;
	private static ArrayList<Double> features23;
	private static ArrayList<Integer> features24;
	private static ArrayList<Double> features25;
	private static ArrayList<Double> features26;
	
	
	public static int MAIN_ITERATIONS = 32768;
	public static int RANDOMNESS_LEVEL = 4350;
	public static int RANDOMNESS_SOURCE = 0;
	public static int FUNCTION_CHOICE = 2;
	public static int RESIGN_THRESHOLD = 3;
	
	
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
	 * init features called once a game
	 */
	
	public static void InitData() {
		
		features = new ArrayList<double[]>();

		
		
	}
	
	/*
	 * init SP at the start of the game
	 */
	
	public static void InitSP() {
		StochasticSystem.initSystem(RANDOMNESS_SOURCE, RANDOMNESS_LEVEL, FUNCTION_CHOICE);
		
	}
	
	
	/*
	 * Generates move, returns it as a vector: {startX,startY,targetX,targetY}
	 * 
	 * 
	 */
	
	public static int[] Generate(Piece[][] board, int turnNumber, boolean white) {
		
		
		features.add(new double[26]);
		features.get(0)[17]++;
		
		MeasurePosition(board,white,turnNumber);
		
		//init board states
		MSystem.ComputeMetrics(board);
		StochasticSystem.inputDiffs(MSystem.ComputeDifferences(features));
		
		//start mine (generate - search - evaluate)
		int i = 0;
		while(i < MAIN_ITERATIONS) {
			//StochasticSystem.();
			//search
			//evaluate
			i++;
		}
		
	
		

		return new int[] {0,1,1,2};
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
		
		features.add(new double[26]);
		
		features.get(turnNumber-1)[0] = PositionFeature.RelM();
		features.get(turnNumber-1)[1] = PositionFeature.RelMAVG();
		features.get(turnNumber-1)[2] = PositionFeature.RelMV();
		features.get(turnNumber-1)[3] = PositionFeature.RelPVAVG();
		features.get(turnNumber-1)[4] = PositionFeature.BestPiece();
		features.get(turnNumber-1)[5] = PositionFeature.KingSquaresSafetyMetric();
		features.get(turnNumber-1)[6] = PositionFeature.RelDistanceFromDefault();
		features.get(turnNumber-1)[7] = PositionFeature.MinDistKing_Enemy();
		features.get(turnNumber-1)[8] = PositionFeature.MinDistKing_Own();
		features.get(turnNumber-1)[9] = PositionFeature.PercentThreat_Own();
		features.get(turnNumber-1)[10] = PositionFeature.PercentThreat_Own();
		features.get(turnNumber-1)[11] = PositionFeature.AmountOfPieces();
		features.get(turnNumber-1)[12] = PositionFeature.OpenSquareCount();
		features.get(turnNumber-1)[13] = PositionFeature.PercentDefended();
		features.get(turnNumber-1)[14] = PositionFeature.MostSquaresAvailableForPiece();
		features.get(turnNumber-1)[15] = PositionFeature.MostDefensesForPiece();
		features.get(turnNumber-1)[16] = PositionFeature.MostFreeSquaresForPiece();
		features.get(turnNumber-1)[17] = PositionFeature.MostSquaresSafeForPiece();
		features.get(turnNumber-1)[18] = PositionFeature.CountAllAvailableSquares();
		features.get(turnNumber-1)[19] = PositionFeature.CountAllFreeSquares();
		features.get(turnNumber-1)[20] = PositionFeature.CountAllSafeSquares();
		features.get(turnNumber-1)[21] = PositionFeature.MoveProgressionLength();
		features.get(turnNumber-1)[22] = PositionFeature.MoveProgressionBranching();
		features.get(turnNumber-1)[23] = PositionFeature.CountProgressionVisibleBranches();
		features.get(turnNumber-1)[24] = PositionFeature.MoveComplexity();
		features.get(turnNumber-1)[25] = PositionFeature.PositionComplexity();
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
		//StochasticSystem s = new StochasticSystem();
		//s.start();
		
		//StochasticSystem.initSystem(RANDOMNESS_SOURCE, RANDOMNESS_LEVEL, FUNCTION_CHOICE);
		
		PositionFeature.initFeatures(board, turnOfWhite, turnNumber);
		PositionFeature.writeAll();
		GameSystem.computeDistances();

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
