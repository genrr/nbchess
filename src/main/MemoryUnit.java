package main;

import java.util.ArrayList;

/*
 * Storage for all information to be stored between games
 * 
 * Strategy efficiency metrics, statistics
 * 
 * 
 */

public class MemoryUnit {

	public static ArrayList<int[]> isstack = new ArrayList<int[]>();
	public static ArrayList<int[]> trigStack = new ArrayList<int[]>();
	public static ArrayList<double[]> heurHistory = new ArrayList<double[]>();
	public static ArrayList<double[]> valHistory = new ArrayList<double[]>();
	public static ArrayList<double[][]> BR1History = new ArrayList<double[][]>();
	public static ArrayList<double[][]> BR2History= new ArrayList<double[][]>();
	public static ArrayList<double[][]> BR3History= new ArrayList<double[][]>();
	public static ArrayList<int[][][]> boardHistory = new ArrayList<int[][][]>();
	
}
