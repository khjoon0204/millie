/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class LoginViewController implements Initializable {
    SkyBooks app;
    Stage stage;
    MainViewController mainViewController;
    ResourceBundle bundle;
    
    
    @FXML
    Button loginButton;
    
    @FXML
    Button cancelButton;
    
    @FXML
    TextField userIdTextField;
    
    @FXML
    TextField passwordTextField;    
    
    @FXML
    CheckBox autoCheckBox;
    
    @FXML
    Label messageLabel;
    
    
    public void setApplication(SkyBooks app) {
       this.app = app;
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }
    
    public boolean checkFields() {
        if (userIdTextField.getText().isEmpty()) {
            userIdTextField.requestFocus();
            displayMessage(bundle.getString("userid_is_empty"));
            return false;
        }
        
        if (passwordTextField.getText().isEmpty()) {
            passwordTextField.requestFocus();
            displayMessage(bundle.getString("password_is_empty"));
            return false;
        }        
        return true;
    }
    
    public User makeUser() {
        User user = new User();
        user.userId = userIdTextField.getText();
        user.password = passwordTextField.getText();
        return user;
    }
    
    public void displayMessage(String msg) {
        messageLabel.setText(msg);
    }
    
    public void clearMessage() {
        messageLabel.setText("");
    }
    
    public void processInit() {
        loginButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {                
                clearMessage();
                if (!checkFields()) return;    
                User user = makeUser();
                if (!app.checkUser(user)) {
                    displayMessage(bundle.getString("login_is_failed"));
                    return;
                }                
                user.keepConnection = autoCheckBox.isSelected();
                app.setAutoEnabled(user);
                app.setUser(user);
                app.processSync();
                mainViewController.setLoginIcon(false);
                stage.close();                
            }
        });
        
        cancelButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {                
                stage.close();                
            }
        });
        
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;
        processInit();
    }    
    
}
