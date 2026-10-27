---
name: sync-canonical
description: >-
  Synchronize the spring-petclinic-langchain4j repository with the original canonical
  spring-projects/spring-petclinic repository. Make sure to use this skill whenever the
  user asks to sync or merge changes from the original/canonical/initial Spring Petclinic
  repository, align with upstream Petclinic commits, upgrade Spring Boot by pulling from
  canonical/main, or resolve merge conflicts between canonical Petclinic and this
  LangChain4j fork.
---

# Synchronize with Canonical Spring Petclinic (`sync-canonical`)

This skill guides you through synchronizing `spring-petclinic-langchain4j` with the official [spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic) repository (`canonical`) while preserving all LangChain4j chatbot features, build configurations, and test setups.

## Context & Goal

`spring-petclinic-langchain4j` is a fork of the canonical `spring-petclinic` sample application that integrates a Generative AI chatbot powered by LangChain4j. Periodically, we merge `canonical/main` into this repository to inherit Spring Boot upgrades, dependency updates, bug fixes, and UI/domain improvements from the official project without losing any LangChain4j customizations.

---

## Step-by-Step Workflow

### 1. Prepare Remotes and Branch

1. Verify the git remotes and ensure `canonical` points to `spring-projects/spring-petclinic`:
   ```bash
   git remote -v
   ```
   If `canonical` is missing, add it:
   ```bash
   git remote add canonical https://github.com/spring-projects/spring-petclinic.git
   ```
2. Fetch the latest changes from `canonical` and `upstream`:
   ```bash
   git fetch --multiple canonical upstream
   ```
