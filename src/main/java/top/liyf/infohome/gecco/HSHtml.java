package top.liyf.infohome.gecco;

import com.geccocrawler.gecco.annotation.Gecco;
import com.geccocrawler.gecco.annotation.HtmlField;
import com.geccocrawler.gecco.spider.HtmlBean;
import lombok.Data;

import java.util.List;


/**
 * @author liyf
 * Created in 2021-07-18
 */
@Gecco(matchUrl = "https://s.weibo.com/top/summary", pipelines = {"hotSearchPipeline"})
@Data
public class HSHtml implements HtmlBean {


    @HtmlField(cssPath = "#pl_top_realtimehot > table > tbody > tr")
    private List<HSItem> list;

}
