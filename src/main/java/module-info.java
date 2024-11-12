module com.etslabs {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.etslabs to javafx.fxml;
    exports com.etslabs;
}
