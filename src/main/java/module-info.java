module com.example.socialnetwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.example.socialnetwork.utils;
    opens com.example.socialnetwork.utils to javafx.fxml;

    exports com.example.socialnetwork.gui;
    opens com.example.socialnetwork.gui to javafx.fxml;

    exports com.example.socialnetwork.domain;
    opens com.example.socialnetwork.domain to javafx.fxml;

    exports com.example.socialnetwork.domain.validators;
    opens com.example.socialnetwork.domain.validators to javafx.fxml;

    exports com.example.socialnetwork.repository;
    opens com.example.socialnetwork.repository to javafx.fxml;

    exports com.example.socialnetwork.service;
    opens com.example.socialnetwork.service to javafx.fxml;

    opens com.example.socialnetwork to javafx.fxml;
    exports com.example.socialnetwork;
}