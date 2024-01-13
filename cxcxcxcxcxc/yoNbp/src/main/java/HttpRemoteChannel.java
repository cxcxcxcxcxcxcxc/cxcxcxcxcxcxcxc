

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.X509Certificate;

import nc.bs.framework.common.ServerLocatorFacade;
import nc.bs.framework.rmi.RemoteChannel;
import nc.bs.framework.rmi.RmiCookieHandler;

import javax.net.ssl.*;

class HttpRemoteChannel implements RemoteChannel {
    private URL url;
    private HttpURLConnection conn;
    private Proxy proxy;
    private RCInputStream rcInput;
    private RCOutputStream rcOutput;

    //忽略https 证书
    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //

    public HttpRemoteChannel(URL url, Proxy proxy) {
        this.url = url;
        this.proxy = proxy;
    }

    public void destroy() {
        if (this.rcOutput != null) {
            try {
                this.rcOutput.close();
            } catch (IOException var3) {
            }
        }

        if (this.rcInput != null) {
            try {
                this.rcInput.close();
            } catch (IOException var2) {
            }
        }

        if (this.conn != null) {
            this.conn.disconnect();
        }

        this.conn = null;
        this.rcInput = null;
        this.rcOutput = null;
    }

    public InputStream getInputStream() {
        return this.rcInput;
    }

    public OutputStream getOutputStream() {
        return this.rcOutput;
    }

    public void init() throws IOException {
        trustAllHosts(); //https 证书
        String protocol = this.url.getProtocol();
        if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) {
            throw new IOException("Illegal Protocol " + this.url.getProtocol());
        } else if (this.conn != null) {
            throw new IllegalStateException("attempt to reprepare remote channel for write: " + this.url);
        } else {
            if (this.proxy != null) {
                this.conn = (HttpURLConnection)this.url.openConnection(this.proxy);
            } else if (protocol.equalsIgnoreCase("https")){
                HttpsURLConnection https  = (HttpsURLConnection)this.url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                this.conn = https;
            }else {
                this.conn = (HttpURLConnection)this.url.openConnection();
            }

            this.conn.setDoOutput(true);
            this.conn.setUseCaches(false);
            this.conn.setRequestMethod("POST");
            this.conn.setRequestProperty("Content-type", "application/octet-stream");
            this.conn.setRequestProperty("serverEnable", "localserver");
            int timeOut = ServerLocatorFacade.getTimeOut();
            if (timeOut > 0) {
                this.conn.setConnectTimeout(timeOut);
                this.conn.setReadTimeout(timeOut);
            }

            this.rcInput = new RCInputStream();
            this.rcOutput = new RCOutputStream();
        }
    }

    public void processIOException(IOException ioe) throws IOException {
        if (ioe instanceof ConnectException) {
            CookieHandler.setDefault(newCookieManager());
        } else if (this.conn.getResponseCode() > 500) {
            CookieHandler.setDefault(newCookieManager());
        }

        InputStream es = this.conn.getErrorStream();
        if (es != null) {
            byte[] buf = new byte[4096];

            while(true) {
                if (es.read(buf) <= 0) {
                    es.close();
                    break;
                }
            }
        }

    }

    public static CookieHandler newCookieManager() {
        return new RmiCookieHandler();
    }

    class RCOutputStream extends OutputStream {
        private OutputStream out;

        public RCOutputStream() throws IOException {
            this.writePrepare();
        }

        public void deactivate() {
            if (this.out != null) {
                try {
                    this.out.close();
                } catch (IOException var2) {
                }
            }

            this.out = null;
        }

        public void write(int b) throws IOException {
            if (this.out == null) {
                throw new IllegalStateException("inactive remote outputstream: " + HttpRemoteChannel.this.url);
            } else {
                this.out.write(b);
            }
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (len != 0) {
                if (this.out == null) {
                    throw new IllegalStateException("inactive remote outputstream: " + HttpRemoteChannel.this.url);
                } else {
                    this.out.write(b, off, len);
                }
            }
        }

        public void flush() throws IOException {
            if (this.out != null) {
                this.out.flush();
            }

        }

        public void close() throws IOException {
            if (this.out != null) {
                this.out.flush();
                this.out.close();
            }

        }

        private void writePrepare() throws IOException {
            HttpRemoteChannel.this.rcInput.deactivate();
            this.out = HttpRemoteChannel.this.conn.getOutputStream();
        }
    }

    class RCInputStream extends InputStream {
        private InputStream in;

        RCInputStream() {
        }

        public void deactivate() {
            this.in = null;
        }

        public int read() throws IOException {
            if (this.in == null) {
                this.in = this.readPrepare();
            }

            return this.in.read();
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            } else {
                if (this.in == null) {
                    this.in = this.readPrepare();
                }

                return this.in.read(b, off, len);
            }
        }

        public long skip(long n) throws IOException {
            if (n == 0L) {
                return 0L;
            } else {
                if (this.in == null) {
                    this.in = this.readPrepare();
                }

                return this.in.skip(n);
            }
        }

        public int available() throws IOException {
            if (this.in == null) {
                this.in = this.readPrepare();
            }

            return this.in.available();
        }

        public void close() throws IOException {
            if (this.in != null) {
                this.in.close();
            }

        }

        public synchronized void mark(int readlimit) {
            if (this.in == null) {
                try {
                    this.in = this.readPrepare();
                } catch (IOException var3) {
                    return;
                }
            }

            this.in.mark(readlimit);
        }

        public synchronized void reset() throws IOException {
            if (this.in == null) {
                this.in = this.readPrepare();
            }

            this.in.reset();
        }

        public boolean markSupported() {
            if (this.in == null) {
                try {
                    this.in = this.readPrepare();
                } catch (IOException var2) {
                    return false;
                }
            }

            return this.in.markSupported();
        }

        private InputStream readPrepare() throws IOException {
            HttpRemoteChannel.this.rcOutput.deactivate();
            this.in = HttpRemoteChannel.this.conn.getInputStream();
            HttpRemoteChannel.this.conn.getContentType();
            return this.in;
        }
    }
}
