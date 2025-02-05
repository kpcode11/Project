package com.example.chatgpt;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubjectsController {

    @FXML
    private TextField subjectNameField;

    @FXML
    private TextField marksField;

    @FXML
    private VBox subjectListContainer;

    @FXML
    private Label totalMarksLabel;

    @FXML
    private Label totalPercentageLabel;

    private String loggedInUser;  // Store the logged-in user

    private List<Subject> subjects = new ArrayList<>();

    // Set the logged-in user
    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
        loadUserSubjects();  // Load subjects for this user
    }

    // Method to add a subject for the logged-in user
    @FXML
    private void addSubject() {
        String subjectName = subjectNameField.getText();
        String marksText = marksField.getText();

        // Validate input values (Ensure no empty fields)
        if (subjectName == null || subjectName.trim().isEmpty() || marksText == null || marksText.trim().isEmpty()) {
            System.out.println("Both subject name and marks are required.");
            return;
        }

        try {
            int marks = Integer.parseInt(marksText);

            // Add subject to the database
            addSubjectToDatabase(subjectName, marks, loggedInUser);

            // Add subject to the list and display it in the UI
            Subject subject = new Subject(subjectName, marks);
            subjects.add(subject);
            displaySubject(subject);

            // Clear input fields
            subjectNameField.clear();
            marksField.clear();

        } catch (NumberFormatException e) {
            System.out.println("Marks must be a valid number.");
        }
    }

    // Method to display a subject in the UI
    private void displaySubject(Subject subject) {
        Text subjectDisplay = new Text(subject.getName() + ": " + subject.getMarks() + " marks");
        subjectListContainer.getChildren().add(subjectDisplay);
    }

    // Method to calculate total marks and percentage
    @FXML
    private void calculateResults() {
        int totalMarks = 0;
        int totalSubjects = subjects.size();

        if (totalSubjects == 0) {
            totalMarksLabel.setText("No subjects added.");
            totalPercentageLabel.setText("");
            return;
        }

        for (Subject subject : subjects) {
            totalMarks += subject.getMarks();
        }

        // Calculate percentage
        double totalPercentage = (totalMarks / (double) (totalSubjects * 100)) * 100;

        // Display total marks and percentage
        totalMarksLabel.setText("Total Marks: " + totalMarks);
        totalPercentageLabel.setText("Total Percentage: " + String.format("%.2f", totalPercentage) + "%");
    }

    private void addSubjectToDatabase(String subjectName, int marks, String username) {
        String query = "INSERT INTO subjects (username, subject_name, marks) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username); // Set the logged-in username
            stmt.setString(2, subjectName);
            stmt.setInt(3, marks);
            stmt.executeUpdate(); // Execute the insertion

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void loadUserSubjects() {
        subjects.clear();
        subjectListContainer.getChildren().clear();

        String query = "SELECT subject_name, marks FROM subjects WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, loggedInUser); // Fetch subjects only for the logged-in user
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String subjectName = rs.getString("subject_name");
                int marks = rs.getInt("marks");

                Subject subject = new Subject(subjectName, marks);
                subjects.add(subject);
                displaySubject(subject); // Display subjects in the UI
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Inner class to represent each subject
    public static class Subject {
        private final String name;
        private final int marks;

        public Subject(String name, int marks) {
            this.name = name;
            this.marks = marks;
        }

        public String getName() {
            return name;
        }

        public int getMarks() {
            return marks;
        }
    }
}
