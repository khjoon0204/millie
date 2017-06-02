package login.protocol;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import login.callback.MyURLConnectionCallback;
import login.view.MainApp;

public class MyURLConnection extends URLConnection 
{

	MyURLConnectionCallback myUrlConnectionCallback;
	public void register(MyURLConnectionCallback callback){
		myUrlConnectionCallback = callback;
	}
	
    protected MyURLConnection(URL url) {
		super(url);
		// TODO Auto-generated constructor stub
		register(MainApp.loginWebviewController);

	}

	//private byte[] data;

    @Override
    public void connect() throws IOException
    {
        if (connected)
        {
            return;
        }
        //loadImage();
        
        callMethod();
        
        connected = true;
    }
    
    private void callMethod(){
    	//System.out.println("callMethod host = " + getURL().getHost()); 
    	
    	String host = getURL().getHost();
    	try {
    		 
    		if(host.equals("apiRequestSuccess")){
    			String msg = getURL().getPath().substring(1);
    			myUrlConnectionCallback.apiRequestSuccessCallback(msg);
    			//System.out.println("path = " + getURL().getPath());

        	}
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	
    	
    }
    

/*    
    public String getHeaderField(String name)
    {
        if ("Content-Type".equalsIgnoreCase(name))
        {
            return getContentType();
        }
        else if ("Content-Length".equalsIgnoreCase(name))
        {
            return "" + getContentLength();
        }
        return null;
    }

    public String getContentType()
    {
        String fileName = getURL().getFile();
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        return "image/" + ext; // TODO: switch based on file-type
    }

    public int getContentLength()
    {
        return data.length;
    }

    public long getContentLengthLong()
    {
        return data.length;
    }

    public boolean getDoInput()
    {
        return true;
    }

    public InputStream getInputStream() throws IOException
    {
        connect();
        return new ByteArrayInputStream(data);
    }

    private void loadImage() throws IOException
    {
        if (data != null)
        {
            return;
        }
        try
        {
            int timeout = this.getConnectTimeout();
            long start = System.currentTimeMillis();
            URL url = getURL();

            String imgPath = url.toExternalForm();
            imgPath = imgPath.startsWith("myapp://") ? imgPath.substring("myapp://".length()) : imgPath.substring("myapp:".length()); // attention: triple '/' is reduced to a single '/'

            // this is my own asynchronous image implementation
            // instead of this part (including the following loop) you could do your own (synchronous) loading logic
            
            MyImage img = MyApp.getImage(imgPath);
            do
            {
                if (img.isFailed())
                {
                    throw new IOException("Could not load image: " + getURL());
                }
                else if (!img.hasData())
                {
                    long now = System.currentTimeMillis();
                    if (now - start > timeout)
                    {
                        throw new SocketTimeoutException();
                    }
                    Thread.sleep(100);
                }
            } while (!img.hasData());
            data = img.getData();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public OutputStream getOutputStream() throws IOException
    {
        // this might be unnecessary - the whole method can probably be omitted for our purposes
        return new ByteArrayOutputStream();
    }

    public java.security.Permission getPermission() throws IOException
    {
        return null; // we need no permissions to access this URL
    }
*/
}
