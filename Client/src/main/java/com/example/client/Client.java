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
    static final int PORT = 8000;
    static final String HOST = "localhost";
    protected Socket socket;
    private String username;
    private String token;
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
            // Create socket to connect to the server
            try {
                Socket socket = new Socket(HOST, PORT);
                objectToServer = new ObjectOutputStream(socket.getOutputStream());
                objectFromServer = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            latch.countDown(); // Signal that the socket and streams are ready
        }).start();

        logIn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    latch.await(); // Wait until socket and streams are initialized
                    Interaction logInUser = new Interaction(tfUsername.getText(), tfPassword.getText(), 1);

                    if(clientLogin(logInUser)){
                        Platform.runLater(() -> {
                            // Create scene
                            Scene mainScene = new Scene(mainPane, 450, 200);
                            primaryStage.setTitle("Client");
                            primaryStage.setScene(mainScene);
                        });
                    }else{
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Failed Operation");
                            alert.setHeaderText(null);
                            alert.setContentText("Incorrect Password/User already logged in");
                            alert.show();
                            tfUsername.setText("");
                            tfPassword.setText("");
                        });
                    }
                } catch (IOException | InterruptedException | ClassNotFoundException ex) {
                    // Handle exceptions (log or show to the user)
                }
            }).start();
        });

        createAccount.setOnAction(e -> {
            new Thread(() -> {
                try {
                    latch.await(); // Wait until socket and streams are initialized
                    Interaction newUser = new Interaction(tfUsername.getText(), tfPassword.getText(), 0);

                    if(clientLogin(newUser)){
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Successful Operation");
                            alert.setHeaderText(null);
                            alert.setContentText("User Added.");
                            alert.show();
                        });
                    }else{
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Failed Operation");
                            alert.setHeaderText(null);
                            alert.setContentText("User Already Exists.");
                            alert.show();
                        });
                    }
                    tfUsername.setText("");
                    tfPassword.setText("");
                } catch (IOException | InterruptedException | ClassNotFoundException ex) {
                    // Handle exceptions (log or show to the user)
                }
            }).start();
        });

        tf.setOnAction(e -> {
            new Thread(() -> {
                try {
                    latch.await(); // Wait until socket and streams are initialized
                    Interaction action = new Interaction(username, token, "Server", tf.getText(), 3);
                    objectToServer.writeObject(action);
                    objectToServer.flush();
                    tf.setText("");
                } catch (IOException | InterruptedException ex) {
                    // Handle exceptions (log or show to the user)
                }
            }).start();
        });

        primaryStage.setOnCloseRequest(event -> {
            new Thread(() -> {
                try {
                    latch.await(); // Wait until socket and streams are initialized
                    Interaction action = new Interaction(username, token, "Server", tf.getText(), 2);
                    objectToServer.writeObject(action);
                    objectToServer.flush();
                } catch (IOException | InterruptedException ex) {
                    // Handle exceptions (log or show to the user)
                }
            }).start();
        });

    }

    private boolean clientLogin(Interaction logInUser) throws IOException, ClassNotFoundException {
        objectToServer.writeObject(logInUser);
        objectToServer.flush();
        Interaction validatedUser = (Interaction) objectFromServer.readObject();
        if (validatedUser.getToken() != null) {
            username = validatedUser.getUsername();
            token = validatedUser.getToken();
            return true;
        }else if(validatedUser.isValid()){
            return true;
        }else{
            return false;
        }
    }

    public static void main (String[]args){
            launch(args);
    }
}

