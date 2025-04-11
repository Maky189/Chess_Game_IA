package com.marcos.chess;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private final int SQUARE_SIZE = 80;
    private final int SIZE = 8;
    private final int WINDOWS_WIDTH = 800;
    private final int WINDOWS_HEIGHT = 800;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Menu menu = new Menu(stage, WINDOWS_WIDTH, WINDOWS_HEIGHT);
        menu.showMenu();
    }
}
