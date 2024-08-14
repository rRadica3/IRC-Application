module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.example.lanproject;


    opens com.example.server to javafx.fxml;
    exports com.example.server;
}