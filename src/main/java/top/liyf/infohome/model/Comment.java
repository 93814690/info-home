package top.liyf.infohome.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author liyf
 * Created in 2021-05-21
 */
@Data
@Entity(name = "zsxq_comment")
public class Comment implements Serializable {

    @Id
    private Long comment_id;

    @ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
    private Owner owner;

    @Column(length = 10000)
    private String text;

    private String create_time;
}
