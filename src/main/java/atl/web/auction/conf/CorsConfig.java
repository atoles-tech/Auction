package atl.web.auction.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 1. Помечаем класс как конфигурационный
public class CorsConfig implements WebMvcConfigurer { // 2. Реализуем интерфейс WebMvcConfigurer

    @Override
    public void addCorsMappings(CorsRegistry registry) { // 3. Переопределяем метод для настройки CORS
        registry
            .addMapping("/**") // Применить ко всем эндпоинтам (например, /api/**)
            .allowedOrigins("http://localhost:5173",  "http://localhost:80", "http://localhost") // Разрешить запросы с вашего фронтенда
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Разрешенные HTTP-методы
            .allowedHeaders("*") // Разрешить все заголовки
            .allowCredentials(true) // Разрешить отправку куки и авторизационных данных
            .maxAge(3600); // Кэшировать ответ на preflight-запрос (в секундах)
    }
}