package dev.portfolio.uptimemonitor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UptimeMonitorApplicationTests {
	@Autowired 
	private MockMvc mockMvc;

	@Test
	void helloEndpointReturnsExpectedMessage() throws Exception {
		mockMvc.perform(get("/api/hello")) 
				.andExpect(status().isOk()) 
				.andExpect(content().json(  
					"{\"message\":\"Uptime Monitor API is ready\"}"
				));

	}

}
