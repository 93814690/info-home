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

    @Scheduled(cron = "0 30 6 * * ?")
    public void getLatestMovie() throws Exception {
        movieService.getLatestMovie();
    }

    @Scheduled(cron = "*/15 * * * * ?")
    public void getMovieInfo() throws IOException {
        Object dbId = redisService.sPop(RedisConst.MV_INFO_SET);
        movieService.getMovieByDouban((Integer) dbId);
    }
}
