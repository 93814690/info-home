package top.liyf.infohome.model.zsxq;

import lombok.Data;

/**
 * @author liyf
 * Created in 2021-05-20
 */
@Data
public class Response {

    private boolean succeeded;

    private long code;

    private ResponseData resp_data;
}
