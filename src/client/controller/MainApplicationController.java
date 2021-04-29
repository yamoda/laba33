package client.controller;

import client.view.*;
import javafx.application.Platform;
import server.model.Item;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Properties;

public class MainApplicationController {
    private static final int DEFAULT_PORT = 9090;
    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    private MainView view;

    Socket client;
    Properties connectProps;

    PrintWriter out;
    ObjectInputStream in;

    Thread clientThread = null;
    private boolean isRunning = true;

    public MainApplicationController(MainView view) {
        this.view = view;
        initController();

        connectProps = readProperties("config.properties");
    }

    private void initController() {
        view.getConnectButton().setOnAction(event -> connect());

        view.getSaveButton().setOnAction(event -> save());
        view.getLoadButton().setOnAction(event -> load());

        view.getNewButton().setOnAction(event -> initNewDialog());
        view.getFindButton().setOnAction(event -> initSearchDialog());
        view.getRemoveButton().setOnAction(event -> initRemoveDialog());

        view.getSaveItem().setOnAction(event -> save());
        view.getLoadItem().setOnAction(event -> load());

        view.getNewItem().setOnAction(event -> initNewDialog());
        view.getFindItem().setOnAction(event -> initSearchDialog());
        view.getRemoveItem().setOnAction(event -> initRemoveDialog());

        view.getTableView().getFirstPageButton().setOnAction(event -> toFirstPage());
        view.getTableView().getLastPageButton().setOnAction(event -> toLastPage());
        view.getTableView().getNextPageButton().setOnAction(event -> nextPage());
        view.getTableView().getPrevPageButton().setOnAction(event -> prevPage());
    }

    private void nextPage() {
        if (out != null) {
            out.println("NP");
        }
    }

    private void prevPage() {
        if (out != null) {
            out.println("PP");
        }
    }

    private void toFirstPage() {
        if (out != null) {
            out.println("FP");
        }
    }

    private void toLastPage() {
        if (out != null) {
            out.println("LP");
        }
    }

    private void connect() {
        if (clientThread == null) {
            clientThread = new Thread() {
                @Override
                public void run() {
                    try {
                        client = new Socket(connectProps.getProperty("address"), Integer.parseInt(connectProps.getProperty("port")));

                        out = new PrintWriter(client.getOutputStream(), true);
                        in = new ObjectInputStream(client.getInputStream());

                        isRunning = true;
                        while (isRunning) {
                            Object receivedArr = in.readObject();
                            if (receivedArr == null) continue;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() { updateTableView((ArrayList<Item>) (receivedArr)); }
                            });
                        }
                    }
                    catch (SocketException e) { clientThread = null; }
                    catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
                }
            };
            clientThread.start();
        }
    }

    private void load() {
        var dialog = new SaveLoadView();
        dialog.getActionButton().setOnAction(e -> {
            String fileName = dialog.getNameField().getText();
            if (out != null)  { out.println("L|"+fileName); }
        });
    }

    private void save() {
        var dialog = new SaveLoadView();
        dialog.getActionButton().setOnAction(e -> {
            String fileName = dialog.getNameField().getText();
            if (out != null)  { out.println("S|"+fileName); }
        });
    }

    private void initNewDialog() {
        var dialog = new NewDialog();
        dialog.getAddButton().setOnAction(event -> {
            var name = dialog.getNameField().getText();
            var manufactName = dialog.getManufactNameField().getText();
            var manufactIdText = dialog.getManufactNumberField().getText();
            var amountText = dialog.getAmountField().getText();
            var address = dialog.getAddressField().getText();

            boolean inputCorrect = true;
            int manufactId=0, amount=0;
            try {
                manufactId = Integer.parseInt(manufactIdText);
                amount = Integer.parseInt(amountText);
            }
            catch (NumberFormatException e) { inputCorrect = false; }

            if (inputCorrect && !name.isEmpty() && !manufactName.isEmpty() && !address.isEmpty()){
               if (out != null) {
                   out.println("ADD" + "|" +
                           name + "|" +
                           manufactName + "|" +
                           manufactIdText + "|" +
                           amountText + "|" +
                           address + "|"
                   );
               }
            }
        });
    }

    private void initRemoveDialog() {
        var dialog = new RemoveDialog();
        dialog.getRemoveButton().setOnAction(event -> {
            var name = dialog.getNameField().getText();
            var manufactName = dialog.getManufactNameField().getText();
            var address = dialog.getAddressField().getText();

            if (name.isEmpty()) name ="-1";
            if (manufactName.isEmpty()) manufactName ="-1";
            if (address.isEmpty()) address ="-1";

            if (out != null) {
                out.println("RMV|" + name + "|" + manufactName + "|" + address);

                try {
                    Thread.sleep(100);

                    Socket removeClient = new Socket(connectProps.getProperty("address"), 8000);
                    BufferedReader outRead = new BufferedReader(new InputStreamReader(removeClient.getInputStream()));

                    int numRemoved = Integer.parseInt(outRead.readLine());

                    if (numRemoved == 0) dialog.getRemovalInfo().setText("Ничего не было удалено");
                    else dialog.getRemovalInfo().setText("Было удалено записей " + numRemoved);

                    removeClient.close();
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void initSearchDialog() {
        var dialog = new SearchDialog();
        dialog.getFindButton().setOnAction(event -> {
            String name = dialog.getNameField().getText();
            String manufactName = dialog.getManufactNameField().getText();
            String address = dialog.getAddressField().getText();

            if (name.isEmpty()) name ="-1";
            if (manufactName.isEmpty()) manufactName ="-1";
            if (address.isEmpty()) address ="-1";

            if (out != null) {
                out.println("SRCH|"+ name + "|" + manufactName + "|" + address);

                try {
                    Thread.sleep(100);

                    Socket searchClient = new Socket(connectProps.getProperty("address"), 9000);
                    ObjectInputStream outRead = new ObjectInputStream(searchClient.getInputStream());

                    ArrayList<Item> foundArr = (ArrayList<Item>) outRead.readObject();

                    dialog.getFoundTable().getTable().getItems().clear();
                    dialog.getFoundTable().getTable().getItems().addAll(foundArr);

                    searchClient.close();
                }
                catch (IOException | InterruptedException | ClassNotFoundException ex) { ex.printStackTrace(); }
            }
        });
    }


    private void updateTableView(ArrayList<Item> newRows) {
        view.getTableView().getTable().getItems().clear();
        view.getTableView().getTable().getItems().addAll(newRows);
    }

    public static Properties readProperties(String fileName) {
        FileInputStream propertiesFileStream = null;
        Properties prop = null;

        try {
            propertiesFileStream = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(propertiesFileStream);
            propertiesFileStream.close();
        }
        catch (Exception e) {
            prop = new Properties();
            prop.setProperty("address", DEFAULT_ADDRESS);
            prop.setProperty("port", String.valueOf(DEFAULT_PORT));
        }

        return prop;
    }
}
