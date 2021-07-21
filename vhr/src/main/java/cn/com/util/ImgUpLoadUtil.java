package cn.com.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * @author wyl
 * @create 2020-08-05 18:55
 */
public class ImgUpLoadUtil {

    public static String upload(byte[] bytes) {
        return aliUpload(bytes);
    }

    public static String aliUpload(byte[] bytes) {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "http://oss-cn-shenzhen.aliyuncs.com";
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = "LTAI4FsM4cfLmHB96TMb7x4c";
        String accessKeySecret = "PDiUyGODSglPxQkWumwZlmT6SpPOb1";

        // https://2020-gmall.oss-cn-shenzhen.aliyuncs.com/vhr/263a5c33-b8e5-45a6-a73a-121efb475379.jpg

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String fileName = UUID.randomUUID().toString().replaceAll("\\-","");



        // <yourObjectName>表示上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
        PutObjectRequest putObjectRequest = new PutObjectRequest("2020-gmall", "vhr/" + fileName + ".jpg", new ByteArrayInputStream(bytes));

        // 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // metadata.setObjectAcl(CannedAccessControlList.Private);
        // putObjectRequest.setMetadata(metadata);

        // 上传图片。
        PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);


        // 关闭OSSClient。
        ossClient.shutdown();

        return "https://2020-gmall.oss-cn-shenzhen.aliyuncs.com/vhr/" + fileName + ".jpg";
    }
}
