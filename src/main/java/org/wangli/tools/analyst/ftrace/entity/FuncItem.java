package org.wangli.tools.analyst.ftrace.entity;

public class FuncItem {

	// 函数名称
	private String funcName;

	// 执行次数
	private double hit = 0;
	private double hitSum = 0;

	// 执行时间
	private double time = 0;
	private double timeSum = 0;

	// 累加的FuncItem的个数
	private int addNum = 1;

	public void addFuncItem(FuncItem funcItem) {
		this.hitSum = this.hitSum + funcItem.getHit();
		this.timeSum = this.timeSum + funcItem.getTime();
		this.addNum++;
	}

	public void doAverage() {
		this.hit = this.hitSum / this.addNum;
		this.time = this.timeSum / this.addNum;
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
		if (this.hitSum == 0) {
			this.hitSum = this.hit;
		}
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
		if (this.timeSum == 0) {
			this.timeSum = this.time;
		}
	}

	public double getHitSum() {
		return hitSum;
	}

	public double getTimeSum() {
		return timeSum;
	}

}
