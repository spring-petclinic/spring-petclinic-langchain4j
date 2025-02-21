package org.springframework.samples.petclinic.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Loads the veterinarians data into an Embedding Store for the purpose of RAG
 * functionality.
 *
 * @author Oded Shopen
 * @author Antoine Rey
 */
@Component
public class EmbeddingStoreInit {

	private final Logger logger = LoggerFactory.getLogger(EmbeddingStoreInit.class);

	private final InMemoryEmbeddingStore<TextSegment> embeddingStore;

	private final EmbeddingModel embeddingModel;

	private final VetRepository vetRepository;

	public EmbeddingStoreInit(InMemoryEmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
			VetRepository vetRepository) {
		this.embeddingStore = embeddingStore;
		this.embeddingModel = embeddingModel;
		this.vetRepository = vetRepository;
	}

	@EventListener
	public void loadVetDataToEmbeddingStoreOnStartup(ApplicationStartedEvent event) {
		Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
		Page<Vet> vetsPage = vetRepository.findAll(pageable);

		String vetsAsJson = convertListToJson(vetsPage.getContent());

		EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
			.documentSplitter(new DocumentByLineSplitter(1000, 200))
			.embeddingModel(embeddingModel)
			.embeddingStore(embeddingStore)
			.build();

		ingestor.ingest(Document.from(vetsAsJson));

		// In-memory embedding store can be serialized and deserialized to/from file
		// String filePath = "embedding.store";
		// embeddingStore.serializeToFile(filePath);
	}

	public String convertListToJson(List<Vet> vets) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// Convert List<Vet> to JSON string
			StringBuilder jsonArray = new StringBuilder();
			for (Vet vet : vets) {
				String jsonElement = objectMapper.writeValueAsString(vet);
				jsonArray.append(jsonElement).append("\n"); // For use of the
															// DocumentByLineSplitter
			}
			return jsonArray.toString();
		}
		catch (JsonProcessingException e) {
			logger.error("Problems encountered when generating JSON from the vets list", e);
			return null;
		}
	}

}
