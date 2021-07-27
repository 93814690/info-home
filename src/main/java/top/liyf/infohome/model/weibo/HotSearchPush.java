package top.liyf.infohome.model.weibo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author liyf
 * Created in 2021-07-13
 */
@Data
public class HotSearchPush implements Serializable {
    private Long id;

    private String userId;

    private String word;

    private LocalDateTime pushTime;

    private static final long serialVersionUID = 1L;
}