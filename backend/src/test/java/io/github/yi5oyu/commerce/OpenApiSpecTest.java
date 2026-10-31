package io.github.yi5oyu.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * backend/openapi.json을 코드와 일치시킨다.
 * 일반 테스트: 커밋된 명세와 다르면 실패 / `./gradlew openApiSpec`: 파일을 다시 쓴다.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=true",
        "springdoc.writer-with-default-pretty-printer=true",
        "springdoc.writer-with-order-by-keys=true"
})
class OpenApiSpecTest {

    private static final Path SPEC = Path.of("openapi.json");
    private static final String HINT = "./gradlew openApiSpec 실행 후 backend/openapi.json을 커밋하세요";

    @Autowired
    WebApplicationContext context;

    @Test
    void openApiSpecIsUpToDate() throws Exception {
        // Security 필터는 적용하지 않는다 — 명세를 읽을 뿐 접근 제어를 검사하지 않음
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        String spec = normalize(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        if (Boolean.getBoolean("openapi.update")) {
            Files.writeString(SPEC, spec);
            return;
        }

        assertThat(SPEC).as(HINT).exists();
        assertThat(normalize(Files.readString(SPEC))).as(HINT).isEqualTo(spec);
    }

    // Jackson pretty printer는 OS 줄바꿈을 쓰므로 LF로 고정해 Windows와 CI 결과를 맞춘다
    private static String normalize(String json) {
        return json.replace("\r\n", "\n").stripTrailing() + "\n";
    }
}
