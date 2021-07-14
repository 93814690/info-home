package top.liyf.infohome.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liyf
 * Created in 2021-07-13
 */
@Data
public class UserChanify implements Serializable {
    private Long id;

    private String userId;

    private String chanifyToken;

    private static final long serialVersionUID = 1L;
}