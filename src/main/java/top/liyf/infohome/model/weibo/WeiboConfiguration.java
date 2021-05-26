package top.liyf.infohome.model.weibo;

import lombok.Data;

import javax.persistence.*;

/**
 * @author liyf
 * Created in 2021-05-26
 */
@Data
@Entity(name = "weibo_configuration")
public class WeiboConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 1000)
    private String cookie;

    private String xsrfToken;
}
