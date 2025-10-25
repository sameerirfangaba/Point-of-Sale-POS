import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.beans.property.SimpleStringProperty;
import java.util.stream.Collectors;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.converter.IntegerStringConverter;
import javafx.beans.property.SimpleDoubleProperty;
import java.util.Optional;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.geometry.Orientation;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.animation.*;
import javafx.util.Duration;

import java.io.*;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;

import javafx.util.converter.DoubleStringConverter;

public class SmartposApp extends Application {

    private static final String PRODUCTS_FILE = "products.txt";
    private static final String EXPENSES_FILE = "expenses.txt";
    private static final String SALES_FILE = "sales.txt";
    private static final String USERS_FILE = "users.txt";

    // Current logged in user
    private String currentUser;
    private String currentUserRole;
    private Stage primaryStage;

    // User credentials storage
    private HashMap<String, UserAccount> userAccounts = new HashMap<>();

    // User account class
    public static class UserAccount {
        private String username;
        private String password;
        private String fullName;
        private String role;
        private String email;
        private LocalDate lastLogin;

        public UserAccount(String username, String password, String fullName, String role, String email) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.role = role;
            this.email = email;
            this.lastLogin = LocalDate.now();
        }

        // Getters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        public String getEmail() { return email; }
        public LocalDate getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDate date) { this.lastLogin = date; }
    }

    // Data models
    public static class Product {
        private String name;
        private double price;
        private int stock;
        private String upc;

        public Product(String name, double price, int stock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.upc = "N/A"; // Default UPC if not provided
        }

        public Product(String name, String upc, double price, int stock) {
            this.name = name;
            this.upc = upc;
            this.price = price;
            this.stock = stock;
        }

        public String getUpc() { return upc; }
        public void setUpc(String upc) { this.upc = upc; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
    }

    public static class Sale {
        private String products;
        private String quantities;
        private double amount;
        private String date;
        private String employeeId;
        private String customerId;
        private String customerName;
        private String customerPhone;
        private double discount;
        private String paymentMethod;
        private String receiptId;

        public Sale(String product, int quantity, double amount, String date) {
            this(product, String.valueOf(quantity), amount, date, "", "", "", "", 0.0, "Cash", "RCPT-" + System.currentTimeMillis());
        }

        public Sale(String products, String quantities, double amount, String date, String employeeId, String customerId,
                    String customerName, String customerPhone, double discount, String paymentMethod, String receiptId) {
            this.products = products;
            this.quantities = quantities;
            this.amount = amount;
            this.date = date;
            this.employeeId = employeeId;
            this.customerId = customerId;
            this.customerName = customerName;
            this.customerPhone = customerPhone;
            this.discount = discount;
            this.paymentMethod = paymentMethod;
            this.receiptId = receiptId;
        }

        public String getProducts() { return products; }
        public String getQuantities() { return quantities; }
        public double getAmount() { return amount; }
        public String getDate() { return date; }
        public String getEmployeeId() { return employeeId; }
        public String getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public double getDiscount() { return discount; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getReceiptId() { return receiptId; }

    }

    public static class Expense {
        private String category;
        private double amount;
        private String date;
        private String description;

        public Expense(String category, double amount, String date, String description) {
            this.category = category;
            this.amount = amount;
            this.date = date;
            this.description = description;
        }

        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public String getDate() { return date; }
        public String getDescription() { return description; }

        public void setCategory(String category) { this.category = category; }
        public void setAmount(double amount) { this.amount = amount; }
        public void setDate(String date) { this.date = date; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class OrderItem {
        private Product product;
        private IntegerProperty quantity = new SimpleIntegerProperty();

        public OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity.set(quantity);
        }

        public Product getProduct() { return product; }
        public String getName() { return product.getName(); }
        public String getUpc() { return product.getUpc(); }
        public double getPrice() { return product.getPrice(); }
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int quantity) { this.quantity.set(quantity); }
        public IntegerProperty quantityProperty() { return quantity; }
        public double getTotal() { return product.getPrice() * getQuantity(); }
    }

    // Data storage
    private List<Product> products = new ArrayList<>();
    private List<Sale> sales = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();

    // UI Components
    private TabPane mainTabPane;
    private TableView<Product> productTable;
    private TableView<Sale> salesTable;
    private TableView<Expense> expenseTable;
    private Label totalSalesLabel;
    private Label totalExpensesLabel;
    private Label profitLabel;

    // POS Components
    private ComboBox<Product> productCombo;
    private TextField searchField;
    private TextField quantityField;
    private TextField priceField;
    private TableView<OrderItem> orderTable;
    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private Label subtotalLabel;
    private Label discountLabel;
    private Label totalLabel;
    private TextField discountField;
    private TextField employeeIdField;
    private TextField customerIdField;
    private TextField customerNameField;
    private TextField customerPhoneField;
    private RadioButton cashRadio;
    private RadioButton creditRadio;
    private TextField amountReceivedField;
    private Label changeLabel;
    private double discountAmount = 0.0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadThemePreference();
        loadUsersFromFile();

        // Initialize default admin account if no users exist
        if (userAccounts.isEmpty()) {
            userAccounts.put("admin", new UserAccount("admin", "admin123", "Administrator", "Admin", "admin@smartpos.com"));
            saveUsersToFile();
        }

        showLoginScreen();
    }

    private void showLoginScreen() {
        primaryStage.setTitle("Smart POS System - Login");

        // Create main container
        StackPane root = new StackPane();
        root.setPrefSize(1000, 700);

        // Create gradient background
        Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#667eea")),
                new Stop(1, Color.web("#764ba2"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill backgroundFill = new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        root.setBackground(background);

        // Create floating particles background
        for (int i = 0; i < 8; i++) {
            Circle particle = new Circle(Math.random() * 5 + 2);
            particle.setFill(Color.web("rgba(255, 255, 255, 0.1)"));
            particle.setLayoutX(Math.random() * 1000);
            particle.setLayoutY(Math.random() * 700);

            // Floating animation
            TranslateTransition tt = new TranslateTransition(Duration.seconds(10 + Math.random() * 10), particle);
            tt.setFromY(particle.getLayoutY());
            tt.setToY(particle.getLayoutY() - 100 - Math.random() * 200);
            tt.setCycleCount(Timeline.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();

            // Fade animation
            FadeTransition ft = new FadeTransition(Duration.seconds(3 + Math.random() * 4), particle);
            ft.setFromValue(0.1);
            ft.setToValue(0.3);
            ft.setCycleCount(Timeline.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();

            root.getChildren().add(0, particle);
        }

        // Create main login card
        VBox loginCard = new VBox(30);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(50));
        loginCard.setMaxWidth(450);
        loginCard.setMaxHeight(600);

        // Card background with rounded corners and shadow
        loginCard.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                        "-fx-background-radius: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0.3, 0, 10);"
        );

        // Create header section
        VBox header = new VBox(20);
        header.setAlignment(Pos.CENTER);

        // Logo/Icon
        Circle logoCircle = new Circle(40);
        logoCircle.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(1, Color.web("#764ba2"))
        ));

        // POS Icon using shapes
        VBox posIcon = new VBox(5);
        posIcon.setAlignment(Pos.CENTER);

        Rectangle screen = new Rectangle(20, 15);
        screen.setFill(Color.WHITE);
        screen.setArcWidth(3);
        screen.setArcHeight(3);

        Rectangle base = new Rectangle(25, 8);
        base.setFill(Color.WHITE);
        base.setArcWidth(2);
        base.setArcHeight(2);

        posIcon.getChildren().addAll(screen, base);

        StackPane logoContainer = new StackPane();
        logoContainer.getChildren().addAll(logoCircle, posIcon);

        // Title
        Label titleLabel = new Label("Smart POS System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#333333"));

        // Subtitle
        Label subtitleLabel = new Label("Point of Sale Management");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#666666"));

        header.getChildren().addAll(logoContainer, titleLabel, subtitleLabel);

        // Create form section
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);

        // Username field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        usernameLabel.setTextFill(Color.web("#555555"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(45);
        usernameField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 12px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-color: white;" +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);"
        );

        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        passwordLabel.setTextFill(Color.web("#555555"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 12px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-color: white;" +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);"
        );

        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Login button
        Button loginButton = new Button("LOGIN");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(50);
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        loginButton.setTextFill(Color.WHITE);
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                        "-fx-background-radius: 25px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 2);"
        );

        // Add hover effect
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #5a6fd8, #6a42a0);" +
                            "-fx-background-radius: 25px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0.4, 0, 4);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(100), loginButton);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });

        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                            "-fx-background-radius: 25px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 2);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(100), loginButton);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Message label for feedback
        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        // Login button action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both username and password.");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                return;
            }

            // Add loading animation to button
            loginButton.setText("LOGGING IN...");
            loginButton.setDisable(true);

            if (validateLogin(username, password)) {
                UserAccount user = userAccounts.get(username);
                currentUser = user.getFullName();
                currentUserRole = user.getRole();
                user.setLastLogin(LocalDate.now());
                saveUsersToFile();

                // Show success message
                messageLabel.setText("Login successful! Loading application...");
                messageLabel.setTextFill(Color.web("#27ae60"));

                // Transition to main application
                FadeTransition fadeOut = new FadeTransition(Duration.millis(600), loginCard);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> showMainApplication());
                fadeOut.play();
            } else {
                // Reset button
                loginButton.setText("LOGIN");
                loginButton.setDisable(false);

                messageLabel.setText("Invalid username or password. Please try again.");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                // Shake animation for error
                TranslateTransition tt = new TranslateTransition(Duration.millis(50), messageLabel);
                tt.setCycleCount(6);
                tt.setAutoReverse(true);
                tt.setFromX(0);
                tt.setToX(5);
                tt.play();
            }
        });

        // Enter key support
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });

        form.getChildren().addAll(usernameBox, passwordBox, loginButton, messageLabel);

        // Create footer section
        VBox footer = new VBox(10);
        footer.setAlignment(Pos.CENTER);

        // Forgot password link
        Hyperlink forgotLink = new Hyperlink("Forgot Password?");
        forgotLink.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        forgotLink.setTextFill(Color.web("#667eea"));
        forgotLink.setOnAction(e -> {
            messageLabel.setText("Password reset functionality would be implemented here.");
            messageLabel.setTextFill(Color.web("#667eea"));
        });

        // Divider
        Separator separator = new Separator();
        separator.setMaxWidth(200);
        separator.setStyle("-fx-background-color: #eee;");

        // Version info
        Label versionLabel = new Label("Version 1.0.0 | © 2024 Smart POS Systems");
        versionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        versionLabel.setTextFill(Color.web("#999999"));

        footer.getChildren().addAll(forgotLink, separator, versionLabel);

        // Add all sections to login card
        loginCard.getChildren().addAll(header, form, footer);

        // Add card to root with center alignment
        root.getChildren().add(loginCard);
        StackPane.setAlignment(loginCard, Pos.CENTER);

        // Create scene and setup stage
        Scene loginScene = new Scene(root, 1000, 700);
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Add entrance animation
        loginCard.setScaleX(0.8);
        loginCard.setScaleY(0.8);
        loginCard.setOpacity(0);

        // Scale animation
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(600), loginCard);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        // Fade animation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(600), loginCard);
        fadeTransition.setToValue(1.0);

        // Play animations
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.setDelay(Duration.millis(200));
        parallelTransition.play();
    }

    private void showMainApplication() {
        initializeData();

        primaryStage.setTitle("Smart POS & Expense Tracker");
        primaryStage.setMaximized(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        HBox header = createHeader();
        root.setTop(header);

        mainTabPane = createMainContent();
        root.setCenter(mainTabPane);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        applyTheme();
        updateDashboard();
    }

    private void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return;
        }

        userAccounts.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String username = parts[0];
                    String password = parts[1];
                    String fullName = parts[2];
                    String role = parts[3];
                    String email = parts[4];

                    UserAccount user = new UserAccount(username, password, fullName, role, email);
                    if (parts.length >= 6) {
                        try {
                            user.setLastLogin(LocalDate.parse(parts[5]));
                        } catch (Exception e) {
                            // Use default date if parsing fails
                        }
                    }

                    userAccounts.put(username, user);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Users file not found: " + e.getMessage());
        }
    }
    private boolean validateLogin(String username, String password) {
        if (userAccounts != null && userAccounts.containsKey(username)) {
            UserAccount user = userAccounts.get(username);
            return user.getPassword().equals(password);
        }
        return false;
    }
    private void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (UserAccount user : userAccounts.values()) {
                writer.println(
                        user.getUsername() + "," +
                                user.getPassword() + "," +
                                user.getFullName() + "," +
                                user.getRole() + "," +
                                user.getEmail() + "," +
                                user.getLastLogin()
                );
            }
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #34495e); " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        // Logo and Title Section
        HBox logoSection = new HBox(10);
        logoSection.setAlignment(Pos.CENTER_LEFT);

        // POS Icon (using emoji)
        Label logoIcon = new Label("\uD83D\uDECD"); // Shopping cart emoji
        logoIcon.setStyle("-fx-font-size: 28; -fx-text-fill: #3498db;");

        // Title
        Label title = new Label("Smart POS & Inventory Tracker");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");

        logoSection.getChildren().addAll(logoIcon, title);

        // Spacer to push stats to center
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Quick Stats Section
        HBox quickStats = new HBox(20);
        quickStats.setAlignment(Pos.CENTER);

        // Calculate dynamic values
        double todaySales = sales.stream()
                .filter(sale -> sale.getDate().equals(LocalDate.now().toString()))
                .mapToDouble(Sale::getAmount)
                .sum();

        int lowStockCount = (int) products.stream()
                .filter(product -> product.getStock() < 10)
                .count();

        // Today's Sales Card
        VBox todaySalesBox = createStatCard("📈", String.format("Rs %.2f", todaySales), "Today's Sales", "#27ae60");

        // Products Count Card
        VBox productsBox = createStatCard("📦", String.valueOf(products.size()), "Products", "#3498db");

        // Low Stock Card (with warning color if count > 0)
        String lowStockColor = lowStockCount > 0 ? "#e74c3c" : "#f39c12";
        VBox lowStockBox = createStatCard("⚠️", String.valueOf(lowStockCount), "Low Stock", lowStockColor);

        quickStats.getChildren().addAll(todaySalesBox, productsBox, lowStockBox);

        // Spacer to push user section to right
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Right Section (Theme, Date, User)
        HBox rightSection = new HBox(15);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        // Theme Toggle Section
        HBox themeSection = new HBox(10);
        themeSection.setAlignment(Pos.CENTER);

        // Sun icon for light theme
        Label sunIcon = new Label("☀️");
        sunIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");

        themeToggle = new ToggleButton();
        themeToggle.setSelected(isDarkTheme);
        themeToggle.setText(isDarkTheme ? "Dark" : "Light");
        themeToggle.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10; " +
                "-fx-background-radius: 15; -fx-cursor: hand;");

        // Moon icon for dark theme
        Label moonIcon = new Label("🌙");
        moonIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");

        themeToggle.setOnAction(e -> {
            isDarkTheme = themeToggle.isSelected();
            themeToggle.setText(isDarkTheme ? "Dark" : "Light");
            applyTheme();
            saveThemePreference();
        });

        themeSection.getChildren().addAll(sunIcon, themeToggle, moonIcon);

        // Date Section
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")));
        dateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        // Logout Button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10; " +
                "-fx-background-radius: 15; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Logout Confirmation");
            confirmAlert.setHeaderText("Are you sure you want to logout?");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                currentUser = null;
                currentUserRole = null;
                showLoginScreen();
            }
        });

        // User Management Button (only visible for admin)
        if ("Admin".equals(currentUserRole)) {
            Button userMgmtBtn = new Button("Manage Users");
            userMgmtBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                    "-fx-text-fill: white; -fx-font-size: 12; -fx-padding: 5 10; " +
                    "-fx-background-radius: 15; -fx-cursor: hand;");
            userMgmtBtn.setOnAction(e -> showUserManagement());
            rightSection.getChildren().add(userMgmtBtn);
        }

        // User Info
        Label userLabel = new Label(currentUser + " (" + currentUserRole + ")");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        rightSection.getChildren().addAll(themeSection, dateLabel, logoutBtn, userLabel);

        // Add all sections to header
        header.getChildren().addAll(logoSection, spacer1, quickStats, spacer2, rightSection);

        return header;
    }

    private VBox createStatCard(String icon, String value, String title, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 10; -fx-border-radius: 10;");

        HBox content = new HBox(8);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18;");

        VBox textBox = new VBox(2);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #bdc3c7;");

        textBox.getChildren().addAll(valueLabel, titleLabel);
        content.getChildren().addAll(iconLabel, textBox);
        card.getChildren().add(content);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                    "-fx-background-radius: 10; -fx-border-radius: 10;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                    "-fx-background-radius: 10; -fx-border-radius: 10;");
        });

        return card;
    }


    private void showUserManagement() {
        // Only allow admin users to access user management
        if (!currentUserRole.equals("Admin")) {
            showAlert("Access Denied", "Only administrators can access user management.");
            return;
        }

        Stage userManagementStage = new Stage();
        userManagementStage.setTitle("User Management");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("User Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // User table
        TableView<UserAccount> userTable = new TableView<>();
        userTable.setPrefHeight(300);

        TableColumn<UserAccount, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<UserAccount, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        fullNameCol.setPrefWidth(200);

        TableColumn<UserAccount, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        TableColumn<UserAccount, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<UserAccount, LocalDate> lastLoginCol = new TableColumn<>("Last Login");
        lastLoginCol.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        lastLoginCol.setPrefWidth(150);

        userTable.getColumns().addAll(usernameCol, fullNameCol, roleCol, emailCol, lastLoginCol);
        userTable.getItems().addAll(userAccounts.values());

        // Add user form
        GridPane addUserForm = new GridPane();
        addUserForm.setHgap(15);
        addUserForm.setVgap(15);
        addUserForm.setPadding(new Insets(20));
        addUserForm.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5;");

        Label formTitle = new Label("Add New User");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        GridPane.setColumnSpan(formTitle, 2);

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Username");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Password");

        TextField newFullNameField = new TextField();
        newFullNameField.setPromptText("Full Name");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Admin", "Manager", "Cashier");
        roleCombo.setValue("Cashier");

        TextField newEmailField = new TextField();
        newEmailField.setPromptText("Email");

        Button addUserButton = new Button("Add User");
        addUserButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        addUserButton.setOnAction(e -> {
            String username = newUsernameField.getText().trim();
            String password = newPasswordField.getText();
            String fullName = newFullNameField.getText().trim();
            String role = roleCombo.getValue();
            String email = newEmailField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                showAlert("Error", "Please fill all fields!");
                return;
            }

            if (userAccounts.containsKey(username)) {
                showAlert("Error", "Username already exists!");
                return;
            }

            UserAccount newUser = new UserAccount(username, password, fullName, role, email);
            userAccounts.put(username, newUser);
            userTable.getItems().add(newUser);
            saveUsersToFile();

            // Clear fields
            newUsernameField.clear();
            newPasswordField.clear();
            newFullNameField.clear();
            newEmailField.clear();
            roleCombo.setValue("Cashier");

            showAlert("Success", "User added successfully!");
        });

        addUserForm.add(formTitle, 0, 0);
        addUserForm.add(new Label("Username:"), 0, 1);
        addUserForm.add(newUsernameField, 1, 1);
        addUserForm.add(new Label("Password:"), 0, 2);
        addUserForm.add(newPasswordField, 1, 2);
        addUserForm.add(new Label("Full Name:"), 0, 3);
        addUserForm.add(newFullNameField, 1, 3);
        addUserForm.add(new Label("Role:"), 0, 4);
        addUserForm.add(roleCombo, 1, 4);
        addUserForm.add(new Label("Email:"), 0, 5);
        addUserForm.add(newEmailField, 1, 5);
        addUserForm.add(addUserButton, 1, 6);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        closeButton.setOnAction(e -> userManagementStage.close());

        root.getChildren().addAll(title, userTable, addUserForm, closeButton);

        Scene scene = new Scene(root, 850, 700);
        userManagementStage.setScene(scene);
        userManagementStage.show();
    }

    private VBox createHeaderStatBox(String label, String value, String icon) {
        VBox statBox = new VBox(2);
        statBox.setAlignment(Pos.CENTER);
        statBox.setPadding(new Insets(5, 15, 5, 15));
        statBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 5; -fx-cursor: hand;");

        HBox contentBox = new HBox(5);
        contentBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16;");

        VBox textBox = new VBox(0);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10;");

        textBox.getChildren().addAll(valueLabel, labelText);
        contentBox.getChildren().addAll(iconLabel, textBox);
        statBox.getChildren().add(contentBox);

        statBox.setOnMouseEntered(e -> {
            statBox.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.2); " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand;"
            );
        });

        statBox.setOnMouseExited(e -> {
            statBox.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand;"
            );
        });

        statBox.setOnMouseClicked(e -> {
            if (label.equals("Low Stock")) {
                mainTabPane.getSelectionModel().select(2);
            } else if (label.contains("Sales")) {
                mainTabPane.getSelectionModel().select(5);
            }
        });

        return statBox;
    }

    private Separator createSeparator() {
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.2);");
        return separator;
    }

    private VBox createQuickStat(String label, String value, String icon) {
        VBox statBox = new VBox(2);
        statBox.setAlignment(Pos.CENTER);
        statBox.setPadding(new Insets(5, 15, 5, 15));
        statBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 5;");

        HBox contentBox = new HBox(5);
        contentBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16;");

        VBox textBox = new VBox(0);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10;");

        textBox.getChildren().addAll(valueLabel, labelText);
        contentBox.getChildren().addAll(iconLabel, textBox);
        statBox.getChildren().add(contentBox);

        return statBox;
    }

    private void updateToggleButtonStyle() {
        if (isDarkTheme) {
            themeToggle.setStyle("""
            -fx-background-color: #64b5f6;
            -fx-text-fill: #000000;
            -fx-border-color: #ffffff;
            -fx-border-width: 1px;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-padding: 5 15 5 15;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            """);
        } else {
            themeToggle.setStyle("""
            -fx-background-color: #34495e;
            -fx-text-fill: white;
            -fx-border-color: #64b5f6;
            -fx-border-width: 1px;
            -fx-border-radius: 15px;
            -fx-background-radius: 15px;
            -fx-padding: 5 15 5 15;
            -fx-font-size: 12px;
            """);
        }
    }

    private void applyTheme() {
        Scene scene = mainTabPane.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();

            if (isDarkTheme) {
                scene.getRoot().setStyle("-fx-base: #2d2d2d; -fx-background: #1e1e1e;");
                applyDarkThemeToComponents();
                updateHeaderForTheme("#1a252f");
            } else {
                scene.getRoot().setStyle("-fx-base: #ffffff;");
                applyLightThemeToComponents();
                updateHeaderForTheme("#2c3e50");
            }

            updateAllComponentsTheme();
        }
    }

    private void updateAllComponentsTheme() {
        if (productTable != null) {
            updateTableTheme(productTable);
        }
        if (expenseTable != null) {
            updateTableTheme(expenseTable);
        }
        if (salesTable != null) {
            updateTableTheme(salesTable);
        }
        updateStatsCardsTheme();
    }

    private void updateTableTheme(TableView<?> table) {
        if (isDarkTheme) {
            table.setStyle("-fx-background-color: #2d2d2d; -fx-control-inner-background: #2d2d2d; " +
                    "-fx-control-inner-background-alt: #353535; -fx-text-background-color: #ffffff;");
        } else {
            table.setStyle("-fx-background-color: #ffffff; -fx-control-inner-background: #ffffff; " +
                    "-fx-control-inner-background-alt: #f8f9fa; -fx-text-background-color: #333333;");
        }
        table.refresh();
    }

    private void updateHeaderForTheme(String backgroundColor) {
        if (mainTabPane != null && mainTabPane.getScene() != null) {
            Scene scene = mainTabPane.getScene();
            BorderPane root = (BorderPane) scene.getRoot();
            HBox header = (HBox) root.getTop();
            if (header != null) {
                if (isDarkTheme) {
                    header.setStyle("-fx-background-color: linear-gradient(to right, #1a252f, #2c3e50); -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);");
                } else {
                    header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #34495e); -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
                }
            }
        }
    }

    private TabPane createMainContent() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(createDashboard());

        Tab posTab = new Tab("POS Billing");
        posTab.setClosable(false);
        posTab.setContent(createPOSView());

        Tab inventoryTab = new Tab("Inventory");
        inventoryTab.setClosable(false);
        inventoryTab.setContent(createInventoryView());

        Tab expensesTab = new Tab("Expenses");
        expensesTab.setClosable(false);
        expensesTab.setContent(createExpensesView());

        Tab reportsTab = new Tab("Reports");
        reportsTab.setClosable(false);
        reportsTab.setContent(createReportsView());

        Tab historyTab = new Tab("Billing History");
        historyTab.setClosable(false);
        historyTab.setContent(createBillingHistoryView());

        tabPane.getTabs().addAll(dashboardTab, posTab, inventoryTab, expensesTab, reportsTab, historyTab);
        return tabPane;
    }

    private VBox createDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: white;");

        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox statsBox = new HBox(20);

        VBox salesCard = createStatsCard("Total Sales", "Rs 0.00", "#27ae60");
        totalSalesLabel = (Label) salesCard.getChildren().get(1);

        VBox expensesCard = createStatsCard("Total Expenses", "Rs 0.00", "#e74c3c");
        totalExpensesLabel = (Label) expensesCard.getChildren().get(1);

        VBox profitCard = createStatsCard("Net Profit", "Rs 0.00", "#3498db");
        profitLabel = (Label) profitCard.getChildren().get(1);

        VBox stockCard = createStatsCard("Low Stock Items", "0", "#f39c12");

        statsBox.getChildren().addAll(salesCard, expensesCard, profitCard, stockCard);

        LineChart<Number, Number> chart = createSalesChart();

        dashboard.getChildren().addAll(title, statsBox, chart);
        return dashboard;
    }

    private VBox createStatsCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("stats-card");

        String backgroundColor = isDarkTheme ? "#2d2d2d" : "#ffffff";
        String textColor = isDarkTheme ? "#ffffff" : "#666666";
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color +
                "; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private void updateStatsCardsTheme() {
        updateDashboard();
    }

    private LineChart<Number, Number> createSalesChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        yAxis.setLabel("Sales (Rs)");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Daily Sales Trend");
        chart.setPrefHeight(300);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Sales");

        for (int i = 1; i <= 30; i++) {
            series.getData().add(new XYChart.Data<>(i, Math.random() * 10000));
        }

        chart.getData().add(series);
        return chart;
    }

    private PieChart createExpenseCategoryPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Utilities", 400),
                new PieChart.Data("Supplies", 300),
                new PieChart.Data("Salaries", 800),
                new PieChart.Data("Maintenance", 200)
        );

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Expense Breakdown");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        return pieChart;
    }

    private VBox createPOSView() {
        VBox posView = new VBox(15);
        posView.setPadding(new Insets(15));
        posView.setStyle("-fx-background-color: white;");

        Label title = new Label("POS Billing System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        searchField = new TextField();
        searchField.setPromptText("Search product by name or UPC");
        searchField.setPrefWidth(300);

        productCombo = new ComboBox<>();
        productCombo.setPrefWidth(320);
        productCombo.setPromptText("Select product");

        productCombo.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product prod, boolean empty) {
                super.updateItem(prod, empty);
                setText((empty || prod == null) ? null : prod.getName() + " (" + prod.getUpc() + ")");
            }
        });
        productCombo.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product prod, boolean empty) {
                super.updateItem(prod, empty);
                setText((empty || prod == null) ? null : prod.getName() + " (" + prod.getUpc() + ")");
            }
        });
        updateProductComboBox();

        priceField = new TextField();
        priceField.setPromptText("Price per unit");
        priceField.setEditable(false);
        priceField.setPrefWidth(120);

        quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setPrefWidth(100);

        orderTable = new TableView<>(orderItems);
        orderTable.setPrefHeight(250);
        orderTable.setEditable(true);

        TableColumn<OrderItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        nameCol.setPrefWidth(180);

        TableColumn<OrderItem, String> upcCol = new TableColumn<>("UPC");
        upcCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUpc()));
        upcCol.setPrefWidth(120);

        TableColumn<OrderItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cell -> cell.getValue().quantityProperty().asObject());
        quantityCol.setPrefWidth(100);
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityCol.setOnEditCommit(event -> {
            OrderItem orderItem = event.getRowValue();
            int newQty = event.getNewValue();
            if (newQty <= 0) {
                showAlert("Quantity Error", "Quantity must be at least 1.");
                orderTable.refresh();
                return;
            }
            if (newQty > orderItem.getProduct().getStock()) {
                showAlert("Stock Error", "Quantity exceeds available stock.");
                orderTable.refresh();
                return;
            }
            orderItem.setQuantity(newQty);
            updateTransactionTotals();
        });

        TableColumn<OrderItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(100);

        TableColumn<OrderItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getTotal()).asObject());
        totalCol.setPrefWidth(120);

        TableColumn<OrderItem, Void> actionCol = new TableColumn<>("Remove");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(param -> new TableCell<OrderItem, Void>() {
            private final Button removeBtn = new Button("Remove");

            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                removeBtn.setOnAction(event -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    orderItems.remove(item);
                    updateTransactionTotals();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        orderTable.getColumns().addAll(nameCol, upcCol, quantityCol, priceCol, totalCol, actionCol);

        Button addToOrderBtn = new Button("Add to Order");
        addToOrderBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        addToOrderBtn.setOnAction(e -> addProductToOrder());

        discountField = new TextField();
        discountField.setPromptText("Discount in Rs");
        discountField.setPrefWidth(120);

        Button applyDiscountBtn = new Button("Apply Discount");
        applyDiscountBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        applyDiscountBtn.setOnAction(e -> applyDiscount());

        HBox discountBox = new HBox(10, discountField, applyDiscountBtn);
        discountBox.setAlignment(Pos.CENTER_LEFT);

        subtotalLabel = new Label("Subtotal: Rs 0.00");
        discountLabel = new Label("Discount: Rs 0.00");
        totalLabel = new Label("Total: Rs 0.00");

        subtotalLabel.setStyle("-fx-font-weight: bold;");
        discountLabel.setStyle("-fx-font-weight: bold;");
        totalLabel.setStyle("-fx-font-weight: bold;");

        VBox transactionBox = new VBox(5, subtotalLabel, discountLabel, totalLabel);
        transactionBox.setPadding(new Insets(10));
        transactionBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label empCustLabel = new Label("Employee & Customer Information");
        empCustLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));

        employeeIdField = new TextField();
        employeeIdField.setPromptText("Employee ID");
        employeeIdField.setPrefWidth(120);

        customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID");
        customerIdField.setPrefWidth(120);

        customerNameField = new TextField();
        customerNameField.setPromptText("Full Name");
        customerNameField.setPrefWidth(180);

        customerPhoneField = new TextField();
        customerPhoneField.setPromptText("Phone Number");
        customerPhoneField.setPrefWidth(150);

        HBox empCustBox = new HBox(15, employeeIdField, customerIdField, customerNameField, customerPhoneField);

        ToggleGroup paymentGroup = new ToggleGroup();
        cashRadio = new RadioButton("Cash");
        cashRadio.setToggleGroup(paymentGroup);
        cashRadio.setSelected(true);
        creditRadio = new RadioButton("Credit Card");
        creditRadio.setToggleGroup(paymentGroup);

        HBox paymentMethodBox = new HBox(15, new Label("Payment Method:"), cashRadio, creditRadio);
        paymentMethodBox.setAlignment(Pos.CENTER_LEFT);

        amountReceivedField = new TextField();
        amountReceivedField.setPromptText("Amount Received");
        amountReceivedField.setPrefWidth(140);

        changeLabel = new Label("Change: Rs 0.00");
        changeLabel.setStyle("-fx-font-weight: bold;");

        amountReceivedField.textProperty().addListener((obs, oldVal, newVal) -> calculateChange());
        cashRadio.setOnAction(e -> updateAmountReceivedState());
        creditRadio.setOnAction(e -> updateAmountReceivedState());

        HBox paymentDetailsBox = new HBox(15, amountReceivedField, changeLabel);
        paymentDetailsBox.setAlignment(Pos.CENTER_LEFT);

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        checkoutBtn.setOnAction(e -> processCheckout());

        HBox inputRow = new HBox(10, searchField, productCombo, quantityField, priceField, addToOrderBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        posView.getChildren().addAll(title, inputRow, orderTable, discountBox, transactionBox,
                empCustLabel, empCustBox, paymentMethodBox, paymentDetailsBox, checkoutBtn);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts(newVal));
        productCombo.setOnAction(e -> {
            Product selected = productCombo.getValue();
            priceField.setText(selected != null ? String.format("%.2f", selected.getPrice()) : "");
        });

        updateAmountReceivedState();

        return posView;
    }
    private VBox createBillingHistoryView() {
        VBox historyView = new VBox(24);
        historyView.setPadding(new Insets(24));
        historyView.setStyle("-fx-background-color: white;");

        Label title = new Label("Billing History");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        salesTable = new TableView<>();
        salesTable.setEditable(false);
        salesTable.setPrefHeight(500);

        TableColumn<Sale, String> receiptIdCol = new TableColumn<>("Receipt ID");
        receiptIdCol.setCellValueFactory(new PropertyValueFactory<>("receiptId"));
        receiptIdCol.setPrefWidth(120);

        TableColumn<Sale, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        TableColumn<Sale, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(150);

        TableColumn<Sale, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        TableColumn<Sale, String> paymentCol = new TableColumn<>("Payment Method");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentCol.setPrefWidth(120);

        TableColumn<Sale, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(param -> new TableCell<Sale, Void>() {
            private final Button printBtn = new Button("View Details");

            {
                printBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                printBtn.setOnAction(event -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    showReceiptDetails(sale);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : printBtn);
            }
        });

        salesTable.getColumns().addAll(receiptIdCol, dateCol, customerCol, amountCol, paymentCol, actionCol);
        salesTable.getItems().addAll(sales);

        Label instructionLabel = new Label("💡 Tip: Click 'View Details' to see the complete receipt for a transaction.");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");

        historyView.getChildren().addAll(title, instructionLabel, salesTable);
        return historyView;
    }


    private void showReceiptDetails(Sale sale) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT DETAILS ===\n");
        receipt.append("Receipt ID: ").append(sale.getReceiptId()).append("\n");
        receipt.append("Date: ").append(LocalDate.parse(sale.getDate()).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).append("\n");
        receipt.append("Employee ID: ").append(sale.getEmployeeId()).append("\n");
        receipt.append("Customer: ").append(sale.getCustomerName()).append(" (ID: ")
                .append(sale.getCustomerId()).append(")\n");
        receipt.append("Phone: ").append(sale.getCustomerPhone()).append("\n\n");

        receipt.append(String.format("%-20s %-8s %-10s\n",
                "Item", "Qty", "Total"));

        // Split the products and quantities
        String[] productNames = sale.getProducts().split("\\|");
        String[] quantities = sale.getQuantities().split("\\|");

        for (int i = 0; i < productNames.length; i++) {
            receipt.append(String.format("%-20s %-8s %-10.2f\n",
                    productNames[i], quantities[i],
                    Double.parseDouble(quantities[i]) * getProductPrice(productNames[i])));
        }

        receipt.append("\n");
        receipt.append(String.format("Subtotal: Rs %.2f\n", sale.getAmount() + sale.getDiscount()));
        receipt.append(String.format("Discount: Rs %.2f\n", sale.getDiscount()));
        receipt.append(String.format("Total: Rs %.2f\n", sale.getAmount()));

        receipt.append("Payment Method: ").append(sale.getPaymentMethod()).append("\n");
        if (sale.getPaymentMethod().equals("Cash")) {
            receipt.append(String.format("Amount Received: Rs %.2f\n", sale.getAmount()));
            receipt.append(String.format("Change Given: Rs %.2f\n", 0.0));
        }
        receipt.append("\nThank you for shopping!");

        TextArea receiptArea = new TextArea(receipt.toString());
        receiptArea.setEditable(false);
        receiptArea.setWrapText(true);
        receiptArea.setPrefWidth(400);
        receiptArea.setPrefHeight(400);

        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Receipt Details");
        receiptAlert.setHeaderText("Receipt ID: " + sale.getReceiptId());
        receiptAlert.getDialogPane().setContent(receiptArea);
        receiptAlert.showAndWait();
    }
    private double getProductPrice(String productName) {
        for (Product p : products) {
            if (p.getName().equals(productName)) {
                return p.getPrice();
            }
        }
        return 0.0;
    }

    private void filterProducts(String filter) {
        productCombo.getItems().clear();
        if (filter == null || filter.isEmpty()) {
            productCombo.getItems().addAll(products);
        } else {
            String lowerCaseFilter = filter.toLowerCase();
            for (Product product : products) {
                if (product.getName().toLowerCase().contains(lowerCaseFilter) ||
                        product.getUpc().toLowerCase().contains(lowerCaseFilter)) {
                    productCombo.getItems().add(product);
                }
            }
        }
    }

    private void updateProductComboBox() {
        if (productCombo != null) {
            Product selectedValue = productCombo.getValue();
            productCombo.getItems().clear();
            productCombo.getItems().addAll(products);

            if (selectedValue != null && productCombo.getItems().contains(selectedValue)) {
                productCombo.setValue(selectedValue);
            }
        }
    }

    private void addProductToOrder() {
        Product selected = productCombo.getValue();
        if (selected == null) {
            showAlert("No Product Selected", "Please select a product to add.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText());
            if (qty <= 0) {
                showAlert("Invalid Quantity", "Quantity must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Quantity", "Please enter a valid quantity.");
            return;
        }
        if (qty > selected.getStock()) {
            showAlert("Insufficient Stock", "Quantity exceeds available stock.");
            return;
        }
        Optional<OrderItem> existing = orderItems.stream()
                .filter(item -> item.getProduct().equals(selected))
                .findFirst();
        if (existing.isPresent()) {
            OrderItem orderItem = existing.get();
            int newQty = orderItem.getQuantity() + qty;
            if (newQty > selected.getStock()) {
                showAlert("Insufficient Stock", "Total quantity exceeds available stock.");
                return;
            }
            orderItem.setQuantity(newQty);
            orderTable.refresh();
        } else {
            orderItems.add(new OrderItem(selected, qty));
        }
        quantityField.clear();
        productCombo.getSelectionModel().clearSelection();
        priceField.clear();
        searchField.clear();
        updateTransactionTotals();
    }

    private void applyDiscount() {
        try {
            String discText = discountField.getText().trim();
            discountAmount = discText.isEmpty() ? 0 : Double.parseDouble(discText);
            if (discountAmount < 0) {
                showAlert("Invalid Discount", "Discount cannot be negative.");
                discountAmount = 0;
                discountField.clear();
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Discount", "Please enter a valid number for discount.");
            discountField.clear();
            discountAmount = 0;
        }
        updateTransactionTotals();
    }

    private void updateTransactionTotals() {
        double subtotal = orderItems.stream().mapToDouble(OrderItem::getTotal).sum();
        subtotalLabel.setText(String.format("Subtotal: Rs %.2f", subtotal));
        discountLabel.setText(String.format("Discount: Rs %.2f", discountAmount));
        double total = Math.max(subtotal - discountAmount, 0);
        totalLabel.setText(String.format("Total: Rs %.2f", total));
        calculateChange();
    }

    private void updateAmountReceivedState() {
        boolean cashSelected = cashRadio.isSelected();
        amountReceivedField.setDisable(!cashSelected);
        if (!cashSelected) {
            amountReceivedField.clear();
            changeLabel.setText("Change: Rs 0.00");
        }
    }

    private void calculateChange() {
        if (!cashRadio.isSelected()) {
            changeLabel.setText("Change: Rs 0.00");
            return;
        }
        double total = Math.max(orderItems.stream().mapToDouble(OrderItem::getTotal).sum() - discountAmount, 0);
        double amountReceived;
        try {
            amountReceived = Double.parseDouble(amountReceivedField.getText());
        } catch (NumberFormatException e) {
            changeLabel.setText("Change: Rs 0.00");
            return;
        }
        double change = amountReceived - total;
        if (change < 0) {
            changeLabel.setText("Insufficient amount received");
        } else {
            changeLabel.setText(String.format("Change: Rs %.2f", change));
        }
    }

    private void processCheckout() {
        if (orderItems.isEmpty()) {
            showAlert("No Items", "Add some items before checkout.");
            return;
        }
        double total = Math.max(orderItems.stream().mapToDouble(OrderItem::getTotal).sum() - discountAmount, 0);
        if (cashRadio.isSelected()) {
            double amountReceived;
            try {
                amountReceived = Double.parseDouble(amountReceivedField.getText());
                if (amountReceived < total) {
                    showAlert("Payment Error", "Amount received is less than total due.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Payment Error", "Invalid amount received for cash payment.");
                return;
            }
        }
        String todayStr = LocalDate.now().toString();
        String receiptId = "RCPT-" + System.currentTimeMillis(); // Unique receipt ID

        // Collect all product names and quantities
        StringBuilder productsStr = new StringBuilder();
        StringBuilder quantitiesStr = new StringBuilder();
        for (OrderItem item : orderItems) {
            if (productsStr.length() > 0) {
                productsStr.append("|");
                quantitiesStr.append("|");
            }
            productsStr.append(item.getProduct().getName());
            quantitiesStr.append(item.getQuantity());

            // Update product stock
            Product p = item.getProduct();
            p.setStock(p.getStock() - item.getQuantity());
        }

        // Create a single sale entry for the entire transaction
        Sale newSale = new Sale(
                productsStr.toString(),
                quantitiesStr.toString(),
                total,
                todayStr,
                employeeIdField.getText().trim(),
                customerIdField.getText().trim(),
                customerNameField.getText().trim(),
                customerPhoneField.getText().trim(),
                discountAmount,
                cashRadio.isSelected() ? "Cash" : "Credit Card",
                receiptId
        );

        sales.add(newSale);
        saveSalesToFile();
        saveProductsToFile();
        printReceipt(receiptId);

        // Update the sales table
        if (salesTable != null) {
            salesTable.getItems().add(newSale);
        }

        // Clear the form
        orderItems.clear();
        discountAmount = 0;
        discountField.clear();
        subtotalLabel.setText("Subtotal: Rs 0.00");
        discountLabel.setText("Discount: Rs 0.00");
        totalLabel.setText("Total: Rs 0.00");
        employeeIdField.clear();
        customerIdField.clear();
        customerNameField.clear();
        customerPhoneField.clear();
        amountReceivedField.clear();
        changeLabel.setText("Change: Rs 0.00");
        updateProductComboBox();
        updateDashboard();
        showAlert("Checkout Complete", "Transaction processed successfully.");
    }

    private void printReceipt(String receiptId) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append("Receipt ID: ").append(receiptId).append("\n");
        receipt.append("Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).append("\n");
        receipt.append("Employee ID: ").append(employeeIdField.getText().trim()).append("\n");
        receipt.append("Customer: ").append(customerNameField.getText().trim()).append(" (ID: ")
                .append(customerIdField.getText().trim()).append(")\n");
        receipt.append("Phone: ").append(customerPhoneField.getText().trim()).append("\n\n");

        receipt.append(String.format("%-20s %-12s %-8s %-8s %-10s\n",
                "Item", "UPC", "Qty", "Price", "Total"));

        for (OrderItem item : orderItems) {
            receipt.append(String.format("%-20s %-12s %-8d %-8.2f %-10.2f\n",
                    item.getName(), item.getUpc(), item.getQuantity(), item.getPrice(), item.getTotal()));
        }

        receipt.append("\n");
        receipt.append(String.format("Subtotal: Rs %.2f\n",
                orderItems.stream().mapToDouble(OrderItem::getTotal).sum()));
        receipt.append(String.format("Discount: Rs %.2f\n", discountAmount));
        receipt.append(String.format("Total: Rs %.2f\n",
                Math.max(orderItems.stream().mapToDouble(OrderItem::getTotal).sum() - discountAmount, 0)));

        if (cashRadio.isSelected()) {
            double total = Math.max(orderItems.stream().mapToDouble(OrderItem::getTotal).sum() - discountAmount, 0);
            double amountReceived = 0;
            try {
                amountReceived = Double.parseDouble(amountReceivedField.getText());
            } catch (Exception ignored) {}
            receipt.append(String.format("Amount Received: Rs %.2f\n", amountReceived));
            receipt.append(String.format("Change Given: Rs %.2f\n", amountReceived - total));
        } else {
            receipt.append("Payment Method: Credit Card\n");
        }
        receipt.append("\nThank you for shopping!");

        TextArea receiptArea = new TextArea(receipt.toString());
        receiptArea.setEditable(false);
        receiptArea.setWrapText(true);
        receiptArea.setPrefWidth(400);
        receiptArea.setPrefHeight(400);

        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Print Receipt");
        receiptAlert.setHeaderText("Receipt preview");
        receiptAlert.getDialogPane().setContent(receiptArea);
        receiptAlert.showAndWait();
    }

    private VBox createInventoryView() {
        VBox inventoryView = new VBox(20);
        inventoryView.setPadding(new Insets(20));
        inventoryView.setStyle("-fx-background-color: white;");

        Label title = new Label("Inventory Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox addProductForm = new HBox(10);
        addProductForm.setPadding(new Insets(15));
        addProductForm.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5;");

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        TextField upcField = new TextField();
        upcField.setPromptText("UPC");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock Quantity");

        Button addProductButton = new Button("Add Product");
        addProductButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        addProductForm.getChildren().addAll(nameField, upcField, priceField, stockField, addProductButton);

        productTable = new TableView<>();
        productTable.setEditable(true);

        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            String newName = event.getNewValue();
            if (newName != null && !newName.trim().isEmpty()) {
                product.setName(newName.trim());
                updateProductComboBox();
                saveProductsToFile();
                showAlert("Success", "Product name updated successfully!");
            } else {
                showAlert("Error", "Product name cannot be empty!");
                productTable.refresh();
            }
        });

        TableColumn<Product, String> upcCol = new TableColumn<>("UPC");
        upcCol.setCellValueFactory(new PropertyValueFactory<>("upc"));
        upcCol.setPrefWidth(120);
        upcCol.setCellFactory(TextFieldTableCell.forTableColumn());
        upcCol.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            String newUpc = event.getNewValue();
            if (newUpc != null && !newUpc.trim().isEmpty()) {
                product.setUpc(newUpc.trim());
                updateProductComboBox();
                saveProductsToFile();
                showAlert("Success", "UPC updated successfully!");
            } else {
                showAlert("Error", "UPC cannot be empty!");
                productTable.refresh();
            }
        });

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceCol.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Double newPrice = event.getNewValue();
            if (newPrice != null && newPrice >= 0) {
                product.setPrice(newPrice);
                saveProductsToFile();
                showAlert("Success", "Price updated successfully!");
            } else {
                showAlert("Error", "Price must be a positive number!");
                productTable.refresh();
            }
        });

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(120);
        stockCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        stockCol.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Integer newStock = event.getNewValue();
            if (newStock != null && newStock >= 0) {
                product.setStock(newStock);
                saveProductsToFile();
                showAlert("Success", "Stock updated successfully!");
            } else {
                showAlert("Error", "Stock must be a non-negative number!");
                productTable.refresh();
            }
        });

        TableColumn<Product, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    deleteProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        productTable.getColumns().addAll(nameCol, upcCol, priceCol, stockCol, actionCol);
        productTable.getItems().addAll(products);

        addProductButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showAlert("Error", "Product name cannot be empty!");
                    return;
                }

                String upc = upcField.getText().trim();
                if (upc.isEmpty()) {
                    upc = "N/A";
                }

                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    showAlert("Error", "Price cannot be negative!");
                    return;
                }

                int stock = Integer.parseInt(stockField.getText());
                if (stock < 0) {
                    showAlert("Error", "Stock cannot be negative!");
                    return;
                }

                Product newProduct = new Product(name, upc, price, stock);
                products.add(newProduct);
                productTable.getItems().add(newProduct);

                saveProductsToFile();
                updateProductComboBox();

                nameField.clear();
                upcField.clear();
                priceField.clear();
                stockField.clear();

                showAlert("Success", "Product added successfully!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter valid price and stock values!");
            }
        });

        Label instructionLabel = new Label("💡 Tip: Double-click on any cell in the table to edit it directly!");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");

        inventoryView.getChildren().addAll(title, addProductForm, instructionLabel, productTable);
        return inventoryView;
    }

    private void deleteProduct(Product product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Product");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("Do you want to delete the product: " + product.getName() + "?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                products.remove(product);
                productTable.getItems().remove(product);
                updateProductComboBox();
                saveProductsToFile();
                showAlert("Success", "Product deleted successfully!");
            }
        });
    }

    private VBox createExpensesView() {
        VBox expensesView = new VBox(20);
        expensesView.setPadding(new Insets(20));
        expensesView.setStyle("-fx-background-color: white;");

        Label title = new Label("Expense Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        GridPane expenseForm = new GridPane();
        expenseForm.setHgap(15);
        expenseForm.setVgap(15);
        expenseForm.setPadding(new Insets(20));
        expenseForm.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5;");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Rent", "Salary", "Electricity", "Water", "Internet", "Supplies", "Other");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button addExpenseButton = new Button("Add Expense");
        addExpenseButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        expenseForm.add(new Label("Category:"), 0, 0);
        expenseForm.add(categoryCombo, 1, 0);
        expenseForm.add(new Label("Amount:"), 0, 1);
        expenseForm.add(amountField, 1, 1);
        expenseForm.add(new Label("Description:"), 0, 2);
        expenseForm.add(descriptionField, 1, 2);
        expenseForm.add(new Label("Date:"), 0, 3);
        expenseForm.add(datePicker, 1, 3);
        expenseForm.add(addExpenseButton, 1, 4);

        expenseTable = new TableView<>();
        expenseTable.setEditable(true);

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(120);
        catCol.setCellFactory(param -> {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll("Rent", "Salary", "Electricity", "Water", "Internet", "Supplies", "Other");

            TableCell<Expense, String> cell = new TableCell<Expense, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                    }
                }
            };

            comboBox.setOnAction(e -> {
                Expense expense = cell.getTableRow().getItem();
                if (expense != null && comboBox.getValue() != null) {
                    expense.setCategory(comboBox.getValue());
                    saveExpensesToFile();
                    showAlert("Success", "Category updated successfully!");
                    updateDashboard();
                }
            });

            return cell;
        });

        TableColumn<Expense, Double> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amtCol.setPrefWidth(120);
        amtCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        amtCol.setOnEditCommit(event -> {
            Expense expense = event.getRowValue();
            Double newAmount = event.getNewValue();
            if (newAmount != null && newAmount >= 0) {
                expense.setAmount(newAmount);
                saveExpensesToFile();
                updateDashboard();
                showAlert("Success", "Amount updated successfully!");
            } else {
                showAlert("Error", "Amount must be a positive number!");
                expenseTable.refresh();
            }
        });

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setOnEditCommit(event -> {
            Expense expense = event.getRowValue();
            String newDescription = event.getNewValue();
            if (newDescription != null && !newDescription.trim().isEmpty()) {
                expense.setDescription(newDescription.trim());
                saveExpensesToFile();
                showAlert("Success", "Description updated successfully!");
            } else {
                showAlert("Error", "Description cannot be empty!");
                expenseTable.refresh();
            }
        });

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(140);
        dateCol.setCellFactory(param -> {
            DatePicker datePickerCell = new DatePicker();

            TableCell<Expense, String> cell = new TableCell<Expense, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        try {
                            LocalDate date = LocalDate.parse(item);
                            datePickerCell.setValue(date);
                        } catch (Exception e) {
                            datePickerCell.setValue(LocalDate.now());
                        }
                        setGraphic(datePickerCell);
                    }
                }
            };

            datePickerCell.setOnAction(e -> {
                Expense expense = cell.getTableRow().getItem();
                if (expense != null && datePickerCell.getValue() != null) {
                    expense.setDate(datePickerCell.getValue().toString());
                    saveExpensesToFile();
                    showAlert("Success", "Date updated successfully!");
                }
            });

            return cell;
        });

        TableColumn<Expense, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(param -> new TableCell<Expense, Void>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteBtn.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    deleteExpense(expense);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        expenseTable.getColumns().addAll(catCol, amtCol, descCol, dateCol, actionCol);
        expenseTable.getItems().addAll(expenses);

        addExpenseButton.setOnAction(e -> {
            try {
                String category = categoryCombo.getValue();
                if (category == null || category.trim().isEmpty()) {
                    showAlert("Error", "Please select a category!");
                    return;
                }

                String amountText = amountField.getText().trim();
                if (amountText.isEmpty()) {
                    showAlert("Error", "Please enter an amount!");
                    return;
                }

                double amount = Double.parseDouble(amountText);
                if (amount < 0) {
                    showAlert("Error", "Amount cannot be negative!");
                    return;
                }

                String description = descriptionField.getText().trim();
                if (description.isEmpty()) {
                    showAlert("Error", "Please enter a description!");
                    return;
                }

                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate == null) {
                    showAlert("Error", "Please select a date!");
                    return;
                }

                String date = selectedDate.toString();

                Expense newExpense = new Expense(category, amount, date, description);
                expenses.add(newExpense);
                expenseTable.getItems().add(newExpense);

                saveExpensesToFile();

                categoryCombo.setValue(null);
                amountField.clear();
                descriptionField.clear();
                datePicker.setValue(LocalDate.now());

                updateDashboard();
                showAlert("Success", "Expense added successfully!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid amount!");
            } catch (Exception ex) {
                showAlert("Error", "Please fill all fields correctly!");
            }
        });

        Label instructionLabel = new Label("💡 Tip: Use Category dropdown, double-click Amount/Description to edit, or click Date picker to modify directly!");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");

        expensesView.getChildren().addAll(title, expenseForm, instructionLabel, expenseTable);
        return expensesView;
    }

    private void deleteExpense(Expense expense) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Expense");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("Do you want to delete this expense: " + expense.getCategory() +
                " - Rs" + expense.getAmount() + "?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                expenses.remove(expense);
                expenseTable.getItems().remove(expense);
                updateDashboard();
                saveExpensesToFile();
                showAlert("Success", "Expense deleted successfully!");
            }
        });
    }

    private VBox createReportsView() {
        VBox reportsView = new VBox(20);
        reportsView.setPadding(new Insets(20));
        reportsView.setStyle("-fx-background-color: white;");

        Label title = new Label("Sales & Expense Reports");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("From Date");

        DatePicker toDate = new DatePicker();
        toDate.setPromptText("To Date");

        Button generateReportButton = new Button("Generate Report");
        generateReportButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        filterBox.getChildren().addAll(new Label("From:"), fromDate, new Label("To:"), toDate, generateReportButton);

        TextArea reportArea = new TextArea();
        reportArea.setPromptText("Reports will appear here...");
        reportArea.setPrefHeight(400);
        reportArea.setEditable(false);

        generateReportButton.setOnAction(e -> {
            LocalDate startDate = fromDate.getValue();
            LocalDate endDate = toDate.getValue();
            // Filter sales and expenses by date range if provided
            List<Sale> filteredSales = sales;
            List<Expense> filteredExpenses = expenses;
            if (startDate != null && endDate != null) {
                filteredSales = sales.stream()
                        .filter(s -> {
                            LocalDate saleDate = LocalDate.parse(s.getDate());
                            return !saleDate.isBefore(startDate) && !saleDate.isAfter(endDate);
                        })
                        .collect(Collectors.toList());
                filteredExpenses = expenses.stream()
                        .filter(event -> {
                            LocalDate expenseDate = LocalDate.parse(event.getDate());
                            return !expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate);
                        })
                        .collect(Collectors.toList());
            }

            double totalSales = filteredSales.stream().mapToDouble(Sale::getAmount).sum();
            double totalExpenses = filteredExpenses.stream().mapToDouble(Expense::getAmount).sum();
            double profit = totalSales - totalExpenses;

            StringBuilder report = new StringBuilder();
            report.append("=== FINANCIAL REPORT ===\n\n");

            if (startDate != null && endDate != null) {
                report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
            }

            report.append("Total Sales: Rs").append(String.format("%.2f", totalSales)).append("\n");
            report.append("Total Expenses: Rs").append(String.format("%.2f", totalExpenses)).append("\n");
            report.append("Net Profit: Rs").append(String.format("%.2f", profit)).append("\n\n");

            // Sales breakdown by product
            report.append("=== SALES BREAKDOWN ===\n");
            Map<String, Double> salesByProduct = filteredSales.stream()
                    .collect(Collectors.groupingBy(Sale::getProducts,
                            Collectors.summingDouble(Sale::getAmount)));

            salesByProduct.forEach((product, amount) -> {
                report.append(product).append(": Rs").append(String.format("%.2f", amount)).append("\n");
            });

            report.append("\n=== EXPENSE BREAKDOWN ===\n");
            Map<String, Double> expensesByCategory = filteredExpenses.stream()
                    .collect(Collectors.groupingBy(Expense::getCategory,
                            Collectors.summingDouble(Expense::getAmount)));

            expensesByCategory.forEach((category, amount) -> {
                report.append(category).append(": Rs").append(String.format("%.2f", amount)).append("\n");
            });

            reportArea.setText(report.toString());
        });

        reportsView.getChildren().addAll(title, filterBox, reportArea);
        return reportsView;
    }

    private void initializeData() {
        loadProductsFromFile();
        loadExpensesFromFile();
        loadSalesFromFile();

        if (products.isEmpty()) {
            System.out.println("Adding sample products...");
            products.add(new Product("Rice (1kg)", "0012345678905", 50.0, 100));
            products.add(new Product("Wheat Flour (1kg)", "0012345678906", 40.0, 80));
            products.add(new Product("Sugar (1kg)", "0012345678907", 45.0, 60));
            products.add(new Product("Cooking Oil (1L)", "0012345678908", 120.0, 50));
            products.add(new Product("Tea Packets", "0012345678909", 25.0, 200));
            saveProductsToFile();
        }

        if (expenses.isEmpty()) {
            System.out.println("Adding sample expenses...");
            expenses.add(new Expense("Rent", 15000.0, LocalDate.now().toString(), "Monthly shop rent"));
            expenses.add(new Expense("Electricity", 2500.0, LocalDate.now().toString(), "Monthly electricity bill"));
            expenses.add(new Expense("Salary", 20000.0, LocalDate.now().toString(), "Staff salary"));
            saveExpensesToFile();
        }

        if (products.isEmpty()) {
            System.out.println("Adding sample products...");
            products.add(new Product("Rice (1kg)", "0012345678905", 50.0, 100));
            products.add(new Product("Wheat Flour (1kg)", "0012345678906", 40.0, 80));
            products.add(new Product("Sugar (1kg)", "0012345678907", 45.0, 60));
            products.add(new Product("Cooking Oil (1L)", "0012345678908", 120.0, 50));
            products.add(new Product("Tea Packets", "0012345678909", 25.0, 200));
            saveProductsToFile();
        }
    }
    private void loadProductsFromFile() {
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) {
            System.out.println("Products file not found, starting with sample data");
            return;
        }

        products.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        String name = parts[0];
                        String upc = parts[1];
                        double price = Double.parseDouble(parts[2]);
                        int stock = Integer.parseInt(parts[3]);
                        products.add(new Product(name, upc, price, stock));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing product line: " + line);
                    }
                }
            }
            System.out.println("Products loaded successfully! Count: " + products.size());
        } catch (FileNotFoundException e) {
            System.err.println("Products file not found: " + e.getMessage());
        }
    }


    private void updateDashboard() {
        double totalSales = sales.stream().mapToDouble(Sale::getAmount).sum();
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double profit = totalSales - totalExpenses;

        if (totalSalesLabel != null) {
            totalSalesLabel.setText("Rs" + String.format("%.2f", totalSales));
            totalExpensesLabel.setText("Rs" + String.format("%.2f", totalExpenses));
            profitLabel.setText("Rs" + String.format("%.2f", profit));

            String color = profit >= 0 ? "#27ae60" : "#e74c3c";
            profitLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void saveProductsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRODUCTS_FILE))) {
            for (Product product : products) {
                writer.println(product.getName() + "," + product.getUpc() + "," + product.getPrice() + "," + product.getStock());
            }
            System.out.println("Products saved successfully!");
        } catch (IOException e) {
            showAlert("Error", "Failed to save products: " + e.getMessage());
        }
    }

    private void saveExpensesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EXPENSES_FILE))) {
            for (Expense expense : expenses) {
                writer.println(expense.getCategory() + "," + expense.getAmount() + "," +
                        expense.getDate() + "," + expense.getDescription());
            }
            System.out.println("Expenses saved successfully!");
        } catch (IOException e) {
            showAlert("Error", "Failed to save expenses: " + e.getMessage());
        }
    }

    private void saveSalesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SALES_FILE))) {
            for (Sale sale : sales) {
                writer.println(
                        sale.getProducts() + "," +
                                sale.getQuantities() + "," +
                                sale.getAmount() + "," +
                                sale.getDate() + "," +
                                sale.getEmployeeId() + "," +
                                sale.getCustomerId() + "," +
                                sale.getCustomerName() + "," +
                                sale.getCustomerPhone() + "," +
                                sale.getDiscount() + "," +
                                sale.getPaymentMethod() + "," +
                                sale.getReceiptId()
                );
            }
            System.out.println("Sales saved successfully!");
        } catch (IOException e) {
            showAlert("Error", "Failed to save sales: " + e.getMessage());
        }
    }


    private void loadSalesFromFile() {
        File file = new File(SALES_FILE);
        if (!file.exists()) {
            System.out.println("Sales file not found, starting with sample data");
            return;
        }

        sales.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 11) {
                    try {
                        String products = parts[0];
                        String quantities = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        String date = parts[3];
                        String employeeId = parts[4];
                        String customerId = parts[5];
                        String customerName = parts[6];
                        String customerPhone = parts[7];
                        double discount = Double.parseDouble(parts[8]);
                        String paymentMethod = parts[9];
                        String receiptId = parts[10];

                        sales.add(new Sale(
                                products, quantities, amount, date,
                                employeeId, customerId, customerName, customerPhone,
                                discount, paymentMethod, receiptId
                        ));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing sale line: " + line);
                    }
                }
            }
            System.out.println("Sales loaded successfully! Count: " + sales.size());
        } catch (FileNotFoundException e) {
            System.err.println("Sales file not found: " + e.getMessage());
        }
    }

    private void loadExpensesFromFile() {
        File file = new File(EXPENSES_FILE);
        if (!file.exists()) {
            System.out.println("Expenses file not found, starting with sample data");
            return;
        }

        expenses.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        String category = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        String date = parts[2];
                        String description = parts[3];
                        expenses.add(new Expense(category, amount, date, description));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing expense line: " + line);
                    }
                }
            }
            System.out.println("Expenses loaded successfully! Count: " + expenses.size());
        } catch (FileNotFoundException e) {
            System.err.println("Expenses file not found: " + e.getMessage());
        }
    }

    private boolean isDarkTheme = false;
    private ToggleButton themeToggle;
    private static final String THEME_FILE = "theme_preference.txt";

    private void saveThemePreference() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(THEME_FILE))) {
            writer.println(isDarkTheme ? "dark" : "light");
        } catch (IOException e) {
            System.err.println("Failed to save theme preference: " + e.getMessage());
        }
    }

    private void loadThemePreference() {
        File file = new File(THEME_FILE);
        if (!file.exists()) {
            isDarkTheme = false;
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) {
                String theme = scanner.nextLine().trim();
                isDarkTheme = "dark".equals(theme);
            }
        } catch (FileNotFoundException e) {
            isDarkTheme = false;
        }
    }

    private void applyDarkThemeToComponents() {
        if (mainTabPane == null) return;

        mainTabPane.setStyle("-fx-background-color: #2d2d2d;");

        for (Tab tab : mainTabPane.getTabs()) {
            tab.getContent().setStyle("-fx-background-color: #2d2d2d;");
            applyDarkThemeToNode(tab.getContent());
        }
    }

    private void applyLightThemeToComponents() {
        if (mainTabPane == null) return;

        mainTabPane.setStyle("-fx-background-color: #ffffff;");

        for (Tab tab : mainTabPane.getTabs()) {
            tab.getContent().setStyle("-fx-background-color: #ffffff;");
            applyLightThemeToNode(tab.getContent());
        }
    }

    private void applyDarkThemeToNode(javafx.scene.Node node) {
        if (node == null) return;

        // Apply styles based on node type
        if (node instanceof VBox || node instanceof HBox || node instanceof GridPane || node instanceof BorderPane) {
            node.setStyle(node.getStyle() + "; -fx-background-color: #2d2d2d;");
        } else if (node instanceof Label) {
            node.setStyle(node.getStyle() + "; -fx-text-fill: #ffffff;");
        } else if (node instanceof TextField) {
            node.setStyle("-fx-background-color: #3d3d3d; -fx-text-fill: #ffffff; -fx-border-color: #555555;");
        } else if (node instanceof TextArea) {
            node.setStyle("-fx-background-color: #3d3d3d; -fx-text-fill: #ffffff; -fx-border-color: #555555;");
        } else if (node instanceof ComboBox) {
            node.setStyle("-fx-background-color: #3d3d3d; -fx-text-fill: #ffffff;");
        } else if (node instanceof TableView) {
            node.setStyle("-fx-background-color: #2d2d2d; -fx-text-background-color: #ffffff;");
        } else if (node instanceof Button && !node.getStyleClass().contains("stats-card")) {
            // Don't override custom button styles, but apply basic dark theme
            String currentStyle = node.getStyle();
            if (!currentStyle.contains("-fx-background-color")) {
                node.setStyle(currentStyle + "; -fx-background-color: #64b5f6; -fx-text-fill: #000000;");
            }
        }

        // Recursively apply to children
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                applyDarkThemeToNode(child);
            }
        }
    }

    private void applyLightThemeToNode(javafx.scene.Node node) {
        if (node == null) return;

        // Apply styles based on node type
        if (node instanceof VBox || node instanceof HBox || node instanceof GridPane || node instanceof BorderPane) {
            node.setStyle(node.getStyle() + "; -fx-background-color: #ffffff;");
        } else if (node instanceof Label) {
            node.setStyle(node.getStyle() + "; -fx-text-fill: #333333;");
        } else if (node instanceof TextField) {
            node.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc;");
        } else if (node instanceof TextArea) {
            node.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc;");
        } else if (node instanceof ComboBox) {
            node.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        } else if (node instanceof TableView) {
            node.setStyle("-fx-background-color: #ffffff; -fx-text-background-color: #333333;");
        } else if (node instanceof Button && !node.getStyleClass().contains("stats-card")) {
            // Don't override custom button styles, but apply basic light theme
            String currentStyle = node.getStyle();
            if (!currentStyle.contains("-fx-background-color")) {
                node.setStyle(currentStyle + "; -fx-background-color: #3498db; -fx-text-fill: #ffffff;");
            }
        }

        // Recursively apply to children
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                applyLightThemeToNode(child);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
