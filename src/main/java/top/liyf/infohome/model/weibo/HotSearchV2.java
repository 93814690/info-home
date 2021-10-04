package top.liyf.infohome.model.weibo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author liyf
 * Created in 2021-07-12
 */
@Data
public class HotSearchV2 implements Serializable {
    private Long id;

    private String word;

    private String emoticon;

    /**
     * 排名
     */
    private Integer rank;

    private Long num;

    /**
     * 0:无
     * 1:新
     * 2:热
     * 3:沸
     * 4:爆
     */
    private Integer state;

    private LocalDateTime recordTime;

    private static final long serialVersionUID = 1L;

    public HotSearchV2() {
    }

}