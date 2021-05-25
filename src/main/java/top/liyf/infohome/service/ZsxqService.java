package top.liyf.infohome.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.liyf.fly.common.core.exception.BusinessException;
import top.liyf.fly.common.core.result.ResultCode;
import top.liyf.fly.common.core.util.HttpClientResult;
import top.liyf.fly.common.core.util.HttpUtils;
import top.liyf.infohome.dao.TopicDao;
import top.liyf.infohome.dao.ZsxqConfigurationDao;
import top.liyf.infohome.model.Response;
import top.liyf.infohome.model.Topic;
import top.liyf.infohome.model.ZsxqConfiguration;
import top.liyf.infohome.util.ZsxqConst;

import java.util.*;

/**
 * @author liyf
 * Created in 2021-05-18
 */
@Service
public class ZsxqService {

    @Autowired
    private TopicDao topicDao;
    @Autowired
    private ZsxqConfigurationDao configurationDao;

    /**
     * 功能描述: 获取星球所有数据
     *
     * @param groupId 星球Id
     * @param endTime endTime(分页开始时间)
     * @author liyf
     */
    public void getDataAll(Long groupId, String endTime) throws Exception {
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
            HashMap<String, Object> result = getData(url, header, param, true);
            hasNext = (boolean) result.get("hasNext");
            if (hasNext) {
                param.put("end_time", (String) result.get("endTime"));
            }
            Thread.sleep(5000);
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
        System.out.println("param = " + param);
        HashMap<String, Object> map = new HashMap<>(4);
        boolean hasNext = false;
        String endTime = "";
        HttpClientResult result = HttpUtils.doGet(url, header, param);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Response response = mapper.readValue(result.getContent(), Response.class);
        if (response.isSucceeded()) {
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
                System.out.println("bu cun zai");
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
            System.out.println("try again");
            Thread.sleep(10000);
            return getData(url, header, param, getAll);
        } else {
            System.out.println("response = " + response);
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
        header.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
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
