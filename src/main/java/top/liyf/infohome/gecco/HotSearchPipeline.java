package top.liyf.infohome.gecco;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.liyf.infohome.dao.HotSearchV2Mapper;
import top.liyf.infohome.model.weibo.HotSearchV2;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author liyf
 * Created in 2021-07-25
 */
@PipelineName("hotSearchPipeline")
@Slf4j
@Service
public class HotSearchPipeline implements Pipeline<HSHtml> {

    @Autowired
    private HotSearchV2Mapper hotSearchV2Mapper;
    @Autowired
    private RedisService redisService;

    @Override
    public void process(HSHtml hsHtml) {
        List<HSItem> items = hsHtml.getList().stream()
                .filter(item -> item.getNum() != 0)
                .collect(Collectors.toList());

        redisService.delete(RedisConst.WB_HOTSEARCH_NEW);

        Set<String> set = new HashSet<>();
        for (HSItem item : items) {
            HotSearchV2 hotSearchV2 = new HotSearchV2(item);
            hotSearchV2Mapper.insert(hotSearchV2);
            redisService.rPush(RedisConst.WB_HOTSEARCH_PUSH_LIST, hotSearchV2);
            set.add(item.getWord());
        }
        redisService.sAdd(RedisConst.WB_HOTSEARCH_NEW, set.toArray());

        Set<String> diffString = redisService.sDiffString(RedisConst.WB_HOTSEARCH_OLD, RedisConst.WB_HOTSEARCH_NEW);
        log.info("下榜热搜: {}", diffString);
        for (String s : diffString) {
            redisService.delete(RedisConst.WB_HOTSEARCH_PUSHED + s);
        }
        redisService.delete(RedisConst.WB_HOTSEARCH_OLD);
        redisService.sAdd(RedisConst.WB_HOTSEARCH_OLD, set.toArray());
    }
}
