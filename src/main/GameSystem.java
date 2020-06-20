package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class GameSystem {

	private static ComplexSystem c;
	




	public static Piece[][][] generateLines(Piece[][] board,boolean white, double[] evaluation, ComplexSystem csys, Objectives o, BlockingQueue<Message> queue) {
	
		int maxHeuristics = 5;
		c = csys;
		ArrayList<Integer> actionList = new ArrayList<Integer>();
		ArrayList<int[]> coeffList = new ArrayList<int[]>();
		Piece[][][] posStack = new Piece[GameLogic.LINES_AMOUNT][][];
		
		int[] eValues = new int[26];
		int[] indexList = new int[maxHeuristics];

		
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
		

		int h = 0;
		
		//[1,4,3,1,5,2,2,5]
		//max = 5, indexList = [4,7,1,2,5]
		
		for (int max = 5; max > 0; max--) {
			for (int i = 0; i < 26; i++) {
				if(eValues[i] == max) {
					if(h < indexList.length) {
						indexList[h] = i;
						h++;				}
					}
			}

			
		}
		
		for(int j = 0; j<GameLogic.LINES_AMOUNT; j++) {
			posStack[j] = fillPosition(board,white,evaluation,indexList,o,queue);
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
	
	
	private static Piece[][] fillPosition(Piece[][] pos0, boolean white, double[] eval, int[] evalList, Objectives o, BlockingQueue<Message> queue) {
		Piece[][] pos = null;
		Piece[][] pos2 = null;
		Piece[][][] posList = new Piece[3][][];
		ArrayList<Piece> l1 = MGameUtility.ReturnAllPieces(pos0, white);
		ArrayList<Piece> l2 = MGameUtility.ReturnAllPieces(pos0, !white);
		
		int heur = evalList[0];
		int dir = (int) Math.signum(eval[0]);
		
	    double[] qualityCoeff = new double[] {16.0,16.0,16.0,16.0};
	    double percentOfPieces = 0.2;
	    int genTries = 64;
	    int posOptimization = 1;
	    int maxTries = 64;
	    int passes;
	    int passTries;
	    int k0 = 4;
	    double temp;
		int closenessTld = 7;		
	    int d = GameLogic.DIST_THRESHOLD;
	    
	    while(genTries > 0){
	    
	        
	        //generate until pos passes valuecontrol
	        do{
	            pos = MGameUtility.generatePos(pos0,l1,l2,k0,eval,o,posOptimization);
	            qualityCoeff[0] -= 0.1;
	            qualityCoeff[1] -= 0.1;
	            qualityCoeff[2] -= 0.1;
	            qualityCoeff[3] -= 0.1;
	            
	            genTries--;
	            
	        }
	        while(!ValueFunction.normalize(pos,white,qualityCoeff));
	        
	        for(int i = 0; i<maxTries; i++) {
		        if(dir == -1) {
			        if(GameLogic.MeasureHeuristic(pos, white, heur) > GameLogic.MeasureHeuristic(pos0, white, heur)) {
			        	break;
			        }
		        }
		        else if(dir == 1) {
		        	 if(GameLogic.MeasureHeuristic(pos, white, heur) < GameLogic.MeasureHeuristic(pos0, white, heur)) {
				        break;
				     }
		        }
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
	    
		for(int i = 1; i < evalList.length; i++) {
		   //6x passes of b9:
		    
		    passTries = 20;
		    
		    while(passTries > 0){
		    	temp = Math.signum(eval[i]);
		        //check b9 at random location
		    	pos2 = pos;
		        while ((temp == 1 && GameLogic.MeasureHeuristic(pos0, white, evalList[i]) < GameLogic.MeasureHeuristic(pos2, white, evalList[i]) ||
		        		temp == -1 && GameLogic.MeasureHeuristic(pos0, white, evalList[i]) > GameLogic.MeasureHeuristic(pos2, white, evalList[i])) &&
		        		passTries > 0){
		        	pos2 = MGameUtility.nearRandomSearch(pos2,1,3);
					passTries--;
		        }
		        
		    }

		    posList[i] = pos2; //-> Bn


		}
		
		//generate positions D1,..Dh which are of equal distance from all B1-B5

		//special method, which returns closest positions ranked by specific criteria, sorted by closeness to all b1,...
		Piece[][][] DPosList = MGameUtility.generateOptimizedSolutions(posList,closenessTld);
		
		//push others to line buffer
		try {
			queue.put(new Message(Arrays.copyOfRange(DPosList, 1, 4),"lines"));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return DPosList[0];
	
	    
		
		
	}


}
