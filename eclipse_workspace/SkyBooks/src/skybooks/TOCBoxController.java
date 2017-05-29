/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.FixedControl;
import com.skytree.epub_desktop.NavPoint;
import com.skytree.epub_desktop.ReflowableControl;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class TOCBoxController implements Initializable {
    @FXML
    AnchorPane mainPane;
    
    @FXML
    Label titleLabel;
    
    @FXML
    ListView tocListView;
    
    @FXML
    Button closeButton;
    
    SkyPopup tocBox;
    
    BookViewController bc;
    ReflowableControl rc;
    
    MagazineViewController mc;
    FixedControl fc;
    
    boolean forFixedLayout = false;

    
    ObservableList<NavPoint> navPoints = FXCollections.observableArrayList();
    
    public void setBookViewController(BookViewController bc) {
        this.bc = bc;
        rc = bc.rc;
        tocBox =  (SkyPopup)mainPane.getScene().getWindow();
        forFixedLayout = false;
    }
    
    public void setMagazineViewController(MagazineViewController mc) {
        this.mc = mc;
        fc = mc.fc;
        tocBox =  (SkyPopup)mainPane.getScene().getWindow();
        forFixedLayout = true;
    }
    
    public void makeNavPoints() {
        System.out.println("makeNavPoints begin");
        if (navPoints.size()!=0) return;
        if (!forFixedLayout) {
            for (int i=0; i<rc.book.navMap.getSize(); i++) {
                NavPoint np = rc.book.navMap.getNavPoint(i);
                this.navPoints.add(np);
            }
        }else {
            for (int i=0; i<fc.book.navMap.getSize(); i++) {
                NavPoint np = fc.book.navMap.getNavPoint(i);
                this.navPoints.add(np);
            }
        }
        System.out.println("makeNavPoints end");
    }
    
        
    public void close() {
        tocBox.hide();
    }
    
    
    private void setupControls() {        
        tocListView.setItems(navPoints);
        tocListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                NavPoint selectedItem = (NavPoint)tocListView.getSelectionModel().getSelectedItem();
                if (!forFixedLayout) {
                    rc.gotoPageByNavPoint(selectedItem);
                }else {
                    fc.gotoPageByNavPoint(selectedItem);
                }
                close();
            }
        });
        
        
        tocListView.setCellFactory(new Callback<ListView<NavPoint>, ListCell<NavPoint>>() {            
            @Override
            public ListCell<NavPoint> call(ListView<NavPoint> arg0) {
                return new ListCell<NavPoint>() {                    
                    @Override
                    protected void updateItem(NavPoint item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null) {
                            Pane pane = new Pane();
                            pane.setMinHeight(50);
                            pane.setPrefHeight(50);
                            pane.setMaxHeight(50);                            
                            Label navLabel = new Label(item.text);                            
                            pane.getChildren().add(navLabel);                            
                            double deltaX = 20*item.depth;
                            navLabel.setLayoutX(10+deltaX);
                            navLabel.setLayoutY(10);
                            pane.setPadding(new Insets( 5,  20,  5,  30));
                            pane.setPrefWidth((double)(tocListView.getWidth()-40));
                            setGraphic(pane);
                        }                        
                    }
                    
                };
            }
            
        });
        
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!forFixedLayout) {
                    bc.hideTOCBox();
                }else {
                    mc.hideTOCBox();
                }
            }
        });
    }
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        setupControls();
    }    
    
}
