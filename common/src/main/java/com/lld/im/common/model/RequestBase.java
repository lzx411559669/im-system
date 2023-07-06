package com.lld.im.common.model;

import lombok.Data;

/**
 * @author liuzhengxing
 * @version v1.0
 * @package com.lld.im.common.model
 * @data 2023/7/4 20:52
 */
@Data
public class RequestBase {
    private Integer appId;

    private String operater;

    private Integer clientType;

    private String imei;
}
