package main;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;

import javafx.application.Platform;
import java.util.ArrayList;

/**
 * High-level engine abstraction class, runs in a separate thread.
 */

public class Engine extends Thread {

	Piece[][] data;
	int turnNumber = 0;
	int[] info;
	private static boolean alreadyStarted = false;
	
	//store all value function y:s for the whole game
	ArrayList<double[]> valStorage; 
	/* e.g. get value3 history: iterate through whole arraylist with double[i] i = 2, get double[]
	 * containing value3:s for the whole game 
	 * [][][]
	 * [][][]
	 * [][][]
	 * [][][]
	 * [][][]
	 */
	
	//Store all data and its development during the whole game of undetermined length
	ArrayList<double[]> heurStorage; //(h23,turnNo) heuristic development
	ArrayList<double[][]>  evalStorage; //(h19,params,turnNo) eval parameter development
	ArrayList<double[][][]>  relStorage; //(h1,h16,z,turnNo) relation development
	//(h21,turnNo,value) heuristic development compared to value development
	ArrayList<double[][]> heurValStorage; 
	 //(h5,params,turnNo,value) eval parameter development compared to value development
	ArrayList<double[][][]> evalValStorage; 
	//(h4,h9,z,turnNo,value) relation development compared to value development
	ArrayList<double[][][][]> relValStorage;  
	
	//store Objectives, searched game tree, Lines
	int[][] objStorage;
	Piece[][][] searchTree;
	Piece[][][] lines;
	
	private BlockingQueue<Message> queue;
	private boolean color;
	
	public Engine(BlockingQueue<Message> q, boolean color) {
		this.queue = q;
		this.color = color;
	}
	

	public void run() {
		
		//run these once at the start 
//		if(!alreadyStarted) {			
//			storage = new Pipeline();
//			storage.start();
//
//			GameLogic.InitData();
//			//GameLogic.InitSP();
//			//GameLogic.InitObjectives();
//			//GameLogic.InitCharacters();
//			//GameLogic.InitIdeals();
//			alreadyStarted = true;
//		}
		

		Message element;
		
		do {
			try {
				element = queue.peek();
				
				if(element.getStatus().equals("request")) {
					element = queue.take();
					
					data = element.getBoardData();
					turnNumber = element.getTurnNumber();
					info = element.getPiecesInfo();
					
	
					Message move = new Message(null,GameLogic.Generate(data,turnNumber,color, info),
							turnNumber,new int[] {-1,-1},"ready");
					
					queue.put(move);
					Platform.runLater(new Runnable() {
						public void run() {
							Board.moveReady.set(true);
						}
					});
					
				}
				else if(element.getStatus().equals("lines")) {
					lines = element.getLines();
				}
				else if(element.getStatus().equals("exit")) {
					break;
				}
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			catch(NullPointerException n) {
				continue;
			}

				
		} while (true);



		
		
		
		
		
	}


	
}
