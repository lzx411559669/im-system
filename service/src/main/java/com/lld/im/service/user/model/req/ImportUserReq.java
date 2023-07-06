package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import com.lld.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @author liuzhengxing
 * @version v1.0
 * @package com.lld.im.service.user.model.req
 * @data 2023/7/4 20:52
 */
@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;


}
