package top.liyf.infohome.gecco;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.pipeline.PipelineFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author liyf
 * Created in 2021-07-27
 */
//@Service
public class Engine implements ApplicationContextAware, InitializingBean {

    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() throws Exception {
        PipelineFactory springPipelineFactory = (PipelineFactory) context.getBean("springPipelineFactory");
        GeccoEngine.create()
                .pipelineFactory(springPipelineFactory)
                .classpath("top.liyf.infohome.gecco")
                .start("https://s.weibo.com/top/summary")
                .interval(61000)
                .loop(true)
                .start();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
