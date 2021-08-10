package application;

import frame.EmptyFrame;
import javafx.application.Application;
import javafx.stage.Stage;

public class test extends Application {
	public void entryPoint(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		EmptyFrame e = new EmptyFrame();
		e.setHeight(1000);
		e.setWidth(1000);
		e.show();
	}
}
