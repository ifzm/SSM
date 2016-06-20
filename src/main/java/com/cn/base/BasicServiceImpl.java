package com.cn.base;

import java.util.List;

public abstract class BasicServiceImpl implements BasicService {

	public abstract BasicMapper getMapper();
	
	public int save(BasicModel model) {
		return getMapper().save(model);
	}

	public int update(BasicModel model) {
		return getMapper().update(model);
	}

	public int delete(Object key) {
		return getMapper().delete(key);
	}

	public BasicModel findByPrimaryKey(Object key) {
		return getMapper().findByPrimaryKey(key);
	}

	public BasicModel findOne(BasicModel model) {
		return getMapper().findOne(model);
	}

	public List<? extends BasicModel> find(BasicModel model) {
		return getMapper().find(model);
	}

	public List<? extends BasicModel> findAll() {
		return getMapper().findAll();
	}

	public long count() {
		return getMapper().count();
	}

}
