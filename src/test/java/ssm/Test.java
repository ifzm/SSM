package ssm;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Test {
	// 定义一个阻塞队列，`盘子` - 可以放5个鸡蛋
	BlockingQueue<Object> eggs = new LinkedBlockingQueue<Object>(5);

	// 放入鸡蛋
	public void putEgg(Object obj) {
		try {
			eggs.put(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("放入：" + obj);
	}

	// 取出鸡蛋
	public Object getEgg() {
		Object obj = null;
		try {
			obj = eggs.take();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("取出：" + obj);

		return obj;
	}

	/** 放鸡蛋线程 */
	static class AddThread extends Thread {
		private Test plate;
		private Map<String, String> map;

		public AddThread(Test plate) {
			this.plate = plate;
			this.map = new HashMap<String, String>();
			this.map.put(UUID.randomUUID().toString().replace("-", ""), "fzm-"
					+ UUID.randomUUID().toString().replace("-", ""));
		}

		public void run() {
			plate.putEgg(map);
		}
	}

	/** 取鸡蛋线程 */
	static class GetThread extends Thread {
		private Test plate;

		public GetThread(Test plate) {
			this.plate = plate;
		}

		public void run() {
			plate.getEgg();
		}
	}

	// main
	public static void main(String[] args) {

		try {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("jedis.properties");

			Properties prop = new Properties();
			prop.load(in);
			in.close();
			
			System.out.println(prop.getProperty("hosts"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
