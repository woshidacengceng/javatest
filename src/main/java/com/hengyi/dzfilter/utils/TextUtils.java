package com.hengyi.dzfilter.utils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hengyi.dzfilter.config.Config;
import com.hengyi.dzfilter.database.FilterDao;
import com.hengyi.dzfilter.wordfilter.WordFilter;

/**
 * 过滤辅助类
 * @author Fanhua
 *
 */
public class TextUtils {
	
	/**
	 * 过滤
	 * @param key
	 * @return
	 */
	public static String delSqlSymbol(String key) {
		String regEx="(?i)(select|insert|update|delete)"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("").trim();
	}
	
	
	/**
	 * 删除所有符号  包括中文和英文
	 * @param key
	 * @return
	 */
	public static String delAllSymbol(String key){
		String regEx="[~！@#￥%……&*（）——+|{}：。”“？》《~!@#$%^&*()_+|{}:\"<>?.]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("").trim();
	}

	/**
	 * 过滤中文所有符号
	 * @param key
	 * @return
	 */
	public static String delChineseSymbol(String key){
		String regEx="[~！@#￥%……&*（）——+|{}：。”“？》《]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("").trim();
	}
	
	/**
	 * 过滤英文符号
	 * @param key
	 * @return
	 */
	public static String delEnglishSymbol(String key){
		String regEx="[~!@#$%^&*()_+|{}:\\\"<>?.]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("").trim();
	}
	
	/**
	 * 过滤字母
	 * @param key
	 * @return
	 */
	public static String delWordChar(String key){
		String regEx="[A-Za-z]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("").trim();
	}
	
	
	/**
	 * 过滤中文汉字
	 * @param key
	 * @return
	 */
	public static String delChineseChar(String key){
		String regEx="[\\u4e00-\\u9fa5]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("").trim();
	}
	
	public static String delNumberChar(String key) {
		String regEx="[0-9]"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("");
	}
	
	public static String delSpaceAndLineTag(String key) {
		String regEx="(\\r\\n|\\n|\\t|\\s*)"; 
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(key);
		return m.replaceAll("").trim();
	}
	
	
	/**
	 * 过滤所有标签
	 * @param htmlStr
	 * @return
	 */
	public static String delHtmlTag(String htmlStr){ 
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式 
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式 
        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式 
        String regEx_line = "(\\r\\n|\\n|\\t|\\s*)";
         
        Pattern p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
        Matcher m_script = p_script.matcher(htmlStr); 
        htmlStr = m_script.replaceAll(""); //过滤script标签 
         
        Pattern p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE); 
        Matcher m_style = p_style.matcher(htmlStr); 
        htmlStr = m_style.replaceAll(""); //过滤style标签 
         
        Pattern p_html = Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
        Matcher m_html = p_html.matcher(htmlStr); 
        htmlStr = m_html.replaceAll(""); //过滤html标签 

        
        Pattern p_line = Pattern.compile(regEx_line,Pattern.CASE_INSENSITIVE); 
        Matcher m_line = p_line.matcher(htmlStr); 
        htmlStr = m_line.replaceAll(""); //过滤html标签 
        return htmlStr.trim(); //返回文本字符串
    }

	
	/**
	 * 过滤Js标签
	 * @param htmlStr
	 * @return
	 */
	public static String delJsTag(String htmlStr){ 
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式 
         
        Pattern p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
        Matcher m_script = p_script.matcher(htmlStr); 
        htmlStr = m_script.replaceAll(""); //过滤script标签 
        return htmlStr.trim(); //返回文本字符串 
    }
	
	/**
	 * 过滤敏感词
	 * @param content
	 * @return
	 */
	public static String filter(String content) {
		return WordFilter.doFilter(content);
	}
	
	public static boolean existFilter(String content) {
		boolean res = WordFilter.isContains(content);
		return res;
	}
	
	/**
	 * 添加敏感词
	 * @param keywords
	 * @return
	 */
	public static int addFilter(String keywords) {
		return addFilter(keywords,true);
	}
	
	/**
	 * 
	 * @param keywords
	 * @param sync	是否同步命令道其他集群
	 * @return
	 */
	public static int addFilter(String keywords,boolean sync) {
		int result_id =  FilterDao.getInstance().addFilter2(keywords);
		if(result_id > 0 && sync) {
			if(PropertiesUtils.getBooleanValue("dzfilter.cluster.open")) {
				ActivemqUtils.SendObjectMessage(result_id,Config.CMD_ADD,PropertiesUtils.getValue("dzfilter.cluster.server_id"),keywords);
			}
		}
		sync();
		return result_id;
	}
	
	/**
	 * 删除敏感词
	 * @param keywords
	 * @return
	 */
	public static int delFilter(String keywords) {
		return delFilter(keywords,true);
	}
	
	
	/**
	 * 
	 * @param keywords
	 * @param sync		是否同步命令道其他集群
	 * @return
	 */
	public static int delFilter(String keywords,boolean sync) {
		int result_id =  FilterDao.getInstance().delFilter2(keywords);
		if(result_id  > 0 && sync) {
			if(PropertiesUtils.getBooleanValue("dzfilter.cluster.open")) {
				ActivemqUtils.SendObjectMessage(result_id,Config.CMD_DELETE,PropertiesUtils.getValue("dzfilter.cluster.server_id"),keywords);
			}
		}
		sync();
		return result_id;
	}
	
	/**
	 * 分页获取敏感词
	 * @param page
	 * @param num
	 * @return
	 */
	public static List<Map<String,Object>> getDataPage(int page,int num){
		return FilterDao.getInstance().getFilterKeywords(page, num);
	}
	
	/**
	 * 分页获取敏感词
	 * @param page
	 * @param num
	 * @return
	 */
	public static List<Map<String,Object>> getDataOffset(int offset,int limit){
		return FilterDao.getInstance().getFilterKeywords2(offset, limit);
	}
	
	
	/***
	 * 获取敏感词总数
	 * @return
	 */
	public static int getFilterDataTotal() {
		return FilterDao.getInstance().getFilterCount();
	}
	
	/**
	 * 同步关键词  适用于分布式同步  默认不同步
	 */
	public static void sync() {
		sync(false);
	}
	
	/**
	 * 只有mysql才能使用强一致性
	 * @param sync	是否同步命令道其他集群
	 */
	public static void sync(boolean sync) {
		WordFilter.resetInit();
		if(sync) {
			if(PropertiesUtils.getBooleanValue("dzfilter.cluster.open") && PropertiesUtils.getBooleanValue("dzfilter.db.is_mysql")) {
				ActivemqUtils.SendObjectMessage(0,Config.CMD_SYNC,PropertiesUtils.getValue("dzfilter.cluster.server_id"),"SYNC");
			}
		}
	}
}
