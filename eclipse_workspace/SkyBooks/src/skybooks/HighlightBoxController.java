/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.FixedControl;
import com.skytree.epub_desktop.Highlight;
import com.skytree.epub_desktop.ReflowableControl;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class HighlightBoxController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    ResourceBundle bundle;
    
    BookViewController bc;
    ReflowableControl rc;
    
    MagazineViewController mc;
    FixedControl fc;
    
    @FXML
    AnchorPane mainPane;
    
    @FXML
    Button yellowButton;
    
    @FXML
    Button greenButton;
    
    @FXML
    Button redButton;
    
    @FXML
    Button blueButton;
    
    @FXML
    Button lineButton;
    
    @FXML
    Button deleteButton;
    
    @FXML
    Button copyButton;
    
    @FXML
    Button noteButton;
    
    @FXML
    Circle yellowCircle;
    
    @FXML
    Circle greenCircle;
    
    @FXML
    Circle blueCircle;
    
    @FXML
    Circle redCircle;
    
    @FXML
    ImageView lineImageView;    
    
    Highlight highlight;
    
    
    Popup highlightBox;
    
    boolean forFixedLayout = false;
    
    public void setBookViewController(BookViewController bc) {
        this.bc = bc;
        rc = bc.rc;
        highlightBox =  (Popup)mainPane.getScene().getWindow();
        forFixedLayout = false;
    }   
    
    public void setMagazineViewController(MagazineViewController mc) {
        this.mc = mc;
        fc = mc.fc;
        highlightBox =  (Popup)mainPane.getScene().getWindow();
        forFixedLayout = true;
    } 
    
    public void close() {
        highlightBox.hide();
    }
    
    private void processHighlight(int color) {
        if (!forFixedLayout) {
            if (bc.currentHighlight==null) {
                rc.markSelectionHighlight(color,"");
            }else {
                rc.changeHighlightColor(bc.currentHighlight,color);
            }
            bc.currentHighlight = null;
        }else {
            if (mc.currentHighlight==null) {
                fc.makeSelectionHighlight(color);
            }else {
                fc.changeHighlightColor(mc.currentHighlight,color);
            }
            mc.currentHighlight = null;
        }        
    }
    
    private void deleteHighlight() {
        if (!forFixedLayout) {
            if (bc.currentHighlight!=null) {
                rc.deleteHighlight(bc.currentHighlight);
                bc.currentHighlight = null;
            }
        }else {
            if (mc.currentHighlight!=null) {
                fc.deleteHighlight(mc.currentHighlight);
                mc.currentHighlight = null;
            }
        }
    }
    
    private void showNoteBox() {
        double hx = highlightBox.getX();
        double hy = highlightBox.getY();
        if (!forFixedLayout) {
            bc.showNoteBox(hx,hy);
        }else {
            mc.showNoteBox(hx,hy);
        }
    }
    
    public void setupButtons() {
        yellowButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final int color = 0xfbf474;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processHighlight(color);
                        close();
                    }
                });
            }
        });
        
        greenButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final int color = 0xd1ff5e;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processHighlight(color);
                        close();
                    }
                });
            }
        });
        
        blueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final int color = 0x8fd6ff;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processHighlight(color);                        
                        close();
                    }
                });
            }
        });
        
        redButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final int color = 0xff886e;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processHighlight(color);
                        close();
                    }
                });
            }
        });
        
        lineButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final int color = 0x000000;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processHighlight(color);
                        close();
                    }
                });
            }
        });
        
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                       deleteHighlight();
                       close();
                    }
                });
            }
        });
        
        noteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showNoteBox();
                        close();
                    }
                });
            }
        });
        
        
        yellowCircle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                yellowCircle.setStrokeWidth(4);
            }
        });
        
        yellowCircle.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                yellowCircle.setStrokeWidth(2);
            }
        });
        
        blueCircle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                blueCircle.setStrokeWidth(4);
            }
        });
        
        blueCircle.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                blueCircle.setStrokeWidth(2);
            }
        });
        
        
        greenCircle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                greenCircle.setStrokeWidth(4);
            }
        });
        
        greenCircle.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                greenCircle.setStrokeWidth(2);
            }
        });
        
        
        redCircle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                redCircle.setStrokeWidth(4);
            }
        });
        
        redCircle.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                redCircle.setStrokeWidth(2);
            }
        });
        
        
        lineButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                lineButton.setStyle("-fx-background-color: linear-gradient(#f2f2f2, #d4d4d4);");
                
            }
        });
        
        lineButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                lineButton.setStyle("-fx-background-color: white;");            }
        });
        
        
        deleteButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                deleteButton.setStyle("-fx-background-color: linear-gradient(#f2f2f2, #d4d4d4);");
                
            }
        });
        
        deleteButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                deleteButton.setStyle("-fx-background-color:  transparent;");            }
        });
        
        noteButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                noteButton.setStyle("-fx-background-color: linear-gradient(#f2f2f2, #d4d4d4);");
                
            }
        });
        
        noteButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                noteButton.setStyle("-fx-background-color:  transparent;");            }
        });
        
        copyButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                copyButton.setStyle("-fx-background-color: linear-gradient(#f2f2f2, #d4d4d4);");
                
            }
        });
        
        copyButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                copyButton.setStyle("-fx-background-color:  transparent;");            }
        });
    }
    
    public void reload() {
        if (!forFixedLayout) {
            if (bc.currentHighlight==null) {
                noteButton.setDisable(true);
            }else {
                highlight = bc.currentHighlight;
                if (highlight.color==0x00000) {
                    deleteButton.setText(bundle.getString("delete_underline"));
                }
                noteButton.setDisable(false);
            }
        }else {
            if (mc.currentHighlight==null) {
                noteButton.setDisable(true);
            }else {
                highlight = mc.currentHighlight;
                if (highlight.color==0x00000) {
                    deleteButton.setText(bundle.getString("delete_underline"));
                }
                noteButton.setDisable(false);
            }            
        }
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.bundle = rb;
        setupButtons();
    }    
    
}
