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
package xyz.noark.game;

import xyz.noark.game.bootstrap.ServerBootstrap;

/**
 * Noark框架启动类.
 * 
 * <pre>
 * Noark.run(DemoApplication.class, args);
 * </pre>
 *
 * @since 3.0
 * @author 小流氓[176543888@qq.com]
 */
public final class Noark {
	/** 私有化构造函数. */
	private Noark() {}

	/**
	 * Return the full version string of the present Noark codebase.
	 * 
	 * @see Package#getImplementationVersion()
	 * @return 返回Noark的版本号
	 */
	public static String getVersion() {
		return Noark.class.getPackage().getImplementationVersion();
	}

	/**
	 * 启动游戏服务器.
	 * <p>
	 * 以启动Main方法所在的包开始扫描需要管理的类.
	 * 
	 * @param klass 启动服务类
	 * @param args 启动参数，可以动态覆盖配置
	 */
	public static void run(Class<? extends ServerBootstrap> klass, String... args) {
		new NoarkInitializer().init(klass, args);
	}
}