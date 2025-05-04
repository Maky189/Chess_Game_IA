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
import jme3tools.savegame.SaveGame;
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
        root.setStyle("-fx-background-image: url('/assets/board/cover.png'); -fx-background-size: cover;");

        // Create title
        Text title = new Text("Saved Games");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        // Create table with custom styling
        TableView<SaveGame> table = new TableView<>();
        table.setStyle("-fx-background-color: rgba(255,255,255,0.85);" +
                       "-fx-table-cell-border-color: transparent;" +
                       "-fx-font-size: 16px;");

        // Configure columns with better styling
        TableColumn<SaveGame, String> nameColumn = new TableColumn<>("Game Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(300);

        TableColumn<SaveGame, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(200);

        TableColumn<SaveGame, String> modeColumn = new TableColumn<>("Mode");
        modeColumn.setCellValueFactory(new PropertyValueFactory<>("mode"));
        modeColumn.setPrefWidth(150);

        // Style the column headers
        String columnStyle = "-fx-background-color: #2E7D32;" + // Changed to a nice green color
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 18px;" +
                            "-fx-padding: 10px;";

        nameColumn.setStyle(columnStyle);
        dateColumn.setStyle(columnStyle);
        modeColumn.setStyle(columnStyle);

        table.getColumns().addAll(nameColumn, dateColumn, modeColumn);
        table.setFixedCellSize(50); // Larger rows

        // Create buttons with matching style from Menu class
        StackPane newGameButton = createButton("New Game", Color.BLUE, Color.DARKBLUE);
        StackPane loadGameButton = createButton("Load Game", Color.GREEN, Color.DARKGREEN);
        StackPane backButton = createButton("Back", Color.GRAY, Color.DARKGRAY);

        // Create VBox for buttons
        VBox buttonBox = new VBox(20);
        buttonBox.getChildren().addAll(newGameButton, loadGameButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout setup
        AnchorPane.setTopAnchor(title, 50.0);
        AnchorPane.setLeftAnchor(title, 50.0);

        AnchorPane.setTopAnchor(table, 150.0);
        AnchorPane.setLeftAnchor(table, 50.0);
        AnchorPane.setRightAnchor(table, 350.0);
        AnchorPane.setBottomAnchor(table, 50.0);

        AnchorPane.setTopAnchor(buttonBox, 150.0);
        AnchorPane.setRightAnchor(buttonBox, 50.0);

        root.getChildren().addAll(title, table, buttonBox);

        // Create scene with the original window dimensions
        Scene scene = new Scene(root, windowsWidth, windowsHeight);
        
        // Set the scene but maintain the current window size
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
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
}
