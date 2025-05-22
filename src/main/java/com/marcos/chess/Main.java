package com.marcos.chess;

/*This is my project of a Chess Game AI developed since March of 2025.
The game has two game modes: The 2D game mode and the 3D game mode.
The struture of the project is divided in the main functionalities of the game, following a singleton
project structure. Where each game mode is controlled from a single point.

- Main.java starts the main application and defines the variables of the window and the type of window used. Using javafx
- Menu.java has the main menu and manages the other menus like the multiplayer menu, options and the games lists

 */

import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private final int WINDOWS_WIDTH = 800;
    private final int WINDOWS_HEIGHT = 800;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Screen.getPrimary();
        Menu menu = new Menu(stage, WINDOWS_WIDTH, WINDOWS_HEIGHT);
        menu.showMenu();
    }
}