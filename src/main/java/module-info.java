module com.example.tischtennissimulation {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tischtennissimulation to javafx.fxml;
    exports com.example.tischtennissimulation;
}