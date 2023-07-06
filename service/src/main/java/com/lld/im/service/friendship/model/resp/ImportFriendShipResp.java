package com.lld.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @author liuzhengxing
 * @version v1.0
 * @package com.lld.im.service.friendship.model.resp
 * @data 2023/7/5 23:06
 */
@Data
public class ImportFriendShipResp {

    private List<String> successId;

    private List<String> errorId;
}
