package vn.team9.auction_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Value("${avatar.upload-dir:uploads/avatars/}")
    private String avatarBaseDir;

    private String toFileLocation(String path) {
        // Ensure ResourceHandler gets a file URI with trailing slash
        String uri = java.nio.file.Paths.get(path).toAbsolutePath().normalize().toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://auctionfrontend-production.up.railway.app")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String absoluteUploadPath = toFileLocation(avatarBaseDir);
                // Serve uploaded avatars from filesystem under /avatars/**
                registry.addResourceHandler("/avatars/**")
                        .addResourceLocations(absoluteUploadPath)
                        .setCachePeriod(3600);
            }
        };
    }
}
