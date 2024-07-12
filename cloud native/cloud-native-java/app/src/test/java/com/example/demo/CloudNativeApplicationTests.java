package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CloudNativeApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private CatRepository catRepository;

	@BeforeEach
	void before() throws Exception {
		Stream.of("Felix", "Garfield", "Whiskers")
				.forEach(n -> catRepository.save(new Cat(n)));
	}

	@Test
	void catsReflectedInRead() throws Exception {
		mvc
				.perform(get("/cats"))
				.andExpect(status().isOk())
				.andExpect(
						mvcResult -> {
							String contentAsString = mvcResult.getResponse().getContentAsString();
                            assertEquals("3", contentAsString.split("totalElements")[1]
                                    .split(":")[1].trim()
                                    .split(",")[0]);
							System.out.println(mvcResult.getResponse().getContentType());
						}
				);
	}

}
