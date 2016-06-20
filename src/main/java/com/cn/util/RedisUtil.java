package com.cn.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.google.common.base.Strings;

/**
 * @ClassName: RedisUtil
 * @Description: Redis操作工具类
 * @Author devfzm@gmail.com
 * @Date 2016年6月17日 下午6:30:43
 */

public class RedisUtil {
	private static Logger logger = Logger.getLogger(RedisUtil.class);

	// 获取redis连接相关配置信息，详细注释请移步jedis.properties
	private static Properties prop = CommUtil.readProperties("jedis.properties");
	private static String HOSTS = prop.getProperty("hosts");
	private static String AUTH = prop.getProperty("auth");
	private static int PORT = Integer.parseInt(prop.getProperty("port"));
	private static int MAX_ACTIVE = Integer.parseInt(prop.getProperty("maxActive"));
	private static int MAX_IDLE = Integer.parseInt(prop.getProperty("maxIdle"));
	private static int MAX_WAIT = Integer.parseInt(prop.getProperty("maxWaitMillis"));
	private static int TIMEOUT = Integer.parseInt(prop.getProperty("timeout"));
	private static boolean TEST_ON_BORROW = Boolean.parseBoolean(prop.getProperty("testOnBorrow"));

	// 连接池
	private static JedisPool jedisPool = null;
	
