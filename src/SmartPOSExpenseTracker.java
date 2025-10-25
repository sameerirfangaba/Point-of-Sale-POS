import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;

public class SmartPOSExpenseTracker extends Application {

    // File paths
    private static final String PRODUCT_FILE = "products.txt";
    private static final String EXPENSE_FILE = "expenses.txt";

    // Observable lists for UI
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    // Labels to show totals
    private Label totalSalesLabel = new Label("$0.00");
    private Label totalExpenseLabel = new Label("$0.00");
    private Label profitLabel = new Label("$0.00");

    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // --- Data Model Classes ---
    public static class Product {
        private final IntegerProperty id;
        private final StringProperty name;
        private final DoubleProperty price;
        private final IntegerProperty stock;

        public Product(int id, String name, double price, int stock) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.stock = new SimpleIntegerProperty(stock);
        }

        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public double getPrice() { return price.get(); }
        public int getStock() { return stock.get(); }

        public void setName(String name) { this.name.set(name); }
        public void setPrice(double price) { this.price.set(price); }
        public void setStock(int stock) { this.stock.set(stock); }

        public IntegerProperty idProperty() { return id; }
        public StringProperty nameProperty() { return name; }
        public DoubleProperty priceProperty() { return price; }
        public IntegerProperty stockProperty() { return stock; }
    }

    public static class Expense {
        private final IntegerProperty id;
        private final StringProperty category;
        private final DoubleProperty amount;
        private final StringProperty date;

        public Expense(int id, String category, double amount, String date) {
            this.id = new SimpleIntegerProperty(id);
            this.category = new SimpleStringProperty(category);
            this.amount = new SimpleDoubleProperty(amount);
            this.date = new SimpleStringProperty(date);
        }

        public int getId() { return id.get(); }
        public String getCategory() { return category.get(); }
        public double getAmount() { return amount.get(); }
        public String getDate() { return date.get(); }

        public void setCategory(String category) { this.category.set(category); }
        public void setAmount(double amount) { this.amount.set(amount); }
        public void setDate(String date) { this.date.set(date); }

        public IntegerProperty idProperty() { return id; }
        public StringProperty categoryProperty() { return category; }
        public DoubleProperty amountProperty() { return amount; }
        public StringProperty dateProperty() { return date; }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load data from files
        loadProductsFromFile();
        loadExpensesFromFile();

        // Main layout with tabs
        TabPane tabPane = new TabPane();

        Tab posTab = new Tab("POS Billing");
        posTab.setContent(createPOSTabContent());
        posTab.setClosable(false);

        Tab expenseTab = new Tab("Expense Tracker");
        expenseTab.setContent(createExpenseTabContent());
        expenseTab.setClosable(false);

        Tab productMgmtTab = new Tab("Product Management");
        productMgmtTab.setContent(createProductMgmtTabContent());
        productMgmtTab.setClosable(false);

        Tab reportTab = new Tab("Reports");
        reportTab.setContent(createReportTabContent());
        reportTab.setClosable(false);

        tabPane.getTabs().addAll(posTab, expenseTab, productMgmtTab, reportTab);

        Scene scene = new Scene(tabPane, 1100, 750);

        primaryStage.setTitle("Smart POS & Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateTotals();
    }

    private Node createReportTabContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        Label reportLabel = new Label("Reports functionality coming soon...");
        reportLabel.setStyle("-fx-font-size: 16px;");
        
        root.getChildren().add(reportLabel);
        return root;
    }

    private VBox createPOSTabContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Products Table
        TableView<Product> table = new TableView<>(productList);
        table.setPrefHeight(300);
        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(cell -> cell.getValue().nameProperty());
        nameCol.setPrefWidth(250);

        TableColumn<Product, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cell -> cell.getValue().priceProperty());
        priceCol.setPrefWidth(100);

        TableColumn<Product, Number> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(cell -> cell.getValue().stockProperty());
        stockCol.setPrefWidth(80);

        table.getColumns().addAll(nameCol, priceCol, stockCol);

        // Add to cart button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No product selected", "Please select a product to add.");
                return;
            }
            // Logic to add to cart (not implemented in this example)
            updateTotals();
        });

        // Totals display
        HBox totalsBox = new HBox(10, new Label("Total: "), totalSalesLabel);
        totalsBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(new Label("Product List:"), table, addToCartBtn, totalsBox);
        return root;
    }

    private VBox createExpenseTabContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Expense Table
        TableView<Expense> expenseTable = new TableView<>(expenseList);
        expenseTable.setPrefHeight(400);

        TableColumn<Expense, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        categoryCol.setPrefWidth(180);
        categoryCol.setCellFactory(TextFieldTableCell.forTableColumn());
        categoryCol.setOnEditCommit(e -> {
            Expense ex = e.getRowValue();
            ex.setCategory(e.getNewValue());
            updateExpense(ex);
        });

        TableColumn<Expense, Number> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> cell.getValue().amountProperty());
        amountCol.setPrefWidth(120);
        amountCol.setCellFactory(TextFieldTableCell.<Expense, Number>forTableColumn(new NumberStringConverter()));
        amountCol.setOnEditCommit(e -> {
            Expense ex = e.getRowValue();
            ex.setAmount(e.getNewValue().doubleValue());
            updateExpense(ex);
            updateTotals();
        });

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());
        dateCol.setPrefWidth(140);

        expenseTable.getColumns().addAll(categoryCol, amountCol, dateCol);
        expenseTable.setEditable(true);

        // Form to add expense
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(150);

        Button addExpenseBtn = new Button("Add Expense");
        addExpenseBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        addExpenseBtn.setOnAction(e -> {
            String cat = categoryField.getText().trim();
            String amtText = amountField.getText().trim();
            LocalDate date = datePicker.getValue();
            
            if (cat.isEmpty()) {
                showError("Input Error", "Category is required.");
                return;
            }
            
            double amt;
            try {
                amt = Double.parseDouble(amtText);
                if (amt < 0) {
                    showError("Input Error", "Amount cannot be negative.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Input Error", "Invalid amount.");
                return;
            }
            
            // Generate new ID
            int newId = expenseList.isEmpty() ? 1 : 
                       expenseList.stream().mapToInt(Expense::getId).max().orElse(0) + 1;
            
            addExpense(new Expense(newId, cat, amt, dateFormatter.format(date)));
            categoryField.clear();
            amountField.clear();
            datePicker.setValue(LocalDate.now());
            updateTotals();
        });

        HBox form = new HBox(10, 
            new Label("Category:"), categoryField, 
            new Label("Amount:"), amountField, 
            new Label("Date:"), datePicker, 
            addExpenseBtn);
        form.setAlignment(Pos.CENTER_LEFT);

        // Total expenses display
        HBox totalsBox = new HBox(10, new Label("Total Expenses:"), totalExpenseLabel);
        totalsBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(new Label("Expenses:"), expenseTable, form, totalsBox);
        return root;
    }

    private void addExpense(Expense expense) {
        expenseList.add(expense);
        saveExpensesToFile(); // Save to file
    }

    private void updateExpense(Expense expense) {
        // Update logic if needed
        saveExpensesToFile(); // Save to file
    }

    private VBox createProductMgmtTabContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Products Table (reuse productList)
        TableView<Product> table = new TableView<>(productList);
        table.setPrefHeight(500);
        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(cell -> cell.getValue().nameProperty());
        nameCol.setPrefWidth(300);
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            Product p = e.getRowValue();
            p.setName(e.getNewValue());
            updateProductInFile(p);
        });

        TableColumn<Product, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cell -> cell.getValue().priceProperty());
        priceCol.setPrefWidth(150);
        priceCol.setCellFactory(TextFieldTableCell.<Product, Number>forTableColumn(new NumberStringConverter()));
        priceCol.setOnEditCommit(e -> {
            Product p = e.getRowValue();
            p.setPrice(e.getNewValue().doubleValue());
            updateProductInFile(p);
        });

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(cell -> cell.getValue().stockProperty().asObject());
        stockCol.setPrefWidth(120);
        stockCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        stockCol.setOnEditCommit(e -> {
            Product p = e.getRowValue();
            if (e.getNewValue() < 0) {
                showError("Invalid Value", "Stock cannot be negative.");
                table.refresh();
                return;
            }
            p.setStock(e.getNewValue().intValue());
            updateProductInFile(p);
        });

        table.getColumns().addAll(nameCol, priceCol, stockCol);
        table.setEditable(true);

        // Add product section
        TextField addNameField = new TextField();
        addNameField.setPromptText("Product Name");
        TextField addPriceField = new TextField();
        addPriceField.setPromptText("Price");
        TextField addStockField = new TextField();
        addStockField.setPromptText("Stock");

        Button addProductBtn = new Button("Add Product");
        addProductBtn.setStyle("-fx-background-color: #009688; -fx-text-fill: white;");
        addProductBtn.setOnAction(e -> {
            String name = addNameField.getText().trim();
            String priceText = addPriceField.getText().trim();
            String stockText = addStockField.getText().trim();
            if (name.isEmpty()) {
                showError("Validation Error", "Product name is required.");
                return;
            }
            double price;
            int stock;
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) {
                    showError("Validation Error", "Price cannot be negative.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Validation Error", "Invalid price.");
                return;
            }
            try {
                stock = Integer.parseInt(stockText);
                if (stock < 0) {
                    showError("Validation Error", "Stock cannot be negative.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Validation Error", "Invalid stock quantity.");
                return;
            }

            // Generate new ID
            int newId = productList.isEmpty() ? 1 : 
                       productList.stream().mapToInt(Product::getId).max().orElse(0) + 1;

            addProductToFile(new Product(newId, name, price, stock));
            addNameField.clear();
            addPriceField.clear();
            addStockField.clear();
        });

        HBox addBox = new HBox(10, addNameField, addPriceField, addStockField, addProductBtn);
        addBox.setPadding(new Insets(10));
        addBox.setAlignment(Pos.CENTER_LEFT);

        // Delete product button
        Button deleteProductBtn = new Button("Delete Selected Product");
        deleteProductBtn.setStyle("-fx-background-color: tomato; -fx-text-fill: white;");
        deleteProductBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No selection", "Please select a product to delete.");
                return;
            }
            
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Product");
            confirmation.setHeaderText("Are you sure?");
            confirmation.setContentText("Do you want to delete the product: " + selected.getName() + "?");
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                productList.remove(selected);
                saveProductsToFile();
            }
        });

        root.getChildren().addAll(new Label("Product Management"), table, addBox, deleteProductBtn);
        return root;
    }

    private void addProductToFile(Product product) {
        productList.add(product);
        saveProductsToFile(); // Save to file
    }

    private void updateProductInFile(Product product) {
        // Update logic if needed
        saveProductsToFile(); // Save to file
    }

    private void saveProductsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRODUCT_FILE))) {
            for (Product product : productList) {
                writer.println(product.getId() + "," + product.getName() + "," + product.getPrice() + "," + product.getStock());
            }
        } catch (IOException e) {
            showError("Error saving products", e.getMessage());
        }
    }

    private void loadProductsFromFile() {
        productList.clear();
        File file = new File(PRODUCT_FILE);
        if (!file.exists()) {
            return; // File doesn't exist yet, that's okay
        }
        
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        double price = Double.parseDouble(parts[2]);
                        int stock = Integer.parseInt(parts[3]);
                        productList.add(new Product(id, name, price, stock));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist, that's okay for first run
        }
    }

    private void loadExpensesFromFile() {
        expenseList.clear();
        File file = new File(EXPENSE_FILE);
        if (!file.exists()) {
            return; // File doesn't exist yet, that's okay
        }
        
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String category = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        String date = parts[3];
                        expenseList.add(new Expense(id, category, amount, date));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist, that's okay for first run
        }
    }

    private void saveExpensesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EXPENSE_FILE))) {
            for (Expense expense : expenseList) {
                writer.println(expense.getId() + "," + expense.getCategory() + "," + expense.getAmount() + "," + expense.getDate());
            }
        } catch (IOException e) {
            showError("Error saving expenses", e.getMessage());
        }
    }

    private void updateTotals() {
        double totalSales = 0; // This can be calculated based on sales data if needed
        double totalExpenses = expenseList.stream().mapToDouble(Expense::getAmount).sum();
        totalExpenseLabel.setText(String.format("$%.2f", totalExpenses));

        double profit = totalSales - totalExpenses;
        profitLabel.setText(String.format("$%.2f", profit));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}