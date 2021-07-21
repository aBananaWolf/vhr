package cn.com.mq.config;

import cn.com.mq.threadpool.MessageScheduleThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.*;

/**
 * @author wyl
 * @create 2020-08-14 13:30
 */
@Slf4j
@Configuration
public class ScheduleThreadPoolConfig {
    @Bean
    public MessageScheduleThreadPool MessageScheduleThreadPool() {
        /*return new MessageScheduleThreadPool(Runtime.getRuntime().availableProcessors() * (1 + 99),
                500,
                new ThreadPoolExecutor.AbortPolicy());*/
        return new MessageScheduleThreadPool(1,
                20,
                new ErrorLogPolicy());
    }

    public static class ErrorLogPolicy implements RejectedExecutionHandler {
        /**
         * Creates an {@code AbortPolicy}.
         */
        public ErrorLogPolicy() { }

        /**
         * Always throws RejectedExecutionException.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         * @throws RejectedExecutionException always
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (log.isErrorEnabled()) {
                log.error(r.toString(), e);
            }
        }
    }
}
