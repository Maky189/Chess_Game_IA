package com.marcos.chess;

import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

public class MenuBackground extends StackPane {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    public MenuBackground(double width, double height) {
        try {
            String videoPath = "/assets/background/output.mp4";
            if (getClass().getResource(videoPath) == null) {
                setBackground(Background.fill(Color.BLACK));
                return;
            }

            String videoFile = getClass().getResource(videoPath).toExternalForm();
            Media media = new Media(videoFile);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);


            media.setOnError(() -> {
                setBackground(Background.fill(Color.BLACK));
            });

            mediaPlayer.setOnError(() -> {
                setBackground(Background.fill(Color.BLACK));
            });

            mediaView.setPreserveRatio(false);
            mediaView.setFitWidth(1600);
            mediaView.setFitHeight(870);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);
            mediaPlayer.setAutoPlay(true);

            setBackground(Background.fill(Color.BLACK));
            setAlignment(Pos.CENTER);
            getChildren().add(mediaView);

        } catch (Exception e) {
            setBackground(Background.fill(Color.BLACK));
        }
    }

    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }
}
