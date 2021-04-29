package server;

import javafx.application.Application;
import javafx.stage.Stage;
import server.controller.ServerController;
import server.view.View;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        var view = new View();
        var controller = new ServerController(view);

        primaryStage.setScene(view.getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
