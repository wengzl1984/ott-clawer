package com.webmagic.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.webmagic.dao.RecClawerLogMapper;
import com.webmagic.dao.VcmClawerTaskDao;
import com.webmagic.entity.VcmClawerTaskVo;
import com.webmagic.pageprocess.BoardPageProcessor;
import com.webmagic.pipeline.BoradPipeline;
import com.webmagic.util.DateUtil;
import com.webmagic.util.JSONUtil;

import us.codecraft.webmagic.Spider;

@Configurable
@Component
@EnableScheduling
@Order(10000)
public class ScheduleTaskConfig implements SchedulingConfigurer {

	private Logger log = LoggerFactory.getLogger(ScheduleTaskConfig.class);

	// 任务编号
	private AtomicInteger atomicTaskId = new AtomicInteger(0);

	// 任务缓存
	private static Map<String, BaseTask> tasks = new HashMap<>();

	// 定时扫描oracle的时间
	private static final String cron = "*/10 * * * * ?"; // 调用set方法可动态设置时间规则
	private static final String cron1 = "*/60 * * * * ?"; // 调用set方法可动态设置时间规则


	// 状态
	public static enum STATUS {
		TASK_NOT_EXISTS, TASK_EXISTS, FAILURE, SUCCESS;
	}
	
	//
	private static final int[] TASKID = {1,2,3};

	private final static String TASK_PREFIX = "clawer";// 爬虫榜单-任务编号

