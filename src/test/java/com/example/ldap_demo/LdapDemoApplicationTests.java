package com.example.ldap_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LdapDemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void redirectsUnauthenticatedBrowserRequestToLogin() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "/login"));
	}

	@Test
	void returnsUnauthorizedJsonForUnauthenticatedApiRequest() throws Exception {
		mockMvc.perform(get("/api/me").accept(APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
				.andExpect(content().json("{\"error\":\"unauthorized\"}"));
	}
}
