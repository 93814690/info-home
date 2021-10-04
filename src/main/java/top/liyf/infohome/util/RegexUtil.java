package top.liyf.infohome.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liyf
 * Created in 2021-10-04
 */
public class RegexUtil {

    /**
     * 功能描述: 判断字符串是否包含中文
     *
     * @param str
     * @return boolean
     * @author liyf
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }
}
