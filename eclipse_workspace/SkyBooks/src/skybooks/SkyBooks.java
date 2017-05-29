/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package skybooks;

import com.skytree.epub_desktop.Book;
import com.skytree.epub_desktop.BookInformation;
import com.skytree.epub_desktop.Highlight;
import com.skytree.epub_desktop.Highlights;
import com.skytree.epub_desktop.KeyListener;
import com.skytree.epub_desktop.PageInformation;
import com.skytree.epub_desktop.PagingInformation;
import com.skytree.epub_desktop.SkyProvider;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author skytree
 */

class Category {
    public int code = -1;
    public String categoryId = "";
    public String categoryName = "";
    public String userId = "";
    public String type = "";
}

class User {
    public int code = -1;
    public String userId="";
    public String password="";
    public boolean keepConnection = false;
    
    public boolean isEmpty() {
        if (this.userId==null || this.userId.isEmpty()) return true;
        else  return false;
    }
}

class SimpleCrypto {
    
    private final static String HEX = "0123456789ABCDEF";
    
    
    public static String encrypt(String seed, String cleartext) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }
    
    public static String decrypt(String seed, String encrypted) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }
    
    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }
    
    
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }
    
    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
    
    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }
    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }
    
    public static byte[] toByte(String hexString) {
        int len = hexString.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
        return result;
    }
    
    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2*buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    
    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
    }
}


interface DownloadListener {
    public void onStated(String url,String targetPath);
    public void onProgressed(String url,String targetPath,long currentSize,long fileSize);
    public void onFinished(String url,String targetPath,long fileSize);
    public void onError(String msg,Exception e);
}

class DownloadTask implements Runnable  {
    String url = "";
    String targetPath = "";
    DownloadListener dd = null;
    
    DownloadTask(String url,String targetPath) {
        this.url = url;
        this.targetPath = targetPath;
    }
    
    DownloadTask(String url,String targetPath,DownloadListener listener) {
        this.url = url;
        this.targetPath = targetPath;
        this.dd = listener;
    }
    
    public void setDownloadListener(DownloadListener listener) {
        this.dd = listener;
    }
    
    private void download() throws Exception {
        BufferedInputStream inStream = null;
        FileOutputStream outStream = null;
        long currentSize = 0;
        try {
            URL fileUrlObj=new URL(this.url);
            URLConnection connection = fileUrlObj.openConnection();
            connection.connect();
            long fileSize = connection.getContentLength();
            
            inStream = new BufferedInputStream(fileUrlObj.openStream());
            
            outStream = new FileOutputStream(this.targetPath);
            if (dd!=null) {
                dd.onStated(url, targetPath);
            }
            byte data[] = new byte[1024];
            int count;
            
            while ((count = inStream.read(data, 0, 1024)) != -1) {
                currentSize+=count;
                outStream.write(data, 0, count);
                if (dd!=null) {
                    dd.onProgressed(url, targetPath, currentSize, fileSize);
                }
            }
        }catch(Exception e) {
            if (dd!=null) {
                dd.onError(e.getMessage(),e);
            }
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
            throw e;
        }
        
        if (inStream != null) inStream.close();
        if (outStream != null) outStream.close();
        if (dd!=null) {
            dd.onFinished(url, targetPath, currentSize);
        }
    }
    
