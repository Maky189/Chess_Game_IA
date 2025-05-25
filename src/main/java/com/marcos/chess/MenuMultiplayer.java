package com.marcos.chess;

import com.marcos.chess.networking.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.net.ServerSocket;
import java.util.List;
import java.io.IOException;

public class MenuMultiplayer {
    private final Stage stage;
    private final int windowsWidth;
    private final int windowsHeight;
    private static final int DEFAULT_PORT = 5000;
    private TableView<Multiplayer> table;
    private ServerSocket serverSocket;
    private Timeline refreshTimer;  // Add this field

    public MenuMultiplayer(Stage stage, int windowsWidth, int windowsHeight) {
        this.stage = stage;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    public Scene createScene() {
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-image: url('/assets/board/menu.png'); -fx-background-size: cover;");

        MenuBackground background = new MenuBackground(windowsWidth, windowsHeight);
        root.getChildren().add(0, background);

        Text title = new Text("Network Games");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        table = new TableView<>();
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.86);" + 
                      "-fx-table-cell-border-color: transparent;" + 
                      "-fx-font-size: 16px;");

        TableColumn<Multiplayer, String> hostColumn = new TableColumn<>("Host");
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("hostName"));
        hostColumn.setPrefWidth(300);

        TableColumn<Multiplayer, String> gameColumn = new TableColumn<>("Game Name");
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        gameColumn.setPrefWidth(200);

        TableColumn<Multiplayer, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(150);

        String columnStyle = "-fx-background-color: #2E7D32;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;" + "-fx-font-size: 18px;" + "-fx-padding: 10px;";

        hostColumn.setStyle(columnStyle);
        gameColumn.setStyle(columnStyle);
        statusColumn.setStyle(columnStyle);

        table.getColumns().addAll(hostColumn, gameColumn, statusColumn);
        table.setFixedCellSize(50);

        
        StackPane hostButton = createButton("Host Game", Color.BLUE, Color.DARKBLUE);
        StackPane joinButton = createButton("Join Game", Color.GREEN, Color.DARKGREEN);
        StackPane refreshButton = createButton("Refresh", Color.ORANGE, Color.DARKORANGE);
        StackPane debugButton = createButton("Debug PvP", Color.PURPLE, Color.DARKVIOLET);
        StackPane backButton = createButton("Back", Color.GRAY, Color.DARKGRAY);

        VBox buttonBox = new VBox(20);
        buttonBox.getChildren().addAll(hostButton, joinButton, refreshButton, debugButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(title, 50.0);
        AnchorPane.setLeftAnchor(title, 50.0);
        AnchorPane.setTopAnchor(table, 150.0);
        AnchorPane.setLeftAnchor(table, 50.0);
        AnchorPane.setRightAnchor(table, 350.0);
        AnchorPane.setBottomAnchor(table, 50.0);
        AnchorPane.setTopAnchor(buttonBox, 150.0);
        AnchorPane.setRightAnchor(buttonBox, 50.0);

        root.getChildren().addAll(title, table, buttonBox);

        // buttonns 
        backButton.setOnMouseClicked(e -> {
            Menu menu = new Menu(stage, windowsWidth, windowsHeight);
            menu.showMenu();
        });

        debugButton.setOnMouseClicked(e -> {
            MainGame.resetGameInstance(8);
            Game game = MainGame.getGameInstance(8);
            Renderer renderer = new Renderer_2D(windowsWidth, windowsHeight);
            ((Renderer_2D)renderer).setStage(stage);
            renderer.initialize();
            Scene gameScene = renderer.createGameScene(windowsWidth, windowsHeight, true);
            stage.setScene(gameScene);
            stage.setFullScreen(true);
        });

        hostButton.setOnMouseClicked(e -> showHostGameDialog());
        joinButton.setOnMouseClicked(e -> {
            Multiplayer selectedGame = table.getSelectionModel().getSelectedItem();
            if (selectedGame != null) {
                joinSelectedGame(selectedGame);
            } else {
                showErrorDialog("No Game Selected", "Please select a game from the list to join.");
            }
        });
        refreshButton.setOnMouseClicked(e -> refreshGamesList());

        refreshTimer = new Timeline(
            new KeyFrame(Duration.seconds(0.5), e -> refreshGamesList())
        );
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();

        // Stop
        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != stage.getScene()) {
                refreshTimer.stop();
            }
        });

        refreshGamesList();

        stage.setOnCloseRequest(e -> background.cleanup());

        return new Scene(root, windowsWidth, windowsHeight);
    }

    private StackPane createButton(String text, Color defaultColor, Color hoverColor) {
        javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(250, 80);
        rectangle.setFill(defaultColor);
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);

        Text buttonText = new Text(text);
        buttonText.setFill(Color.WHITE);
        buttonText.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        StackPane button = new StackPane(rectangle, buttonText);
        button.setOnMouseEntered(e -> rectangle.setFill(hoverColor));
        button.setOnMouseExited(e -> rectangle.setFill(defaultColor));

        return button;
    }

    private void showHostGameDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Host Game");

        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: white;");

        Text label = new Text("Game Name:");
        TextField nameField = new TextField();
        nameField.setMaxWidth(300);

        StackPane hostButton = createButton("Start Hosting", Color.GREEN, Color.DARKGREEN);
        hostButton.setOnMouseClicked(e -> {
            String gameName = nameField.getText().trim();
            if (!gameName.isEmpty()) {
                startHosting(gameName);
                dialog.close();
            }
        });

        dialogVbox.getChildren().addAll(label, nameField, hostButton);
        Scene dialogScene = new Scene(dialogVbox, 400, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showJoinGameDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Join Game");

        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: white;");

        Text ipLabel = new Text("Host IP:");
        TextField ipField = new TextField("localhost");
        ipField.setMaxWidth(300);

        StackPane joinButton = createButton("Join Game", Color.GREEN, Color.DARKGREEN);
        joinButton.setOnMouseClicked(e -> {
            Multiplayer selectedGame = table.getSelectionModel().getSelectedItem();
            if (selectedGame != null) {
                joinSelectedGame(selectedGame);
            } else {
                showErrorDialog("No Game Selected", "Please select a game from the table to join.");
            }
            dialog.close();
        });

        dialogVbox.getChildren().addAll(ipLabel, ipField, joinButton);
        Scene dialogScene = new Scene(dialogVbox, 400, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void refreshGamesList() {
        List<Multiplayer> games = OptionsMultiplayer.getActiveGames();
        table.getItems().clear();
        table.getItems().addAll(games);
    }

    private void startHosting(String gameName) {
        GameSession gameSession = new GameSession(true);
        gameSession.setGameName(gameName);
        ChessServer server = new ChessServer(DEFAULT_PORT, gameSession);
        
        try {
            if (server.start()) {
                String hostIP = "localhost"; //just localhost for testing

                OptionsMultiplayer.addGame(
                    System.getProperty("user.name"),
                    gameName,
                    "Waiting for player",
                    hostIP,
                    DEFAULT_PORT
                );
                
                refreshGamesList();

                startMultiplayerGame(gameSession, true);
            } else {
                return;
            }
        } catch (Exception e) {
            OptionsMultiplayer.removeGame(gameName);
            return;
        }
    }

    private void joinSelectedGame(Multiplayer game) {
        if (game == null) {
            return;
        }

        GameSession gameSession = new GameSession(false);
        ChessClient client = new ChessClient(game.getHostIP(), game.getPort(), gameSession);

        if (!client.connect()) {
            OptionsMultiplayer.removeGame(game.getGameName());
            refreshGamesList();
            return;
        }

        OptionsMultiplayer.updateGameStatus(game.getGameName(), "Game in progress");
        refreshGamesList();
        startMultiplayerGame(gameSession, false);
    }

    private void startMultiplayerGame(GameSession gameSession, boolean isHost) {
        MainGame.resetGameInstance(8);
        Game game = MainGame.getGameInstance(8);
        gameSession.setGame(game);
        
        Renderer renderer = Menu.is3DModeEnabled() ? 
            new Render_3D(windowsWidth, windowsHeight) : 
            new Renderer_2D(windowsWidth, windowsHeight);
        
        if (renderer instanceof Renderer_2D) {
            ((Renderer_2D)renderer).setStage(stage);
            ((Renderer_2D)renderer).setGameSession(gameSession);
        }
        
        renderer.initialize();
        
        if (Menu.is3DModeEnabled()) {
            stage.hide();
            renderer.createGameScene(windowsWidth, windowsHeight, true);
        } else {
            Scene scene = renderer.createGameScene(windowsWidth, windowsHeight, true);
            stage.setScene(scene);
            stage.setFullScreen(true);

            stage.setOnCloseRequest(e -> {
                if (isHost) {
                    OptionsMultiplayer.removeGame(gameSession.getGameName());
                }
            });
        }
    }

    private void showErrorDialog(String title, String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle(title);

        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: white;");

        Text errorText = new Text(message);
        errorText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        StackPane okButton = createButton("OK", Color.RED, Color.DARKRED);
        okButton.setOnMouseClicked(e -> dialog.close());

        dialogVbox.getChildren().addAll(errorText, okButton);
        Scene dialogScene = new Scene(dialogVbox, 400, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}