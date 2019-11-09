import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

// Code modiceret fra András' CitiesAndLanguagesGUI

public class FindEntrySubject extends Application {

    // Statement for executing queries
    private Statement stmt;
    private TextField studentTextField = new TextField();
    private TextField languageTextField = new TextField();
    private Label resultLabelSubject = new Label();
    private Label resultLabelTeacher = new Label();

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) throws SQLException {
        // Initialize database connection and create a Statement object
        initializeDB();

        Button showGradeButton = new Button("What subject(s) are the student assigned to?");
        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(new Label("Student first name"), studentTextField, (showGradeButton));

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(hBox, resultLabelSubject, resultLabelTeacher);

        studentTextField.setPrefColumnCount(8);
        languageTextField.setPrefColumnCount(8);
        showGradeButton.setOnAction(e -> {
            try {
                findEntry();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Create a scene and place it in the stage
        Scene scene = new Scene(vBox, 620, 120);
        primaryStage.setTitle("Students and subjects"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
    }

    private void initializeDB() throws SQLException {
        // Establish a connection
        String password = DB_Settings.getPassword();
        String username = DB_Settings.geUsername();
        Connection connection = DriverManager.getConnection
                ("jdbc:mysql://localhost/zealand", username, password);
        System.out.println("Database connected.");

        // Create a statement
        stmt = connection.createStatement();
    }

    private void findEntry() throws SQLException {
        String studentText = studentTextField.getText();

        String queryString =
                "SELECT studerende.fornavn, fag.navn, undervisere.fornavn\n" +
                        "FROM undervisere, fag, studerende, tilmeldinger\n" +
                        "WHERE tilmeldinger.studentID = studerende.id\n" +
                        "AND tilmeldinger.FagID=fag.id\n" +
                        "AND fag.underviserID=undervisere.id\n" +
                        "AND studerende.fornavn = '" + studentText + "'\n";

        ResultSet resultSet = stmt.executeQuery(queryString);

        if (resultSet.next()) {
            String student = resultSet.getString("studerende.fornavn");

            // Øvre label med fagene.
            String resultSubject = "     " + student + " is assigned to these subjects: ";

            // Cursoren vil flytte ved næste .next() så første fag indsættes før loopet.
            String firstSubject = resultSet.getString("fag.navn");
            resultSubject = resultSubject.concat(firstSubject);

            resultLabelSubject.setText(resultSubject);


            // Nedre label med lærene.
            String resultTeacher = "     The teacher(s) respectively for these subjects are: ";

            // Cursoren vil flytte ved næste .next() så første lærer indsættes før loopet.
            String firstTeacher = resultSet.getString("undervisere.fornavn");
            resultTeacher = resultTeacher.concat(firstTeacher);

            resultLabelTeacher.setText(resultTeacher);
            resultLabelTeacher.setVisible(true);

            while (resultSet.next()) {
                String subject = resultSet.getString("fag.navn");
                String teacher = resultSet.getString("undervisere.fornavn");

                // Display result in a label
                resultSubject = resultSubject.concat(", " + subject);
                resultLabelSubject.setText(resultSubject);

                resultTeacher = resultTeacher.concat(", " + teacher);
                resultLabelTeacher.setText(resultTeacher);

            }

            resultLabelSubject.setText(resultSubject + ".");
        } else {
            resultLabelSubject.setText("     Student not found.");
            resultLabelTeacher.setVisible(false);
        }
    }

    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static void main(String[] args) {
        launch(args);
    }
}