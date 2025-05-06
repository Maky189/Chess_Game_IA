package com.marcos.chess;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Node;

public class Renderer_2D implements Renderer {
    private final int windowsWidth;
    private final int windowsHeight;
    private Canvas canvas;
    private DragHandler handler;
    private Pane pieceLayer;
    private Stage stage;
    private String profileName; // Add this field at the class level

    public Renderer_2D(int windowsWidth, int windowsHeight) {
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    public void setCurrentProfile(String name) {
        this.profileName = name;
    }

    private String getCurrentProfileName() {
        return this.profileName;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Scene createGameScene(int windowsWidth, int windowsHeight, boolean isMultiplayer) {
        int squareSize = 80;
        int size = 8;

        Game game =  MainGame.getGameInstance(size);

        Board board = new Board(squareSize, size, windowsWidth, windowsHeight);
        canvas = new Canvas(windowsWidth, windowsHeight);

        pieceLayer = new Pane();
        pieceLayer.setMouseTransparent(true);
        pieceLayer.setPrefSize(windowsWidth, windowsHeight);

        board.drawBoard(canvas.getGraphicsContext2D(), game.getBoard());

        handler = new DragHandler(board, game, canvas, pieceLayer, isMultiplayer);

        canvas.setOnMousePressed(handler::MousePressed);
        canvas.setOnMouseReleased(handler::MouseReleased);
        canvas.setOnMouseDragged(handler::MouseDragged);

        StackPane gameLayout = new StackPane();
        gameLayout.getChildren().addAll(canvas, pieceLayer);

        StackPane quitButton = createButton("Quit", Color.DARKRED, Color.DARKRED);
        
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(gameLayout, quitButton);

        AnchorPane.setTopAnchor(gameLayout, 0.0);
        AnchorPane.setLeftAnchor(gameLayout, 0.0);
        AnchorPane.setRightAnchor(gameLayout, 0.0);
        AnchorPane.setBottomAnchor(gameLayout, 0.0);

        AnchorPane.setTopAnchor(quitButton, 20.0);
        AnchorPane.setRightAnchor(quitButton, 20.0);

        quitButton.setOnMouseClicked(e -> {
            this.stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            showQuitDialog(game);
        });

        Scene scene = new Scene(root, windowsWidth, windowsHeight);

        // F3 to change modes 3D to 2D and vice versa
        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.F3) {
                cleanup();
                Render_3D render_3D = new Render_3D(windowsWidth, windowsHeight);
                render_3D.initialize();
                render_3D.createGameScene(windowsWidth, windowsHeight, isMultiplayer);
            }
        });

        return scene;
    }

    private StackPane createButton(String text, Color defaultColor, Color hoverColor) {
        Rectangle rectangle = new Rectangle(120, 40);
        rectangle.setFill(defaultColor);
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);

        Text buttonText = new Text(text);
        buttonText.setFill(Color.WHITE);
        buttonText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        StackPane button = new StackPane(rectangle, buttonText);
        button.setOnMouseEntered(e -> rectangle.setFill(hoverColor));
        button.setOnMouseExited(e -> rectangle.setFill(defaultColor));

        return button;
    }

    private void showQuitDialog(Game game) {
        if (stage == null) {
            Platform.exit();
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Quit Game");

        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: white;");

        Text message = new Text("Would you like to save the game before quitting?");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        StackPane yesButton = createButton("Yes", Color.GREEN, Color.DARKGREEN);
        StackPane noButton = createButton("No", Color.RED, Color.DARKRED);

        yesButton.setOnMouseClicked(e -> {
            if (profileName != null) {
                GameSaver.saveGame(profileName, game, "SinglePlayer");
            }
            dialog.close();
            stage.close();
            Platform.exit();
            System.exit(0);
        });

        noButton.setOnMouseClicked(e -> {
            if (profileName != null) {
                GameSaver.deleteProfile(profileName);
            }
            dialog.close();
            stage.close();
            Platform.exit();
            System.exit(0);
        });

        buttonBox.getChildren().addAll(yesButton, noButton);
        dialogVbox.getChildren().addAll(message, buttonBox);

        Scene dialogScene = new Scene(dialogVbox, 400, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showSaveGameDialog(Game game) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Save Game");

        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: white;");

        Text label = new Text("Enter save game name:");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TextField nameField = new TextField();
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-font-size: 14px;");

        StackPane saveButton = createButton("Save", Color.GREEN, Color.DARKGREEN);
        saveButton.setOnMouseClicked(e -> {
            String gameName = nameField.getText().trim();
            if (!gameName.isEmpty()) {
                GameSaver.saveGame(gameName, game, "SinglePlayer");
                dialog.close();
                Menu menu = new Menu(stage, windowsWidth, windowsHeight);
                menu.showMenu();
            }
        });

        dialogVbox.getChildren().addAll(label, nameField, saveButton);
        Scene dialogScene = new Scene(dialogVbox, 400, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void cleanup() {
        if (pieceLayer != null) {
            pieceLayer.getChildren().clear();
        }
    }
}