package top.liyf.infohome.model.weibo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author liyf
 * Created in 2021-10-01
 */
@Data
public class TidResult {

    private String tid;

    @JsonProperty(value = "new_tid")
    private boolean newTid;

    private Integer conficence;
}
