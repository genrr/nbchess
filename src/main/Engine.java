package main;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
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
	ArrayList<double[]> heurStorage; //(h_index,turnNo) heuristic development
	ArrayList<double[][]>  evalStorage; //(h_index,[param1,param2,param3],turnNo) eval parameter development
	ArrayList<double[][][]>  relStorage; //(h_index1,h_index2,z,turnNo) relation development
	//(h_index,turnNo,value) heuristic development compared to value development
	ArrayList<double[][]> heurValStorage; 
	 //(h_index,params x3,turnNo,value) eval parameter development compared to value development
	ArrayList<double[][][]> evalValStorage; 
	//(hindex1,hindex2,z,turnNo,value) relation development compared to value development
	ArrayList<double[][][][]> relValStorage;  
	
	//store Objectives, searched game tree, Lines
	int[][] objStorage;
	Piece[][][] searchTree;
	Piece[][][] lines;
	
	private BlockingQueue<Message> queue;
	private BlockingQueue<Message> queue2;
	private boolean color;
	private Message move;
	private boolean drawOfferedByPlayer;
	
	public Engine(BlockingQueue<Message> q, boolean color) {
		this.queue = q;
		this.color = color;
	}
	

	public void run() {
		
		


		Message element;
		
		do {
			try {
				element = queue.peek();
				
				if(element.getStatus().equals("request")) {
					element = queue.take();				
					data = element.getBoardData();
					turnNumber = element.getTurnNumber();
					info = element.getPiecesInfo();					

					//run these once at the start 
					if(!alreadyStarted) {
						queue2 = new ArrayBlockingQueue<Message>(1);
						Pipeline storage = new Pipeline(queue2,color);
						storage.start();

						alreadyStarted = true;
						move = GameLogic.Generate(data,turnNumber,color, info,queue2,true);
					}	
					else {
						move = GameLogic.Generate(data,turnNumber,color, info,queue2,!alreadyStarted);
					}
					
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
					//shut down storage thread
					queue2.put(new Message(null,"exit"));
					//shut down this thread
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


		System.out.println("exiting..");
		
		
		
		
		
	}


	
}
