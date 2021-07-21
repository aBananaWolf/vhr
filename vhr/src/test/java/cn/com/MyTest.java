package cn.com;

import cn.com.mq.threadpool.MessageScheduleThreadPool;
import cn.com.security.aware.ApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;

//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author wyl
 * @create 2020-08-02 11:19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MyTest {
    // ================================ redis 测试
  /*  @Autowired
    private ApplicationTest applicationTest;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Test
    public void test() {
        redisTemplate.boundHashOps("user1").put("1","zzzzzzz");
    }*/

    // ================================ 邮件测试
    /* @Autowired
    private JavaMailSenderImpl javaMailSender;
    @Test
    public void smsTest() throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setSubject("开饭通知");
        mimeMessageHelper.setText("你的好友<b style='color:red'>小白</b>带上了防化服邀请您去吃热干面",true);
        mimeMessageHelper.setTo("abananawolf@qq.com");
        mimeMessageHelper.setFrom("13049394389@163.com");
        javaMailSender.send(mimeMessage);
    }*/

    // ================================ 延时任务线程池测试
    @Autowired
    private MessageScheduleThreadPool messageScheduleThreadPool;
    @Test
    public void scheduleTest() throws Throwable {
        messageScheduleThreadPool.schedule(() -> {
            for (int i = 1; i < 999999; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(i);
            }
        }, 0 , TimeUnit.SECONDS);
        messageScheduleThreadPool.schedule(()-> {System.out.println(1);},10, TimeUnit.SECONDS);
        messageScheduleThreadPool.schedule(()-> {System.out.println(1);},10, TimeUnit.SECONDS);
//        messageScheduleThreadPool.schedule(()-> {System.out.println(1);},10, TimeUnit.SECONDS);
//        messageScheduleThreadPool.schedule(()-> {System.out.println(1);},10, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(100);


    }
}
