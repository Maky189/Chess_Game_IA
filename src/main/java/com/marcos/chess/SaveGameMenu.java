package com.marcos.chess;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class SaveGameMenu {
    private final Stage stage;
    private final int windowsWidth;
    private final int windowsHeight;

    public SaveGameMenu(Stage stage, int windowsWidth, int windowsHeight) {
        this.stage = stage;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    public Scene createScene() {
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-image: url('/assets/board/menu.png'); -fx-background-size: cover;");

        MenuBackground background = new MenuBackground(windowsWidth, windowsHeight);
        root.getChildren().add(0, background);

        Text title = new Text("Saved Games");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        TableView<SaveGame> table = new TableView<>();
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.86);" + "-fx-table-cell-border-color: transparent;" + "-fx-font-size: 16px;");

        TableColumn<SaveGame, String> nameColumn = new TableColumn<>("Game Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(300);

        TableColumn<SaveGame, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(200);

        TableColumn<SaveGame, String> modeColumn = new TableColumn<>("Mode");
        modeColumn.setCellValueFactory(new PropertyValueFactory<>("mode"));
        modeColumn.setPrefWidth(150);

        String columnStyle = "" + "-fx-text-fill: black;" + "-fx-font-weight: bold;" + "-fx-font-size: 18px;" + "-fx-padding: 10px;";

        nameColumn.setStyle(columnStyle);
        dateColumn.setStyle(columnStyle);
        modeColumn.setStyle(columnStyle);

        table.getColumns().addAll(nameColumn, dateColumn, modeColumn);
        table.setFixedCellSize(50);

        // Add save games
        table.getItems().addAll(GameSaver.getSavedGames());

        StackPane newGameButton = createButton("New Game", Color.BLUE, Color.DARKBLUE);
        StackPane loadGameButton = createButton("Load Game", Color.GREEN, Color.DARKGREEN);
        StackPane deleteButton = createButton("Delete", Color.RED, Color.DARKRED);
        StackPane backButton = createButton("Back", Color.GRAY, Color.DARKGRAY);

        VBox buttonBox = new VBox(20);
        buttonBox.getChildren().addAll(newGameButton, loadGameButton, deleteButton, backButton);
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

        backButton.setOnMouseClicked(e -> {
            Menu menu = new Menu(stage, windowsWidth, windowsHeight);
            menu.showMenu();
        });

        newGameButton.setOnMouseClicked(e -> {
            showNewGameDialog();
        });

        loadGameButton.setOnMouseClicked(e -> {
            SaveGame selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Game game = GameSaver.loadGame(selected.getName());
                if (game != null) {
                    Renderer renderer = new Renderer_2D(windowsWidth, windowsHeight);
                    ((Renderer_2D)renderer).setStage(stage);  // Set stage
                    ((Renderer_2D)renderer).setCurrentProfile(selected.getName());  // Set profile name
                    renderer.initialize();
                    Scene gameScene = renderer.createGameScene(windowsWidth, windowsHeight, false);
                    stage.setScene(gameScene);
                    stage.setFullScreen(true);
                }
            }
        });

        deleteButton.setOnMouseClicked(e -> {
            SaveGame selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                GameSaver.deleteProfile(selected.getName());
                table.getItems().clear();
                table.getItems().addAll(GameSaver.getSavedGames());
            }
        });

        Scene scene = new Scene(root, windowsWidth, windowsHeight);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);

        stage.setOnCloseRequest(e -> background.cleanup());

        return scene;
    }

    private StackPane createButton(String text, Color defaultColor, Color hoverColor) {
        Rectangle rectangle = new Rectangle(250, 80);
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

    private void showNewGameDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("New Game");

        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: white;");

        Text label = new Text("Enter save game name:");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TextField nameField = new TextField();
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-font-size: 14px;");

        StackPane confirmButton = createButton("Start Game", Color.GREEN, Color.DARKGREEN);
        confirmButton.setOnMouseClicked(e -> {
            String gameName = nameField.getText().trim();
            if (!gameName.isEmpty()) {
                dialog.close();
                startGame(gameName);
            }
        });

        dialogVbox.getChildren().addAll(label, nameField, confirmButton);
        Scene dialogScene = new Scene(dialogVbox, 400, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void startGame(String gameName) {
        MainGame.resetGameInstance(8);
        Game game = MainGame.getGameInstance(8);
        boolean playAsWhite = GameOptions.getInstance().isPlayingAsWhite();
        
        // If playing as black, let AI make first move as white (player = 1)
        if (!playAsWhite) {
            IA ia = new IA();
            // Change this line: use player = 1 for white
            IA.Move aiMove = ia.makeMove(game, 1);  // AI plays as white
            if (aiMove != null) {
                game.getBoard()[aiMove.toX][aiMove.toY] = game.getBoard()[aiMove.fromX][aiMove.fromY];
                game.getBoard()[aiMove.fromX][aiMove.fromY] = 0;
                game.updateLastMove(aiMove.fromX, aiMove.fromY, aiMove.toX, aiMove.toY);
                game.switchPlayer();  // Switch to black (player's turn)
            }
        }
        
        GameSaver.saveGame(gameName, game, "SinglePlayer");
        
        // Use appropriate renderer based on 3D mode
        Renderer renderer = Menu.is3DModeEnabled() ? 
            new Render_3D(windowsWidth, windowsHeight) : 
            new Renderer_2D(windowsWidth, windowsHeight);
        
        if (renderer instanceof Renderer_2D) {
            ((Renderer_2D)renderer).setStage(stage);
            ((Renderer_2D)renderer).setCurrentProfile(gameName);
        }
        
        renderer.initialize();
        
        if (Menu.is3DModeEnabled()) {
            stage.hide();
            renderer.createGameScene(windowsWidth, windowsHeight, false);
        } else {
            Scene gameScene = renderer.createGameScene(windowsWidth, windowsHeight, false);
            stage.setScene(gameScene);
            stage.setFullScreen(true);
        }
    }
}