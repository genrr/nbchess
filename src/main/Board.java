package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Board extends Application {
	private static Engine mayflower;	
	private static int[][][] board = new int[9][8][];
	private static List<String> posList = new ArrayList<String>();
	private static List<int[][][]> posHistory = new ArrayList<int[][][]>();
	public int moveCounter = 0;	
	private static int maxID = 42;
	private static int turnsWoPMOrC = 0; 
	private static boolean drawOfferedByBot = false;
	private static boolean gameRunning = false;
	private static boolean freePlay = true;
	private static SimpleIntegerProperty halfTurns  = new SimpleIntegerProperty(0);
	private static SimpleIntegerProperty fullTurns  = new SimpleIntegerProperty(1);
	public static SimpleBooleanProperty moveReady = new SimpleBooleanProperty(false);
	private static int currentTurn;
	private static boolean isDefaultBoardRotation = true;
	private static int PlayerColor;
	private static int BotColor;
	private static BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
	private static ScrollPane consoleContainer = new ScrollPane();
	private static TextArea console = new TextArea();
	private static GridPane grid = new GridPane();
	private static Color gridColor1;
	private static Color gridColor2;
	private static Color selectedColor = new Color(0.74,0.47,0.47,1.0);
	private static int colorSwitch = -1;
	private static int startX;
	private static int startY;
	private static String turnLabelText = "";
	private static Pane selectedTargetSquare = null;
	private Pane[][] sqArray = new Pane[8][8];
	private boolean check;
	
	Label turnLabel;


	@Override
	public void start(Stage window) throws Exception {
		colorChanger(grid);
		BorderPane bp = new BorderPane();
		Scene scene = new Scene(bp,1080,920);
		window.setScene(scene);
		window.show();
		VBox sidebar = new VBox();

		Button startButton = new Button("Start new game");
		Button loadButton = new Button("Load a game");
		Button resetButton = new Button("Reset board");
		Button resignButton = new Button("Resign");
		Button drawButton = new Button("Offer draw");
		Button rotateButton = new Button("Rotate board");
		Button colorButton = new Button("Change color scheme");
		Button leftButton = new Button("<-");
		Button rightButton = new Button("->");
		Button exitButton = new Button("Exit");
		exitButton.setAlignment(Pos.TOP_RIGHT);
		HBox initButtonContainer = new HBox();
		HBox arrowButtonContainer = new HBox();
		leftButton.setPrefSize(70, 70);
		rightButton.setPrefSize(70, 70);
		arrowButtonContainer.setPrefHeight(100);
		arrowButtonContainer.setAlignment(Pos.CENTER);
		initButtonContainer.getChildren().addAll(startButton,loadButton);
		arrowButtonContainer.getChildren().addAll(leftButton,rightButton);
		HBox functionSettingsContainer = new HBox();
		HBox.setMargin(colorButton,new Insets(25.0,10.0,50.0,10.0));		
		consoleContainer.setPadding(new Insets(15));
		console.resize(250, 550);	
		
//		console.textProperty().addListener(new ChangeListener<Object>() {
//			@Override
//			public void changed(ObservableValue<?> observable, Object oldValue,
//					Object newValue) {
//				console.setScrollTop(Double.MAX_VALUE);
//			}
//		});
		

		consoleContainer.setContent(console);
		turnLabel = new Label("");
		turnLabel.setPrefSize(250,50);
		turnLabel.setAlignment(Pos.CENTER);
		turnLabel.setText("Turn: 1 : White");
		


		halfTurns.addListener((observableValue,oldValue,newValue) -> {
				
				turnLabelText = halfTurns.get() % 2 == 0 ? "white" : "black";
				turnLabel.setText("Turn: "+fullTurns.get()+" : "+turnLabelText);

				System.out.println("halfturns | fullTurns : "+halfTurns+" "+fullTurns);
				

				if(currentTurn == BotColor && !freePlay) {
					if(mayflower == null) {
						mayflower = new Engine(queue,BotColor);
						mayflower.start();
					}

					moveReady.set(false);
					int temp;

					if((int) newValue % 2 == 1) {
						temp = fullTurns.get()+1;
					}
					else {
						temp = fullTurns.get();
					}


					Message msg = new Message(board,null,fullTurns.get(),"request");	
					try {
						queue.put(msg);
					}
					catch(InterruptedException e) {
						e.printStackTrace();
					}

				}

		});

		moveReady.addListener((observableValue,oldValue,newValue) -> {

			if(newValue) {
				System.out.println("move ready!");
				try {
					//System.out.println(queue.size());
					receiveMove(queue.take());
					
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

		});


		//add. controls for the ndf algorithm
		Button setparam1 = new Button("shape");
		Button setparam2 = new Button("f(x)");
		functionSettingsContainer.getChildren().addAll(setparam1,setparam2);
		HBox.setMargin(functionSettingsContainer, new Insets(0.5,0.5,0.5,0.5));
		exitButton.setAlignment(Pos.CENTER_RIGHT);
		VBox menu1 = new VBox();
		VBox menu2 = new VBox();

		menu1.getChildren().addAll(resignButton,drawButton,rotateButton, colorButton);
		menu2.getChildren().add(functionSettingsContainer);

		sidebar.getChildren().addAll(exitButton,consoleContainer,initButtonContainer,turnLabel,
				resetButton,menu1,menu2);
		startButton.setOnAction(e -> startNewGame(initButtonContainer));
		loadButton.setOnAction(e -> loadGame());
		resetButton.setOnAction(e -> resetGame(initButtonContainer,consoleContainer,startButton,loadButton));
		exitButton.setOnAction(e -> exitGame());
		colorButton.setOnAction(e -> colorChanger(grid));
		resignButton.setOnAction(e -> endGame());
		rotateButton.setOnAction(e -> {
			isDefaultBoardRotation = !isDefaultBoardRotation;
			redraw(grid);
		});
		drawButton.setOnAction(e -> offerDraw());
		leftButton.setOnAction(e -> rewindPos(-1));
		rightButton.setOnAction(e -> rewindPos(1));

		bp.setRight(sidebar);
		bp.setLeft(initBoard(grid));
		bp.setBottom(arrowButtonContainer);

		board[8][0] = new int[] {-1};
		board[8][1] = new int[] {-1};

		gameRunning = true;
		currentTurn = 1;

		posList.add(Resources.calculateHash(board));
		
		console.appendText("1. ");

		
		
		setparam1.setOnAction(e -> {
				PositionFeature p = new PositionFeature(board, 1);
				System.out.println("material count difference "+p.RelM()+"\n unitvalue sum difference "+p.RelMV()+
				"\ndifference of AVG unitvalues "+p.RelMAVG()+
				"\nown AVG rel. piecevalue "+p.RelPVAVG()+
				"\n%defended "+p.PercentDefended()+
				"\nmost defenses for own piece currently "+p.MostDefensesForPiece()+
				"\n%threat own "+p.PercentThreat_Own()+
				"\n%threat enemy "+p.PercentThreat_Enemy()+
				"\ncount open squares "+p.OpenSquareCount()+
				"\nno of safe sq "+p.CountAllSafeSquares()+
				"\nno of legal sq "+p.CountAllAvailableSquares()+
				"\nno of free sq "+p.CountAllFreeSquares()+
				"\nmin distance opponent has to our king "+p.MinDistKing_Enemy()+
				"\nmin distance we have to enemy king "+p.MinDistKing_Own());
		});
		
	}




	//colors
	//0.28,0.0,0.18,1
	//


	private void startNewGame(HBox cont) {
		Button selectionW = new Button("White");
		Button selectionB = new Button("Black");
		Button selectionR = new Button("Random");

		Random generator = new Random();

		selectionW.setOnAction(e -> {	PlayerColor = 1;
			BotColor = 0;
			//counter.setValue(true);
			cont.setVisible(false);
			currentTurn = 1;
			freePlay = false;
			gameRunning = true;
			fullTurns.set(1);
			halfTurns.set(0);
		});
		selectionB.setOnAction(e -> {	PlayerColor = 0; 
			BotColor = 1; 
			//counter.setValue(true); 
			cont.setVisible(false);
			isDefaultBoardRotation = false;
			redraw(grid);
			currentTurn = 1;
			freePlay = false;
			gameRunning = true;
			fullTurns.set(1);
			halfTurns.set(0);

		});
		selectionR.setOnAction(e -> {	PlayerColor = generator.nextInt(2); 
			BotColor = (PlayerColor + 1) % 2; 
			//counter.setValue(true); 
			cont.setVisible(false);
	
			if((PlayerColor + 1) % 2 == 1) {
				isDefaultBoardRotation = false;
				redraw(grid);
			}
			currentTurn = 1;
			freePlay = false;
			gameRunning = true;
			fullTurns.set(1);
			halfTurns.set(0);

		});

		cont.getChildren().clear();
		cont.getChildren().addAll(selectionW,selectionB,selectionR);



	}


	private void resetGame(HBox initButtonContainer, ScrollPane consoleCont, Button s, Button l) {
		initButtonContainer.getChildren().clear();
		initButtonContainer.getChildren().addAll(s,l);
		initButtonContainer.setVisible(true);
		if(mayflower != null) {
			try {
				queue.put(new Message(null, null, 0, "exit"));
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
		//gameRunning = true;
		setWOPMORC(0);
		freePlay = true;
		moveCounter = 0;
		currentTurn = 1;
		fullTurns.set(1);
		halfTurns.set(0);
		gameRunning = true;
		((TextArea)consoleCont.getContent()).clear();
		console.clear();
		console.appendText("1.");
		initBoard(grid);
		posList.clear();
		posList.add(Resources.calculateHash(board));
	}

	private void exitGame() {
		gameRunning = false;
		
		if(mayflower != null && mayflower.isAlive()) {
			try {
				queue.put(new Message(null, null, 0, "exit"));
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}

		Platform.exit();
	}




	private void loadGame() {

		VBox v = new VBox();
		TextArea tb = new TextArea();
		Button b = new Button("load FEN");
		v.getChildren().addAll(tb,b);

		Stage smallmenu = new Stage();
		Scene smScene = new Scene(v,700,125);

		b.setOnAction(e -> {
			if(tb.getText() != null) {
				if(!parseFEN(tb.getText()))
				{
					console.appendText("Error parsing FEN position!\n");
					gameRunning = false;
					return;
				}
				else
				{
					gameRunning = true;
				}
				
				if(currentTurn == 0)
				{
					halfTurns.set(1);
				}
				else
				{
					halfTurns.set(0);
				}
				turnLabelText = halfTurns.get() % 2 == 0 ? "white" : "black";
				turnLabel.setText("Turn: "+fullTurns.get()+" : "+turnLabelText);
				redraw(grid);
				moveCounter = 0;
				posHistory.clear();
				posHistory.add(MGameUtility.cloneArray(board));
				posList.clear();
				posList.add(Resources.calculateHash(board));
				smallmenu.hide();
			}
		});



		smallmenu.setScene(smScene);
		smallmenu.show();


	}
	
	//board
	//halfturns, fullturns, currentTurn
	//en passant square
	//wopmorc
	//3-fold, 5-fold repetition
	
	void rewindPos(int direction)
	{
		if(direction == -1)
		{
			if(moveCounter > 0)
			{
				moveCounter--;
				turnLabelText = currentTurn == 0 ? "white" : "black";
				turnLabel.setText("Turn: "+fullTurns.get()+" : "+turnLabelText);
				currentTurn = Math.abs(currentTurn - 1);
			}
			
			
//			if(halfTurns.get() > 0)
//			{
//				halfTurns.set(halfTurns.get() - 1);
//			}

			
//			if(halfTurns.get() % 2 == 1)
//			{
//				fullTurns.set(fullTurns.get()-1);
//			}
		}
		else if(direction == 1)
		{
			if(moveCounter < posHistory.size()-1)
			{
				moveCounter++;
				turnLabelText = currentTurn == 0 ? "white" : "black";
				turnLabel.setText("Turn: "+fullTurns.get()+" : "+turnLabelText);
				currentTurn = (currentTurn + 1) % 2;
			}
			
//			if(halfTurns.get() < posHistory.size()-1)
//			{
//				halfTurns.set(halfTurns.get() + 1);
//			}

			
//			if(halfTurns.get() % 2 == 1)
//			{
//				fullTurns.set(fullTurns.get()+1);
//			}
		}
		
		if(!gameRunning)
		{
			gameRunning = true;
		}

		
		System.out.println("movecounter "+moveCounter);

		board = MGameUtility.cloneArray(posHistory.get(moveCounter));
		

		redraw(grid);
		
	}
	

	/*
	 * called when we resign the game
	 */

	private void endGame() {
		if(gameRunning) {
			if(mayflower != null){
				if(BotColor == 1) {
					
					console.appendText("\nWhite wins by resignation");
				}
				else {
					console.appendText("\nBlack wins by resignation");
				}
				try {
					queue.put(new Message(null, null, 0, "exit"));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				if(currentTurn == 1) {
					console.appendText("\nBlack wins by resignation");
				}
				else if(currentTurn == 0) {
					console.appendText("\nWhite wins by resignation");
				}
			}
			
			gameRunning = false;

		}
	}




	/*
	 * Offers draw to engine
	 * 
	 * checks if bot offered draw, if yes, end game
	 * if not, send draw request "move" to engine
	 * 
	 */


	private void offerDraw() {
		if(drawOfferedByBot) {
			gameRunning = false;
			console.appendText("draw accepted");
		}
		else {
			console.appendText("Draw offered by player..");
			try {
				queue.put(new Message("draw requested by player"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}



	//set turn number (fullturns, halfturns) and change current turn (0,1)
	
	private void setTurn(int pieceId, int y, int tx, int ty,int result) {
		currentTurn = (currentTurn+1) % 2;
		moveCounter++;
		

		if(halfTurns.get() % 2 == 1) {

			fullTurns.set(fullTurns.get()+1);
			
		}
		
		
		halfTurns.set(halfTurns.getValue()+1);
		
		if(result == 7 || result == 9) {
			console.appendText("O-O ");
		}
		else if(result == 8 || result == 10) {
			console.appendText("O-O-O ");
		}
		else {
			console.appendText(pieceSymbol(pieceId,y,tx,ty,result == 11)+(result == 5 ? '+' : "")+" ");
			
			if(halfTurns.get() % 2 == 0)
			{
				console.appendText("\n"+fullTurns.get()+". ");
			}
			
			if(result == 5) {
				check = true;
			}
			
			if(result == 61)
			{
				console.appendText("\n3-fold repetition!");
				//canRequestDraw = true;
			}
			else if(result == 63)
			{
				console.appendText("\n50 moves passed without a pawn move or capture!");
				//canRequestDraw = true;
			}
			else if(result == 60) {
				console.appendText("\nDraw by insufficient material!");
				gameRunning = false;
			}

			else if(result == 62)
			{
				console.appendText("\nDraw by 5-fold repetition!");
				gameRunning = false;
			}

			else if(result == 64)
			{
				console.appendText("\nDraw by 75-move rule!");
				gameRunning = false;
			}
			else if(result == 65)
			{
				console.appendText("\nStalemate!");
				gameRunning = false;
			}

		}


			
		System.out.println("botColor "+BotColor+", current turn "+currentTurn+", movecounter: "+moveCounter+" counter: "+halfTurns+"\n");



	}



	public void receiveMove(Message m) {

		int[] move = m.getMove();
		String status = m.getStatus();

		System.out.println("setting message object: "+"board: "+board+" turn: "+(fullTurns+""));
		System.out.println(move);


		//handle engine resignation
		if(status.equals("resign") || status.equals("draw accepted")) {
			if(!status.equals("draw accepted")) {
				if(PlayerColor == 1) {
					console.appendText("White wins by resignation");
				}
				else {
					console.appendText("Black wins by resignation");
				}
			}
			else {
				console.appendText("Draw accepted");
			}
			

			//shut down engine thread
			try {
				queue.put(new Message(null, null, 0, "exit"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gameRunning = false;
			return;	
		}
		else if(m.getStatus().equals("draw requested")) {
			drawOfferedByBot = true;
			console.appendText("Draw offered by mayflower -- press 'Draw' to accept, play move to decline");
		}


		moveHandler(move[4],move[0],move[1],move[2],move[3]);
	}


	private GridPane redraw(GridPane grid) {
		int i;
		int j;
		int i2 = 0;
		int j2 = 0;

		grid.getChildren().clear();

		for(i = isDefaultBoardRotation ? 0 : 7; i<8 && i > -1; i = isDefaultBoardRotation ? i + 1 : i - 1, i2++) {
			for(j = isDefaultBoardRotation ? 0 : 7; j<8 && j > -1; j = isDefaultBoardRotation ? j + 1 : j - 1, j2 = (j2+1)%8) {

				sqArray[i][j] = new Pane();
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

				sqArray[i][j].setId(temp+""+temp2);
				sqArray[i][j].getChildren().add(canvas);
				sqArray[i][j].getChildren().add(piece);
				

				if(squareContent(board[temp][temp2]) != null) {
					piece.setImage(new Image(getClass().getClassLoader().getResource("resources/"+squareContent(board[temp][temp2])+".png").toExternalForm(),100,100,true,true));
				}
				
				Pane p = sqArray[i][j];
				
				//p.setOnMouseDragged(value);
				//p.setOnMouseDragEntered(value);
				//p.setOnMouseDragReleased(value);
				
				p.setOnMousePressed(e -> p.setMouseTransparent(true));
				p.setOnMouseReleased(e -> p.setMouseTransparent(false));
				
				p.setOnDragDetected(e -> {
					if(!gameRunning)
					{
						return;
					}
					
					((Rectangle)(p.getChildren().get(0))).setFill(new Color(0.53, 0.59, 0.57, 1));
					p.startFullDrag();
					
					e.consume();
					});
				

				p.setOnMouseDragEntered(e -> {
					Pane n = (Pane)(e.getGestureSource());
					//Image s = ((ImageView) n.getChildren().get(1)).getImage();
					int a = Integer.parseInt(""+n.getId().charAt(0));
					int b = Integer.parseInt(""+n.getId().charAt(1));
					((sqArray[temp2][temp].getChildren().get(0))).setViewOrder(5);
					if(board[a][b] != null && MGameUtility.attack(board, board[a][b], temp, temp2))
					{
						((Rectangle)(sqArray[temp2][temp].getChildren().get(0))).setFill(new Color(0.2, 0.6, 0.4, 1));
					}
					
					((sqArray[temp2][temp].getChildren().get(1))).setViewOrder(4);
					//((ImageView)(sqArray[temp2][temp].getChildren().get(1))).setImage(s);
					//((ImageView)(sqArray[b][a].getChildren().get(1))).setImage(null);
					e.consume();
				});
				
				p.setOnMouseDragReleased(e -> {
					Pane n = (Pane)(e.getGestureSource());
					int a = Integer.parseInt(""+n.getId().charAt(0));
					int b = Integer.parseInt(""+n.getId().charAt(1));
					
					for(int v = 0; v<8; v++)
					{
						for(int w = 0;w <8;w++)
						{
							if(v%2 == 0) {
								if(w%2 == 1) {
									((Rectangle)(sqArray[w][v].getChildren().get(0))).setFill(gridColor1);
								}
								else if (w%2 == 0) {
									((Rectangle)(sqArray[w][v].getChildren().get(0))).setFill(gridColor2);

								}
							}
							else {
								if(w%2 == 0) {
									((Rectangle)(sqArray[w][v].getChildren().get(0))).setFill(gridColor1);
								}
								else if (w%2 == 1) {
									((Rectangle)(sqArray[w][v].getChildren().get(0))).setFill(gridColor2);
								}
							}
							
						}
					}
					
					moveValidator(a, b, temp, temp2);
					e.consume();
				});

				int[] t = MGameUtility.getKingPos(board,currentTurn);
				
				if(check && i == t[1] && j == t[0]) {
					((Rectangle)p.getChildren().get(0)).setFill(new Color(1.0,0.2,0.0,0.7));
					check = false;
				}
		
				GridPane.setConstraints(sqArray[i][j],i2,j2);
				grid.getChildren().add(sqArray[i][j]);

				
			}
		}


		return grid;
	}

	
	
	
	//Checks whether current moves is one of the legal moves, if it is, call moveHandler()

	private void moveValidator(int sx, int sy, int tx, int ty) {

		int result = 0;
		boolean moveIsLegal = false;

		if(currentTurn == PlayerColor || freePlay) {
			storeKingAndRookPositions(board, currentTurn);

			ArrayList<int[]> allMoves = MGameUtility.getAllMoves(board, currentTurn);

			for (int[] list : allMoves) {
				System.out.println(list[0]+ " "+
						list[1]+ " "+
						list[2]+ " "+
						list[3]+ " "+
						list[4]);
				if(list[0] == sx &&
						list[1] == sy &&
						list[2] == tx &&
						list[3] == ty) {
					moveIsLegal = true;
					result = list[4];
					break;
				}

				
			}
			
			System.out.println("#current move "+sx+" "+sy+" "+tx+" "+ty+" "+result+" "+moveIsLegal);
			

			if(moveIsLegal) {
				moveHandler(result,sx,sy,tx,ty);
			}
			else {
				console.appendText("\nnot a valid move\n");
			}
			
			

		}
	}
	
	//Handles everything related to moves, like 
	//does the game end?
	//updating board
	//setting turn
	//calculates hash of the position and stores it to a list in order to check for 3-fold repetition
	//stores position in a list in order to allow traversing back and forth the current game

	public void moveHandler(int result,int sx, int sy, int tx, int ty) {
		
		//if current move is a pawn move or a capture, reset 50-move rule counter
		if(board[sx][sy][5] == 1 || board[tx][ty] != null) {
			Board.setWOPMORC(0);
		}
		else
		{
			incrementWOPMORC();
		}
		
		
		if(result == 0 || result == 3 || result == 4 || result == 5 || result == 6) {
			setTurn(board[sx][sy][5], sx, tx, ty,result);
			makeMove(board[sx][sy],sx,sy,tx,ty);
			
			if(result == 3) {
				console.appendText("White wins by checkmate!\n");
				if(mayflower != null) {
					try {
						queue.put(new Message(null, null, 0,"exit"));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				gameRunning = false;
			}
			else if(result == 4) {
				console.appendText("Black wins by checkmate!\n");
				if(mayflower != null) {
					try {
						queue.put(new Message(null, null, 0, "exit"));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				gameRunning = false;
			}
			
		}
		else {
			if(result == 7) {
				makeMove(board[0][7],0,7,0,5);
				makeMove(board[0][4],0,4,0,6);
			}
			else if(result == 8) {
				makeMove(board[0][0],0,0,0,3);
				makeMove(board[0][4],0,4,0,2);
			}
			else if(result == 9) {
				makeMove(board[7][7],7,7,7,5);
				makeMove(board[7][4],7,4,7,6);
			}
			else if(result == 10) {
				makeMove(board[7][0],7,0,7,3);
				makeMove(board[7][4],7,4,7,2);
			}
			else if(result == 11) {
				board[tx][ty] = board[sx][sy];
				board[sx][sy] = null;
				board[sx][ty] = null;
			}
			else if(result == 12) {
				promotionSelector(sx, sy, tx, ty);
				makeMove(board[sx][sy],sx,sy,tx,ty);
			}
			else if(result == 13) {
				setTurn(board[sx][sy][5], sx, tx, ty,result);
				makeMove(board[sx][sy],sx,sy,tx,ty);
				board[8][0] = new int[] {tx + 1};
				board[8][1] = new int[] {ty};
			}
			else if(result == 14) {
				setTurn(board[sx][sy][5], sx, tx, ty,result);
				makeMove(board[sx][sy],sx,sy,tx,ty);
				board[8][0] = new int[]{tx - 1};
				board[8][1] = new int[]{ty};
			}
			
			if(result != 13 && result != 14) {
				board[8][0] = new int[]{-1};
				board[8][1] = new int[]{-1};
			}
			
			if(result >= 60 && result <= 65)
			{
				makeMove(board[sx][sy],sx,sy,tx,ty);
				setTurn(board[tx][ty][5], sx, tx, ty,result);
			}
			else if(result != 12 && result != 13 && result != 14) {
				setTurn(board[tx][ty][5], sx, tx, ty,result);
			}
			
		}
		redraw(grid);
		posList = posList.subList(0, moveCounter);
		posList.add(Resources.calculateHash(board));
		posHistory = posHistory.subList(0, moveCounter);
		posHistory.add(MGameUtility.cloneArray(board));
		System.out.println(posList.toString());
		System.out.println("added position to position history, now containing "+posHistory.size()+" elements");
		
	}



	public static int getMaxID() {
		return maxID;
	}

	
	
	public void makeMove(int[] piece, int x, int y, int tX, int tY){
		board[x][y] = null;
		board[tX][tY] = piece;
		piece[3] = tX;
		piece[4] = tY;
	}


	//This method tracks whether the king or rooks have been moved to determine castling rights
	//t = {rookw1,rookw2,rookb1,rookb2,wk,bk,epx,epy}

	public static void storeKingAndRookPositions(int[][][] board, int turn) {


		for(int j = 0; j < 8; j++) {
			for(int i = 0; i < 8; i++) {

				if(board[i][j] == null) {
					continue;
				}
				else {
					//w king moved
					if(board[i][j][0] == 23 && (i != 7 || j != 4)) {
						System.out.println("w king moved "+i+" "+j);
						board[8][6] = new int[] {1};
					}
					// b king moved
					if(board[i][j][0] == 16 && (i != 0 || j != 4)) {
						System.out.println("b king moved "+i+" "+j);
						board[8][7] = new int[] {1};
					}

				}

				//rooks moved?
				if(board[i][j][1] == 11) {
					if(i != 0 || j != 0) {
						System.out.println("11 rook moved "+i+" "+j);
						board[8][2] =  new int[] {1};
					}
				}
				else if(board[i][j][1] == 18) {
					if(i != 0 || j != 7) {
						System.out.println("18 rook moved "+i+" "+j);
						board[8][3] = new int[] {1};
					}
				}
				else if(board[i][j][1] == 27){
					if(i != 7 || j != 0) {
						System.out.println("27 rook moved "+i+" "+j);
						board[8][4] = new int[] {1};
					}
				}
				else if(board[i][j][1] == 34){
					if(i != 7 || j != 7) {
						System.out.println("34 rook moved "+i+" "+j);
						board[8][5] = new int[] {1};
					}
				}
			}
		}

		//System.out.println("own king: "+t[0]+", "+t[1]+" enemy king: "+t[2]+", "+t[3]);

	}

	

	
	//Handles promotion logic in its own Stage and Scene
	//player selects the piece he wants to promote the pawn by clicking image in the pop-up box

	private void promotionSelector(int X, int Y, int tX, int tY) {
		VBox list = new VBox();
		ImageView rook = new ImageView();
		ImageView knight = new ImageView();
		ImageView bishop = new ImageView();
		ImageView queen = new ImageView();

		char color = (currentTurn == 1 ? 'w' : 'b');
		rook.setImage(new Image(getClass().getClassLoader().getResource("resources/rook_"+color+".png").toExternalForm(),100,100,true,true));
		knight.setImage(new Image(getClass().getClassLoader().getResource("resources/knight_"+color+".png").toExternalForm(),100,100,true,true));
		bishop.setImage(new Image(getClass().getClassLoader().getResource("resources/bishop_"+color+".png").toExternalForm(),100,100,true,true));
		queen.setImage(new Image(getClass().getClassLoader().getResource("resources/queen_"+color+".png").toExternalForm(),100,100,true,true));	
		list.getChildren().addAll(rook,knight,bishop,queen);

		Stage smallmenu = new Stage();
		Scene smScene = new Scene(list,100,400);
		smallmenu.setScene(smScene);
		smallmenu.show();

		maxID++;

		rook.setOnMousePressed(e -> {
			if(board[tX][tY] != null) {
				board[tX][tY][3] = -1;
				board[tX][tY][4] = -1;
			}
			board[tX][tY] = new int[] {currentTurn == 1 ? 18 : 11,maxID+1,currentTurn,tX,tY,2};
			board[X][Y] = null;
			grid.getChildren().remove(grid.getChildren().size()-1);
			smallmenu.hide();
			RuleSet.validate(board, currentTurn, X, Y, tX, tY);
			setTurn(board[tX][tY][5], X, tX, tY,12);
			console.appendText("=R ");
			posHistory.add(MGameUtility.cloneArray(board));
			redraw(grid);

		});
		knight.setOnMousePressed(e -> {
			if(board[tX][tY] != null) {
				board[tX][tY][3] = -1;
				board[tX][tY][4] = -1;		
			}
			board[tX][tY] = new int[] {currentTurn == 1 ? 19 : 12,maxID+2,currentTurn,tX,tY,3};
			board[X][Y] = null;
			grid.getChildren().remove(grid.getChildren().size()-1);
			smallmenu.hide();
			setTurn(board[tX][tY][5], X, tX, tY,12);
			posHistory.add(MGameUtility.cloneArray(board));
			redraw(grid);
			console.appendText("=N ");
		});

		bishop.setOnMousePressed(e -> {
			if(board[tX][tY] != null) {
				board[tX][tY][3] = -1;
				board[tX][tY][4] = -1;			
			}
			board[tX][tY] = new int[] {currentTurn == 1 ? 20 : 13, maxID+3,currentTurn,tX,tY,4};
			board[X][Y] = null;
			grid.getChildren().remove(grid.getChildren().size()-1);
			smallmenu.hide();
			setTurn(board[tX][tY][5], X, tX, tY,12);
			posHistory.add(MGameUtility.cloneArray(board));
			redraw(grid);
			console.appendText("=B ");
		});		
		queen.setOnMousePressed(e -> {
			if(board[tX][tY] != null) {
				board[tX][tY][3] = -1;
				board[tX][tY][4] = -1;			
			}
			board[tX][tY] = new int[] {currentTurn == 1 ? 22 : 15,maxID+4,currentTurn,tX,tY,5};
			board[X][Y] = null;
			grid.getChildren().remove(grid.getChildren().size()-1);
			smallmenu.hide();
			setTurn(board[tX][tY][5], X, tX, tY,12);
			posHistory.add(MGameUtility.cloneArray(board));
			redraw(grid);
			console.appendText("=Q ");
		});

		maxID += 4;

	}

	//Forms a string denoting piece moved, whether or not it captures and the target square

	public static String pieceSymbol(int id, int startingX, int targetX, int targetY,boolean enPassant) {
		String symbol = "";

		
		if(id == 3) {
			symbol = "N";
		}
		else if(id == 4) {
			symbol = "B";
		}
		else if(id == 5) {
			symbol = "Q";
		}
		else if(id == 2) {
			symbol = "R";
		}
		else if(id == 6) {
			symbol = "K";
		}

		symbol += (board[targetX][targetY] != null || enPassant) ? 'x' : "";
		symbol += xConv(targetY)+(8-targetX);


		return symbol;
	}

	//Converts y coordinates of board into characters, so e.g. x=4, y=4 is converted to e4

	private static String xConv(int a) {
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

	//returns the piece at board in (x,y)

	public static String squareContent(int[] element) {

		
		if(element == null) {
			return null;
		}
		
		String s = "";
		char color = (element[2] == 1 ? 'w' : 'b');
		
		switch(element[5])
		{
		case 1:
			s = "pawn";
			break;
		case 2:
			s = "rook";
			break;
		case 3:
			s = "knight";
			break;
		case 4:
			s = "bishop";
			break;
		case 5:
			s = "queen";
			break;
		case 6:
			s = "king";
			break;
		}

		return s + "_" + color;

	}

	/*
	 * parses FEN position strings like:
	 * 
	 * rnbqkbnr/ppppp2p/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	 */

	private static boolean parseFEN(String FEN) {

		boolean[] piecePlacedStatus = new boolean[] {true,true,true,true,true,true,false,false};
		int i = 0;
		int j = 0;
		int index = 0;
		int n = 0;
		char c = ' ';
		int temp = 0;

		while(index < FEN.length()) {
			c = FEN.charAt(index);
			if(c == '/') {
				i++;
			}

			else if(c == ' ') {
				parseSpecial(FEN,0,index);
				break;
			}

			else {

				try {
					n = Integer.parseInt(c+"");
				} 
				//not a number
				catch (NumberFormatException e) {
					parsePieces(c, index, i, j,piecePlacedStatus);
					j = (j + 1) % 8;
					index++;
					continue;
				}
				//a number
				temp = 0;

				while(temp < n) {
					//System.out.println("j: "+j+" temp: "+temp);

					board[i][temp+j] = null;
					temp++;
				}
				j = (j+temp) % 8;

			}

			index++;

		}
		
		if(!piecePlacedStatus[6] || !piecePlacedStatus[7])
		{
			console.appendText("current FEN position does not contain enough kings!\n");
			return false;
		}

		return true;


	}
	
	//creates FEN string from current position, opens up pop-up box, where it can be copied 
	
	public static void generateFEN()
	{
		
	}


	//This method parses FEN string info other than actual board (en passant square, castling, turns for 50-move rule, current turn number, whose turn)

	private static void parseSpecial(String FEN, int read, int index) {
		String castlingRights = "";

		System.out.println(FEN.length());

		while(index < FEN.length()) {	

			System.out.println(FEN.charAt(index)+" <-");
			System.out.println("read "+read);

			if(read != 0 && FEN.charAt(index) == ' ') {
				index++;
				read++;
			}

			//set active color
			if(read == 0) {
				index++;
				if(FEN.charAt(index) == 'b') {
					currentTurn = 0;
				}
				else if(FEN.charAt(index) == 'w') {
					currentTurn = 1;
				}
				read++;
				index += 2;
				continue;
			}
			//set castling rights, if found
			else if(read == 1) {
				if(FEN.charAt(index) != '-') {

					while(FEN.charAt(index) != ' ') {
						castlingRights += FEN.charAt(index);
						System.out.println(castlingRights);
						index++;
					}
					System.out.println("exited loop, char: "+FEN.charAt(index)+" <-");
					read++;

				}
				setCastling(castlingRights);
			}
			//set en passant square if found
			else if(read == 2) {
				if(FEN.charAt(index) != '-') {

					int tempY = -1;

					System.out.println(FEN.charAt(index));

					switch(FEN.charAt(index)) {
					case 'a':
						tempY = 0;
						break;
					case 'b':
						tempY = 1;
						break;
					case 'c':
						tempY = 2;
						break;
					case 'd':
						tempY = 3;
						break;
					case 'e':
						tempY = 4;
						break;
					case 'f':
						tempY = 5;
						break;
					case 'g':
						tempY = 6;
						break;
					case 'h':
						tempY = 7;
						break;
					}


					int tempX = 7 - Integer.parseInt(FEN.charAt(index+1)+"") - 1;

					board[8][0][0] = tempX;
					board[8][1][0] = tempY;

					index++;

				}
			}
			//set moves for the 50-move rule
			else if(read == 3) {
				String turns = "";
				if(FEN.charAt(index+1) == ' ')
				{
					turns = ""+FEN.charAt(index);
				}
				else
				{
					turns = ""+FEN.charAt(index)+FEN.charAt(index+1);
					index++;
				}
				
				turnsWoPMOrC = Integer.parseInt(turns);
				System.out.println("50-moves "+turnsWoPMOrC);
			}
			//set full moves
			else if(read == 4) {
				fullTurns.set(Integer.parseInt(FEN.charAt(index)+""));
			}



			index++;


		}
	}

	
	//This method parses the actual board from FEN string

	private static void parsePieces(char piece,int index,int i, int j,boolean[] t) {


		int uniqueBP = 0;
		int uniqueWP = 0;

		switch(piece) {

		case 'r':
			if(t[0]) {
				board[i][j] = new int[] {11,11,0,i,j,2};
				t[0] = false;
			}
			else {
				board[i][j] = new int[] {11,18,0,i,j,2};
			}
			break;	
		case 'n':
			if(t[1]) {
				board[i][j] = new int[] {12,12,0,i,j,3};
				t[1] = false;
			}
			else {
				board[i][j] = new int[] {12,17,0,i,j,3};
			}
			break;

		case 'b':
			if(t[2]) {
				board[i][j] = new int[] {13,13,0,i,j,4};
				t[2] = false;
			}
			else {
				board[i][j] = new int[] {14,16,0,i,j,4};
			}
			break;
		case 'q':
			board[i][j] = new int[] {15,14,0,i,j,5};
			break;
		case 'k':
			board[i][j] = new int[] {16,15,0,i,j,6};
			t[6] = true;
			break;
		case 'p':
			board[i][j] = new int[] {17,19+uniqueBP,0,i,j,1};
			uniqueBP++;
			break;

		case 'R':
			if(t[3]) {
				board[i][j] = new int[] {18,27,1,i,j,2};
				t[3] = false;
			}
			else {
				board[i][j] = new int[] {18,34,1,i,j,2};
			}
			break;
		case 'N':
			if(t[4]) {
				board[i][j] = new int[] {19,28,1,i,j,3};
				t[4] = false;
			}
			else {
				board[i][j] = new int[] {19,33,1,i,j,3};
			}
			break;
		case 'B':
			if(t[5]) {
				board[i][j] = new int[] {20,29,1,i,j,4};
				t[5] = false;
			}
			else {
				board[i][j] = new int[] {21,32,1,i,j,4};
			}
			break;
		case 'Q':
			board[i][j] = new int[] {22,30,1,i,j,5};
			break;
		case 'K':
			board[i][j] = new int[] {23,31,1,i,j,6};
			t[7] = true;
			break;
		case 'P':
			board[i][j] = new int[] {24,35+uniqueWP,1,i,j,1};
			uniqueWP++;
			break;

		}
		index++;

	}
	
	//Parses castling rights from FEN string 

	public static void setCastling(String s) {

		if(s.equals("-")) {
			board[8][7] = new int[] {1};
			board[8][6] = new int[] {1};
		}
		else if(s.equals("KQ")) {
			board[8][7] = new int[] {1};
		}
		else if(s.equals("kq")) {
			board[8][6] = new int[] {1};
		}
		else if(s.equals("Kkq")) {
			board[8][4] = new int[] {1};
		}
		else if(s.equals("Qkq")) {
			board[8][5] = new int[] {1};
		}
		else if(s.equals("KQk")) {
			board[8][2] =  new int[] {1};
		}
		else if(s.equals("KQq")) {
			board[8][3] = new int[] {1};
		}
		else if(s.equals("Qq")) {
			board[8][3] = new int[] {1};
			board[8][5] = new int[] {1};
		}
		else if(s.equals("Kk")) {
			board[8][2] = new int[] {1};
			board[8][4] = new int[] {1};
		}
		else if(s.equals("Qk")) {
			board[8][5] = new int[] {1};
			board[8][2] = new int[] {1};
		}
		else if(s.equals("Kq")) {
			board[8][4] = new int[] {1};
			board[8][3] = new int[] {1};
		}

	}
	
	//initialize board to the starting position
	
	private GridPane initBoard(GridPane grid) {

		board[0][0] = new int[] {11,11,0,0,0,2};
		board[0][1] = new int[] {12,12,0,0,1,3};
		board[0][2] = new int[] {13,13,0,0,2,4};
		board[0][3] = new int[] {15,14,0,0,3,5};
		board[0][4] = new int[] {16,15,0,0,4,6};
		board[0][5] = new int[] {14,16,0,0,5,4};
		board[0][6] = new int[] {12,17,0,0,6,3};
		board[0][7] = new int[] {11,18,0,0,7,2};

		board[7][0]  = new int[] {18,27,1,7,0,2};
		board[7][1]  = new int[] {19,28,1,7,1,3};
		board[7][2]  = new int[] {20,29,1,7,2,4};
		board[7][3]  = new int[] {22,30,1,7,3,5};
		board[7][4]  = new int[] {23,31,1,7,4,6};
		board[7][5]  = new int[] {21,32,1,7,5,4};
		board[7][6]  = new int[] {19,33,1,7,6,3};
		board[7][7]  = new int[] {18,34,1,7,7,2};
		
		for(int i = 0; i<8; i++) {
			board[8][i] = new int[] {0};
		}

		for (int i = 0; i < 8; i++) {
			board[1][i] = new int[] {17,19+i,0,1,i,1};
			board[6][i] = new int[] {24,35+i,1,6,i,1};
		}

		for(int i = 2; i<6; i++) {
			for(int j = 0; j<8; j++) {
				board[i][j] = null;
			}
		}

		posHistory.add(MGameUtility.cloneArray(board));
		

		return redraw(grid);
	}


	public static List<String> getPosList(){
		return posList;
	}

	public static void setWOPMORC(int i) {
		System.out.println("setting counter for 50-moves rule"+i);
		turnsWoPMOrC = i;
	}

	public static void incrementWOPMORC() {
		turnsWoPMOrC++;
	}

	public static int getWOPMORC() {
		return turnsWoPMOrC;
	}

	
	//
	
	private void colorChanger(GridPane grid) {
		colorSwitch = (colorSwitch+1) % 3; 
		if(colorSwitch == 0) {
			gridColor1 = new Color(12.0/255,0.0,35.0/255,0.25);//Color(0.84,0.67,0.47,1.0);
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
		launch(args);
	}

}

