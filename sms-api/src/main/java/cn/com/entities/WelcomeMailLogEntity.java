package cn.com.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author wyl
 * @since 2020-08-18
 */
@TableName("welcome_mail_log")
public class WelcomeMailLogEntity implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 消息唯一id，主键去重
     */
      @TableId(value = "msg_id", type = IdType.INPUT)
    private String msgId;

    /**
     * 员工id
     */
    private Integer empId;

    /**
     * 消息重试次数
     */
    private Integer attemptCount;

    /**
     * 消息状态(0执行中，1发送成功，2发送失败，3消息抵达消费端，4消费成功，5消费失败)
     */
    private Integer status;

    /**
     * 定时任务捕获时间，适当推迟这个时间，避免定时任务捕获正常的消息
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime tryTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由键
     */
    private String routingKey;


    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getTryTime() {
        return tryTime;
    }

    public void setTryTime(LocalDateTime tryTime) {
        this.tryTime = tryTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    @Override
    public String toString() {
        return "WelcomeMailLogEntity{" +
        "msgId=" + msgId +
        ", empId=" + empId +
        ", attemptCount=" + attemptCount +
        ", status=" + status +
        ", tryTime=" + tryTime +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        ", exchange=" + exchange +
        ", routingKey=" + routingKey +
        "}";
    }
}
