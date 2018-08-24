/*
 * Copyright © 2018 www.noark.xyz All Rights Reserved.
 * 
 * 感谢您选择Noark框架，希望我们的努力能为您提供一个简单、易用、稳定的服务器端框架 ！
 * 除非符合Noark许可协议，否则不得使用该文件，您可以下载许可协议文件：
 * 
 * 		http://www.noark.xyz/LICENSE
 *
 * 1.未经许可，任何公司及个人不得以任何方式或理由对本框架进行修改、使用和传播;
 * 2.禁止在本项目或任何子项目的基础上发展任何派生版本、修改版本或第三方版本;
 * 3.无论你对源代码做出任何修改和改进，版权都归Noark研发团队所有，我们保留所有权利;
 * 4.凡侵犯Noark版权等知识产权的，必依法追究其法律责任，特此郑重法律声明！
 */
package xyz.noark.game.monitor.impl;

import static xyz.noark.log.LogHelper.logger;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import xyz.noark.game.monitor.AbstractMonitorService;

/**
 * 内存监控服务.
 *
 * @since 3.1
 * @author 小流氓(176543888@qq.com)
 */
public class MemoryMonitorService extends AbstractMonitorService {
	private final Field maxMemory;
	private final Field reserverdMemory;

	public MemoryMonitorService() {
		try {
			Class<?> c = Class.forName("java.nio.Bits");
			maxMemory = c.getDeclaredField("maxMemory");
			maxMemory.setAccessible(true);
			reserverdMemory = c.getDeclaredField("reservedMemory");
			reserverdMemory.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	@Override
	protected long getInitialDelay() {
		return 60;
	}

	@Override
	protected long getDelay() {
		return 60;
	}

	@Override
	protected TimeUnit getUnit() {
		return TimeUnit.SECONDS;
	}

	@Override
	protected void exe() throws Exception {
		Runtime runtime = Runtime.getRuntime();
		Long maxMemoryValue = (Long) maxMemory.get(null);
		final AtomicLong reserverdMemoryValue = (AtomicLong) reserverdMemory.get(null);
		final long totalMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();
		logger.info("服务器堆内存总共 {} M,占用堆内存 {} M,直接内存总共 {} M,占用直接内存 {} M", new Object[] { totalMemory / (1024 * 1024), (totalMemory - freeMemory) / (1024 * 1024), maxMemoryValue / (1024 * 1024), reserverdMemoryValue.get() / (1024 * 1024) });
	}
}