package login.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class RootController extends AnchorPane {

	private MainApp mainApp;
	
	@FXML
	private TextField id;
	@FXML
	private TextField pw;	
	@FXML
	private Button login;
	@FXML
	private Button loginNaver;
	@FXML
	private Button loginKakao;
	@FXML
	private Button loginFacebook;	
	
	public RootController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@FXML
    private void initialize() {
		
		
	}
	
	public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

    }
	
	@FXML
	private void pressedLogin(){
		System.out.println("pressedLogin!");
		mainApp.gotoWebview(-1);
	}
	
	@FXML
	private void pressedLoginNaver(){
		//System.out.println("pressedLoginNaver!");
		
		mainApp.gotoWebview(0);
		
	}
	
	@FXML
	private void pressedLoginKakao(){
		//System.out.println("pressedLoginNaver!");
		
		mainApp.gotoWebview(1);
		
	}
	
	@FXML
	private void pressedLoginFacebook(){
		//System.out.println("pressedLoginNaver!");
		
		mainApp.gotoWebview(2);
	}
	
}
