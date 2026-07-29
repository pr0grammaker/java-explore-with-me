//package json;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.json.JsonTest;
//import org.springframework.boot.test.json.JacksonTester;
//import org.springframework.boot.test.json.JsonContent;
//import org.springframework.test.context.ContextConfiguration;
//import ru.practicum.StatApplicationServer;
//import ru.practicum.dto.ViewStats;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@JsonTest
//@ContextConfiguration(classes = StatApplicationServer.class)
//class ViewStatsJsonTest {
//
//    @Autowired
//    private JacksonTester<ViewStats> json;
//
//    @Test
//    void serializeViewStatsTest() throws Exception {
//        ViewStats stats = new ViewStats("stats-service", "/items/42", 5L);
//
//        JsonContent<ViewStats> result = json.write(stats);
//
//        String expectedJson = """
//                {"app":"stats-service","uri":"/items/42","hits":5}""";
//
//
//        assertThat(result).isEqualToJson(expectedJson);
//    }
//
//    @Test
//    void deserializeViewStatsTest() throws Exception {
//        String content = """
//                {"app":"stats-service","uri":"/items/42","hits":5}""";
//
//
//        ViewStats parsed = json.parseObject(content);
//
//        assertThat(parsed.getApp()).isEqualTo("stats-service");
//        assertThat(parsed.getUri()).isEqualTo("/items/42");
//        assertThat(parsed.getHits()).isEqualTo(5L);
//    }
//}
//
