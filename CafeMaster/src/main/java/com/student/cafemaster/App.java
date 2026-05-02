package com.student.cafemaster;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Change "dashboard.fxml" to "primary.fxml"
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
        
        // Adjust the size to something smaller for a login screen (e.g., 600x400)
        Scene scene = new Scene(fxmlLoader.load(), 600, 400); 
        
        stage.setTitle("CafeMaster - Login");
        
        // Optional: You can remove or lower the min-width/height for the login screen
        stage.setMinWidth(400);
        stage.setMinHeight(300);
        
        stage.setScene(scene);
        stage.centerOnScreen(); // Center the login box on the screen
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}