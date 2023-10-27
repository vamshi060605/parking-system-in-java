import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class ParkingSystem extends Application {

    private Connection connection;
    private int totalSlots;
    private ArrayList<ParkedVehicle> parkedVehicles = new ArrayList<>();
    private Label availableSlotsLabel;
    private TableView<ParkedVehicle> parkedVehiclesTable;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Parking System");

        Label titleLabel = new Label("Interactive Parking System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        Label totalSlotsLabel = new Label("Enter the total number of parking slots:");
        TextField totalSlotsField = new TextField();
        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Add padding to elements
        titleLabel.setPadding(new Insets(10));
        totalSlotsLabel.setPadding(new Insets(10, 0, 0, 10));
        totalSlotsField.setPadding(new Insets(10));
        submitButton.setPadding(new Insets(10));

        totalSlotsField.setOnAction(e -> {
            try {
                totalSlots = Integer.parseInt(totalSlotsField.getText());
                initializeDatabase();
                createParkingUI(primaryStage);
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid number for total slots.");
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(titleLabel, totalSlotsLabel, totalSlotsField, submitButton);
        Scene scene = new Scene(layout, 500, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/parkingSystem");
            Statement statement = connection.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS parked_vehicles (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "license_plate TEXT, " +
                    "entry_time TIMESTAMP, " +
                    "exit_time TIMESTAMP)";
            statement.executeUpdate(createTableSQL);

            String selectSQL = "SELECT license_plate, entry_time, exit_time FROM parked_vehicles";
            ResultSet resultSet = statement.executeQuery(selectSQL);
            while (resultSet.next()) {
                String licensePlate = resultSet.getString("license_plate");
                Timestamp entryTime = resultSet.getTimestamp("entry_time");
                Timestamp exitTime = resultSet.getTimestamp("exit_time");

                parkedVehicles.add(new ParkedVehicle(licensePlate, entryTime, exitTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createParkingUI(Stage primaryStage) {
        primaryStage.setTitle("Interactive Parking System");

        BorderPane borderPane = new BorderPane();
        availableSlotsLabel = new Label("Available slots: " + (totalSlots - parkedVehicles.size()));
        availableSlotsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableColumn<ParkedVehicle, String> licensePlateColumn = new TableColumn<>("License Plate");
        TableColumn<ParkedVehicle, Timestamp> entryTimeColumn = new TableColumn<>("Entry Time");
        TableColumn<ParkedVehicle, Timestamp> exitTimeColumn = new TableColumn<>("Exit Time");

        licensePlateColumn.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        entryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("entryTime"));
        exitTimeColumn.setCellValueFactory(new PropertyValueFactory<>("exitTime"));

        parkedVehiclesTable = new TableView<>();
        parkedVehiclesTable.getColumns().addAll(licensePlateColumn, entryTimeColumn, exitTimeColumn);

        Button parkButton = new Button("Park a vehicle");
        Button removeButton = new Button("Remove a vehicle");
        Button viewButton = new Button("View parked vehicles");
        Button exitButton = new Button("Exit");

        parkButton.setPadding(new Insets(10));
        removeButton.setPadding(new Insets(10));
        viewButton.setPadding(new Insets(10));
        exitButton.setPadding(new Insets(10));

        parkButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        removeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        viewButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
        exitButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

        parkButton.setOnAction(e -> parkVehicle(primaryStage));
        removeButton.setOnAction(e -> removeVehicle(primaryStage));
        viewButton.setOnAction(e -> viewParkedVehicles(primaryStage));
        exitButton.setOnAction(e -> System.exit(0));

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(parkButton, removeButton, viewButton, exitButton);

        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(availableSlotsLabel, parkedVehiclesTable, buttonBox);

        borderPane.setCenter(centerBox);

        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
    }

    private void parkVehicle(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Park a vehicle");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter the license plate number of the vehicle:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(licensePlate -> {
            LocalDateTime currentTime = LocalDateTime.now();
            parkedVehicles.add(new ParkedVehicle(licensePlate, Timestamp.valueOf(currentTime), null));

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO parked_vehicles (license_plate, entry_time) VALUES (?, ?)");
                preparedStatement.setString(1, licensePlate);
                preparedStatement.setTimestamp(2, Timestamp.valueOf(currentTime));
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            availableSlotsLabel.setText("Available slots: " + (totalSlots - parkedVehicles.size()));
            parkedVehiclesTable.getItems().add(new ParkedVehicle(licensePlate, Timestamp.valueOf(currentTime), null));
        });
    }

    private void removeVehicle(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove a vehicle");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter the license plate number of the vehicle to be removed:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(licensePlate -> {
            for (ParkedVehicle parkedVehicle : parkedVehicles) {
                if (parkedVehicle.getLicensePlate().equals(licensePlate) && parkedVehicle.getExitTime() == null) {
                    LocalDateTime exitTime = LocalDateTime.now();
                    parkedVehicle.setExitTime(Timestamp.valueOf(exitTime));

                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement(
                                "UPDATE parked_vehicles SET exit_time = ? WHERE license_plate = ?");
                        preparedStatement.setTimestamp(1, Timestamp.valueOf(exitTime));
                        preparedStatement.setString(2, licensePlate);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    availableSlotsLabel.setText("Available slots: " + (totalSlots - parkedVehicles.size()));

                    for (ParkedVehicle item : parkedVehiclesTable.getItems()) {
                        if (item.getLicensePlate().equals(licensePlate) && item.getExitTime() == null) {
                            item.setExitTime(Timestamp.valueOf(exitTime));
                        }
                    }
                    return;
                }
            }
            showAlert("The vehicle is not parked here or has already been removed.");
        });
    }

    private void viewParkedVehicles(Stage primaryStage) {
        if (parkedVehicles.isEmpty()) {
            showAlert("There are no parked vehicles.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Parked vehicles");
        alert.setHeaderText(null);
        StringBuilder content = new StringBuilder("Parked vehicles:\n");

        for (ParkedVehicle parkedVehicle : parkedVehicles) {
            content.append("License Plate: ").append(parkedVehicle.getLicensePlate()).append("\n");
            content.append("Entry Time: ").append(parkedVehicle.getEntryTime()).append("\n");
            if (parkedVehicle.getExitTime() != null) {
                content.append("Exit Time: ").append(parkedVehicle.getExitTime()).append("\n");
            }
            content.append("\n");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class ParkedVehicle {
        private final String licensePlate;
        private final Timestamp entryTime;
        private Timestamp exitTime;

        public ParkedVehicle(String licensePlate, Timestamp entryTime, Timestamp exitTime) {
            this.licensePlate = licensePlate;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public Timestamp getEntryTime() {
            return entryTime;
        }

        public Timestamp getExitTime() {
            return exitTime;
        }

        public void setExitTime(Timestamp exitTime) {
            this.exitTime = exitTime;
        }
    }
}
