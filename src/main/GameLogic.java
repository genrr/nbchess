package main;

import java.util.ArrayList;

public class GameLogic {
	
	
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
	
	public static void setConstants(int iter, int rndlvl, int fchoice) {
		MAIN_ITERATIONS = iter;
		RANDOMNESS_LEVEL = rndlvl;
		FUNCTION_CHOICE = fchoice;
	}

	
	
	
	/*
	 * Generates move, returns it as a vector: {startX,startY,targetX,targetY}
	 * 
	 * 
	 */
	
	public static int[] generate(Piece[][] board, int turnNumber, boolean white, int[][][][][] stateVector) {
		
		ArrayList<int[]> candidateMoves = run(board,turnNumber,white);
		
		if(calculateDiff(candidateMoves) < RESIGN_THRESHOLD) {
			return new int[] {0};
		}

		
		//fill arraylist, if generated list has a sum lower than certain %, post resign flag
		
		
		//each move is in vector format of size 5: {sX,sY,tX,tY,%}
		
		
		//sort arraylist according to % (ascending)

		
		int[] bestMove = new int[4];
		bestMove[0] = candidateMoves.get(0)[0];
		bestMove[1] = candidateMoves.get(0)[1];
		bestMove[2] = candidateMoves.get(0)[2];
		bestMove[3] = candidateMoves.get(0)[3];
		 
		
		return bestMove; 
		
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
		PositionFeature.initFeatures(board, turnOfWhite, turnNumber);
		PositionFeature.writeAll();
		GameSystem.computeDistances();
		StochasticSystem.prepare(GameSystem.outputData());
		int[] progressions = StochasticSystem.generate();
		
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
		

		
		
		
		return moveList;
	
		
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
