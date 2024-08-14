package com.example.server;

import com.example.lanproject.*;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
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
                state.execute("create table user_info(userName varchar(100), password varchar(100), admin boolean, token varchar(100), online boolean)");
                System.out.println("user_info created");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        TextArea ta = new TextArea();
        ta.setFont(Font.font("Arial", 30));
        Scene scene = new Scene(ta, 450, 200);
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();

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
