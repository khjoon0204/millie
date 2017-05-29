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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author skytree
 */
enum BookCellState {
    Downloaded,
    NotDownloaded,
    BeingDownloaded    
}

public class BookCellController implements Initializable {
    @FXML
    Label titleLabel;
    @FXML
    Label subLabel;
    @FXML
    ImageView coverImageView;   
    @FXML
    AnchorPane emptyCoverPane;
    @FXML
    ImageView cloudImageView;
    @FXML
    ProgressIndicator progressIndicator;
    
    BookInformation bookInformation;
    MainViewController mainViewController;
    
    public boolean coverExists() {
        return mainViewController.coverExists(bookInformation);
    }
    
    public void setBookInformation(BookInformation bi) {
        bookInformation = bi;
    }
    
    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }
    
    public void update() {
        if (coverExists()) {
            String coverPath = mainViewController.getCoverPath(bookInformation);
            File file = new File(coverPath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                coverImageView.setImage(image);
                DropShadow ds = new DropShadow();
                ds.setOffsetY(4.0f);
                ds.setOffsetX(4.0f);
                ds.setColor(Color.GRAY);
                coverImageView.setEffect(ds);
            }            
        }
        
        titleLabel.setText(bookInformation.title);
        String subText ="";
        if (!bookInformation.creator.isEmpty()) {
            subText = bookInformation.creator;
        }else {
            subText = bookInformation.publisher;
        }
        subText = subText.trim();
        subLabel.setText(subText);        
        processDisplay();
    }
    
    public void updateProgress(float progress) {
        this.progressIndicator.setProgress(progress);
    }
    
    public void setState(BookCellState state) {
        if (state==BookCellState.NotDownloaded) {
            cloudImageView.setVisible(true);            
            progressIndicator.setMaxSize(1,1);
        }else if (state==BookCellState.BeingDownloaded) {
            cloudImageView.setVisible(false);    
            cloudImageView.setImage(null);
            progressIndicator.setVisible(true);
            progressIndicator.setMaxSize(24,48);
            progressIndicator.setMinSize(24,48);
        }else if (state==BookCellState.Downloaded) {
            cloudImageView.setVisible(false);
            progressIndicator.setMaxSize(1,1);
            progressIndicator.setVisible(false);
        }
    }
    
    
    public void processDisplay() {
        if (coverExists()) {
            Image img = coverImageView.getImage();
            if (img != null) {
                emptyCoverPane.setVisible(false);
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
        }else {
            emptyCoverPane.setVisible(true);            
        }
    }

    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
