package com.cn.base;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

public abstract class BasicModel implements Serializable {
	
	private static final long serialVersionUID = 6192626890191812241L;

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
}
