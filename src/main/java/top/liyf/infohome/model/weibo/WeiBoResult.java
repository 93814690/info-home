package top.liyf.infohome.model.weibo;

import lombok.Data;

/**
 * @author liyf
 * Created in 2021-10-01
 */
@Data
public class WeiBoResult {

    private int retcode;

    private String msg;

    private Object data;
}
