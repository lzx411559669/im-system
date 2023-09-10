package com.lld.im.service.utils;

import com.lld.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
public class Zkit {
    private static Logger logger = Logger.getLogger(Zkit.class.getName());

    @Autowired
    private ZkClient zkClient;

    public List<String> getAllTcpNode(){
        return zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
    }

    public List<String> getAllWebNode(){
        return zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
    }

}
