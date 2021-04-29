package client;

import client.controller.MainApplicationController;
import client.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        var mainView = new MainView(1200, 700);
        var controller = new MainApplicationController(mainView);

        primaryStage.setScene(mainView.getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
