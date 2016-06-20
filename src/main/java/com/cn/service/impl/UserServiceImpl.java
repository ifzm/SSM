package com.cn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.base.BasicMapper;
import com.cn.base.BasicServiceImpl;
import com.cn.dao.UserMapper;
import com.cn.model.User;
import com.cn.service.UserService;

@Service
public class UserServiceImpl extends BasicServiceImpl implements UserService {
	
	@Autowired
	UserMapper userMapper;

	@Override
	public BasicMapper getMapper() {
		return userMapper;
	}

	@Override
	public void saveOrUpdate(User user) {
		// 将事务放在controller层是不合理的想法，业务逻辑理应放在service层
		// ，一旦某个环节出错，相关save&update&delete操作应该回滚
		userMapper.save(user);
		
		user.setAccount("fzm");
		userMapper.update(user);
	}
	
}
