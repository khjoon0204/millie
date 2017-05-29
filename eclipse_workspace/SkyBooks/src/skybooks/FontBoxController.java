/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.ReflowableControl;
import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javafx.scene.text.Font;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author skytree
 */
public class FontBoxController implements Initializable {
    @FXML
    AnchorPane mainPane;
    
    @FXML
    Button increaseButton;
    
    @FXML
    Button decreaseButton;
    
    @FXML
    Button increaseLineButton;
    
    @FXML
    Button decreaseLineButton;
    
    @FXML
    Button whiteThemeButton;
    
    @FXML
    Button ivoryThemeButton;
    
    @FXML
    Button nightThemeButton;
    
    @FXML
    Button greenThemeButton;
    
    @FXML
    Button redThemeButton;
    
    @FXML
    Button closeButton;
    
    @FXML
    ListView fontListView;
    
    SkyPopup fontBox;
    
    BookViewController bc;
    ReflowableControl rc;
    
    ResourceBundle bundle;
    ObservableList<String> displayFontNames = FXCollections.observableArrayList();
    String fontFamilyNames[];
    String localeFamilyNames[];
    
    boolean fontDisabled = false;
    
    public void setFontEnabled(boolean enabled) {        
        this.fontDisabled = !enabled;
        this.checkSettings();
    }
    
    // 화면에 표시된 폰트 이름을 가지고 실제 폰트명을 찾는다.
    // 실제 엔진으로 전달되어야 하는 이름을 반환한다 
    // 여기서 오류가 발생할 수 있다 
    // localFontFamilyNames개수가 fontFamilyNames과 다르거나 순서가 다르거나 기타의 경우에 문제가 발생할 수 있다     
    public String getRealFontName(String familyName) {
        String fontName = "";
        for (int i=0; i<localeFamilyNames.length; i++) {
            if (localeFamilyNames[i].equalsIgnoreCase(familyName)) {
                fontName = fontFamilyNames[i];
            }
        }
        return fontName;
    }
    
    
    private void setupFonts() {
        localeFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        modifyFamilyNames();
        modifyLocaleNames();
        displayFontNames.add("Book Fonts");
        for (int i=0; i<localeFamilyNames.length; i++) {   
            if (this.isKorean()) {
                if (this.hasKorean(localeFamilyNames[i])) {
                    displayFontNames.add(localeFamilyNames[i]);
                }
            }else {
                displayFontNames.add(localeFamilyNames[i]);
            }
            
        }
        System.out.println("SetupFonts finished !!!!!!!!");
    }
    
    private void modifyFamilyNames() {
        for (int i=0; i<fontFamilyNames.length; i++) {            
            if (fontFamilyNames[i].equalsIgnoreCase("바탕체")) {
                fontFamilyNames[i]   = "Batangche";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("바탕")) {
                fontFamilyNames[i]    = "Batang";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("궁서체")) {
                fontFamilyNames[i]   = "GungsuhChe";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("궁서")) {
                fontFamilyNames[i]   = "Gungsuh";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("돋움체")) {
                fontFamilyNames[i]   = "DotumChe";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("돋움")) {
                fontFamilyNames[i]   = "Dotum";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("굴림체")) {
                fontFamilyNames[i]   = "GulimChe";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("굴림")) {
                fontFamilyNames[i]   = "Gulim";
            }
            if (fontFamilyNames[i].equalsIgnoreCase("맑은 고딕")) {
                fontFamilyNames[i]   = "Malgun Gothic";
            }
        }
        
    }
    
    private void modifyLocaleNames() {
        for (int i=0; i<localeFamilyNames.length; i++) {            
            if (localeFamilyNames[i].equalsIgnoreCase("Batangche")) {
                localeFamilyNames[i]   = "바탕체";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("Batang")) {
                localeFamilyNames[i]    = "바탕";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("GungsuhChe")) {
                localeFamilyNames[i]   = "궁서체";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("Gungsuh")) {
                localeFamilyNames[i]   = "궁서";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("DotumChe")) {
                localeFamilyNames[i]   = "돋움체";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("Dotum")) {
                localeFamilyNames[i]   = "돋움";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("GulimChe")) {
                localeFamilyNames[i]   = "굴림체";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("Gulim")) {
                localeFamilyNames[i]   = "굴림";
            }
            if (localeFamilyNames[i].equalsIgnoreCase("Malgun Gothic")) {
                localeFamilyNames[i]   = "맑은 고딕";
            }
        }
        
    }

    private boolean isKorean() {
        String language = System.getProperty("user.language");
        if (language.equalsIgnoreCase("ko")) return true;
        else return false;
    }
    
    private boolean hasKorean(String text) {
        if(text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
            return true;
        } else {
            return false;
        }
    }
    
    private int getFontIndex(String fontName) {
        int target = -1;
        String targetLocalName = "";
        for (int i=0; i<fontFamilyNames.length; i++) {            
            if (fontFamilyNames[i].equalsIgnoreCase(fontName)) {                
                targetLocalName = this.localeFamilyNames[i];
            }
        }
        for (int i=0; i<displayFontNames.size(); i++) {            
            if (displayFontNames.get(i).equalsIgnoreCase(targetLocalName)) {                
                target = i;
            }
        }
        return target;
    }
    
