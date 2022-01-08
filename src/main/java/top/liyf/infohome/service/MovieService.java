package top.liyf.infohome.service;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import top.liyf.fly.common.core.util.HttpClientResult;
import top.liyf.fly.common.core.util.HttpUtils;
import top.liyf.infohome.dao.MovieMapper;
import top.liyf.infohome.dao.MovieRatingMapper;
import top.liyf.infohome.model.movie.DbResponse;
import top.liyf.infohome.model.movie.DbSubject;
import top.liyf.infohome.model.weibo.Movie;
import top.liyf.infohome.model.weibo.MovieRating;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author liyf
 * Created in 2022-01-03
 */
@Service
@Slf4j
public class MovieService {

    private final MovieMapper movieMapper;
    private final RedisService redisService;
    private final MovieRatingMapper ratingMapper;

    public MovieService(MovieMapper movieMapper, RedisService redisService, MovieRatingMapper ratingMapper) {
        this.movieMapper = movieMapper;
        this.redisService = redisService;
        this.ratingMapper = ratingMapper;
    }

    public void getLatestMovie() throws Exception {
        getLatestMovie(0);
    }

    public void getLatestMovie(int pageStart) throws Exception {
        String url = "https://movie.douban.com/j/search_subjects?type=movie&tag=%E6%9C%80%E6%96%B0&page_limit=50&page_start=" + pageStart;
        HashMap<String, String> header = new HashMap<>(8);
        header.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        HttpClientResult result = HttpUtils.doGet(url, header, null);
        log.info("获取最新电影: {}", result);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DbResponse dbResponse = mapper.readValue(result.getContent(), DbResponse.class);
        HashSet<Integer> set = new HashSet<>();
        for (DbSubject subject : dbResponse.getSubjects()) {
            set.add(subject.getId());
        }
        redisService.sAdd(RedisConst.MV_INFO_SET, set.toArray());
    }

    public void getMovieByDouban(Integer dbId) throws IOException {
        log.info("update by dbId: {}", dbId);
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
        if (runtimeElement != null) {
            String runtime = runtimeElement.attr("content");
            movie.setRuntime(Integer.valueOf(runtime));
        }

        Elements infoElements = doc.select("#info");
        Elements pl = infoElements.select(".pl");
        ArrayList<String> plList = new ArrayList<>();
        for (Element element : pl) {
            plList.add(element.text());
        }
        String info = infoElements.text();
        movie.setCountry(getInfoByName(info, plList, "制片国家/地区:"));
        movie.setOtherTitle(getInfoByName(info, plList, "又名:"));
        movie.setImdbCode(getInfoByName(info, plList, "IMDb:"));

        movie.setUpdateTime(LocalDateTime.now());

        if (movie.getId() == null) {
            movieMapper.insert(movie);
        } else {
            movieMapper.updateByPrimaryKeySelective(movie);
        }

        // update rating
        MovieRating rating = ratingMapper.selectByPrimaryKey(movie.getId());
        if (rating == null) {
            rating = new MovieRating();
        }
        String dbRating = doc.select("#interest_sectl > div > div.rating_self.clearfix > strong").text();
        if (org.springframework.util.StringUtils.hasText(dbRating)) {
            rating.setDbRating(Double.valueOf(dbRating));
            String dbRatingPeople = doc.select("#interest_sectl > div > div.rating_self.clearfix > div > div.rating_sum > a > span").text();
            rating.setDbRatingPeople(Integer.valueOf(dbRatingPeople));
        } else {
            rating.setDbRating(null);
            rating.setDbRatingPeople(null);
        }

        rating.setUpdateTime(LocalDateTime.now());

        if (rating.getMovieId() == null) {
            rating.setMovieId(movie.getId());
            ratingMapper.insert(rating);
        } else {
            ratingMapper.updateByPrimaryKeySelective(rating);
        }
    }

    /**
     * 功能描述: 获取内容的信息
     * 
     * @param info
     * @param plList
     * @param name
     * @return java.lang.String
     * @author liyf
     */
    private String getInfoByName(String info, ArrayList<String> plList, String name) {
        String value;
        if (plList.contains(name)) {
            int index = plList.indexOf(name);
            if (index == plList.size() - 1) {
                value = StringUtils.subString(info, name, null);
            } else {
                value = StringUtils.subString(info, name, plList.get(index + 1));
            }
            return value.trim();
        }
        return null;
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
