/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.PageInformation;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class BookmarkItemController implements Initializable {
    @FXML
    Label chapterLabel;
    
    @FXML
    Label createdDateLabel;
    
    @FXML
    Label pageIndexLabel;
    
    @FXML
    Button deleteButton;
    
    RecordBoxController rbc;
    PageInformation item;
    
    
    public void setRecordBoxController(RecordBoxController rbc) {
        this.rbc  = rbc;
    }
    
    public void setBookmark(String chapterTitle, String pageIndex,String createdDate,PageInformation bookmark) {
        chapterLabel.setText(chapterTitle);
        createdDateLabel.setText(createdDate);
        if (!pageIndex.equalsIgnoreCase("-1")) {
            pageIndexLabel.setText(pageIndex);
        }else {
            pageIndexLabel.setVisible(false);
        }
        this.item = bookmark;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                rbc.bc.deleteBookmark(item);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        rbc.reload();
                                    }
                                });
                            }
                        },
                        250
                );
                
            }
        });

    }    
    
}
