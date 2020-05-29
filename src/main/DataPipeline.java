package main;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class DataPipeline implements Runnable {
	
	private BlockingQueue<Message> queue;
	private Piece[][] pos;
	private int[] move;
	private String status;
	
	
	/*
	 * Data sending and receiving between Board and Engine
	 * 
	 * Board sets status to 'request' while requesting move and sending the board Piece matrix
	 * 
	 * at DataPipeline: 
	 * 		'request' state: push message into Queue, state := 'sent'
	 * at Engine:
	 * 		'sent' state: Queue is emptied,
	 * 		when move has been computed, put move in Queue, state := 'response'
	 * at DataPipeline:
	 * 		'response' state: take move from Queue, status := 'ready', fire eventhandler in Board
	 * at Board:
	 * 		moveReady listener changed: call receiveMove passing the DataPipeline getMove() move to it
	 * 		receiveMove makes the changes in Board
	 * 
	 * 
	 */
	
	public DataPipeline(BlockingQueue<Message> q, Piece[][] pos, String status) {
		this.queue = q;
		this.pos = pos;
		this.status = status;
	}
	
	
	public void run() {
		System.out.println(Thread.currentThread());
		
		while(!status.equals("exit")) {
			//System.out.println("size "+queue.size());
			//System.out.println("queue "+" "+(queue.peek()).getBoardData());
			
			if(status.equals("request")) {
				System.out.println("pos "+pos);
				Message msg = new Message(pos,null,status);			
				try {
					queue.put(msg);
					status = "sent";
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}

			}
			else if(status.equals("response")) {
				System.out.println("[[[[[[[");
				//now move is generated
				try {
					System.out.println("move = "+queue.peek().getMove());
					move = queue.take().getMove();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				status = "ready";
				Board.moveReady.set(true);
			}
			
			
			
		}
		try {
			queue.put(new Message(null,null,"exit"));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public int[] getMove() {
		return move;
	}

	public void setBoard(Piece[][] pos) {
		this.pos = pos;
	}
	
	public void setStatus(String status) {
		System.out.println(status);
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
}
