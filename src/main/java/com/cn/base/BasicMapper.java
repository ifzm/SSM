package com.cn.base;

import java.util.List;

public interface BasicMapper {

	int save(BasicModel model);

	int update(BasicModel model);

	int delete(Object key);

	BasicModel findByPrimaryKey(Object key);

	BasicModel findOne(BasicModel model);

	List<? extends BasicModel> find(BasicModel model);

	List<? extends BasicModel> findAll();

	long count();

}
