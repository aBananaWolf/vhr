package cn.com.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;

/**
 * 发送短信的阿里云jdk,0.01元/条
 * @author wyl
 * @create 2020-08-04 15:12
 */
@Slf4j
public class SmsCodeSenderUtils {
    // https://img.alicdn.com/tfs/TB16JcyXHr1gK0jSZR0XXbP8XXa-24-26.png ，只需要跟则错误码提示和调试代码集成即可
    public static void sendSmsCode(String code, String phone) throws ClientException {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI4FsM4cfLmHB96TMb7x4c", "PDiUyGODSglPxQkWumwZlmT6SpPOb1");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", "青橙商城");
        request.putQueryParameter("TemplateCode", "SMS_179610791");
        request.putQueryParameter("TemplateParam", "{\"code\":" + code + "}");
        client.getCommonResponse(request);
    }
}
