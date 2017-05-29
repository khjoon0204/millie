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
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class NoteBoxController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    @FXML
    AnchorPane mainPane;
    @FXML
    TextArea noteArea;
    
    Color backgroundColor;
    String text;
    Highlight highlight;
    BookViewController bc;
    MagazineViewController mc;
    
    @FXML
    Button closeButton;
    
    boolean forFixedLayout = false;
    
    public String getNote() {
        return noteArea.getText();
    }
    
    public void setBookViewController(BookViewController bc) {
        this.bc = bc;
        forFixedLayout = false;
    }
    
    public void setMagazineViewController(MagazineViewController mc) {
        this.mc = mc;
        forFixedLayout = true;
    }
    
    public void setHighlight(Highlight highlight) {
        this.highlight = highlight;
    }
    
    public Highlight getHighlight() {
        return this.highlight;
    }
    
    public void reload() {
        noteArea.setText(this.highlight.note);
        String colorValue = "";
        if (highlight.color!=0x0000) {
            if (highlight.color==0xfbf474)        colorValue = "linear-gradient(#fdfbd9,#fbf474)";
            else if (highlight.color==0xd1ff5e)   colorValue = "linear-gradient(#f0ffc8,#d1ff5e)";
            else if (highlight.color==0x8fd6ff)   colorValue = "linear-gradient(#cfecfd,#8fd6ff)";
            else if (highlight.color==0xff886e)   colorValue = "linear-gradient(#fed2c8,#ff886e)";
            mainPane.setStyle("-fx-background-radius:5;-fx-background-color:"+colorValue);
        }else {
            mainPane.setStyle("-fx-border-color:lightgray;-fx-border-radius:5;-fx-background-radius:5;-fx-background-color:linear-gradient(#ffffff,#eaeaea)");
        }
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        noteArea.requestFocus();
                        noteArea.deselect();
                        noteArea.end();
                    }
                });
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        noteArea.setWrapText(true);
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.hideNoteBox();
            }
        });
    }    
    
}
