/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.FixedControl;
import com.skytree.epub_desktop.Highlight;
import com.skytree.epub_desktop.Highlights;
import com.skytree.epub_desktop.PageInformation;
import com.skytree.epub_desktop.ReflowableControl;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class RecordBoxController implements Initializable {
    @FXML
    Button highlightButton;
    
    @FXML
    Button bookmarkButton;
    
    @FXML
    ListView highlightListView;
    
    @FXML
    ListView bookmarkListView;
    
    @FXML
    AnchorPane mainPane;   
    
    @FXML
    Button closeButton;
    
    BookViewController bc;
    ReflowableControl rc;
    
    MagazineViewController mc;
    FixedControl fc;
    
    boolean forFixedLayout = false;
    
    SkyPopup recordBox;
    SkyBooks sb;
    int bookCode;
    
    ObservableList<PageInformation> bookmarks;
    ObservableList<Highlight> highlights;
    
    RecordBoxController rbc;
    
    public void setBookViewController(BookViewController bc) {
        this.bc = bc;
        rc = bc.rc;
        recordBox =  (SkyPopup)mainPane.getScene().getWindow();
        sb = (SkyBooks)bc.app;
        forFixedLayout = false;
        bookCode = bc.bookCode;
        rbc = this;
    }
    
    public void setMagazineViewController(MagazineViewController mc) {
        this.mc = mc;
        fc = mc.fc;
        recordBox =  (SkyPopup)mainPane.getScene().getWindow();
        sb = (SkyBooks)mc.app;
        forFixedLayout = true;
        bookCode = mc.bookCode;
        rbc = this;
    }
    
    public void reload() {    
        System.out.println("reload !!!!");
        if (highlightListView.isVisible()) {
            highlightButton.requestFocus();
        }else {
            bookmarkButton.requestFocus();
        }

        ArrayList<PageInformation> pis = sb.fetchBookmarks(bookCode);
        bookmarks = FXCollections.observableArrayList();
        for (int i=0; i<pis.size();i++) {
            PageInformation pi = pis.get(i);
            bookmarks.add(pi);
        }
        
        Highlights hts = sb.fetchAllHighlights(bookCode);
        highlights = FXCollections.observableArrayList();
        for (int i=0; i<hts.getSize();i++) {
            Highlight ht = hts.getHighlight(i);
            highlights.add(ht);
        }
        
        bookmarkListView.setItems(null);
        highlightListView.setItems(null);
        
        bookmarkListView.setItems(bookmarks);
        highlightListView.setItems(highlights);
        
        if (!forFixedLayout) {
            bc.checkBookmark();        
        }else {
            
        }
        System.out.println("size "+pis.size()+" : "+bookmarks.size());
    }
    
    private void setupControls() {
        highlightListView.setVisible(true);
        bookmarkListView.setVisible(false);

        
        highlightButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                highlightListView.setVisible(true);
                bookmarkListView.setVisible(false);
            }
        });
        
        bookmarkButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                highlightListView.setVisible(false);
                bookmarkListView.setVisible(true);
            }
        });
        
        
        highlightListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Highlight selectedItem = (Highlight)highlightListView.getSelectionModel().getSelectedItem();
                if (!forFixedLayout) {
                    rc.gotoPageByHighlight(selectedItem);
                }else {
                    fc.gotoPageByHighlight(selectedItem);
                }
                close();
            }
        });
        highlightListView.setCellFactory(new Callback<ListView<Highlight>, ListCell<Highlight>>() {
            @Override
            public ListCell<Highlight> call(ListView<Highlight> arg0) {
                return new ListCell<Highlight>() {
                    @Override
                    protected void updateItem(Highlight item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null) {
                            AnchorPane pane = new AnchorPane();
                            try {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HighlightItem.fxml"));
                                AnchorPane itemPane = (AnchorPane)fxmlLoader.load();
                                pane.getChildren().add(itemPane);
                                String chapterTitle = "";
                                int pageIndex = -1;
                                if (!forFixedLayout) {
                                    chapterTitle = rc.book.getChapterTitle(item.chapterIndex);
                                    pageIndex = rc.getPageIndexInBookByPagePositionInChapter(item.chapterIndex,item.pagePositionInChapter);
                                    if (rc.isDoublePaged()) pageIndex=pageIndex*2+1;
                                    else pageIndex = pageIndex+1;
                                }else {
                                    chapterTitle = fc.book.getChapterTitle(item.chapterIndex);
                                    pageIndex = item.chapterIndex+1;
                                }
                                
                                int maxLengthForTitle = 30;
                                if (chapterTitle==null || chapterTitle.isEmpty()) {
                                    chapterTitle = String.format("Chapter %d",item.chapterIndex);
                                }
                                if (chapterTitle.length()>maxLengthForTitle) {
                                    chapterTitle = chapterTitle.substring(0,maxLengthForTitle);
                                }

                                HighlightItemController highlightItemController = (HighlightItemController)fxmlLoader.getController();
//                                highlightItemController.setBookmark(chapterTitle,String.valueOf(pageIndex),item.datetime);
                                highlightItemController.setRecordBoxController(rbc);
                                itemPane.setLayoutX(0);
                                itemPane.setLayoutY(0);
                                if (item.note==null || item.note.isEmpty()) itemPane.setMaxHeight(itemPane.getPrefHeight()-70);
                                pane.setMaxHeight(itemPane.getHeight());                                
                                highlightItemController.setHighlight(chapterTitle,String.valueOf(pageIndex),item.text,item.note,item.datetime,item.color,item);
                            }catch(Exception e) {
                                e.printStackTrace();
                            }
                            setGraphic(pane);
                        }else {
                            setGraphic(null);
                        }
                        
                    }
                    
                };
            }
            
        });
        
        bookmarkListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                PageInformation selectedItem = (PageInformation)bookmarkListView.getSelectionModel().getSelectedItem();
                if (!forFixedLayout) {
                    rc.gotoPageByPagePositionInBook(selectedItem.pagePositionInBook);
                }else {
                    fc.gotoPage(selectedItem.chapterIndex);
                }
                close();
            }
        });
        
        
        bookmarkListView.setCellFactory(new Callback<ListView<PageInformation>, ListCell<PageInformation>>() {            
            @Override
            public ListCell<PageInformation> call(ListView<PageInformation> arg0) {
                return new ListCell<PageInformation>() {                    
                    @Override
                    protected void updateItem(PageInformation item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null) {
                          AnchorPane pane = new AnchorPane();
                            try {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BookmarkItem.fxml"));
                                AnchorPane itemPane = (AnchorPane)fxmlLoader.load();                                
                                pane.getChildren().add(itemPane);
                                String chapterTitle = item.chapterTitle;
                                int maxLengthForTitle = 30;
                                if (chapterTitle==null || chapterTitle.isEmpty()) {
                                    chapterTitle = String.format("Chapter %d",item.chapterIndex);
                                }
                                if (chapterTitle.length()>maxLengthForTitle) {
                                    chapterTitle = chapterTitle.substring(0,maxLengthForTitle);
                                }
                                int pageIndex = -1;
                                if (!forFixedLayout) {
                                    if (bc.rc.isGlobalPagination()) {
                                        pageIndex = rc.getPageIndexInBookByPagePositionInChapter(item.chapterIndex,item.pagePositionInChapter);
                                        if (rc.isDoublePaged()) pageIndex=pageIndex*2+1;
                                        else pageIndex = pageIndex+1;
                                    }else {
                                        pageIndex = -1;
                                    }
                                }else {
                                    pageIndex = item.chapterIndex+1;
                                }
                                

                                BookmarkItemController bookmarkItemController = (BookmarkItemController)fxmlLoader.getController();
                                bookmarkItemController.setBookmark(chapterTitle,String.valueOf(pageIndex),item.datetime,item);
                                bookmarkItemController.setRecordBoxController(rbc);
                                itemPane.setLayoutX(0);
                                itemPane.setLayoutY(0);
                            }catch(Exception e) {
                                e.printStackTrace();
                            }
                            setGraphic(pane);
                        }else {
                            setGraphic(null);
                        }                        
                    }
                    
                };
            }
            
        });
        
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!forFixedLayout) {
                    bc.hideRecordBox();
                }else {
                    mc.hideRecordBox();
                }
            }
        });

    }
    
    public void close() {
        recordBox.hide();
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
