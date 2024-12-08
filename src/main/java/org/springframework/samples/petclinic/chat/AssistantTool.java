package org.springframework.samples.petclinic.chat;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Functions that are invoked by the LLM will use this bean to query the system of record
 * for information such as listing owners and vets, or adding pets to an owner.
 *
 * @author Oded Shopen
 * @author Antoine Rey
 */
@Component
public class AssistantTool {

	private final OwnerRepository ownerRepository;

	public AssistantTool(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * This tool is available to {@link Assistant}
	 */
	@Tool("current date, today")
	String currentDate() {
		return LocalDate.now().toString();
	}

	@Tool("List the owners that the pet clinic has: ownerId, name, address, phone number, pets")
	public OwnersResponse getAllOwners() {
		Pageable pageable = PageRequest.of(0, 100);
		Page<Owner> ownerPage = ownerRepository.findAll(pageable);
		return new OwnersResponse(ownerPage.getContent());
	}

	@Tool("Add a pet with the specified petTypeId, to an owner identified by the ownerId")
	public AddedPetResponse addPetToOwner(AddPetRequest request) {
		Owner owner = ownerRepository.findById(request.ownerId()).orElseThrow();
		// Waiting for https://github.com/langchain4j/langchain4j/issues/2249
		Pet pet = request.pet();
		pet.setName(request.petName());
		owner.addPet(pet);
		this.ownerRepository.save(owner);
		return new AddedPetResponse(owner);
	}

	@Tool("List all pairs of petTypeId and pet type name")
	public List<PetType> populatePetTypes() {
		return this.ownerRepository.findPetTypes();
	}

	@Tool("""
			Add a new pet owner to the pet clinic. \
			The Owner must include a first name and a last name as two separate words, \
			plus an address and a 10-digit phone number""")
	public OwnerResponse addOwnerToPetclinic(OwnerRequest ownerRequest) {
		ownerRepository.save(ownerRequest.owner());
		return new OwnerResponse(ownerRequest.owner());
	}

}

record AddPetRequest(Pet pet, String petName, Integer ownerId) {
}

record OwnerRequest(Owner owner) {
}

record OwnersResponse(List<Owner> owners) {
}

record OwnerResponse(Owner owner) {
}

record AddedPetResponse(Owner owner) {
}

record VetResponse(List<String> vet) {
}

record VetRequest(Vet vet) {
}
