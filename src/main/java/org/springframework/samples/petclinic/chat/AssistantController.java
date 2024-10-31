package org.springframework.samples.petclinic.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
class AssistantController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AssistantController.class);

	private final Assistant assistant;

	private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

	AssistantController(Assistant assistant) {
		this.assistant = assistant;
	}

	// Using the POST method due to chat memory capabilities
	@PostMapping(value = "/chat/{user}")
	public SseEmitter chat(@PathVariable UUID user, @RequestBody String query) {
		SseEmitter emitter = new SseEmitter();
		nonBlockingService.execute(() -> assistant.chat(user, query).onNext(message -> {
			try {
				sendMessage(emitter, message);
			}
			catch (IOException e) {
				LOGGER.error("Error while writing next token", e);
				emitter.completeWithError(e);
			}
		}).onComplete(token -> emitter.complete()).onError(error -> {
			LOGGER.error("Unexpected chat error", error);
			try {
				sendMessage(emitter, error.getMessage());
			}
			catch (IOException e) {
				LOGGER.error("Error while writing next token", e);
			}
			emitter.completeWithError(error);
		}).start());
		return emitter;
	}

	private static void sendMessage(SseEmitter emitter, String message) throws IOException {
		String token = message
			// Hack line break problem when using Server Sent Events (SSE)
			.replace("\n", "<br>")
			// Escape JSON quotes
			.replace("\"", "\\\"");
		emitter.send("{\"t\": \"" + token + "\"}");
	}

}
