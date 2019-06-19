package com.webmagic.pageprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webmagic.util.DateUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class BoardPageProcessor implements PageProcessor {

	private Logger log = LoggerFactory.getLogger(BoardPageProcessor.class);
	//private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	public static final String MAOYAN_URL = "https://maoyan.com/board/7";
	public static final String CBOOO_URL = "http://cbooo.cn/";
	public static final String CBOOO_MOVIE_URL= "http://www.cbooo.cn/search?k=";
	public static final String BAIDU_TV_URL = "http://top.baidu.com/buzz?b=4&c=2&fr=topcategory_c2";
	public static final String BAIDU_MOVIE_URL = "http://top.baidu.com/buzz?b=26&c=1&fr=topcategory_c1";
	//{"videoName":"赤壁","contentType":1,"videoRanking":10,"videoRate":7.6,"videoReleaseDates":"2005-11-10","videoCast":"刘德华","videoBoxOffice":1000}

	public static final String videoBoxOffice= "videoBoxOffice";//票房(万元)
	public static final String videoName = "videoName";//资源名称
	public static final String contentType = "contentType";//资源类别(1.电影,2.电视剧)
	public static final String videoRanking = "videoRanking";//排名
	public static final String videoRate = "videoRate";//评分/指数
	public static final String videoReleaseDates = "videoReleaseDates";//上映时间
	public static final String videoCast = "videoCast";//主演
	
	public static final int  MAOYAN_ID = 3;
	public static final int  BAIDU_TV_ID = 2;
	public static final int  BAIDU_MOVIE_ID = 1;

	private Site site;
	private Map<String, Object> ruleJson;
	//private List<String> targetUrlMap;
	private Map<String, Object> data;
	private AtomicInteger targetUrlCnt ;

	public BoardPageProcessor(Map<String, Object> ruleJson) {
		this.ruleJson = ruleJson;
		targetUrlCnt = new  AtomicInteger(0);
		//targetUrlMap = new ArrayList<>();
		data = new HashMap<>();
		site = Site.me().setCharset(ruleJson.get("charset").toString())
				.setTimeOut(Integer.parseInt(ruleJson.get("timeout").toString()))
				.setRetryTimes(Integer.parseInt(ruleJson.get("retry").toString()))
				// .setUserAgent(UserAgentUtil.getRandomUserAgent())
				.setUserAgent(ruleJson.get("userAgent").toString())
				.setSleepTime(Integer.parseInt(ruleJson.get("retry").toString()));
	}

	@Override
	public void process(Page page) {
		if (page.getUrl().regex(MAOYAN_URL).match() || page.getUrl().toString().startsWith(CBOOO_MOVIE_URL)) {
			getMaoyanHot(page);
		} else if (page.getUrl().regex(BAIDU_TV_URL).match()) {
			getBaiduHot(page, BAIDU_TV_ID);
		} else if (page.getUrl().regex(BAIDU_MOVIE_URL).match()) {
			getBaiduHot(page, BAIDU_MOVIE_ID);
		} else {
			System.out.println("url["+page.getUrl()+"]，不处理..");
		}
	}

	@Override
	public Site getSite() {
		return site;
	}

//	public void getBaiduMoive(Page page) {
//		
//		log.info("百度电影榜单，url=" + page.getUrl());
//		// 得到排名
//		List<String> videoRankiList = page.getHtml()
//				.xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[1]/span/text()").all();
//		// 得到搜索指
//		List<String> videoRateList = page.getHtml()
//				.xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[4]/span/text()").all();
//		// 得到媒资名称
//		List<String> videoNameList = page.getHtml()
//				.xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[2]/a[1]/text()").all();
//		
//		Map<String, Object> data = new HashMap<>();
//		data.put(videoRanking, videoRankiList);//排名
//		data.put(videoRate, videoRateList);//搜索指数
//		data.put(videoName, videoNameList);//媒资名称
//
//		page.putField("data", data);
//		page.putField("ID", 1);
//		page.putField("STATUS", 0);
//		page.putField("EXEC_DATE", DateUtil.getCurrDate());
//		log.info("videoRankiList=" + videoRankiList);
//		log.info("videoRateList=" + videoRateList);
//		log.info("videoNameList=" + videoNameList);
//	} 

	public void getBaiduHot(Page page, int id) {
		// 得到排名
		log.info("百度电视榜单，url=" + page.getUrl());

		List<String> videoRankiList = page.getHtml()
				.xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[1]/span/text()").all();
		// 得到搜索指
		List<String> videoRateList = page.getHtml()
				.xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[4]/span/text()").all();
		// 得到媒资名称
		List<String> videoNameList = page.getHtml()
				.xpath("//*[@id=\"main\"]/div[2]/div/table/tbody/*/td[2]/a[1]/text()").all();
		
		Map<String, Object> data = new HashMap<>();
		data.put(videoRanking, videoRankiList);//排名
		data.put(videoRate, videoRateList);//搜索指数
		data.put(videoName, videoNameList);//媒资名称
		page.putField("data", data);
		page.putField("ID", id);
		log.info("videoRankiList=" + videoRankiList);
		log.info("videoRateList=" + videoRateList);
		log.info("videoNameList=" + videoNameList);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getMaoyanHot(Page page) {
		
		if(page.getUrl().regex(MAOYAN_URL).match()) {
			targetUrlCnt.set(0);
			data.clear();
			log.info("猫眼热映电影榜单，url=" + page.getUrl());
			// 排名
			List<String> videoRankiListTmp = page.getHtml().xpath("//*[@id=\"app\"]/div/div/div/dl/*/i/text()").all();
			
			// 电影名称
			List<String> videoNameList = page.getHtml()
					.xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[1]/a/text()").all();

			// 上映时间
			List<String> videoReleaseListtmp = page.getHtml()
					.xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[3]/text()").all();
			List<String> videoReleaseList = new ArrayList<String>();
			videoReleaseListtmp.stream().forEach(release->videoReleaseList.add(release.replace("上映时间：", "")));

			
			// 主演名单
			List<String> videoCastListtmp = page.getHtml()
					.xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[1]/p[2]/text()").all();
			List<String> videoCastList = new ArrayList<String>();
			videoCastListtmp.forEach(cast->videoCastList.add(cast.replaceAll(" 主演：", "")));

			
			// 评分分前后
			List<String> videoRateStartList = page.getHtml()
					.xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[2]/p/i[1]/text()").all();
			List<String> videoRateEndList = page.getHtml()
					.xpath("//*[@id=\"app\"]/div/div/div/dl/*/div/div/div[2]/p/i[2]/text()").all();
			List<String> videoRateList = new ArrayList<>();
			if (videoRateStartList != null && videoRateEndList != null) {
				for (int i = 0; i < videoReleaseList.size(); i++) {
					videoRateList.add(videoRateStartList.get(i) + videoRateEndList.get(i));
				}
			}

			// 票房
			targetUrlCnt.set(videoNameList.size());
			videoNameList.stream().forEach((String movieName)->{ page.addTargetRequest("http://www.cbooo.cn/search?k="+movieName);});

			//缓存数据
			data.put(videoName, videoNameList);//资源名称
			data.put(contentType,Collections.nCopies(videoNameList.size(), "1"));//资源类别(1.电影,2.电视剧)
			data.put(videoRanking, videoRankiListTmp);//排名
			data.put(videoRate, videoRateList);//评分/指数
			data.put(videoReleaseDates, videoReleaseList);//上映时间
			data.put(videoCast, videoCastList);//主演

			log.info("资源名称=" + videoNameList);
			log.info("上映时间=" + videoReleaseList);
			log.info("评分/指数=" + videoRateList);
			log.info("排名=" + videoRankiListTmp);
			log.info("主演=" + videoCastList);
			//log.info("data.size=" + data.size());
			
		} else {

			log.info("中国票房网，url=" + page.getUrl() + ",a=" + data.size());
			List<String> pf =page.getHtml().xpath("//*[@id=\"top\"]/div[3]/div[2]/div[2]/ul[1]/li/span/text()").all();
			pf.stream().forEach((String info) -> {
				if (info.indexOf(DateUtil.getCurrYear()) != -1 && info.indexOf("万") != -1) {
					String[] infos=info.split("  ");
					//都获取到票房后
					List<String> boxs = (List)data.get(videoBoxOffice);
					if (boxs == null) {
						boxs = new ArrayList<String>();
					} 
					boxs.add(infos[infos.length-1]);
					data.put(videoBoxOffice, boxs);
					if(targetUrlCnt.decrementAndGet() == 0) {
						page.putField("data", data);
						page.putField("ID", MAOYAN_ID);
					}
				}
			});
		}
		
	}

	
	public Map<String, Object> getRuleJson() {
		return ruleJson;
	}

	public void setRuleJson(Map<String, Object> ruleJson) {
		this.ruleJson = ruleJson;
	}

	public void setSite(Site site) {
		this.site = site;
	}

}
