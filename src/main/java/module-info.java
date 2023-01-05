module com.jdbaptista.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens com.jdbaptista.app to javafx.fxml;
    exports com.jdbaptista.app;
    exports com.jdbaptista.app.labor;
    opens com.jdbaptista.app.labor to javafx.fxml;
    exports com.jdbaptista.app.labor.error;
    opens com.jdbaptista.app.labor.error to javafx.fxml;
}