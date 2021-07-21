package cn.com.vo;

import java.util.List;

/**
 * @author wyl
 * @create 2020-08-05 19:29
 */
public class RespPageBean {
    private Long total;
    private List<?> data;

    public RespPageBean() {
    }

    public static RespPageBean processing(List<List<?>> array) {
        RespPageBean respPageBean = new RespPageBean();
        respPageBean.setData(array.get(0));
        respPageBean.setTotal((Long)array.get(1).get(0));
        return respPageBean;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }


    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }
}
