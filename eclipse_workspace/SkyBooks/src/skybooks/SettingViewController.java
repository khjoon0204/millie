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
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;


/**
 * FXML Controller class
 *
 * @author skytree
 */
public class SettingViewController implements Initializable {
    Stage stage;
    SkyBooks app;
    
    @FXML
    ToggleButton tb0;
    @FXML
    ToggleButton tb1;
    @FXML
    ToggleButton tb2;
    @FXML
    ToggleButton tb3;
    @FXML
    Button installButton;
    
    MainViewController mainViewController;
    SkySetting setting;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setApplication(SkyBooks app) {
        this.app = app;
    }
    
    public void setMainViewController(MainViewController mc) {
        this.mainViewController = mc;
    }
    
    
    public void processInit() {
        setting = app.fetchSetting();
        tb0.setSelected(setting.globalPagination);
        tb1.setSelected(setting.doublePaged);
        tb2.setSelected(setting.autoStartMediaOverlay);
        tb3.setSelected(setting.createThumbnails);
        installButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {      
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            mainViewController.openFileChooser();                    
                        }
                    });
                    
                }
            }
        });
        
        setCaptions();
    }
    
    public void processAction() {
        setCaptions();        
    }
    
    public void processClose() {
        setting.globalPagination = tb0.isSelected();
        setting.doublePaged = tb1.isSelected();
        setting.autoStartMediaOverlay = tb2.isSelected();
        setting.createThumbnails = tb3.isSelected();
        app.updateSetting(setting);
    }
    
    public void setCaptions() {
        tb0.setText(tb0.isSelected() ? "YES" : "NO");
        tb1.setText(tb1.isSelected() ? "YES" : "NO");
        tb2.setText(tb2.isSelected() ? "YES" : "NO");
        tb3.setText(tb3.isSelected() ? "YES" : "NO");
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                processInit();
                            }
                        });
                    }
                },
                100
        );
    }    
    
}
