package com.cn.service;

import com.cn.base.BasicService;
import com.cn.model.User;

public interface UserService extends BasicService {
	
	void saveOrUpdate(User user);
	
}
