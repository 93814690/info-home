package top.liyf.infohome.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import top.liyf.infohome.model.Topic;

/**
 * @author liyf
 * Created in 2021-05-21
 */
public interface TopicDao extends JpaRepository<Topic,Long> {


}
