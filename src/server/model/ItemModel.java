package server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemModel {
    private int rowsOnPage;
    private int currentPage=1;
    ArrayList<Item> items;

    public ItemModel(int rowsOnPage) {
        items = new ArrayList<Item>();
        this.rowsOnPage = rowsOnPage;
    }

    public int getRowsOnPage() { return rowsOnPage; }

    public void setRowsOnPage(int rowsOnPage) { this.rowsOnPage = rowsOnPage; }

    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getCurrentPage() { return currentPage; }

    public List<Item> getPage(int page) {
        if (items.size() / rowsOnPage + 1 < page) return null;
        if (page <= 0) return null;

        int startIdx = (page-1)*rowsOnPage;
        int endIdx = Math.min(page*rowsOnPage, items.size());

        return new ArrayList<>(items.subList(startIdx, endIdx));
    }

    public ItemModel(ArrayList<Item> items) { this.items = items; }

    public ArrayList<Item> getItems() { return items; }

    public void setItems(ArrayList<Item> items) { this.items = items; }
}

