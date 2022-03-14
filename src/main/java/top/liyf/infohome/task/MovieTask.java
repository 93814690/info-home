package top.liyf.infohome.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liyf.infohome.service.MovieService;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import java.io.IOException;

/**
 * @author liyf
 * Created in 2022-01-07
 */
@Component
@Slf4j
public class MovieTask {

    private final MovieService movieService;
    private final RedisService redisService;

    public MovieTask(MovieService movieService, RedisService redisService) {
        this.movieService = movieService;
        this.redisService = redisService;
    }

    @Scheduled(cron = "0 30 6 ? * MON")
    public void getLatestMovie() throws Exception {
        movieService.getLatestMovie();
    }

    @Scheduled(cron = "0 30 7 1 * ?")
    public void getTop250() throws Exception {
        movieService.getTop250();
    }

    @Scheduled(cron = "*/30 * * * * ?")
    public void getMovieInfo() throws IOException {
        Object dbId = redisService.sPop(RedisConst.MV_INFO_SET);
        if (dbId != null) {
            movieService.getMovieByDouban((Integer) dbId);
        }
    }

    //@Scheduled(cron = "*/30 * * * * ?")
    public void initMovie() throws Exception {
        Integer startPage = (Integer) redisService.get("startPage");
        if (startPage == null) {
            startPage = 0;
        }
        movieService.getLatestMovie(startPage);
        redisService.set("startPage", startPage + 50);
    }
}
