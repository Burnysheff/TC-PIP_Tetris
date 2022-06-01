module com.example.jigsaw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.jigsaw to javafx.fxml;
    opens com.example.jigsaw.client to javafx.fxml;

    exports com.example.jigsaw;
    exports com.example.jigsaw.client;
}