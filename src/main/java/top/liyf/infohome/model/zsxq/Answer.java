package top.liyf.infohome.model.zsxq;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author liyf
 * Created in 2021-05-20
 */
@Data
@Entity(name = "zsxq_answer")
public class Answer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.MERGE)
    private Owner owner;

    @Type(type = "text")
    private String text;
}
