package com.lld.im.tcp;


import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.register.ZKit;
import com.lld.im.tcp.reviver.MessageReviver;
import com.lld.im.tcp.server.LimServer;
import com.lld.im.tcp.utils.MqFactory;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Starter {
    //    HTTP GET POST PUT DELETE 1.0 1.1 2.0
    //client IOS 安卓 pc(windows mac) web //支持json 也支持 protobuf
    //appId
    //28 + imei + body
    //请求头（指令 版本 clientType 消息解析类型 imei长度 appId bodylen）+ imei号 + 请求体
    //len+body

    public static void main(String[] args) {
        System.out.println(args.length);
        if (args.length>0){
            System.out.println(args[0]);
            start(args[0]);
        }
    }

    public static void start(String path){
        try {
            Yaml yaml = new Yaml();
            FileInputStream fileInputStream = new FileInputStream(path);
            BootstrapConfig bootStrapConfig = yaml.loadAs(fileInputStream, BootstrapConfig.class);
            new LimServer(bootStrapConfig.getLim()).start();

            RedisManager.init(bootStrapConfig);
            MqFactory.init(bootStrapConfig.getLim().getRabbitmq());
            MessageReviver.init(bootStrapConfig.getLim().getBrokerId().toString());
            registerZK(bootStrapConfig);


        }catch (Exception e){
            e.printStackTrace();
            System.exit(500);
        }

    }


    public static void registerZK(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(),
                config.getLim().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        ZKit.RegistryZK registryZK = new ZKit.RegistryZK(zKit, hostAddress, config.getLim());
        Thread thread = new Thread(registryZK);
        thread.start();
    }
}
