package main;

public class GameSystem {

	
	
	/*
	 * calls read() function to read the states to array
	 * gets the array -> evaluates it -> returns evaluation value
	 */
	
	public static void computeDistances() {
		
		double[] featureValues;
		double[] evaluatedValues = new double[27];
		double[] relations = new double[650];
		
		ParameterizedValueFunction.RawFeatureValueRelationReader();
		
		//get featurevalues
		featureValues = ParameterizedValueFunction.getFeatureStates();
		
		//add noise and get evaluated values for features

		for(int i = 1; i < 27; i++) {
			evaluatedValues[i] = EvaluationFunction.evaluate(i, featureValues[i] + StochasticSystem.getNoise(featureValues[i]));
		}

		relations = Relation.computeRelations(featureValues);
		
		
		//TODO: TuneFeatures() && PruneFeatures() && COmplexify
		
		
		Pipeline.inputData(evaluatedValues,relations);
		
		
	}
	
	
	
	
	public static int[] outputData() {
		
		return null;
	}
	
	
	
	public static void ReEvaluate(int[] list) {
		
	}
	
	
	public static void mine() {
		
	}


	public static int[] returnOptimalSolutions() {
		
		int[] solutions = new int[128];
		
		
		return solutions;
	}




	public static int[] generate() {
	


		
		
		return null;
	}

	
	
	
	
	


}
