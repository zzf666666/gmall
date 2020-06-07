package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.ums.exception.UserException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean check(String data, Integer type) {

        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();

        switch (type){
            case 1: queryWrapper.eq("username", data); break;
            case 2: queryWrapper.eq("phone",data); break;
            case 3: queryWrapper.eq("email",data); break;
            default: break;
        }

        return baseMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public void register(UserEntity user,String code) {
        String password = user.getPassword();

        String salt = UUID.randomUUID().toString().substring(0, 6);
        user.setSalt(salt);

        user.setPassword(DigestUtils.md5Hex(password + salt));

        user.setLevelId(1l);
        user.setSourceType(1);
        user.setIntegration(1000);
        user.setGrowth(2000);
        user.setStatus(1);
        user.setCreateTime(new Date());
        baseMapper.insert(user);
    }

    @Override
    public UserEntity queryByUsernameAndpassword(String username, String password) {
        UserEntity userEntity = baseMapper.selectOne(new QueryWrapper<UserEntity>().eq("username", username).or().eq("phone", username).or().eq("email", username));
        if(userEntity == null){
            throw new UserException("用户名或密码错误");
        }

        if(!StringUtils.equals(userEntity.getPassword(), DigestUtils.md5Hex(password + userEntity.getSalt()))){
            throw new UserException("用户名或密码错误");
        }

        return userEntity;
    }

}