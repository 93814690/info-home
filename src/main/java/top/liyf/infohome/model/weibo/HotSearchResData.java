package top.liyf.infohome.model.weibo;

import lombok.Data;

import java.util.List;

/**
 * @author liyf
 * Created in 2021-05-25
 */
@Data
public class HotSearchResData {

    private HotSearch hotgov;

    private List<HotSearch> realtime;
}
