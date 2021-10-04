package view;

import frame.EmptyFrame;
import javafx.application.Platform;
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
	Label jdaLabel = new Label("JDA: ");
	Label dbLabel = new Label("DB: ");
	Label jdaStatusLabel = new Label("Offline");
	Label dbStatusLabel = new Label("Offline");
	public TextField botTokenField = new TextField();
	public Button jdaConnectBtn = new Button("Connect to JDA");
	public Button dbConnectBtn = new Button("Connect to DB");
	
	public InitGUI() { 
		//setting style classes
		root.getStyleClass().add("root-grid");
		welcomeLabel.setId("welcome-label");
		botTokenLabel.setId("bot-token-label");
		botTokenField.setId("bot-token-field");
		jdaConnectBtn.getStyleClass().add("connect-button");
		jdaStatusLabel.getStyleClass().add("status-label");
		dbConnectBtn.getStyleClass().add("connect-button");
		dbStatusLabel.getStyleClass().add("status-label");
		showJdaOffline(); showDbOffline();
		
		botTokenVBox.getChildren().addAll(botTokenLabel, botTokenField);
		//adding components to the root pane
		root.getChildren().addAll(welcomeLabel, botTokenVBox, jdaConnectBtn, jdaStatusLabel, dbStatusLabel, dbLabel, jdaLabel);
		GridPane.setConstraints(welcomeLabel, 0, 0, 3, 1, HPos.CENTER, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS, new Insets(0,0,10,0));
		GridPane.setConstraints(botTokenVBox, 0, 1, 3, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(jdaConnectBtn, 2, 3, 1, 1, HPos.RIGHT, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
		GridPane.setConstraints(jdaStatusLabel, 1, 3, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
		GridPane.setConstraints(jdaLabel, 0, 3, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
		GridPane.setConstraints(dbStatusLabel, 1, 2, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
		GridPane.setConstraints(dbLabel, 0, 2, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
		
		frame.setFrameResizable(false);
		frame.makeTitle("To Do Disc");
		frame.setMainNode(root);
		frame.setHeight(350); frame.setWidth(600);
		frame.addCSS("CSS/initCSS.css");
	}
	
	public void show() {
		frame.show();
	}
	
	public void disableConnect() { 
		jdaConnectBtn.setDisable(true);
	}
	
	public void enableConnect() { 
		jdaConnectBtn.setDisable(false);
	}
	
	public void showDbOnline() { 
		Platform.runLater(()->{
			dbStatusLabel.setText("Connected");
			dbStatusLabel.setStyle("-fx-text-fill: RGB(100, 255, 100)");
		});
	}
	
	public void showDbOffline() { 
		Platform.runLater(()->{
			dbStatusLabel.setText("Disconnected");
			dbStatusLabel.setStyle("-fx-text-fill: RGB(255, 100, 100)");
		});
	}
	
	public void showJdaOnline() { 
		Platform.runLater(()->{
			jdaStatusLabel.setText("Online");
			jdaStatusLabel.setStyle("-fx-text-fill: RGB(100, 255, 100)");
		});
	}
	
	public void showJdaOffline() { 
		Platform.runLater(()->{
			jdaStatusLabel.setText("Offline");
			jdaStatusLabel.setStyle("-fx-text-fill: RGB(255, 100, 100)");
		});
	}
}
