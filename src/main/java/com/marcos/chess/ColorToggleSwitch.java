package com.marcos.chess;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ColorToggleSwitch extends StackPane {
    private final Rectangle back;
    private final Circle trigger;
    private boolean isWhite;
    private final TranslateTransition translateAnimation;

    public ColorToggleSwitch(boolean initialState) {
        isWhite = initialState;

        back = new Rectangle(70, 30);
        back.setArcHeight(back.getHeight());
        back.setArcWidth(back.getHeight());
        back.setFill(isWhite ? Color.LIGHTGRAY : Color.DARKGRAY);
        back.setStroke(Color.BLACK);

        trigger = new Circle(15);
        trigger.setCenterX(15);
        trigger.setFill(isWhite ? Color.WHITE : Color.BLACK);
        trigger.setStroke(Color.BLACK);

        translateAnimation = new TranslateTransition(Duration.seconds(0.25), trigger);

        getChildren().addAll(back, trigger);

        setTriggerPosition();

        
        setOnMouseEntered(e -> {
            back.setStroke(Color.WHITESMOKE);
            setCursor(javafx.scene.Cursor.HAND);
        });
        
        setOnMouseExited(e -> {
            back.setStroke(Color.BLACK);
            setCursor(javafx.scene.Cursor.DEFAULT);
        });
    }

    private void setTriggerPosition() {
        trigger.setTranslateX(isWhite ? 0 : 40);
        back.setFill(isWhite ? Color.LIGHTGRAY : Color.DARKGRAY);
        trigger.setFill(isWhite ? Color.WHITE : Color.BLACK);
    }

    public void toggle() {
        isWhite = !isWhite;
        translateAnimation.setToX(isWhite ? 0 : 40);
        translateAnimation.play();
        back.setFill(isWhite ? Color.LIGHTGRAY : Color.DARKGRAY);
        trigger.setFill(isWhite ? Color.WHITE : Color.BLACK);
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setWhite(boolean white) {
        if (white != isWhite) {
            toggle();
        }
    }
}