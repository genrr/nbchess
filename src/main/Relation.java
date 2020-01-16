package main;

public class Relation {

	
	public static double[] computeRelations(double[] rtFunctionArray) {
	
		double[] relationsMatrix = new double[650];
		int temp = 0;
		
		for(int feature1 = 1; feature1 < 27; feature1++) {
			for(int feature2 = 2; feature2 < 27; feature2++) {
				if(feature1 == feature2) {
					continue;
				}
				relationsMatrix[temp] = rtFunctionArray[feature1] / rtFunctionArray[feature2];
				temp++;
			}
		}
		
	}
}
