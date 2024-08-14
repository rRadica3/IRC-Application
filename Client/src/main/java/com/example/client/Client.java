package com.example.client;

import com.example.lanproject.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class Client extends Application {
    private String username = "";
    private String token = "";
    private ObjectOutputStream objectToServer;
    private ObjectInputStream objectFromServer;
    private CountDownLatch latch = new CountDownLatch(1); // CountDownLatch for synchronization

    @Override
    public void start(Stage primaryStage) {
        // Panel p to hold the label and text field
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(5));
        pane.setStyle("-fx-border-color: green");

        TextField tf = new TextField();
        tf.setPromptText("Enter Message");
        tf.setAlignment(Pos.BOTTOM_RIGHT);
        tf.setFont(Font.font("Arial", 30));
        pane.setCenter(tf);

        BorderPane mainPane = new BorderPane();
        TextArea ta = new TextArea();
        ta.setFont(Font.font("Arial", 30));
        mainPane.setCenter(ta);
        mainPane.setTop(pane);

        Label title = new Label("Log in");
        title.setPrefWidth(850);
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(20, 0, 40, 0));

        Label lbUsername = new Label("Username");
        TextField tfUsername = new TextField();
        Label lbPassword = new Label("Password");
        PasswordField tfPassword = new PasswordField();
        Button logIn = new Button("Log In");
        Button createAccount = new Button("Create Account");

        title.setFont(Font.font("Verdana", 28));
        lbUsername.setFont(Font.font("Verdana", 28));
        tfUsername.setFont(Font.font("Verdana", 28));
        lbPassword.setFont(Font.font("Verdana", 28));
        tfPassword.setFont(Font.font("Verdana", 28));
        logIn.setFont(Font.font("Verdana", 28));
        createAccount.setFont(Font.font("Verdana", 28));

        GridPane gp = new GridPane();
        gp.add(lbUsername, 0, 0);
        gp.add(tfUsername, 1, 0, 2, 1);
        gp.add(lbPassword, 0, 1);
        gp.add(tfPassword, 1, 1, 3, 1);
        gp.add(logIn, 0, 3);
        gp.add(createAccount, 1, 3);
        gp.setVgap(30);
        gp.setHgap(20);

        VBox mainLayout = new VBox(title, gp);
        mainLayout.setPadding(new Insets(20));

        primaryStage.setTitle("LAN IRC APP - Log in");
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Create a background thread for socket connection
        new Thread(() -> {
            try {
                // Create socket to connect to the server
                Socket socket = new Socket("localhost", 8000);
                objectToServer = new ObjectOutputStream(socket.getOutputStream());
                objectFromServer = new ObjectInputStream(socket.getInputStream());
                latch.countDown(); // Signal that the socket and streams are ready
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    // Handle connection error on the UI thread
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Connection Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Could not connect to the server. Please try again later.");
                    alert.showAndWait();
                });
            }
        }).start();

        logIn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    latch.await(); // Wait until socket and streams are initialized
                    User logInUser = new User(tfUsername.getText(), tfPassword.getText());
                    objectToServer.writeObject(logInUser);
                    objectToServer.flush();
                    User validatedUser = (User) objectFromServer.readObject();
                    if (validatedUser.getToken() != null) {
                        username = validatedUser.getUsername();
                        token = validatedUser.getToken();
                        Platform.runLater(() -> {
                            try {
                                // Create scene
                                Scene mainScene = new Scene(mainPane, 450, 200);
                                primaryStage.setTitle("Client");
                                primaryStage.setScene(mainScene);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                } catch (IOException | InterruptedException | ClassNotFoundException ex) {
                    // Handle exceptions (log or show to the user)
                }
            }).start();
        });

        new Thread(() -> {
            try {
                latch.await(); // Wait until socket and streams are initialized

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        tf.setOnAction(e -> {
            new Thread(() -> {
                try {
                    latch.await(); // Wait until socket and streams are initialized
                    ClientAction action = new ClientAction(username, token, 1, "Server", tf.getText());
                    objectToServer.writeObject(action);
                    objectToServer.flush();
                } catch (IOException | InterruptedException ex) {
                    // Handle exceptions (log or show to the user)
                }
            }).start();
        });

        primaryStage.setOnCloseRequest(event -> {
            new Thread(() -> {
                try {
                    latch.await(); // Wait until socket and streams are initialized
                    ClientAction action = new ClientAction(username, token, 0);
                    objectToServer.writeObject(action);
                    objectToServer.flush();
                } catch (IOException | InterruptedException ex) {
                    // Handle exceptions (log or show to the user)
                }
            }).start();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}