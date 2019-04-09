package main;

import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Engine extends Application {
	//HashMap<Integer, Character> board = new HashMap<Integer, Character>();
	private String[][] board = new String[8][8];
	//private boolean turn = false; 
	private SimpleBooleanProperty counter  = new SimpleBooleanProperty(true);
	
	@Override
	public void start(Stage window) throws Exception {
		GridPane grid = new GridPane();
		initBoard(grid);
		
		BorderPane bp = new BorderPane();
		Scene scene = new Scene(bp,1080,920);
		window.setScene(scene);
		window.show();
		
		//ImageView imV = new ImageView();
		//Image board = new Image(getClass().getClassLoader().getResource("resources/grid.png").toExternalForm(),860,860,true,true);
		//imV.setImage(board);
		
		
		FlowPane sidebar = new FlowPane();
		Button b1 = new Button("start");
		Button b2 = new Button("pause");
		Button b3 = new Button("end");
		HBox container1 = new HBox();
		HBox container2 = new HBox();
		HBox.setMargin(b1,new Insets(25.0,10.0,50.0,10.0));
		HBox.setMargin(b2,new Insets(25.0,10.0,50.0,10.0));
		HBox.setMargin(b3,new Insets(25.0,10.0,50.0,10.0));
		container1.getChildren().addAll(b1,b2,b3);
		Pane consolePane = new Pane();
		TextArea console = new TextArea();
		consolePane.setPadding(new Insets(30));
		console.setPrefSize(180, 400);
		consolePane.getChildren().add(console);
		Label turnLabel = new Label("awaiting game..");
		turnLabel.setPrefSize(250,50);
		turnLabel.setAlignment(Pos.CENTER);
		counter.addListener((observableValue,oldValue,newValue) -> {
			if(newValue) {
				turnLabel.setText("white");
			}
			else {
				turnLabel.setText("black");
			}
		});
		
		Button setparam1 = new Button("shape");
		Button setparam2 = new Button("f(x)");
		container2.getChildren().addAll(setparam1,setparam2);
		sidebar.setPrefWrapLength(120);
		sidebar.getChildren().addAll(consolePane,turnLabel,container1,container2);
		b3.setOnAction(e -> Platform.exit());
		b1.setOnAction(e -> playTurn(grid));
		bp.setRight(sidebar);

		
		bp.setLeft(redraw(grid));
		
		
	}

	private GridPane redraw(GridPane grid) {
		String indexCorr = "/99991357";
		
		for(int i = 1; i<9; i++) {
			for(int j = 8; j>0; j--) {
				
				Pane square = new Pane();
				Rectangle canvas = null;
				ImageView piece = new ImageView();
				
				
				if(i%2 == 0) {
					if(j%2 == 1) {
						canvas = new Rectangle(100,100,new Color(0.28,0.0,0.18,1));
					}
					else if (j%2 == 0) {
						canvas = new Rectangle(100,100,Color.WHITE);
						
					}
				}
				else {
					if(j%2 == 0) {
						canvas = new Rectangle(100,100,new Color(0.28,0.0,0.18,1));
					}
					else if (j%2 == 1) {
						canvas = new Rectangle(100,100,Color.WHITE);
					}
				}

				int temp = i;
				
				int temp2 = Math.abs(j - Integer.parseInt(""+indexCorr.charAt(j)));
				
				square.setId(temp+""+temp2);
				square.getChildren().add(canvas);
				square.getChildren().add(piece);

				square.setOnMousePressed(e -> gridFn(temp,temp2));
				square.setOnMouseReleased(e -> gridFn2(square,temp,temp2));
				
				
				//Canvas canvas = new Canvas(300, 250);
		        //GraphicsContext gc = canvas.getGraphicsContext2D();
		        //gc.setFill(Color.BLACK);
				//Pane canvas = new Pane();
				//canvas.setStyle("-fx-background-color: red;");
				GridPane.setConstraints(square,i,j);
				grid.getChildren().add(square);
				
				
				if(gridState(temp,temp2) != null) {
					piece.setImage(new Image(getClass().getClassLoader().getResource("resources/"+gridState(temp,temp2)+".png").toExternalForm(),100,100,true,true));
				}
				
				
			}
		}
		return grid;
	}
	/*
	private void initBoard() {
		for (int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				board[i][j] = " ";
			}
		}
		

	}*/
	
	private void playTurn(GridPane g) {
		setTurn();
		redraw(g);
	}
	
	private void setTurn() {
		counter.setValue(!counter.getValue());
		//turn = white;
	}
	
	private void gridFn(int x, int y) {
		System.out.println("grid selected at position:"+x+", "+y);
	}
	
	private void gridFn2(Pane p,int startX, int startY) {
		p.setOnMouseEntered(e -> gridFn3(p,startX,startY));
		System.out.println("mouse released at position:"+p.getId()+", ");
	}
	
	private void gridFn3(Pane p,int a, int b) {
		System.out.println("mouse entered position:"+p.getId()+", ");
		RuleSet.validate(board, a, b, Integer.parseInt(""+p.getId().charAt(0)), Integer.parseInt(""+p.getId().charAt(1)));
	}
	
	
	private String gridState(int x, int y) {
		String element = board[x-1][y-1];
		
		if(element == " ") {
			return null;
		}
		
		return element;

	}
	
	
	private void initBoard(GridPane grid) {
		board[0][7] = "rook_b";
		board[1][7] = "knight_b";
		board[2][7] = "bishop_b";
		board[3][7] = "queen_b";
		board[4][7] = "king_b";
		board[5][7] = "bishop_b";
		board[6][7] = "knight_b";
		board[7][7] = "rook_b";
		
		board[0][0] = "rook_w";
		board[1][0] = "knight_w";
		board[2][0] = "bishop_w";
		board[3][0] = "queen_w";
		board[4][0] = "king_w";
		board[5][0] = "bishop_w";
		board[6][0] = "knight_w";
		board[7][0] = "rook_w";
		
		for (int i = 0; i < 8; i++) {
			board[i][6] = "pawn_b";
			board[i][1] = "pawn_w";
		}
		
		redraw(grid);
		setTurn();
		
		
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