    public void setupControls() {
        setupFonts();
        
        decreaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.decreaseFont();
                checkSettings();
            }
        });
        increaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.increaseFont();
                checkSettings();
            }
        });
        decreaseLineButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.decreaseLineSpace();
                checkSettings();
            }
        });
        increaseLineButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.increaseLineSpace();
                checkSettings();
            }
        });
        whiteThemeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.changeTheme(0);
                checkSettings();
            }
        });
        ivoryThemeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.changeTheme(1);
                checkSettings();
            }
        });
        nightThemeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.changeTheme(2);
                checkSettings();
            }
        });
        greenThemeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.changeTheme(3);
                checkSettings();
            }
        });
        redThemeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.changeTheme(4);
                checkSettings();
            }
        });
        
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bc.hideFontBox();
            }
        });
        
        whiteThemeButton.setText(bundle.getString("white"));
        ivoryThemeButton.setText(bundle.getString("ivory"));
        nightThemeButton.setText(bundle.getString("night"));
        greenThemeButton.setText(bundle.getString("green"));
        redThemeButton.setText(bundle.getString("red"));
        
        fontListView.setItems(displayFontNames);
        fontListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String selectedFont = (String)fontListView.getSelectionModel().getSelectedItem();
                bc.changeFontName(getRealFontName(selectedFont));
            }
        });
        
        
        fontListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {            
            @Override
            public ListCell<String> call(ListView<String> arg0) {
                return new ListCell<String>() {
                    
                    @Override
                    protected void updateItem(String item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null) {
                            Pane pane = new Pane();
                            pane.setMinHeight(50);
                            pane.setPrefHeight(50);
                            pane.setMaxHeight(50);                            
                            Label fontLabel = new Label(item);                            
                            pane.getChildren().add(fontLabel);                            
                            fontLabel.setFont(Font.font(getRealFontName(item), 15));
                            fontLabel.setLayoutX(10);
                            fontLabel.setLayoutY(10);
                            pane.setPadding(new Insets( 5,  20,  5,  30));
                            pane.setPrefWidth((double)(fontListView.getWidth()-40));
                            setGraphic(pane);
                        }                        
                    }
                    
                };
            }
            
        });

    }
    
    public void checkSettings() {
        decreaseButton.setDisable(bc.setting.fontSize==0 || this.fontDisabled);
        increaseButton.setDisable(bc.setting.fontSize==bc.maxFontSizeIndex || this.fontDisabled);
        decreaseLineButton.setDisable(bc.setting.lineSpacing==0 || this.fontDisabled);
        increaseLineButton.setDisable(bc.setting.lineSpacing==bc.maxLineSpacingIndex || this.fontDisabled);
        
        if (bc.setting.theme==0) {
            whiteThemeButton.setStyle("-fx-border-width:3;-fx-border-color: #C3C3C3;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#ffffff, #f1f1f1);");
        }else {
            whiteThemeButton.setStyle("-fx-border-width:1;-fx-border-color: #C3C3C3;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#ffffff, #f1f1f1);");
        }
        if (bc.setting.theme==1) {
            ivoryThemeButton.setStyle("-fx-border-width:3;-fx-border-color: #e7e19a;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#fffcde, #e4e4b9);");
        }else {
            ivoryThemeButton.setStyle("-fx-border-width:1;-fx-border-color: #e7e19a;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#fffcde, #e4e4b9);");
        }
        if (bc.setting.theme==2) {
            nightThemeButton.setStyle("-fx-border-width:3;-fx-border-color:LightGray;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#484848, #8e8e8e);");
        }else {
            nightThemeButton.setStyle("-fx-border-width:1;-fx-border-color: LightGray;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#484848, #8e8e8e);");
        }
        if (bc.setting.theme==3) {
            greenThemeButton.setStyle("-fx-border-width:3;-fx-border-color:#a3b96b;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color:  linear-gradient(#f7ffe6, #cbdaa6);");
        }else {
            greenThemeButton.setStyle("-fx-border-width:1;-fx-border-color: #a3b96b;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#f7ffe6, #cbdaa6);");
        }
        if (bc.setting.theme==4) {
            redThemeButton.setStyle("-fx-border-width:3;-fx-border-color: #c79f9d;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#fff1f1, #e2b9b9);");
        }else {
            redThemeButton.setStyle("-fx-border-width:1;-fx-border-color:  #c79f9d;-fx-border-insets:0;-fx-border-radius:100;-fx-background-radius:100;-fx-background-color: linear-gradient(#fff1f1, #e2b9b9);");
        }
        
        int fontIndex = getFontIndex(bc.setting.fontName);
        if (fontIndex==-1) fontIndex = 0;
        fontListView.getSelectionModel().select(fontIndex);
        fontListView.getFocusModel().focus(fontIndex);
        fontListView.scrollTo(fontIndex);
        fontListView.setDisable(fontDisabled);     
        
        System.out.println("checkSetting "+bc.setting.fontName+" : "+fontIndex);
    }
    
    
    public void setBookViewController(BookViewController bc) {
        this.bc = bc;
        rc = bc.rc;
        fontBox =  (SkyPopup)mainPane.getScene().getWindow();
    }
            
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;
        setupControls();
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                checkSettings();
                                // do something
                            }
                        });
                    }
                },
                100
        );
        
    }    
    
}
