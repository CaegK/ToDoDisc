package view;

import frame.EmptyFrame;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class InitGUI {
	public EmptyFrame frame = new EmptyFrame();
	GridPane root = new GridPane();
	VBox botTokenVBox = new VBox();
	Label welcomeLabel = new Label("ToDoDisc");
	Label botTokenLabel = new Label("Bot token: ");
	public TextField botTokenField = new TextField();
	public Button connectBtn = new Button("Connect");
	
	public InitGUI() { 
		//setting style classes
		root.getStyleClass().add("root-grid");
		welcomeLabel.setId("welcome-label");
		botTokenLabel.setId("bot-token-label");
		botTokenField.setId("bot-token-field");
		connectBtn.setId("connect-button");
		
		botTokenVBox.getChildren().addAll(botTokenLabel, botTokenField);
		//adding components to the root pane
		root.getChildren().addAll(welcomeLabel, botTokenVBox, connectBtn);
		GridPane.setConstraints(welcomeLabel, 0, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS, new Insets(0,0,10,0));
		GridPane.setConstraints(botTokenVBox, 0, 1, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(connectBtn, 0, 2, 1, 1, HPos.RIGHT, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		
		frame.setFrameResizable(false);
		frame.makeTitle("Connect to Discord Bot");
		frame.setMainNode(root);
		frame.setHeight(300); frame.setWidth(600);
		frame.addCSS("CSS/initCSS.css");
		
		//TESTING PURPOSES
		String dToken = "ODc0ODg4MjIwNjQ5ODY1Mjc2.YRNhJg.bwQJCma7xVZCw7URvL1t2Ny0zgQ";
		botTokenField.setText(dToken);
	}
	
	public void show() {
		frame.show();
	}
}
