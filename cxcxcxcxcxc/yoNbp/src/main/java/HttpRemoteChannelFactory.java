
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import nc.bs.framework.common.RuntimeEnv;
import nc.bs.framework.exception.FrameworkRuntimeException;
import nc.bs.framework.rmi.Address;
import nc.bs.framework.rmi.RemoteChannel;
import nc.bs.framework.rmi.RemoteChannelFactory;

public class HttpRemoteChannelFactory implements RemoteChannelFactory {
    private static boolean myline = "true".equalsIgnoreCase(System.getProperty("ncc.myLine"));
    private Proxy proxy;

    public HttpRemoteChannelFactory() {
        CookieHandler.setDefault(HttpRemoteChannel.newCookieManager());
    }

    public boolean isKeepAlive() {
        String ka = System.getProperty("http.keepAlive");
        return ka != null ? Boolean.valueOf(ka) : true;
    }

    public void setKeepAlive(boolean keepAlive) {
        System.setProperty("http.keepAlive", "" + keepAlive);
    }

    public int getMaxConnections() {
        String mc = System.getProperty("http.maxConnections");
        return mc != null ? Integer.parseInt(mc) : 5;
    }

    public void setMaxConnections(int maxConnections) {
        System.setProperty("http.maxConnections", "" + maxConnections);
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public RemoteChannel createRemoteChannel(Address addr) {
        URL url;
        try {
            url = addr.toURL();
        } catch (MalformedURLException var4) {
            throw new FrameworkRuntimeException("error when create channel", var4);
        }

        return (RemoteChannel)(this.serverEnable() ? new HttpRemoteChannel(url, this.proxy) : new Http1RemoteChannel(url, this.proxy));
    }

    private boolean serverEnable() {
        return RuntimeEnv.getInstance().isRunningInServer() || myline;
    }

    static {
        CookieHandler.setDefault(HttpRemoteChannel.newCookieManager());
    }


}
