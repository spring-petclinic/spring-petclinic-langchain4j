package org.springframework.samples.petclinic.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.UUID;

@AiService
interface Assistant {

	@SystemMessage(fromResource = "/prompts/system.st")
	TokenStream chat(@MemoryId UUID memoryId, @UserMessage String userMessage);

}
