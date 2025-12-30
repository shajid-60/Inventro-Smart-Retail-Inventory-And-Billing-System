
package com.shajid.app.inventro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BillingController {

    @FXML
    private void onBackToCustomer(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/customer_dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Inventro - Customer Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // simple: no extra UI here, just print stacktrace or add an Alert if desired
        }
    }
}
