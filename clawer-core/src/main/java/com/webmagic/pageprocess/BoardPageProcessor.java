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
	public static final String rankingId = "rankingId";//榜单id --批次号
	
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
		if (page.getUrl().toString().equals(MAOYAN_URL)|| page.getUrl().toString().startsWith(CBOOO_MOVIE_URL)) {
			getMaoyanHot(page);
		} else if (page.getUrl().toString().equals(BAIDU_TV_URL)) {
			getBaiduHot(page, BAIDU_TV_ID);
		} else if (page.getUrl().toString().equals(BAIDU_MOVIE_URL)) {
			getBaiduHot(page, BAIDU_MOVIE_ID);
		} else {
			log.info("url["+page.getUrl()+"]，不处理..");
		}
	}

	@Override
	public Site getSite() {
		return site;
	}


	
	public void getBaiduHot(Page page, int id) {

		log.info("百度风云榜，url=" + page.getUrl());
		// 得到排名
		List<String> videoRankiList = page.getHtml().xpath(getXpath(videoRanking).toString()).all();
		// 得到搜索指
		List<String> videoRateList = page.getHtml().xpath(getXpath(videoRate)).all();
		// 得到媒资名称
		List<String> videoNameList = page.getHtml().xpath(getXpath(videoName)).all();
		Map<String, Object> data = new HashMap<>();
		data.put(videoRanking, videoRankiList);//排名
		data.put(videoRate, videoRateList);//搜索指数
		data.put(videoName, videoNameList);//媒资名称
		data.put(contentType,Collections.nCopies(videoNameList.size(), id+""));//资源类别(1.电影,2.电视剧)
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
			List<String> videoRankiListTmp = page.getHtml().xpath(getXpath(videoRanking)).all();
			
			// 电影名称
			List<String> videoNameList = page.getHtml().xpath(getXpath(videoName)).all();

			// 上映时间
			List<String> videoReleaseListtmp = page.getHtml().xpath(getXpath(videoReleaseDates)).all();
			List<String> videoReleaseList = new ArrayList<String>();
			videoReleaseListtmp.stream().forEach(release->videoReleaseList.add(release.replace("上映时间：", "")));

			
			// 主演名单
			List<String> videoCastListtmp = page.getHtml().xpath(getXpath(videoCast)).all();
			List<String> videoCastList = new ArrayList<String>();
			videoCastListtmp.forEach(cast->videoCastList.add(cast.replaceAll(" 主演：", "")));

			
			// 评分分前后
			List<String> videoRateStartList = page.getHtml().xpath(getXpath("videoRateStart")).all();
			List<String> videoRateEndList = page.getHtml().xpath(getXpath("videoRateEnd")).all();
			List<String> videoRateList = new ArrayList<>();
			if (videoRateStartList != null && videoRateEndList != null) {
				for (int i = 0; i < videoReleaseList.size(); i++) {
					videoRateList.add(videoRateStartList.get(i) + videoRateEndList.get(i));
				}
			}

			// 票房
			targetUrlCnt.set(videoNameList.size());
			String cboooPrefix = ruleJson.get("cboooPrefix").toString();
			videoNameList.stream().forEach((String movieName)->{ page.addTargetRequest(cboooPrefix + movieName);});

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

			log.info("中国票房网，url=" + page.getUrl() );
			List<String> pf =page.getHtml().xpath(getXpath(videoBoxOffice)).all();
			pf.stream().forEach((String info) -> {
				if (info.indexOf(DateUtil.getCurrYear()) != -1 && info.indexOf("万") != -1) {
					String[] infos=info.split("  ");
					//都获取到票房后
					List<String> boxs = (List)data.get(videoBoxOffice);
					if (boxs == null) {
						boxs = new ArrayList<String>();
					} 
					boxs.add(infos[infos.length-1].replace("万", ""));
					data.put(videoBoxOffice, boxs);
					if(targetUrlCnt.decrementAndGet() == 0) {
						page.putField("data", data);
						page.putField("ID", MAOYAN_ID);
					}
				}
			});
		}
		
	}

	
	public String getXpath(String attr) {
		return ruleJson.get(attr + "XPath").toString();
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
