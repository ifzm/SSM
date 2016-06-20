package com.cn.model;

import com.cn.base.BasicModel;

/**
 * @ClassName User
 * @Description TODO 
 * @Author devfzm@gmail.com
 * @Date Thu Jun 16 17:05:13 CST 2016
 */

public class User extends BasicModel {
	// SerialVersionUID
	private static final long serialVersionUID = 14660679132917L;
	// 管理员编号
	private Integer id;
	// 管理员名称
	private String name;
	// 管理员账号
	private String account;
	// 管理员密码
	private String password;
	// 管理员状态(1-激活，2-冻结）
	private Integer state;
	// 角色编号
	private Integer role_id;

	public User() { }

	public User(Integer id, String name, String account, String password, Integer state, Integer role_id) {
		super();
		this.id = id;
		this.name = name;
		this.account = account;
		this.password = password;
		this.state = state;
		this.role_id = role_id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getRoleId() {
		return role_id;
	}

	public void setRoleId(Integer role_id) {
		this.role_id = role_id;
	}

}

