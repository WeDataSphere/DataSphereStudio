package com.webank.wedatasphere.dss.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局恢复 Spring Boot 2 的尾部斜杠(trailing slash)匹配行为。
 *
 * <p>Spring Boot 3 / Spring Framework 6 起默认关闭了尾部斜杠匹配，导致前端带尾部斜杠的请求
 * (例如 {@code /dss/guide/query/groupdetail/}) 无法匹配到 {@code @RequestMapping("/groupdetail")}，
 * 直接返回 404。DSS 前端普遍使用带尾部斜杠的 URL（见 apiPath.js），因此在此统一开启，
 * 避免逐个修改后端 mapping。
 *
 * <p>{@link PathMatchConfigurer#setUseTrailingSlashMatch} 在 Spring 6.x 中仍可用，
 * 仅被标记为 @Deprecated(since="6.0")，功能正常，是官方推荐的 SB2->SB3 迁移过渡方案。
 */
@Configuration
public class TrailingSlashWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }
}
