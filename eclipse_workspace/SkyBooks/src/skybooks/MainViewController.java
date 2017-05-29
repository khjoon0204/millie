/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.BookInformation;
import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.SegmentedButton;

/**
 * FXML Controller class
 *
 * @author skytree
 */

class SkyTooltip extends Tooltip {
    SkyTooltip(String title) {
        super.setText(title);
        super.setStyle("-fx-font: 13px 'Malgun Gothic'");
    }
}

public class MainViewController implements Initializable {    
    Stage stage;
    Application app;
    Scene scene;    
    @FXML
    GridView gridView;
    //------------------
    @FXML
    Button libraryButton;
    @FXML
    ImageView libraryImageView;
    @FXML
    Button sideButton;
    @FXML
    ImageView sideImageView;
    @FXML
    Button settingButton;
    @FXML
    ImageView settingImageView;
    @FXML
    Button logButton;
    @FXML
    ImageView logImageView;
    @FXML
    Button reloadButton;
    @FXML
    ImageView reloadImageView;
    
    // -----------------------
    
    @FXML
    Button clearButton;
    @FXML
    TextField searchField;
    @FXML
    AnchorPane switchPane;
    @FXML
    SplitPane splitPane;
    @FXML
    ListView categoryListView;
    
    @FXML
    Label statusLabel;
    
    
    @FXML
    private Button viewButton;
    
    @FXML
    private ImageView viewImageView;
    
    @FXML
    private TableView tableView;
    
    @FXML
    private StackPane viewStackPane;    

    
    ToggleButton switchButton0;
    ToggleButton switchButton1;
    ToggleButton switchButton2;
    ToggleButton switchButton3;
    
    ResourceBundle bundle;
    
    private SkyPopup bookDetailBox;
    ObservableList<BookInformation> bis  = null;
    ObservableList<BookInformation> tis  = null;
    ObservableList<Category> cts  = null;
    
    HashMap<String,BookInformation> ecu = new HashMap<String,BookInformation>();
    
    boolean isCategoryPaneShown = true;
    boolean isGridViewShown = false;
    
    public MainViewController getMainViewController() {
        return this;
    }
    
    public boolean coverExists(BookInformation bookInformation) {
        String coverPath = getCoverPath(bookInformation);
        File file = new File(coverPath);
        boolean exists = file.exists();
//        return false;
        return exists;
    }
    
    class CoverDownloadDelegate implements DownloadListener {
        BookCell cell;
        BookInformation bi;
        
        CoverDownloadDelegate(BookCell cell, BookInformation bi) {
            this.cell = cell;
            this.bi = bi;            
        }

        @Override
        public void onStated(String url, String targetPath) {}

        @Override
        public void onProgressed(String url, String targetPath, long currentSize, long fileSize) {}

