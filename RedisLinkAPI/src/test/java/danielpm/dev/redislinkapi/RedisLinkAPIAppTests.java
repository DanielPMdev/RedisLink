package danielpm.dev.redislinkapi;

import danielpm.dev.redislinkapi.config.RedisConfig;
import danielpm.dev.redislinkapi.controller.UrlController;
import danielpm.dev.redislinkapi.repository.UrlRepository;
import danielpm.dev.redislinkapi.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests de contexto de la aplicación")
class RedisLinkAPIAppTests {

    private final ApplicationContext context;

    public RedisLinkAPIAppTests(ApplicationContext context) {
        this.context = context;
    }

    @Test
    @DisplayName("El contexto de Spring debe cargar correctamente")
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    @DisplayName("Debe cargar todos los beans principales")
    void shouldLoadMainBeans() {
        // Verificar que todos los componentes principales se crearon
        assertThat(context.getBean(UrlController.class)).isNotNull();
        assertThat(context.getBean(UrlService.class)).isNotNull();
        assertThat(context.getBean(UrlRepository.class)).isNotNull();
        assertThat(context.getBean(StringRedisTemplate.class)).isNotNull();
        assertThat(context.getBean(RedisConfig.class)).isNotNull();
    }

    @Test
    @DisplayName("Debe tener configuración de Redis")
    void shouldHaveRedisConfiguration() {
        assertThat(context.containsBean("redisTemplate")).isTrue();
        assertThat(context.containsBean("longRedisTemplate")).isTrue();
    }

    @Test
    @DisplayName("El número total de beans debe ser mayor que 0")
    void shouldHaveBeans() {
        String[] beanNames = context.getBeanDefinitionNames();
        assertThat(beanNames).isNotEmpty();

        //System.out.println("Total beans: " + beanNames.length);
    }
}