package com.lld.im.service.friendship.model.req;

import lombok.Data;

/**
 * @author liuzhengxing
 * @version v1.0
 * @package com.lld.im.service.friendship.model.req
 * @data 2023/7/5 23:20
 */
@Data
public class FriendDto {

    private String toId;

    private String remark;

    private String addSource;

    private String extra;

    private String addWording;

}
