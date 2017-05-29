/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.Book;
import com.skytree.epub_desktop.BookInformation;
import com.skytree.epub_desktop.ClickListener;
import com.skytree.epub_desktop.Highlight;
import com.skytree.epub_desktop.HighlightListener;
import com.skytree.epub_desktop.Highlights;
import com.skytree.epub_desktop.KeyListener;
import com.skytree.epub_desktop.MediaOverlayListener;
import com.skytree.epub_desktop.PageInformation;
import com.skytree.epub_desktop.PageMovedListener;
import com.skytree.epub_desktop.PagingInformation;
import com.skytree.epub_desktop.PagingListener;
import com.skytree.epub_desktop.Parallel;
import com.skytree.epub_desktop.ReflowableControl;
import com.skytree.epub_desktop.SearchListener;
import com.skytree.epub_desktop.SearchResult;
import com.skytree.epub_desktop.SelectionListener;
import com.skytree.epub_desktop.SkyProvider;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author skytree
 */

class SkyPopup extends Popup {
    Initializable controller;
    
    public Initializable getController() {
        return this.controller;
    }
    
    public void setController(Initializable controller) {
        this.controller = controller;
    }
}

class Theme {
    String title = "";
    Color foregroundColor = Color.BLACK;
    Color backgroundColor = Color.WHITE;
    
    Theme(String title,Color foregroundColor,Color backgroundColor) {
        this.title = title;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }
}


