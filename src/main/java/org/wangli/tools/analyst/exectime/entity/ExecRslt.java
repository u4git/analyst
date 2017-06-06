package org.wangli.tools.analyst.exectime.entity;

import java.util.LinkedList;
import java.util.List;

public class ExecRslt {

	// 所有执行次的执行时间
	private List<TimeRslt> timeRslts = new LinkedList<TimeRslt>();
	// 执行次结果的游标
	private int timeRsltIndex = 0;

	// 执行了多少次
	private int totalTimes = 0;

	// 出错了多少次
	private int errorTimes = 0;

	/**
	 * 添加次执行结果
	 * 
	 * @param timeRslt
	 */
	public void addTimeRslt(TimeRslt timeRslt) {
		if (timeRslt != null) {
			this.timeRslts.add(timeRslt);
			this.totalTimes++;
		}
	}

	/**
	 * 增加出错次数
	 * 
	 * @param errorTimes
	 */
	public void addErrorTimes(int errorTimes) {
		this.errorTimes = this.errorTimes + errorTimes;
		this.totalTimes = this.totalTimes + errorTimes;
	}

	/**
	 * 获取执行时间
	 * 
	 * @return
	 */
	public double getAvgExecTime() {
		double execTime = 0;
		if (this.timeRslts.size() > 0) {
			for (TimeRslt timeRslt : this.timeRslts) {
				execTime = execTime + timeRslt.getCostTime();
			}
			execTime = execTime / this.timeRslts.size();
		}
		return execTime;
	}

	/**
	 * 重置次执行结果的游标
	 */
	public void resetTimeRsltIndex() {
		this.timeRsltIndex = 0;
	}

	/**
	 * 是否还有次执行结果
	 * 
	 * @return
	 */
	public boolean hasNextTimeRslt() {
		return this.timeRsltIndex < this.timeRslts.size();
	}

	/**
	 * 下一个次执行结果的执行时间
	 * 
	 * @return
	 */
	public TimeRslt nextTimeRslt() {
		return this.timeRslts.get(this.timeRsltIndex++);
	}

	/**
	 * 获取正确次数
	 * 
	 * @return
	 */
	public int getCorrectTimes() {
		return this.timeRslts.size();
	}

	public int getTotalTimes() {
		return totalTimes;
	}

	public int getErrorTimes() {
		return errorTimes;
	}

}
