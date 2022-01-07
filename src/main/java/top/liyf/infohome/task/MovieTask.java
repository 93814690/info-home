package top.liyf.infohome.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liyf.infohome.service.MovieService;

/**
 * @author liyf
 * Created in 2022-01-07
 */
@Component
@Slf4j
public class MovieTask {

    private final MovieService movieService;

    public MovieTask(MovieService movieService) {
        this.movieService = movieService;
    }

    @Scheduled(cron = "0 30 6 * * ?")
    public void getLatestMovie() throws Exception {
        movieService.getLatestMovie();
    }
}
