import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;

public class ParkingSystem extends Application {

    private Connection connection;
    private int totalSlots;
    private ArrayList<String> parkedVehicles = new ArrayList<>();
    private Label availableSlotsLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Parking System");
        // Create a colorful style for the title label
        Label titleLabel = new Label("Interactive Parking System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        Label totalSlotsLabel = new Label("Enter the total number of parking slots:");
        TextField totalSlotsField = new TextField();
        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

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
        layout.getChildren().addAll(titleLabel, totalSlotsLabel, totalSlotsField, submitButton);
        Scene scene = new Scene(layout, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/parkingSystem");
            Statement statement = connection.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS parked_vehicles (license_plate TEXT)";
            statement.executeUpdate(createTableSQL);

            // Load parked vehicles from the database
            String selectSQL = "SELECT license_plate FROM parked_vehicles";
            ResultSet resultSet = statement.executeQuery(selectSQL);
            while (resultSet.next()) {
                parkedVehicles.add(resultSet.getString("license_plate"));
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

        ListView<String> parkedVehiclesListView = new ListView<>();
        Button parkButton = new Button("Park a vehicle");
        Button removeButton = new Button("Remove a vehicle");
        Button historyButton = new Button("history of vechicals");
        Button viewButton = new Button("View parked vehicles");
        Button exitButton = new Button("Exit");

        // Style the buttons with different colors
        parkButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        removeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        historyButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        viewButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
        exitButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

        parkButton.setOnAction(e -> parkVehicle(primaryStage, parkedVehiclesListView));
        removeButton.setOnAction(e -> removeVehicle(primaryStage, parkedVehiclesListView));
        viewButton.setOnAction(e -> viewParkedVehicles(primaryStage, parkedVehiclesListView));
        exitButton.setOnAction(e -> System.exit(0));

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(parkButton, removeButton, viewButton, exitButton);

        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(availableSlotsLabel, parkedVehiclesListView, buttonBox);

        borderPane.setCenter(centerBox);

        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
    }

    private void parkVehicle(Stage primaryStage, ListView<String> parkedVehiclesListView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Park a vehicle");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter the license plate number of the vehicle:");

        String licensePlate = dialog.showAndWait().orElse("");
        if (!licensePlate.isEmpty()) {
            parkedVehicles.add(licensePlate);

            // Insert the vehicle into the database
            try {
                PreparedStatement preparedStatement = connection
                        .prepareStatement("INSERT INTO parked_vehicles (license_plate) VALUES (?)");
                preparedStatement.setString(1, licensePlate);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            availableSlotsLabel.setText("Available slots: " + (totalSlots - parkedVehicles.size()));
            parkedVehiclesListView.getItems().add(licensePlate);
        }
    }

    private void removeVehicle(Stage primaryStage, ListView<String> parkedVehiclesListView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove a vehicle");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter the license plate number of the vehicle to be removed:");

        String licensePlate = dialog.showAndWait().orElse("");
        if (!licensePlate.isEmpty()) {
            if (parkedVehicles.contains(licensePlate)) {
                parkedVehicles.remove(licensePlate);

                // Remove the vehicle from the database
                try {
                    PreparedStatement preparedStatement = connection
                            .prepareStatement("DELETE FROM parked_vehicles WHERE license_plate = ?");
                    preparedStatement.setString(1, licensePlate);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                availableSlotsLabel.setText("Available slots: " + (totalSlots - parkedVehicles.size()));
                parkedVehiclesListView.getItems().remove(licensePlate);
            } else {
                showAlert("The vehicle is not parked here.");
            }
        }
    }

    private void viewParkedVehicles(Stage primaryStage, ListView<String> parkedVehiclesListView) {
        if (parkedVehicles.isEmpty()) {
            showAlert("There are no parked vehicles.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Parked vehicles");
        alert.setHeaderText(null);
        alert.setContentText("Parked vehicles:\n" + String.join("\n", parkedVehicles));
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
        // Close the database connection when the application exits
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
