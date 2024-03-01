module alany.labb {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;

    opens alany.labb to javafx.fxml;
    opens alany.labb.model; // Opening alany.labb.model package
    opens alany.labb.view; // Opening alany.labb.view package
    exports alany.labb;
}
