/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybooks;

import com.skytree.epub_desktop.BookInformation;
import java.util.ArrayList;

/**
 *
 * @author skytree
 */
public interface SyncListener {
    public ArrayList<BookInformation> getBookInformationsFromServer();
    public ArrayList<Category> getCategoriesFromServer();
    public void setUser(User user);
    public boolean checkUser(User user);
    public String getKey(String uuidForContent,String uuidForEpub);
}
