package com.webmagic.pipeline;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webmagic.dao.RecClawerLogMapper;
import com.webmagic.entity.VcmClawerLogVo;
import com.webmagic.pageprocess.BoardPageProcessor;
import com.webmagic.util.JSONUtil;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class BoradPipeline implements Pipeline {
    private static Logger log = Logger.getLogger(BoradPipeline.class);
    private RecClawerLogMapper recClawerLogMapper;

    public  BoradPipeline(RecClawerLogMapper recClawerLogMapper){
    	this.recClawerLogMapper = recClawerLogMapper;
    }
    
	@Override	
	public void process(ResultItems resultItems, Task task) {
		if (resultItems.getAll() == null || resultItems.getAll().size() == 0) {
			return;
		}
		
		//int 
		Map<String, List<String>> data = resultItems.get("data");
		List<String> videoName = data.get(BoardPageProcessor.videoName);//资源名称
		List<String> videoRanking = data.get(BoardPageProcessor.videoRanking);//排名
		List<String> videoRate = data.get(BoardPageProcessor.videoRate);//评分/指数
		List<String> videoReleaseDates = data.get(BoardPageProcessor.videoReleaseDates);//上映时间
		List<String> videoCast = data.get(BoardPageProcessor.videoCast);//主演
		List<String> videoBoxOffice = data.get(BoardPageProcessor.videoBoxOffice);//票房
		List<String> contentType = data.get(BoardPageProcessor.contentType);//票房
		int relateType = resultItems.get("ID");

		log.info("pipeLine资源名称=" + videoName);
		log.info("pipeLine上映时间=" + videoReleaseDates);
		log.info("pipeLine评分/指数=" + videoRate);
		log.info("pipeLine排名=" + videoRanking);
		log.info("pipeLine主演=" + videoCast);
		log.info("pipeLine票房=" + videoBoxOffice);
		log.info("pipeLine类型=" + contentType);
		//List<VcmClawerLogVo> vcmList = new ArrayList<>();
		VcmClawerLogVo vcmLog = new VcmClawerLogVo();
		Map<String, Object> responeContent = null;
		for (int index=0; index<videoName.size(); index++) {
			responeContent = new HashMap<>();
			if (videoName != null) {//资源名称
				responeContent.put(BoardPageProcessor.videoName, videoName.get(index));
			}
			if (videoRanking != null) {//排名
				responeContent.put(BoardPageProcessor.videoRanking, Integer.parseInt(videoRanking.get(index)));
			}
			if (videoRate != null) {//评分/指数
				responeContent.put(BoardPageProcessor.videoRate, Double.parseDouble(videoRate.get(index)));
			}
			if (videoReleaseDates != null) {//上映时间
				responeContent.put(BoardPageProcessor.videoReleaseDates, videoReleaseDates.get(index));
			}
			if (videoCast != null) {//主演
				responeContent.put(BoardPageProcessor.videoCast, videoCast.get(index));
			}
			if (videoBoxOffice != null) {//票房万
				responeContent.put(BoardPageProcessor.videoBoxOffice, Integer.parseInt(videoBoxOffice.get(index)));
			}
			if (contentType != null) {//类型
				responeContent.put(BoardPageProcessor.contentType, Integer.parseInt(contentType.get(index)));
			}

			//入库
			vcmLog.setId(0);
			vcmLog.setExecDate(new Date(System.currentTimeMillis()));
			vcmLog.setStatus(0);
			vcmLog.setResponeContext(JSONUtil.toJson(responeContent));
			vcmLog.setRelateType(relateType);
			//vcmList.add(vcmLog); 
			recClawerLogMapper.insertRecord(vcmLog);
		}
		//recClawerLogMapper.insertRecdBatch(vcmList);
	}

}
