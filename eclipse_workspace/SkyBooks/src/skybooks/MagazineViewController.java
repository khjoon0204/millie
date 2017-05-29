/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javax.imageio.ImageIO;
import com.skytree.epub_desktop.Book;
import com.skytree.epub_desktop.CacheListener;
import com.skytree.epub_desktop.ClickListener;
import com.skytree.epub_desktop.FixedControl;
import com.skytree.epub_desktop.Highlight;
import com.skytree.epub_desktop.HighlightListener;
import com.skytree.epub_desktop.Highlights;
import com.skytree.epub_desktop.KeyListener;
import com.skytree.epub_desktop.MediaOverlayListener;
import com.skytree.epub_desktop.PageInformation;
import com.skytree.epub_desktop.PageMovedListener;
import com.skytree.epub_desktop.Parallel;
import com.skytree.epub_desktop.SearchListener;
import com.skytree.epub_desktop.SearchResult;
import com.skytree.epub_desktop.SelectionListener;
import com.skytree.epub_desktop.SkyProvider;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author skytree
 */

// Server.java 903에서 다수의 오류

class ThumbnailItem extends AnchorPane {
    int pageIndex  = -1;
    public ImageView thumbnailImageView = new ImageView();
    public Label pageIndexLabel = new Label();
    double width=-1,height=-1;
    double padding = 2;
    boolean isMarked = false;
    ThumbnailItem(int pageIndex,double width,double height) {
        this.pageIndex = pageIndex;
        
        this.setId(""+pageIndex);
        this.width = width;
        this.height = height;
        
        this.setPrefWidth(width);
        this.setPrefHeight(height);        
        this.setStyle("-fx-background-color:transparent;");        
        
        pageIndexLabel.setText(""+pageIndex);
        pageIndexLabel.setLayoutX(0);
        pageIndexLabel.setLayoutY(0);
        pageIndexLabel.setPrefWidth(width);
        pageIndexLabel.setPrefHeight(height);
        pageIndexLabel.setTextAlignment(TextAlignment.RIGHT);
        pageIndexLabel.setAlignment(Pos.CENTER);
        
        pageIndexLabel.setWrapText(false);
        pageIndexLabel.setFont(new Font("Arial", 30));
        this.getChildren().add(pageIndexLabel);
        this.getChildren().add(thumbnailImageView);
        
        thumbnailImageView.setId(""+pageIndex);
        thumbnailImageView.setLayoutX(padding);
        thumbnailImageView.setLayoutY(padding);
        thumbnailImageView.setFitWidth(this.width-padding*2);
        thumbnailImageView.setStyle("-fx-background-color: transparent;");
        thumbnailImageView.setPreserveRatio(true);   
    }
    
    public void setMarked(boolean marked) {
        this.isMarked = marked;
        if (this.isMarked) {
            this.setStyle("-fx-background-color:gray;");
        }else {
            this.setStyle("-fx-background-color:lightgray;");
        }
    }
    
    public int getPageIndex() {
        return this.pageIndex;        
    }
    
    public ImageView getImageView() {
        return this.thumbnailImageView;
    }
    
    public void setImagePath(String filePath) {    
        File file = new File(filePath);
        if (file.exists()) {
            double ih = this.height;
            double iw = this.width;
            Image image = new Image(file.toURI().toString(), iw,ih, false, false);
            this.thumbnailImageView.setImage(image);
        }        
    } 
    
    public void setImage(Image image) {   
        this.thumbnailImageView.setImage(image);
    } 
}

public class MagazineViewController implements Initializable {
    Stage stage;
    Application app;
    SkySetting setting;
    ResourceBundle bundle;
    
    @FXML
    private AnchorPane rootPane;
    
    @FXML
    private AnchorPane mainPane; 
    
    @FXML
    private HBox topBar;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private Button minimizeButton;
    
    @FXML
    private Button expandButton;
    
    // --------------------------
    @FXML
    private Button homeButton;
    
    @FXML
    private ImageView homeImageView;

    @FXML
    private Button tocButton;
    
    @FXML
    private ImageView tocImageView;

    @FXML
    private Button thumbNailsButton;
    
    @FXML
    private ImageView thumbNailsImageView;         
    
    @FXML
    private Button recordButton;
    
