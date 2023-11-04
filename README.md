# Parking System in java

This is a Java application for an Interactive Parking System, built using JavaFX and a MySQL database. It allows users to manage parking slots, park and remove vehicles, and view information about parked vehicles.

## Features

- **Dynamic Slot Management:** Define the total number of parking slots when the application starts.
- **Park Vehicles:** Park a vehicle by entering its license plate number.
- **Remove Vehicles:** Remove a vehicle by entering its license plate number.
- **View Parked Vehicles:** View a list of all parked vehicles, including entry and exit times.
- **Database Integration:** Uses a MySQL database to store vehicle information, ensuring data persistence.

## Installation and Setup

1. Clone this repository to your local machine:

   ```bash
   git clone https://github.com/vamshi060605/parking-system.git
2. Create a MySQL database named "parkingSystem" using the following SQL script:

   
   ```cs
      CREATE DATABASE IF NOT EXISTS parkingSystem;

      USE parkingSystem;

      CREATE TABLE IF NOT EXISTS parked_vehicles (
          id INT AUTO_INCREMENT PRIMARY KEY,
          license_plate TEXT,
          entry_time TIMESTAMP,
          exit_time TIMESTAMP
   );
   
   ```
3. Make sure you have Java and JavaFX installed on your system.

4. Compile and run the application:

## Usage
Launch the application, and you'll be prompted to enter the total number of parking slots.

Once configured, you can park vehicles, remove vehicles, and view parked vehicles using the respective buttons in the application's UI.

The available slots count is dynamically updated as vehicles are parked or removed.

## Contributors
**Vamshi T** <br>
**Harish Sridhar**