public class BookViewController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    Stage stage;
    Application app;
    SkySetting setting;
    BookInformation bi;
    
    ResourceBundle bundle;
    
    @FXML
    private AnchorPane rootPane;
    
    @FXML
    private AnchorPane mainPane; 
    
    @FXML
    private HBox topBar;
    
    @FXML
    private Label chapterLabel;
    
    // ----------------------
    
    @FXML
    private Button homeButton;
    
    @FXML
    private ImageView homeImageView;

    @FXML
    private Button tocButton;
    
    @FXML
    private ImageView tocImageView;

    @FXML
    private Button recordButton;
    
    @FXML
    private ImageView recordImageView;
    
    
    @FXML
    private Button fontButton;
    
    @FXML
    private ImageView fontImageView;

    
    @FXML
    private Button searchButton;
    
    @FXML
    private ImageView searchImageView;
    
    @FXML
    private Button bookmarkButton;
    
    @FXML
    private ImageView bookmarkImageView;

    // ----------------
    
    @FXML
    private VBox pageLabelBox;
    
    @FXML
    private Label pageLabelLeft;
    
    @FXML
    private Label pageLabelRight;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Pane mediaBox;
    @FXML
    private Button prevMediaButton;
    @FXML
    private Button nextMediaButton;
    @FXML
    private Button playMediaButton;
    @FXML
    private ImageView playMediaImageView;
    @FXML
    private Button stopMediaButton;
    
    @FXML
    private ProgressIndicator indicator;
    
    @FXML
    private Slider seekBar;
    
    @FXML
    private Button seekBox;
    
    @FXML
    private ToggleButton pagedToggleButton;
    
    @FXML
    private ImageView pagedImageView;


    
    private SkyPopup highlightBox;
    private SkyPopup noteBox;
    private SkyPopup searchBox;
    private SkyPopup fontBox;
    private SkyPopup tocBox;
    private SkyPopup recordBox;
    
    private AnchorPane searchBoxParent;
    private SearchBoxController searchBoxController;
    private AnchorPane noteBoxParent;
    private NoteBoxController noteBoxController;
    
    public ReflowableControl rc = new ReflowableControl();
    
    
    Highlight currentHighlight;
    
    String bookPath;
    int bookCode;
    
    double initialX,initialY;
    
    PageInformation currentPageInformation = null;
    double startPositionInBook = -1;
    
    boolean isPaginationStarted = false;
    
    ArrayList<Theme> themes = new ArrayList<Theme>();
    
    Parallel currentParallel;
    boolean autoStartPlayingWhenNewPagesLoaded = false;
    boolean autoMoveChapterWhenParallesFinished = true;
    boolean isAutoPlaying = true;
    boolean isPageTurnedByMediaOverlay = true;
    
    public MainViewController mainViewController;
    
    int lastSeekBoxPosition = -1;
    
    int maxFontSizeIndex = 7;
    int maxLineSpacingIndex = 10;
    int minFontSize = 16;
    int maxFontSize = 34;
    int minLineSpacing = 120;
    int maxLineSpacing = 400;
    
    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }
    
    public void setBookInformation(BookInformation bi) {
        this.bi = bi;
    }
    
    private void addDraggableNode(final Node node) {
        
        node.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    initialX = me.getSceneX();
                    initialY = me.getSceneY();
                }
            }
        });
        
        node.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    node.getScene().getWindow().setX(me.getScreenX() - initialX);
                    node.getScene().getWindow().setY(me.getScreenY() - initialY);
                }
            }
        });
    }
    
    public void setApplication(Application app) {
        this.app = app;        
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setBookPath(String path) {
        this.bookPath = path;
    }
    
    public void setBookCode(int code) {
        this.bookCode = code;
        rc.setBookCode(this.bookCode);
    }
    
    public void setStartPositionInBook(double startPositionInBook) {
        this.startPositionInBook = startPositionInBook;
    }
    
    public void makeThemes() {
        themes.add(new Theme("White",Color.BLACK,Color.WHITE));
        themes.add(new Theme("Ivory",Color.BLACK,Color.web("#fffede")));
        themes.add(new Theme("Night",Color.WHITE,Color.web("#484848")));
        themes.add(new Theme("靑綠",Color.web("#364613"),Color.web("#f7ffe6")));
        themes.add(new Theme("夕陽",Color.web("#8f0202"),Color.web("#fff1f1")));        
    }
    
    public void changeTheme(int index) {
        this.setting.theme = index;
        Theme theme = this.themes.get(this.setting.theme);
        rc.changeForegroundColor(theme.foregroundColor);
        rc.changeBackgroundColor(theme.backgroundColor);        
        if (setting.theme==0) topBar.setStyle("-fx-background-color:linear-gradient(#ffffff, #f1f1f1);");
        else if (setting.theme==1) topBar.setStyle("-fx-background-color:linear-gradient(#fffcde, #e4e4b9)");
        else if (setting.theme==2) topBar.setStyle("-fx-background-color:linear-gradient(#484848, #8e8e8e);");
        else if (setting.theme==3) topBar.setStyle("-fx-background-color:linear-gradient(#f7ffe6, #cbdaa6)");
        else if (setting.theme==4) topBar.setStyle("-fx-background-color:linear-gradient(#fff1f1, #e2b9b9);");
    }
    
    public void makeSearchBox() {
        if (this.searchBoxParent!=null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SearchBox.fxml"),bundle);
            searchBoxParent = (AnchorPane)fxmlLoader.load();  
            searchBoxController = (SearchBoxController)fxmlLoader.getController();
            searchBoxController.setBookViewController(this);
        }catch(Exception e) {
            e.printStackTrace();
        }
        /*
        if (this.searchBox!=null) return;
        try {
            this.searchBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SearchBox.fxml"),bundle);
            searchBox.getContent().add((Parent)fxmlLoader.load());            
            SearchBoxController searchBoxController = (SearchBoxController)fxmlLoader.getController();
            searchBoxController.setBookViewController(this);
            searchBox.setController(searchBoxController);
            searchBox.setAutoHide(true);
            searchBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
        */
    }
    
    public void showSearchBox() {
        if (this.searchBoxParent==null) {
            this.makeSearchBox();
            
        }
        int delta = 40;        
        if (!mainPane.getChildren().contains(searchBoxParent)) mainPane.getChildren().add(searchBoxParent);
        searchBoxParent.setLayoutX(mainPane.getWidth()-304-delta);
        searchBoxParent.setLayoutY(topBar.getHeight()-delta);
        searchBoxParent.setPrefHeight(mainPane.getHeight());

/*
        if (this.searchBox==null) {
            this.makeSearchBox();
        }
        searchBox.setX(x);
        searchBox.setY(y);  
        SearchBoxController sc = (SearchBoxController)searchBox.controller;
        sc.mainPane.setPrefHeight(this.stage.getHeight()-this.topBar.getHeight());
        
        searchBox.show(this.stage);
        */
    }
    
    public void hideSearchBox() {
        if (mainPane.getChildren().contains(searchBoxParent)) {
            mainPane.getChildren().remove(searchBoxParent);
        }        
//        searchBox.hide();
    }
    
    public void makeHighlightBox() {
        if (this.highlightBox!=null) return;
        try {
            this.highlightBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HighlightBox.fxml"),bundle);
            highlightBox.getContent().add((Parent)fxmlLoader.load());
            HighlightBoxController highlightBoxController = (HighlightBoxController)fxmlLoader.getController();
            highlightBoxController.setBookViewController(this);
            highlightBox.setController(highlightBoxController);
            highlightBox.setAutoHide(true);
            
            highlightBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                    currentHighlight = null;
                    rc.clearSelection();
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void showHighlightBox(double x,double y) {
        if (this.highlightBox==null) {
            this.makeHighlightBox();
        }
        
        highlightBox.setX(x);
        highlightBox.setY(y);
        
        highlightBox.show(this.stage);
        HighlightBoxController hc = (HighlightBoxController)(highlightBox.controller);
        hc.reload();
    }
    
    public void hideHighlightBox() {
        highlightBox.hide();
    }
    

    public void makeNoteBox() {
        if (this.noteBoxParent!=null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NoteBox.fxml"),bundle);
            noteBoxParent = (AnchorPane)fxmlLoader.load();  
            noteBoxController = (NoteBoxController)fxmlLoader.getController();
            noteBoxController.setBookViewController(this);
        }catch(Exception e) {
            e.printStackTrace();
        }
        /*
        if (this.noteBox!=null) return;
        try {
        this.noteBox = new SkyPopup();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NoteBox.fxml"),bundle);
        noteBox.getContent().add((Parent)fxmlLoader.load());
        NoteBoxController noteBoxController = (NoteBoxController)fxmlLoader.getController();
        noteBoxController.setBookViewController(this);
        noteBox.setController(noteBoxController);
        noteBox.setAutoHide(true);
        
        noteBox.setOnAutoHide(new EventHandler<Event>() {
        public void handle(Event event) {
        NoteBoxController nc = (NoteBoxController)noteBox.controller;
        rc.changeHighlightNote(nc.highlight,nc.getNote());
        currentHighlight = null;
        }
        });
        
        
        }catch(Exception e) {
        e.printStackTrace();
        }
        */
    }
    
    
    
    public void showNoteBox(double x,double y) {
        if (this.noteBoxParent==null) {
            this.makeNoteBox();            
        }   
        if (!mainPane.getChildren().contains(noteBoxParent)) mainPane.getChildren().add(noteBoxParent);
        double sx = x-220;
        double sy = y-100;
        double sw = 248; 
        double sh = 256;
        
        if ((sx+sw)>this.stage.getWidth()) sx = this.stage.getWidth()-sw-10;
        if ((sy+sh)>(this.stage.getHeight()-60)) sy = this.stage.getHeight()-sh-10-60;
        
        noteBoxParent.setLayoutX(sx);
        noteBoxParent.setLayoutY(sy);
        noteBoxController.setHighlight(this.currentHighlight);
        noteBoxController.reload();

        /*
        
        
        if (this.noteBox==null) {
            this.makeNoteBox();
        }
        noteBox.setX(x);
        noteBox.setY(y);                

        noteBox.show(this.stage);
        NoteBoxController nc = (NoteBoxController)noteBox.controller;
        nc.setHighlight(this.currentHighlight);
        nc.reload();
        */
    }
    
    public void hideNoteBox() {
        if (mainPane.getChildren().contains(noteBoxParent)) {
            mainPane.getChildren().remove(noteBoxParent);
            rc.changeHighlightNote(noteBoxController.highlight,noteBoxController.getNote());
            currentHighlight = null;
        }
//        noteBox.hide();
    }

    FontBoxController fontBoxController = null;
    
    public void makeFontBox() {
        if (this.fontBox!=null) return;
        try {
            this.fontBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FontBox.fxml"),bundle);
            AnchorPane parent = (AnchorPane)fxmlLoader.load();
//            fontBox.getContent().add((Parent)fxmlLoader.load());            
            fontBox.getContent().add(parent);            
            fontBoxController = (FontBoxController)fxmlLoader.getController();
            fontBoxController.setBookViewController(this);
            fontBox.setController(fontBoxController);
            fontBox.setAutoHide(true);
            fontBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void showFontBox(double x,double y) {
        if (this.fontBox==null) {
            this.makeFontBox();
        }
        fontBox.setX(x);
        fontBox.setY(y);  
        FontBoxController fc = (FontBoxController)fontBox.controller;
        fontBox.show(this.stage);
        
    }
    
    public void hideFontBox() {
        fontBox.hide();
    }
    
    public void makeTOCBox() {
        if (this.tocBox!=null) return;
        try {
            this.tocBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TOCBox.fxml"),bundle);
            tocBox.getContent().add((Parent)fxmlLoader.load());            
            TOCBoxController tocBoxController = (TOCBoxController)fxmlLoader.getController();
            tocBoxController.setBookViewController(this);
            tocBox.setController(tocBoxController);
            tocBox.setAutoHide(true);
            tocBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void showTOCBox(double x,double y) {
        if (this.tocBox==null) {
            this.makeTOCBox();
        }
        tocBox.setX(x);
        tocBox.setY(y);  
        TOCBoxController tc = (TOCBoxController)tocBox.controller;
        tc.mainPane.setPrefHeight(this.stage.getHeight()-this.topBar.getHeight());
        tocBox.show(this.stage);
        tc.makeNavPoints();
    }
    
    public void hideTOCBox() {
        tocBox.hide();
    }

    public void makeRecordBox() {
        if (this.recordBox!=null) return;
        try {
            this.recordBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RecordBox.fxml"),bundle);
            recordBox.getContent().add((Parent)fxmlLoader.load());
            RecordBoxController recordBoxController = (RecordBoxController)fxmlLoader.getController();
            recordBoxController.setBookViewController(this);
            recordBox.setController(recordBoxController);
            recordBox.setAutoHide(true);
            recordBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                }
            });            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void showRecordBox(double x,double y) {
        if (this.recordBox==null) {
            this.makeRecordBox();
        }
        recordBox.setX(x);
        recordBox.setY(y);  
        RecordBoxController rc = (RecordBoxController)recordBox.controller;
        rc.mainPane.setPrefHeight(this.stage.getHeight()-this.topBar.getHeight());
        recordBox.show(this.stage);
        rc.reload();
    }
    
    public void hideRecordBox() {
        recordBox.hide();
    }
    
    public void showRatingView() {
        try {
            if (getApp().currentUser==null || getApp().currentUser.userId==null || getApp().currentUser.userId.isEmpty()) {
                return;
            }
            Stage stage = new Stage(StageStyle.UNDECORATED);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RatingView.fxml"),bundle);
            Parent root =  (Parent)fxmlLoader.load();            
            RatingViewController ratingViewController = (RatingViewController)fxmlLoader.getController();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.stage);
            ratingViewController.setBookViewController(this);
            ratingViewController.setStage(stage);
            stage.show();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    
    public void close() {
        this.updatePosition(currentPageInformation);
        this.updateSetting(setting);
        this.stage.close();
    }
    
    private void hideBoxes() {
        hideSearchBox();
        hideNoteBox();
    }
    
    public SkyBooks getApp() {
        SkyBooks sb = (SkyBooks)app;
        return sb;        
    }
    
    private void toggleBookmark() {        
        boolean isBookmarked = getApp().toggleBookmark(currentPageInformation);
        this.setBookmarked(isBookmarked);
    }
    
    private void setBookmarked(boolean isMarked) {        
        Image image;
        if (isMarked) {
            image = new Image("resources/red_mark.png");            
        }else {
            image = new Image("resources/black_mark.png");
        }   
        if (image!=null) {
            bookmarkImageView.setImage(image);
        }        
    }
    
    public void deleteBookmark(PageInformation bookmark) {
        this.getApp().deleteBookmark(bookmark);
    }
    
    // 현재 총 5단계 
    public int getRealLineSpace(int lineSpaceIndex) {
        int rs = 150;
        
        if (lineSpaceIndex<0 || lineSpaceIndex>maxLineSpacingIndex) {
            return rs;
        }
        double step = (maxLineSpacing-minLineSpacing)/maxLineSpacingIndex;
        rs = (int)(minLineSpacing+lineSpaceIndex*step);
        return rs;
    }
    
    // 현재 총 5단계 
    int getRealFontSize(int fontSizeIndex) {
        int rs = 24;
        
        if (fontSizeIndex<0 || fontSizeIndex>maxFontSizeIndex) {
            return rs;
        }
        double step = (maxFontSize-minFontSize)/maxFontSizeIndex;
        rs = (int)(minFontSize+fontSizeIndex*step);
        return rs;
    }
    
    private void checkSettings() {
        
    }


    
    public void decreaseFont() {
        if (this.setting.fontSize!=0) {
            this.setting.fontSize--;
            rc.changeFont(setting.fontName,this.getRealFontSize(setting.fontSize));
        }
        this.checkSettings();
    }
    
    public void increaseFont() {
        if (this.setting.fontSize!=maxFontSizeIndex) {
            this.setting.fontSize++;
            rc.changeFont(setting.fontName,this.getRealFontSize(setting.fontSize));
        }
        this.checkSettings();
    }
    
    public void changeFontName(String fontName) {
        this.setting.fontName = fontName;
        rc.changeFont(setting.fontName,this.getRealFontSize(setting.fontSize));        
    }
    
    
    public void decreaseLineSpace() {
        if (this.setting.lineSpacing!=0) {
            this.setting.lineSpacing--;
            this.checkSettings();
            rc.changeLineSpacing(this.getRealLineSpace(setting.lineSpacing));
        }
    }
    
    public void increaseLineSpace() {
        if (this.setting.lineSpacing!=maxLineSpacingIndex) {
            this.setting.lineSpacing++;
            this.checkSettings();
            rc.changeLineSpacing(this.getRealLineSpace(setting.lineSpacing));
        }
    }
    
    private void setupButtons() {
        fontButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showFontBox(stage.getX()+stage.getWidth()-307,stage.getY()+topBar.getHeight());                
                System.out.println("Font Button");
            }
        });
        
        fontButton.setTooltip(new SkyTooltip(bundle.getString("font")));
        
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showSearchBox();               
            }
        });
        
        searchButton.setTooltip(new SkyTooltip(bundle.getString("search")));
        
        bookmarkButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                toggleBookmark();
            }
        });
        
        bookmarkButton.setTooltip(new SkyTooltip(bundle.getString("bookmark")));
        
        tocButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showTOCBox(stage.getX(),stage.getY()+topBar.getHeight());
                System.out.println("TOC Button");
            }
        });
        
        tocButton.setTooltip(new SkyTooltip(bundle.getString("table_of_contents")));
        
        recordButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showRecordBox(stage.getX(),stage.getY()+topBar.getHeight());
                System.out.println("Record Button");
            }
        });
        
        recordButton.setTooltip(new SkyTooltip(bundle.getString("record")));
        
                
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                processClose();                     
            }
        }); 
        
                prevMediaButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                playPrev();
            }
        });
        
        playMediaButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                playAndPause();
            }
        });
        
        stopMediaButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stopPlaying();
            }
        });
        
        nextMediaButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                playNext();
            }
        });
        
        homeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainViewController.show();
            }
        });
        
        homeButton.setTooltip(new SkyTooltip(bundle.getString("home")));
        
        pageLabelBox.setOnMouseEntered(new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent t) {
                if (!isPaginationStarted) seekBar.setVisible(true);
            }
        });
        
        pageLabelBox.setOnMouseExited(new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent t) {
                seekBar.setVisible(false);
            }
        });
        
        seekBar.setMin(0);
        seekBar.setMax(999);
        
        
        seekBar.valueProperty().addListener(new ChangeListener() {            
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                double ppb = 0;
                PageInformation pi = null;
                int progress = (int)seekBar.getValue();
                if (rc.isGlobalPagination()) {
                    int pib = progress;
                    ppb = rc.getPagePositionInBookByPageIndexInBook(pib);
                    pi = rc.getPageInformation(ppb);
                }else {
                    ppb = (double)progress/(double)999.0f;
                    pi = rc.getPageInformation(ppb);
                }
                if (pi!=null) moveSeekBox(pi);
            }
        });
        
        seekBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                seekBox.setVisible(true);
            }
        });
        

        
        seekBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                System.out.println(seekBar.getValue());
                seekBox.setVisible(false);
                int position = (int)seekBar.getValue();
                display("seekBar "+lastSeekBoxPosition);
                if (rc.isGlobalPagination()) {
                    int pib = lastSeekBoxPosition;
                    double ppb = rc.getPagePositionInBookByPageIndexInBook(pib);
                    rc.gotoPageByPagePositionInBook(ppb);
                }else {
                    double ppb = (double)position/(double)999;
                    rc.gotoPageByPagePositionInBook(ppb);
                }
            }
        });
        
        if (!setting.doublePaged) {
            pagedToggleButton.setSelected(true);            
        }
        changePagedImage();
        
        pagedToggleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) { 
                togglePaged();
            }
        });    
                
        
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setOffsetX(3.0f);
        ds.setColor(Color.DARKGRAY);

        applyImageFilter(homeImageView,homeButton,ds);
        applyImageFilter(tocImageView,tocButton,ds);
        applyImageFilter(recordImageView,recordButton,ds);
        applyImageFilter(fontImageView,fontButton,ds);
        applyImageFilter(searchImageView,searchButton,ds);
        applyImageFilter(bookmarkImageView,bookmarkButton,ds);
        
        this.stage.setTitle(getApp().getAppTitle());
    }
    
    private void changePagedImage() {
        boolean isSingled = pagedToggleButton.isSelected();
        Image image;
        if (isSingled)  {
            image = new Image("resources/singled.png");
            pagedToggleButton.setTooltip(new SkyTooltip(bundle.getString("singled")));
        }else {
            image = new Image("resources/doubled.png");
            pagedToggleButton.setTooltip(new SkyTooltip(bundle.getString("doubled")));
        }
        if (image!=null) {
            pagedImageView.setImage(image);
        }
        String tip = isSingled ? bundle.getString("singled") : bundle.getString("doubled");
        pagedToggleButton.setTooltip(new SkyTooltip(tip));

    }
    
    private void togglePaged() {
        changePagedImage();
        boolean isSingled = pagedToggleButton.isSelected();
        rc.setDoublePagedForLandscape(!isSingled);
        this.getApp().globalSetting.doublePaged = !isSingled;
        rc.reload();
    }
    
    public void applyImageFilter(ImageView imageView,Button parentButton,Effect effect) {
        imageView.effectProperty().bind(
                Bindings
                        .when(parentButton.hoverProperty())
                        .then((Effect) effect)
                        .otherwise((Effect) null)
        );
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);
    }
    
    public void processClose() {
        this.updatePosition(currentPageInformation);
        this.updateSetting(setting);
        this.rc.destory();
        getApp().deleteOpenedBook(this.bookCode);    
        this.stage.setFullScreen(false);
        mainViewController.show();
        
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                stage.close();   
                                
                            }
                        });
                    }
                },
                100
        );
    }
    
    private void makeBoxes() {
        this.makeFontBox();
        this.makeHighlightBox();
        this.makeNoteBox();
        this.makeSearchBox();  
        this.makeTOCBox();
        this.makeRecordBox();
    }
    
    private void makePageLabels() {
        chapterLabel.toFront();
        pageLabelBox.toFront();    
        pageLabelLeft.setVisible(true);
        pageLabelRight.setVisible(true);
        indicator.toFront();
        seekBox.toFront();
    }
    
    // toggle play/pause button.
    public void changePlayAndPauseButton() {
        //
        Image image;
        if (!rc.isPlayingStarted() || rc.isPlayingPaused())  {
            image = new Image("resources/play.png");
        }else {
            image = new Image("resources/pause.png");
        }
        if (image!=null) {
            playMediaImageView.setImage(image);
        }
    }
    
    void playAndPause() {
        if (rc.isPlayingPaused()) {
            if (!rc.isPlayingStarted()) {
                rc.playFirstParallelInPage();
                if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = true;
            }else {
                rc.resumePlayingParallel();
                if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = true;
            }
            
        }else {
            rc.pausePlayingParallel();
            if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = false;
        }
        this.changePlayAndPauseButton();
    }
    
    void stopPlaying() {
//	    [button1 setTitle:@"Play" forState:UIControlStateNormal];
        rc.stopPlayingParallel();
        rc.restoreElementColor();
        if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = false;
        this.changePlayAndPauseButton();
    }
    
    void playPrev() {
        rc.playPrevParallel();
    }
    
    void playNext() {
        rc.playNextParallel();
    }
    
    class MediaOverlayDelegate implements MediaOverlayListener {
        @Override
        public void onParallelStarted(Parallel parallel) {
            currentParallel = parallel;
            if (rc.pageIndexInChapter()!=parallel.pageIndex) {
                if (autoMoveChapterWhenParallesFinished) {
                    rc.gotoPageByPageIndex(parallel.pageIndex);
                    isPageTurnedByMediaOverlay = true;
                }
            }
            rc.changeElementColor("#FFFF00",parallel.hash);
        }
        
        @Override
        public void onParallelEnded(Parallel parallel) {
            rc.restoreElementColor();
        }
        
        @Override
        public void onParallelsEnded() {
            rc.restoreElementColor();
            if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = true;
            if (autoMoveChapterWhenParallesFinished) {
                rc.gotoNextChapter();
            }
        }
    }
    
    public String getTime() {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        String timeString = timeFormatter.format(System.currentTimeMillis());
        return timeString;
    }
    
    public void display(String message) {
        System.out.println(getTime()+" "+message);
    }
    
    class ClickDelegate implements ClickListener {
        @Override
        public void onClick(double x, double y) {
            hideBoxes();
            System.out.println("x "+x+" : y "+y+" onClick");
        }

        @Override
        public void onImageClicked(double x, double y, String src) {
            System.out.println("x "+x+" : y "+y+" onImageClick");
        }

        @Override
        public void onLinkClicked(double x, double y, String href,int pageIndex) {
            System.out.println("x "+x+" : y "+y+" onLinkClick");
        }

        @Override
        public void onLinkForLinearNoClicked(double x, double y, String href) {
            System.out.println("x "+x+" : y "+y+" onLinkForLinearNoClicked");
        }

        @Override
        public boolean ignoreLink(double x, double y, String href) {
            return false; 
        }

        @Override
        public void onIFrameClicked(double x, double y, String src) {
            System.out.println("x "+x+" : y "+y+" onIFrameClicked");
        }

        @Override
        public void onVideoClicked(double x, double y, String src) {
            System.out.println("x "+x+" : y "+y+" onVideoClicked");
        }

        @Override
        public void onAudioClicked(double x, double y, String src) {
            System.out.println("x "+x+" : y "+y+" onAudioClicked");
        }        
    }
    
    
    int numberOfSearched = 0;
    int ms = 10;
    class SearchDelegate implements SearchListener {
        @Override
        public void onKeySearched(SearchResult searchResult) {
            searchBoxController.addSearchResult(searchResult);
        }

        @Override
        public void onSearchFinishedForChapter(SearchResult searchResult) {                        
            /*
            if (searchResult.numberOfSearchedInChapter!=0) {
                System.out.println(searchResult.chapterIndex+" is finnised");
                rc.pauseSearch();
                numberOfSearched = searchResult.numberOfSearched;
            }else {
                rc.searchMore();
                numberOfSearched = searchResult.numberOfSearched;
            }
            */
            rc.searchMore();
        }

        @Override
        public void onSearchFinished(SearchResult searchResult) {
            System.out.println("All search process finished");
            searchBoxController.reportStatus(bundle.getString("search_finished"));
        }        
    }
    
    private void checkBookmark(PageInformation pi) {   
        if (getApp().isBookmarked(pi)) {
            this.setBookmarked(true);
        }else {
            this.setBookmarked(false);
        }
    }
    
    public void checkBookmark() {   
        PageInformation pi = this.currentPageInformation;
        if (getApp().isBookmarked(pi)) {
            this.setBookmarked(true);
        }else {
            this.setBookmarked(false);
        }
    }

    
    private void updatePosition(PageInformation pi) {        
        getApp().updatePosition(bookCode, pi.pagePositionInBook);
    }
    
    private void updateSetting(SkySetting setting) {
        getApp().updateSetting(setting);
    }
    
    public int getPageIndex(int chapterIndex,int pageIndexInChapter) {
        int pageIndexToDisplay = rc.getPageIndexInBookByPageIndexInChapter(chapterIndex,pageIndexInChapter);
        if (rc.isDoublePaged()) {
            pageIndexToDisplay = pageIndexToDisplay*2+1;
        }else {
            pageIndexToDisplay = pageIndexToDisplay*1+1;
        }
        return pageIndexToDisplay;
    }
    
    private void recalcPageLabelPositions() {
        if (rc.isDoublePaged()) {
            pageLabelLeft.setPrefWidth(this.mainPane.getWidth()/2);
            pageLabelLeft.setMinWidth(this.mainPane.getWidth()/2);
            
            pageLabelRight.setPrefWidth(this.mainPane.getWidth()/2);
            pageLabelRight.setMinWidth(this.mainPane.getWidth()/2);
        }else {
            pageLabelLeft.setPrefWidth(this.mainPane.getWidth());
            pageLabelLeft.setMinWidth(this.mainPane.getWidth());
        }
    }
    
    private void checkPageLabels(PageInformation pi) {  
        if (titleLabel.getText().isEmpty()) {
            titleLabel.setText(rc.book.title);
        }
        chapterLabel.setText(pi.chapterTitle);
        if (this.isPaginationStarted) return; 
        int pageIndexModified = pi.pageIndex;
        int pageIndexToDisplay = getPageIndex(pi.chapterIndex,pi.pageIndex);        
        int numberOfPagesInBook = pi.numberOfPagesInBook;
        int numberOfPagesInChapter = pi.numberOfPagesInChapter;
        if (rc.isDoublePaged()) {
            pageIndexModified=(pageIndexModified*2)+2;
            numberOfPagesInBook*=2;
            numberOfPagesInChapter*=2;
        }else {
            pageIndexModified=(pageIndexModified*1)+1;
        }
        
        int leftPagesInChapter = (numberOfPagesInChapter-pageIndexModified);
        String leftPagesMessage =  "";
        if (leftPagesInChapter!=0 && !rc.isGlobalPagination()) {
//            leftPagesMessage = "                        "+leftPagesInChapter +" Pages Left In Chapter";
            leftPagesMessage = "                    "+String.format(bundle.getString("page_left"),leftPagesInChapter);
        }else {
            leftPagesMessage = "";
        }
        if (!rc.isRTL()) {
            if (numberOfPagesInBook!=0) {
                pageLabelLeft.setText(pageIndexToDisplay+" / "+numberOfPagesInBook);
                pageLabelRight.setText(pageIndexToDisplay+1+" / "+numberOfPagesInBook);
            }else {                
                pageLabelLeft.setText(pageIndexToDisplay+"");
                pageLabelRight.setText(pageIndexToDisplay+1+"");                
            }
        }else {
            if (rc.isDoublePaged()) {
                if (numberOfPagesInBook!=0) {
                    pageLabelLeft.setText(pageIndexToDisplay+1+" / "+numberOfPagesInBook);
                    pageLabelRight.setText(pageIndexToDisplay+" / "+numberOfPagesInBook);
                }else {                    
                    pageLabelLeft.setText(pageIndexToDisplay+1+"");
                    pageLabelRight.setText(pageIndexToDisplay+"");
                }
            }
        }
        
        if (!rc.isDoublePaged()) {
            pageLabelLeft.setText(pageLabelLeft.getText()+leftPagesMessage);
        }else {
            pageLabelRight.setText(pageLabelRight.getText()+leftPagesMessage);
        }
        
        
        this.recalcPageLabelPositions();
    }
    
    private void hidePageLabels() {
        pageLabelLeft.setText("...");
        pageLabelRight.setText("...");
        seekBar.setVisible(false);
        
        this.recalcPageLabelPositions();
    }
    
    private void updatePageLablesForPaging(int currentCountToPaginate,int numberOfChaptersToPaginate) {
        double progress  = (double)currentCountToPaginate/(double)numberOfChaptersToPaginate;
        pageLabelLeft.setText(String.format(bundle.getString("progress_detail"),currentCountToPaginate, numberOfChaptersToPaginate));
        pageLabelRight.setText(String.format(bundle.getString("progress_percent"),(int)(progress*100)));
    }
    
    class PageMovedDelegate implements PageMovedListener {
        @Override
        public void onPageMoved(PageInformation pi) {            
            currentPageInformation = pi;
            System.out.println("Page "+pi.pageIndex+ " / "+pi.numberOfPagesInChapter);
            checkBookmark(pi);
            checkPageLabels(pi); 
            rc.requestFocus();  
            indicator.setVisible(false);
            
            
            double ppb = pi.pagePositionInBook;
            double pageDelta = ((1.0f/pi.numberOfChaptersInBook)/pi.numberOfPagesInChapter);
            int progress = (int)((double)999.0f * (ppb));
            int pib = pi.pageIndexInBook;
            
            if (rc.isGlobalPagination()) {
                if (!isPaginationStarted) {
                    seekBar.setMax(pi.numberOfPagesInBook-1);
                    seekBar.setValue(pib);
                }
            }else {
                seekBar.setValue(progress);                
            }
            
            hideNoteBox();
        }

        @Override
        public void onChapterLoaded(int chapterIndex) {
            System.out.println("Chapter "+chapterIndex+" is loaded.");
        }

        @Override
        public void onFailedToMove(boolean isFirstPage) {
            if (!isFirstPage && !bi.isRated && (!bi.url.isEmpty()) ) {
                showRatingView();
            }
        }        
    }
    
    int op = 0;
    int targetPageIndexInBook = 0;
    public void moveSeekBox(PageInformation pi) {
        int position = (int)seekBar.getValue();
        targetPageIndexInBook = position;
        if (Math.abs(op-position)<10) {
            return;
        }
        if (pi==null) return;
        String chapterTitle = null;
        
        chapterTitle = pi.chapterTitle;
        if (pi.chapterTitle==null || pi.chapterTitle.isEmpty()) {
            chapterTitle = "Chapter "+pi.chapterIndex;
        }
        
        lastSeekBoxPosition = position;
        int pageIndexInSeekBar = position+1;
        if (rc.isDoublePaged()) pageIndexInSeekBar*=2;
        String pageInfo = String.format("%d %s",pageIndexInSeekBar,chapterTitle);
        
        display("seekBox "+position);
        
        if (rc.isGlobalPagination()) {
//			seekLabel.setText(String.format("%s %d",chapterTitle,position+1));
//            seekBox.setText(chapterTitle);            
            seekBox.setText(pageInfo);            
        }else {
            seekBox.setText(chapterTitle);
        }
        int max = (int)seekBar.getMax();
        if (this.rc.isRTL()) {
            position = max - position;
        }
        float cx = (float)((mainPane.widthProperty().floatValue()-50*2) * (float)((float)position/max));
        float cy = mainPane.heightProperty().floatValue()-50;
        
        seekBox.setLayoutX(cx);
        seekBox.setLayoutY(cy);
        
        op = position;
    }

    
    class PagingDelegate implements PagingListener {

        @Override
        public void onPagingStarted(int bookCode,int numberOfChaptersToPaginate) {
            isPaginationStarted = true;
            hidePageLabels();
//            fontBoxController.setFontEnabled(false);
        }

        @Override
        public void onPaged(PagingInformation pagingInformation,int currentCountToPaginate,int numberOfChaptersToPaginate) {
            getApp().insertPagingInformation(pagingInformation);
            updatePageLablesForPaging(currentCountToPaginate,numberOfChaptersToPaginate);
        }

        @Override
        public void onPagingFinished(int bookCode,int currentCountToPaginate,int numberOfChaptersToPaginate) {
            isPaginationStarted = false;
//            fontBoxController.setFontEnabled(true);
            
        }

        @Override
        public int getNumberOfPagesForPagingInformation(PagingInformation pagingInformation) {
            PagingInformation pgi = getApp().fetchPagingInformation(pagingInformation);
            if (pgi!=null) return pgi.numberOfPagesInChapter;
            else return 0;
        }
        
    }
    
    public Color intToColor(int colorValue) {
        String hexColor = String.format("0x%06X", (0xFFFFFF & colorValue));
        Color color = Color.web(hexColor);
        return color;
    }
    
    class HighlightDelegate implements HighlightListener {
        @Override
        public void onHighlightDeleted(Highlight highlight) {
            getApp().deleteHighlight(highlight);            
        }

        @Override
        public void onHighlightInserted(Highlight highlight) {
            getApp().insertHighlight(highlight);
        }

        @Override
        public void onHighlightUpdated(Highlight highlight) {
            getApp().updateHighlight(highlight);
        }

        @Override
        public void onHighlightHit(Highlight highlight, double x, double y, Rectangle2D startRect, Rectangle2D endRect) {
            currentHighlight = highlight;
            showHighlightBox(stage.getX()+x,stage.getY()+y+40);
            System.out.println("highlight Hit");
        }

        @Override
        public Highlights getHighlightsForChapter(int chapterIndex) {
            Highlights hts = getApp().fetchHighlights(rc.bookCode,chapterIndex);            
            return hts;
        }

        @Override
        public void onNoteIconHit(Highlight highlight,double x,double y) {
            currentHighlight = highlight;
            double sx,sy;
            if (x<stage.getWidth()/2) {
                sx = stage.getX()+x+60;
                sy = stage.getY()+y+60;
            }else {
                sx = stage.getX()+x-210;
                sy = stage.getY()+y+80;
            }            
            showNoteBox(sx,sy);
        }

        @Override
        public Image getNoteIconImageForColor(int color, int style) {
            String target = "";
            if (color==0xfbf474) target = "noteyellow.png";
            else if (color==0xd1ff5e) target = "notegreen.png";
            else if (color==0x8fd6ff) target = "noteblue.png";
            else if (color==0xff886e) target = "notered.png";
            else target = "note.png";
            
            target = "/resources/"+target;            
            Image icon = new Image(getClass().getResourceAsStream(target));
            return icon;
        }

        @Override
        public Rectangle2D getNoteIconRect(int color, int style) {
            return new Rectangle2D(0,0,15,18);        
        }

        @Override
        public void onDrawHighlightRect(Pane pane, Highlight highlight, Rectangle2D rect) {
            if (highlight.color!=0x0000) {
                Rectangle rectangle = new Rectangle(rect.getMinX(),rect.getMinY(),rect.getWidth()+25,rect.getHeight());
                Color hc = intToColor(highlight.color);                
                rectangle.setFill(hc);
                rectangle.setOpacity(0.8f);
                pane.getChildren().add(rectangle);            
            }else {                
                Line redLine = LineBuilder.create()
                        .startX(rect.getMinX())
                        .startY(rect.getMaxY())
                        .endX(rect.getMinX()+rect.getWidth()+15)
                        .endY(rect.getMaxY())
                        .fill(Color.RED)
                        .strokeWidth(3.0f)
                        .build();
                redLine.setStroke(Color.RED);
                 pane.getChildren().add(redLine);
            }
        }
    }
    
    class SelectionDelegate implements SelectionListener {

        @Override
        public void selectionStarted(Highlight highlight, Rectangle2D startRect, Rectangle2D endRect) {
            hideHighlightBox();
        }

        @Override
        public void selectionChanged(Highlight highlight, Rectangle2D startRect, Rectangle2D endRect) {
            hideHighlightBox();
            System.out.println("changed");
        }

        @Override
        public void selectionEnded(Highlight highlight, Rectangle2D startRect, Rectangle2D endRect) {
            showHighlightBox(stage.getX()+startRect.getMinX()+80,stage.getY()+startRect.getMinY()+110);
        }

        @Override
        public void selectionCancelled() {
//            currentHighlight = null;
            hideHighlightBox();
        }
        
    }
    
    class KeyDelegate implements KeyListener {
        @Override
        public String getKeyForEncryptedData(String uuidForContent, String contentName, String uuidForEpub) {
            // TODO Auto-generated method stub
           return getApp().getKey(uuidForContent, uuidForEpub);
        }
        
        @Override
        public Book getBook() {
            // TODO Auto-generated method stub
            return rc.getBook();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        display("initialize");
        this.bundle = rb;
        Platform.runLater(new Runnable() { // 400
            @Override
            public void run() {
                autoStartPlayingWhenNewPagesLoaded = getApp().globalSetting.autoStartMediaOverlay;
                makeLayout();
            }
        });
    }
    
    private void makeLayout() {
        display("makeLayout #1");
        setting = getApp().globalSetting;
        makeThemes();
        makeHighlightBox();
        rc.prefWidthProperty().bind(mainPane.widthProperty());
        rc.prefHeightProperty().bind(mainPane.heightProperty());
        mainPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                hideBoxes();
            }
        });
        mainPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                hideBoxes();
            }
        });
        
        SkyProvider skyProvider = new SkyProvider();
        skyProvider.setKeyListener(new KeyDelegate());
        rc.setContentProvider(skyProvider);
        rc.setSelectionListener(new SelectionDelegate());
        rc.setHighlightListener(new HighlightDelegate());
        rc.setPageMovedListener(new PageMovedDelegate());
        rc.setPagingListener(new PagingDelegate());
        rc.setSearchListener(new SearchDelegate());
        rc.setClickListener(new ClickDelegate());
        rc.setMediaOverlayListener(new MediaOverlayDelegate());
        rc.setDoublePagedForLandscape(setting.doublePaged);
        rc.setGlobalPagination(setting.globalPagination);
        this.changeTheme(setting.theme);
        rc.setFont(this.setting.fontName,this.getRealFontSize(setting.fontSize));
        rc.setLineSpacing(this.getRealLineSpace(setting.lineSpacing));
        rc.setCustomDrawHighlight(true);
        addDraggableNode(topBar);
        mainPane.getChildren().add(rc);
        setupButtons();
        
        rc.setLicenseKey(getApp().getLicenseKey0());
        
        rc.setBookCode(bookCode);
        rc.setFilePath(bookPath);
        rc.setStartPosition(startPositionInBook);
        
        makeBoxes();
        makePageLabels();
        
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                rc.startInit();
                            }
                        });
                    }
                },
                100
        );
        
        display("makeLayout #10");
    }
    
    
}