    @FXML
    private ImageView recordImageView;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private ImageView searchImageView;
    
    @FXML
    private Button bookmarkButton;    
    
    @FXML
    private ImageView bookmarkImageView;
    
    // ----------------------------------------------
    
    @FXML
    private HBox pageLabelBox;
    
    @FXML
    private Label pageLabelLeft;
    
    @FXML
    private Label pageLabelRight;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private AnchorPane controlButtonsBox;
    
    @FXML
    private ScrollPane thumbnailsBox;
    
    HBox thumbnailsContent;
    private SkyPopup tocBox;
    
    private AnchorPane searchBoxParent;
    private SearchBoxController searchBoxController;
    private SkyPopup highlightBox;
    
    public FixedControl fc = new FixedControl();
    Parallel currentParallel;
    boolean autoStartPlayingWhenNewPagesLoaded = true;
    boolean autoMovePageWhenParallesFinished = true;
    boolean isAutoPlaying = true;	
    public int bookCode = 0;
    
    Button button0,button1,button2,button3,button4;
    TextField searchField;
    VBox searchBox;
    ListView <SearchResult> searchView;
    
    public Button toLeftButton = new Button();
    public Button toRightButton = new Button();
        
    String bookPath;
    double startPositionInBook = -1;
    
    double initialX,initialY;
    PageInformation currentPageInformation;
    
    Highlight currentHighlight;
    private AnchorPane noteBoxParent;
    private NoteBoxController noteBoxController;
    private SkyPopup noteBox;
    private SkyPopup recordBox;
    
    int currentColor;
    
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
    
