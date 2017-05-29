/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.BookInformation;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class BookDetailController implements Initializable {
    ResourceBundle bundle;
    
    @FXML
    AnchorPane mainPane;
    @FXML
    Button openButton;
    @FXML
    Button openFirstButton;
    @FXML
    Button deleteButton;
    @FXML
    Button deleteCachesButton;
    
    @FXML
    ImageView coverImageView;
    
    @FXML
    Label titleLabel;
    @FXML
    Label authorLabel;
    @FXML
    Label publisherLabel;
    
    @FXML
    Button closeButton;
    
    MainViewController mainViewController;
    BookInformation bi;
    
    public void setBookInformation(BookInformation bookInformation) {
        this.bi = bookInformation;
        this.loadBookInformation();
    }
    
    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }
    
    public void setupUI() {
        openButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    mainViewController.openBookByCode(bi.bookCode,false);
                    mainViewController.hideBookDetailBox();
                }
            }
        });
        
        openFirstButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    mainViewController.openBookByCode(bi.bookCode,true);
                    mainViewController.hideBookDetailBox();
                }
            }
        });
        
        deleteButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    Alert alert = new Alert(AlertType.WARNING,String.format(bundle.getString("delete_format"), bi.title), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        mainViewController.deleteBooByCode(bi.bookCode);
                        mainViewController.hideBookDetailBox();
                    }                    
                    
                }
            }
        });

        deleteCachesButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    Alert alert = new Alert(AlertType.WARNING, String.format(bundle.getString("delete_caches_format"), bi.title), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        mainViewController.deleteCachesByCode(bi.bookCode);
                        mainViewController.hideBookDetailBox();
                    }
                }
            }
        });
        
        closeButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    mainViewController.hideBookDetailBox();
                }
            }
        });
    }
    
    public void loadBookInformation() {
        try {
            titleLabel.setText(this.bi.title);
            authorLabel.setText(this.bi.creator);
            publisherLabel.setText(this.bi.publisher);
            System.out.println(bi.fileName);
            String coverPath = mainViewController.getCoverPath(bi);
            File file = new File(coverPath);
            Image image = new Image(file.toURI().toString());
            coverImageView.setImage(image);
            centerImage();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void centerImage() {
        Image img = coverImageView.getImage();
        if (img != null) {
            double w = 0;
            double h = 0;
            
            double ratioX = coverImageView.getFitWidth() / img.getWidth();
            double ratioY = coverImageView.getFitHeight() / img.getHeight();
            
            double reducCoeff = 0;
            if(ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }
            
            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;
            
            coverImageView.setX((coverImageView.getFitWidth() - w) / 2);
            coverImageView.setY((coverImageView.getFitHeight() - h) / 2);
            
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {   
        bundle = rb;
        setupUI();
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                loadBookInformation();
                                // do something
                            }
                        });
                    }
                },
                200
        );
    }    
    
}
