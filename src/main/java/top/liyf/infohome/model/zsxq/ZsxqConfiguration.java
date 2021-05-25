package top.liyf.infohome.model.zsxq;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@Entity(name = "zsxq_configuration")
@Data
public class ZsxqConfiguration {

    @Id
    private long groupId;

    private String requestId;

    private String signature;

    private String timestamp;

    private String version;

    @Column(length = 1000)
    private String cookie;

}
