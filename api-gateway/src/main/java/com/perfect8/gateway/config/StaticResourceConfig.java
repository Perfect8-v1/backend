package com.perfect8.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Serverar Flutter web-app från /app/flutter/ i containern.
 * 
 * API-routes (/api/**, /shop/**, /blog/**, etc.) hanteras av Gateway routing.
 * Denna config serverar endast Flutter-specifika filer.
 */
@Configuration
public class StaticResourceConfig {

    private static final String FLUTTER_DIR = "/app/flutter";

    @Bean
    public RouterFunction<ServerResponse> flutterStaticRouter() {
        return route(GET("/"), this::serveIndex)
                .andRoute(GET("/index.html"), this::serveIndex)
                .andRoute(GET("/main.dart.js"), req -> serveFile("main.dart.js", "application/javascript"))
                .andRoute(GET("/flutter.js"), req -> serveFile("flutter.js", "application/javascript"))
                .andRoute(GET("/flutter_bootstrap.js"), req -> serveFile("flutter_bootstrap.js", "application/javascript"))
                .andRoute(GET("/flutter_service_worker.js"), req -> serveFile("flutter_service_worker.js", "application/javascript"))
                .andRoute(GET("/manifest.json"), req -> serveFile("manifest.json", "application/json"))
                .andRoute(GET("/favicon.png"), req -> serveFile("favicon.png", "image/png"))
                .andRoute(GET("/icons/**"), this::serveAsset)
                .andRoute(GET("/assets/**"), this::serveAsset)
                .andRoute(GET("/canvaskit/**"), this::serveAsset);
    }

    private Mono<ServerResponse> serveIndex(ServerRequest request) {
        return serveFile("index.html", "text/html");
    }

    private Mono<ServerResponse> serveFile(String filename, String contentType) {
        File file = new File(FLUTTER_DIR + "/" + filename);
        if (file.exists() && file.isFile()) {
            Resource resource = new FileSystemResource(file);
            return ServerResponse.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(resource);
        }
        return ServerResponse.notFound().build();
    }

    private Mono<ServerResponse> serveAsset(ServerRequest request) {
        String path = request.path();
        File file = new File(FLUTTER_DIR + path);
        
        if (file.exists() && file.isFile()) {
            String contentType = guessContentType(path);
            Resource resource = new FileSystemResource(file);
            return ServerResponse.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(resource);
        }
        return ServerResponse.notFound().build();
    }

    private String guessContentType(String path) {
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".woff")) return "font/woff";
        if (path.endsWith(".woff2")) return "font/woff2";
        if (path.endsWith(".ttf")) return "font/ttf";
        if (path.endsWith(".wasm")) return "application/wasm";
        return "application/octet-stream";
    }
}
