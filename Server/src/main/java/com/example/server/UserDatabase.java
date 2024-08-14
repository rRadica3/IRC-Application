package com.example.server;

import com.example.lanproject.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;

public class UserDatabase extends Application {
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
    public void start(Stage primaryStage) throws Exception {
        Label title = new Label("User Information Form");
        title.setPrefWidth(850);
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(20, 0, 40, 0));

        Label lbUsername = new Label("Username");
        TextField tfUsername = new TextField();
        Label lbPassword = new Label("Password");
        PasswordField tfPassword = new PasswordField();
        Label lbAdmin = new Label("Administrator?");
        RadioButton rbYes = new RadioButton("yes");
        RadioButton rbNo = new RadioButton("no");
        ToggleGroup tgAdmin = new ToggleGroup();
        rbNo.setToggleGroup(tgAdmin);
        rbYes.setToggleGroup(tgAdmin);
        Button submit = new Button("Submit Record");
        Button view = new Button("View Table");
        Button adminView = new Button("View Admins");

        title.setFont(Font.font("Verdana", 28));
        lbUsername.setFont(Font.font("Verdana", 28));
        tfUsername.setFont(Font.font("Verdana", 28));
        lbPassword.setFont(Font.font("Verdana", 28));
        tfPassword.setFont(Font.font("Verdana", 28));
        lbAdmin.setFont(Font.font("Verdana", 28));
        rbNo.setFont(Font.font("Verdana", 28));
        rbYes.setFont(Font.font("Verdana", 28));
        submit.setFont(Font.font("Verdana", 28));
        view.setFont(Font.font("Verdana", 28));
        adminView.setFont(Font.font("Verdana", 28));

        GridPane gp = new GridPane();
        gp.add(lbUsername, 0, 0);
        gp.add(tfUsername, 1, 0, 2, 1);
        gp.add(lbPassword, 0, 1);
        gp.add(tfPassword, 1, 1, 3, 1);
        gp.add(lbAdmin, 0, 2);
        gp.add(rbYes, 1, 2);
        gp.add(rbNo, 2, 2);
        gp.add(submit, 0, 3);
        gp.add(view, 1, 3);
        gp.add(adminView, 2, 3);
        gp.setVgap(30);
        gp.setHgap(20);

        VBox mainLayout = new VBox(title, gp);
        mainLayout.setPadding(new Insets(20));

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

        submit.setOnAction(e -> {
            try (
                    Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                    PreparedStatement validateUsername = connect.prepareStatement("SELECT 1 FROM user_info WHERE userName = ?");
                    PreparedStatement addRecord = connect.prepareStatement("INSERT INTO user_info VALUES(?, ?, ?, ?, ?)");
            ) {
                System.out.println("Connected to database.");
                System.out.println("Prepared Statement created.");

                String usernameText = tfUsername.getText();
                String password = tfPassword.getText();
                boolean adminStatus = rbYes.isSelected();

                System.out.println("Data retrieved from controls.");

                validateUsername.setString(1, usernameText);
                ResultSet result = validateUsername.executeQuery();

                if(result.next()){
                    System.out.println("User already exists");
                }else{
                    addRecord.setString(1, usernameText);
                    addRecord.setString(2, password);
                    addRecord.setString(3, null);
                    addRecord.setBoolean(4, adminStatus);
                    addRecord.setBoolean(5, false);
                    addRecord.executeUpdate();
                    System.out.println("Added record to table");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Successful Operation");
                    alert.setHeaderText(null);
                    alert.setContentText("Record Added.");
                    alert.show();
                }

                tfUsername.setText("");
                tfPassword.setText("");
                rbYes.setSelected(false);

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        });

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

        adminView.setOnAction(e -> {
            table.getItems().clear();

            try (
                    Connection connect = DriverManager.getConnection("jdbc:derby:UserDatabase");
                    Statement state = connect.createStatement();
                    ResultSet result = state.executeQuery("SELECT * FROM user_info WHERE admin = true");
            ) {
                System.out.println("Connected to database.");
                System.out.println("Statement created.");
                System.out.println("ResultSet created.");

                while (result.next()) {
                    String userName = result.getString("userName");
                    String password = result.getString("password");
                    String token = result.getString("token");
                    boolean admin = result.getBoolean("admin");
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
                smallStage.setTitle("Administrators");
                smallStage.show();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        });

        primaryStage.setTitle("User Information Entry Form");
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
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