	/**
	 * @Title: initialPool
	 * @Description: 初始化 Redis 连接池
	 * @return void
	 * @throws
	 */
	private static void initialPool() {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(MAX_ACTIVE);
			config.setMaxIdle(MAX_IDLE);
			config.setMaxWaitMillis(MAX_WAIT);
			config.setTestOnBorrow(TEST_ON_BORROW);
			jedisPool = new JedisPool(config, HOSTS.split(",")[0].trim(), PORT, TIMEOUT, AUTH);
		} catch (Exception e) {
			// 记录日志并发送邮件通知
			logger.error("Host create JedisPool error: " + e);

			// 主机连接异常，切换到备机，主备实现同步
			try {
				JedisPoolConfig config = new JedisPoolConfig();
				config.setMaxTotal(MAX_ACTIVE);
				config.setMaxIdle(MAX_IDLE);
				config.setMaxWaitMillis(MAX_WAIT);
				config.setTestOnBorrow(TEST_ON_BORROW);
				jedisPool = new JedisPool(config, HOSTS.split(",")[1].trim(), PORT, TIMEOUT, AUTH);
			} catch (Exception ex) {
				// 记录日志并发送邮件通知
				logger.error("Slave create JedisPool error: " + ex);
			}
		}
	}

	/**
	 * @Title: poolInit
	 * @Description: 在多线程环境同步初始化
	 * @return void
	 * @throws
	 */
	private static synchronized void poolInit() {
		if (jedisPool == null) {
			initialPool();
		}
	}

	/**
	 * @Title: getJedis
	 * @Description: 同步获取Jedis实例
	 * @return 一个实例
	 * @return Jedis
	 * @throws
	 */
	public synchronized static Jedis getJedis() {
		if (jedisPool == null) {
			poolInit();
		}
		Jedis jedis = null;
		try {
			if (jedisPool != null) {
				jedis = jedisPool.getResource();
			}
			// 2.5版本以上不需要手动回收
		} catch (Exception e) {
			logger.error("Get jedis error: " + e);
		}
		return jedis;
	}

	/**
	 * @Title: set 
	 * @Description: TODO
	 * @param key
	 * @param value    
	 * @return void
	 * @throws
	 */
	public static void set(String key, String value) {
		try {
			value = Strings.isNullOrEmpty(value) ? "" : value;
			getJedis().set(key, value);
		} catch (Exception e) {
			logger.error("Set key error: " + e);
		}
	}

	/**
	 * @Title: setex
	 * @Description: TODO
	 * @param key
	 * @param seconds
	 *            过期时间，单位/秒
	 * @param value
	 * @return void
	 * @throws
	 */
	public static void setex(String key, int seconds, String value) {
		try {
			value = Strings.isNullOrEmpty(value) ? "" : value;
			getJedis().setex(key, seconds, value);
		} catch (Exception e) {
			logger.error("Set keyex error: " + e);
		}
	}

	/**
	 * @Title: get
	 * @Description: TODO
	 * @param key
	 * @return value
	 * @return String
	 * @throws
	 */
	public static String get(String key) {
		if (getJedis() == null || !getJedis().exists(key)) {
			return null;
		}
		return getJedis().get(key);
	}
	
	/**
	 * @Title: exists 
	 * @Description: 检测Key是否存在
	 * @param key
	 * @return boolean
	 * @throws
	 */
	public static boolean exists(String key) {
		return getJedis().exists(key);
	}
	
	/**
	 * ....
	 * @param keys
	 * @return 返回删除的个数
	 * @return Long
	 * @throws
	 */
	public static Long del(String... keys) {
		return getJedis().del(keys);
	}
	
	/**
	 * @Title: ttl 
	 * @Description: 查询key的过期时间
	 * @param key 
	 * @return 返回剩余过期时间/秒
	 * @return Long
	 * @throws
	 */
	public static Long ttl(String key) {
		return getJedis().ttl(key);
	}
	
	/**
	 * @Title: expired 
	 * @Description: 设置key的过期时间，以秒为单位
	 * @param key
	 * @param seconds
	 * @return 受影响的记录数
	 * @return Long
	 * @throws
	 */
	public static Long expired(String key, int seconds) {
		return getJedis().expire(key, seconds);
	}
	
	/**
	 * @Title: getSet 
	 * @Description: 设置key的值，并返回最近的一个旧值
	 * @param key
	 * @param value
	 * @return 返回旧值，如果key不存在，返回null
	 * @return String
	 */
	public static String getSet(String key, String value) {
		return getJedis().getSet(key, value);
	}
	
	
	/**
	 * @Title: incr 
	 * @Description: 对key的value进行++操作，key不存在时value=1
	 * @param key
	 * @return 返回++操作后的值
	 * @return Long
	 * @throws 如果value不是int类型抛出异常
	 */
	public static Long incr(String key) {
		return getJedis().incr(key);
	}
	
	/**
	 * @Title: incrBy 
	 * @Description: 通过key给指定的value加integer，如果key不存在，则value=integer
	 * @param key
	 * @param integer
	 * @return 返回 value + integer 后的值
	 * @return Long
	 * @throws 如果value不是int类型抛出异常
	 */
	public static Long incrBy(String key, Long integer) {
		return getJedis().incrBy(key, integer);
	}
	
	/**
	 * @Title: decr 
	 * @Description: 对key的value进行--操作，如果key不存在，则value=-1
	 * @param key
	 * @return 返回--操作后的值
	 * @return Long
	 * @throws 如果value不是int类型抛出异常
	 */
	public static Long decr(String key) {
		return getJedis().decr(key);
	}
	
	/**
	 * @Title: decrBy 
	 * @Description: 通过key给指定的value减integer，如果key不存在，则value=-integer
	 * @param key
	 * @param integer
	 * @return 返回value - integer后的值
	 * @return Long
	 * @throws 如果value不是int类型抛出异常
	 */
	public static Long decrBy(String key, Long integer) {
		return getJedis().decrBy(key, integer);
	}
	
	/**
	 * @Title: hset 
	 * @Description: 通过key给field设置指定的value,如果key不存在,则先创建
	 * @param key
	 * @param field
	 * @param value
	 * @return Long
	 * @throws
	 */
	public static Long hset(String key, String field, String value) {
		return getJedis().hset(key, field, value);
	}
	
	/**
	 * @Title: hget 
	 * @Description: 通过key和field获取对应的value
	 * @param key
	 * @param field
	 * @return    
	 * @return String
	 * @throws
	 */
	public static String hget(String key, String field) {
		return getJedis().hget(key, field);
	}
	
	/**
	 * @Title: hmset 
	 * @Description: 通过key同时设置hash的多个field-value
	 * @param key
	 * @param hash
	 * @return String
	 * @throws
	 */
	public static String hmset(String key, Map<String, String> hash) {
		return getJedis().hmset(key, hash);
	}
	
	/**
	 * @Title: hmget 
	 * @Description: 通过key和fields获取指定的values，可以是多个field
	 * @param key
	 * @param fields
	 * @return 返回结果集
	 * @return List<String>
	 * @throws
	 */
	public static List<String> hmget(String key, String... fields) {
		return getJedis().hmget(key, fields);
	}
	
	
}
