package com.example.server;

import com.example.lanproject.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;
import javafx.application.Platform;
import javafx.scene.Scene;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Date;
import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Server extends Application {
    static final int PORT = 8000;
    static final SecureRandom random = new SecureRandom();
    static final Base64.Encoder encoder = Base64.getUrlEncoder();
    static BlockingQueue<String> handlerQueue = new LinkedBlockingQueue<>();
    private final TableView<Interaction> table = new TableView<>();
    private final ObservableList<Interaction> userData = FXCollections.observableArrayList();

    @Override
    public void init() throws Exception {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase; create = true");
                Statement state = connect.createStatement();
        ) {
            System.out.println("Connected to database.");
            System.out.println("Statement created.");
            DatabaseMetaData dbm = connect.getMetaData();
            System.out.println("MetaData created.");
            ResultSet result = dbm.getTables(null, null, "USER_INFO", null);
            System.out.println("ResultSet created.");
            if (result.next()) {
                System.out.println("user_info exists");
            } else {
                state.execute("create table user_info(userName varchar(100), password varchar(100), token varchar(100), admin boolean, online boolean)");
                System.out.println("user_info created");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Label title = new Label("Server Title");
        TextArea ta = new TextArea();
        ta.setFont(Font.font("Arial", 30));
        Button view = new Button("View Table");
        view.setFont(Font.font("Verdana", 28));

        GridPane gp = new GridPane();
        gp.add(ta, 0, 0);
        gp.add(view, 0, 1);
        gp.setVgap(30);
        gp.setHgap(20);

        VBox mainLayout = new VBox(title, gp);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 450, 200);
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        TableColumn<Interaction, String> usernamecol = new TableColumn<>("Username");
        TableColumn<Interaction, String> passwordcol = new TableColumn<>("Password");
        TableColumn<Interaction, String> tokencol = new TableColumn<>("Token");
        TableColumn<Interaction, Boolean> admincol = new TableColumn<>("Administrator");
        TableColumn<Interaction, Boolean> onlinecol = new TableColumn<>("Online");

        table.getColumns().add(usernamecol);
        table.getColumns().add(passwordcol);
        table.getColumns().add(tokencol);
        table.getColumns().add(admincol);
        table.getColumns().add(onlinecol);

        table.setStyle("-fx-font-size: 30px; -fx-pref-width: 750");
        usernamecol.setStyle("-fx-font-size: 20px; -fx-pref-width: 250px");
        passwordcol.setStyle("-fx-font-size: 20px; -fx-pref-width: 250px");
        tokencol.setStyle("-fx-font-size: 20px; -fx-pref-width: 250px");
        admincol.setStyle("-fx-font-size: 20px; -fx-pref-width: 250px");
        onlinecol.setStyle("-fx-font-size: 20px; -fx-pref-width: 250px");

        Scene viewScene = new Scene(table);

        new Thread(()->{
            Platform.runLater(()-> ta.appendText("SERVER START: [" + new Date() + "]\n" +
                    "----------------------------------------------------------------------\n\n"));
            ServerSocket serverSocket = null;
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(PORT);
            } catch (IOException e) {
                e.printStackTrace();

            }
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    System.out.println("I/O error: " + e);
                }
                // new thread for a client
                new ServerActionHandler(socket).start();
            }

        }).start();

        new Thread(()->{
            while(true){
                try {
                    // Process messages from the action handler
                    String handlerIn = handlerQueue.take();
                    Platform.runLater(()-> ta.appendText(handlerIn +  "\t\t[" + new Date() + "]\n"));
                    // Further server processing here
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        view.setOnAction(e -> {
            table.getItems().clear();

            try (
                    Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                    Statement state = connect.createStatement();
                    ResultSet result = state.executeQuery("SELECT * FROM user_info");
            ) {
                System.out.println("Connected to database.");
                System.out.println("Statement created.");
                System.out.println("ResultSet created.");

                while (result.next()) {
                    String userName = result.getString("userName");
                    String password = result.getString("password");
                    boolean admin = result.getBoolean("admin");
                    String token = result.getString("token");
                    boolean online = result.getBoolean("online");

                    Interaction currentUser = new Interaction(userName, password, token, admin, online);
                    userData.add(currentUser);

                    usernamecol.setCellValueFactory(new PropertyValueFactory<>("username"));
                    passwordcol.setCellValueFactory(new PropertyValueFactory<>("password"));
                    tokencol.setCellValueFactory(new PropertyValueFactory<>("token"));
                    admincol.setCellValueFactory(new PropertyValueFactory<>("admin"));
                    onlinecol.setCellValueFactory(new PropertyValueFactory<>("online"));

                    table.setItems(userData);
                }

                System.out.println("Table populated.");
                Stage smallStage = new Stage();
                smallStage.setScene(viewScene);
                System.out.println("Scene set to stage.");
                smallStage.setTitle("All Records");
                smallStage.show();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }

    public void stop() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
