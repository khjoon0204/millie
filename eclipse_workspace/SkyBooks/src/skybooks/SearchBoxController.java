/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.FixedControl;
import com.skytree.epub_desktop.ReflowableControl;
import com.skytree.epub_desktop.SearchResult;
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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;


/**
 * FXML Controller class
 *
 * @author skytree
 */
public class SearchBoxController implements Initializable {
    BookViewController bc;
    MagazineViewController mc;
    ReflowableControl rc;
    FixedControl fc;
    
    ResourceBundle bundle;
    
    @FXML 
    AnchorPane mainPane;
    
    @FXML
    AnchorPane topPane;
    
    @FXML
    Button clearButton;
    
    @FXML
    Label statusLabel;
    
    @FXML
    TextField searchField;
    
    @FXML
    ListView resultListView;
    
    SkyPopup searchBox;
    
    boolean forFixedLayout = false;
    
    ObservableList<SearchResult> results = FXCollections.observableArrayList();
    
    public void setBookViewController(BookViewController bc) {
        this.bc = bc;
        rc = bc.rc;          
        forFixedLayout = false;        
    }
    
    public void setMagazineViewController(MagazineViewController mc) {
        this.mc = mc;
        fc = mc.fc;
        forFixedLayout = true;
        searchField.requestFocus();
    }
    
    public void requestFocus() {
        searchField.requestFocus();
    }
    
    public void addSearchResult(SearchResult result) {
        results.add(result);
    }
    
    public void reportStatus(String msg) {
        this.statusLabel.setText(msg);
        System.out.println(msg+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
    
    public void close() {
        if (!forFixedLayout) {
            bc.hideSearchBox();
        }else {
            mc.hideSearchBox();
        }
    }
    
    
    private void setupControls() {
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchField.getText().isEmpty()) {
                    bc.hideSearchBox();
                }else {
                    searchField.setText("");
                    searchField.requestFocus();
                }
            }
        });
        searchField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER){
                reportStatus(this.bundle.getString("searching")+" ....");
                resultListView.getItems().clear();
                if (!forFixedLayout) {                    
                    rc.searchKey(searchField.getText());
                }else {
                    
                    fc.searchKey(searchField.getText());
                }   
            }else if (event.getCode() == KeyCode.ESCAPE){
                searchField.setText("");
                searchField.requestFocus();
            }
        });
        
        resultListView.setItems(results);
        resultListView.setCellFactory(new Callback<ListView<SearchResult>, ListCell<SearchResult>>() {
            
            @Override
            public ListCell<SearchResult> call(ListView<SearchResult> arg0) {
                return new ListCell<SearchResult>() {                    
                    @Override
                    protected void updateItem(SearchResult item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null) {
                            int maxLengthForContent = 150;
                            int maxLengthForTitle = 30;
                            
                            if (item.text.length()>maxLengthForContent) {
                                item.text = item.text.substring(0,maxLengthForContent);
                            }
                            String chapterTitle = item.chapterTitle;
                            if (!forFixedLayout) {                                
                                if (chapterTitle==null || chapterTitle.isEmpty()) {
                                    chapterTitle = String.format("%s %d",bundle.getString("chapter"),item.chapterIndex);
                                }
                                if (chapterTitle.length()>maxLengthForTitle) {
                                    chapterTitle = chapterTitle.substring(0,maxLengthForTitle);
                                }
                            }else {
                                chapterTitle = "";
                            }
                            Pane pane = new Pane();
                            pane.setMinHeight(100);
                            pane.setPrefHeight(125);
                            pane.setMaxHeight(200);
                            
                            Label titleLabel = new Label(chapterTitle);                            
                            String simpleText = item.text.substring(0, Math.min(item.text.length(), 80));
                            if (forFixedLayout && fc.isSVGUsedForTextContent()) {  
                                int pos = item.pageText.indexOf(item.text);
                                int start = pos-30;
                                int end = pos+50;
                                if (start<0) start = 0;                                
                                simpleText = item.pageText.substring(start,end);
                            } 
                            Label contentLabel = new Label(simpleText);
                            int pageIndexInBook = -1;
                            if (!forFixedLayout) {
                                pageIndexInBook = rc.getPageIndexInBookByPageIndexInChapter(item.chapterIndex,item.pageIndex);
                            }else {
                                pageIndexInBook = item.chapterIndex;
                            }
                            Label pageLabel = new Label(Integer.toString(pageIndexInBook+1));
                            pageLabel.setMinWidth(60);
                            pageLabel.setPrefWidth(60);
                            
                            pane.getChildren().add(titleLabel);
                            pane.getChildren().add(contentLabel);
                            pane.getChildren().add(pageLabel);
                            
                            titleLabel.setLayoutX(10);
                            titleLabel.setLayoutY(5);
                            titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));

                            contentLabel.setLayoutX(10);
                            contentLabel.setLayoutY(35);
                            contentLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 14));
                            contentLabel.setWrapText(true);
                            contentLabel.setTextAlignment(TextAlignment.JUSTIFY);
                            contentLabel.setPrefWidth((double)(resultListView.getWidth()-50));
                            contentLabel.setMaxHeight((double)(140));
                            
                            pageLabel.setLayoutX(resultListView.getWidth()-65);
                            pageLabel.setLayoutY(5);

                            
//                          Insets(double top, double right, double bottom, double left)
                            pane.setPadding(new Insets( 5,  20,  5,  30));
                            pane.setPrefWidth((double)(resultListView.getWidth()-40));
                            setGraphic(pane);
                        }else {
                            setGraphic(null);
                        }
                        
                    }
                    
                };
            }
            
        });
        
        resultListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                SearchResult searchResult = (SearchResult)resultListView.getSelectionModel().getSelectedItem();
                if (!forFixedLayout) {
                    rc.gotoPageBySearchResult(searchResult); 
                }else {
                    fc.gotoPageBySearchResult(searchResult); 
                }
                
                close();
            }
        });
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.bundle = rb;
        this.setupControls();
    }    
    
}
