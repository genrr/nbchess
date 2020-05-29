package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameSystem {

	private static ComplexSystem c;
	




	public static Piece[][][] optimize(Piece[][] board,boolean white, double[] evaluation, ComplexSystem csys, Objectives O, Pipeline pipeline) {
	
		c = csys;
		ArrayList<Integer> actionList = new ArrayList<Integer>();
		ArrayList<int[]> coeffList = new ArrayList<int[]>();
		Piece[][][] posStack = new Piece[GameLogic.LINES_AMOUNT][][];
		
		int[] eValues = new int[26];

		
		for (int i = 0; i < evaluation.length; i++) {
			if(Math.abs(evaluation[i]) > 15) {
				eValues[i] = 5;
			}
			else if(Math.abs(evaluation[i]) > 8) {
				eValues[i] = 4;
			}
			else if(Math.abs(evaluation[i]) > 5) {
				eValues[i] = 3;
			}
			else if(Math.abs(evaluation[i]) > 3) {
				eValues[i] = 2;
			}
			else {
				eValues[i] = 1;
			}
		}
		

		
		Arrays.sort(eValues);
		
		for (int i = 0; i < eValues.length; i++) {
			if(eValues[i] != 1) {
				int[] temp = actionMapping(i,(int)Math.signum(evaluation[i]),eValues[i]);
				int[] temp2 = coeffMapping(eValues[i]);
				
				for(int k= 0; k<temp2[0]; k++) {
					actionList.add(temp[0]);
				}
				
				for(int k= 0; k<temp2[1]; k++) {
					actionList.add(temp[1]);
				}
				
				for(int k= 0; k<temp2[2]; k++) {
					actionList.add(temp[2]);
				}

				//TODO: merge duplicates

			}
			
		}
		
		Arrays.sort(actionList.toArray());
		
		
		for(int j = 0; j<GameLogic.LINES_AMOUNT; j++) {
			posStack[j] = fillPosition(board,white,evaluation,actionList,c,O,pipeline);
		}
		
		
		
		/*
		actionlist [19][27][11]
		coefflist  [ 8][ 5][ 1]
		
		actionlist [10][ 1][12]
		coefflist  [ 5][ 3][ 0]
		
		actionlist [18][11][29]
		coefflist  [14][ 2][ 0]

		
		*/

		
		//expand lines at
		//phase 3.
		
		
		return posStack;
	}

	
	private static int[] actionMapping(int feature, int sign, int e) {
		
		int[] actions = null;

		
		switch(feature) {
		case 0:
			if(sign == 1) {
				actions = new int[] {};
	
			}
			else if(sign == -1) {
				
			}
		}
		
		
		return actions;
		
	}
	
	private static int[] coeffMapping(int e){
		int[] multipliers = new int[3];
		
		double multValue = Math.pow(2, e);
		Random r = new Random();
		for(int i = 0; i < 3; i++) {
			multipliers[i] = (int) Math.round(multValue * Math.log(-(3*r.nextDouble()+1)/2.0 + 3));
		}
		
		return multipliers;
	}
	
	
	private static Piece[][] fillPosition(Piece[][] pos0, boolean white, double[] diff, ArrayList<Integer> actions, ComplexSystem c, Objectives o, Pipeline pipeline) {
		Piece[][] pos;
		Piece[][][] posList = new Piece[3][][];
		ArrayList<Piece> l1 = MGameUtility.ReturnAllPieces(pos0, white);
		ArrayList<Piece> l2 = MGameUtility.ReturnAllPieces(pos0, !white);
		
		
	    double[] qualityCoeff = new double[] {16.0,16.0,16.0,16.0};
	    double percentOfPieces = 0.2;
	    int genTries = 64;
	    int posOptimization = 1;
	    int passes;
	    int passTries;
	    double temp;
	    int d = GameLogic.DIST_THRESHOLD;
	    
	    while(genTries > 0){
	    
	        
	        //generate until pos passes valuecontrol
	        do{
	            pos = MGameUtility.generatePos(pos0,l1,l2,diff,o,posOptimization);
	            qualityCoeff[0] -= 0.1;
	            qualityCoeff[1] -= 0.1;
	            qualityCoeff[2] -= 0.1;
	            qualityCoeff[3] -= 0.1;
	            
	            genTries--;
	            
	        }
	        while(!ValueFunction.valuesCheck(pos,white,qualityCoeff));
	        
	        
	        if (MGameUtility.posDist(pos0,pos) < d){
	                //12x passes of b10 <=> actions contains 12x of b10
	                //passTries = 20;
	                
	                for(int i = 0; i<actions.size(); i++){
	                    //check current action(character) element at random location
	                    if (!MGameUtility.checkCharacter(pos,actions.get(i))){
	                        //move pieces according to pattern: b10 at pos[i]
	                        if(!ValueFunction.valuesCheck(pos,white,qualityCoeff)){
	                            qualityCoeff[0] -= 0.1;
	                            qualityCoeff[1] -= 0.1;
	                            qualityCoeff[2] -= 0.1;
	                            qualityCoeff[3] -= 0.1;

	                            
	                            continue;
	                        }
	                    }
	                    //passTries--;
	                }
	                if(passes <= 12/5){
	                    posList[0] = pos; //-> B1
	                }
	                continue;

	                
	        }
	        else{
	            genTries--;
	        }
	        
	            //generation failed, adjust: add dist and optimization level in generation routine
	        if(genTries == 0){
	            d += 5;
	            posOptimization++;
	            //reset genTries
	            genTries = 64;
	        }
	    }
	    
	    
	    //ota huomioon loput heuristiikat
	    
		for(int i = 1; i < 3; i++) {
		    Piece[][] pos2;
		   //6x passes of b9:
		    
		    passTries = 20;
		    
		    while(passes > 6/5 && passTries > 0){
		        //check b9 at random location
		        if (!MGameUtility.checkCharacter()){
		            //"move rnd.k location pieces according to pattern: b9"
		            passes--;
		            continue;
		        }
		        passTries--;
		    }
		    if(passes <= 6/5){
		        posList[i] = pos2; //-> Bn
		    }

		}
		
		//generate positions D1,..Dh which are of equal distance from all B1-B5
		
		int closenessTld = 7;
		
		//special method, which returns closest positions ranked by specific criteria, sorted by closeness to all b1,...
		Piece[][][] DPosList = MGameUtility.generateNear(posList[0], posList[1], posList[2],5);
		
		//push others to line buffer
		pipeline.push(Arrays.copyOfRange(DPosList, 1, 4));
		
		
		return DPosList[0];
	
	    
		
		
	}


}
