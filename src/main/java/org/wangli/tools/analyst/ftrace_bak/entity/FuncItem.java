package org.wangli.tools.analyst.ftrace_bak.entity;

public class FuncItem {

	// 函数名称
	private String funcName;

	// 执行次数
	private double hit = 0;
	private double hitSum = 0;

	// 执行时间
	private double time = 0;
	private double timeSum = 0;

	// 平均执行时间
	private double avg = 0;
	private double avgSum = 0;

	// 执行时间标准差
	private double s2 = 0;
	private double s2Sum = 0;

	// 相同函数命出现的次数
	private int num = 1;

	/**
	 * 合并一个FuncItem2对象到当前对象
	 * 
	 * @param funcItem
	 */
	public void merge(FuncItem funcItem) {
		this.hitSum = this.hitSum + funcItem.getHit();
		this.timeSum = this.timeSum + funcItem.getTime();
		this.avgSum = this.avgSum + funcItem.getAvg();
		this.s2Sum = this.s2Sum + funcItem.getS2();

		this.num++;
	}

	/**
	 * 求各字段经过合并后的平均值
	 */
	public void doAverage() {
		if (this.num > 0) {
			this.hit = this.hitSum / this.num;
			this.time = this.timeSum / this.num;
			this.avg = this.avgSum / this.num;
			this.s2 = this.s2Sum / this.num;
		}
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public double getHit() {
		return hit;
	}

	public void setHit(double hit) {
		this.hit = hit;
		this.hitSum = this.hit;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
		this.timeSum = this.time;
	}

	public double getAvg() {
		return avg;
	}

	public void setAvg(double avg) {
		this.avg = avg;
		this.avgSum = this.avg;
	}

	public double getS2() {
		return s2;
	}

	public void setS2(double s2) {
		this.s2 = s2;
		this.s2Sum = this.s2;
	}

}
