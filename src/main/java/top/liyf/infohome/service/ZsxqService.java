package top.liyf.infohome.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.liyf.fly.common.core.exception.BusinessException;
import top.liyf.fly.common.core.result.ResultCode;
import top.liyf.fly.common.core.util.HttpClientResult;
import top.liyf.fly.common.core.util.HttpUtils;
import top.liyf.fly.push.api.domain.ChanifyText;
import top.liyf.infohome.dao.TopicDao;
import top.liyf.infohome.dao.ZsxqConfigurationDao;
import top.liyf.infohome.feign.ChanifyClient;
import top.liyf.infohome.model.zsxq.Response;
import top.liyf.infohome.model.zsxq.Topic;
import top.liyf.infohome.model.zsxq.ZsxqConfiguration;
import top.liyf.infohome.util.RedisConst;
import top.liyf.infohome.util.ZsxqConst;
import top.liyf.redis.service.RedisService;

import java.util.*;

/**
 * @author liyf
 * Created in 2021-05-18
 */
@Service
@Slf4j
public class ZsxqService {

    @Autowired
    private TopicDao topicDao;
    @Autowired
    private ZsxqConfigurationDao configurationDao;
    @Autowired
    private ChanifyClient chanifyClient;
    @Autowired
    private RedisService redisService;

    /**
     * 功能描述: 获取星球数据
     *
     * @param groupId 星球Id
     * @param endTime endTime(分页开始时间)
     * @param getAll  是否获取全部
     * @author liyf
     */
    public void getData(Long groupId, String endTime, boolean getAll) throws Exception {
        String url = "https://api.zsxq.com/v2/groups/" + groupId + "/topics";

        HashMap<String, String> header = getHeader(groupId);

        Map<String, String> param = new HashMap<>(4);
        param.put("scope", "all");
        param.put("count", String.valueOf(ZsxqConst.COUNT));
        if (endTime != null) {
            param.put("end_time", endTime);
        }

        boolean hasNext = true;
        while (hasNext) {
            HashMap<String, Object> result = getData(url, header, param, getAll);
            hasNext = (boolean) result.get("hasNext");
            if (hasNext) {
                param.put("end_time", (String) result.get("endTime"));
                Thread.sleep(15000);
            }
        }
    }

    /**
     * 功能描述: 获取星球数据
     *
     * @param url
     * @param header
     * @param param
     * @param getAll
     * @return java.util.HashMap<java.lang.String,java.lang.Object>
     * @author liyf
     */
    public HashMap<String, Object> getData(String url, HashMap<String, String> header, Map<String, String> param, boolean getAll) throws Exception {
        HashMap<String, Object> map = new HashMap<>(4);
        boolean hasNext = false;
        String endTime = "";
        HttpClientResult result = HttpUtils.doGet(url, header, param);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Response response = mapper.readValue(result.getContent(), Response.class);
        if (response.isSucceeded()) {
            redisService.set(RedisConst.ZSXQ_COOKIE_EXPIRED, false);
            redisService.set(RedisConst.ZSXQ_ERROR_OTHER, false);
            List<Topic> topics = response.getResp_data().getTopics();
            for (Topic topic : topics) {
                Optional<Topic> byId = topicDao.findById(topic.getTopic_id());
                if (byId.isPresent()) {
                    hasNext = false;
                    endTime = byId.get().getCreate_time();
                    if (byId.get().getModify_time() != null) {
                        topicDao.save(topic);
                    }
                    continue;
                }
                hasNext = true;
                topicDao.save(topic);
                endTime = topic.getCreate_time();
            }
            if (getAll && topics.size() == ZsxqConst.COUNT) {
                map.put("hasNext", true);
            } else {
                map.put("hasNext", hasNext);
            }
            map.put("endTime", endTime);
            return map;
        } else if (response.getCode() == 1059) {
            log.info("try again");
            Thread.sleep(15000);
            return getData(url, header, param, getAll);
        } else {
            if (response.getCode() == 401) {
                Boolean expired = (Boolean) redisService.get(RedisConst.ZSXQ_COOKIE_EXPIRED);
                if (expired == null || !expired) {
                    redisService.set(RedisConst.ZSXQ_COOKIE_EXPIRED, true);
                    ChanifyText text = new ChanifyText();
                    text.setTitle("error - 知识星球");
                    text.setText("cookie 已过期");
                    text.setToken("CICy4YgGEiJBREpGVTM3RFpNNEZMRlZaN1FCWDVGSE5BTlY0TVM0RFpNGhRmU0vjxji92dxl8bfsQfWCC4Km-SIECAEQASoiQUhSN1pLV1czUkNRQVFJUlpCNUVDVElFS09WWFBSU05TTQ..sbiZSJu63KdZK1dm2l0Rtljnz-btD3V3tdLX3SeRimA");
                    chanifyClient.text(text);
                }
            } else {
                Boolean error = (Boolean) redisService.get(RedisConst.ZSXQ_ERROR_OTHER);
                if (error == null || !error) {
                    redisService.set(RedisConst.ZSXQ_ERROR_OTHER, true);
                    ChanifyText text = new ChanifyText();
                    text.setTitle("error - 知识星球");
                    text.setText(response.toString());
                    text.setToken("CICy4YgGEiJBREpGVTM3RFpNNEZMRlZaN1FCWDVGSE5BTlY0TVM0RFpNGhRmU0vjxji92dxl8bfsQfWCC4Km-SIECAEQASoiQUhSN1pLV1czUkNRQVFJUlpCNUVDVElFS09WWFBSU05TTQ..sbiZSJu63KdZK1dm2l0Rtljnz-btD3V3tdLX3SeRimA");
                    chanifyClient.text(text);
                }
            }
            log.error("response = " + response);
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }

    }

    private HashMap<String, String> getHeader(Long groupId) {
        Optional<ZsxqConfiguration> byId = configurationDao.findById(groupId);
        if (byId.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        ZsxqConfiguration configuration = byId.get();
        HashMap<String, String> header = new HashMap<>(32);
        header.put("Referer", "https://wx.zsxq.com/");
        header.put("Cookie", configuration.getCookie());
        header.put("User-Agent", configuration.getUserAgent());
        header.put("x-request-id", configuration.getRequestId());
        header.put("x-signature", configuration.getSignature());
        header.put("x-timestamp", configuration.getTimestamp());
        header.put("x-version", configuration.getVersion());
        header.put("Origin", "https://wx.zsxq.com");
        header.put("Accept", "application/json, text/plain, */*");
        header.put("authority", "api.zsxq.com");
        header.put("accept-language", "zh-CN,zh;q=0.9");
        header.put("accept-encoding", "gzip, deflate, br");
        return header;
    }
}
