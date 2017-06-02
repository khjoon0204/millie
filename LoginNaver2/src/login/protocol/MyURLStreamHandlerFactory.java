package login.protocol;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import login.view.LoginWebviewController;

public class MyURLStreamHandlerFactory implements URLStreamHandlerFactory
{

    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        if (protocol.equals(LoginWebviewController.PROTOCOL_SCHEME))
        {
            return new MyURLHandler();
        }
        return null;
    }

}
