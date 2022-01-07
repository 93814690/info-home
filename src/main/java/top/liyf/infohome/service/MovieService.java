package top.liyf.infohome.service;

import com.alibaba.druid.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import top.liyf.infohome.dao.MovieMapper;
import top.liyf.infohome.model.weibo.Movie;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liyf
 * Created in 2022-01-03
 */
@Service
public class MovieService {

    private final MovieMapper movieMapper;

    public MovieService(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    public void getMovieByDouban(Integer dbId) throws IOException {
        Movie movie = movieMapper.selectOneByDbCode(dbId);
        if (movie == null) {
            movie = new Movie();
        }
        movie.setDbCode(dbId);
        String url = "https://movie.douban.com/subject/" + dbId;
        Document doc = Jsoup.connect(url).get();
        String title = doc.title().replace(" (豆瓣)", "");
        movie.setChineseTitle(title);
        String allTitle = doc.selectXpath("//*[@id=\"content\"]/h1/span[1]").text();
        String originalTitle = allTitle.replace(title, "").trim();
        movie.setOriginalTitle(originalTitle);
        String year = doc.selectXpath("//*[@id=\"content\"]/h1/span[2]").text();
        year = year.replace("(", "").replace(")", "");
        movie.setYear(Integer.valueOf(year));

        Elements genres = doc.select("#info span[property=v:genre]");
        movie.setGenre(elementsToString(genres));

        Elements initialReleaseDateElements = doc.select("#info span[property=v:initialReleaseDate]");
        movie.setInitialReleaseDate(elementsToString(initialReleaseDateElements));

        Element runtimeElement = doc.selectFirst("#info span[property=v:runtime]");
        String runtime = runtimeElement.attr("content");
        movie.setRuntime(Integer.valueOf(runtime));

        Elements infoElements = doc.select("#info");
        String info = infoElements.text();
        String country = StringUtils.subString(info, "制片国家/地区:", "语言").trim();
        movie.setCountry(country);
        String otherTitle = StringUtils.subString(info, "又名:", "IMDb").trim();
        movie.setOtherTitle(otherTitle);

        List<TextNode> textNodes = infoElements.first().textNodes();
        String imdb = textNodes.get(textNodes.size() - 2).text().trim();
        movie.setImdbCode(imdb);

        movie.setUpdateTime(LocalDateTime.now());

        if (movie.getId() == null) {
            movieMapper.insert(movie);
        } else {
            movieMapper.updateByPrimaryKeySelective(movie);
        }
    }

    private String elementsToString(Elements genres) {
        StringBuilder sb = new StringBuilder();
        for (Element genre : genres) {
            sb.append(genre.text()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
