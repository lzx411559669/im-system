package com.lld.im.service.user.controller;

import com.lld.im.common.ClientType;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.RouteInfo;
import com.lld.im.common.utils.RouteInfoParseUtil;
import com.lld.im.service.user.model.req.DeleteUserReq;
import com.lld.im.service.user.model.req.ImportUserReq;
import com.lld.im.service.user.model.req.LoginReq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.Zkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liuzhengxing
 * @version v1.0
 * @package com.lld.im.service.user.controller
 * @data 2023/7/4 21:29
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {
    @Autowired
    ImUserService imUserService;

    @Autowired
    private Zkit zkit;

    @Resource
    private RouteHandle routeHandle;


    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId) {
        return imUserService.importUser(req);
    }

    @DeleteMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description im的登录接口，返回im地址
     * @author chackylee
     */
    @PostMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        ResponseVO login = imUserService.login(req);
        if (login.isOk()){
            List<String> allNode = new ArrayList<>();
            if (req.getClientType() == ClientType.WEB.getCode()){
                allNode = zkit.getAllWebNode();
            }else{
                allNode = zkit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }
        return ResponseVO.errorResponse();



    }

}