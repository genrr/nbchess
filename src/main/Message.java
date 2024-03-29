package main;

import java.util.ArrayList;

public class Message {
	
	private int[][][] pos;
	private int turnNo;
	private String status;
	private int[] move;
	private int[] piecesInfo;
	private int[][][] lines;
	ArrayList<double[]> hs;
	ArrayList<double[][]> es;
	ArrayList<double[][][]> rs;
	ArrayList<double[][]> hvs;
	ArrayList<double[][][]> evs;
	ArrayList<double[][][][]> rvs;
	
	public Message(int[][][] pos,int[] move, int turnNo,String status) {
		this.pos = pos;
		this.move = move;
		this.turnNo = turnNo;
		this.status = status;
	}
	
	public Message(ArrayList<double[]> t, ArrayList<double[][]> t2, ArrayList<double[][][]> t3, ArrayList<double[][]> t4,
			ArrayList<double[][][]> t5, ArrayList<double[][][][]> t6, String status) {
		hs = t;
		es = t2;
		rs = t3;
		hvs = t4;
		evs = t5;
		rvs = t6;
		this.status = status;
	}
	

	
	public Message(String status) {
		this.status = status;
	}

	
	public int[][][] getBoardData() {
		return pos;
	}
	
	public int[] getMove() {
		return move;
	}
	
	public String getStatus() {
		return status;
	}
	
	public int getTurnNumber() {
		return turnNo;
	}
	
	public int[] getPiecesInfo() {
		return piecesInfo;
	}

	public ArrayList<double[]> getHeuristics(){
		return hs;
	}
	
	public ArrayList<double[][]> getEvaluations(){
		return es;
	}
	
	public int[][][] getLines(){
		return lines;
	}
}
