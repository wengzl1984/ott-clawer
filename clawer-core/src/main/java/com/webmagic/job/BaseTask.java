package com.webmagic.job;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.config.ScheduledTask;

/**
 * 基本的任务配置类
 */
public abstract class BaseTask implements Runnable,InitializingBean {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private ScheduledTask scheduledTask;
    @Override
	public String toString() {
		return "BaseTask [id=" + id + ", expression=" + expression + "]";
	}

	private final String id;
	private String  expression;

    public BaseTask(String taskId, String expression) {
    	this.expression = expression;
        this.id = taskId;
    }
    
    /**
     *  获取任务表达式如：0 0 0/1 * * *? (每个整点执行)
     * @return
     */
    public String getExpression() {
		return expression;
	}
    //public abstract String getExpression();


    /**
     * 设置任务表达式
     * @param expression
     */
	public void setExpression(String expression) {
		this.expression = expression;
	}
    //public abstract void setExpression(String expression);

    /**
     * 固定频率执行的时间间隔
     * @return
     */
    //public abstract long interval();

    /**
     * 固定频率执行的延迟时间
     * @return
     */
    //public abstract long delay();

    /**
     * 获取任务唯一标识
     * @return
     */
    public String getId() {
        return id;
    }

    public final ScheduledTask getScheduledTask() {
        return scheduledTask;
    }

    public final void setScheduledTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @Override
    public void afterPropertiesSet() {
        TaskConfig.addTask(this);
    }
}