    @Override
    public void run() {
        try {
            download();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}

public class SkyBooks extends Application {
    final String defaultFontName = "TimesRoman";
    final int defaultFontSizeIndex = 1;
    final int defaultLineSpacingIndex = 2;
    
    final boolean isDebugging = false;
    MainViewController mainViewController = null;
    public User currentUser = new User();
    public String currentCategoryId = "";
    final int maxCoverDownload = 10;
    public ExecutorService  coverExecutor = null;
    public ExecutorService  bookExecutor = null;
    public Parameters parameters;
    public Map<String, String> namedParameters;
    public List<String> rawParameters;
    public List<String> unnamedParameters;
    SyncListener sd = null;
    SkySetting globalSetting = new SkySetting();
    
    ResourceBundle bundle;
    
    public void setSyncListener(SyncListener syncListener) {
        this.sd = syncListener;
    }
    
    public SyncListener getSyncListener() {
        return this.sd;
    }
    
    ArrayList<BookInformation> openedBookList = new ArrayList<BookInformation>();
    
    public void clearImageClipboard() {
        if (isDebugging) return;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                java.awt.datatransfer.Clipboard clipboard1 = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard.hasImage()) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString("");
//                    content.putImage(new Image("resources/logout.png")); // the image you want, as javafx.scene.image.Image
clipboard.setContent(content);
System.out.println("clearClipboard");

String kd = getScreenShotsDirectoryInKorean();
File kdir = new File(kd);
long ct = new Date().getTime();
for(File file: kdir.listFiles()) {
    long diff = ct - file.lastModified();
    if (diff<60*1000) {
        file.delete();
    }
}
                }
            }
        });
        
    }
    
    public void clearTextClipboard() {
        if (isDebugging) return;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                java.awt.datatransfer.Clipboard clipboard1 = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                boolean forceToClearTextClipboard = (openedBookList.size()!=0) ? true:false;
                if (clipboard.hasString() && forceToClearTextClipboard) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString("");
                    clipboard.setContent(content);
                }
            }
        });
    }
    
    class CaptureProtection implements Runnable {
        public CaptureProtection() {}
        public void run() {
            try {
                while(true) {
                    if (isCaptureProtected()) clearImageClipboard();
                    if (isCopyProtected()) clearTextClipboard();
                    Thread.sleep(50);
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public String getOsName()  {
        return  System.getProperty("os.name");
    }
    public boolean isWindows()  {
        String os = getOsName();
        if (os.startsWith("Windows")) return true;
        else return false;
    }
    
    public boolean is64Bit() {
        boolean ret =  new File("c:\\Program Files (x86)\\"+getAppName()+"\\"+getAppName()+".exe").exists();
        return ret;
    }
    
    public boolean isRegistrySet() {
        try {
            String value = WinRegistry.readString (
                    WinRegistry.HKEY_LOCAL_MACHINE,                             //HKEY
                    "SOFTWARE\\Classes\\"+getCUSName(),           //Key
                    "URL_Protocol");                                              //ValueName
            if (value==null) return false;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public void setupRegistry() throws IOException {
        if (!this.isWindows() || isRegistrySet()) return;
        try{
            String key = "Software\\Classes\\"+getCUSName();
            WinRegistry.createKey(WinRegistry.HKEY_LOCAL_MACHINE, key);
            WinRegistry.writeStringValue(WinRegistry.HKEY_LOCAL_MACHINE,key,"URL_Protocol","");
            key = key+File.separator+"shell";
            WinRegistry.createKey(WinRegistry.HKEY_LOCAL_MACHINE, key);
            key = key+File.separator+"open";
            WinRegistry.createKey(WinRegistry.HKEY_LOCAL_MACHINE, key);
            key = key+File.separator+"command";
            WinRegistry.createKey(WinRegistry.HKEY_LOCAL_MACHINE, key);
            if (is64Bit()) {
                WinRegistry.writeStringValue(WinRegistry.HKEY_LOCAL_MACHINE,key,"","\"c:\\Program Files (x86)\\"+getAppName()+"\\"+getAppName()+".exe\" \"%1\"");
            }else {
                WinRegistry.writeStringValue(WinRegistry.HKEY_LOCAL_MACHINE,key,"","\"c:\\Program Files\\"+getAppName()+"\\"+getAppName()+".exe\" \"%1\"");
            }            
        }catch(Exception asd){
            Alert alert = new Alert(AlertType.WARNING, this.bundle.getString("request_admin"), ButtonType.YES,ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                System.exit(0);
            }
        }
    }
    
    
    public boolean isOpened(BookInformation bi) {
        for (int i=0; i<openedBookList.size(); i++) {
            BookInformation b = openedBookList.get(i);
            if (b.bookCode==bi.bookCode) return true;
        }
        return false;
    }
    
    public void addOpenedBook(BookInformation bi) {
        openedBookList.add(bi);
    }
    
    public void deleteOpenedBook(int bookCode) {
        for (int i=0; i<openedBookList.size(); i++) {
            BookInformation b = openedBookList.get(i);
            if (b.bookCode==bookCode) {
                openedBookList.remove(i);
                return;
            }
        }
    }
    
    public void terminateApp() {
        Platform.exit();
        System.exit(0);
    }
    
    class DownloadDelegate implements DownloadListener {
        
        @Override
        public void onStated(String url, String targetPath) {
            System.out.println(url+" will be saved to "+targetPath);
        }
        
        @Override
        public void onProgressed(String url, String targetPath, long currentSize, long fileSize) {
            System.out.println(url+" is being downloaded to "+targetPath+" currentSize is "+currentSize+" of "+fileSize);
        }
        
        @Override
        public void onFinished(String url, String targetPath, long fileSize) {
            System.out.println(url+" was be saved to "+targetPath+" fileSize is "+fileSize);
        }
        
        @Override
        public void onError(String msg, Exception e) {
            System.out.println("Error is detected "+msg);
        }
    }
    
    public void processParameters() {
        this.parameters = getParameters();
        namedParameters = parameters.getNamed();
        rawParameters = parameters.getRaw();
        unnamedParameters = parameters.getUnnamed();
    }
    
    
    @Override
    public void start(Stage stage) throws Exception {
        // final String path = parameters.isEmpty() ? parameters.get(0) : "";
        String country = System.getProperty("user.country");
        String language = System.getProperty("user.language");
        Locale locale = new Locale(language,country);
        this.bundle = ResourceBundle.getBundle("resources/strings", locale);
        
        // this.setupRegistry();
        this.processInit();
        if (getSyncListener()==null) installSamples();
        this.launchMainView(stage);
        this.processParameters();
    }
    
    // for AutoLogin
    public User getAutoUser() {
        try {
            User user = new User();
            String sql = String.format(Locale.US,"select * from User where keepConnection = 1");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                user.code = rs.getInt("Code");
                user.userId = rs.getString("UserID");
                user.password = rs.getString("Password");
                user.keepConnection = true;
                rs.close();
                return user;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void setAutoEnabled(User user) {
        try {
            if (!user.keepConnection) {
                PreparedStatement statement = connection.prepareStatement("Delete from User");
                statement.executeUpdate();
            }else {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO User (UserID,Password,KeepConnection) VALUES(?,?,1)");
                statement.setString(1,user.userId);
                statement.setString(2,user.password);
                statement.executeUpdate();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean checkUser(User user) {
        return this.sd.checkUser(user);
    }
    
    public void processLogout() {
        this.currentUser = new User();
        this.setAutoEnabled(currentUser);
        this.processSync();
    }
    
    public void syncCategories() {
        try {
            deleteAllCategories();
            ArrayList<Category> cs = sd.getCategoriesFromServer();
            for (int i = 0; i < cs.size(); i++){
                Category category = cs.get(i);
                Category tc= this.fetchCategory(category.categoryId);
                if (tc==null) {
                    this.insertCategory(category);
                }else {
                    tc.categoryId =  category.categoryId;
                    tc.categoryName = category.categoryName;
                    this.updateCategory(tc);
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void syncBooks() {
        try {
            this.connection.setAutoCommit(false);
            HashMap<String,BookInformation> biu= this.getBookMap();
            ArrayList<BookInformation> nbs = sd.getBookInformationsFromServer();
            for (int i = 0; i < nbs.size(); i++){
                BookInformation nb = nbs.get(i);
                BookInformation ob = null;
                ob = biu.get(nb.url);
                if (ob==null) { // if not exist, insert new one
                    nb.isDownloaded = false;
                    nb.fileName = this.randomString(32)+".epub";
                    nb.userId = this.currentUser.userId;
                    this.insertBook(nb);
                }else {         // if exists
                    if (!ob.categoryId.equalsIgnoreCase(nb.categoryId) || !ob.expiredDate.equalsIgnoreCase(nb.expiredDate) ) {
                        ob.categoryId = nb.categoryId;
                        ob.categoryName = nb.categoryName;
                        ob.expiredDate = nb.expiredDate;
                        this.updateBookCategory(ob);
                    }
                }
            }
            this.connection.commit();
            this.connection.setAutoCommit(true);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // called in processInit, loginView and reloadButton
    public void processSync() {
        if (sd==null) return;
        sd.setUser(currentUser);
        this.syncCategories();
        this.syncBooks();
        if (mainViewController!=null) mainViewController.reload();
        
        String wf = System.getProperty("user.dir");
        if (!wf.contains("NetBeansProjects")) {
            System.out.println("Working Directory "+wf);
            System.out.println("Copy Protection is Initiated.");
            CaptureProtection cp = new CaptureProtection();
            Thread cpt = new Thread(cp);
            cpt.start();
        }
    }
    
    public void setUser(User user) {
        this.currentUser = user;
    }
    
    public String getKey(String uuidForContent,String uuidForEpub) {
        String key = this.fetchKey(uuidForEpub, uuidForContent);
        if (key!=null) {
            return key;
        }else {
            key = sd.getKey(uuidForContent, uuidForEpub);
            this.insertKey(uuidForEpub, uuidForContent, key);
            return key;
        }
    }
    
    
    class KeyDelegate implements KeyListener {
        @Override
        public String getKeyForEncryptedData(String uuidForContent, String contentName, String uuidForEpub) {
            // TODO Auto-generated method stub
            return getKey(uuidForContent, uuidForEpub);
        }
        
        @Override
        public Book getBook() {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    public void launchMainView(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainView.fxml"),this.bundle);
            Parent root = (Parent)fxmlLoader.load();
            mainViewController = (MainViewController)fxmlLoader.getController();
            mainViewController.setApplication(this);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            mainViewController.setStage(stage);
            mainViewController.setScene(scene);
            stage.show();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    // system
    public String getUserHome() {
        String home = System.getProperty("user.home");
        return home;
    }
    
    public String getScreenShotsDirectoryInKorean() {
        String dir = this.getUserHome()+File.separator+"OneDrive"+File.separator+"사진"+File.separator+"스크린샷";
        return dir;
    }
    
    public String getScreenShotsDirectory() {
        String dir = this.getUserHome()+File.separator+"OneDrive"+File.separator+"Pictures"+File.separator+"ScreenShots";
        return dir;
    }
    
    
    public String getSkyDirectory() {
        String data = this.getUserHome()+File.separator+getAppName();
        return data;
    }
    
    public String getBooksDirectory() {
        String dir = this.getSkyDirectory()+File.separator+"books";
        return dir;
    }
    
    public String getCachesDirectory() {
        String dir = this.getSkyDirectory()+File.separator+"caches";
        return dir;
    }
    
    public String getCoversDirectory() {
        String dir = this.getSkyDirectory()+File.separator+"covers";
        return dir;
    }
    
    public String getFontsDirectory() {
        String dir = this.getSkyDirectory()+File.separator+"fonts";
        return dir;
    }
    
    public void makeSkyDirectories() {
        File skyDir = new File(this.getSkyDirectory());
        if (!skyDir.exists()) {
            skyDir.mkdirs();
        }
        File cachesDir = new File(this.getCachesDirectory());
        if (!cachesDir.exists()) {
            cachesDir.mkdirs();
        }
        File coversDir = new File(this.getCoversDirectory());
        if (!coversDir.exists()) {
            coversDir.mkdirs();
        }
        File fontsDir = new File(this.getFontsDirectory());
        if (!fontsDir.exists()) {
            fontsDir.mkdirs();
        }
        File booksDir = new File(this.getBooksDirectory());
        if (!booksDir.exists()) {
            booksDir.mkdirs();
        }
        
    }
    
    public void setup() {
        try {
            // System.setProperty("prism.lcdtext", "false");
            // System.setProperty("prism.text", "t2k");
            display("setup #0");
            coverExecutor = Executors.newFixedThreadPool(maxCoverDownload);
            bookExecutor = Executors.newFixedThreadPool(maxCoverDownload);
            display("setup #1");
            this.makeSkyDirectories();
            display("setup #2");
            this.openDatabase();
            this.globalSetting = this.fetchSetting();
            display("setup #3");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // database routines
    public Connection connection = null;
    
    public String getDatabasePath() {
        return this.getSkyDirectory()+File.separator+"books.sqlite";
    }
    
    public void openDatabase() {
        try {
            File file = new File(getDatabasePath());
            if (!file.exists()) {
                this.createDatabase();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:"+getDatabasePath());
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getStringFromFile(String path) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
        String result = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
        return result;
    }
    
    public void createDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+getDatabasePath());
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            // execute ddl (data definition language in sql) to create tables.
            statement.executeUpdate(getStringFromFile("resources/book.ddl"));
            statement.executeUpdate(getStringFromFile("resources/highlight.ddl"));
            statement.executeUpdate(getStringFromFile("resources/bookmark.ddl"));
            statement.executeUpdate(getStringFromFile("resources/paging.ddl"));
            statement.executeUpdate(getStringFromFile("resources/User.ddl"));
            statement.executeUpdate(getStringFromFile("resources/Category.ddl"));
            statement.executeUpdate(getStringFromFile("resources/setting.ddl"));
            statement.executeUpdate(getStringFromFile("resources/key.ddl"));
            
            String sql = "";
//            sql = "CREATE INDEX IF NOT EXISTS Book_purchaseDate_Index 	ON Book(purchaseDate DESC);";
//            statement.executeUpdate(sql);
sql = "CREATE INDEX IF NOT EXISTS Book_Author_Index 		ON Book(Author);";
statement.executeUpdate(sql);
sql = "CREATE INDEX IF NOT EXISTS Book_Title_Index 		ON Book(Title);";
statement.executeUpdate(sql);
sql = "CREATE INDEX IF NOT EXISTS Book_shelfcode_Index 	ON Book(CategoryId);";
statement.executeUpdate(sql);
sql = "CREATE INDEX IF NOT EXISTS Book_LastRead_Index 		ON Book(LastRead);";
statement.executeUpdate(sql);
sql = "CREATE INDEX IF NOT EXISTS Book_URL_Index 		ON Book(URL);";
statement.executeUpdate(sql);

statement.executeUpdate(String.format("INSERT INTO Setting(BookCode,FontName,FontSize,LineSpacing,Foreground,Background,Theme,Brightness,TransitionType,LockRotation) VALUES(0,'%s',%d,%d,-1,-1,0,1,2,1)",defaultFontName,defaultFontSizeIndex,defaultLineSpacingIndex));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void closeDatabase() {
        try {
            if (connection!=null) {
                connection.close();
            }
        }catch(Exception e) {
            e.printStackTrace();
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
    
    
    public String getDateString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
    
    public void copyFile(String src,String dst) throws Exception {
        try {
            Path srcPath = Paths.get(src);
            Path dstPath = Paths.get(dst);
            Files.copy(srcPath,dstPath,StandardCopyOption.REPLACE_EXISTING);
        }catch(Exception e) {
            throw e;
        }
    }
    
    public String copyFileToBooks(String src) throws Exception {
        try {
            Path p = Paths.get(src);
//            String dstFileName = p.getFileName().toString();
String dstFileName = "";
dstFileName = randomString(32)+".epub";
this.copyFile(src,this.getBooksDirectory()+File.separator+dstFileName);
return dstFileName;
        }catch(Exception e) {
            throw e;
        }
    }
    
    public void installLocalEpub(String filePath) {
        try {
            String targetName = this.copyFileToBooks(filePath);
            String targetPath = this.getBooksDirectory()+File.separator+targetName;
            String coverPathToSave = this.getCoversDirectory()+File.separator+targetName.replace(".epub",".jpg");
            SkyProvider skyProvider = new SkyProvider();
            skyProvider.setKeyListener(new KeyDelegate());
            BookInformation bi = new BookInformation(targetPath,skyProvider,coverPathToSave);
            bi.isDownloaded = true;
            bi.userId = this.currentUser.userId;
            if (bi.isValid()) {
                if (this.bookExists(bi)) {
                    Alert alert = new Alert(AlertType.WARNING, bi.title + "already exists.Overwrite it ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        BookInformation ob = this.fetchBookInformation(bi.title,bi.creator);
                        this.deleteBookByBookCode(ob.bookCode);
                        this.insertBook(bi);
                    }else {
                        File file = new File(targetPath);
                        file.delete();
                    }
                }else {
                    this.insertBook(bi);
                }
            }else {
                File file = new File(targetPath);
                file.delete();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateSetting(SkySetting setting) {
        if (setting==null) return;
        try {
            this.globalSetting = setting;
            PreparedStatement statement = connection.prepareStatement("Update Setting Set FontName=?,FontSize=?,LineSpacing=?"
                    + ",Foreground=?,Background=?,Theme=?,Brightness=?,TransitionType=?,LockRotation=?,DoublePaged=?,Allow3G=?,GlobalPagination=?,AutoStartMediaOverlay=?,CreateThumbnails=?,UserID=?,CategoryID=?,SortType=?,IsThumbnailView=?,TableSortName=?,TableSortDirection=?  where BookCode=0");
            statement.setString(1,setting.fontName);
            statement.setInt(2,setting.fontSize);
            statement.setInt(3,setting.lineSpacing);
            statement.setInt(4, setting.foreground);
            statement.setInt(5,setting.background);
            statement.setInt(6, setting.theme);
            statement.setDouble(7, setting.brightness);
            statement.setInt(8,setting.transitionType);
            statement.setInt(9,setting.lockRotation ? 1:0);
            statement.setInt(10,setting.doublePaged ? 1:0);
            statement.setInt(11,setting.allow3G ? 1:0);
            statement.setInt(12,setting.globalPagination ? 1:0);
            statement.setInt(13,setting.autoStartMediaOverlay ? 1:0);
            statement.setInt(14,setting.createThumbnails ? 1:0);
            statement.setString(15,setting.userId);
            statement.setString(16,setting.categoryId);
            statement.setInt(17,setting.sortType);
            statement.setInt(18,setting.isThumbnailView ? 1:0);
            statement.setString(19,setting.tableSortName);
            statement.setInt(20,setting.tableSortDirection);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public SkySetting fetchSetting() {
        try {
            String sql = String.format(Locale.US,"SELECT * FROM Setting where BookCode=0");
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            while(result.next()) {
                SkySetting setting = new SkySetting();
                setting.bookCode =      result.getInt(1);
                setting.fontName =      result.getString(2);
                setting.fontSize =      result.getInt(3);
                setting.lineSpacing=    result.getInt(4);
                setting.foreground=     result.getInt(5);
                setting.background=     result.getInt(6);
                setting.theme  =        result.getInt(7);
                setting.brightness =    result.getDouble(8);
                setting.transitionType= result.getInt(9);
                setting.lockRotation =  result.getInt(10)!=0;
                setting.allow3G 	=  result.getInt(11)!=0;
                setting.globalPagination =  result.getInt(12)!=0;
                setting.doublePaged =  result.getInt(13)!=0;
                setting.autoStartMediaOverlay = result.getInt(14)!=0;
                setting.createThumbnails = result.getInt(15)!=0;
                setting.userId = result.getString(16);
                setting.categoryId = result.getString(17);
                setting.sortType  =        result.getInt(18);
                setting.isThumbnailView = result.getInt(19)!=0;
                setting.tableSortName = result.getString(20);
                setting.tableSortDirection = result.getInt(21);
                result.close();
                return setting;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean isLocalFile(BookInformation bi) {
        if (bi.url==null || bi.url.isEmpty()) return true;
        return false;
    }
    
    public Category fetchCategory(String categoryId) {
        try {
            Category ct = new Category();
            String sql = String.format(Locale.US,"select * from Category where CategoryID = '%s' and UserID = '%s'",categoryId,currentUser.userId);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                ct.code = rs.getInt("Code");
                ct.categoryId = rs.getString("CategoryID");
                ct.categoryName = rs.getString("Name");
                ct.userId = rs.getString("UserID");
                ct.type = rs.getString("Type");
                rs.close();
                return ct;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Category fetchCategoryByName(String categoryName) {
        try {
            Category ct = new Category();
            String sql = String.format(Locale.US,"select * from Category where Name = '%s' and UserID = '%s'",categoryName,currentUser.userId);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                ct.code = rs.getInt("Code");
                ct.categoryId = rs.getString("CategoryID");
                ct.categoryName = rs.getString("Name");
                ct.userId = rs.getString("UserID");
                ct.type = rs.getString("Type");
                rs.close();
                return ct;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public ArrayList<Category> fetchAllCategories() {
        try {
            ArrayList<Category>cts = new ArrayList<Category>();
            String sql = String.format(Locale.US,"select * from Category where UserID = '%s' Order By Type DESC",currentUser.userId );
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                Category ct = new Category();
                ct.code = rs.getInt("Code");
                ct.categoryId = rs.getString("CategoryID");
                ct.categoryName = rs.getString("Name");
                ct.userId = rs.getString("UserID");
                ct.type = rs.getString("Type");
                cts.add(ct);
            }
            rs.close();
            return cts;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    public void insertCategory(String categoryId,String categoryName,String type) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Category (CategoryID,Name,UserID,Type) VALUES(?,?,?,?)");
            statement.setString(1,categoryId);
            statement.setString(2,categoryName);
            statement.setString(3,currentUser.userId);
            statement.setString(4,type);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void insertCategory(Category category) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Category (CategoryID,Name,UserID,Type) VALUES(?,?,?,?)");
            statement.setString(1,category.categoryId);
            statement.setString(2,category.categoryName);
            statement.setString(3,currentUser.userId);
            statement.setString(4,category.type);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateCategory(Category category) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE Category Set CategoryId=?,type=? where Name=? And UserId=?");
            statement.setString(1,category.categoryId);
            statement.setString(2,category.type);
            statement.setString(3,category.categoryName);
            statement.setString(4,currentUser.userId);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getCategoryIdByName(String categoryName) {
        String categoryId = "";
        try {
            String sql = String.format(Locale.US,"select * from Category where Name = '%s' and userId='%s'",categoryName,currentUser.userId);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                categoryId = rs.getString("categoryId");
                rs.close();
                break;
                
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return categoryId;
    }
    
    public String getCategoryNameById(String categoryId) {
        String categoryName = "";
        try {
            String sql = String.format(Locale.US,"select * from Category where CategoryId='%s' and userId='%s'",categoryId,currentUser.userId);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                categoryName = rs.getString("Name");
                rs.close();
                break;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return categoryName;
    }
    
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();
    
    public String randomString( int len ) 	{
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
    
    public BookInformation processCategory(BookInformation bi) {
        String categoryId = "";
        String categoryName = "";
        
        if (bi.categoryName!=null && !bi.categoryName.isEmpty()) {
            if (bi.categoryId.isEmpty()) {
                categoryId = this.getCategoryIdByName(bi.categoryName);
                bi.categoryId = categoryId;
            }
        }else {
            if (bi.subject!=null && !bi.subject.isEmpty()) {
                categoryId = this.getCategoryIdByName(bi.subject);
                if (categoryId==null || categoryId.isEmpty()) {
                    categoryName = bi.subject;
                    categoryId = randomString(32);
                    this.insertCategory(categoryId, categoryName,"LOCAL");
                    bi.categoryName = categoryName;
                    bi.categoryId = categoryId;
                }
            }else { // subject도 없는 경우
                categoryId = this.getCategoryIdByName("No Category");
                categoryName = "No Category";
                if (categoryId==null || categoryId.isEmpty()) {
                    categoryId = randomString(32);
                    this.insertCategory(categoryId, categoryName,"LOCAL");
                }
                bi.categoryName = categoryName;
                bi.categoryId = categoryId;
            }
        }
        return bi;
    }
    
    public void insertBook(BookInformation bookInformation) {
        try {
            BookInformation bi = this.processCategory(bookInformation);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Book (Title,Author,Publisher,Subject,Type,Date,Language,Filename,IsFixedLayout,IsRTL,Position,Spread,UserID,IsFree,IsSample,ExpiredDate,CategoryID,URL,CoverURL,PurchaseDate,IsDownloaded) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setString(1,bi.title);
            statement.setString(2,bi.creator);
            statement.setString(3,bi.publisher);
            statement.setString(4,bi.subject);
            statement.setString(5,bi.type);
            statement.setString(6,bi.date);
            statement.setString(7,bi.language);
            statement.setString(8,bi.fileName);
            statement.setInt(9, bi.isFixedLayout ? 1:0);
            statement.setInt(10, bi.isRTL ? 1:0);
            double position = 0.0f;
            statement.setDouble(11, position);
            statement.setInt(12, bi.spread);
            
            // Spread,UserID,IsFree,IsSample,ExpiredDate,CategoryID
            statement.setString(13,currentUser.userId);
            statement.setInt(14,bi.isFree ? 1:0);
            statement.setInt(15,bi.isSample ? 1:0);
            statement.setString(16,bi.expiredDate);
            statement.setString(17,bi.categoryId);
            statement.setString(18,bi.url);
            statement.setString(19,bi.coverUrl);
            
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = new Date();
            String pd = dateFormat.format(date);
            
            if (bi.purchaseDate==null || bi.purchaseDate.isEmpty()) {
                statement.setString(20,pd);
            }else {
                statement.setString(20,bi.purchaseDate);
            }
            
            statement.setInt(21,bi.isDownloaded ? 1:0);
            
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public BookInformation fetchBookInformation(int bookCode) {
        try {
            BookInformation bi = new BookInformation();
            String sql = String.format(Locale.US,"select * from Book where BookCode = %d",bookCode);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                bi.bookCode = rs.getInt("BookCode");
                bi.title = rs.getString("Title");
                bi.creator =    rs.getString("Author");
                bi.publisher =  rs.getString("Publisher");
                bi.subject =    rs.getString("Subject");
                bi.type =       rs.getString("Type");
                bi.date =       rs.getString("Date");
                bi.language =   rs.getString("Language");
                bi.fileName =   rs.getString("FileName");
                bi.position =   rs.getDouble("Position");
                bi.isFixedLayout = (rs.getInt("IsFixedLayout") == 1 ? true:false);
                bi.isGlobalPagination = (rs.getInt("IsGlobalPagination") == 1 ? true:false);
                bi.isDownloaded = (rs.getInt("IsDownloaded") == 1 ? true:false);
                bi.fileSize = rs.getInt("FileSize");
                bi.customOrder = rs.getInt("CustomOrder");
                bi.url = rs.getString("URL");
                bi.coverUrl = rs.getString("CoverURL");
                bi.downSize = rs.getInt("DownSize");
                bi.isRead = (rs.getInt("IsRead") == 1 ? true:false);
                bi.lastRead = rs.getString("LastRead");
                bi.isRTL = (rs.getInt("IsRTL") == 1 ? true:false);
                bi.isVerticalWriting = (rs.getInt("IsVerticalWriting") == 1 ? true:false);
                bi.spread = rs.getInt("Spread");
                
                bi.userId = rs.getString("UserID");
                bi.isFree = (rs.getInt("IsFree") == 1 ? true:false);
                bi.isSample = (rs.getInt("IsSample") == 1 ? true:false);
                bi.expiredDate = rs.getString("ExpiredDate");
                bi.categoryId = rs.getString("CategoryID");
                bi.purchaseDate = rs.getString("PurchaseDate");
                
                bi.isRated = (rs.getInt("IsRated") == 1 ? true:false);
                
                rs.close();
                return bi;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public BookInformation fetchBookInformationByURL(String url) {
        try {
            BookInformation bi = new BookInformation();
            String sql = String.format(Locale.US,"select * from Book where URL like '%%%s%%'",url);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                bi.bookCode = rs.getInt("BookCode");
                bi.title = rs.getString("Title");
                bi.creator =    rs.getString("Author");
                bi.publisher =  rs.getString("Publisher");
                bi.subject =    rs.getString("Subject");
                bi.type =       rs.getString("Type");
                bi.date =       rs.getString("Date");
                bi.language =   rs.getString("Language");
                bi.fileName =   rs.getString("FileName");
                bi.position =   rs.getDouble("Position");
                bi.isFixedLayout = (rs.getInt("IsFixedLayout") == 1 ? true:false);
                bi.isGlobalPagination = (rs.getInt("IsGlobalPagination") == 1 ? true:false);
                bi.isDownloaded = (rs.getInt("IsDownloaded") == 1 ? true:false);
                bi.fileSize = rs.getInt("FileSize");
                bi.customOrder = rs.getInt("CustomOrder");
                bi.url = rs.getString("URL");
                bi.coverUrl = rs.getString("CoverURL");
                bi.downSize = rs.getInt("DownSize");
                bi.isRead = (rs.getInt("IsRead") == 1 ? true:false);
                bi.lastRead = rs.getString("LastRead");
                bi.isRTL = (rs.getInt("IsRTL") == 1 ? true:false);
                bi.isVerticalWriting = (rs.getInt("IsVerticalWriting") == 1 ? true:false);
                bi.spread = rs.getInt("Spread");
                
                bi.userId = rs.getString("UserID");
                bi.isFree = (rs.getInt("IsFree") == 1 ? true:false);
                bi.isSample = (rs.getInt("IsSample") == 1 ? true:false);
                bi.expiredDate = rs.getString("ExpiredDate");
                bi.categoryId = rs.getString("CategoryID");
                bi.purchaseDate = rs.getString("PurchaseDate");
                
                bi.isRated = (rs.getInt("IsRated") == 1 ? true:false);
                
                rs.close();
                return bi;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public BookInformation fetchBookInformation(String title,String author) {
        try {
            BookInformation bi = new BookInformation();
            String sql = String.format(Locale.US,"select * from Book where Title = \"%s\" and Author=\"%s\"",title,author);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                bi.bookCode = rs.getInt("BookCode");
                bi.title = rs.getString("Title");
                bi.creator =    rs.getString("Author");
                bi.publisher =  rs.getString("Publisher");
                bi.subject =    rs.getString("Subject");
                bi.type =       rs.getString("Type");
                bi.date =       rs.getString("Date");
                bi.language =   rs.getString("Language");
                bi.fileName =   rs.getString("FileName");
                bi.position =   rs.getDouble("Position");
                bi.isFixedLayout = (rs.getInt("IsFixedLayout") == 1 ? true:false);
                bi.isGlobalPagination = (rs.getInt("IsGlobalPagination") == 1 ? true:false);
                bi.isDownloaded = (rs.getInt("IsDownloaded") == 1 ? true:false);
                bi.fileSize = rs.getInt("FileSize");
                bi.customOrder = rs.getInt("CustomOrder");
                bi.url = rs.getString("URL");
                bi.coverUrl = rs.getString("CoverURL");
                bi.downSize = rs.getInt("DownSize");
                bi.isRead = (rs.getInt("IsRead") == 1 ? true:false);
                bi.lastRead = rs.getString("LastRead");
                bi.isRTL = (rs.getInt("IsRTL") == 1 ? true:false);
                bi.isVerticalWriting = (rs.getInt("IsVerticalWriting") == 1 ? true:false);
                bi.spread = rs.getInt("Spread");
                
                bi.userId = rs.getString("UserID");
                bi.isFree = (rs.getInt("IsFree") == 1 ? true:false);
                bi.isSample = (rs.getInt("IsSample") == 1 ? true:false);
                bi.expiredDate = rs.getString("ExpiredDate");
                bi.categoryId = rs.getString("CategoryID");
                bi.purchaseDate = rs.getString("PurchaseDate");
                
                bi.isRated = (rs.getInt("IsRated") == 1 ? true:false);
                
                rs.close();
                return bi;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public BookInformation fetchBookInformation(String url) {
        try {
            BookInformation bi = new BookInformation();
            String sql = String.format(Locale.US,"select * from Book where url = \"%s\"",url);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                bi.bookCode = rs.getInt("BookCode");
                bi.title = rs.getString("Title");
                bi.creator =    rs.getString("Author");
                bi.publisher =  rs.getString("Publisher");
                bi.subject =    rs.getString("Subject");
                bi.type =       rs.getString("Type");
                bi.date =       rs.getString("Date");
                bi.language =   rs.getString("Language");
                bi.fileName =   rs.getString("FileName");
                bi.position =   rs.getDouble("Position");
                bi.isFixedLayout = (rs.getInt("IsFixedLayout") == 1 ? true:false);
                bi.isGlobalPagination = (rs.getInt("IsGlobalPagination") == 1 ? true:false);
                bi.isDownloaded = (rs.getInt("IsDownloaded") == 1 ? true:false);
                bi.fileSize = rs.getInt("FileSize");
                bi.customOrder = rs.getInt("CustomOrder");
                bi.url = rs.getString("URL");
                bi.coverUrl = rs.getString("CoverURL");
                bi.downSize = rs.getInt("DownSize");
                bi.isRead = (rs.getInt("IsRead") == 1 ? true:false);
                bi.lastRead = rs.getString("LastRead");
                bi.isRTL = (rs.getInt("IsRTL") == 1 ? true:false);
                bi.isVerticalWriting = (rs.getInt("IsVerticalWriting") == 1 ? true:false);
                bi.spread = rs.getInt("Spread");
                
                bi.userId = rs.getString("UserID");
                bi.isFree = (rs.getInt("IsFree") == 1 ? true:false);
                bi.isSample = (rs.getInt("IsSample") == 1 ? true:false);
                bi.expiredDate = rs.getString("ExpiredDate");
                bi.categoryId = rs.getString("CategoryID");
                bi.purchaseDate = rs.getString("PurchaseDate");
                
                bi.isRated = (rs.getInt("IsRated") == 1 ? true:false);
                
                rs.close();
                return bi;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public ArrayList<BookInformation> fetchBookInformations(int sortType,String key,String categoryId) {
        try {
            ArrayList<BookInformation>bis = new ArrayList<BookInformation>();
            
            String orderBy="";
            if (sortType==0)		orderBy = " ORDER BY PurchaseDate DESC";
            else if (sortType==1) 	orderBy = " ORDER BY Title, PurchaseDate DESC";
            else if (sortType==2)	orderBy = " ORDER BY Author, PurchaseDate DESC";
            else if (sortType==3)       orderBy = " ORDER BY LastRead DESC,PurchaseDate DESC";
            else if (sortType==4)       orderBy = " ORDER BY PurchaseDate DESC";
            String condition = "";
            
            if (!(key==null || key.isEmpty())) {
                if(categoryId!=null&&!categoryId.isEmpty()){
                    if (categoryId.equalsIgnoreCase("download")) {
                        condition =String.format(Locale.US," WHERE Title like '%%%s%%' OR Author like '%%%s%%' AND IsDownloaded=1 ",key,key);
                    }else {
                        condition =String.format(Locale.US," WHERE Title like '%%%s%%' OR Author like '%%%s%%' AND CategoryId='%s' ",key,key,categoryId);
                    }
                    
                }else{
                    condition =String.format(Locale.US," WHERE Title like '%%%s%%' OR Author like '%%%s%%'",key,key);
                }
            }else{
                if(categoryId!=null&&!categoryId.isEmpty()){
                    if (categoryId.equalsIgnoreCase("download")) {
                        condition =String.format(Locale.US," WHERE IsDownloaded=1 ");
                    }else {
                        condition =String.format(Locale.US," WHERE CategoryId='%s' ",categoryId);
                    }
                }
            }
            String sql = String.format(Locale.US,"SELECT* from Book")+condition+orderBy;
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                BookInformation bi = new BookInformation();
                bi.bookCode = rs.getInt("BookCode");
                bi.title = rs.getString("Title");
                bi.creator =    rs.getString("Author");
                bi.publisher =  rs.getString("Publisher");
                bi.subject =    rs.getString("Subject");
                bi.type =       rs.getString("Type");
                bi.date =       rs.getString("Date");
                bi.language =   rs.getString("Language");
                bi.fileName =   rs.getString("FileName");
                bi.position =   rs.getDouble("Position");
                bi.isFixedLayout = (rs.getInt("IsFixedLayout") == 1 ? true:false);
                bi.isGlobalPagination = (rs.getInt("IsGlobalPagination") == 1 ? true:false);
                bi.isDownloaded = (rs.getInt("IsDownloaded") == 1 ? true:false);
                bi.fileSize = rs.getInt("FileSize");
                bi.customOrder = rs.getInt("CustomOrder");
                bi.url = rs.getString("URL");
                bi.coverUrl = rs.getString("CoverURL");
                bi.downSize = rs.getInt("DownSize");
                bi.isRead = (rs.getInt("IsRead") == 1 ? true:false);
                bi.lastRead = rs.getString("LastRead");
                bi.isRTL = (rs.getInt("IsRTL") == 1 ? true:false);
                bi.isVerticalWriting = (rs.getInt("IsVerticalWriting") == 1 ? true:false);
                bi.spread = rs.getInt("Spread");
                bi.userId = rs.getString("UserID");
                bi.isFree = (rs.getInt("IsFree") == 1 ? true:false);
                bi.isSample = (rs.getInt("IsSample") == 1 ? true:false);
                bi.expiredDate = rs.getString("ExpiredDate");
                bi.categoryId = rs.getString("CategoryID");
                bi.purchaseDate = rs.getString("PurchaseDate");
                
                bi.isRated = (rs.getInt("IsRated") == 1 ? true:false);
                
                if (bi.userId.equalsIgnoreCase("") || bi.userId.equalsIgnoreCase(currentUser.userId) ) {
                    java.util.Date expiredDate = SkyBooks.getDateByString(bi.expiredDate);
                    java.util.Date nowDate = new java.util.Date();
                    if(expiredDate!=null && !expiredDate.after(nowDate)){
                        if(bi.isDownloaded==true){
                            deleteBookByBookCode(bi.bookCode);
                        }
                    }else {
                        bis.add(bi);
                    }
                }
            }
            rs.close();
            return bis;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public HashMap<String,BookInformation> getBookMap() {
        HashMap<String,BookInformation> biu = new HashMap<String,BookInformation>();
        ArrayList<BookInformation>bis = this.fetchBookInformations(0,"","");
        for (int i=0; i<bis.size();i++) {
            BookInformation bi = bis.get(i);
            biu.put(bi.url, bi);
        }
        return biu;
    }
    
    
    public boolean bookExists(BookInformation bi) {
        BookInformation bt = null;
        bt = this.fetchBookInformation(bi.title,bi.creator);
        if (bt!=null) {
            String filePath = this.getBooksDirectory()+File.separator+bt.fileName;
            File file = new File(filePath);
            if (file.exists()) return true;
        }
        return false;
    }
    
    // 파일도 같이 삭제해야 한다
    public void deleteBookByBookCode(int bookCode) {
        try {
            BookInformation bi = this.fetchBookInformation(bookCode);
            if (this.isLocalFile(bi)) {
                // delete file
                String filePath = this.getBooksDirectory()+File.separator+bi.fileName;
                File file = new File(filePath);
                file.delete();
                // delete cover
                String coverPath = this.getCoversDirectory()+File.separator+bi.fileName.replace(".epub",".jpg");
                file = new File(coverPath);
                file.delete();
                // delete caches
                deleteCachesByBookCode(bookCode);
                // delete category
                ArrayList<BookInformation> bcs = this.fetchBookInformations(0,"",bi.categoryId);
                if (bcs.size()==1) {
                    String ds = String.format(Locale.US,"DELETE FROM Category where CategoryID = '%s'",bi.categoryId);
                    PreparedStatement dss = connection.prepareStatement(ds);
                    dss.executeUpdate();
                }
                // delete it from db
                String sql = String.format(Locale.US,"DELETE FROM Book where BookCode = %d",bookCode);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.executeUpdate();
            }else {
                // delete downloaded file
                String filePath = this.getBooksDirectory()+File.separator+bi.fileName;
                File file = new File(filePath);
                file.delete();
                // delete caches.
                deleteCachesByBookCode(bookCode);
                // update db to undownloaded
                String sql = String.format(Locale.US,"UPDATE BOOK Set IsDownloaded=0  where BookCode = %d",bookCode);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.executeUpdate();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateRated(BookInformation bi) {
        try {
            String sql = String.format(Locale.US,"UPDATE BOOK Set IsRated=1  where BookCode = %d",bi.bookCode);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    public void deleteCachesByBookCode(int bookCode) {
        String prefix = String.format("sb%d",bookCode);
        String cacheFolder = this.getCachesDirectory();
        File[] directory = new File(cacheFolder).listFiles();
        if (directory!=null) {
            for(File file: directory) {
                if(file.getName().startsWith(prefix)) {
                    file.delete();
                }
            }
        }
    }
    
    
    
    public ArrayList<PageInformation> fetchBookmarks(int bookCode) {
        try{
            ArrayList<PageInformation>pis = new ArrayList<PageInformation>();
            String sql = String.format(Locale.US,"SELECT * from Bookmark where bookCode=%d ORDER BY ChapterIndex",bookCode);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                PageInformation pi = new PageInformation();
                pi.bookCode = rs.getInt(1);
                pi.code = rs.getInt(2);
                pi.chapterIndex = rs.getInt(3);
                pi.pagePositionInChapter = rs.getDouble(4);
                pi.pagePositionInBook = rs.getDouble(5);
                pi.datetime = rs.getString(7);
                pis.add(pi);
            }
            rs.close();
            return pis;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    public void insertBookmark(PageInformation pi) {
        try {
            PreparedStatement statement = connection.prepareStatement("insert into bookmark(BookCode,ChapterIndex,PagePositionInChapter,PagePositionInBook,CreatedDate) values(?,?,?,?,?); ");
            statement.setInt(1,pi.bookCode);
            statement.setInt(2,pi.chapterIndex);
            statement.setDouble(3,pi.pagePositionInChapter);
            statement.setDouble(4,pi.pagePositionInBook);
            statement.setString(5,this.getDateString());
            statement.executeUpdate();
        }catch(Exception e ) {
            e.printStackTrace();
        }
    }
    
    public int getBookmarkCode(PageInformation pi) {
        try {
            int bookCode = pi.bookCode;
            BookInformation bi = this.fetchBookInformation(bookCode);
            if (bi==null) return -1;
            boolean isFixedLayout = bi.isFixedLayout;
            
            if (!isFixedLayout) {
                double pageDelta = 1.0f/pi.numberOfPagesInChapter;
                double target = pi.pagePositionInChapter;
                String sql = String.format(Locale.US,"SELECT Code,PagePositionInChapter from Bookmark where BookCode=%d and ChapterIndex=%d",bookCode,pi.chapterIndex);
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                while(rs.next()) {
                    double ppc = rs.getDouble("PagePositionInChapter");
                    int code = rs.getInt("Code");
                    if (target>=(ppc-pageDelta/2) && target<=(ppc+pageDelta/2.0f)) {
                        rs.close();
                        return code;
                    }
                }
                rs.close();
            }else {
                String sql = String.format(Locale.US,"SELECT Code from Bookmark where BookCode=%d and ChapterIndex=%d",bookCode,pi.chapterIndex);
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    int code = rs.getInt("Code");
                    return code;
                }
                rs.close();
            }
            
        }catch(Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public boolean isBookmarked(PageInformation pi) {
        int code = this.getBookmarkCode(pi);
        if (code==-1) {
            return false;
        }else {
            return true;
        }
    }
    
    public void deleteBookmark(PageInformation pi) {
        int code = pi.code;
        this.deleteBookmarkByCode(code);
    }
    
    public void deleteBookmarkByCode(int code) {
        try {
            String sql = String.format(Locale.US,"DELETE FROM Bookmark where Code = %d",code);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteAllCategories() {
        try {
            String sql = String.format(Locale.US,"DELETE FROM Category where type=\"SERVER\"");
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean toggleBookmark(PageInformation pi) {
        int code = this.getBookmarkCode(pi);
        if (code == -1) { // if not exist
            this.insertBookmark(pi);
            return true;
        }else {
            this.deleteBookmarkByCode(code); // if exist, delete it
            return false;
        }
    }
    
    public void updatePosition(int bookCode,double position) {
        try {
            PreparedStatement statement = connection.prepareStatement("Update Book Set Position=?,LastRead=?,IsRead=? where BookCode=?");
            statement.setDouble(1,position);
            statement.setString(2,getDateString());
            statement.setInt(3,1);
            statement.setInt(4,bookCode);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateBookCategory(BookInformation bi) {
        try {
            PreparedStatement statement = connection.prepareStatement("Update Book Set CategoryId=? where BookCode=?");
            statement.setString(1,bi.categoryId);
            statement.setInt(2,bi.bookCode);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updatedBookDownloaded(BookInformation bi,boolean isDownloaded) {
        try {
            int value = 1;
            if (isDownloaded) value = 1;
            else value = 0;
            PreparedStatement statement = connection.prepareStatement("Update Book Set IsDownloaded=? where BookCode=?");
            statement.setInt(1,value);
            statement.setInt(2,bi.bookCode);
            statement.executeUpdate();
            bi.isDownloaded = true;
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    Highlights fetchHighlights(int bookCode, int chapterIndex) {
        Highlights results = new Highlights();
        try {
            String sql = String.format(Locale.US,"SELECT * FROM Highlight where BookCode=%d and ChapterIndex=%d ORDER BY ChapterIndex",bookCode,chapterIndex);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                Highlight highlight = new Highlight();
                highlight.bookCode = bookCode;
                highlight.code = rs.getInt("Code");
                highlight.chapterIndex = chapterIndex;
                highlight.startIndex    = rs.getInt("StartIndex");
                highlight.startOffset   = rs.getInt("startOffset");
                highlight.endIndex      = rs.getInt("endIndex");
                highlight.endOffset     = rs.getInt("endOffset");
                highlight.color         = rs.getInt("Color");
                highlight.pagePositionInBook = rs.getDouble("pagePositionInBook");
                highlight.pagePositionInChapter = rs.getDouble("pagePositionInChapter");
                highlight.text          = rs.getString("Text");
                highlight.note          = rs.getString("Note");
                highlight.isNote        = rs.getInt("IsNote")!=0;
                highlight.datetime      = rs.getString("CreatedDate");
                highlight.style         = rs.getInt("Style");
                results.addHighlight(highlight);
            }
            rs.close();
            statement.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return results;
    }
    
    Highlights fetchAllHighlights(int bookCode) {
        Highlights results = new Highlights();
        try {
            String sql = String.format(Locale.US,"SELECT * FROM Highlight where BookCode=%d ORDER BY ChapterIndex",bookCode);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                Highlight highlight = new Highlight();
                highlight.bookCode = bookCode;
                highlight.code = rs.getInt("Code");
                highlight.chapterIndex = rs.getInt("ChapterIndex");
                highlight.startIndex    = rs.getInt("StartIndex");
                highlight.startOffset   = rs.getInt("startOffset");
                highlight.endIndex      = rs.getInt("endIndex");
                highlight.endOffset     = rs.getInt("endOffset");
                highlight.color         = rs.getInt("Color");
                highlight.pagePositionInBook = rs.getDouble("pagePositionInBook");
                highlight.pagePositionInChapter = rs.getDouble("pagePositionInChapter");
                highlight.text          = rs.getString("Text");
                highlight.note          = rs.getString("Note");
                highlight.isNote        = rs.getInt("IsNote")!=0;
                highlight.datetime      = rs.getString("CreatedDate");
                highlight.style         = rs.getInt("Style");
                results.addHighlight(highlight);
            }
            rs.close();
            statement.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return results;
    }
    
    // Using db method
    public void insertHighlight(Highlight highlight) {
        try {
            PreparedStatement statement = connection.prepareStatement("Insert into Highlight(BookCode,ChapterIndex,StartIndex,StartOffset,EndIndex,EndOffset,pagePositionInChapter,pagePositionInBook,Color,Text,Note,IsNote,CreatedDate,Style) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
            String dateString = this.getDateString();
            
            statement.setInt(1,highlight.bookCode);
            statement.setInt(2,highlight.chapterIndex);
            statement.setInt(3,highlight.startIndex);
            statement.setInt(4,highlight.startOffset);
            statement.setInt(5,highlight.endIndex);
            statement.setInt(6,highlight.endOffset);
            statement.setDouble(7,highlight.pagePositionInChapter);
            statement.setDouble(8,highlight.pagePositionInBook);
            statement.setInt(9,highlight.color);
            statement.setString(10,highlight.text);
            statement.setString(11,highlight.note);
            statement.setInt(12,highlight.isNote?1:0);
            statement.setString(13,dateString);
            statement.setInt(14,highlight.style);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateHighlight(Highlight highlight) {
        try {
            PreparedStatement statement = connection.prepareStatement("Update Highlight Set StartIndex=?,StartOffset=?,EndIndex=?,EndOffset=?,Color=?,Text=?,Note=?,IsNote=?,Style=? where BookCode=? and ChapterIndex=? and StartIndex=? and StartOffset=? and EndIndex=? and EndOffset=?");
            // update arguments
            String str = "Update Highlight Set "
                    + "StartIndex=?,"   // 1
                    + "StartOffset=?,"  // 2
                    + "EndIndex=?,"     // 3
                    + "EndOffset=?,"    // 4
                    + "Color=?,"        // 5
                    + "Text=?,"         // 6
                    + "Note=?,"         // 7
                    + "IsNote=?,"       // 8
                    + "Style=? "        // 9
                    + "where "
                    + "BookCode=? "         // 10
                    + "and ChapterIndex=? " // 11
                    + "and StartIndex=? "   // 12
                    + "and StartOffset=? "  // 13
                    + "and EndIndex=? "     // 14
                    + "and EndOffset=?";    // 15
            statement.setInt(1,highlight.startIndex);
            statement.setInt(2,highlight.startOffset);
            statement.setInt(3,highlight.endIndex);
            statement.setInt(4,highlight.endOffset);
            statement.setInt(5,highlight.color);
            statement.setString(6,highlight.text);
            statement.setString(7,highlight.note);
            statement.setInt(8,highlight.isNote?1:0);
            statement.setInt(9,highlight.style);
            // where arguments
            statement.setInt(10,highlight.bookCode);
            statement.setInt(11,highlight.chapterIndex);
            statement.setInt(12,highlight.startIndex);
            statement.setInt(13,highlight.startOffset);
            statement.setInt(14,highlight.endIndex);
            statement.setInt(15,highlight.endOffset);
            
            int rc = statement.executeUpdate();
            System.out.println(rc);
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void deleteHighlight(Highlight highlight) {
        try {
            String sql = String.format(Locale.US,"DELETE FROM Highlight where BookCode=%d and ChapterIndex=%d and StartIndex=%d and StartOffset=%d and EndIndex=%d and EndOffset=%d"
                    ,highlight.bookCode
                    ,highlight.chapterIndex
                    ,highlight.startIndex
                    ,highlight.startOffset
                    ,highlight.endIndex
                    ,highlight.endOffset);
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteHighlightByCode(int code) {
        try {
            String sql = String.format(Locale.US,"DELETE FROM Highlight where Code=%d",code);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public PagingInformation fetchPagingInformation(PagingInformation pgi) {
        try {
            String sql = String.format(Locale.US,	"SELECT * FROM Paging WHERE BookCode=%d AND ChapterIndex=%d AND FontName='%s' AND FontSize=%d AND LineSpacing=%d AND ABS(Width-%f)<=2 AND ABS(Height-%f)<=2 AND IsPortrait=%d AND IsDoublePagedForLandscape=%d",
                    pgi.bookCode,   pgi.chapterIndex,   pgi.fontName,     pgi.fontSize,   pgi.lineSpacing,   pgi.width,              pgi.height,	pgi.isPortrait ? 1:0,	pgi.isDoublePagedForLandscape?1:0);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                PagingInformation pg = new PagingInformation();
                pg.bookCode = rs.getInt(1);
                pg.code = rs.getInt(2);
                pg.chapterIndex = rs.getInt(3);
                pg.numberOfPagesInChapter = rs.getInt(4);
                pg.fontName = rs.getString(5);
                pg.fontSize = rs.getInt(6);
                pg.lineSpacing = rs.getInt(7);
                pg.width = rs.getDouble(8);
                pg.height = rs.getDouble(9);
                pg.verticalGapRatio = rs.getDouble(10);
                pg.horizontalGapRatio = rs.getDouble(11);
                pg.isPortrait = rs.getInt(12)!=0;
                pg.isDoublePagedForLandscape = rs.getInt(13)!=0;
                rs.close();
                statement.close();
                return pg;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void deletePagingInformation(PagingInformation pgi) {
        try {
            String sql = String.format(Locale.US,"DELETE FROM Paging WHERE BookCode=%d AND ChapterIndex=%d AND FontName='%s' AND FontSize=%d AND LineSpacing=%d AND Width=%f AND Height=%f AND HorizontalGapRatio=%f AND VerticalGapRatio=%f AND IsPortrait=%d AND IsDoublePagedForLandscape=%d",
                    pgi.bookCode,	pgi.chapterIndex,		pgi.fontName,		pgi.fontSize,		pgi.lineSpacing,	pgi.width,		pgi.height,		pgi.horizontalGapRatio,		pgi.verticalGapRatio,		pgi.isPortrait ? 1:0,	pgi.isDoublePagedForLandscape?1:0);            PreparedStatement statement = connection.prepareStatement(sql);
                    statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // if existing pagingInformation found, update it.
    public void insertPagingInformation(PagingInformation pgi) {
        PagingInformation tgi = this.fetchPagingInformation(pgi);
        if (tgi!=null) {
            this.deletePagingInformation(tgi);
        }
        try {
            PreparedStatement statement = connection.prepareStatement("Insert into Paging(BookCode,ChapterIndex,NumberOfPagesInChapter,FontName,FontSize,LineSpacing,Width,Height,VerticalGapRatio,HorizontalGapRatio,IsPortrait,IsDoublePagedForLandscape) values(?,?,?,?,?,?,?,?,?,?,?,?);");
            
            statement.setInt(1, pgi.bookCode);
            statement.setInt(2, pgi.chapterIndex);
            statement.setInt(3, pgi.numberOfPagesInChapter);
            statement.setString(4, pgi.fontName);
            statement.setInt(5, pgi.fontSize);
            statement.setInt(6, pgi.lineSpacing);
            statement.setDouble(7, pgi.width);
            statement.setDouble(8, pgi.height);
            statement.setDouble(9, pgi.verticalGapRatio);
            statement.setDouble(10, pgi.horizontalGapRatio);
            statement.setInt(11, pgi.isPortrait ? 1:0);
            statement.setInt(12, pgi.isDoublePagedForLandscape ? 1:0);
            
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public String fetchKey(String uuidForEpub, String uuidForContent) {
        try {
            String sql = String.format(Locale.US,"select * from Key where UUIDForEpub = '%s' and UUIDForContent = '%s'",uuidForEpub,uuidForContent);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                String key = rs.getString("Key");
                return SimpleCrypto.decrypt("Ui0taCjK5A", key);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void insertKey(String uuidForEpub, String uuidForContent,String rawKey) {
        try {
            String key = SimpleCrypto.encrypt("Ui0taCjK5A", rawKey);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Key (UUIDForEpub,UUIDForContent,Key) VALUES(?,?,?)");
            statement.setString(1,uuidForEpub);
            statement.setString(2,uuidForContent);
            statement.setString(3,key);
            statement.executeUpdate();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getSHA1HashFromString(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = input.getBytes();
        md.update(buffer);
        byte[] digest = md.digest();
        
        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr;
    }
    
    public static String getHashFromString(String input) throws Exception
    {
        try {
            return SkyBooks.getSHA1HashFromString(input);
        }catch(Exception e) {
            throw e;
        }
    }
    
    public static Date getDateByString(String expdate){
        
        try{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            Date d=sdf.parse(expdate);
            return d;
        }catch(Exception e){
            return null;
        }
        
    }
    
    private String doGet(String urlString){
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(urlString);
        HttpResponse httpResponse = null;
        
        try {
            httpResponse = httpClient.execute(httpGet);
        } catch (ClientProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        
        HttpEntity httpEntity = httpResponse.getEntity();
        InputStream is = null;
        try {
            is = httpEntity.getContent();
        } catch (IllegalStateException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8);
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
        } catch (Exception e) {
            return null;
        }
        
        return sb.toString();
    }
    
    public String toCDATA(String rawString) {
        String string = rawString;
        string = "<![CDATA[" + string.replaceAll("]]>", "]]>]]><![CDATA[") + "]]>";
        return string;
    }
    
    public String removeNewLine(String rawString) {
        String string = rawString;
        string = string.replace("\r\n","!@#$NEWLINE!@#$");
        string = string.replace("\n","!@#$NEWLINE!@#$");
        return string;
    }
    
    
    // DEDICATED ROUTINES
    public boolean isCopyProtected() {
        return false;
    }
    
    public boolean isCaptureProtected() {
        return false;
    }
    
    
    public void reportRating(BookInformation bi,int rating,String rawReview) {
        try {
            bi.isRated = true;
            updateRated(bi);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public String getLatestVersion() throws Exception {
        try{
            String version = "1.0.1";
            return version;
        }catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public void installSampleFromURL(String url) {
        try {
            String sampleName = url.substring(url.lastIndexOf('/')+1);
            sampleName = sampleName.replace("?dl=1", "");
            String targetPath = this.getBooksDirectory()+"/"+sampleName;
            bookExecutor.submit(new DownloadTask(url,targetPath,new SampleDownloadDelegate(targetPath)));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void installSampleFromResources(String sample) {
        InputStream is = null;
        try {
            String targetPath = this.getBooksDirectory()+"/"+sample;
            File file = new File(targetPath);
            if (file.exists()) {
                return;
            }
            is = getClass().getClassLoader().getResourceAsStream("resources/"+sample);
            this.copyInputStreamToFile(is, targetPath);
            this.installLocalEpub(targetPath);
            is.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void copyInputStreamToFile(InputStream in, String targetPath ) {
        try {
            File file = new File(targetPath);
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class SampleDownloadDelegate implements DownloadListener {
        String targetPath = "";
        SampleDownloadDelegate(String targetPath) {
            this.targetPath = targetPath;
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
                    installLocalEpub(targetPath);
                    File file = new File(targetPath);
                    file.delete();
                    if (mainViewController!=null) {
                        mainViewController.reload();
                    }
                }
            });
        }
        
        @Override
        public void onError(String msg, Exception e) {
        }
    }
    
    
    public String getAppName() {
        return "skybooks";
    }
    
    public String getCUSName() {
        return "sky";
    }
    
    public String getAppTitle() {
        return "SkyBooks for Desktop";
    }
    
    public String getHomeURL() {
        return "http://www.skyepub.net";
    }
    
    public String getDownloadURL() {
        return "http://www.skyepub.net/downloads";
    }
    
    public String getHomeName() {
        return "SkyEpub";
    }
    
    public String getLicenseKey0() {
        return "0000-0000-0000-0000";
    }
    
    public String getLicenseKey1() {
        return "0000-0000-0000-0000";
    }
    
    
    public void installSamples() {
//        installSampleFromURL("http://sws.skyepub.net/samples/Alice.epub");
installSampleFromResources("Alice.epub");
    }
    
    // entry point
    public void processInit() {
        System.setProperty("prism.lcdtext", "true");
        
        this.setup();
        User user = this.getAutoUser();
        if (user!=null) this.setUser(user);
        // The only part to be replaced according to Server.
        //this.setSyncListener(new SyncData());
        this.processSync();
    }
}