	private ScheduledTaskRegistrar scheduledTaskRegistrar;

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		this.scheduledTaskRegistrar = scheduledTaskRegistrar;
		initTask();
	}
	
	@Autowired // 注入mapper
	private VcmClawerTaskDao vcmClawerTaskDao;
	
	
    @Autowired // 注入mapper
    private RecClawerLogMapper recClawerLogMapper;
	
	/**
	 * 定时扫描oracle任务
	 */
	private void initTask() {

		String taskId = TASK_PREFIX + atomicTaskId.incrementAndGet();
		BaseTask baseCronTask = new BaseTask(taskId, cron) {
		
			@Override
			public void run() {
				try {
					log.info("定时["+cron+"] ,获取榜单爬取任务中...");
					List<VcmClawerTaskVo> resultMap = vcmClawerTaskDao.findAll(TASKID);
					if (resultMap == null || resultMap.size() == 0) {
						System.out.println("未查询到相关记录..");
						return;
					}
					
					for (VcmClawerTaskVo vcmClawerTask : resultMap) {
						
						String taskId = String.valueOf(vcmClawerTask.getId());
						BaseTask oldTask = TaskConfig.getTask(taskId);
						String expression = getTaskCronExpression(vcmClawerTask.getReptileDate(), vcmClawerTask.getFrequencyNum());
		
						if (TaskConfig.containsTask(taskId)) {
							// 任务时间是否有调整，有调整的话获取新的cron表达式
							//if (oldTask.getExpression().equals(vcmClawerTask.get))
							if (!expression.equals(oldTask.getExpression())) {
								log.info("任务旧cron表达式[" + oldTask.getExpression() + "]新表达式["+expression+
										"],更新任务执行频率...");
								//TODO 更新任务
								//changeTask(taskId, expression);
							} else {
								log.info("定时爬取任务URL[" + vcmClawerTask.getReptileUrl() + "],执行频率[" +expression+"],不需要更新爬取计划.");
							}
						} else {
							if(new Date().compareTo(vcmClawerTask.getStartDate()) > 0) {
								//任务开始时间小于当前系统时间，任务开始
								log.info("获取到爬取任务，URL[" + vcmClawerTask.getReptileUrl() + "],起始时间["+DateUtil.formatDate(vcmClawerTask.getStartDate())+
										"],执行频率[" +expression+"]");
								
								// TODO 任务时间随便写个
								BaseTask clawerTask = new BaseTask(String.valueOf(vcmClawerTask.getId()), cron1) {

									@Override
									public void run() {
									
										Map<String, Object> ruleJson = JSONUtil.json2Map(vcmClawerTask.getRuleJson());
										BoardPageProcessor processor = new BoardPageProcessor(ruleJson);
										Spider.create(processor)
				                        .addUrl(vcmClawerTask.getReptileUrl())
				                        .addPipeline(new BoradPipeline(recClawerLogMapper))
				                        .thread(Integer.parseInt(ruleJson.get("thread").toString()))
				                        .run();
									} 
								};
								addTask(clawerTask);
								
							} else {
								//任务开始时间，
								log.info("扫描URL[" + vcmClawerTask.getReptileUrl() + "]开始时间["+DateUtil.formatDate(vcmClawerTask.getStartDate())+
										"],大于当前系统时间,任务未开始...");
								return ;
							}
						} 	
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		addTask(baseCronTask);
	

	}

	//获取cron表达式
	public String getTaskCronExpression(String reptileDate, int frequencyNum) {		
		String[] dateInfo = reptileDate.split(":");
		StringBuffer BufferCron = new StringBuffer();
		BufferCron.append("0")
		.append(" ")
		.append(Integer.parseInt(dateInfo[1]))
		.append(" ")
		.append(Integer.parseInt(dateInfo[0]))
		.append(" ")
		.append("0/"+frequencyNum)
		.append(" * ?");
		return BufferCron.toString();
	}
	
	/**
	 * 添加任务
	 * 
	 * @param task
	 * @return
	 */
	public STATUS addTask(BaseTask task) {
		if (scheduledTaskRegistrar == null || task == null) {
			return STATUS.FAILURE;
		}
		if (TaskConfig.containsTask(task.getId())) {
			return STATUS.TASK_EXISTS;
		}
		try {
			addTask0(task);
			TaskConfig.addTask(task);
			return STATUS.SUCCESS;
		} catch (Exception e) {
			log.error("新增定时任务失败:" + task, e);
			throw e;
		}
	}

	/**
	 * 改变任务执行频率
	 * 
	 * @param taskId
	 * @param expression
	 * @return
	 */
	public STATUS changeTask(String taskId, String expression) {
		BaseTask baseTask = TaskConfig.getTask(taskId);
		if (baseTask == null || expression == null) {
			return STATUS.TASK_NOT_EXISTS;
		}
		log.info("change trigger expression:(id=" + taskId + ",expression=" + expression + ")");
		baseTask.setExpression(expression);
		return STATUS.SUCCESS;
	}

	/**
	 * 取消定时任务
	 * 
	 * @param taskId
	 * @return
	 */
	public STATUS cancelTask(String taskId) {
		if (!TaskConfig.containsTask(taskId)) {
			return STATUS.TASK_NOT_EXISTS;
		}
		try {
			log.info("cancel task:" + taskId);
			TaskConfig.removeTask(taskId).getScheduledTask().cancel();
		} catch (Exception e) {
			log.error("取消任务失败:" + taskId, e);
			throw e;
		}
		return STATUS.SUCCESS;
	}

	private void addTask0(BaseTask task) {
		log.info("add task:" + task);
		task.setScheduledTask(addTriggerTask(task));

	}

	/**
	 * 添加可变时间task
	 * 
	 * @param task
	 * @return
	 */
	private ScheduledTask addTriggerTask(BaseTask task) {
		return scheduledTaskRegistrar.scheduleTriggerTask(new TriggerTask(task, triggerContext -> {
			CronTrigger trigger = new CronTrigger(task.getExpression());
			Date nextExec = trigger.nextExecutionTime(triggerContext);
			return nextExec;
		}));
	}

	/**
	 * 设置固定频率的定时任务
	 * 
	 * @param task
	 * @param interval
	 */
	private ScheduledTask addFixedRateTask(Runnable task, long interval) {
		return scheduledTaskRegistrar.scheduleFixedRateTask(new IntervalTask(task, interval, 0L));
	}

	/**
	 * 设置延迟以固定频率执行的定时任务
	 * 
	 * @param task
	 * @param interval
	 * @param delay
	 */
	private ScheduledTask addFixedDelayTask(Runnable task, long interval, long delay) {
		return scheduledTaskRegistrar.scheduleFixedDelayTask(new IntervalTask(task, interval, delay));
	}

	/**
	 * 添加不可改变时间表的定时任务
	 * 
	 * @param task
	 */
	private ScheduledTask addCronTask(Runnable task, String expression) {
		return scheduledTaskRegistrar.scheduleCronTask(new CronTask(task, expression));
	}

}
