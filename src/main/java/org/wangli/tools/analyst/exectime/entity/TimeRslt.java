package org.wangli.tools.analyst.exectime.entity;

public class TimeRslt {

	// 本次执行时间
	private double costTime;

	// 本次执行时间（分钟）
	private double costTimeMinutes;

	// 本次执行时间（秒）
	private double costTimeSeconds;

	// 本次执行时间（毫秒）
	private double costTimeMs;

	public double getCostTime() {
		return costTime;
	}

	public void setCostTime(double costTime) {
		this.costTime = costTime;
	}

	public double getCostTimeMinutes() {
		return costTimeMinutes;
	}

	public void setCostTimeMinutes(double costTimeMinutes) {
		this.costTimeMinutes = costTimeMinutes;
	}

	public double getCostTimeSeconds() {
		return costTimeSeconds;
	}

	public void setCostTimeSeconds(double costTimeSeconds) {
		this.costTimeSeconds = costTimeSeconds;
	}

	public double getCostTimeMs() {
		return costTimeMs;
	}

	public void setCostTimeMs(double costTimeMs) {
		this.costTimeMs = costTimeMs;
	}
}
