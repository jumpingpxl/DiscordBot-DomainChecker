package de.jumpingpxl.womenareproperties.domainchecker.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ScheduledTask {

	private final Timer timer = new Timer();
	private final Runnable runnable;
	private boolean started;

	public ScheduledTask(Runnable runnable) {
		this.runnable = runnable;
	}

	public ScheduledTask delay(long time, TimeUnit timeUnit) {
		if (!started) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					runnable.run();
				}
			}, timeUnit.toMillis(time));

			started = true;
		}

		return this;
	}

	public ScheduledTask repeat(long time, TimeUnit timeUnit) {
		if (!started) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					runnable.run();
				}
			}, timeUnit.toMillis(time), timeUnit.toMillis(time));

			started = true;
		}

		return this;
	}

	public void cancel() {
		timer.cancel();
	}
}
