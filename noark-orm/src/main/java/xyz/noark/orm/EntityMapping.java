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
package xyz.noark.orm;

import xyz.noark.core.annotation.orm.CreatedDate;
import xyz.noark.core.annotation.orm.Entity;
import xyz.noark.core.annotation.orm.Entity.FetchType;
import xyz.noark.core.annotation.orm.LastModifiedDate;
import xyz.noark.reflectasm.ConstructorAccess;
import xyz.noark.reflectasm.MethodAccess;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static xyz.noark.log.LogHelper.logger;

/**
 * 实体映射描述接口.
 *
 * @author 小流氓[176543888@qq.com]
 * @since 3.0
 */
public class EntityMapping<T> {
    protected final Class<T> klass;
    /**
     * 抓取策略
     */
    protected final FetchType fetchType;
    private final MethodAccess methodAccess;
    private final ConstructorAccess<T> constructorAccess;
    /**
     * 表名
     */
    protected String tableName;
    /**
     * 主键字段
     */
    protected FieldMapping primaryId;
    /**
     * 玩家ID字段
     */
    protected FieldMapping playerId;
    /**
     * 全部属性
     */
    protected List<FieldMapping> fieldInfo;
    /**
     * 创建时间
     */
    protected FieldMapping createdDate;
    /**
     * 最后修改时间
     */
    protected FieldMapping lastModifiedDate;
    /**
     * 注释
     */
    private String tableComment;

    public EntityMapping(Class<T> klass) {
        this.klass = klass;
        Entity entity = klass.getAnnotation(Entity.class);
        this.fetchType = entity.fetch();
        this.methodAccess = MethodAccess.get(klass);
        this.constructorAccess = ConstructorAccess.get(klass);
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public FieldMapping getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(FieldMapping primaryId) {
        this.primaryId = primaryId;
    }

    public FieldMapping getPlayerId() {
        return playerId;
    }

    public void setPlayerId(FieldMapping playerId) {
        this.playerId = playerId;
    }

    public List<FieldMapping> getFieldInfo() {
        return fieldInfo;
    }

    public void setFieldInfo(List<FieldMapping> fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    /**
     * 获取主键的值.
     *
     * @param entity 实体对象
     * @return 对象的主键值
     */
    public Serializable getPrimaryIdValue(Object entity) {
        return (Serializable) methodAccess.invoke(entity, primaryId.getGetMethodIndex());
    }

    /**
     * 获取玩家ID的值.
     *
     * @param entity 实体对象
     * @return 对象的玩家ID
     */
    public Serializable getPlayerIdValue(Object entity) {
        return (Serializable) methodAccess.invoke(entity, playerId.getGetMethodIndex());
    }

    public Class<T> getEntityClass() {
        return klass;
    }

    public List<FieldMapping> getFieldMapping() {
        return fieldInfo;
    }

    public void setCreatedDate(FieldMapping createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedDate(FieldMapping lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * 构造一个回写数据的唯一Key.
     * <p>
     * 类的全名+主键值
     *
     * @param entity 实体对象
     * @return 拼接后的唯一Key
     */
    public String getPrimaryKey(Object entity) {
        return new StringBuilder(64).append(klass.getName()).append(':').append(this.getPrimaryIdValue(entity)).toString();
    }

    public T newEntity() {
        return constructorAccess.newInstance();
    }

    public MethodAccess getMethodAccess() {
        return methodAccess;
    }

    /**
     * 如果当前实体有实现了{@link CreatedDate}或{@link LastModifiedDate}注解的字段，那就要按规则给他赋值
     *
     * @param entity 实体对象
     */
    public void touchForCreate(T entity) {
        // 实现了CreatedDate注解 并且 这个属性没有值
        if (createdDate != null && methodAccess.invoke(entity, createdDate.getGetMethodIndex()) == null) {
            this.touchDate(entity, createdDate);
        }
        // 实现了LastModifiedDate注解 并且 这个属性没有值
        if (lastModifiedDate != null && methodAccess.invoke(entity, lastModifiedDate.getGetMethodIndex()) == null) {
            this.touchDate(entity, lastModifiedDate);
        }
    }

    /**
     * 如果当前实体有实现了{@link LastModifiedDate}注解的字段，那就要按规则给他赋值
     *
     * @param entity 实体对象
     */
    public void touchForUpdate(T entity) {
        // 更新逻辑，只要有更新那就要重新给值
        if (lastModifiedDate != null) {
            this.touchDate(entity, lastModifiedDate);
        }
    }

    private void touchDate(T entity, FieldMapping fm) {
        switch (fm.getType()) {
            case AsDate:
                methodAccess.invoke(entity, fm.getSetMethodIndex(), new Date());
                break;
            case AsInstant:
                methodAccess.invoke(entity, fm.getSetMethodIndex(), Instant.now());
                break;
            case AsLocalDateTime:
                methodAccess.invoke(entity, fm.getSetMethodIndex(), LocalDateTime.now());
                break;
            case AsLong:
                methodAccess.invoke(entity, fm.getSetMethodIndex(), System.currentTimeMillis());
                break;
            default:
                logger.warn("未实现的类型 type={}", fm.getType().name());
                break;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((klass == null) ? 0 : klass.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityMapping<?> other = (EntityMapping<?>) obj;
        if (klass == null) {
            if (other.klass != null) {
                return false;
            }
        } else if (!klass.equals(other.klass)) {
            return false;
        }
        return true;
    }
}