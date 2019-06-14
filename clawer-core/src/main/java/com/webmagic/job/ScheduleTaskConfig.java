package com.webmagic.job;

import java.util.Date;
import java.util.HashMap;
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

import com.webmagic.dao.VcmClawerTaskDao;

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
	private static final String cron = "0/30 * * * * ?"; // 调用set方法可动态设置时间规则
	private static final String cron1 = "0/10 * * * * ?"; // 调用set方法可动态设置时间规则

	// 状态
	public static enum STATUS {
		TASK_NOT_EXISTS, TASK_EXISTS, FAILURE, SUCCESS;
	}

	private final static String TASK_PREFIX = "clawer";// 爬虫榜单-任务编号

	private ScheduledTaskRegistrar scheduledTaskRegistrar;

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		this.scheduledTaskRegistrar = scheduledTaskRegistrar;
		initTask();
	}

	@Autowired // 注入mapper
	@SuppressWarnings("all")
	VcmClawerTaskDao vcmClawerTaskDao;

	/**
	 * 定时扫描oracle任务
	 */
	private void initTask() {
		// TaskConfig.getTasks().forEach(task -> addTask0(task));
		String taskId = TASK_PREFIX + atomicTaskId.incrementAndGet();
		BaseTask baseCronTask = new BaseTask(taskId, cron) {

			@Override
			public void run() {
				try {
					System.out.println(Thread.currentThread().getId() + ":" + this.getExpression());
					changeTask(taskId, cron1);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		};
		addTask(baseCronTask);
		
		if (vcmClawerTaskDao == null) {
			System.out.println("cronMapper == null");
		}
		Map<String, Object> resultMap = new HashMap<>();
		String a = vcmClawerTaskDao.selectByPrimary(1);

		
		// mybatis全局配置文件
//        String resource = "SqlMapConfig.xml";
//
//        // 根据mybatis的全局配置文件构造 一个流
//        InputStream inputStream;
//		try {
//			inputStream = Resources.getResourceAsStream(resource);
//			
//	        // 创建SqlSessionFactory
//	        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
//	        SqlSession sqlSession = sqlSessionFactory.openSession();
//	     // 获取UserDao的代理对象
//	        CronMapper cronMapper = sqlSession.getMapper(CronMapper.class);
//	        Map<String, Object> cronMap = cronMapper.selectByPrimaryKey(1);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
