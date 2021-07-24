<h3>vhr : 项目的主体，crud、鉴权、登录校验 和 错误处理</h3>
<h3>vhr-service : 业务层</h3>
<h3>vhr-dao : 持久层</h3>
<h3>timed-task-service : 定时任务</h3>
<h3>sms-api : 发送短信的 api 和 发送消息的 api</h3>
<h3>sms-service : 消费消息的业务代码 和 发送短信的代码</h3>

"微人事" 是采用前后端分离的人力资源管理系统，属于 Web 项目，基于 SpringBoot 框架开发。
项目的页面整洁、操作简单。能够快速的对员工数据进行管理和统计

该项目具有以下功能点：
1. 项目持久层采用了 Mybatis 框架实现登录、修改、查看信息、模糊查询等功能；控制层采用了 SpringMVC 框架响应 JSON 数据来与前端交互、并约定 HTTP 状态码以跳出 HTML 的框架集来到授权登录界面
2. 完成基本的登录功能，为项目提供图片验证码和短信验证码的校验和登录流程
3. 完成 Session 集群化后的登录下线、并发会话控制和管理员踢出用户功能。用户的每次请求都会获取项目中的所有菜单路径资源，来匹配访问一个路径需要哪些角色，这两部分我采用了 Redis 将数据进行缓存，以提高响应速度
4. 对请求用户进行鉴权，使不同的角色能够看到不同的功能页面，而且不能进行越级操作
5. 完成发送邮件的功能（主要是在保存员工数据的时候使用）。由于邮件的发送非常的耗时，所以邮件将以异步消息的形式发出，达到功能解耦、提高响应速度和流量削峰的目的，为了保证消息投递的可靠性，使用了分布式定时任务 Elastic-Job 进行消息补偿机制
6. 最后是项目的全局异常处理和错误页面处理，在处理过后，任何以 4 开头的默认错误页面都会被替换为自定义的错误信息提醒页

这个项目起源于其他人提供的单体项目，因为本人不会前端，后被我修改为分布式项目。
1.springBoot 框架和 springSecurity 框架整合后，默认是只有单体的并发会话控制的，分布式并发会话控制需要自己
手动将 session 信息提取到 redis 中才能做到并发会话控制
2.发送邮件的功能原项目中只是使用了线程池，这里被我改造成了 rabbitmq + elastic-job 保证了消息投递的可靠性，同时考虑了消息消费的幂等性

3.并发会话控制：vhr子模块的 cn.com.security.session
`org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer#getSessionAuthenticationStrategy`
上面这个配置类使用了 `SessionRegistry` 、 `ConcurrentSessionControlAuthenticationStrategy` 、  `RegisterSessionAuthenticationStrategy`
三个具体的类，这些类我们要自己修改，并实现 SessionControlCustomizer 接口配置生效

3.管理员踢出用户功能，其实在并发会话控制后，要实现变的很简单

2.短信认证：vhr 子模块的 cn.com.security.verification.code.sms
`Spring Security`使用`UsernamePasswordAuthenticationFilter`过滤器来拦截用户名密码认证请求，
将用户名和密码封装成一个`UsernamePasswordToken`对象交给`AuthenticationManager`处理。
`AuthenticationManager`将挑出一个支持处理该类型`Token`的`AuthenticationProvider`
（这里为`DaoAuthenticationProvider`，`AuthenticationProvider`的其中一个实现类）来进行认证，
认证过程中`DaoAuthenticationProvider`将调用`UserDetailService`的`loadUserByUsername`方法来处理认证，
如果认证通过（即`UsernamePasswordToken`中的用户名和密码相符）则返回一个`UserDetails`类型对象，
并将认证信息保存到`Session`中，认证后我们便可以通过`Authentication`对象获取到认证的信息了。

由于`Spring Security`并没用提供短信验证码认证的流程，所以我们需要仿照上面这个流程来实现：

因为过滤器排序问题，所以我将两个认证过滤器进行抽象（vhr子模块的 cn.com.security.verification.code.common），做出一个上层的统一的认证过滤器，这样做，如果有新的认证流程需要加入也可以快速地完成开发，
利用 spring 框架 Bean 的生命周期，应用了 ImportBeanDefinitionRegistrar 与 BeanPostProcessor 两个类 和 策略模式

5.使用了 rabbitmq 消息队列 和 elastic-job 分布式定时任务 



