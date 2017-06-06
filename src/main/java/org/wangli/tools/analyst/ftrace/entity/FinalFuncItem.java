package org.wangli.tools.analyst.ftrace.entity;

public class FinalFuncItem {

	// 执行次数
	private double hit = 0;

	// 执行时间
	private double time = 0;

	public void mergeFinalFuncItem(FinalFuncItem finalFuncItem) {
		this.hit += finalFuncItem.getHit();
		this.time += finalFuncItem.getTime();
	}

	public double getHit() {
		return hit;
	}

	public void setHit(double hit) {
		this.hit = hit;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

}
