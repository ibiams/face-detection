package application;
	
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import view.CameraController;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		
		CameraController controller;
		
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			BorderPane root = (BorderPane)fxmlLoader.load(getClass().getResource("/view/Camera.fxml").openStream());
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
			controller = (CameraController)fxmlLoader.getController();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		          public void handle(WindowEvent we) {
		        	 controller.stopCamera();
		          }
		      });
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
