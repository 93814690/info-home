package top.liyf.infohome.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * @author liyf
 * Created in 2022-01-03
 */
@SpringBootTest
class TestMovieService {

    @Autowired
    MovieService movieService;

    @Test
    void getMovieByDouban() throws IOException {
        movieService.getMovieByDouban(34868380);
    }

    @Test
    void getLatestMovie() throws Exception {
        movieService.getLatestMovie();
    }

    @Test
    void getTop250() throws Exception {
        movieService.getTop250();

    }
}