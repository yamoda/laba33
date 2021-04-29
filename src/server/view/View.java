package server.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class View {
    Group mainRoot;
    Scene mainScene;

    Label logArea;
    Button startButton;
    Button stopButton;

    public View() {
        mainRoot = new Group();
        mainScene = new Scene(mainRoot, 800, 600);

        startButton = new Button("Запустить сервер");
        stopButton = new Button("Остановить сервер");

        logArea = new Label(" ");
        logArea.setFont(new Font("Helvetica", 17));

        startButton.setTranslateX(0);
        startButton.setTranslateY(0);
        startButton.setPrefWidth(400);
        startButton.setPrefHeight(50);

        stopButton.setTranslateX(400);
        stopButton.setTranslateY(0);
        stopButton.setPrefWidth(400);
        stopButton.setPrefHeight(50);

        logArea.setTranslateY(50);
        mainRoot.getChildren().addAll(startButton, stopButton, logArea);
    }

    public Scene getScene() { return mainScene; }

    public Button getStartButton() { return  startButton; }

    public Button getStopButton() { return stopButton; }

    public Label getLogArea() { return logArea; }
}
