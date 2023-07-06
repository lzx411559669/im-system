package com.lld.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.AllowFriendTypeEnum;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.enums.FriendShipStatusEnum;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.model.resp.ImportFriendShipResp;
import com.lld.im.service.friendship.service.ImFriendService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.service.ImUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuzhengxing
 * @version v1.0
 * @package com.lld.im.service.friendship.service.impl
 * @data 2023/7/5 23:11
 */
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Autowired
    private ImFriendShipMapper imFriendShipMapper;

    @Autowired
    private ImUserService imUserService;
    @Override
    public ResponseVO importFriendShip(ImporFriendShipReq req) {

        if(req.getFriendItem().size() > 100){
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImporFriendShipReq.ImportFriendDto dto:
                req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto,entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if(insert == 1){
                    successId.add(dto.getToId());
                }else{
                    errorId.add(dto.getToId());
                }
            }catch (Exception e){
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {
        //查询 from-to两个人是否存在
        ResponseVO<ImUserDataEntity> fromItem = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromItem.isOk()){
            return fromItem;
        }
        ResponseVO<ImUserDataEntity> toItem = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toItem.isOk()){
            return toItem;
        }

        ImUserDataEntity data = toItem.getData();
        //添加好友不需要验证
        if (data.getFriendAllowType()!=null && data.getFriendAllowType().equals(AllowFriendTypeEnum.NOT_NEED)){
            return doAddFriend(req.getFromId(),req.getToItem(),req.getAppId());
        }else{
            //需要验证
            QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("from_id",req.getFromId())
                    .eq("to_id",req.getToItem().getToId())
                    .eq("app_id",req.getAppId());
            ImFriendShipEntity from = imFriendShipMapper.selectOne(queryWrapper);
            //如果form方不存在好友关系记录,或者好友关系不正常
            if (from == null || from.getStatus() !=FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
                //插入一条申请记录

            }else{
                //已经是好友关系
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }

        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {
        //判断是否存在form和to用户
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if(!fromInfo.isOk()){
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if(!toInfo.isOk()){
            return toInfo;
        }

        ResponseVO responseVO = this.doUpdate(req.getFromId(), req.getToItem(), req.getAppId());
        return responseVO;
    }

    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id",req.getFromId())
                .eq("to_id",req.getToId())
                .eq("app_id",req.getAppId());
        ImFriendShipEntity from = imFriendShipMapper.selectOne(queryWrapper);
        //from 为空，已经不是好友
        if (from == null){
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }else{
            //如果存在好友关系，则更新好友关系为删除状态
            if (from.getStatus()!=null && from.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
               from = new ImFriendShipEntity();
               from.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
               imFriendShipMapper.update(from,queryWrapper);
            }else{
                //已经是删除好友关系
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("status",FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update( update,query);
        return ResponseVO.successResponse();
    }

    public ResponseVO doUpdate(String fromId, FriendDto dto, Integer appId){
        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getAddSource,dto.getAddSource())
                .set(ImFriendShipEntity::getExtra,dto.getExtra())
                .set(ImFriendShipEntity::getRemark,dto.getRemark())
                .eq(ImFriendShipEntity::getAppId,appId)
                .eq(ImFriendShipEntity::getToId,dto.getToId())
                .eq(ImFriendShipEntity::getFromId,fromId);

        int update = imFriendShipMapper.update(null, updateWrapper);
        if(update == 1){
            return ResponseVO.successResponse();
        }
        return ResponseVO.errorResponse();
    }

    @Transactional
    public ResponseVO doAddFriend(String fromId, FriendDto dto, Integer appId){
        //A-B
        //Friend插入A，B两条记录
        //查询是否存在记录，如果记录存在，判断状态，如果是未添加测修改状态，如果是已经添加则返回已添加

        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",appId).eq("from_id",fromId).eq("to_id",dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryWrapper);
        if (fromItem == null){
            //未添加
            fromItem = new ImFriendShipEntity();
            fromItem.setAppId(appId);
            BeanUtils.copyProperties(dto,fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert!=1){
                //TODO 返回失败

            }
        }else{
            //如果是已经添加
            if (fromItem.getStatus().equals(FriendShipStatusEnum.FRIEND_STATUS_NORMAL)){
                //TODO 返回已经添加
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
            }
            if (fromItem.getStatus().equals(FriendShipStatusEnum.BLACK_STATUS_NORMAL)){
                
                ImFriendShipEntity update = new ImFriendShipEntity();

                if (StringUtils.hasText(dto.getAddSource())){
                    update.setAddSource(dto.getAddSource());
                }
                if (StringUtils.hasText(dto.getExtra())){
                    update.setExtra(dto.getExtra());
                }
                if (StringUtils.hasText(dto.getRemark())){
                    update.setRemark(dto.getRemark());
                }
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(update, queryWrapper);
                if (result!=1){
                    //TODO返回失败
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }
        }

        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("to_id",fromId).eq("from_id",dto.getToId()).eq("app_id",appId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);

        if (toItem == null){
            //插入对方关系表
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            toItem.setToId(fromId);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            BeanUtils.copyProperties(dto,toItem);
            toItem.setCreateTime(System.currentTimeMillis());
            int result = imFriendShipMapper.insert(toItem);
        }else{
            //如果对方关系表是非好友关系
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toItem.getStatus()){
                toItem = new ImFriendShipEntity();
                toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                imFriendShipMapper.update(toItem,toQuery);
            }
        }

        return ResponseVO.successResponse();

    }

    @Override
    public ResponseVO checkBlck(CheckFriendShipReq req) {
        return null;
    }
}
