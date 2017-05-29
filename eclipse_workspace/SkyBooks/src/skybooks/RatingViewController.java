/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class RatingViewController implements Initializable {
    @FXML
    Button submitButton;
    
    @FXML
    Button cancelButton;
    
    @FXML
    Rating starRating;
    
    @FXML
    TextArea reviewTextArea;
    
    BookViewController bc;
    Stage stage;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setBookViewController(BookViewController bc) {
        this.bc = bc;
    }
    
    public void setupUI() {
        submitButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    bc.getApp().reportRating(bc.bi,starRating.ratingProperty().intValue(),reviewTextArea.getText());
                    stage.close();                    
                }
            }
        });
        
        cancelButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    stage.close();
                }
            }
        });
    }
    
    public void startInit() {
        this.setupUI();
    }
            

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                startInit();
                            }
                        });
                    }
                },
                10
        );
    }    
    
}
