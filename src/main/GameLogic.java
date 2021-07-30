package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;

public class GameLogic {
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
	private static int white;
	private static ArrayList<ArrayList<int[][][]>> lineStack = new ArrayList<ArrayList<int[][][]>>(NO_LINES);
	private static ArrayList<ArrayList<double[]>> moveStack = new ArrayList<ArrayList<double[]>>(NO_LINES);
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

		localE = new ArrayList<double[][]>();
		localE.add(new double[3][26]);

		for(int i = 0; i<26; i++) {
			for(int j = 0; j<3; j++) {
				localE.get(0)[j][i] = 6*r.nextDouble()-3;
			}
		}

		
		localH = new ArrayList<double[]>();
		localV = new ArrayList<double[]>();
		localR = new ArrayList<double[][][]>();
		localHV = new ArrayList<double[][]>();
		localEV = new ArrayList<double[][][]>();
		localRV = new ArrayList<double[][][][]>();
	}
	
	
	
	
	/*
	 * Generates move, returns it as a vector: {startX,startY,targetX,targetY}
	 * 
	 * TODO: eval changing as part of the algorithm
	 * COTable implementation, Heuristic implementations
	 * 
	 */
	
	public static Message Generate(int[][][] board, int turnNumber, int w, BlockingQueue<Message> storage, StochasticSystem c) {
		System.out.println("starting generation");
		int[] priorityList;
		white = w;
		moveStack.clear();
		MCS = new HashMap<int[],double[]>();
		int[][] d = new int[NO_GP][];
		double[] evaluatedValues = new double[26];
		
		Message element = null;
		try {
			if(!init) {
				if(storage.peek()!= null)
					System.out.println("status-"+storage.peek().getStatus());
				storage.put(new Message(null,"request P -> E"));
				System.out.println("taking");
				while(storage.peek() != null) {
					System.out.println("waiting for pipeline to take..");
					try {
						if(storage.peek().getStatus().contentEquals("send P -> E")) {
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.println(storage.size());
				if(storage.peek()!= null)
					System.out.println("status-"+storage.peek().getStatus());
				element = storage.take();
				System.out.println(storage.size());
				if(storage.peek()!= null)
					System.out.println("status-"+storage.peek().getStatus());
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
				init = false;
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		
		/*
		 * START Move Generation
		 * 
		 * generate 64 pos by pattern in SP.get() and search
		 * until avg of evaluatedValues of pos[i] < posEvalTld, computeBoard(),computeMetrics() on them -> save states
		 * 
		 * start gameState closeness pruned search ->
		 * 
		 */
		
		//reset everything
		totalPlies = 0;
		depth = 0;
		localDepth = 0;
		posValue = 0;
		localH.clear();
		localV.clear();

		localH.add(MeasureAllHeuristics(board, w));
		localV.add(ValueFunction.computeValue(board, w, true));
		for(int j = 0; j<26; j++) {
			evaluatedValues[j] = evaluate(j,turnNumber,localE.get(0)) - localH.get(0)[j];
		}
		
		priorityList = GameSystem.prioritize(evaluatedValues, hMax); //[h1,h2,h3,..]		
		correlationTable = simulate(0,board,evaluatedValues);		

		double temp1,temp2;
		int h2 = 0;
		int y = 0;
		
		

		for(int i = 0; i<MAIN_ITERATIONS; i++) {
			moveStack.add(new ArrayList<double[]>()); // add a line of moves
			search(board,w,w,moveStack.get(moveStack.size()-1));
			posList.clear(); //clear the poslist in anticipation of a new round, new line
			posList.add(Resources.calculateHash(board)); //add starting position
		}

		
		try {
			if(storage.peek()!= null)
				System.out.println("status-"+storage.peek().getStatus());
			storage.put(new Message(localH,localE,localR,localHV,localEV,localRV,"send E -> P"));
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}

		
		String decision = "ready";
		
		if(resign) {System.out.println("#######");
			decision = "resign";
		}
		else if(drawOffered) {
			decision = "draw requested";
		}
		else if(drawOfferedByPlayer && drawOffered) {
			decision = "draw accepted";
		}
		
		double posv,posv2,totalv,totalv2;
		int max = 0;
		
		for (int i = 0; i < moveStack.size(); i++) {
			for (int j = 0; j < moveStack.size(); j++) {
				posv = moveStack.get(i).get(0)[1];
				totalv =  moveStack.get(i).get(0)[2];
				posv2 = moveStack.get(j).get(0)[1];
				totalv2 =  moveStack.get(j).get(0)[2];
				
				if(i != j && posv*100 + totalv*50 > posv2*100 + totalv2*50) {
					max = i;
				}
			}
		}
		
		System.out.println("selected line: ");
		
		for(int i = 0; i<moveStack.get(max).size(); i++) {
			System.out.println(Arrays.toString(moveStack.get(max).get(i)));
		}
		
		int[] move = new int[] {
				(int)moveStack.get(max).get(1)[0],
				(int)moveStack.get(max).get(1)[1],
				(int)moveStack.get(max).get(1)[2],
				(int)moveStack.get(max).get(1)[3],
				(int)moveStack.get(max).get(1)[4]};
		
		
		return new Message(null, move,
				turnNumber,decision);
		

	}


	private static void search(int[][][] pos, int white, int current, ArrayList<double[]> line){
	    ArrayList<int[]> moves = MGameUtility.getAllMoves(pos, white);
	    
	    int[][][] pos1 = MGameUtility.cloneArray(pos);
	    int[][][] pos2 = MGameUtility.cloneArray(pos);
	    double[] y1,y2;
	    double plyEval = 1.0;
	    
	    for(int i = 0; i<moves.size(); i++){
	    	System.out.println("totalPlies "+totalPlies+" depth: "+depth);
	        totalPlies++; //increment total positions searched
	        
	        makeMove(moves.get(i),pos2);

	        y1 = MeasureAllHeuristics(pos1, white);
			y2 = MeasureAllHeuristics(pos2, white);

			for(int h=0;h<26;h++) {
				for(int v=0;v<5;v++) {
					plyEval += Math.abs(correlationTable[h][v] + (y2[h] - y1[h])); 
				}
			}
			

	        if(current == white)
	            posValue += compute(pos1,pos2,white);
	        else
	            posValue -= compute(pos1,pos2,white);
	            
	        if(depth <= maxDepth && totalPlies < maxPlies){
        		line.add(new double[] {moves.get(i)[0],moves.get(i)[1],moves.get(i)[2],moves.get(i)[3],
        				moves.get(i)[4], posValue, plyEval});
	
	            ply++; //increment vertical plies counter, used in calculating the depth
	            if(ply % 2 == 0)
	                depth++; //increment depth
	            
	            if(posValue > 3 && plyEval > 1) {
	            	System.out.println("recursion is fun");
	            	search(pos2,(white+1)%2, current,line);
	            }
	        }
	        if(totalPlies >= maxPlies) {
	        	line.add(0,new double[] {-1,posValue,posValue/depth}); //add line result
	        	return;
	        }
 
	    }
	    

	    if(current == white)
	        posValue -= compute(pos1,pos2,white);
	    else
	        posValue += compute(pos1,pos2,white);
	    ply--;
	    depth--;
	    posList.remove(posList.size()-1);

	}
	
	
	//TODO: add positionalcrunch, add objective effects
	
	public static double compute(int[][][] previous, int[][][] pos, int white) {
		
		double[] posValues = ValueFunction.computeValue(pos, white, true);
		double[] h = MeasureAllHeuristics(pos, white);
		double[] prevH = MeasureAllHeuristics(previous, white);
		double a = 0;
		double b = 0;
		double c = 0;
		double d = 0;
		double kingSafety = 24*posValues[0];
		
		for(int i = 0; i<5; i++){
			double temp = Math.abs(1-posValues[i]);
			if(temp <= 3) {
				a *= temp;
			}
			else {
				a += temp;
			}
		}
		
		a /= 5;
		
		for(int i = 0; i<repList.size(); i++) {
			b += MSystem.boardDistance(MSystem.computeBoard(pos, white), repList.get(i));
		}
		
		b /= repList.size();
		
		for(int j = 0; j<26; j++) {
			c += evaluate(j,localE.get(0)) - h[j];
		}
		
		for(int j = 0; j<26; j++) {
			d += evaluate(j,localE.get(0)) - prevH[j];
		}
		
		
		
		return 1.0 / (kingSafety*a*b*c) + (c-d) + 256*MeasureHeuristic(pos, white, 14) + 460 / MeasureHeuristic(pos,white,12) + ValueFunction.crunch(posValues, new double[] {16,16,16,16,16});
	}
	
	
	public static double[][] simulate(int mode, int[][][] pos0, double[] evaluatedValues) {
		
		double[][] result = new double[26][5];
		double[] temp1,temp2,temp3,temp4,temp5,temp6;
		int size;
		int index = 0;
		
		//FULL simulation
		if(mode == 0) {
			explore(pos0,white);
			//System.out.println(result[0][200]);
			size = localH.size();
			System.out.println("size = "+size);

			for(int h = 0; h<26; h++) {
				temp1 = new double[size]; //array for heur h
				temp2 = new double[size]; //array for Values
				
				for(int i = 0; i < size; i++) {
					//copy h from explore(localH) to temp1
					temp1[i] = localH.get(i)[h];
				}

				for(int v = 0; v<5; v++) {
					//fill the Values
					for(int i = 0; i < size; i++) {
						temp2[i] = localV.get(i)[v];
					}					
					result[h][v] = MGameUtility.correlation(temp1,temp2,1); //h correlation

				}

			}
		
		}


		
		return result;
		
		
	}
	
	public static void explore(int[][][] pos, int turn) {
		double[] h;
		double[] v;
		double[] y1;
		double[] y2;
		double[] hChange = new double[26];
		int[][][] pos1 = MGameUtility.cloneArray(pos);
		int[][][] pos2 = MGameUtility.cloneArray(pos);
		double[][] currentEval = new double[3][26];
		double[][] newEval = new double[3][26];
		ArrayList<int[]> moves = MGameUtility.getAllMoves(pos2, turn);
		double[] d = new double[26];
		double temp = 0;
		double minSum = 0;
		int minimalDiffIndex = 0;
		int val = 1;
		
		for (int x = 0; x<moves.size(); x++) {
//			System.out.println("move "+ Board.pieceSymbol(pos2[is[0]][is[1]][0],is[1],is[2],is[3],is[4]==11) +" from ("+is[0]+ " "+
//					is[1]+ ") to ("+
//					is[2]+ " "+
//					is[3]+ "), result: "+
//					is[4]);
			
			makeMove(moves.get(x),pos2);			
			y1 = MeasureAllHeuristics(pos1, turn);
			y2 = MeasureAllHeuristics(pos2, turn);
			for(int j = 0; j<26; j++) {
				hChange[j] = Math.signum(y2[j] - y1[j]);
				d[j] = evaluate(j, localE.get(0)) - y2[j];
				temp += d[j];
			}
			
			temp /= 26;
			
			if(temp < minSum) {
				minSum = temp;
				minimalDiffIndex = x;
			}
			
			if(1.0 / (ValueFunction.computeValue(pos2, turn, true)[0] - 1) > 10){
				val += 100;
			}
			if(1.0 / (ValueFunction.computeValue(pos2, turn, true)[1] - 1) > 10){
				val += 67;
			}
			if(1.0 / (ValueFunction.computeValue(pos2, turn, true)[2] - 1) > 10){
				val += 33;
			}
			if(1.0 / (ValueFunction.computeValue(pos2, turn, true)[3] - 1) > 10){
				val += 33;
			}
			if(1.0 / (ValueFunction.computeValue(pos2, turn, true)[4] - 1) > 10){
				val += 60;
			}
			
			if(val > 70) {
				repList.add(MSystem.computeBoard(pos2, turn));
			}
		
			
			repChanges.add(MSystem.boardSubtraction(MSystem.computeBoard(pos1,turn), MSystem.computeBoard(pos2, turn)));
			diffTable.add(hChange);	
			h = MeasureAllHeuristics(pos2, turn);
			v = ValueFunction.computeValue(pos2, turn, true);
			v = ValueFunction.direction(v,true);
			localH.add(h);
			localV.add(v);
			currentEval = localE.get(localE.size()-1);
			
			if(ValueFunction.crunch(v, new double[] {16,16,16,16,16}) < 20) {
					for(int i = 0; i<26; i++) {
						for(int j = 0; j<3; j++) {
							do{
								newEval[j][i] = 6*r.nextDouble()-3;
							}
							while(Math.abs(newEval[j][i]-currentEval[j][i]) <= 4);
						}
					}

				localE.add(newEval);
			}
			else {
				for(int i = 0; i<26; i++) {
					for(int j = 0; j<3; j++) {
						do{
							newEval[j][i] = 6*r.nextDouble()-3;
						}
						while(Math.abs(newEval[j][i]-currentEval[j][i]) > 2);
					}
				}
				localE.add(newEval);
			}
			

			posList.remove(posList.size()-1);
			pos2 = pos1;
		}
		
		
		//test for maximum depth, decrement current depth
		if(depth+1 > maxDepth) { 
			localDepth--;
			//System.out.println("returning up one level, depth: "+depth);
			return;
		}
		//only update depth variable after recursion has fully returned to ensure that
		//recursion returns fully first
		else if(localDepth == 0) {
			depth = 0; 
		}
		//go deeper, increment both depth variables
		else {
			localDepth++;
			depth++;
			//System.out.println("going down one level, depth: "+depth);
			explore(makeMove(moves.get(minimalDiffIndex), pos2),(turn + 1) % 2);
		}

		
		
		
	}
	
	
	private static void gpSearch(int[][][] pos0, int[][] targetGpList) {
		ArrayList<int[]> moves = MGameUtility.getAllMoves(pos0, white);		
		boolean match = false;
		int xi = 3;		
		int[][][] pos1 = MGameUtility.cloneArray(pos0);
		int k;
		double diffs = 0;
		double bestDiffs = 0;
		int bestDiffIndex = 0;
		ArrayList<Integer> matchIndexList = new ArrayList<Integer>();
		int[][][] pos2;
		
		ArrayList<double[]> posGpList = returnGPs(pos1);
		
		for(int m = 0; m<moves.size(); m++) {
			for(int i = 0; i<posGpList.size(); i++) {
				for(int j = 0; j<NO_GP; j++) {
					k = 0;
					while((posGpList.get(i)[k] == targetGpList[j][k] || 
						 ((k == 2 || k == 4 ) &&
						   Math.abs(posGpList.get(i)[k]-targetGpList[j][k]) < xi))) {
						k++;
						if(k == 5) {
							match = true;
							matchIndexList.add(m);
						}
					}
					
					
					pos2 =  MGameUtility.cloneArray(pos1);
					makeMove(moves.get(m), pos2);
					double[] t = MeasureAllHeuristics(pos2, white);
					diffs = 0;
	
					for(int n = 0; n<26; n++) {
						diffs += 1 / evaluate(j,localE.get(0)) - localH.get(0)[j];
					}
					if(diffs > bestDiffs) {
						bestDiffs = diffs;
						m = bestDiffIndex;
					}
	
					match = false;
					if(counter > tempLimit) {
						MCS.put(targetGpList[j],ValueFunction.computeValue(pos1, white, true));
						return;
					}
				}
			}
		}
		
		if(match) {
			for(int i = 0; i<matchIndexList.size();i++) {
				if(depthMax < depthCounter) {
					depthCounter++;
					counter++;
					white = (white+1)%2;
					gpSearch(makeMove(moves.get(matchIndexList.get(i)), pos1),targetGpList);
					
				}
			}
		}
		else {
			if(depthMax < depthCounter) {
				depthCounter++;
				counter++;
				white = (white+1)%2;
				gpSearch(makeMove(moves.get(bestDiffIndex), pos1),targetGpList);
				
			}
			depthCounter = 0;
		}
	}
	
	//Returns all GamePlans at pos0
	
	private static ArrayList<double[]> returnGPs(int[][][] pos0){
		
		ArrayList<double[]> gpList = new ArrayList<>();
		ArrayList<int[]> moves = MGameUtility.getAllMoves(pos0, white);
		
		double temp;
		
		for(int i = 0; i<moves.size(); i++) {
			//add random move
			gpList.add(new double[] {0,0,0,0,moves.get(i)[2],moves.get(i)[3]});
			
			int[] p = pos0[moves.get(i)[0]][moves.get(i)[1]];
			int[] q = pos0[moves.get(i)[2]][moves.get(i)[3]];
			
			for(int k = 0; k<11; k++) {
				
				for(int j = 1; j<17; j++) {	
					
					if(k == 0) {
						//random move selection j with selectionresult temp
						temp = computeGPatJ(pos0,p,j);
						gpList.add(new double[] {k,j,temp,0,moves.get(i)[0],moves.get(i)[1],moves.get(i)[2],moves.get(i)[3]});
					}
					else if(k == 1) {
						if(q != null && q[2] == p[2]) {
							temp = computeGPatJ(pos0,q,j);
							gpList.add(new double[] {k,j,temp,1,moves.get(i)[0],moves.get(i)[1],moves.get(i)[2],moves.get(i)[3]});
						}
					}
					else if(k == 2) {
						
					}
					
				}
			}
			
		}
		
		
		return gpList;
	}
	
	//GamePlan piece-wise implementation for the search
	
	private static double computeGPatJ(int[][][] board, int[] piece, int index) {
		boolean safe;
		int temp = 0;
		int temp1 = 0;
		int temp2 = 0;
		int temp3 = 0;
		int attRelSum = 0;
		int defRelSum = 0;
		int threatRelSum = 0;
		int attAbsSum = 0;
		int defAbsSum = 0;
		int threatAbsSum = 0;
		int color = piece[2];
		ArrayList<int[]> ownPieces = MGameUtility.ReturnAllPieces(board, color);
		ArrayList<int[]> enemyPieces = MGameUtility.ReturnAllPieces(board, (color+1)%2);
		ArrayList<int[]> moves = MGameUtility.getAllMoves(board, color);
		
		for(int i = 0; i<moves.size(); i++) {
			//this moves (startX,startY) equals pieces (X,Y) -> pieces move!
			if(moves.get(i)[0] == piece[3] && moves.get(i)[1] == piece[4]) {
				temp1++;
				//target square is empty
				if(board[moves.get(i)[2]][moves.get(i)[3]] == null) {
					temp2++;
				}
				else {
					//is enemy piece?
					if(board[moves.get(i)[4]][moves.get(i)[5]][2] != piece[2]) {
						attRelSum += MSystem.RelPV(board, board[moves.get(i)[4]][moves.get(i)[5]]);
						attAbsSum += MGameUtility.unitValue(board[moves.get(i)[4]][moves.get(i)[5]]);
					}
					//is own piece?
					else {
						defRelSum += MSystem.RelPV(board, board[moves.get(i)[4]][moves.get(i)[5]]);
						defAbsSum += MGameUtility.unitValue(board[moves.get(i)[4]][moves.get(i)[5]]);
					}
				}
				safe = true;
				for(int k = 0; k<enemyPieces.size(); k++) {
					if(MGameUtility.distance(board, enemyPieces.get(i), enemyPieces.get(i)[3], enemyPieces.get(i)[4], moves.get(i)[2], moves.get(i)[3], false) == 1) {
						safe = false;
					}
				}
				if(safe) {
					temp3++;
				}
			}
		}
		
		for(int i = 0; i<enemyPieces.size(); i++) {
			//piece is attacked by piece q in enemyPieces
			if(MGameUtility.distance(board, enemyPieces.get(i), enemyPieces.get(i)[3],
					 enemyPieces.get(i)[4], piece[3], piece[4], false) == 1) {
				threatRelSum += MSystem.RelPV(board, enemyPieces.get(i));
				threatAbsSum += MGameUtility.unitValue(enemyPieces.get(i));
			}
		}
		
		switch(index) {
		//defenderCount
		case 1:
			for(int i = 0; i<ownPieces.size(); i++) {
				if(MGameUtility.distance(board, ownPieces.get(i), ownPieces.get(i)[3], ownPieces.get(i)[4], piece[3], piece[4], false) == -1) {
					temp++;
				}
			}
			return temp;
		//attackCount
		case 2:
			for(int i = 0; i<enemyPieces.size(); i++) {
				if(MGameUtility.distance(board, piece, piece[3], piece[4], enemyPieces.get(i)[3], enemyPieces.get(i)[4], false) == 1) {
					temp++;
				}
			}
			return temp;
		//threatCount
		case 3:
			for(int i = 0; i<enemyPieces.size(); i++) {
				if(MGameUtility.distance(board, enemyPieces.get(i), enemyPieces.get(i)[3], enemyPieces.get(i)[4], piece[3], piece[4], false) == 1) {
					temp++;
				}
			}
			return temp;
		//legal square count for piece
		case 4:

			return temp1;
		//free square count for piece
		case 5:
			return temp2;
		//safe square count for piece		
		case 6:
			return temp3;
		//relative piece value	
		case 7:
			return MSystem.RelPV(board, piece);
		//absolute piece value
		case 8:
			return MGameUtility.unitValue(piece);
		//piece type
		case 9:
			int i = 0;
			if(piece[0] == 11 || piece[0] == 18) {
				i = 1;
			}
			else if(piece[0] == 12 || piece[0] == 19) {
				i = 2;
			}
			else if(piece[0] == 13 || piece[0] == 14 || piece[0] == 20 || piece[0] == 21) {
				i = 3;
			}
			else if(piece[0] == 15 || piece[0] == 22) {
				i = 4;
			}
			else if(piece[0] == 16 || piece[0] == 23) {
				i = 5;
			}
			return i;
		//sum of relative piece values of the pieces attacked by Piece 
		case 10:
			return attRelSum;
		//sum of relative piece values of the pieces defended by Piece
		case 11:
			return defRelSum;
		//sum of relative piece values of those enemy pieces attacking Piece
		case 12:
			return threatRelSum;
		//sum of absolute piece values of pieces attacked by Piece 
		case 13:
			return attAbsSum;
		//sum of absolute piece values of pieces defended by Piece
		case 14:
			return defAbsSum;
		//sum of absolute piece values of those enemy pieces, that attack Piece
		case 15:
			return threatAbsSum;
			
		default:
			return 0;
		}
		
	}


	private static ArrayList<int[]> searchf(int[][][] pos, int t){

		int depth = 1;
		ArrayList<int[]> line = new ArrayList<>(depth);
		ArrayList<int[]> moves;
		int[][][] posTemp = MGameUtility.cloneArray(pos);
		int[][][] posTemp2;
		Random r = new Random();
		double[] d;
		int[] f;
		double sum = 0;
		double rating = 0;
		double maxRating = 0;
		double maxValue = 0;
		double[] evaluatedValues = new double[26];
		int selected = 0;
		ArrayList<Double> ratingList = new ArrayList<Double>();
		ArrayList<double[]> ratedList = new ArrayList<double[]>();
		ArrayList<Double> valueList = new ArrayList<Double>();
		int turn = white;
		double[] evalC = new double[] {5.00, 1.5,  3.5, 3.20, 4.5, 3.0, 1.15, 5.0,  1.0,  0.1,
									   0.35, 0.0, 25.0, 0.75, 6.0, 3.2, 4.50, 3.5, 32.0, 20.0,
									   11.0, 5.0,  5.0,  3.0, 3.0, 1.0};
		
		for(int i = 0; i<26; i++) {
			evalC[i] += 5*r.nextDouble()-r.nextInt(3);
		}		

		
		for(int j = 0; j<depth; j++) {
			moves = MGameUtility.getAllMoves(posTemp, turn);
			for(int k = 0; k<moves.size(); k++) {
				posTemp2 = makeMove(moves.get(k),posTemp);
				posTemp2[8][0][0] = moves.get(k)[6];
				posTemp2[8][1][0] = moves.get(k)[7];
				d = MeasureAllHeuristics(posTemp2, white);
				for(int i = 0; i<26; i++) {
					evaluatedValues[i] = evalC[i] - d[i];
				}				
				f = GameSystem.prioritize(evaluatedValues, 5);					
				double[] de = ValueFunction.computeValue(posTemp2, white,true);
				double[] dn = ValueFunction.normalize(posTemp2, white, de, new double[] {16,16,16,16,16});
				double dv = ValueFunction.crunch(dn,new double[] {20,9,9,5,11});
				valueList.add(dv);

				sum = 0;

			}
	
			int sum2 = 0;
			while(ratingList.size() != 0) {
				selected = 0;
				maxRating = ratingList.get(0);
				maxValue = valueList.get(0);
				for (int i = 0; i < ratingList.size(); i++) {

					if(ratingList.get(i) < maxRating && valueList.get(i) > badValueTld ) {
						maxRating = ratingList.get(i);
						selected = i;
					}
					if(valueList.get(i) > maxValue) {
						maxValue = valueList.get(i);
					}
					
					sum2 += ratingList.get(i);

				}
				ratingList.remove(selected);
				ratedList.add(new double[] {rating,selected});
			}

			//best move heurAvg * all moves' heurAvg -avg
			double eval2 = ratedList.get(0)[0] * (sum2 / ratedList.size());			
			int limit = ratedList.size();

			
			for(int i = 0; i<ratedList.size();i++) {
				if(ratedList.get(i)[0] <= badValueTld) {
					if(i == 0 && eval2 > 21) {
						resign = true;
					}
					limit = i;
				}
			}
			

			int n = r.nextInt(limit);
			//make the one of the best moves in posTemp
			posTemp = makeMove(moves.get((int) ratedList.get(n)[1]),posTemp);
			posTemp[8][0][0] = moves.get(selected)[6];
			posTemp[8][1][0] = moves.get(selected)[7];
			line.add(moves.get((int) ratedList.get(n)[1]));
			turn = (turn+1)%2;
			ratedList.clear();
			
		}

		return line;
		
	}
	

	
	
	public static void searchTree(int i, int[][][] pos, int turn, double[][] targetRep) {
		int[][][] pos1 = MGameUtility.cloneArray(pos);
		int[][][] pos2 = MGameUtility.cloneArray(pos);
		
		ArrayList<int[]> moves = MGameUtility.getAllMoves(pos2, turn);
		double temp;
		
		if(tries > 1600) {
			return;
		}
		
		for (int[] is : moves) {
			dist = 0;
			tries++;
			
			pos2 = makeMove(is,pos1);
			temp = MSystem.boardDistance(MSystem.computeBoard(pos2,turn), targetRep);
			
			dist += temp;
			
			moveStack.get(i).add(is);

			if(depth > 5 || dist/depth > 1 || ValueFunction.crunch(ValueFunction.normalize(pos2, turn, ValueFunction.computeValue(pos2, turn, true), new double[] {16,16,16,16,16}),new double[] {20,9,9,5,11}) < 1) {
				moveStack.get(i).remove(moveStack.size()-1);
				depth--;
				dist -= temp;
				return;
			}
			else if(dist/depth < 0.25) {
				return;
			}
			else {
				depth++;
				searchTree(i, pos2,(turn+1)%2, targetRep);
			}
			
		}
		
	
		
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
