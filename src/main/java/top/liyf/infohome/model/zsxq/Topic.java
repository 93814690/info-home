package top.liyf.infohome.model.zsxq;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author liyf
 * Created in 2021-05-20
 */
@Data
@Entity(name = "zsxq_topic")
public class Topic implements Serializable {

    @Id
    private long topic_id;

    @ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
    private Group group;

    private String type;

    @OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
    private Question question;

    @OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
    private Answer answer;

    @ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
    private Talk talk;

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
    private List<Comment> show_comments;

    private String create_time;

    private String modify_time;
}
