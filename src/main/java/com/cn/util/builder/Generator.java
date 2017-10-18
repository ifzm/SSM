package com.cn.util.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.util.CommUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * @ClassName: Generator
 * @Description: [Model + Dao + Mapper + Service] - generator
 * @Author devfzm@gmail.com
 * @Date 2016年6月18日 下午12:07:51
 */

public class Generator {

	// 缓存数据库连接
	private Connection con;

	// 配置文件信息
	private JSONObject config;

	// 缓存表名
	private String table;
	// 缓存表名---驼峰形式
	private String tableName;
	// 缓存表字段和字段类型
	private Map<String, Object> cols;

	public static void main(String[] args) {
		// ...
		long start = System.currentTimeMillis();
		new Generator();
		long end = System.currentTimeMillis();

		System.out.println("Builder Time: " + (end - start) + "ms");
	}

	public Generator() {
		String path = new File(this.getClass().getResource("").getPath())
				+ "\\config.json";
		// 读取配置文件，并解析成JSON
		config = JSON.parseObject(CommUtil.readFile(path));
		// 获取连接
		con = connection(config.getJSONObject("db"));

		try {
			// ...
			this.init();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭连接
			try {
				con.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @Title: init
	 * @Description: 初始化
	 * @return void
	 * @throws
	 */
	public void init() {
		Statement stm = null;
		ResultSet rs = null;
		try {
			// init
			// 获取所有需要生成的表名
			String[] tables = config.getString("tables").split(",");

			stm = con.createStatement();
			for (String table : tables) {
				this.table = table;
				this.tableName = this.initcap(table);

				// 构建sql
				String sql = "show full columns from `" + table + "`";

				// 执行sql并获取结果
				rs = stm.executeQuery(sql);

				// 存储该表的所有字段和对应字段类型
				cols = new LinkedHashMap<String, Object>();

				while (rs.next()) {
					// 字段的其他参数
					Map<String, String> params = new HashMap<String, String>();
					params.put("type", this.sqlType2JavaType(rs.getString(
							"Type").replaceAll("[(].*[)]", "")));
					params.put("note", rs.getString("Comment"));
					params.put("isPK",
							rs.getString("Key").equals("PRI") ? "true"
									: "false");

					cols.put(rs.getString("Field"), params);
				}

				// 生成表对应的相关文件，包括 Model、Dao、Mapper、Service、ServiceImpl
				this.generateModel().generateDao().generateMapper()
						.generateService().generateServiceImpl();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stm != null) {
					stm.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @Title: generateModel
	 * @Description: 生成Model
	 * @return GenModel
	 * @throws
	 */
	public Generator generateModel() {
		// 获取包名
		String pkg = config.getString("modelPackage");
		// ...
		StringBuffer sb = new StringBuffer();
		try {
			// package
			sb.append("package ");
			sb.append(pkg);
			sb.append(";\n\n");

			// extends
			sb.append("import com.cn.base.BasicModel;\n\n");

			// utils
			// ...

			// notes
			sb.append("/**\n");
			sb.append(" * @ClassName " + tableName + "\n");
			sb.append(" * @Description TODO \n");
			sb.append(" * @Author devfzm@gmail.com\n");
			sb.append(" * @Date " + new Date() + "\n");
			sb.append(" */\n\n");

			// class
			sb.append("public class ");
			sb.append(tableName);
			sb.append(" extends BasicModel {\n");
			sb.append("\t// SerialVersionUID\n");
			sb.append("\tprivate static final long serialVersionUID = ");
			sb.append(System.currentTimeMillis());
			sb.append((int) (Math.random() * 10));
			sb.append("L;\n");

			/** attrbuite **/
			// 保存所有属性字段，用于下面生成构造函数
			List<String> attrs = new ArrayList<String>();
			StringBuffer attrBuffer = new StringBuffer();
			for (Entry<String, Object> entry : cols.entrySet()) {
				JSONObject params = JSON.parseObject(JSON.toJSONString(entry
						.getValue()));
				// note
				sb.append("\t// ");
				sb.append(params.getString("note"));
				sb.append("\n");
				// attr
				sb.append("\tprivate ");
				sb.append(params.getString("type"));
				sb.append(" ");
				sb.append(entry.getKey());
				sb.append(";\n");

				// save
				attrs.add(params.getString("type") + " " + entry.getKey());
				attrBuffer.append("\t\tthis.");
				attrBuffer.append(entry.getKey());
				attrBuffer.append(" = ");
				attrBuffer.append(entry.getKey());
				attrBuffer.append(";\n");
			}

			/** constructor **/
			// default
			sb.append("\n\tpublic ");
			sb.append(tableName);
			sb.append("() { }\n\n");
			// arguments
			sb.append("\tpublic ");
			sb.append(tableName);
			sb.append("(");
			sb.append(Joiner.on(", ").join(attrs));
			sb.append(") {\n");
			sb.append("\t\tsuper();\n");
			sb.append(attrBuffer.toString());
			sb.append("\t}\n\n");

			/** getter & setter **/
			for (Entry<String, Object> entry : cols.entrySet()) {
				JSONObject params = JSON.parseObject(JSON.toJSONString(entry
						.getValue()));

				// getter
				sb.append("\tpublic ");
				sb.append(params.getString("type"));
				sb.append(" get");
				sb.append(this.initcap(entry.getKey()));
				sb.append("() {\n");
				sb.append("\t\treturn ");
				sb.append(entry.getKey());
				sb.append(";\n");
				sb.append("\t}\n\n");

				// setter
				sb.append("\tpublic void set");
				sb.append(this.initcap(entry.getKey()));
				sb.append("(");
				sb.append(params.getString("type"));
				sb.append(" ");
				sb.append(entry.getKey());
				sb.append(") {\n");
				sb.append("\t\tthis.");
				sb.append(entry.getKey());
				sb.append(" = ");
				sb.append(entry.getKey());
				sb.append(";\n");
				sb.append("\t}\n\n");
			}

			sb.append("}\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writeFile(pkg, sb.toString(), "Model");
	}

	/**
	 * @Title: generateDao
	 * @Description: 生成Dao
	 * @return GenModel
	 * @throws
	 */
	public Generator generateDao() {
		// 获取包名
		String pkg = config.getString("daoPackage");
		// ...
		StringBuffer sb = new StringBuffer();
		try {
			// package
			sb.append("package ");
			sb.append(pkg);
			sb.append(";\n\n");

			// import extends
			sb.append("import com.cn.base.BasicMapper;\n\n");

			// notes
			sb.append("/**\n");
			sb.append(" * @ClassName " + tableName + "Mapper\n");
			sb.append(" * @Description TODO \n");
			sb.append(" * @Author devfzm@gmail.com\n");
			sb.append(" * @Date " + new Date() + "\n");
			sb.append(" */\n\n");

			// interface
			sb.append("public interface ");
			sb.append(tableName);
			sb.append("Mapper extends BasicMapper {\n\n}\n\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writeFile(pkg, sb.toString(), "Dao");
	}

	/**
	 * @Title: generateMapper
	 * @Description: 生成Mapper.xml
	 * @return GenModel
	 * @throws
	 */
	public Generator generateMapper() {
		// 获取包名
		String pkg = config.getString("mapperPackage");
		// ...
		StringBuffer sb = new StringBuffer();
		try {
			// 找出主键
			String pk = null;
			for (Entry<String, Object> entry : cols.entrySet()) {
				JSONObject params = JSON.parseObject(JSON.toJSONString(entry
						.getValue()));
				// 找出主键
				if (params.getBooleanValue("isPK")) {
					pk = entry.getKey();
					// 找到之后跳出循环
					break;
				}
			}

			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
			sb.append("<mapper namespace=\"");
			sb.append(config.getString("daoPackage"));
			sb.append(".");
			sb.append(tableName);
			sb.append("Mapper\">\n");

			// cache, default useCache
			sb.append("\t<!-- use cache -->\n ");
			sb.append("\t<cache eviction=\"LRU\" type=\"");
			sb.append(config.getString("redisCache"));
			sb.append("\" />\n\n");

			// findByPrimaryKey
			sb.append("\t<!-- ============================= SELECT ============================= -->\n");
			sb.append("\t<select id=\"findByPrimaryKey\" parameterType=\"java.lang.Integer\" resultType=\"");
			sb.append(tableName);
			sb.append("\">\n");
			sb.append("\t\tSELECT * FROM `" + table + "`\n");
			sb.append("\t\tWHERE ");
			if (pk != null) {
				sb.append(pk + "=#{" + pk + "}\n");
			} else {
				// ...
			}
			sb.append("\t</select>\n\n");

			// findOne
			sb.append("\t<select id=\"findOne\" parameterType=\"");
			sb.append(tableName);
			sb.append("\" resultType=\"");
			sb.append(tableName);
			sb.append("\">\n");
			sb.append("\t\tSELECT * FROM `" + table + "` WHERE 1=1\n");
			for (Entry<String, Object> entry : cols.entrySet()) {
				sb.append("\t\t<if test=\"");
				sb.append(entry.getKey());
				sb.append(" != null\">\n");
				sb.append("\t\t\tAND ");
				sb.append(entry.getKey());
				sb.append("=#{");
				sb.append(entry.getKey());
				sb.append("}\n\t\t</if>\n");
			}
			sb.append("\t\tLIMIT 1\n");
			sb.append("\t</select>\n\n");

			// find
			sb.append("\t<select id=\"find\" parameterType=\"");
			sb.append(tableName);
			sb.append("\" resultType=\"");
			sb.append(tableName);
			sb.append("\">\n");
			sb.append("\t\tSELECT * FROM `" + table + "` WHERE 1=1\n");
			for (Entry<String, Object> entry : cols.entrySet()) {
				sb.append("\t\t<if test=\"");
				sb.append(entry.getKey());
				sb.append(" != null\">\n");
				sb.append("\t\t\tAND ");
				sb.append(entry.getKey());
				sb.append("=#{");
				sb.append(entry.getKey());
				sb.append("}\n\t\t</if>\n");
			}
			sb.append("\t</select>\n\n");

			// findAll
			sb.append("\t<select id=\"findAll\" resultType=\"");
			sb.append(tableName);
			sb.append("\">\n");
			sb.append("\t\tSELECT * FROM `" + table + "`\n");
			sb.append("\t</select>\n\n");

			// count
			sb.append("\t<select id=\"count\" resultType=\"java.lang.Long\">\n");
			sb.append("\t\tSELECT COUNT(1) FROM `" + table + "`\n");
			sb.append("\t</select>\n\n");

			// save
			sb.append("\t<!-- ============================= INSERT ============================= -->\n");
			sb.append("\t<insert id=\"save\" parameterType=\"");
			sb.append(tableName);
			sb.append("\" useGeneratedKeys=\"true\" keyProperty=\"");
			if (pk != null) {
				sb.append(pk);
			} else {
				// ...
			}
			sb.append("\">\n");
			sb.append("\t\tINSERT INTO `" + table + "` (");
			LinkedList<String> list = new LinkedList<String>();
			for (Entry<String, Object> entry : cols.entrySet()) {
				list.add(entry.getKey());
			}
			sb.append(Joiner.on(", ").join(list) + ")\n");
			sb.append("\t\tVALUES(#{");
			sb.append(Joiner.on("}, #{").join(list) + "})\n");
			sb.append("\t</insert>\n\n");

			// update
			sb.append("\t<!-- ============================= UPDATE ============================= -->\n");
			sb.append("\t<update id=\"update\" parameterType=\"");
			sb.append(tableName);
			sb.append("\" useGeneratedKeys=\"true\" keyProperty=\"");
			if (pk != null) {
				sb.append(pk);
			} else {
				// ...
			}
			sb.append("\">\n");
			sb.append("\t\tUPDATE `" + table + "`\n");
			sb.append("\t\t<set>\n");
			int len = cols.size(), i = 1;
			for (Entry<String, Object> entry : cols.entrySet()) {
				JSONObject params = JSON.parseObject(JSON.toJSONString(entry
						.getValue()));
				// 如果是主键, 跳过主键
				if (params.getBooleanValue("isPK")) {
					continue;
				}
				sb.append("\t\t\t<if test=\"");
				sb.append(entry.getKey());
				sb.append(" != null\">\n\t\t\t\t");
				sb.append(entry.getKey() + "=#{" + entry.getKey() + "}");
				if (++i != len) {
					sb.append(",");
				}
				sb.append("\n\t\t\t</if>\n");
			}
			sb.append("\t\t</set>\n");
			sb.append("\t\tWHERE ");
			if (pk != null) {
				sb.append(pk + "=#{" + pk + "}\n");
			} else {
				// ...
			}
			sb.append("\t</update>\n\n");

			// delete
			sb.append("\t<!-- ============================= DELETE ============================= -->\n");
			sb.append("\t<delete id=\"delete\">\n");
			sb.append("\t\tDELETE FROM `" + table + "`\n");
			sb.append("\t\tWHERE ");
			if (pk != null) {
				sb.append(pk + "=#{" + pk + "}\n");
			} else {
				// ...
			}
			sb.append("\t</delete>\n\n");

			sb.append("</mapper>");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writeFile(pkg, sb.toString(), "Mapper");
	}

	/**
	 * @Title: generateService
	 * @Description: 生成Service
	 * @return Generator
	 * @throws
	 */
	public Generator generateService() {
		// 获取包名
		String pkg = config.getString("servicePackage");
		// ...
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("package " + pkg + ";\n\n");
			sb.append("import com.cn.base.BasicService;\n\n");
			sb.append("public interface " + tableName
					+ "Service extends BasicService {\n\n}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writeFile(pkg, sb.toString(), "Service");
	}

	/**
	 * @Title: generateServiceImpl
	 * @Description: 生成Service实现
	 * @return Generator
	 * @throws
	 */
	public Generator generateServiceImpl() {
		// 获取包名
		String pkg = config.getString("serviceImplPackage");
		// ...
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("package " + pkg + ";\n\n");
			sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
			sb.append("import org.springframework.stereotype.Service;\n\n");

			sb.append("import com.cn.base.BasicMapper;\n");
			sb.append("import com.cn.base.BasicServiceImpl;\n");
			sb.append("import " + config.getString("servicePackage") + "."
					+ tableName + "Service;\n");
			sb.append("import " + config.getString("daoPackage") + "."
					+ tableName + "Mapper;\n\n");

			sb.append("@Service\n");
			sb.append("public class " + tableName
					+ "ServiceImpl extends BasicServiceImpl implements "
					+ tableName + "Service {\n\n");

			sb.append("\t@Autowired\n");
			sb.append("\t" + tableName + "Mapper "
					+ toLowerCaseFirstOne(tableName) + ";\n\n");

			sb.append("\t@Override\n");
			sb.append("\tpublic BasicMapper getMapper() {\n");
			sb.append("\t\treturn " + toLowerCaseFirstOne(tableName)
					+ ";\n\t}\n\n}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writeFile(pkg, sb.toString(), "ServiceImpl");
	}

	/**
	 * @Title: writeFile
	 * @Description: 写入文件
	 * @param pkg
	 *            包名
	 * @param text
	 *            写入内容
	 * @param type
	 *            文件类型
	 * @return GenModel
	 * @throws
	 */
	public Generator writeFile(String pkg, String text, String type) {
		try {
			if (Strings.isNullOrEmpty(text) || Strings.isNullOrEmpty(pkg)) {
				return this;
			}

			String temp = "";

			if ("Dao".equals(type)){
				temp = "Dao.java";
			}
			if ("Mapper".equals(type)){
				temp = "Mapper.xml";
			}
			if ("Model".equals(type)){
				temp = "Model.java";
			}
			if ("Service".equals(type)){
				temp = "Service.java";
			}
			if ("ServiceImpl".equals(type)){
				temp = "ServiceImpl.java";
			}

			if (temp.equals("")){
				throw new Exception(
						"Unknown type, Optional parameters [\"Dao\", \"Mapper\", \"Model\", \"Service\", \"ServiceImpl\"]");
			}


			// 构建绝对路径
			String path = new File("").getAbsolutePath()
					+ "\\src\\main\\java\\" + pkg.replace(".", "\\") + "\\";
			File dir = new File(path);
			// 目录检测
			if (!dir.exists()) {
				dir.mkdirs();
			}
			// 文件写入
			FileWriter fw = new FileWriter(path + tableName + type);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(text);
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * 功能：将输入字符串的首字母和"_a"格式的字母改成A
	 * 
	 * @param str
	 * @return
	 */
	public String initcap(String str) {
		String[] arr = str.split("_");
		for (int i = 0; i < arr.length; i++) {
			char[] ch = arr[i].toCharArray();
			if (ch[0] >= 'a' && ch[0] <= 'z') {
				ch[0] = (char) (ch[0] - 32);
			}
			arr[i] = new String(ch);
		}
		return Joiner.on("").join(arr);
	}

	/**
	 * @Title: toLowerCaseFirstOne
	 * @Description: 将字符串的首字母转换成小写
	 * @param str
	 * @return String
	 * @throws
	 */
	public String toLowerCaseFirstOne(String str) {
		if (!Strings.isNullOrEmpty(str)) {
			char a = str.charAt(0);
			String first = String.valueOf(a);
			if (a >= 'a' && a <= 'z') {
				return str.replaceFirst(first, first.toLowerCase());
			}
		}
		return str;
	}

	/**
	 * 功能：获得列的数据类型
	 * 
	 * @param sqlType
	 * @return javaType
	 */
	public String sqlType2JavaType(String sqlType) {
		if (sqlType.equalsIgnoreCase("bit")) {
			return "Boolean";
		} else if (sqlType.equalsIgnoreCase("tinyint")) {
			return "byte";
		} else if (sqlType.equalsIgnoreCase("smallint")) {
			return "short";
		} else if (sqlType.equalsIgnoreCase("int")) {
			return "Integer";
		} else if (sqlType.equalsIgnoreCase("bigint")) {
			return "Long";
		} else if (sqlType.equalsIgnoreCase("float")) {
			return "float";
		} else if (sqlType.equalsIgnoreCase("decimal")
				|| sqlType.equalsIgnoreCase("numeric")
				|| sqlType.equalsIgnoreCase("real")
				|| sqlType.equalsIgnoreCase("money")
				|| sqlType.equalsIgnoreCase("smallmoney")) {
			return "String";
		} else if (sqlType.equalsIgnoreCase("varchar")
				|| sqlType.equalsIgnoreCase("char")
				|| sqlType.equalsIgnoreCase("nvarchar")
				|| sqlType.equalsIgnoreCase("nchar")
				|| sqlType.equalsIgnoreCase("text")) {
			return "String";
		} else if (sqlType.equalsIgnoreCase("datetime")) {
			return "String";
		} else if (sqlType.equalsIgnoreCase("date")) {
			return "Date";
		} else if (sqlType.equalsIgnoreCase("image")) {
			return "Blod";
		}
		return null;
	}

	/**
	 * @Title: connection
	 * @Description: 连接数据库
	 * @param db
	 *            数据库连接相关信息
	 * @return Connection
	 * @throws
	 */
	public Connection connection(JSONObject db) {
		try {
			Class.forName(db.getString("driver"));
			Connection con = DriverManager.getConnection(db.getString("url"),
					db.getString("username"), db.getString("password"));
			return con;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
