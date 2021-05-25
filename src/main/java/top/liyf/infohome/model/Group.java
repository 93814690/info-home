package top.liyf.infohome.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author liyf
 * Created in 2021-05-20
 */
@Data
@Entity(name = "zsxq_group")
public class Group implements Serializable {

    @Id
    private long group_id;

    private String name;

    private String type;
}
