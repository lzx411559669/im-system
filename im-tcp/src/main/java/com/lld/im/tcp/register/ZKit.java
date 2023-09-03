package com.lld.im.tcp.register;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKit {
    private ZkClient zkClient;

    public ZKit(ZkClient zkClient){
        this.zkClient = zkClient;
    }

    //im-coreRoot/tcp/ip:port
    public void createRootNode(){
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if(!exists){
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }
        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        if(!tcpExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        }
        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot
                + Constants.ImCoreZkRootWeb);
        if(!webExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        }
    }
    //ip+port
    public void createNode(String path){
        if (!zkClient.exists(path)){
            zkClient.createPersistent(path);
        }
    }

    public static class RegistryZK implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(RegistryZK.class);
        private ZKit zKit;

        private String ip;

        private BootstrapConfig.TcpConfig tcpConfig;

        public RegistryZK(ZKit zKit,String ip,BootstrapConfig.TcpConfig tcpConfig){
            this.zKit = zKit;
            this.ip = ip;
            this.tcpConfig = tcpConfig;
        }

        public void run(){
            zKit.createRootNode();
            String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip+":" + tcpConfig.getTcpPort();
            zKit.createNode(tcpPath);
            logger.info("Registry zookeeper tcpPath success, msg=[{}]",tcpPath);

            String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip+":" + tcpConfig.getTcpPort();
            zKit.createNode(webPath);
            logger.info("Registry zookeeper webPath success, msg=[{}]",webPath);
        }



    }
}
