module com.student.cafemaster {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j; // Added to ensure Maven finds the driver
    requires java.base;

    // Opening to javafx.base allows the TableView to "see" your productData getters
    opens com.student.cafemaster to javafx.fxml, javafx.base;
    
    exports com.student.cafemaster;
}