package client.view;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SaveLoadView {
    Stage dialogStage;
    Scene dialogScene;
    Group dialogPane;

    Button actionButton;
    Label actionLabel;
    TextField nameField;

    public SaveLoadView() {
        int width = 300;
        int height = 120;

        dialogPane = new Group();
        dialogScene = new Scene(dialogPane, width, height);

        actionLabel = new Label("Введите имя массива");
        nameField = new TextField();
        actionButton = new Button("Отправить на сервер");

        nameField.setTranslateY(20);
        nameField.setPrefHeight(50);
        nameField.setPrefWidth(300);

        actionButton.setTranslateY(70);
        actionButton.setPrefWidth(300);
        actionButton.setPrefHeight(50);

        dialogPane.getChildren().addAll(actionLabel, actionButton, nameField);

        dialogStage = new Stage();
        dialogStage.setResizable(false);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    public Button getActionButton() { return actionButton; }

    public TextField getNameField() { return nameField; }
}
