package com.example.server;

import com.example.lanproject.*;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;

import static com.example.server.Server.encoder;
import static com.example.server.Server.random;

public class ServerActionHandler extends Thread{
    protected Socket socket;
    ObjectInputStream objectFromClient;
    ObjectOutputStream objectToClient;

    public ServerActionHandler(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        try {
            objectFromClient = new ObjectInputStream(socket.getInputStream());
            objectToClient = new ObjectOutputStream(socket.getOutputStream());
            login();
            active();

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateToken() {
        byte[] token = new byte[16];
        random.nextBytes(token);
        return encoder.encodeToString(token);
    }

    private void active() throws IOException, ClassNotFoundException {
        while(true){
            Interaction action = (Interaction) objectFromClient.readObject();
            if(validateToken(action.getToken())){
                switch(action.getAction()){
                    case 2:
                        logOutProtocol(action.getUsername());
                        return;
                    case 3:
                        try {
                            // Perform some action and send the result back to the server
                            Server.handlerQueue.put(action.getUsername() + ": " + action.getPayload() + " ");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        break;
                }
            }
        }
    }

    private void login() throws IOException, ClassNotFoundException {
        while(true) {
            Interaction logInUser = (Interaction) objectFromClient.readObject();
            switch(logInUser.getAction()){
                case 0:
                    if(searchForUserName(logInUser.getUsername())){
                        Interaction response = new Interaction(null, false, false);
                        objectToClient.writeObject(response);
                    }else{
                        addUser(logInUser.getUsername(), logInUser.getPassword());
                        Interaction response = new Interaction(null, false, true);
                        objectToClient.writeObject(response);
                    }
                    break;
                case 1:
                    if(searchForUser(logInUser.getUsername(), logInUser.getPassword()) && !checkIfOnline(logInUser.getUsername())){
                        Interaction response = (Interaction) loginProtocol(logInUser.getUsername(), logInUser.getPassword());
                        objectToClient.writeObject(response);
                        return;
                    }else{
                        Interaction response = new Interaction(null, false, false);
                        objectToClient.writeObject(response);
                    }
                    break;
            }
        }
    }

    private void addUser(String username, String password) {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                PreparedStatement addRecord = connect.prepareStatement("INSERT INTO user_info VALUES(?, ?, ?, ?, ?)");
        ) {
            addRecord.setString(1, username);
            addRecord.setString(2, password);
            addRecord.setString(3, null);
            addRecord.setBoolean(4, false);
            addRecord.setBoolean(5, false);
            addRecord.executeUpdate();
            System.out.println("Added record to table");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private Object loginProtocol(String username, String password) {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                PreparedStatement tokenSet = connect.prepareStatement("UPDATE user_info SET token = ?, online = true WHERE userName = ? AND password = ?");
        ) {
            String token = generateToken();

            tokenSet.setString(1, token);
            tokenSet.setString(2, username);
            tokenSet.setString(3, password);

            tokenSet.executeUpdate();

            Interaction returnUser = new Interaction();
            returnUser.setUsername(username);
            returnUser.setToken(token);

            try {
                // Perform some action and send the result back to the server
                Server.handlerQueue.put(username + " logged in ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return returnUser;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private void logOutProtocol(String username) {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                PreparedStatement logOut = connect.prepareStatement("UPDATE user_info SET token = null, online = false WHERE userName = ?");
        ) {
            logOut.setString(1, username);
            logOut.executeUpdate();
            try {
                // Perform some action and send the result back to the server
                Server.handlerQueue.put(username + " logged out ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private boolean searchForUser(String username, String password) {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                PreparedStatement validateUser = connect.prepareStatement("SELECT 1 FROM user_info WHERE userName = ? AND password = ?");
        ) {
            validateUser.setString(1, username);
            validateUser.setString(2, password);

            ResultSet result = validateUser.executeQuery();
            if(result.next()){
                return true;
            }else{
                return false;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    private boolean searchForUserName(String username) {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                PreparedStatement validateUsername = connect.prepareStatement("SELECT 1 FROM user_info WHERE userName = ?");

        ) {
            validateUsername.setString(1, username);
            ResultSet result = validateUsername.executeQuery();

            if(result.next()){
                return true;
            }else{
               return false;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    private boolean validateToken(String token) {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                PreparedStatement validateToken = connect.prepareStatement("SELECT 1 FROM user_info WHERE token = ?");
        ) {
            validateToken.setString(1, token);

            ResultSet result = validateToken.executeQuery();
            if(result.next()){
                return true;
            }else{
                return false;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    private boolean checkIfOnline(String username) {
        try (
                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                PreparedStatement validateOnline = connect.prepareStatement("SELECT 1 FROM user_info WHERE userName = ? AND online = true");
        ) {
            validateOnline.setString(1, username);

            ResultSet result = validateOnline.executeQuery();

            if(result.next()){
                return true;
            }else{
                return false;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }
}