        @Override
        public void onFinished(String url, String targetPath, long fileSize) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    cell.updateItem(bi, false);
                }
            });            
        }

        @Override
        public void onError(String msg, Exception e) {
            ecu.put(bi.url, bi);   
        }        
    }
    
    class BookDownloadDelegate implements DownloadListener {
        BookCell cell;
        BookInformation bi;
        
        BookDownloadDelegate(BookCell cell, BookInformation bi) {
            this.cell = cell;
            this.bi = bi;            
        }

        @Override
        public void onStated(String url, String targetPath) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    float progress = 0.25f;
                    cell.bookCellController.setState(BookCellState.BeingDownloaded);
                    cell.bookCellController.updateProgress(progress);
                }
            });
        }

        @Override
        public void onProgressed(String url, String targetPath, long currentSize, long fileSize) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    float progress = 0;
                    if (fileSize!=0) {
                        progress = (float)currentSize/(float)fileSize;
                    }
                    if (progress>0.25f) cell.bookCellController.updateProgress(progress);
                }
            });            

        }

        @Override
        public void onFinished(String url, String targetPath, long fileSize) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    cell.bookCellController.updateProgress(1.0f);
                    bi.isDownloaded = true;
                    getApp().updatedBookDownloaded(bi, true);
                    cell.bookCellController.setState(BookCellState.Downloaded);
                    reloadDataViews();
                }
            });            
        }

        @Override
        public void onError(String msg, Exception e) {
            ecu.put(bi.url, bi);       
            reloadDataViews();
        }        
    }
    
    public void refreshTableView() {
        reloadTableView();
    }
    
    class RowDownloadDelegate implements DownloadListener {
        BookInformation bi;
        
        RowDownloadDelegate( BookInformation bi) {
            this.bi = bi;            
        }

        @Override
        public void onStated(String url, String targetPath) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
//                    bi.downSize = 25;
//                    bi.fileSize = 100;
                   // tableView.refresh();    
//                   refreshTableView();
                    backupTableViewSort();
                }
            });
        }

        @Override
        public void onProgressed(String url, String targetPath, long currentSize, long fileSize) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    bi.downSize = (int)currentSize;
                    bi.fileSize = (int)fileSize;  
                }
            });            

        }

        @Override
        public void onFinished(String url, String targetPath, long fileSize) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    getApp().updatedBookDownloaded(bi, true);
                    reloadDataViews();
                }
            });            
        }

        @Override
        public void onError(String msg, Exception e) {
            ecu.put(bi.url, bi);        
        }        
    }
    
    class OpenAfterDownloadDelegate implements DownloadListener {
        BookInformation bi;
        
        OpenAfterDownloadDelegate( BookInformation bi) {
            this.bi = bi;            
        }

        @Override
        public void onStated(String url, String targetPath) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onProgressed(String url, String targetPath, long currentSize, long fileSize) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {      
                }
            });            

        }

        @Override
        public void onFinished(String url, String targetPath, long fileSize) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    bi.isDownloaded = true;
                    getApp().updatedBookDownloaded(bi, true);
                    reloadDataViews();
                    openBook(bi,true);
                }
            });            
        }

        @Override
        public void onError(String msg, Exception e) {
            ecu.put(bi.url, bi);      
            reloadDataViews();
        }        
    }


    
    

    class  BookCell extends GridCell<BookInformation> {
        Parent root = null;
        BookCellController bookCellController = null;        
        
        private Parent makeCell(BookInformation item) {
            Parent parent = null;
            BookCell thisCell = this;
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BookCell.fxml"));
                parent = (Parent)fxmlLoader.load();
                bookCellController = (BookCellController)fxmlLoader.getController();
                bookCellController.setBookInformation(item);
                bookCellController.setMainViewController(getMainViewController());
                
                parent.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        BookInformation bi = bookCellController.bookInformation;
                        if (bi.isDownloaded) {
                            if (mouseEvent.getButton()==MouseButton.PRIMARY) {
                                openBookByCode(bi.bookCode,false);
                                System.out.println("left click detected on "+bi.title  + mouseEvent.getSource());
                            }else if (mouseEvent.getButton()==MouseButton.SECONDARY) {
                                System.out.println("right click detected on "+bi.title  + mouseEvent.getSource());
                                showBookDetailBox(mouseEvent.getSceneX(),mouseEvent.getSceneY(),bi);
                            }
                        }else { // start download from server
                            if (mouseEvent.getButton()==MouseButton.PRIMARY) {
                                bookCellController.setState(BookCellState.BeingDownloaded);
                                bookCellController.updateProgress(0.1f);
                                String targetPath = getFilePath(bi);
                                BookDownloadDelegate bd = new BookDownloadDelegate(thisCell,bi);
                                getApp().bookExecutor.submit(new DownloadTask(bi.url,targetPath,bd));                                
                            }
                        }
                    }
                });
                
            }catch(Exception e) {
                e.printStackTrace();
            }
            return parent;
        }
        
        @Override
        protected void updateItem(BookInformation item, boolean empty) {
            // TODO Auto-generated method stub
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {            
                try {
                    if (root==null) {
                        root = makeCell(item);
                    }else {
                        bookCellController.setBookInformation(item);
                    }
                    if (!coverExists(item) && ecu.get(item.coverUrl)==null && !item.coverUrl.isEmpty()) {
                        String coverPath = getCoverPath(item);
                        CoverDownloadDelegate cd = new CoverDownloadDelegate(this,item);
                        getApp().coverExecutor.submit(new DownloadTask(item.coverUrl,coverPath,cd));
                    }
                    if (!item.isDownloaded) bookCellController.setState(BookCellState.NotDownloaded);
                    else bookCellController.setState(BookCellState.Downloaded);
                    bookCellController.update();
                    setGraphic(root);
                }catch(Exception e) {}
            }
        }
    }
    
    class BookCellFactory implements Callback<GridView<BookInformation>, GridCell<BookInformation>> {
        @Override
        public GridCell<BookInformation> call(GridView<BookInformation> listview) {
            return new BookCell();
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

    
    public void setApplication(Application app) {
        this.app = app;
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public SkyBooks getApp() {
        SkyBooks sb = (SkyBooks)app;
        return sb;
    }
    
    public void setScene(Scene scene) {
        this.scene = scene;
    }
    
    public void displayStatus(String msg) {
        statusLabel.setText(msg);
    }
    
    public void setupTableView() {
        tableView.setStyle("-fx-focus-color: transparent;");
        
        TableColumn titleColumn = new TableColumn();
        titleColumn.setText(this.bundle.getString("title"));
        titleColumn.setCellValueFactory(new PropertyValueFactory("fxTitle"));        
        titleColumn.setPrefWidth(390);
        
        TableColumn creatorColumn = new TableColumn();
        creatorColumn.setText(this.bundle.getString("author"));
        creatorColumn.setCellValueFactory(new PropertyValueFactory("fxCreator"));
        
        TableColumn lastReadColumn = new TableColumn();
        lastReadColumn.setText(this.bundle.getString("last_read"));
        lastReadColumn.setCellValueFactory(new PropertyValueFactory("fxLastRead"));
        
        
        TableColumn purchaseDateColumn = new TableColumn();
        purchaseDateColumn.setText(this.bundle.getString("purchase"));
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory("fxPurchaseDate"));

        
        TableColumn isDownloadedColumn = new TableColumn();
        isDownloadedColumn.setText(this.bundle.getString("download"));
        isDownloadedColumn.setCellValueFactory(new PropertyValueFactory("fxIsDownloaded"));
        isDownloadedColumn.setStyle( "-fx-alignment: CENTER;");
        
        tableView.getColumns().addAll(titleColumn,creatorColumn,lastReadColumn,purchaseDateColumn,isDownloadedColumn);
        
        tableView.setRowFactory(tv -> {
            TableRow<BookInformation> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (! row.isEmpty() && mouseEvent.getClickCount() == 1) {
                    
                    BookInformation bi = row.getItem();
                    if (bi.isDownloaded) {
                        if (mouseEvent.getButton()==MouseButton.PRIMARY) {
                            openBookByCode(bi.bookCode,false);
                            System.out.println("left click detected on "+bi.title  + mouseEvent.getSource());
                        }else if (mouseEvent.getButton()==MouseButton.SECONDARY) {
                            System.out.println("right click detected on "+bi.title  + mouseEvent.getSource());
                            showBookDetailBox(mouseEvent.getSceneX(),mouseEvent.getSceneY(),bi);
                        }
                    }else { // start download from server
                        if (mouseEvent.getButton()==MouseButton.PRIMARY) {
                            String targetPath = getFilePath(bi);
                            RowDownloadDelegate rd = new RowDownloadDelegate(bi);
                            getApp().bookExecutor.submit(new DownloadTask(bi.url,targetPath,rd));
                        }
                    }
                    System.out.println(bi.title);                    
                }
            });
            return row ;
        });
    }

    public void openAfterDownloadBook(String url) {
        User currentUser = getApp().currentUser;
        if (currentUser.userId.isEmpty()) {
            showLoginView();
            return;
        }   
        BookInformation bi = getApp().fetchBookInformationByURL(url);
        if (bi==null) {
            return;
        }
        if (!bi.isDownloaded) {
            String targetPath = getFilePath(bi);
            OpenAfterDownloadDelegate od = new OpenAfterDownloadDelegate(bi);
            getApp().bookExecutor.submit(new DownloadTask(bi.url,targetPath,od));
        }else {
            openBook(bi,false);
        }
    }
    
    public void setupUI() {
        this.stage.setTitle(getApp().getAppTitle());
        switchButton0 = new ToggleButton(this.bundle.getString("by_title"));
        switchButton1 = new ToggleButton(this.bundle.getString("by_author"));
        switchButton2 = new ToggleButton(this.bundle.getString("by_last_read"));
        switchButton3 = new ToggleButton(this.bundle.getString("by_purchase"));         
        SegmentedButton segmentedButton = new SegmentedButton(switchButton0, switchButton1, switchButton2, switchButton3);  
        segmentedButton.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
        switchPane.getChildren().add(segmentedButton); 
        
        segmentedButton.setLayoutX(5);
        segmentedButton.setLayoutY(8);
        
        ToggleGroup tg = new ToggleGroup();
        switchButton0.setToggleGroup(tg);
        switchButton1.setToggleGroup(tg);
        switchButton2.setToggleGroup(tg);
        switchButton3.setToggleGroup(tg);
        segmentedButton.setToggleGroup(tg);
        
        if (getApp().globalSetting.sortType==1) switchButton0.setSelected(true);
        else if (getApp().globalSetting.sortType==2) switchButton1.setSelected(true);
        else if (getApp().globalSetting.sortType==3) switchButton2.setSelected(true);
        else if (getApp().globalSetting.sortType==4) switchButton3.setSelected(true);
        
        setupTableView();
        
        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle,Toggle newToggle) {
                if (newToggle==null) {
                    tg.selectToggle(oldToggle);
                }else {
                    getApp().globalSetting.sortType = getSortType();
                    getApp().updateSetting(getApp().globalSetting);
                    reloadDataViews();
                }
            }
        });

        clearButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                searchField.clear();
                return;
            }
        });
        
        searchField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE){
                searchField.clear();
                return;
            }
            if (event.getCode() == KeyCode.ENTER){
                reloadDataViews();
                return;
            }
        });
        
        
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            reloadDataViews();
        });
        
        
        libraryButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    if (getApp().getSyncListener()==null) {
                        openFileChooser();
                    }else {
                        launchWeb(getApp().getHomeURL());                    
                    }
                }
            }
        });
        
        sideButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    if (isCategoryPaneShown) hideCategoryPane();
                    else showCategoryPane();
                }
            }
        });
        
        viewButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    if (isGridViewShown) {
                        showTableView();
                        getApp().globalSetting.isThumbnailView = false;
                    }else {
                        backupTableViewSort();
                        showGridView();
                        getApp().globalSetting.isThumbnailView = true;
                    }
                }
            }
        });
        
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setOffsetX(3.0f);
        ds.setColor(Color.DARKGRAY);

        applyImageFilter(libraryImageView,libraryButton,ds);
        applyImageFilter(sideImageView,sideButton,ds);
        applyImageFilter(settingImageView,settingButton,ds);
        applyImageFilter(reloadImageView,reloadButton,ds);
        applyImageFilter(logImageView,logButton,ds);
        
        libraryButton.setTooltip(new SkyTooltip(getApp().getHomeName()));
        sideButton.setTooltip(new SkyTooltip(this.bundle.getString("show_hide_sideview")));
        settingButton.setTooltip(new SkyTooltip(this.bundle.getString("setting")));
        reloadButton.setTooltip(new SkyTooltip(this.bundle.getString("reload")));
        logButton.setTooltip(new SkyTooltip(this.bundle.getString("login")));
        
        logButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                toggleLogin();
            }
        });
        
        settingButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                showSettingView();
            }
        });
        
        reloadButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                getApp().processSync();
            }
        });
        
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });
        
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        System.out.println(filePath);
                        getApp().installLocalEpub(filePath);
                    }
                }
                reload();
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                processClose();
            }
        });
        
        if (getApp().currentUser.userId.isEmpty()) {
            setLoginIcon(true);            
        }else {
            setLoginIcon(false);            
        }
        
        if (getApp().globalSetting.isThumbnailView) {
            showGridView();
        }else {
            showTableView();
        }

        
        displayStatus("총 210권 (32권 다운로드 됨)");
    }
    
    public void launchWeb(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());            
        }catch (Exception e) {
            e.printStackTrace();            
        }
    }
    
    
    public void checkVersion() {
        String currentVersion = "1.0.1";
        String[] currentDigits = currentVersion.split("\\.");
        String[] digits;
        
        boolean firstDigitChanged = false;
        boolean secondDigitChanged = false;
        boolean thirdDigitChanged = false;
        boolean versionChanged = false;
        
        try {
            String latestVersion = this.getApp().getLatestVersion();
            digits = latestVersion.split("\\.");
            
            if (digits.length==1 || digits.length==2) {
                return;
            }
            
            if (!currentDigits[0].equalsIgnoreCase(digits[0])) {
                firstDigitChanged = true;
                versionChanged = true;
            }
            
            if (!currentDigits[1].equalsIgnoreCase(digits[1])) {
                secondDigitChanged = true;
                versionChanged = true;
            }
            
            if (!currentDigits[2].equalsIgnoreCase(digits[2])) {
                thirdDigitChanged = true;
                versionChanged = true;
            }            
            
            if (versionChanged) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                
                alert.setTitle(String.format(this.bundle.getString("new_version_message"),latestVersion));                
                String s = String.format(this.bundle.getString("request_update"),latestVersion); 
                alert.setContentText(s);
                Optional<ButtonType> result = alert.showAndWait();
                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            launchWeb(getApp().getDownloadURL());
                                            System.exit(0);
                                        }
                                    });
                                }
                            },
                            100
                    );
                }else {
                    if (firstDigitChanged) {
                        System.exit(0);
                    }
                }
            }
            
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }
    
    public void setViewIcon(boolean isGridViewShown) {
        Image image = null;
        if (!isGridViewShown) {
            image = new Image("resources/tableview.png");
            viewButton.setTooltip(new SkyTooltip(this.bundle.getString("tableview")));
        }else {
            image = new Image("resources/gridview.png");
            viewButton.setTooltip(new SkyTooltip(this.bundle.getString("gridview")));
        }
        if (image!=null) {
            viewImageView.setImage(image);
        }                           
    }
    
    public void setLoginIcon(boolean enabled) {
        Image image = null;
        if (!enabled) {
            image = new Image("resources/logout.png");
            logButton.setTooltip(new SkyTooltip(this.bundle.getString("logout")));
        }else {
            image = new Image("resources/login.png");
            logButton.setTooltip(new SkyTooltip(this.bundle.getString("login")));
        }
        if (image!=null) {
            logImageView.setImage(image);
        }                    
    }
    
    public void showLoginView() {
        if (getApp().getSyncListener()==null) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginView.fxml"),this.bundle);
            Parent root = (Parent)fxmlLoader.load();
            LoginViewController loginViewController = (LoginViewController)fxmlLoader.getController();
            loginViewController.setApplication(getApp());
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            loginViewController.setStage(stage);  
            loginViewController.setMainViewController(this);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    public void toggleLogin() { 
        if (getApp().getSyncListener()==null) return;
        User currentUser = getApp().currentUser;
        if (currentUser.userId.isEmpty()) {            
            showLoginView();
        }else {
            this.setLoginIcon(true);
            getApp().processLogout();            
        }
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
    
    public void showCategoryPane() {
        isCategoryPaneShown = true;
        splitPane.setDividerPosition(0,0.19f);
        splitPane.setDividerPosition(1,0.81f);
    }
    
    public void hideCategoryPane() {
        isCategoryPaneShown = false;
        splitPane.setDividerPosition(0,0.0f);
        splitPane.setDividerPosition(1,1.0f);
    }
    
    
    public void showGridView() {
        isGridViewShown = true;
        this.gridView.setVisible(true);
        this.tableView.setVisible(false);
        this.gridView.toFront();
        this.setViewIcon(isGridViewShown);
        switchPane.setVisible(true);
    }
    
    public void showTableView() {
        isGridViewShown = false;
        this.gridView.setVisible(false);
        this.tableView.setVisible(true);
        this.tableView.toFront();
        this.setViewIcon(isGridViewShown);
        switchPane.setVisible(false);
    }
    
    public void processClose() {
        if (getApp().openedBookList.size()==0) {
            this.backupTableViewSort();
            getApp().updateSetting(getApp().globalSetting);
            getApp().closeDatabase();
            getApp().terminateApp();
        }else {
            hide();
        }
    }
    
    public String getFilePath(BookInformation bi) {
        return getApp().getBooksDirectory()+File.separator+bi.fileName;
    }
    
    public String getCoverPath(BookInformation bi) {
        return getApp().getCoversDirectory()+File.separator+bi.fileName.replace(".epub", ".jpg");
    }
    
    public void showSettingView() {
        try {
            SkyPopup settingBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingView.fxml"),bundle);
            settingBox.getContent().add((Parent)fxmlLoader.load());
            SettingViewController settingViewController = (SettingViewController)fxmlLoader.getController();
            settingViewController.setApplication(getApp());
            settingViewController.setStage(stage);  
            settingViewController.setMainViewController(this);
            settingBox.setController(settingViewController);
            settingBox.setAutoHide(true);
            settingBox.setX(this.stage.getX() + this.stage.getWidth() / 2 - settingBox.getWidth() / 2);
            settingBox.setY(this.stage.getY() + (this.stage.getHeight() / 2 - settingBox.getHeight() / 2)/1.5);
            
            settingBox.show(this.stage);
            
            settingBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                    settingViewController.processClose();
                }
            });
            
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    public void openReflowableBookByCode(int code,boolean fromStart) {
        try {
            BookInformation bi = getApp().fetchBookInformation(code);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BookView.fxml"),this.bundle);
            Parent root = (Parent)fxmlLoader.load();
            BookViewController bookViewController = (BookViewController)fxmlLoader.getController();
            bookViewController.setApplication(getApp());
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(scene);
            bookViewController.setStage(stage);
            bookViewController.setBookInformation(bi);
            
            stage.initStyle(StageStyle.DECORATED);
            
            ResizeHelper.addResizeListener(stage);
            bookViewController.setBookCode(bi.bookCode);
            if (!fromStart) bookViewController.setStartPositionInBook(bi.position);
            else bookViewController.setStartPositionInBook(0);
            bookViewController.setMainViewController(this);
            bookViewController.setBookPath(getApp().getBooksDirectory()+File.separator+bi.fileName);
            stage.show();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void openFixedBookByCode(int code,boolean fromStart) {
        try {
            BookInformation bi = getApp().fetchBookInformation(code);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MagazineView.fxml"),this.bundle);
            Parent root = (Parent)fxmlLoader.load();
            MagazineViewController magazineViewController = (MagazineViewController)fxmlLoader.getController();
            magazineViewController.setApplication(getApp());
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(scene);
            magazineViewController.setStage(stage);
            stage.initStyle(StageStyle.DECORATED);
            ResizeHelper.addResizeListener(stage);
            magazineViewController.setBookCode(bi.bookCode);
            if (!fromStart) magazineViewController.setStartPositionInBook(bi.position);
            else magazineViewController.setStartPositionInBook(0);
            magazineViewController.setMainViewController(this);
            magazineViewController.setBookPath(getApp().getBooksDirectory()+File.separator+bi.fileName);
            stage.show();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void openBookByCode(int code,boolean fromStart) {
        try {            
            BookInformation bi = getApp().fetchBookInformation(code);
            if (getApp().isOpened(bi)) return;            
            else getApp().addOpenedBook(bi);
            this.hide();
            if (!bi.isFixedLayout) {
                this.openReflowableBookByCode(code,fromStart);
            }else {
                this.openFixedBookByCode(code,fromStart);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void openBook(BookInformation bi,boolean fromStart) {
        try {            
            if (bi==null) return;
            int code = bi.bookCode;
            if (getApp().isOpened(bi)) return;            
            else getApp().addOpenedBook(bi);
            this.hide();
            if (!bi.isFixedLayout) {
                this.openReflowableBookByCode(code,fromStart);
            }else {
                this.openFixedBookByCode(code,fromStart);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void show() {
        this.stage.show();        
    }
    
    public void hide() {
        this.stage.hide();
    }
    
    
    public void deleteBooByCode(int code) {
        getApp().deleteBookByBookCode(code);    
        this.reload();
    }
    
    public void deleteCachesByCode(int code) {
        getApp().deleteCachesByBookCode(code);                
    }
    
    public void makeBookDetailBox() {
        if (this.bookDetailBox!=null) return;
        try {
            this.bookDetailBox = new SkyPopup();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BookDetail.fxml"),bundle);
            Parent parent = (Parent)fxmlLoader.load();
            bookDetailBox.getContent().add(parent);            
            BookDetailController bookDetailBoxController = (BookDetailController)fxmlLoader.getController();
            bookDetailBoxController.setMainViewController(this);
            bookDetailBox.setController(bookDetailBoxController);
            bookDetailBox.setAutoHide(true);
            bookDetailBox.setOnAutoHide(new EventHandler<Event>() {
                public void handle(Event event) {
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }    
    
    public void showBookDetailBox(double x,double y,BookInformation bi) {
        if (this.bookDetailBox==null) {
            this.makeBookDetailBox();
        }
        bookDetailBox.setX(x+150);
        bookDetailBox.setY(y+120);  
        BookDetailController bc = (BookDetailController)bookDetailBox.controller;
        bc.setBookInformation(bi);
        bookDetailBox.show(this.stage);
    }
    
    public void hideBookDetailBox() {
        bookDetailBox.hide();
    }
    
    private int getSortType() {
        int res = 1;
        try {
            if (switchButton0.isSelected()) res = 1;
            else if (switchButton1.isSelected()) res = 2;
            else if (switchButton2.isSelected()) res = 3;
            else if (switchButton3.isSelected()) res = 4;
            return res;
        }catch(Exception e) {}
        return res;
    }
    
    private String getSearchKey() {
        return searchField.getText();
    }
    
    public void openFileChooser() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(this.bundle.getString("open_book_file"));
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Epub Files", "*.epub"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(this.stage);
            if (selectedFile != null) {
                this.getApp().installLocalEpub(selectedFile.getPath());
                this.reload();
//                this.stage.display(selectedFile);
            }
            
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void reloadBis() {
        ArrayList<BookInformation> bil = this.getApp().fetchBookInformations(this.getSortType(),this.getSearchKey(),getApp().globalSetting.categoryId);
        bis = FXCollections.observableArrayList(bil);
        tis = FXCollections.observableArrayList(bil);
        System.out.println("");
    }
    
    public void reloadGridView() {
        ArrayList<BookInformation> bil = this.getApp().fetchBookInformations(this.getSortType(),this.getSearchKey(),getApp().globalSetting.categoryId);
        bis = FXCollections.observableArrayList(bil);
        tis = FXCollections.observableArrayList(bil);
        gridView.setCellWidth(180);
        gridView.setCellHeight(180);
        gridView.setHorizontalCellSpacing(10);
        gridView.setVerticalCellSpacing(10);
        gridView.setItems(bis);
        gridView.setCache(true);
        gridView.setCacheShape(true);
        gridView.setCacheHint(CacheHint.SPEED);        
        gridView.setCellFactory(new BookCellFactory());   
        displayStatus();
    }
    
    TableColumn getColumnByName(String columnName) {
        if (columnName==null || columnName.isEmpty()) return null;
        ObservableList<TableColumn> columns = tableView.getColumns();
        for (int i=0; i<columns.size();i++) {
            TableColumn col = columns.get(i);
            if (col.getText().equalsIgnoreCase(columnName)) return col; 
        }
        return null;
    }
    
    private void backupTableViewSort() {
        if (tableView.getSortOrder().size()>0) {
            TableColumn sortColumn = (TableColumn) tableView.getSortOrder().get(0);            
            SortType st = sortColumn.getSortType();
            getApp().globalSetting.tableSortName = sortColumn.getText();
            String stName = st.name();
            if (stName.equalsIgnoreCase("ASCENDING")) getApp().globalSetting.tableSortDirection = 0;
            else getApp().globalSetting.tableSortDirection = 1;            
            getApp().updateSetting(getApp().globalSetting);
        }
    }
    
    private void restoreTableViewSort() {
        TableColumn target = this.getColumnByName(getApp().globalSetting.tableSortName);
        if (target!=null) {
            tableView.getSortOrder().add(target);
            if (getApp().globalSetting.tableSortDirection==0) {
                target.setSortType(SortType.ASCENDING);
            }else {
                target.setSortType(SortType.DESCENDING);
            }
        }        
    }
    
    public void reloadTableView() {
        // Backup SortType in TableView
        this.backupTableViewSort();
        ArrayList<BookInformation> bil = this.getApp().fetchBookInformations(this.getSortType(),this.getSearchKey(),getApp().globalSetting.categoryId);
        bis = FXCollections.observableArrayList(bil);
        tis = FXCollections.observableArrayList(bil);
        System.out.println("");
        tableView.setItems(tis);
        this.restoreTableViewSort();
        displayStatus();
    }
    
    
    public void reloadDataViews() {
        int sortType = this.getSortType();
        ArrayList<BookInformation> bil = this.getApp().fetchBookInformations(sortType,this.getSearchKey(),getApp().globalSetting.categoryId);
        bis = FXCollections.observableArrayList(bil);
        tis = FXCollections.observableArrayList(bil);
        System.out.println("");
        
        gridView.setCellWidth(180);
        gridView.setCellHeight(180);
        gridView.setHorizontalCellSpacing(10);
        gridView.setVerticalCellSpacing(10);
        gridView.setItems(bis);
        gridView.setCache(true);
        gridView.setCacheShape(true);
        gridView.setCacheHint(CacheHint.SPEED);        
        gridView.setCellFactory(new BookCellFactory());   
        
        tableView.setItems(tis);
        this.restoreTableViewSort();
        displayStatus();
    }
    
    private void displayStatus() {
        int count = bis.size();
        String ci = getApp().globalSetting.categoryId;
        String cn = "";
        if (ci.isEmpty()) {
            cn = this.bundle.getString("all_books");
        }else if (ci.equalsIgnoreCase("download")) {
            cn = this.bundle.getString("download");
        }else {
            Category ca = this.getApp().fetchCategory(ci);
            cn = ca.categoryName;
        } 
        String tt = this.bundle.getString("book_count");
        String dn = String.format(tt,count,cn);
        displayStatus(dn);        
    }
    
    
    
    public void reloadCategoryListView() {
        ArrayList<Category> cl = this.getApp().fetchAllCategories();
        Category ac = new Category();
        ac.categoryName = bundle.getString("all_books");
        ac.categoryId = "";
        cl.add(0,ac);
        
        Category dc = new Category();
        dc.categoryName = bundle.getString("downloaded");
        dc.categoryId = "DOWNLOAD";
        cl.add(1,dc);
        
        
        cts = FXCollections.observableArrayList(cl);
        System.out.println("");
        
        categoryListView.setItems(cts);        
        categoryListView.setCellFactory(new Callback<ListView<Category>, ListCell<Category>>(){
            @Override
            public ListCell<Category> call(ListView<Category> p) {
                ListCell<Category> cell = new ListCell<Category>(){
                    @Override
                    protected void updateItem(Category item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null) {
                            setText(item.categoryName);
                        }
                    }
                };
                return cell;
            }
        });
        categoryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {            
            try {
                backupTableViewSort();
                Category item = (Category)categoryListView.getSelectionModel().getSelectedItem();
                getApp().globalSetting.categoryId =  item.categoryId;
                reloadDataViews();
                restoreTableViewSort();
            }catch(Exception e) {}

        });
        
        int ci = 0;        
        for (int i=0; i<cts.size();i++) {
            Category cc = cts.get(i);
            if (cc.categoryId.equalsIgnoreCase(getApp().globalSetting.categoryId)) {
                ci = i;
            }
        }        
        categoryListView.getSelectionModel().select(ci);
    }
    
    public void reloadCategories() {
        ArrayList<Category> cl = this.getApp().fetchAllCategories();
        Category ac = new Category();
        ac.categoryName = "All Books";
        ac.categoryId = "";
        cl.add(0,ac);
        cts = FXCollections.observableArrayList(cl);        
        System.out.println("");
    }
    
    public void reload() {
        reloadCategoryListView();        
        reloadDataViews();
    }
    
    public void checkBookToOpen() {
        String bookToOpen = null;
        for (String parameter:getApp().rawParameters) {
            if (parameter.contains(this.getApp().getCUSName()+"://")) {
                int pos = parameter.indexOf("=");
                bookToOpen = parameter.substring(pos+1);
            }
        }
        if (bookToOpen!=null) {
            this.openAfterDownloadBook(bookToOpen);
        }
    }
    
    private void startInit() { 
        setupUI();
        reload();
        checkVersion();
        checkBookToOpen();
        display("startInit end");
    }
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        display("initialize start");
        this.bundle = rb;
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                display("startInit start");
                                startInit();        
                            }
                        });
                    }
                },
                10
        );
    }    
    
}
