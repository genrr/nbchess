package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Engine extends Application {
	private static Piece[][] board = new Piece[8][8];
	public Piece[] pieces = new Piece[32];
	public int moveCounter = 0;
	private int turn = 0;
	private static boolean gameRunning = false;

	private static SimpleBooleanProperty counter  = new SimpleBooleanProperty(false);
	
	TextArea console = new TextArea();
	private static int startX;
	private static int startY;
	private static GridPane grid = new GridPane();
	private static boolean awaitingMove = false;
	private static StringBuilder sb = new StringBuilder();
	private static Color gridColor1;
	private static Color gridColor2;
	private static int colorSwitch = -1;
	
	private static boolean PlayerColor;
	private static boolean BotColor;
	
	@Override
	public void start(Stage window) throws Exception {
		initBoard(grid);
		colorChanger(grid);
		BorderPane bp = new BorderPane();
		Scene scene = new Scene(bp,1080,920);
		window.setScene(scene);
		window.show();
		
		VBox sidebar = new VBox();	
		//sidebar.setOrientation(Orientation.VERTICAL);

		Button startButton = new Button("Start new game");
		Button loadButton = new Button("Load a game");
		Button resignButton = new Button("Resign");
		Button colorButton = new Button("Change color scheme");
		Button exitButton = new Button("Exit");
		HBox initButtonContainer = new HBox();
		initButtonContainer.getChildren().addAll(startButton,loadButton);
		
		HBox functionSettingsContainer = new HBox();
		HBox.setMargin(colorButton,new Insets(25.0,10.0,50.0,10.0));
		
		
		Pane consolePane = new Pane();
		consolePane.setPadding(new Insets(30));
		console.setPrefSize(180, 400);
		consolePane.getChildren().add(console);
		Label turnLabel = new Label("");
		turnLabel.setPrefSize(250,50);
		turnLabel.setAlignment(Pos.CENTER);

		counter.addListener((observableValue,oldValue,newValue) -> {
			if(newValue) {
				turnLabel.setText("Turn: "+String.valueOf(turn)+" White");
			}
			else {
				turnLabel.setText("Turn: "+String.valueOf(turn)+" Black");
			}
		});
		

		
		
		/*
		bUnitsLost.addListener((observableValue,oldValue,newValue) -> {
			wgameInfo.setText(newValue);
		});
		
		
		wUnitsLost.addListener((observableValue,oldValue,newValue) -> {
			bgameInfo.setText(newValue);
		});
		*/
		
		
		//add. controls for the ndf algorithm
		Button setparam1 = new Button("shape");
		Button setparam2 = new Button("f(x)");
		functionSettingsContainer.getChildren().addAll(setparam1,setparam2);	
		exitButton.setAlignment(Pos.CENTER_RIGHT);
		sidebar.getChildren().addAll(exitButton,consolePane,initButtonContainer,turnLabel,functionSettingsContainer,resignButton,colorButton);
		startButton.setOnAction(e -> StartNewGame(initButtonContainer));
		loadButton.setOnAction(e -> LoadGame());
		exitButton.setOnAction(e -> Platform.exit());
		colorButton.setOnAction(e -> colorChanger(grid));
		resignButton.setOnAction(e -> EndGame());
		bp.setRight(sidebar);
		bp.setLeft(redraw(grid));
		
		
		
	}
	
	

	//colors
	//0.28,0.0,0.18,1
	//
	
	
	private void StartNewGame(HBox cont) {
		Button selectionW = new Button("White");
		Button selectionB = new Button("Black");
		Button selectionR = new Button("Random");
		
		Random generator = new Random();
		
		selectionW.setOnAction(e -> {	PlayerColor = true;
										BotColor = false;
										counter.setValue(true);
										cont.setVisible(false);
									});
		selectionB.setOnAction(e -> {	PlayerColor = false; 
										BotColor = true; 
										counter.setValue(false); 
										cont.setVisible(false);
									});
		selectionR.setOnAction(e -> {	PlayerColor = generator.nextBoolean(); 
										BotColor = !PlayerColor; 
										counter.setValue(true); 
										cont.setVisible(false);
									});
		
		cont.getChildren().clear();
		cont.getChildren().addAll(selectionW,selectionB,selectionR);
		
		turn = 1;
		gameRunning = true;
	}
	
	
	private void LoadGame() {
		
	}
	
	private void EndGame() {
		if(gameRunning) {
			if(BotColor) {
				sb.append("White wins by resignation");
			}
			else {
				sb.append("Black wins by resignation");
			}
			gameRunning = false;
		}
	}
	
	private GridPane redraw(GridPane grid) {
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				
				Pane square = new Pane();
				Rectangle canvas = null;
				ImageView piece = new ImageView();
				
				
				if(i%2 == 0) {
					if(j%2 == 1) {
						canvas = new Rectangle(100,100,gridColor1);
					}
					else if (j%2 == 0) {
						canvas = new Rectangle(100,100,gridColor2);
						
					}
				}
				else {
					if(j%2 == 0) {
						canvas = new Rectangle(100,100,gridColor1);
					}
					else if (j%2 == 1) {
						canvas = new Rectangle(100,100,gridColor2);
					}
				}

				int temp = j;
				int temp2 = i;
				
				square.setId(temp+""+temp2);
				square.getChildren().add(canvas);
				square.getChildren().add(piece);
				square.setOnMousePressed(e -> gridFn(square,temp,temp2));
				GridPane.setConstraints(square,i,j);
				grid.getChildren().add(square);
				
				//System.out.println("temp: "+temp+"temp2: "+temp2);
				if(gridState(temp,temp2) != null) {
					piece.setImage(new Image(getClass().getClassLoader().getResource("resources/"+gridState(temp,temp2)+".png").toExternalForm(),100,100,true,true));
				}
				
				
			}
		}
		return grid;
	}
	

	private void setTurn(String piece, int x, int tx, int ty) {
		moveCounter++;
		
		if(moveCounter % 2 == 0) {
			turn++;
			sb.append(turn+". ");
		}
		else {
			sb.append("\n");
		}
		
		sb.append(pieceSymbol(piece,x,tx,ty)+" ");
		
		counter.setValue(!counter.getValue());
	}
	
	private void gridFn(Pane p, int x, int y) {
		if(awaitingMove) {
			gridFn2(p,startX,startY,x,y);
			awaitingMove = false;
		}
		else {
			startX = x; 
			startY = y;
			awaitingMove = true;
		}
	}
	
	
	private void gridFn2(Pane p,int sx, int sy, int tx, int ty) {
		//System.out.println(sx+""+sy);
		//ystem.out.println(tx+""+ty);
		//System.out.println("###########"+board[sx][sy]);

		
		int result = 0;
		
		int[] move = new int[4];
		
		if(counter.get() == PlayerColor) {
			RuleSet.validate(board, counter.getValue(), sx, sy, tx, ty);
	
			if(result == 0 || result == 5) {
				//System.out.println("moving "+board[sx][sy]+" from "+sx+","+sy+" to "+tx+","+ty);
				//makeMove(board[sx][sy],sx,sy,tx,ty);
				redraw(grid);
				setTurn(board[tx][ty].getName(), sx, tx, ty);
				
				if(result == 5) {
					sb.append("Check.\n");
				}
			}
			else if(result == 2){
				sb.append("King in check!\n");
			}
			else if(result == 3) {
				sb.append("White wins by checkmate\n");
				gameRunning = false;
			}
			else if(result == 4) {
				sb.append("Black wins by checkmate!\n");
				gameRunning = false;
			}
			else if(result == 6) {
				sb.append("Draw.\n");
				gameRunning = false;
			}
			else {
				sb.append("Illegal move!").append('\n');
			}
			
		}
		else if(counter.get() == BotColor) {
			move = MSystem.MainFunction(board,turn,BotColor);
			
			//handle resignation
			if(move.length == 1) {
				if(PlayerColor) {
					sb.append("White wins by resignation");
				}
				else {
					sb.append("Black wins by resignation");
				}
				gameRunning = false;
				return;	
			}
			
			makeMove(board[move[0]][move[1]],move[0],move[1],move[2],move[3]);
			redraw(grid);
			setTurn(board[move[2]][move[3]].getName(), move[0], move[2], move[3]);
		}

		
		console.setText(sb.toString());

	}

	public static void makeMove(Piece piece, int x, int y, int tX, int tY){
		if(board[tX][tY] != null){
			//scoreboard(board[tX][tY].getName());
		}
		
		board[x][y] = null;
		board[tX][tY] = piece;
		
		
		/*
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if(board[i][j] != null)
				System.out.println(board[i][j].getName()+" at"+i+","+j);
			}
		}
		*/
		
		
		
		
	}
	
	private  String pieceSymbol(String piece, int startingX, int targetX, int targetY) {
		String symbol = "";
		
		if(piece.contains("knight")) {
			symbol = "N";
		}
		else if(piece.contains("bishop")) {
			symbol = "B";
		}
		else if(piece.contains("queen")) {
			symbol = "Q";
		}
		else if(piece.contains("rook")) {
			symbol = "R";
		}
		else if(piece.contains("king")) {
			symbol = "K";
		}

		
		symbol += xConv(targetX)+targetY;
		
		
		
		return symbol;
	}

	
	private String xConv(int a) {
		String symbol = "";
		
		switch(a) {
		case 0:
			symbol += "a";
			break;
		case 1:
			symbol += "b";
			break;
		case 2:
			symbol += "c";
			break;
		case 3:
			symbol += "d";
			break;
		case 4:
			symbol += "e";
			break;
		case 5:
			symbol += "f";
			break;
		case 6:
			symbol += "g";
			break;
		case 7: 
			symbol += "h";
			break;
		}
		
		return symbol;
	}
	
	
	private String gridState(int x, int y) {
		Piece element = board[x][y];
		
		if(element == null) {
			return null;
		}
		
		return element.getName();

	}
	
	/*
	private static void scoreboard(String piece) {
		if(counter.get()) {
			wUnitsLost.concat(piece+"["+MGameStuff.unitValue(piece)+"]\n");
		}
		else {
			bUnitsLost.concat(piece+"["+MGameStuff.unitValue(piece)+"]\n");
		}
		
	}
	*/
	
	
	
	private void initBoard(GridPane grid) {
		//fillBoard();
		board[0][0] = new Piece("rook_b",0,false,0,0);
		board[0][1] = new Piece("knight_b",1,false,0,1);
		board[0][2] = new Piece("bishop_b",2,false,0,2);
		board[0][3] = new Piece("queen_b",3,false,0,3);
		board[0][4] = new Piece("king_b",4,false,0,4);
		board[0][5] = new Piece("bishop_b",5,false,0,5);
		board[0][6] = new Piece("knight_b",6,false,0,6);
		board[0][7] = new Piece("rook_b",7,false,0,7);
		
		board[7][0]  = new Piece("rook_w",8,true,7,0);
		board[7][1]  = new Piece("knight_w",9,true,7,1);
		board[7][2]  = new Piece("bishop_w",10,true,7,2);
		board[7][3]  = new Piece("queen_w",11,true,7,3);
		board[7][4]  = new Piece("king_w",12,true,7,4);
		board[7][5] = new Piece("bishop_w",13,true,7,5);
		board[7][6]  = new Piece("knight_w",14,true,7,6);
		board[7][7]  = new Piece("rook_w",15,true,7,7);
		
		for (int i = 0; i < 8; i++) {
			board[1][i] = new Piece("pawn_b",16+i,false,1,i);
			board[6][i] = new Piece("pawn_w",24+i,true,6,i);
		}
		
		redraw(grid);

	}
	
	private void colorChanger(GridPane grid) {
		colorSwitch = (colorSwitch+1) % 3; 
		if(colorSwitch == 0) {
			gridColor1 = new Color(0.84,0.67,0.47,1.0);
			gridColor2 = new Color(0.98,0.88,0.71,1.0);
		}
		else if(colorSwitch == 1) {
			gridColor1 = new Color(0.28,0.0,0.18,1.0);
			gridColor2 = new Color(0.98,0.78,0.51,1.0);
		}
		else if(colorSwitch == 2){
			gridColor1 = new Color(0.57,0.12,0.27,1.0);
			gridColor2 = new Color(0.7,0.7,0.7,1.0);
		}
		redraw(grid);
	}
	
	public static void main(String[] args) {
		
		Random r = new Random();
		String temp = "";
		String temp1 = ""; String temp2 = "";


		FileReader freader = null;
		try {
			freader = new FileReader("weights04.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(freader);
		String s;
		int i = 0;

		
		try {
			while((s = br.readLine()) != null) {
				System.out.println(temp);
				temp += s;
				
				if(r.nextInt(2) == 1) {
					while(s.charAt(i) != ' ') {
						i++;
					}
				}
				
				temp1 = temp.substring(0,i+1);
				
				i++;
				
				while(s.charAt(i) != ' ') {
					i++;
				}
				
				temp2 = r.nextDouble() + temp.substring(i);
				
				temp = temp1 + " " + temp2;
				
				i = 0;
				
			


			}
		}
		catch(Exception e) {
			
		}
		
		
		try(FileWriter outFile = new FileWriter("weights05.txt",true);
				BufferedWriter bWriter = new BufferedWriter(outFile)){
				bWriter.write(temp);
			}
			catch(IOException e){
				e.printStackTrace();
			}
		
		launch(args);
	}
	
}

class Piece{
	
	private String name;
	private int id;
	private boolean color;
	private int x;
	private int y;

	
	public Piece(String name, int id, boolean color, int x, int y) {
		this.name = name;
		this.id = id;
		this.color = color;
		this.x = x;
		this.y = y;
	}
	
	
	
	public Piece(String name) {
		this.name = name;
	}
	
	
	public boolean getColor() {
		return color;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
}
