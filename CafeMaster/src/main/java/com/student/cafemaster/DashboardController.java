package com.student.cafemaster;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // =========================================================================
    //  SIDEBAR NAV BUTTONS
    // =========================================================================
    @FXML private Button inventory_btn, orders_btn, reports_btn, staff_btn, settings_btn;

    // =========================================================================
    //  CONTENT FORM PANES — one per nav button
    // =========================================================================
    @FXML private AnchorPane inventory_form;   // visible by default
    @FXML private AnchorPane dashboard_form;   // generic placeholder (kept for safety)
    @FXML private AnchorPane orders_form;      // NEW
    @FXML private AnchorPane reports_form;     // NEW
    @FXML private AnchorPane staff_form;       // NEW
    @FXML private AnchorPane settings_form;    // NEW

    // =========================================================================
    //  INVENTORY — form fields  (YOUR ORIGINAL CODE — UNCHANGED)
    // =========================================================================
    @FXML private TextField inventory_search;
    @FXML private Label dashboard_totalProducts, dashboard_criticalStock,
                        dashboard_mediumStock, dashboard_inventoryValue;

    @FXML private TextField inventory_productID, inventory_productName,
                            inventory_type, inventory_stock, inventory_price;
    @FXML private Button inventory_addBtn, inventory_updateBtn,
                         inventory_deleteBtn, inventory_clearBtn;

    @FXML private Label label_tableCount;
    @FXML private TableView<productData>            inventory_tableView;
    @FXML private TableColumn<productData, String>  inventory_col_productID;
    @FXML private TableColumn<productData, String>  inventory_col_productName;
    @FXML private TableColumn<productData, String>  inventory_col_type;
    @FXML private TableColumn<productData, Integer> inventory_col_stock;
    @FXML private TableColumn<productData, Double>  inventory_col_price;
    @FXML private TableColumn<productData, String>  inventory_col_status;

    @FXML private Label statusDot, db_status_label;

    private final ObservableList<productData> productList = FXCollections.observableArrayList();

    // =========================================================================
    //  ORDERS — cart table + menu
    // =========================================================================
    @FXML private Label                                  orders_totalLabel;
    @FXML private TableView<OrderItem>                   orders_cartTable;
    @FXML private TableColumn<OrderItem, String>         orders_col_item;
    @FXML private TableColumn<OrderItem, Integer>        orders_col_qty;
    @FXML private TableColumn<OrderItem, Double>         orders_col_price;
    @FXML private TableColumn<OrderItem, Double>         orders_col_total;
    @FXML private TilePane                               orders_menuGrid;  // dynamic menu
    @FXML private Label                                  orders_qtyLabel;  // qty display
    @FXML private TextField                              orders_cashField; // cash input
    @FXML private Label                                  orders_changeLabel; // change display

    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();

    // =========================================================================
    //  REPORTS — stat labels + BarChart
    // =========================================================================
    @FXML private Label      reports_totalSales;
    @FXML private Label      reports_ordersCount;
    @FXML private Label      reports_bestSeller;
    @FXML private BarChart<String, Number> reports_barChart;
    @FXML private CategoryAxis reports_xAxis;
    @FXML private NumberAxis   reports_yAxis;
    // Sales chart
    @FXML private BarChart<String, Number> reports_salesChart;
    @FXML private CategoryAxis reports_salesXAxis;
    @FXML private NumberAxis   reports_salesYAxis;

    @FXML private DatePicker reports_datePicker;

    // Reports data is now permanently persisted to database through OrderDAO

    // =========================================================================
    //  STAFF — form + table
    // =========================================================================
    @FXML private TextField         staff_nameField;
    @FXML private ComboBox<String>  staff_roleCombo;
    @FXML private Label             staff_countLabel;
    @FXML private TableView<StaffMember>            staff_tableView;
    @FXML private TableColumn<StaffMember, String>  staff_col_name;
    @FXML private TableColumn<StaffMember, String>  staff_col_role;
    @FXML private TableColumn<StaffMember, String>  staff_col_status;

    private final ObservableList<StaffMember> staffList = FXCollections.observableArrayList();

    // =========================================================================
    //  SETTINGS
    // =========================================================================
    @FXML private TextField settings_shopName;
    @FXML private TextField settings_adminName;
    @FXML private TextField settings_dbUrl;
    @FXML private Button    settings_logoutBtn;

    // Sidebar labels updated live by onSaveSettings()
    @FXML private Label sidebar_shopName;
    @FXML private Label sidebar_adminName;
    @FXML private Label sidebar_adminRole;

    // =========================================================================
    //  NAV BUTTON STYLES
    // =========================================================================
    private static final String STYLE_ACTIVE =
        "-fx-background-color: #C8922A; -fx-text-fill: #1C1008; -fx-font-weight: bold; " +
        "-fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand; " +
        "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14; " +
        "-fx-effect: dropshadow(gaussian, rgba(200,146,42,0.4), 10, 0, 0, 3);";

    private static final String STYLE_INACTIVE =
        "-fx-background-color: transparent; -fx-text-fill: #9E826A; -fx-font-size: 13; " +
        "-fx-background-radius: 8; -fx-cursor: hand; " +
        "-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 14;";

    // =========================================================================
    //  INITIALIZE
    // =========================================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();        // inventory — YOUR original
        setupSearch();       // inventory — YOUR original
        setupOrdersCart();   // NEW — orders
        setupStaff();        // NEW — staff
        setupReportsChart(); // NEW — reports
        setupSettings();     // NEW -- settings DB
        refreshData();       // inventory — YOUR original
    }

    private void setupSettings() {
        try {
            String[] sets = SettingsDAO.getSettings();
            if (settings_shopName != null) settings_shopName.setText(sets[0]);
            if (settings_adminName != null) settings_adminName.setText(sets[1]);
            if (sidebar_shopName != null) sidebar_shopName.setText(sets[0]);
            if (sidebar_adminName != null) sidebar_adminName.setText(sets[1]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //  NAVIGATION — switchForm
    //  YOUR original pattern: hide all → reset buttons → show correct form
    // =========================================================================
    @FXML
    public void switchForm(ActionEvent event) {

        // 1. Hide ALL forms
        setFormVisible(inventory_form, false);
        setFormVisible(dashboard_form,  false);
        setFormVisible(orders_form,     false);
        setFormVisible(reports_form,    false);
        setFormVisible(staff_form,      false);
        setFormVisible(settings_form,   false);

        // 2. Reset all nav buttons to INACTIVE
        Button[] navBtns = { inventory_btn, orders_btn, reports_btn, staff_btn, settings_btn };
        for (Button btn : navBtns) {
            if (btn != null) btn.setStyle(STYLE_INACTIVE);
        }

        Object src = event.getSource();

        // 3. Show the matching form and highlight its button
        if (src == inventory_btn) {
            if (inventory_btn != null) inventory_btn.setStyle(STYLE_ACTIVE);
            setFormVisible(inventory_form, true);
            refreshData();

        } else if (src == orders_btn) {
            if (orders_btn != null) orders_btn.setStyle(STYLE_ACTIVE);
            setFormVisible(orders_form, true);
            refreshOrdersMenu();   // populate menu from DB

        } else if (src == reports_btn) {
            if (reports_btn != null) reports_btn.setStyle(STYLE_ACTIVE);
            setFormVisible(reports_form, true);
            refreshReports();

        } else if (src == staff_btn) {
            if (staff_btn != null) staff_btn.setStyle(STYLE_ACTIVE);
            setFormVisible(staff_form, true);

        } else if (src == settings_btn) {
            if (settings_btn != null) settings_btn.setStyle(STYLE_ACTIVE);
            setFormVisible(settings_form, true);
        }
    }

    /** Null-safe visibility toggle — YOUR original helper, kept intact. */
    private void setFormVisible(AnchorPane pane, boolean visible) {
        if (pane != null) pane.setVisible(visible);
    }


    // =========================================================================
    //  INVENTORY — setupTable  (YOUR ORIGINAL CODE — UNCHANGED)
    // =========================================================================
    private void setupTable() {
        inventory_col_productID.setCellValueFactory(new PropertyValueFactory<>("productID"));
        inventory_col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        inventory_col_type.setCellValueFactory(new PropertyValueFactory<>("type"));
        inventory_col_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        inventory_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));

        inventory_col_status.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getStock();
            if (stock <= 5)  return new SimpleStringProperty("⚠ Critical");
            if (stock <= 15) return new SimpleStringProperty("● Medium");
            return new SimpleStringProperty("✓ Good");
        });

        inventory_col_status.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setStyle("");
                } else if (status.contains("Critical")) {
                    setText(status); setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                } else if (status.contains("Medium")) {
                    setText(status); setStyle("-fx-text-fill: #D68910; -fx-font-weight: bold;");
                } else {
                    setText(status); setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;");
                }
            }
        });

        inventory_col_price.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("₱%.2f", price));
            }
        });
    }

    // =========================================================================
    //  INVENTORY — setupSearch  (YOUR ORIGINAL CODE — UNCHANGED)
    // =========================================================================
    private void setupSearch() {
        FilteredList<productData> filteredData = new FilteredList<>(productList, p -> true);

        inventory_search.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(product -> {
                if (newVal == null || newVal.trim().isEmpty()) return true;
                String keyword = newVal.trim().toLowerCase();
                return product.getProductID().toLowerCase().contains(keyword)
                    || product.getProductName().toLowerCase().contains(keyword)
                    || product.getType().toLowerCase().contains(keyword);
            });
            if (label_tableCount != null)
                label_tableCount.setText("Showing " + filteredData.size() + " items");
        });

        SortedList<productData> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(inventory_tableView.comparatorProperty());
        inventory_tableView.setItems(sortedData);
    }

    // =========================================================================
    //  INVENTORY — refreshData  (YOUR ORIGINAL CODE — UNCHANGED)
    // =========================================================================
    private void refreshData() {
        if (db_status_label != null) db_status_label.setText("Loading data...");
        if (statusDot != null) statusDot.setStyle("-fx-text-fill: #F39C12;");

        Task<ObservableList<productData>> task = new Task<>() {
            @Override
            protected ObservableList<productData> call() throws Exception {
                return FXCollections.observableArrayList(ProductDAO.getAllProducts());
            }
        };

        task.setOnSucceeded(e -> {
            productList.setAll(task.getValue());
            updateDashboardStats();
            if (label_tableCount != null)
                label_tableCount.setText("Showing " + productList.size() + " items");
            if (db_status_label != null)
                db_status_label.setText("Connected — " + productList.size() + " product(s) loaded");
            if (statusDot != null)
                statusDot.setStyle("-fx-text-fill: #1E8449;");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (db_status_label != null) db_status_label.setText("Database connection error");
            if (statusDot != null) statusDot.setStyle("-fx-text-fill: #C0392B;");
            showAlert(Alert.AlertType.ERROR, "Database Error",
                "Failed to load products:\n" + ex.getMessage());
        });

        new Thread(task, "db-load-thread").start();
    }

    // =========================================================================
    //  INVENTORY — updateDashboardStats  (YOUR ORIGINAL CODE — UNCHANGED)
    // =========================================================================
    private void updateDashboardStats() {
        int total = productList.size();
        int critical = 0, medium = 0;
        double totalValue = 0.0;

        for (productData p : productList) {
            int s = p.getStock();
            if (s <= 5) critical++;
            else if (s <= 15) medium++;
            totalValue += p.getPrice() * s;
        }

        if (dashboard_totalProducts  != null) dashboard_totalProducts.setText(String.valueOf(total));
        if (dashboard_criticalStock  != null) dashboard_criticalStock.setText(String.valueOf(critical));
        if (dashboard_mediumStock    != null) dashboard_mediumStock.setText(String.valueOf(medium));
        if (dashboard_inventoryValue != null) dashboard_inventoryValue.setText(String.format("₱%.2f", totalValue));
    }

    // =========================================================================
    //  INVENTORY CRUD  (YOUR ORIGINAL CODE — UNCHANGED)
    // =========================================================================
    @FXML
    void inventoryAddBtn(ActionEvent event) {
        if (isFormEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill in all fields.");
            return;
        }
        try {
            int id = Integer.parseInt(inventory_productID.getText().trim());
            if (ProductDAO.idExists(id)) {
                showAlert(Alert.AlertType.ERROR, "Duplicate ID",
                    "Product ID " + id + " already exists.");
                return;
            }
            ProductDAO.insert(buildProductFromForm());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
            inventoryClearBtn(null);
            refreshData();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                "ID and Stock must be whole numbers, Price must be a decimal.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                "Could not add product:\n" + e.getMessage());
        }
    }

    @FXML
    void inventoryUpdateBtn(ActionEvent event) {
        productData selected = inventory_tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Select a product in the table first.");
            return;
        }
        if (isFormEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill in all fields.");
            return;
        }
        try {
            int oldId = Integer.parseInt(selected.getProductID());
            ProductDAO.update(buildProductFromForm(), oldId);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
            inventoryClearBtn(null);
            refreshData();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                "ID, Stock, and Price must be valid numbers.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                "Could not update product:\n" + e.getMessage());
        }
    }

    @FXML
    void inventoryDeleteBtn(ActionEvent event) {
        productData selected = inventory_tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Select a product in the table first.");
            return;
        }
        try {
            int id = Integer.parseInt(selected.getProductID());
            ProductDAO.delete(id);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
            inventoryClearBtn(null);
            refreshData();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                "Could not delete product:\n" + e.getMessage());
        }
    }

    @FXML
    void inventoryClearBtn(ActionEvent event) {
        inventory_productID.clear();
        inventory_productName.clear();
        inventory_type.clear();
        inventory_stock.clear();
        inventory_price.clear();
        inventory_productID.setDisable(false);
        inventory_tableView.getSelectionModel().clearSelection();
    }

    @FXML
    void inventorySelectData(MouseEvent event) {
        productData selected = inventory_tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            inventory_productID.setText(selected.getProductID());
            inventory_productName.setText(selected.getProductName());
            inventory_type.setText(selected.getType());
            inventory_stock.setText(String.valueOf(selected.getStock()));
            inventory_price.setText(String.format("%.2f", selected.getPrice()));
            inventory_productID.setDisable(true);
        }
    }

    private boolean isFormEmpty() {
        return inventory_productID.getText().trim().isEmpty()
            || inventory_productName.getText().trim().isEmpty()
            || inventory_type.getText().trim().isEmpty()
            || inventory_stock.getText().trim().isEmpty()
            || inventory_price.getText().trim().isEmpty();
    }

    private productData buildProductFromForm() {
        return new productData(
            inventory_productID.getText().trim(),
            inventory_productName.getText().trim(),
            inventory_type.getText().trim(),
            Integer.parseInt(inventory_stock.getText().trim()),
            Double.parseDouble(inventory_price.getText().trim())
        );
    }


    // =========================================================================
    //  ORDERS — Setup cart table columns
    // =========================================================================
    private void setupOrdersCart() {
        orders_col_item .setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getItem()));
        orders_col_qty  .setCellValueFactory(cd ->
            new SimpleIntegerProperty(cd.getValue().getQty()).asObject());
        orders_col_price.setCellValueFactory(cd ->
            new SimpleDoubleProperty(cd.getValue().getUnitPrice()).asObject());
        orders_col_total.setCellValueFactory(cd ->
            new SimpleDoubleProperty(cd.getValue().getQty() * cd.getValue().getUnitPrice()).asObject());

        orders_col_price.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("₱%.2f", v));
            }
        });
        orders_col_total.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("₱%.2f", v));
            }
        });

        orders_cartTable.setItems(cartItems);

        // Update qty label when user selects a row
        orders_cartTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, sel) -> {
                if (orders_qtyLabel == null) return;
                if (sel != null)
                    orders_qtyLabel.setText(
                        sel.getItem() + "  × " + sel.getQty());
                else
                    orders_qtyLabel.setText("Select an item to adjust qty");
            });

        // Auto-calculate change when cash input changes
        if (orders_cashField != null) {
            orders_cashField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*(\\.\\d*)?")) {
                    orders_cashField.setText(oldVal); // force numeric
                } else {
                    recalcChange();
                }
            });
        }
    }

    /**
     * Rebuilds the Orders menu TilePane from the current productList.
     * Called every time the Orders nav button is clicked so it stays in sync
     * with whatever products are in the Inventory.
     */
    private void refreshOrdersMenu() {
        if (orders_menuGrid == null) return;
        orders_menuGrid.getChildren().clear();

        if (productList.isEmpty()) {
            Label empty = new Label("No products found.\nAdd products in Inventory first.");
            empty.setStyle("-fx-text-fill: #9E826A; -fx-font-size: 13; " +
                           "-fx-text-alignment: center; -fx-wrap-text: true;");
            orders_menuGrid.getChildren().add(empty);
            return;
        }

        String btnStyle =
            "-fx-background-color: #FBF8F3; " +
            "-fx-border-color: #DDD3C0; -fx-border-radius: 8; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-font-size: 12; -fx-text-fill: #1C1008; " +
            "-fx-alignment: CENTER; " +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.05),6,0,0,2);";

        for (productData p : productList) {
            // Format text same as the old static buttons: "Name\n₱price"
            String label = p.getProductName() + "\n₱" +
                           String.format("%.2f", p.getPrice());
            Button btn = new Button(label);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(80.0);
            btn.setWrapText(true);
            btn.setStyle(btnStyle);
            btn.setOnAction(this::onMenuItemClick);
            orders_menuGrid.getChildren().add(btn);
        }
    }

    /**
     * Called by every menu button (onAction="#onMenuItemClick").
     * Button text format: "☕ Name\n₱price"
     */
    @FXML
    public void onMenuItemClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        String[] parts = clicked.getText().split("\n");
        if (parts.length < 2) return;

        String itemName = parts[0].trim().replaceAll("^[^a-zA-Z]+", "").trim();
        String priceStr = parts[1].replace("₱", "").replace(",", "").trim();
        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException ignored) { return; }

        productData product = null;
        for (productData p : productList) {
            String normPName = p.getProductName().trim().replaceAll("^[^a-zA-Z]+", "").trim();
            if (normPName.equalsIgnoreCase(itemName) || p.getProductName().equalsIgnoreCase(itemName)) {
                product = p;
                break;
            }
        }
        if (product == null) return;

        OrderItem existingItem = null;
        for (OrderItem existing : cartItems) {
            if (existing.getItem().equalsIgnoreCase(itemName)) {
                existingItem = existing;
                break;
            }
        }

        int currentCartQty = (existingItem != null) ? existingItem.getQty() : 0;
        
        if (currentCartQty >= product.getStock()) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock",
                "Cannot add more " + itemName + " to cart. Available stock: " + product.getStock());
            return;
        }

        if (existingItem != null) {
            existingItem.setQty(existingItem.getQty() + 1);
            orders_cartTable.refresh();
            recalcOrderTotal();
            return;
        }
        cartItems.add(new OrderItem(itemName, 1, price));
        recalcOrderTotal();
    }

    @FXML
    public void onClearCart(ActionEvent event) {
        cartItems.clear();
        if (orders_totalLabel != null) orders_totalLabel.setText("₱0.00");
        if (orders_qtyLabel   != null) orders_qtyLabel.setText("Select an item to adjust qty");
        if (orders_cashField  != null) orders_cashField.clear();
        if (orders_changeLabel!= null) orders_changeLabel.setText("₱0.00");
    }

    /** Increase qty of selected cart row by 1. */
    @FXML
    public void onIncreaseQty(ActionEvent event) {
        OrderItem sel = orders_cartTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        productData product = null;
        for (productData p : productList) {
            String normPName = p.getProductName().trim().replaceAll("^[^a-zA-Z]+", "").trim();
            if (normPName.equalsIgnoreCase(sel.getItem()) || p.getProductName().equalsIgnoreCase(sel.getItem())) {
                product = p;
                break;
            }
        }

        if (product != null && sel.getQty() >= product.getStock()) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock",
                "Cannot add more " + sel.getItem() + " to cart. Available stock: " + product.getStock());
            return;
        }

        sel.setQty(sel.getQty() + 1);
        orders_cartTable.refresh();
        recalcOrderTotal();
        if (orders_qtyLabel != null)
            orders_qtyLabel.setText(sel.getItem() + "  × " + sel.getQty());
    }

    /** Decrease qty of selected cart row by 1; removes item at 0. */
    @FXML
    public void onDecreaseQty(ActionEvent event) {
        OrderItem sel = orders_cartTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (sel.getQty() > 1) {
            sel.setQty(sel.getQty() - 1);
            orders_cartTable.refresh();
            recalcOrderTotal();
            if (orders_qtyLabel != null)
                orders_qtyLabel.setText(sel.getItem() + "  × " + sel.getQty());
        } else {
            cartItems.remove(sel);
            orders_cartTable.getSelectionModel().clearSelection();
            recalcOrderTotal();
            if (orders_qtyLabel != null)
                orders_qtyLabel.setText("Select an item to adjust qty");
        }
    }

    /** Remove the selected cart row entirely. */
    @FXML
    public void onRemoveCartItem(ActionEvent event) {
        OrderItem sel = orders_cartTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        cartItems.remove(sel);
        orders_cartTable.getSelectionModel().clearSelection();
        recalcOrderTotal();
        if (orders_qtyLabel != null)
            orders_qtyLabel.setText("Select an item to adjust qty");
    }

    @FXML
    public void onPlaceOrder(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart",
                "Add items before placing an order.");
            return;
        }
        double total = cartItems.stream()
            .mapToDouble(i -> i.getQty() * i.getUnitPrice()).sum();

        double cash = 0.0;
        if (orders_cashField != null && !orders_cashField.getText().trim().isEmpty()) {
            try { cash = Double.parseDouble(orders_cashField.getText().trim()); }
            catch (NumberFormatException ignored) {}
        }

        if (cash < total) {
            showAlert(Alert.AlertType.WARNING, "Insufficient Cash",
                String.format("Total is ₱%.2f but only ₱%.2f tendered.", total, cash));
            return;
        }

        double change = cash - total;

        StringBuilder sb = new StringBuilder();
        for (OrderItem i : cartItems)
            sb.append(String.format("  %-18s x%d  ₱%.2f%n",
                i.getItem(), i.getQty(), i.getQty() * i.getUnitPrice()));
        sb.append(String.format("%n  TOTAL:   ₱%.2f", total));
        sb.append(String.format("%n  CASH:    ₱%.2f", cash));
        sb.append(String.format("%n  CHANGE:  ₱%.2f", change));

        showAlert(Alert.AlertType.INFORMATION, "Order Placed! 🎉", sb.toString());

        try {
            OrderDAO.saveOrder(total, cash, cartItems, productList);
            // Refresh inventory table to reflect new stock
            refreshData(); 
            
            if (reports_datePicker != null && LocalDate.now().equals(reports_datePicker.getValue())) {
                refreshReports();
            }
        } catch (SQLException e) {
             showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save order:\n" + e.getMessage());
             return;
        }

        cartItems.clear();
        if (orders_totalLabel != null) orders_totalLabel.setText("₱0.00");
        if (orders_qtyLabel   != null) orders_qtyLabel.setText("Select an item to adjust qty");
        if (orders_cashField  != null) orders_cashField.clear();
        if (orders_changeLabel!= null) orders_changeLabel.setText("₱0.00");
    }

    private void recalcOrderTotal() {
        double total = cartItems.stream()
            .mapToDouble(i -> i.getQty() * i.getUnitPrice()).sum();
        if (orders_totalLabel != null)
            orders_totalLabel.setText(String.format("₱%.2f", total));
        recalcChange();
    }

    private void recalcChange() {
        if (orders_changeLabel == null || orders_cashField == null) return;
        double total = cartItems.stream()
            .mapToDouble(i -> i.getQty() * i.getUnitPrice()).sum();

        double cash = 0.0;
        if (!orders_cashField.getText().trim().isEmpty()) {
            try { cash = Double.parseDouble(orders_cashField.getText().trim()); }
            catch (NumberFormatException ignored) {}
        }

        double change = cash - total;
        if (change < 0) {
            orders_changeLabel.setText("₱0.00");
        } else {
            orders_changeLabel.setText(String.format("₱%.2f", change));
        }
    }


    // =========================================================================
    //  REPORTS — BarChart setup and refresh
    // =========================================================================
    private void setupReportsChart() {
        if (reports_totalSales  != null) reports_totalSales.setText("₱0.00");
        if (reports_ordersCount != null) reports_ordersCount.setText("0");
        if (reports_bestSeller  != null) reports_bestSeller.setText("—");
        
        if (reports_datePicker != null) {
            reports_datePicker.setValue(LocalDate.now());
            reports_datePicker.setOnAction(e -> refreshReports());
        }
    }

    /**
     * Populates both BarCharts:
     *   - Stock chart  : current inventory stock levels
     *   - Sales chart  : revenue per product for the selected date
     * Called automatically when the Reports nav button is clicked or date changes.
     */
    private void refreshReports() {
        LocalDate selectedDate = reports_datePicker != null && reports_datePicker.getValue() != null 
                             ? reports_datePicker.getValue() : LocalDate.now();

        try {
            Map<String, Integer> dayStock = OrderDAO.getStockHistoryByDate(selectedDate);
            Map<String, Double> daySales = OrderDAO.getSalesByDate(selectedDate);
            int orders = OrderDAO.getOrderCountByDate(selectedDate);

            // --- Stock chart ---
            if (reports_barChart != null) {
                reports_barChart.getData().clear();
                XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
                stockSeries.setName("Stock Remaining");
                
                if (!dayStock.isEmpty()) {
                    for (Map.Entry<String, Integer> e : dayStock.entrySet()) {
                        stockSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
                    }
                } else if (selectedDate.equals(LocalDate.now())) {
                    for (productData p : productList) {
                        stockSeries.getData().add(new XYChart.Data<>(p.getProductName(), p.getStock()));
                    }
                } else {
                    for (productData p : productList) {
                        stockSeries.getData().add(new XYChart.Data<>(p.getProductName(), 0));
                    }
                }
                reports_barChart.getData().add(stockSeries);
            }

            // --- Sales chart & Stats ---
            if (reports_salesChart != null) {
                reports_salesChart.getData().clear();
                XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
                salesSeries.setName("Revenue (₱)");
                
                if (daySales.isEmpty()) {
                    for (productData p : productList)
                        salesSeries.getData().add(new XYChart.Data<>(p.getProductName(), 0));
                } else {
                    for (Map.Entry<String, Double> e : daySales.entrySet())
                        salesSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
                }
                reports_salesChart.getData().add(salesSeries);
                
                // Update Stat Cards for the selected date
                double totalRev = daySales.values().stream().mapToDouble(Double::doubleValue).sum();
                String best = daySales.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("—");
                    
                if (reports_ordersCount != null) reports_ordersCount.setText(String.valueOf(orders));
                if (reports_totalSales != null) reports_totalSales.setText(String.format("₱%.2f", totalRev));
                if (reports_bestSeller != null) reports_bestSeller.setText(best);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================================================
    //  STAFF — table setup and CRUD (in-memory)
    // =========================================================================
    private void setupStaff() {
        staff_col_name  .setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getName()));
        staff_col_role  .setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getRole()));
        staff_col_status.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getStatus()));

        // Colour-code the Status column
        staff_col_status.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle("Active".equalsIgnoreCase(v)
                    ? "-fx-text-fill: #1E8449; -fx-font-weight: bold;"
                    : "-fx-text-fill: #C0392B; -fx-font-weight: bold;");
            }
        });

        staff_tableView.setItems(staffList);

        if (staff_roleCombo != null)
            staff_roleCombo.getItems().addAll(
                "Barista", "Cashier", "Manager", "Cleaner", "Waiter");

        try {
            staffList.setAll(StaffDAO.getAllStaff());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateStaffCount();
    }

    @FXML
    public void onAddStaff(ActionEvent event) {
        if (staff_nameField == null || staff_nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Name",
                "Enter a full name for the staff member.");
            return;
        }
        if (staff_roleCombo == null || staff_roleCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Role",
                "Select a role from the dropdown.");
            return;
        }
        StaffMember newStaff = new StaffMember(
            staff_nameField.getText().trim(),
            staff_roleCombo.getValue(),
            "Active"
        );
        try {
            StaffDAO.addStaff(newStaff);
            staffList.add(newStaff);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not add staff.");
            return;
        }
        staff_nameField.clear();
        staff_roleCombo.setValue(null);
        updateStaffCount();
    }

    @FXML
    public void onRemoveStaff(ActionEvent event) {
        StaffMember sel = staff_tableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Select a staff member to remove.");
            return;
        }
        try {
            StaffDAO.removeStaff(sel.getName());
            staffList.remove(sel);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not remove staff.");
            return;
        }
        updateStaffCount();
    }

    private void updateStaffCount() {
        if (staff_countLabel != null)
            staff_countLabel.setText(staffList.size() +
                " staff member" + (staffList.size() != 1 ? "s" : ""));
    }


    // =========================================================================
    //  SETTINGS
    // =========================================================================
    @FXML
    public void onSaveSettings(ActionEvent event) {
        String shop  = settings_shopName  != null ? settings_shopName.getText().trim()  : "";
        String admin = settings_adminName != null ? settings_adminName.getText().trim() : "";
        if (shop.isEmpty() && admin.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nothing to Save",
                "Enter at least one setting to save.");
            return;
        }

        try {
            String finalShop = shop.isEmpty() ? (sidebar_shopName != null ? sidebar_shopName.getText() : "CafeMaster") : shop;
            String finalAdmin = admin.isEmpty() ? (sidebar_adminName != null ? sidebar_adminName.getText() : "Admin User") : admin;
            SettingsDAO.saveSettings(finalShop, finalAdmin);
            
            // Update sidebar labels immediately
            if (!shop.isEmpty()  && sidebar_shopName  != null) sidebar_shopName.setText(shop);
            if (!admin.isEmpty() && sidebar_adminName != null) sidebar_adminName.setText(admin);

            showAlert(Alert.AlertType.INFORMATION, "Settings Saved ✅",
                "Shop Name : " + (shop.isEmpty()  ? "(unchanged)" : shop) +
                "\nAdmin    : " + (admin.isEmpty() ? "(unchanged)" : admin));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save settings:\n" + e.getMessage());
        }
    }

    /**
     * Logout — closes the dashboard window and reopens the login screen.
     * Mapped to onAction="#onLogout" on both the sidebar button and the
     * Settings form logout button.
     */
    @FXML
    public void onLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("Any unsaved changes will be lost.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Open the login window
                    Parent loginRoot = FXMLLoader.load(
                        getClass().getResource("primary.fxml"));
                    Stage loginStage = new Stage();
                    loginStage.setScene(new Scene(loginRoot));
                    loginStage.setTitle("CafeMaster - Login");
                    loginStage.show();
                    // Close the dashboard window
                    Stage dashStage = (Stage) ((javafx.scene.Node) event.getSource())
                        .getScene().getWindow();
                    dashStage.close();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Logout Error", e.getMessage());
                }
            }
        });
    }


    // =========================================================================
    //  HELPERS  (YOUR ORIGINAL showAlert — UNCHANGED)
    // =========================================================================
    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private int parseIntLabel(Label label) {
        try { return label != null ? Integer.parseInt(label.getText().trim()) : 0; }
        catch (NumberFormatException e) { return 0; }
    }

    private double parseDoubleLabel(Label label) {
        try {
            return label != null
                ? Double.parseDouble(label.getText().replace("₱", "").replace(",", "").trim())
                : 0.0;
        } catch (NumberFormatException e) { return 0.0; }
    }


    // =========================================================================
    //  INNER MODEL — OrderItem  (one row in the cart)
    // =========================================================================
    public static class OrderItem {
        private String item;
        private int    qty;
        private double unitPrice;

        public OrderItem(String item, int qty, double unitPrice) {
            this.item = item; this.qty = qty; this.unitPrice = unitPrice;
        }
        public String getItem()          { return item; }
        public int    getQty()           { return qty; }
        public void   setQty(int qty)    { this.qty = qty; }
        public double getUnitPrice()     { return unitPrice; }
    }

    // =========================================================================
    //  INNER MODEL — StaffMember  (one row in the staff table)
    // =========================================================================
    public static class StaffMember {
        private final String name, role, status;

        public StaffMember(String name, String role, String status) {
            this.name = name; this.role = role; this.status = status;
        }
        public String getName()   { return name; }
        public String getRole()   { return role; }
        public String getStatus() { return status; }
    }
}
