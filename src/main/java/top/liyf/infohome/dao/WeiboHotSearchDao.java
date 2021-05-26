package top.liyf.infohome.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import top.liyf.infohome.model.weibo.HotSearch;

import java.util.Optional;

/**
 * @author liyf
 * Created in 2021-05-25
 */
public interface WeiboHotSearchDao extends JpaRepository<HotSearch, String> {

    Optional<HotSearch> findByMid(String mid);

    Optional<HotSearch> findByNoteAndOnboardTime(String note, long onboardTime);
}
