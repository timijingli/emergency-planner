package com.emergency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 个性化应急预案生成器 — 启动入口
 *
 * 运行方式：
 *   1. IDEA 中右键此类 → Run
 *   2. 或终端执行：mvnw spring-boot:run
 *   3. 浏览器打开 http://localhost:8080
 */
@SpringBootApplication
public class EmergencyPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmergencyPlannerApplication.class, args);
        System.out.println("======================================");
        System.out.println("  应急预案生成器已启动！");
        System.out.println("  打开浏览器访问: http://localhost:8080");
        System.out.println("======================================");
    }
}
