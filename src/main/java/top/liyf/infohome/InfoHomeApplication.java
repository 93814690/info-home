package top.liyf.infohome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"top.liyf"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"top.liyf"})
@EnableScheduling
@EnableAsync
public class InfoHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfoHomeApplication.class, args);
    }

}
