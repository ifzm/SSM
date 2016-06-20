package com.cn.util;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

/**
 * @ClassName: RedisCache
 * @Description: Redis - Mybatis二级缓存
 * @Author devfzm@gmail.com
 * @Date 2016年6月18日 下午11:47:34
 */

public class RedisCache implements Cache {

	private static Logger logger = Logger.getLogger(RedisCache.class);

	// 通过统一的RedisUtil获取Jedis
	private Jedis redisClient = RedisUtil.getJedis();
	
	// 读写锁
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private String id;

	public RedisCache(final String id) {
		if (id == null) {
			throw new IllegalArgumentException("Cache instances require an ID");
		}
		logger.info(" >>> RedisCache: id = " + id);
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public int getSize() {
		return Integer.valueOf(redisClient.dbSize().toString());
	}

	public void putObject(Object key, Object value) {
		// logger.info(" >>> putObject: " + key + " = " + value);
		redisClient.setex(SerializeUtil.serialize(key.toString()), 6000,
				SerializeUtil.serialize(value));
	}

	public Object getObject(Object key) {
		Object value = SerializeUtil.unserialize(redisClient.get(SerializeUtil
				.serialize(key.toString())));
		// logger.info(" >>> getObject: " + key + " = " + value);
		return value;
	}

	public Object removeObject(Object key) {
		return redisClient.expire(SerializeUtil.serialize(key.toString()), 0);
	}

	public void clear() {
		redisClient.flushDB();
	}

	public ReadWriteLock getReadWriteLock() {
		return readWriteLock;
	}

}
