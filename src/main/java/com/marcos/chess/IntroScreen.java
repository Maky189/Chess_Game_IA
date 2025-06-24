package com.marcos.chess;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class IntroScreen extends StackPane {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Runnable onFinish;

    public IntroScreen(Stage stage, double width, double height, Runnable onFinish) {
        this.onFinish = onFinish;
        String videoPath = "/assets/background/intro.mp4";
        if (getClass().getResource(videoPath) == null) {
            // If intro video is missing, skip to menu
            if (onFinish != null) onFinish.run();
            return;
        }
        String videoFile = getClass().getResource(videoPath).toExternalForm();
        Media media = new Media(videoFile);
        mediaPlayer = new MediaPlayer(media);
        mediaView = new MediaView(mediaPlayer);
        mediaView.fitWidthProperty().bind(stage.widthProperty());
        mediaView.fitHeightProperty().bind(stage.heightProperty());
        mediaView.setPreserveRatio(false);
        getChildren().add(mediaView);

        Scene scene = new Scene(this, width, height);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                skipIntro();
            }
        });
        stage.setScene(scene);
          stage.setFullScreenExitHint(""); // Hide exit hint
        stage.setFullScreen(true); // Enable full screen
        stage.show();

        mediaPlayer.setOnEndOfMedia(this::endIntro);
        mediaPlayer.play();
    }

    private void skipIntro() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        endIntro();
    }

    private void endIntro() {
        if (onFinish != null) {
            onFinish.run();
        }
    }
}
