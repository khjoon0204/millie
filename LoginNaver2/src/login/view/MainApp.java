package login.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	public static LoginWebviewController loginWebviewController;
	
    public Stage primaryStage;
    
    private final double MINIMUM_WINDOW_WIDTH = 800.0;
    private final double MINIMUM_WINDOW_HEIGHT = 600.0;
    
    
    public MainApp() {
		super();
		// TODO Auto-generated constructor stub
		
	}

	@Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("로그인");
        this.primaryStage.setMinWidth(MINIMUM_WINDOW_WIDTH);
        this.primaryStage.setMinHeight(MINIMUM_WINDOW_HEIGHT);

        //initRootLayout();
        gotoRoot();
        primaryStage.show();
    }


    public void gotoWebview(int where) {
        try {
            LoginWebviewController controller = (LoginWebviewController) replaceSceneContent("LoginWebview.fxml");
            controller.setMainApp(this);
            controller.setLoginWhere(where);
            controller.webengineLoad();  
            controller.setMainApp(this);
            loginWebviewController = controller;
        } catch (Exception ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void gotoRoot() {
        try {
            RootController controller = (RootController) replaceSceneContent("Root.fxml");
            controller.setMainApp(this);
        } catch (Exception ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void gotoLoginSuccess() {
        try {
        	LoginSuccessController controller = (LoginSuccessController) replaceSceneContent("LoginSuccess.fxml");
            controller.setMainApp(this);
        } catch (Exception ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Node replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = MainApp.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(MainApp.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        }
        
        // Store the stage width and height in case the user has resized the window
        double stageWidth = primaryStage.getWidth();
        if (!Double.isNaN(stageWidth)) {
            stageWidth -= (primaryStage.getWidth() - primaryStage.getScene().getWidth());
        }
        
        double stageHeight = primaryStage.getHeight();
        if (!Double.isNaN(stageHeight)) {
            stageHeight -= (primaryStage.getHeight() - primaryStage.getScene().getHeight());
        }
        
        Scene scene = new Scene(page);
        if (!Double.isNaN(stageWidth)) {
            page.setPrefWidth(stageWidth);
        }
        if (!Double.isNaN(stageHeight)) {
            page.setPrefHeight(stageHeight);
        }
        
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        return (Node) loader.getController();
    }
    
    
    
    

    
    
    /**
     * 메인 스테이지를 반환한다.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}