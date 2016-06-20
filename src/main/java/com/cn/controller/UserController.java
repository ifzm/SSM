package com.cn.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.base.BasicController;
import com.cn.model.User;
import com.cn.service.UserService;
import com.cn.util.CommUtil;
import com.cn.util.RedisUtil;

/**
 * @ClassName: UserController
 * @Description: TODO
 * @Author devfzm@gmail.com
 * @Date 2016年6月16日 下午10:33:24
 */

@Controller
@RequestMapping("/user")
public class UserController extends BasicController {

	@Autowired
	private UserService userService;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{userid}/show", method = RequestMethod.GET)
	public @ResponseBody Object show(@PathVariable long userid) {
		// 错误日志自动发送邮件通知
		logger.info("userid: " + userid);
		
		// Redis Test， 同样可以使用RedisUtil实现Session分布式存储
		RedisUtil.set("ifzm", "guoguo");
		logger.info(RedisUtil.get("ifzm"));
		
		RedisUtil.decrBy("", (long) 0);
		
		// 首先从redis缓存中取数据，如果取不到，再查询数据库，这里是直接使用mybatis缓存配置，也可自行封装调用
		List<User> users = (List<User>) userService.findAll();
		logger.info(users);

		return users;
	}

	/**
	 * @Title: commitTest
	 * @Description: 事务测试
	 * @param user
	 * @return Object
	 * @throws
	 */
	@RequestMapping("/test")
	@ResponseBody
	public Object commitTest(@ModelAttribute User user) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 模拟数据，进行事务测试
		User test = new User();
		test.setAccount(CommUtil.randomUUID());

		try {
			// account 拥有唯一索引，添加后再次修改 account 为已存在的值，异常触发事务回滚
			userService.saveOrUpdate(test);
		} catch (Exception e) {
			logger.error("Commit error: " + e);
			map.put("msg", test);
		}

		return map;
	}
}
