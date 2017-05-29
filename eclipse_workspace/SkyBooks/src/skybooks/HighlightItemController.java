/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.Highlight;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class HighlightItemController implements Initializable {
    @FXML
    AnchorPane mainPane;
    
    @FXML
    Label chapterLabel;
    
    @FXML
    Pane colorPane;
    
    @FXML
    Label textLabel;
    
    @FXML
    Label noteLabel;
    
    @FXML
    Label createdDateLabel;
    
    @FXML
    Button deleteButton;
    
    Highlight item;

    /**
     * Initializes the controller class.
     */
    
    RecordBoxController rbc;
    
    public void setRecordBoxController(RecordBoxController rbc) {
        this.rbc  = rbc;
    }
    
    public void setHighlight(String chapterTitle,String pageIndex, String text, String note,String createdDate,int color,Highlight item) {
        this.item = item;
        
        chapterLabel.setText(chapterTitle);
        textLabel.setText(text);
        noteLabel.setText(note);
        createdDateLabel.setText(createdDate);
        
        if (note==null || note.isEmpty()) {
            noteLabel.setVisible(false);
            colorPane.setPrefHeight(colorPane.getPrefHeight()+30);
        }
        String colorValue = "";
        if (color!=0x000000) {
            if (color==0xfbf474)        colorValue = "linear-gradient(#fdfbd9,#fbf474)";
            else if (color==0xd1ff5e)   colorValue = "linear-gradient(#f0ffc8,#d1ff5e)";
            else if (color==0x8fd6ff)   colorValue = "linear-gradient(#cfecfd,#8fd6ff)";
            else if (color==0xff886e)   colorValue = "linear-gradient(#fed2c8,#ff886e)";
            colorPane.setStyle("-fx-background-radius:5;-fx-background-color:"+colorValue);
        }else {
            textLabel.setUnderline(true);
        }   
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                rbc.bc.rc.deleteHighlight(item);
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
