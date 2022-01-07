package top.liyf.infohome.model.movie;

import lombok.Data;

import java.util.List;

/**
 * @author liyf
 * Created in 2022-01-07
 */
@Data
public class DbResponse {

    private List<DbSubject> subjects;
}
