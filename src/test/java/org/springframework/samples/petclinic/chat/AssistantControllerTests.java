package org.springframework.samples.petclinic.chat;

import dev.langchain4j.service.TokenStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(AssistantController.class)
class AssistantControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private Assistant assistant;

	@Test
	void preservesLineBreaksInStreamedMessages() throws Exception {
		TokenStream tokenStream = mock(TokenStream.class);
		given(this.assistant.chat(any(UUID.class), any())).willReturn(tokenStream);
		given(tokenStream.onPartialResponse(any())).willAnswer(invocation -> {
			Consumer<String> callback = invocation.getArgument(0);
			callback.accept("first line\nsecond line");
			return tokenStream;
		});
		given(tokenStream.onCompleteResponse(any())).willAnswer(invocation -> {
			Consumer<Object> callback = invocation.getArgument(0);
			callback.accept(null);
			return tokenStream;
		});
		given(tokenStream.onError(any())).willReturn(tokenStream);

		var result = this.mockMvc
			.perform(post("/chat/{user}", UUID.randomUUID()).contentType("application/json").content("\"question\""))
			.andExpect(request().asyncStarted())
			.andReturn();

		this.mockMvc.perform(asyncDispatch(result))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("first line\\nsecond line")));
	}

}