    public MainViewController mainViewController;
    
    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
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


    
    final ObservableList<SearchResult> searchResults = FXCollections.observableArrayList();
    
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
        fc.setBookCode(this.bookCode);
    }
    
    public void setStartPositionInBook(double startPositionInBook) {
        this.startPositionInBook = startPositionInBook;
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
            return fc.getBook();
        }
    }
    
    class PageMovedDelegate implements PageMovedListener {
        public void onPageMoved(PageInformation pi) {            
            currentPageInformation = pi;
            checkBookmark(pi);
            sendThumbnailToCenter(pi.pageIndex);
            String msg = String.format("pn:%d/tn:%d ps:%f",pi.pageIndex,pi.numberOfPagesInBook,pi.pagePositionInBook);
            
            if (fc.isMediaOverlayAvailable()) {
                showMediaBox();
                if (isAutoPlaying) {
                    fc.playFirstParallel();
                }
            }else {
                hideMediaBox();
            }
        }
        
        public void onChapterLoaded(int chapterIndex) {
            // do nothing in FixedLayout.
        }

        @Override
        public void onFailedToMove(boolean isFirstPage) {
            //
        }
    }
    
    class MediaOverlayDelegate implements MediaOverlayListener {
        @Override
        public void onParallelStarted(Parallel parallel) {
            // TODO Auto-generated method stub
            fc.changeElementColor("#FFFF00",parallel.hash,parallel.pageIndex);
            currentParallel = parallel;
        }
        
        @Override
        public void onParallelEnded(Parallel parallel) {
            // TODO Auto-generated method stub
            fc.restoreElementColor();
        }
        
        @Override
        public void onParallelsEnded() {
            // TODO Auto-generated method stub
            fc.restoreElementColor();
            if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = true;
            if (autoMovePageWhenParallesFinished) {
                fc.gotoNext();
            }
        }
    }
    
    private String getCacheFolder() {
        String dir = getApp().getCachesDirectory();
        return dir;
    }
    
    private String getFilePath(int pageIndex) {
        String prefix = this.getCacheFolder();
        String name = String.format("sb%d-cache%d.png",this.bookCode,pageIndex);
        String filePath = prefix+File.separator+name;
        
        return filePath;
    }
    
    class SearchDelegate implements SearchListener {
        @Override
        public void onKeySearched(SearchResult searchResult) {
            searchBoxController.addSearchResult(searchResult);
        }

        @Override
        public void onSearchFinishedForChapter(SearchResult searchResult) {
            fc.searchMore();
        }

        @Override
        public void onSearchFinished(SearchResult searchResult) {
            searchBoxController.reportStatus(String.format("Searching All Finished. (%d found)",searchResult.numberOfSearched));
        }        
    }
    
    class SelectionDelegate implements SelectionListener {
        @Override
        public void selectionStarted(Highlight highlight, Rectangle2D startRect, Rectangle2D endRect) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void selectionChanged(Highlight highlight, Rectangle2D startRect, Rectangle2D endRect) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void selectionEnded(Highlight highlight, Rectangle2D startRect, Rectangle2D endRect) {
           showHighlightBox(stage.getX()+startRect.getMinX()+20,stage.getY()+startRect.getMinY()+60);
        }

        @Override
        public void selectionCancelled() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
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
            showHighlightBox(stage.getX()+x,stage.getY()+y+80);
        }

        @Override
        public Highlights getHighlightsForChapter(int chapterIndex) {
            Highlights hts = getApp().fetchHighlights(fc.bookCode,chapterIndex);            
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
        }
    }
    
    private void makeBoxes() {
        this.makeRecordBox();
        this.makeTOCBox();
        this.makeSearchBox();
        this.makeHighlightBox();
        this.makeNoteBox();
    }
    
    private void hideBoxes() {
        this.hideSearchBox();
        this.hideNoteBox();
        fc.requestFocus();
    }
    
    // coordinates x,y are view based. (not web coordination)
    class ClickDelegate implements ClickListener {
        @Override
        public void onClick(double x, double y) {
            hideBoxes();
        }
        
        @Override
        public void onImageClicked(double x, double y, String src) {
        }
        
        @Override
        public void onLinkClicked(double x, double y, String href,int pageIndex) {
        }
        
        @Override
        public void onLinkForLinearNoClicked(double x, double y, String href) {
        }
        
        @Override
        public boolean ignoreLink(double x, double y, String href) {
            return false;
        }
        
        @Override
        public void onIFrameClicked(double x, double y, String src) {
        }
        
        @Override
        public void onVideoClicked(double x, double y, String src) {
        }
        
        @Override
        public void onAudioClicked(double x, double y, String src) {
        }
    }
    
    class CacheDelegate implements CacheListener {
        @Override
        public void onCachingStarted(int numberOfUncached) {
        }
        
        @Override
        public void onCachingFinished(int numberOfCached) {
        }        
        
        @Override
        public void onCached(int pageIndex, WritableImage image, double progress) {
            // TODO Auto-generated method stub      
            try {
                ThumbnailItem ti = getThumbnailItemByPageIndex(pageIndex);
                ti.setImage(image);
                String filePath = getFilePath(pageIndex);
                File file = new File(filePath);
                BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bi,"png", file);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public boolean cacheExist(int pageIndex) {
            // TODO Auto-generated method stub
            String filePath = getFilePath(pageIndex);
            File file = new File(filePath);
            if (file.exists()) return true;
            return false;
        }
    }
    
    // toggle play/pause button.
    public void changePlayAndPauseButton() {
        //
        Image image;
        if (!fc.isPlayingStarted() || fc.isPlayingPaused())  {
            image = new Image("resources/play.png");
        }else {
            image = new Image("resources/pause.png");
        }
        if (image!=null) {
            playMediaImageView.setImage(image);
        }
    }
    
    void playAndPause() {
        if (fc.isPlayingPaused()) {
            if (!fc.isPlayingStarted()) {
                fc.playFirstParallel();
                if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = true;
            }else {
                fc.resumePlayingParallel();
                if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = true;
            }
            
        }else {
            fc.pausePlayingParallel();
            if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = false;
        }
        this.changePlayAndPauseButton();
    }
    
    void stopPlaying() {
        if (true) return;
        fc.stopPlayingParallel();
        fc.restoreElementColor();
        if (autoStartPlayingWhenNewPagesLoaded) isAutoPlaying = false;
        this.changePlayAndPauseButton();
    }
    
    void playPrev() {
        fc.restoreElementColor();
        if (currentParallel.parallelIndex==0) {
            if (autoMovePageWhenParallesFinished) fc.gotoPrev();
        }else {
            fc.playPrevParallel();
        }
    }
    
    void playNext() {
        fc.restoreElementColor();
        fc.playNextParallel();
    }
    
    
    public void makeHighlightBox() {
        if (this.highlightBox!=null) return;
        try {
            this.highlightBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HighlightBox.fxml"),bundle);
            highlightBox.getContent().add((Parent)fxmlLoader.load());
            HighlightBoxController highlightBoxController = (HighlightBoxController)fxmlLoader.getController();
            highlightBoxController.setMagazineViewController(this);
            highlightBox.setController(highlightBoxController);
            highlightBox.setAutoHide(true);
            
            highlightBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                    currentHighlight = null;
                    fc.clearSelection();
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
        fc.requestFocus();
    }
    
    
    public void makeNoteBox() {
        if (this.noteBoxParent!=null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NoteBox.fxml"),bundle);
            noteBoxParent = (AnchorPane)fxmlLoader.load();
            noteBoxController = (NoteBoxController)fxmlLoader.getController();
            noteBoxController.setMagazineViewController(this);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void showNoteBox(double x,double y) {
        if (this.noteBoxParent==null) {
            this.makeNoteBox();
        }
        mainPane.getChildren().add(noteBoxParent);
        noteBoxParent.setLayoutX(x-220);
        noteBoxParent.setLayoutY(y-140);
        noteBoxController.setHighlight(this.currentHighlight);
        noteBoxController.reload();
    }
    
    public void hideNoteBox() {
        if (mainPane.getChildren().contains(noteBoxParent)) {
            mainPane.getChildren().remove(noteBoxParent);
            fc.changeHighlightNote(noteBoxController.highlight,noteBoxController.getNote());
            currentHighlight = null;
        }
        fc.requestFocus();
    }

    
    public void makeThumbnailsBox() {
        this.thumbnailsContent = new HBox();
        thumbnailsBox.setStyle("-fx-background-color: transparent;");
        thumbnailsContent.setStyle("-fx-background-color: transparent;");
        this.thumbnailsBox.setContent(thumbnailsContent);
        
        double tw = 80;
        double th = 80/(fc.book.fixedRatio);  
        
        int numberOfPages = fc.book.spine.size();
        for (int i=0; i<numberOfPages; i++) {    
            ThumbnailItem ti = new ThumbnailItem(i,tw,th);
            HBox.setMargin(ti, new Insets(0, 10, 0, 10));  // top,left,bottom,right
            ti.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {                
                @Override
                public void handle(MouseEvent event) {
                    ThumbnailItem ti = (ThumbnailItem)event.getSource();
                    int pi  = ti.getPageIndex();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            fc.gotoPage(pi);
                            thumbnailsBox.setVisible(false);
                        }
                    });
                    event.consume();
                }
            });
            thumbnailsContent.getChildren().add(ti);
        }
        fillThumbnails();
        thumbnailsBox.setVisible(false);
        thumbnailsBox.toFront();
    }
    
    ThumbnailItem getThumbnailItemByPageIndex(int pageIndex) {
        ThumbnailItem item = (ThumbnailItem)thumbnailsContent.getChildren().get(pageIndex);
        return item;
    }
    
    private void fillThumbnails() {
        int numberOfPages = fc.book.spine.size();
        for (int i=0; i<numberOfPages; i++) {
            ThumbnailItem ti = this.getThumbnailItemByPageIndex(i);
            ti.setImagePath(getFilePath(i));
        }        
    }
    
    public void makeSearchBox() {
        if (this.searchBoxParent!=null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SearchBox.fxml"),bundle);
            searchBoxParent = (AnchorPane)fxmlLoader.load();  
            searchBoxController = (SearchBoxController)fxmlLoader.getController();
            searchBoxController.setMagazineViewController(this);
            mainPane.getChildren().add(searchBoxParent);
            searchBoxParent.setVisible(false);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void showSearchBox() {
        if (this.searchBoxParent==null) {
            this.makeSearchBox();
        }
        if (!searchBoxParent.isVisible()) {
            int delta = 40;            
            searchBoxParent.setLayoutX(mainPane.getWidth()-304-delta);
            searchBoxParent.setLayoutY(topBar.getHeight()-delta);
            searchBoxParent.setPrefHeight(mainPane.getHeight());
            searchBoxParent.setVisible(true);
            searchBoxParent.toFront();
            searchBoxController.requestFocus();
        }else {
            hideSearchBox();
        }
    }
    
    public void hideSearchBox() {
        if (searchBoxParent.isVisible()) {
            searchBoxParent.setVisible(false);            
        }        
        fc.requestFocus();
    }
    
    
    public void makeTOCBox() {
        if (this.tocBox!=null) return;
        try {
            this.tocBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TOCBox.fxml"),bundle);
            tocBox.getContent().add((Parent)fxmlLoader.load());
            TOCBoxController tocBoxController = (TOCBoxController)fxmlLoader.getController();
            tocBoxController.setMagazineViewController(this);
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
        tc.makeNavPoints();
        tc.mainPane.setPrefHeight(this.stage.getHeight()-this.topBar.getHeight()-20);
        tocBox.show(this.stage);        
    }
    
    public void hideTOCBox() {
        tocBox.hide();
        fc.requestFocus();
    }
    
    public void makeRecordBox() {
        if (this.recordBox!=null) return;
        try {
            this.recordBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RecordBox.fxml"),bundle);
            recordBox.getContent().add((Parent)fxmlLoader.load());
            RecordBoxController recordBoxController = (RecordBoxController)fxmlLoader.getController();
            recordBoxController.setMagazineViewController(this);
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
        fc.requestFocus();
    }
    
    public void showMediaBox() {
        mediaBox.setVisible(true);        
    }
    
    public void hideMediaBox() {
        mediaBox.setVisible(false);
        fc.requestFocus();
    }
    
    private void recalcLayout() {        
        double buttonWidth = 30;
        double buttonHeight = mainPane.getHeight();
        
        toLeftButton.setPrefWidth(buttonWidth);
        toLeftButton.setPrefHeight(buttonHeight);
        toLeftButton.setLayoutX(0);
        toLeftButton.setLayoutY(0);
        
        toRightButton.setPrefWidth(buttonWidth);
        toRightButton.setPrefHeight(buttonHeight);
        toRightButton.setLayoutX(mainPane.getWidth()-buttonWidth);
        toRightButton.setLayoutY(0);      
    }
    
    public SkyBooks getApp() {
        SkyBooks sb = (SkyBooks)app;
        return sb;
    }
    
    private void updatePosition(PageInformation pi) {
        getApp().updatePosition(bookCode, pi.pageIndex);
    }
    
    private void updateSetting(SkySetting setting) {
        getApp().updateSetting(setting);
    }
    
    public void processClose() {
        this.updatePosition(currentPageInformation);
        this.updateSetting(setting);
        this.fc.destory();
        getApp().deleteOpenedBook(this.bookCode);
        this.mainViewController.show();
        this.stage.close();
    }

    
    private void makeControls() {
        toLeftButton.setStyle("-fx-background-color: transparent;");
        toRightButton.setStyle("-fx-background-color: transparent;");
        this.mainPane.getChildren().add(toLeftButton);
        this.mainPane.getChildren().add(toRightButton);
        toLeftButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                fc.gotoPrev();
            }
        });
        toRightButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                fc.gotoNext();
            }
        });
        
        toLeftButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                toLeftButton.setStyle("-fx-background-color:#00000020;");
                toLeftButton.setText("<");
            }
        });
        
        toLeftButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                toLeftButton.setStyle("-fx-background-color:transparent;");
                toLeftButton.setText("");
            }
        }); 
        
        toRightButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                toRightButton.setStyle("-fx-background-color:#00000020;");
                toRightButton.setText(">");
            }
        });
        
        toRightButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                toRightButton.setStyle("-fx-background-color:transparent;");
                toRightButton.setText("");
            }
        });
        

        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                processClose();
            }
        });
        
        minimizeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = (Stage)minimizeButton.getScene().getWindow();
                stage.setIconified(true);
            }
        });
        
        
        expandButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = (Stage)expandButton.getScene().getWindow();
                if (!stage.isFullScreen()) {
                    Screen screen = Screen.getPrimary();
                    Rectangle2D bounds = screen.getVisualBounds();
                    stage.setX(bounds.getMinX());
                    stage.setY(bounds.getMinY());
                    stage.setWidth(bounds.getWidth());
                    stage.setHeight(bounds.getHeight());
//                    stage.setMaximized(true);
                    stage.setFullScreen(true);
                }else {
                    stage.setFullScreen(false);
                }
            }
        });
        
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showSearchBox();
            }
        });
        
        searchButton.setTooltip(new SkyTooltip(bundle.getString("search")));
        
        recordButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showRecordBox(stage.getX(),stage.getY()+topBar.getHeight());
            }
        });
        recordButton.setTooltip(new SkyTooltip(bundle.getString("record")));
        
        bookmarkButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                toggleBookmark();
                fc.requestFocus();
            }
        });
        bookmarkButton.setTooltip(new SkyTooltip(bundle.getString("bookmark")));
        
        tocButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showTOCBox(stage.getX(),stage.getY()+topBar.getHeight()+20);
            }
        });
        tocButton.setTooltip(new SkyTooltip(bundle.getString("table_of_contents")));

        
        thumbNailsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (thumbnailsBox.isVisible()) {
                    thumbnailsBox.setVisible(false);
                    fc.requestFocus();
                }else {
                    thumbnailsBox.setVisible(true);
                    fc.requestFocus();
                }

            }
        });
        
        thumbNailsButton.setTooltip(new SkyTooltip(bundle.getString("thumbnails")));
        
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
        
        // 임시 
        homeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainViewController.show();                
            }
        });
        
        homeButton.setTooltip(new SkyTooltip(bundle.getString("home")));
        
        
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setOffsetX(3.0f);
        ds.setColor(Color.DARKGRAY);

        applyImageFilter(homeImageView,homeButton,ds);
        applyImageFilter(tocImageView,tocButton,ds);
        applyImageFilter(recordImageView,recordButton,ds);
        applyImageFilter(thumbNailsImageView,thumbNailsButton,ds);
        applyImageFilter(searchImageView,searchButton,ds);
        applyImageFilter(bookmarkImageView,bookmarkButton,ds);
        

        
        this.mainPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                recalcLayout();
            }
        });
        
        this.mainPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                recalcLayout();
            }
        });
        
        this.stage.setTitle(getApp().getAppTitle());
        this.controlButtonsBox.setVisible(false);
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
    
    
    public void resetThumbnailMark() {
        for (int i=0; i<fc.book.spine.size(); i++) {
            ThumbnailItem box = this.getThumbnailItemByPageIndex(i);        
            box.setMarked(false);
        }
    }
    
    public void highlightThumbnail(int pageIndex) {
        resetThumbnailMark();
        for (int i=0; i<fc.book.spine.size(); i++) {
            ThumbnailItem box = this.getThumbnailItemByPageIndex(i);        
            if (i==pageIndex) {
                box.setMarked(true);
            }
        }
    }
    
    public void sendThumbnailToCenter(int pageIndex) {
        ThumbnailItem box = this.getThumbnailItemByPageIndex(pageIndex);        
        double w = thumbnailsBox.getContent().getBoundsInLocal().getWidth();
        double x = (box.getBoundsInParent().getMaxX() +
                box.getBoundsInParent().getMinX()) / 2.0;
        double vw = thumbnailsBox.getViewportBounds().getWidth();
        thumbnailsBox.setHvalue(thumbnailsBox.getHmax() * ((x - 0.5 * vw) / (w - vw)));
        highlightThumbnail(pageIndex);
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
    
    private void checkBookmark(PageInformation pi) {
        if (getApp().isBookmarked(pi)) {
            this.setBookmarked(true);
        }else {
            this.setBookmarked(false);
        }
    }
        
    double mainPaneHeight;
    double rootPaneHeight;
    double stageHeight;
    double mainPaneWidth;
    double rootPaneWidth;
    double stageWidth;
    
    private void adjustAspectRatio() {
        this.adjustAspectRatio(fc.book);
    }
    
    public void adjustAspectRatio(Book book) {
        if (stage.isFullScreen() || stage.isMaximized()) {
            return;
        }
        double bookWidth = book.fixedWidth;
        double bookHeight = book.fixedHeight;
        if (fc.isDoublePaged()) bookWidth*=2;
        double ratio = bookWidth/bookHeight;
        
        double sh = stage.getHeight();
        double sw = stage.getWidth();
        
        double mh = mainPane.getHeight();
        double mw = mainPane.getWidth();
        
        double dw = sw-mw;
        double dh = sh-mh;
        double nh=0,nw=0;
        
        if (stageWidth!=sw) {
            nh = (sw-dw)/ratio+dh;
            stage.setHeight(nh);
        }else if (stageHeight!=sh) {
            nw = (sh-dh)*ratio+dw;
            stage.setWidth(nw);
        }
        
        mainPaneHeight = mainPane.getHeight();
        rootPaneHeight = rootPane.getHeight();
        stageHeight = stage.getHeight();
        mainPaneWidth = mainPane.getWidth();
        rootPaneWidth = rootPane.getWidth();
        stageWidth = stage.getWidth();
    }
    
    public static void showBox(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informaiton");
        alert.setHeaderText("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    boolean wasMaximized = false;
    boolean isRecalcPrevented = false;
    
    public void setRecalcPrevented(boolean value) {
        this.isRecalcPrevented = value;
    }

    private void setupStageListener() {        
        // create a listener
        titleLabel.setText(fc.book.title);
        
        stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                // when iconized, 
                if (t1.booleanValue()==true) {
                    if (wasMaximized) {
                        stage.setIconified(false);
                        stage.setMaximized(false);
                    }
                }
            }
        });
        
        stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t1.booleanValue()==true) {
                    wasMaximized = true;
                }
                if (t.booleanValue()==true) {
                    wasMaximized = false;
                }                
            }
        });
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                processClose();                     
            }
        }); 
        
        final ChangeListener<Number> listener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, final Number newValue) {                
                if (Math.abs(oldValue.intValue()-newValue.intValue())<2) {
                    return;
                }
                stopRecalcTask();
                fc.setResizingEnabled(false);
                startAdjustRatioTask();
            }
        };
        
        stage.widthProperty().addListener(listener);
        stage.heightProperty().addListener(listener);
    }
    
    public Timer adjustRatioTimer = null;
    
    class AdjustRatioTask extends TimerTask {
        public void run() {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    fc.setResizingEnabled(true);
                    adjustAspectRatio();
                }
            });
        }
    }
    
    public void startAdjustRatioTask() {
        if (adjustRatioTimer!=null) {
            stopRecalcTask();
        }        
        final int firstInterval = 100;
        adjustRatioTimer = new Timer();
        AdjustRatioTask ct = new AdjustRatioTask();
        adjustRatioTimer.schedule(ct, firstInterval);
    }
    
    public void stopRecalcTask() {
        if (adjustRatioTimer!=null) adjustRatioTimer.cancel();
        adjustRatioTimer = null;
    }
    

    private void makeLayout() {
        fc.prefWidthProperty().bind(mainPane.widthProperty());
        fc.prefHeightProperty().bind(mainPane.heightProperty());
        fc.useSVGForTextContent(false);
        SkyProvider skyProvider = new SkyProvider();
        skyProvider.setKeyListener(new KeyDelegate());
        fc.setContentProvider(skyProvider);
        SkyProvider skyProviderForCache = new SkyProvider();
        skyProviderForCache.setKeyListener(new KeyDelegate());
        fc.setContentProviderForCache(skyProviderForCache);
        fc.setPageMovedListener(new PageMovedDelegate());
        fc.setMediaOverlayListener(new MediaOverlayDelegate());
        fc.setCacheListener(new CacheDelegate());
        fc.setClickListener(new ClickDelegate());
        fc.setSearchListener(new SearchDelegate());
        fc.setSelectionListener(new SelectionDelegate());
        fc.setHighlightListener(new HighlightDelegate());
        fc.setLicenseKey(getApp().getLicenseKey1());
        fc.setCalibrations(5,5);
        fc.setCaptureScale(0.2);
        fc.setStartPageIndex((int)startPositionInBook);
        mainPane.getChildren().add(fc);
        
        makeControls();
        makeBoxes();        
        
        // Dev Demo
        Platform.runLater(new Runnable() { // 400
            @Override
            public void run() {
                fc.setFilePath(bookPath);                
                fc.startInit();
                fc.startCaching();
                adjustAspectRatio();
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        adjustAspectRatio();
                                        setupStageListener();
                                        makeThumbnailsBox();  
                                        fc.requestFocus();
                                    }
                                });
                            }
                        },
                        200
                );
                // your code here
            }
        });       
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.bundle = rb;        
        Platform.runLater(new Runnable() { // 400
            @Override
            public void run() {
                autoStartPlayingWhenNewPagesLoaded = getApp().globalSetting.autoStartMediaOverlay;
                makeLayout();
            }
        });
    }    
}
