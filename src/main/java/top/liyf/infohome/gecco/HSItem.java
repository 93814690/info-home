package top.liyf.infohome.gecco;


import com.geccocrawler.gecco.annotation.Attr;
import com.geccocrawler.gecco.annotation.HtmlField;
import com.geccocrawler.gecco.annotation.Text;
import com.geccocrawler.gecco.spider.HtmlBean;
import lombok.Data;

/**
 * @author liyf
 * Created in 2021-07-19
 */
@Data
public class HSItem implements HtmlBean {

    @Text
    @HtmlField(cssPath = "td.td-01.ranktop")
    private int rank;

    @HtmlField(cssPath = "td.td-02 > a")
    private String word;

    @Text
    @HtmlField(cssPath = "td.td-02 > span")
    private long num;

    @HtmlField(cssPath = "td.td-02 > img")
    @Attr(value = "title")
    private String emoticon;

    @HtmlField(cssPath = "td.td-03 > i")
    private String state;
}
