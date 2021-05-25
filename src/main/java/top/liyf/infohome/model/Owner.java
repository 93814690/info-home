package top.liyf.infohome.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author liyf
 * Created in 2021-05-20
 */
@Data
@Entity(name = "zsxq_user")
public class Owner {

    @Id
    private long user_id;

    private String name;

    private String description;

    private String avatar_url;
}
