   package com.student.cafemaster;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // NEW
import javafx.scene.Parent; // NEW
import javafx.scene.Scene; // NEW
import javafx.stage.Stage; // NEW
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // NEW (if your button id is used)
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class FXMLController {

    @FXML private TextField user_field;
    @FXML private PasswordField pass_field;
    @FXML private Button login_btn; // Make sure your button in Scene Builder has fx:id="login_btn"

    @FXML
    public void loginAction() {
        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
        Connection connect = Database.connectDB();

        try {
            if (connect == null) {
                System.out.println("Connection failed! Check MySQL Workbench.");
                return;
            }

            PreparedStatement prepare = connect.prepareStatement(sql);
            prepare.setString(1, user_field.getText());
            prepare.setString(2, pass_field.getText());

            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                // 1. Success Message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Successfully Logged In!");
                alert.showAndWait();

                // 2. Hide Login Window
                user_field.getScene().getWindow().hide(); 

                // 3. Open Dashboard Window
                Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("CafeMaster Dashboard");
                stage.show();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Wrong Username or Password");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}