3. Inspect what changed in `canonical/main` since the last synchronization:
   ```bash
   git log HEAD..canonical/main --oneline
   git diff HEAD...canonical/main --stat
   ```
   Look out for:
   - Spring Boot version upgrades (e.g., `3.4.x`, `4.0.x`, `4.1.x`)
   - Domain or repository refactoring (e.g., changes to `OwnerRepository`, `PetRepository`, `VetRepository`, or entity classes)
   - Build plugin updates in `pom.xml` and `build.gradle`
   - Test infrastructure changes or **newly added `@SpringBootTest` classes** (which won't trigger merge conflicts on their own, but will require `@ActiveProfiles("test")`)
4. Create and switch to a dedicated synchronization branch from an up-to-date `main`:
   ```bash
   git checkout -b feature/sync-spring-boot-<version> main
   ```
   *(Or `feature/sync-canonical` if not tied to a specific Spring Boot upgrade; check `git branch -a` first in case `feature/sync-canonical` already exists).*

---

### 2. Merge `canonical/main` Without Auto-Committing

Run the merge with `--no-commit` so you can inspect both conflicted and auto-merged files before finalizing:
```bash
git merge canonical/main --no-commit --no-ff
```

---

### 3. Resolve Conflicts & Preserve LangChain4j Invariants

When resolving conflicts and reviewing merged files, adopt all upstream upgrades from `canonical/main` while preserving the following `spring-petclinic-langchain4j` invariants. Understanding *why* each customization exists ensures you adapt cleanly even if `canonical` refactors surrounding code:

#### A. Maven Build (`pom.xml`)
- **Project Coordinates**: Keep `<artifactId>spring-petclinic-langchain4j</artifactId>` (do not revert to `spring-petclinic`) and omit redundant `<version>` if inherited or keep it aligned with the parent version.
- **Parent & Plugin Versions**: Adopt the new `<parent>` (`spring-boot-starter-parent`) version and tool/plugin versions (`checkstyle`, `jacoco`, `lifecycle-mapping`, `maven-checkstyle`, `nohttp-checkstyle`, `spring-format`, webjars) from `canonical`.
- **LangChain4j & Webjar Properties**: Preserve `<webjars-marked.version>` (used by the chat UI to render Markdown responses) and `<langchain4j.version>`.
- **Dependency Management**: Preserve the `<dependencyManagement>` section importing `dev.langchain4j:langchain4j-bom:${langchain4j.version}`.
- **Dependencies**:
  - Preserve the LangChain4j dependencies (`langchain4j-spring-boot-starter`, `langchain4j-embeddings-all-minilm-l6-v2`, `langchain4j-http-client-spring-boot4-restclient`, `langchain4j-open-ai-spring-boot4-starter`).
  - Preserve `org.webjars.npm:marked:${webjars-marked.version}`.
  - Preserve `org.testcontainers:testcontainers-postgresql` in `test` scope (required because `PostgresIntegrationTests` uses `PostgreSQLContainer` directly instead of Docker Compose).
- **LLM Maven Profiles**: Preserve the three profiles at the bottom of `pom.xml`: `openai` (active by default), `azure-openai`, and `ollama`.

#### B. Gradle Build (`build.gradle`)
- **Versions**: Update the `org.springframework.boot` plugin version, project `version`, and `ext.*` dependency versions to match `canonical`.
- **Disable Native & AOT Tasks**: Keep the `org.graalvm.buildtools.native` plugin commented out (`// id 'org.graalvm.buildtools.native' ...`) as well as the `checkstyleAot*`, `checkFormatAot*`, and `formatAot*` task configurations at the bottom of `build.gradle`. LangChain4j is not configured for GraalVM Native AOT in this Gradle setup, and enabling the native plugin triggers AOT tasks that fail or conflict.
- **Formatting Plugin**: Keep `apply plugin: 'io.spring.javaformat'` (along with `java` and `checkstyle`).
- **LangChain4j & Extra Dependencies**:
  - Preserve `ext.webjarsMarkedVersion` and `ext.langchain4jVersion`.
  - Preserve the `dependencyManagement` block importing `dev.langchain4j:langchain4j-bom:${langchain4jVersion}`.
  - Preserve the `dev.langchain4j:*` starters, `io.projectreactor:reactor-core` (needed for streaming / AOT compatibility), `org.webjars.npm:marked`, and `org.testcontainers:testcontainers-postgresql`.

#### C. Domain Entities, Repositories & `chat/` Package
- **Serialization (`serialVersionUID`)**: Preserve `private static final long serialVersionUID` across `BaseEntity`, `NamedEntity`, `Person`, `Owner`, `Pet`, `PetType`, `Visit`, `Specialty`, `Vet`, and anonymous `PetType` subclasses in `PetTypeFormatterTests`.
- **RAG Serialization on `Vet.java`**: `EmbeddingStoreInit` serializes `Vet` entities to JSON using Jackson (`JsonMapper`) to ingest veterinarian data into the in-memory embedding store at startup. Therefore, in `Vet.java`, preserve:
  - `@JsonProperty("specialties")` and `@JsonSerialize(as = ArrayList.class)` on `getSpecialties()` (replacing `@XmlElement` if canonical touches it)
  - `@JsonIgnore` on `getNrOfSpecialties()` and `addSpecialty(Specialty)`
- **`VetRepository.java` & `VetController.java`**:
  - Preserve `Integer countVets()` in `VetRepository` (`@Query("SELECT COUNT(v) FROM Vet v")`).
  - Preserve `int pageSize = 20;` in `VetController.findPaginated` (increased from 5 so the enlarged vet dataset displays comfortably).
- **Adapting `chat/` to Domain Changes**: If `canonical` changes method signatures in `OwnerRepository`, `VetRepository`, or domain entities (such as `Owner`, `Pet`, `Visit`), update `src/main/java/org/springframework/samples/petclinic/chat/AssistantTool.java` and any other `chat/` classes to match the new APIs.

#### D. Configuration, UI & Dataset
- **`src/main/resources/application.properties`**: Preserve the `logging.level.dev.langchain4j=DEBUG`, `logging.level.dev.ai4j.openai4j=DEBUG`, `logging.level.org.springframework.samples.petclinic=DEBUG` settings and all `langchain4j.azure-open-ai.*`, `langchain4j.open-ai.*`, and `langchain4j.ollama.*` configuration blocks.
- **Chatbox UI (`layout.html`, `petclinic.scss`, `petclinic.css`, `.gitattributes`, `.gitignore`)**:
  - In `src/main/resources/templates/fragments/layout.html`, preserve `<link rel="stylesheet" th:href="@{/resources/css/chat.css}" />`, the `<div class="chatbox" id="chatbox">...</div>` markup, `<script th:src="@{/webjars/marked/.../marked.min.js}"></script>`, and `<script th:src="@{/resources/js/chat.js}"></script>`. If `webjars-marked.version` changed, update the script path in `layout.html` accordingly.
  - Preserve the custom SCSS/CSS rules in `src/main/scss/petclinic.scss` and `src/main/resources/static/resources/css/petclinic.css`.
- **Enriched H2 Dataset (`data.sql` & `ClinicServiceTests.java`)**:
  - `src/main/resources/db/h2/data.sql` contains an expanded set of vets, owners, pets, and visits tailored for rich chatbot & RAG interactions. Preserve this dataset.
  - Because `data.sql` differs from canonical, keep the adjusted vet assertions in `ClinicServiceTests.shouldFindVets()` (checking vet ID 2 "Leary" with "anesthesia" and "dentistry").
- **`README.md`**: Keep the LangChain4j introduction, architecture explanation, LLM setup instructions, and `spring-petclinic/spring-petclinic-langchain4j` URLs, while integrating any relevant updates from `canonical` (such as updated MySQL/PostgreSQL container image tags or Java version notes).

#### E. Integration Tests
- **`@ActiveProfiles("test")` on all `@SpringBootTest` classes**: All `@SpringBootTest` integration tests (`PetClinicIntegrationTests`, `PetClinicConcurrencyTests`, `MySqlIntegrationTests`, `PostgresIntegrationTests`, `CrashControllerIntegrationTests`, plus any newly added `@SpringBootTest` class from `canonical`) must include the `"test"` profile (e.g., `@ActiveProfiles("test")` or `@ActiveProfiles(profiles = { "mysql", "test" })`). This activates `src/test/resources/application-test.properties`, which configures `OPENAI_API_KEY=demo` so the Spring context can initialize the LangChain4j beans during tests without requiring a personal OpenAI API key.
  - **Tip**: Always check newly added test files in `src/test/java` for `@SpringBootTest` because new files are auto-merged without conflict markers and will fail at test runtime if `@ActiveProfiles("test")` is missing.
- **`PostgresIntegrationTests.java`**: Preserve the `@Testcontainers(disabledWithoutDocker = true)` + `@ServiceConnection` + `PostgreSQLContainer` implementation rather than canonical's `spring.docker.compose` approach, as it runs reliably in isolation alongside the `"test"` profile.

---

### 4. Format, Verify, and Validate Both Builds

`spring-petclinic` enforces strict formatting (`spring-javaformat`) and Checkstyle rules (including `nohttp` and import ordering). Always run formatting before running tests:

1. **Apply Spring Java Format**:
   ```bash
   ./mvnw spring-javaformat:apply
   ```
   *(Or `./gradlew format` if working with Gradle — do not combine `format` and `check` in the same `./gradlew format check` invocation on Gradle 9+, as Gradle task validation rejects `:checkFormatMain` reading `:formatMain`'s output without an explicit task dependency).*
2. **Run Maven verification (Checkstyle + Tests)**:
   ```bash
   ./mvnw test
   ```
3. **Run Gradle verification (CheckFormat + Checkstyle + Tests)**:
   ```bash
   ./gradlew check
   ```
4. **Final Diff Sanity Check**:
   Compare the working tree against `canonical/main` to ensure only expected LangChain4j additions and customizations differ:
   ```bash
   git diff canonical/main --stat
   ```

---

### 5. Commit, Push, and Create the Pull Request

1. **Stage and Commit the Merge**:
   Once both Maven and Gradle builds succeed cleanly:
   ```bash
   git add -u
   git commit --no-edit
   ```
2. **Push the Synchronization Branch to `origin`**:
   Push the branch to your fork (`origin`):
   ```bash
   git push -u origin <branch-name>
   ```
3. **Create the Pull Request Against `spring-petclinic/spring-petclinic-langchain4j`**:
   Use the GitHub CLI (`gh`) or REST API to open the PR against `spring-petclinic/spring-petclinic-langchain4j` (`main` branch), summarizing the Spring Boot upgrade and main changes brought in from `canonical/main`:
   ```bash
   gh pr create \
     --repo spring-petclinic/spring-petclinic-langchain4j \
     --base main \
     --head arey:<branch-name> \
     --title "Sync with the original Spring Boot <version> version" \
     --body "<summary of canonical changes and preserved LangChain4j invariants>"
   ```
