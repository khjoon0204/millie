package login.view;

import java.net.URL;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import login.callback.MyURLConnectionCallback;
import login.protocol.MyURLStreamHandlerFactory;
import netscape.javascript.JSObject;

public class LoginWebviewController extends AnchorPane implements MyURLConnectionCallback {


	// login page
	public final static String NAVER_LOGINPAGE		= "https://khjoon0204.github.io/millie/loginpage/naver/naverlogin.html";
	public final static String KAKAO_LOGINPAGE 		= "https://khjoon0204.github.io/millie/loginpage/kakao/kakaologin.html";
	public final static String FACEBOOK_LOGINPAGE 	= "https://khjoon0204.github.io/millie/loginpage/facebook/facebooklogin.html";
	
	
	public final static String PROTOCOL_SCHEME 	= "millie";
	
	@FXML
	private WebView webView;
	
	private WebEngine webEngine;
	private Stage popupStage;
	
	// 메인 애플리케이션 참조
    private MainApp mainApp;
   
    
    // 로그인 종
    private int loginWhere = 0; // 0: 네이버, 1: 카카오, 2: 페이스북  
    
	/**
     * 생성자.
     * initialize() 메서드 이전에 호출된다.
     */
    public LoginWebviewController() {
    }

    /**
     * 컨트롤러 클래스를 초기화한다.
     * fxml 파일이 로드되고 나서 자동으로 호출된다.
     */
    @FXML
    private void initialize() {
    	
    	// init webengine
		webEngine = webView.getEngine();			
		webEngine.setJavaScriptEnabled(true);
		
		webEngine.setOnError((WebErrorEvent wEvent) -> {
			System.out.println("JS alert() error message: " + wEvent.getMessage() );
		});	
		
		webEngine.setOnAlert((WebEvent<String> wEvent) -> {
			
			System.out.println("JS alert() message: " + wEvent.getData() );
		});	
		
		webEngine.setCreatePopupHandler((PopupFeatures param) -> {
			
			popupStage = new Stage(StageStyle.UTILITY);
            WebView wv2 = new WebView();
            popupStage.setScene(new Scene(wv2));
            popupStage.show();
		
			return wv2.getEngine();
		});
		
		//addBridge();
		
		
    }

    private void showAlert(String content){
    	
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Information Dialog");
    	alert.setHeaderText("회원 정보");
    	alert.setContentText(content);

    	alert.showAndWait();
    	
    }
    
    /*
    private void addBridge(){

    	
		webEngine.getLoadWorker().stateProperty()
        .addListener((obs, oldValue, newValue) -> {
          
        	//System.out.println("old = " + oldValue + " new = " + newValue);
        	
        	if (newValue == Worker.State.SUCCEEDED) {
        	  
	            JSObject jsobj = (JSObject) webEngine.executeScript("window");
	            jsobj.setMember(PROTOCOL_SCHEME, new JSBridge());
	            
        	}
        	
        });
    }
    
    public class JSBridge {
    	// 

    }
*/
    
    public void webengineLoad(){

    	URL.setURLStreamHandlerFactory(new MyURLStreamHandlerFactory());

		if(loginWhere == 0){
			webEngine.load(NAVER_LOGINPAGE);	
		}
		else if(loginWhere == 1){
			webEngine.load(KAKAO_LOGINPAGE);
		}
		else if(loginWhere == 2){
			webEngine.load(FACEBOOK_LOGINPAGE);
		}
		else if(loginWhere == -1){
			URL url = getClass().getResource("/bridgeEX.html");
			webEngine.load(url.toExternalForm());

		}
    }
    
    /**
     * 참조를 다시 유지하기 위해 메인 애플리케이션이 호출한다.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

    }

	public int getLoginWhere() {
		return loginWhere;
	}

	public void setLoginWhere(int loginWhere) {
		this.loginWhere = loginWhere;
	}

	@Override
	public void apiRequestSuccessCallback(String msg) {
		// TODO Auto-generated method stub
		//System.out.println("apiRequestSuccessCallback!");
		
		
		// javaFX Thread
		Platform.runLater(() -> {
            // code that updates UI
			
			if(popupStage != null) popupStage.close();
			
			showAlert(msg);
			
			mainApp.gotoLoginSuccess();
			
        });
				
	}
	

}
