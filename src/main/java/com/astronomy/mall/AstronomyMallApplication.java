package com.astronomy.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 天文器材商城系统启动类
 *
 * @author [吴梓骞]
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.astronomy.mall.module.*.mapper")
@EnableScheduling  // 🆕 开启定时任务
public class AstronomyMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstronomyMallApplication.class, args);
        System.out.println("\n=================================================");
        System.out.println("🌌 天文商城系统启动成功!");
        System.out.println("📄 接口文档地址: http://localhost:8080/doc.html");
        System.out.println("=================================================\n");
    }
}