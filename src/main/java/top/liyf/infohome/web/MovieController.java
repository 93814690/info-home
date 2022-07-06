package top.liyf.infohome.web;

import cn.hutool.core.thread.ThreadUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.liyf.infohome.service.MovieService;
import top.liyf.infohome.util.RedisConst;
import top.liyf.redis.service.RedisService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author liyf
 * Created in 2022-04-04
 */
@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService service;
    private final HttpServletRequest request;
    private final RedisService redisService;

    public MovieController(MovieService service, HttpServletRequest request, RedisService redisService) {
        this.service = service;
        this.request = request;
        this.redisService = redisService;
    }

    @GetMapping("/{tag}/{genres}/{countries}/{year}/{start}")
    public String getByTag(@PathVariable String tag, @PathVariable String genres, @PathVariable String countries, @PathVariable int year, @PathVariable int start) throws Exception {
        String uri = request.getRequestURI();
        Boolean isMember = redisService.sIsMember(RedisConst.MV_TAG_URI, uri);
        if (isMember) {
            return "fail";
        }
        redisService.sAdd(RedisConst.MV_TAG_URI, uri);
        service.getByTagAll(tag, genres, countries, year, start);
        return "success";
    }
}
