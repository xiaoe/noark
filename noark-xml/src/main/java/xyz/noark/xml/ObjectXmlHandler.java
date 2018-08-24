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
package xyz.noark.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 用SAX解析XML的Handler.
 * <p>
 * 这个XML根节点必需是object
 *
 * @since 3.1
 * @author 小流氓(176543888@qq.com)
 */
class ObjectXmlHandler<T> extends AbstractXmlHandler<T> {

	private final ObjectData data = new ObjectData();
	protected boolean isRootNode = false;

	public ObjectXmlHandler(Class<T> klass, String tplFileName) {
		super(klass, tplFileName);
	}

	@Override
	public void startDocument() throws SAXException {
		// 文档开始，标识为根节点
		isRootNode = true;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// 不是根节点，需要把当前的处理节点，压入队列
		if (!isRootNode) {
			data.addHandleNode(qName);
		}

		for (int i = 0, len = attributes.getLength(); i < len; i++) {
			data.putAttrData(attributes.getQName(i), attributes.getValue(i));
		}

		if (isRootNode) {
			this.isRootNode = false;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (!isRootNode) {
			data.removeHandleNode(qName);
		}
	}

	/**
	 * @return 获取结果
	 */
	public T getResult() {
		// 唯一对象，正常是配置文件，那需要修正EL表达式的.
		return buildObject(data, true);
	}
}