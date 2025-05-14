package com.marcos.chess;

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

public class MenuMultiplayer {
    private final Stage stage;
    private final int windowsWidth;
    private final int windowsHeight;

    public MenuMultiplayer(Stage stage, int windowsWidth, int windowsHeight) {
        this.stage = stage;
        this.windowsWidth = windowsWidth;
        this.windowsHeight = windowsHeight;
    }

    public Scene createScene() {
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-image: url('/assets/board/menu.png'); -fx-background-size: cover;");

        Text title = new Text("Network Games");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        TableView<Multiplayer> table = new TableView<>();
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

        String columnStyle = "-fx-background-color: #2E7D32;" +
                           "-fx-text-fill: white;" + 
                           "-fx-font-weight: bold;" + 
                           "-fx-font-size: 18px;" + 
                           "-fx-padding: 10px;";

        hostColumn.setStyle(columnStyle);
        gameColumn.setStyle(columnStyle);
        statusColumn.setStyle(columnStyle);

        table.getColumns().addAll(hostColumn, gameColumn, statusColumn);
        table.setFixedCellSize(50);

        
        StackPane hostButton = createButton("Host Game", Color.BLUE, Color.DARKBLUE);
        StackPane joinButton = createButton("Join Game", Color.GREEN, Color.DARKGREEN);
        StackPane debugButton = createButton("Debug PvP", Color.PURPLE, Color.DARKVIOLET);
        StackPane backButton = createButton("Back", Color.GRAY, Color.DARKGRAY);

        VBox buttonBox = new VBox(20);
        buttonBox.getChildren().addAll(hostButton, joinButton, debugButton, backButton);
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
}