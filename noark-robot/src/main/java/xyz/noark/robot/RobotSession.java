/*
 * Copyright © 2018 www.noark.xyz All Rights Reserved.
 *
 * 感谢您选择Noark框架，希望我们的努力能为您提供一个简单、易用、稳定的服务器端框架 ！
 * 除非符合Noark许可协议，否则不得使用该文件，您可以下载许可协议文件：
 *
 *        http://www.noark.xyz/LICENSE
 *
 * 1.未经许可，任何公司及个人不得以任何方式或理由对本框架进行修改、使用和传播;
 * 2.禁止在本项目或任何子项目的基础上发展任何派生版本、修改版本或第三方版本;
 * 3.无论你对源代码做出任何修改和改进，版权都归Noark研发团队所有，我们保留所有权利;
 * 4.凡侵犯Noark版权等知识产权的，必依法追究其法律责任，特此郑重法律声明！
 */
package xyz.noark.robot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import xyz.noark.core.lang.ByteArray;
import xyz.noark.core.network.*;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static xyz.noark.log.LogHelper.logger;

/**
 * 机器人Session.
 *
 * @author 小流氓[176543888@qq.com]
 * @since 3.4
 */
public class RobotSession extends AbstractSession {
    protected final Channel channel;
    /**
     * Session中存储的属性值
     */
    protected final Map<SessionAttrKey<?>, SessionAttr<?>> attrs;
    private String playerId;
    private PacketEncrypt packetEncrypt;

    public RobotSession(Channel channel) {
        super(channel.id(), ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress());
        this.channel = channel;
        this.attrs = new ConcurrentHashMap<>();
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public String getUid() {
        return playerId;
    }

    @Override
    public Serializable getPlayerId() {
        return playerId;
    }

    /**
     * 设置玩家ID.
     *
     * @param playerId 玩家ID
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public void send(Serializable opcode, Object protocol) {
        this.send(buildPacket(opcode, protocol));
    }

    @Override
    public void send(ByteArray packet) {
        // 链接已关闭了...
        if (!channel.isActive()) {
            logger.debug("send packet fail isActive=false. channel={}, playerId={}", channel, this.getPlayerId());
            return;
        }

        // 不可写，未发送的数据已达最高水位了...
        if (!channel.isWritable()) {
            logger.debug("send packet fail isWritable=false. channel={}, playerId={}", channel, this.getPlayerId());
            return;
        }

        this.writeAndFlush(packet);
    }

    @Override
    public void send(NetworkProtocol networkProtocol) {
        this.send(PacketCodecHolder.getPacketCodec().encodePacket(networkProtocol));
    }

    /**
     * 发送封包逻辑.
     *
     * @param packet 封包逻辑
     */
    protected void writeAndFlush(ByteArray packet) {
        channel.writeAndFlush(packet, channel.voidPromise());
    }

    @Override
    public void sendAndClose(Serializable opcode, Object protocol) {
        channel.writeAndFlush(buildPacket(opcode, protocol)).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 构建发送的封包对象.
     *
     * @param opcode   协议编号
     * @param protocol 协议内容
     * @return 封包对象
     */
    protected ByteArray buildPacket(Serializable opcode, Object protocol) {
        return PacketCodecHolder.getPacketCodec().encodePacket(new NetworkProtocol(opcode, protocol));
    }

    @Override
    public PacketEncrypt getPacketEncrypt() {
        return packetEncrypt;
    }

    /**
     * 设计封包密码方案.
     * <p>
     * 当不喜欢默认的方案可以自己实现此接口重置加密方案
     *
     * @param packetEncrypt 封包密码方案
     */
    public void setPacketEncrypt(PacketEncrypt packetEncrypt) {
        this.packetEncrypt = packetEncrypt;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> SessionAttr<T> attr(SessionAttrKey<T> key) {
        return (SessionAttr<T>) attrs.computeIfAbsent(key, k -> new SessionAttr<>());
    }

    @Override
    public void clearUidAndPlayerId() {
        this.playerId = null;
        this.state = State.CONNECTED;
    }
}