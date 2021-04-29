package server.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import server.model.Item;
import server.model.ItemModel;
import server.model.ItemParser;
import server.model.Logger;
import server.view.View;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerController {
    private boolean isRunning = true;
    private final int port = 9090;

    Thread serverThread = null;
    Socket clientSocket = null;
    ServerSocket serverSocket = null;

    private View view;
    private Logger logger;

    private ItemModel model;

    public ServerController(View view) {
        this.view = view;
        this.model = new ItemModel(10);

        this.logger = new Logger(view.getLogArea());

        this.view.getStartButton().setOnAction(e -> runServer());
        this.view.getStopButton().setOnAction(e -> stopServer());
    }

    private void runServer() {
        if (serverThread == null) {
            serverThread = new Thread() {
                @Override
                public void run() {
                    try {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                logger.addLog("Сервер запущен");
                                logger.addLog("Ожидание подключения клиента");
                            }
                        });

                        isRunning = true;
                        serverSocket = new ServerSocket(9090);

                        clientSocket = serverSocket.accept();

                        Platform.runLater(new Runnable() {public void run() {
                                logger.addLog("Клиент подключен");
                            }});

                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

                        while (isRunning) {
                            String request = in.readLine();
                            if (request != null) {
                                String[] args = request.split("\\|");
                                switch (args[0]) {
                                    case "L" -> {
                                        String fileName = args[1] + ".xml";
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на загрузку массива " + fileName); }});
                                        load(fileName, out);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на загрузку массива выполнен успешно");; }});
                                    }
                                    case "S" -> {
                                        String fileName = args[1] + ".xml";
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на сохранение массива " + fileName); }});
                                        save(fileName);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на сохранение массива выполнен успешно");; }});
                                    }
                                    case "NP" -> {
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на следующую страницу"); }});
                                        nextPage(out);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на следующую страницу выполнен успешно"); }});
                                    }
                                    case "PP" -> {
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на предыдущую страницу"); }});
                                        prevPage(out);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на предыдущую страницу выполнен успешно"); }});
                                    }
                                    case "FP" -> {
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на первую страницу"); }});
                                        firstPage(out);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на первую страницу выполнен успешно"); }});
                                    }
                                    case "LP" -> {
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на последнюю страницу"); }});
                                        lastPage(out);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на последнюю страницу выполнен успешно"); }});
                                    }
                                    case "ADD" -> {
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на добавление страницы"); }});
                                        add(args, out);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Страница добавлена успешно"); }});
                                    }
                                    case "SRCH" -> {
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на поиск"); }});
                                        find(args);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Найденные элементы успешно отправлены"); }});
                                    }
                                    case "RMV" -> {
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Запрос на удаление"); }});
                                        remove(args, out);
                                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Ответ на удаление отправлен"); }});
                                    }
                                }
                            }
                        }

                        in.close();
                        out.close();

                        clientSocket.close();
                        serverSocket.close();
                    } catch (SocketException e) {
                        Platform.runLater(new Runnable() {public void run() { logger.addLog("Сервер остановлен"); }});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            serverThread.start();
        }
    }

    private void remove(String[] args, ObjectOutputStream out) throws IOException {
        List<Item> toRemove = find(args[1], args[2], args[3]);
        model.getItems().removeAll(toRemove);

        ServerSocket removeSocket = new ServerSocket(8000);
        Socket removeRequest = removeSocket.accept();

        PrintWriter removeOut = new PrintWriter(removeRequest.getOutputStream());

        removeOut.println(toRemove.size());
        out.writeObject(model.getPage(model.getCurrentPage()));

        removeOut.close();
        removeRequest.close();
        removeSocket.close();
    }

    private void add(String[] args, ObjectOutputStream out) throws IOException {
        String name = args[1];
        String manufactName = args[2];
        int manufactId = Integer.parseInt(args[3]);
        int amount = Integer.parseInt(args[4]);
        String address = args[5];

        ArrayList<Item> itemsModel = model.getItems();
        itemsModel.add(new Item(name, manufactName, manufactId, amount, address));
        model.setItems(itemsModel);

        out.writeObject(model.getPage(model.getCurrentPage()));
    }

    private List<Item> find(String name, String manufactName, String address) {
        return model.getItems().stream()
                .filter(row -> row.getName().equals(name) || name.equals("-1"))
                .filter(row -> row.getManufactName().equals(manufactName) || manufactName.equals("-1"))
                .filter(row -> row.getAddress().equals(address) || address.equals("-1"))
                .collect(Collectors.toList());
    }

    private void find(String[] args) throws IOException {
        List<Item> foundArr = find(args[1], args[2], args[3]);

        ServerSocket foundSocket = new ServerSocket(9000);
        Socket foundRequest = foundSocket.accept();

        ObjectOutputStream foundOut = new ObjectOutputStream(foundRequest.getOutputStream());
        foundOut.writeObject(foundArr);

        foundOut.close();
        foundRequest.close();
        foundSocket.close();
    }

    private void nextPage(ObjectOutputStream out) throws IOException {
        int currentPage = model.getCurrentPage();
        List<Item> nextPage = model.getPage(currentPage + 1);
        out.writeObject(nextPage);

        if (nextPage != null) model.setCurrentPage(currentPage + 1);
    }

    private void prevPage(ObjectOutputStream out) throws  IOException {
        int currentPage = model.getCurrentPage();
        List<Item> nextPage = model.getPage(currentPage - 1);
        out.writeObject(nextPage);

        if (nextPage != null) model.setCurrentPage(currentPage - 1);
    }

    private void firstPage(ObjectOutputStream out) throws IOException {
        model.setCurrentPage(1);
        out.writeObject(model.getPage(1));
    }

    private void lastPage(ObjectOutputStream out) throws IOException {
        int lastPage = model.getItems().size() / model.getRowsOnPage() + 1;
        model.setCurrentPage(lastPage);
        out.writeObject(model.getPage(lastPage));
    }

    private void save(String fileName) {
        ItemParser xmlModelWriter = new ItemParser();
        xmlModelWriter.parseAndWrite("Items", "Item", model.getItems(), fileName);
    }

    private void load(String fileName, ObjectOutputStream out) throws IOException {
        ItemParser xmlDocumentReader = new ItemParser();
        ArrayList<Item> documentEntries = xmlDocumentReader.readAndParse(fileName);
        model.setItems(documentEntries);
        out.writeObject(model.getPage(1));
    }

    private void stopServer() {
        if (serverSocket != null) {
            try {
                isRunning = false;
                serverSocket.close();
                serverThread = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
