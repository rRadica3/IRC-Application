package com.example.server;

import com.example.lanproject.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.example.server.Server.encoder;
import static com.example.server.Server.random;

public class ServerActionHandler extends Thread{
    protected Socket socket;
    ObjectInputStream objectFromClient;
    ObjectOutputStream objectToClient;

    public ServerActionHandler(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public static String generateToken() {
        byte[] token = new byte[16];
        random.nextBytes(token);
        return encoder.encodeToString(token);
    }

    public void run() {
        try {
            objectFromClient = new ObjectInputStream(socket.getInputStream());
            objectToClient = new ObjectOutputStream(socket.getOutputStream());
            boolean login = false;
            while(!login) {
                User logInUser = (User) objectFromClient.readObject();
                try (
                        Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                        PreparedStatement tokenSet = connect.prepareStatement("UPDATE user_info SET token = ?, online = true WHERE userName = ? AND password = ?");
                ) {
                    String token = generateToken();
                    tokenSet.setString(1, token);
                    tokenSet.setString(2, logInUser.getUsername());
                    tokenSet.setString(3, logInUser.getPassword());
                    int result = tokenSet.executeUpdate();
                    User returnUser = new User();
                    if (result == 1) {
                        returnUser.setUsername(logInUser.getUsername());
                        returnUser.setToken(token);
                        objectToClient.writeObject(returnUser);
                        try {
                            // Perform some action and send the result back to the server
                            Server.handlerQueue.put(logInUser.getUsername() + " logged in ");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        login = true;
                    } else {
                        returnUser.setToken(null);
                        objectToClient.writeObject(returnUser);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }

            while(login){
                ClientAction action = (ClientAction)objectFromClient.readObject();
                switch(action.getAction()){
                    case 0:
                        try (
                                Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                                PreparedStatement logoff = connect.prepareStatement("UPDATE user_info SET token = null, online = false WHERE userName = ? AND token = ?");
                        ) {
                            logoff.setString(1, action.getUsername());
                            logoff.setString(2, action.getSessionToken());
                            logoff.executeUpdate();
                            try {
                                // Perform some action and send the result back to the server
                                Server.handlerQueue.put(action.getUsername() + " logged off ");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } catch (SQLException ex) {
                            System.out.println(ex.getMessage());
                        }
                        login = false;
                        break;
                    case 1:
                        try {
                            // Perform some action and send the result back to the server
                            Server.handlerQueue.put(action.getUsername() + ": " + action.getPayload() + " ");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        break;
                }
            }
        }catch (IOException ex){
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
