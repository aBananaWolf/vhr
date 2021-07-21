package cn.com.security.verification.code.img.util;

import cn.com.security.verification.code.img.raw.ImageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author wyl
 * @create 2020-08-02 16:03
 */
@Component
public class ImageCodeGeneratorUtil {
    private final String[] RANDOM_CODE = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","0","1","2","3","4","5","6","7","8","9"};

    private final String[] fontNames = { "宋体", "楷体", "隶书", "微软雅黑" };

    public ImageCode createCode(long expireTime) {
        return doCreateCode(expireTime,TimeUnit.SECONDS);
    }
    public ImageCode createCode(long expireTime, TimeUnit timeUnit) {
        return doCreateCode(expireTime, timeUnit);
    }

    private ImageCode doCreateCode(long expireTime, TimeUnit timeUnit) {
        int width = 100; // 验证码图片宽度
        int height = 36; // 验证码图片长度
        int length = 4; // 验证码位数

        // 随机数
        Random random = new Random();

        // 创建图片对象
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 2d 画笔
        Graphics2D g = (Graphics2D)img.getGraphics();

        // 调整画笔rgb颜色
        g.setColor(this.randomColor(200,255,random));
        // 填充长方形
        g.fillRect(0,0,width,height);

        StringBuilder builder = new StringBuilder();
        String str;
        int shift;
        int degree;
        // 填充验证码
        for (int i = 0; i < length; i++) {
            // 随机验证码
            str = RANDOM_CODE[random.nextInt(RANDOM_CODE.length)];
            builder.append(str);

            // 注意颜色和划线部分重合即可
            g.setColor(this.randomColor(80,155,random));
            g.setFont(this.randomFont(random));

            degree = random.nextInt(30);
            shift = 22 * i + 8;
            // 以弧度为参数的 旋转角度
            g.rotate(degree * Math.PI / 180, shift, 25);
            g.drawString(str, shift,25);
            // 旋转回去
            g.rotate(-degree * Math.PI / 180, shift, 25);
        }

        // 填充横线
        int strokesCount = 25;
        int scribing = 3;
        for (int i = 0; i < strokesCount; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(50);
            int yl = random.nextInt(height);
            if (scribing > 0) {
                // 横线
                g.setColor(this.randomColor(100,200, random));
                g.drawLine(0, y, 100, yl);
                scribing--;
            }
            g.setColor(this.randomColor(133,200, random));
            // 随机画笔
            g.drawLine(x, y, x + xl, y + yl);

        }

        // 处理并释放资源
        g.dispose();

        return new ImageCode(expireTime,timeUnit,builder.toString(),img);
    }

    /**
     * 随机一个字体
     */
    public Font randomFont(Random random){
        String name = fontNames[random.nextInt(fontNames.length)];
        int style = random.nextInt(4);
        int size = random.nextInt(3) + 30;
        return new Font(name, style, size);
    }

    /**
     * 随机一个颜色
     */
    public Color randomColor(int floor, int ceiling, Random random) {
        return new Color(
                floor + random.nextInt(ceiling - floor),
                floor + random.nextInt(ceiling - floor),
                floor + random.nextInt(ceiling - floor)
                );
    }

    /**
     * 打印验证码我们的 码表，使用 ascii 码表生成字母
     * @return
     */
    private static String printImageCode() {
        List<Integer> list1 = IntStream.range(65, 65 + 26).boxed().map(t -> new Integer(t.toString())).collect(toList());
        List<Integer> list2 = IntStream.range(65 + 32 , 65 + 32 + 26).boxed().map(t -> new Integer(t.toString())).collect(toList());
        list1.addAll(list2);
        List<Character> collect = list1.stream().map(num -> new Character((char) num.intValue())).collect(toList());
        collect.addAll(Arrays.asList('0','1','2','3','4','5','6','7','8','9'));

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Character character : collect) {
            builder.append("\"");
            builder.append(String.valueOf(character));
            builder.append("\"");
            builder.append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append("]");
        System.out.println(builder.toString());
        return builder.toString();
    }
}
