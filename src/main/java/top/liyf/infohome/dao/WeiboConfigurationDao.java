package top.liyf.infohome.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import top.liyf.infohome.model.weibo.WeiboConfiguration;

/**
 * @author liyf
 * Created in 2021-05-26
 */
public interface WeiboConfigurationDao extends JpaRepository<WeiboConfiguration, Integer> {
}
