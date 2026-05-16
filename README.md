# GenAI Spring PetClinic Sample Application build with LangChain4j [![Build Status](https://github.com/spring-petclinic/spring-petclinic-langchain4j/actions/workflows/maven-build.yml/badge.svg)](https://github.com/spring-petclinic/spring-petclinic-langchain4j/actions/workflows/maven-build.yml)[![Build Status](https://github.com/spring-petclinic/spring-petclinic-langchain4j/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/spring-petclinic/spring-petclinic-langchain4j/actions/workflows/gradle-build.yml)

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/spring-petclinic/spring-petclinic-langchain4j) [![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=875544168)

## Understanding the Spring Petclinic LangChain4j application

A chatbot using **Generative AI** has been added to the famous Spring Petclinic application.
This version uses the **[LangChain4j project](https://docs.langchain4j.dev/)** and currently supports **OpenAI** or **Azure's OpenAI** or **Ollama** (partial) as the **LLM provider**.
This is a fork from the **[spring-petclinic-ai](https://github.com/spring-petclinic/spring-petclinic-ai)** based on Spring AI.

This sample demonstrates how to **easily integrate AI/LLM capabilities into a Java application using LangChain4j**.
This can be achieved thanks to:
* A unified **abstraction layer** designed to decouple your code from specific implementations like LLM or embedding providers, enabling easy component swapping.
  Only the [application.properties](src/main/resources/application.properties) file references LLM providers such as OpenAI or Azure OpenAI.
* **Memory** offers context to the LLM for both your current and previous conversations, with support for multiple users.
  Refer to the use of the `MessageWindowChatMemory` class in [AssistantConfiguration](src/main/java/org/springframework/samples/petclinic/chat/AssistantConfiguration.java)
  and the `@MemoryId` annotation in the [Assistant](src/main/java/org/springframework/samples/petclinic/chat/Assistant.java) interface.
* **AI Services** enables declarative definitions of complex AI behaviors through a straightforward Java API.
  See the use of the `@AiService` annotation in the [Assistant](src/main/java/org/springframework/samples/petclinic/chat/Assistant.java) interface.
* **System prompts** play a vital role in LLMs as they shape how models interpret and respond to user queries.
  Look at the `@SystemMessage` annotation usage in the [Assistant](src/main/java/org/springframework/samples/petclinic/chat/Assistant.java) interface.
* **Streaming** response token-by-token when using the `TokenStream` return type and Spring *Server-Sent Events* supports.
  Take a look at the [AssistantController](src/main/java/org/springframework/samples/petclinic/chat/AssistantController.java) REST controller
* **Function calling** or **Tools** allows the LLM to call, when necessary, one or more java methods.
  The [AssistantTool](src/main/java/org/springframework/samples/petclinic/chat/AssistantTool.java) component declares functions using the `@Tool` annotation from LangChain4j.
* **Structured outputs** allow LLM responses to be received in a specified format as Java POJOs.
  [AssistantTool](src/main/java/org/springframework/samples/petclinic/chat/AssistantTool.java) uses Java records as the LLM/ input/output data structure.
* **Retrieval-Augmented Generation** (RAG) enables an LLM to incorporate and respond based on specific data—such as data from the petclinic database—by ingesting and referencing it during interactions.
  The [AssistantConfiguration](src/main/java/org/springframework/samples/petclinic/chat/AssistantConfiguration.java) declares the `EmbeddingModel`, `InMemoryEmbeddingStore` and `EmbeddingStoreContentRetriever`beans while the [EmbeddingStoreInit](src/main/java/org/springframework/samples/petclinic/chat/EmbeddingStoreInit.java) class handles vets data ingestion at startup. 
  The [VetQueryRouter](src/main/java/org/springframework/samples/petclinic/chat/VetQueryRouter.java) demonstrates how to conditionally skip retrieval, with decision-making driven by an LLM.

The French blog post [Integrating a chatbot into a Java webapp with LangChain4j](https://javaetmoi.com/2024/11/integrer-un-chatbot-dans-une-webapp-java-avec-langchain4j/) provides
a detailed explanation of the integration of the integration of LangChain4j into the Spring Petclinic application.

Spring Petclinic integrates a Chatbot that allows you to interact with the application in a natural language.
Here are **some examples** of what you could ask:

1. Please list the owners that come to the clinic.
2. How many veterinary cardiologists are there?
3. Is there an owner named Betty? What's her lastname?
4. Which owners have dogs?
5. Add a dog for Betty. Its name is Moopsie. His birthday is on 2 October 2024.
6. Add today's visit to Moopsie.

![Screenshot of the chat dialog](docs/chat-dialog.png)

## Choosing the LLM provider

Spring Petclinic currently supports **OpenAI** or **Azure's OpenAI** or **Ollama** (partial support) as the LLM provider.
**OpenAI** is the **default**.

Please note that the Spring Petclinic is not fully functional with the `llama3.1` model.
See the issue [#10](https://github.com/spring-petclinic/spring-petclinic-langchain4j/issues/10 ) for more information.

### 1. Use the selected LangChain4j Spring Boot starter

Spring Petclinic supports both `Maven` and `Gradle` build tools.

#### Maven build

Switching between LLM is done using **Maven profiles**. Three Maven profiles are provided: 
1. `openai` (default)
2. `azure-openai`
3. `ollama`

By default, thanks to the default `openai` profile, the `langchain4j-open-ai-spring-boot4-starter` dependency is enabled.
You can change it to `langchain4j-azure-open-ai-spring-boot-starter` or `langchain4j-ollama-spring-boot4-starter` by activating the corresponding profile.
```shell
./mvnw package -P azure-openai
```
`in either`pom.xml` or in `build.gradle`, depending on your build tool of choice.

#### Gradle build

Gradle users will need to comment or uncomment the appropriate `dev.langchain4j:langchain4j-<llm>>-spring-boot4-starter` dependency
in the `build.gradle` file, depending on the LLM provider they want to use.


### 2. Setup your LLM provider
   
#### OpenAI

Create an OpenAI API key by following the [OpenAI's quickstart](https://platform.openai.com/docs/quickstart).
If you don't have your own OpenAI API key, don't worry!
You can temporarily use the `demo` key, which OpenAI provides free of charge for demonstration purposes.
This `demo` key has a quota, is limited to the gpt-4o-mini model, and is intended solely for demonstration use.

Export your OpenAI API key as environment variable:
```bash
export OPENAI_API_KEY="your_api_key_here"
 ```

#### Azure OpenAI

Create a Azure OpenAI resource in your Azure Portal.
Refer to the [Azure's documentation](https://learn.microsoft.com/en-us/azure/ai-services/openai/) for further information on how to obtain these.

Then export your API keys and endpoint as environment variables:
```bash
export AZURE_OPENAI_ENDPOINT="https://your_resource.openai.azure.com"
export AZURE_OPENAI_KEY="your_api_key_here"
```

#### Ollama

Download the Ollama client from the [Ollama website](https://ollama.com/).
Run the `llama3.1` model:
```shell
ollama run llama3.1
```
By default, the Ollama REST API starts on `http://localhost:11434`. This URL is used in the `application.properties` file.

See the presentation here:  
[Spring Petclinic Sample Application (legacy slides)](https://speakerdeck.com/michaelisvy/spring-petclinic-sample-application?slide=20)

> **Note:** These slides refer to a legacy, pre–Spring Boot version of Petclinic and may not reflect the current Spring Boot–based implementation.  
> For up-to-date information, please refer to this repository and its documentation.


## Run Petclinic locally

Spring Petclinic is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Maven](https://spring.io/guides/gs/maven/) or [Gradle](https://spring.io/guides/gs/gradle/).
Java 17 or later is required for the build, and the application can run with Java 17 or newer:

```bash
git clone https://github.com/spring-petclinic/spring-petclinic-langchain4j.git
cd spring-petclinic
./mvnw package
java -jar target/*.jar
```

(On Windows, or if your shell doesn't expand the glob, you might need to specify the JAR file name explicitly on the command line at the end there.)

You can then access the Petclinic at <http://localhost:8080/>.

![Screenshot of the Find Owners menu](docs/find-owners-screenshot.png)

Or you can run it from Maven directly using the Spring Boot Maven plugin. If you do this, it will pick up changes that you make in the project immediately (changes to Java source files require a compile as well - most people use an IDE for this):

```bash
./mvnw spring-boot:run
```

> NOTE: If you prefer to use Gradle, you can build the app using `./gradlew build` and look for the jar file in `build/libs`.

## Building a Container

There is no `Dockerfile` in this project. You can build a container image (if you have a docker daemon) using the Spring Boot build plugin:

```bash
./mvnw spring-boot:build-image
```

## In case you find a bug/suggested improvement for Spring Petclinic

Our issue tracker is available [here](https://github.com/spring-petclinic/spring-petclinic-langchain4j/issues).

## Database configuration

In its default configuration, Petclinic uses an in-memory database (H2) which
gets populated at startup with data. The h2 console is exposed at `http://localhost:8080/h2-console`,
and it is possible to inspect the content of the database using the `jdbc:h2:mem:<uuid>` URL. The UUID is printed at startup to the console.

A similar setup is provided for MySQL and PostgreSQL if a persistent database configuration is needed. Note that whenever the database type changes, the app needs to run with a different profile: `spring.profiles.active=mysql` for MySQL or `spring.profiles.active=postgres` for PostgreSQL. See the [Spring Boot documentation](https://docs.spring.io/spring-boot/how-to/properties-and-configuration.html#howto.properties-and-configuration.set-active-spring-profiles) for more detail on how to set the active profile.

You can start MySQL or PostgreSQL locally with whatever installer works for your OS or use docker:

```bash
docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:9.5
```

or

```bash
docker run -e POSTGRES_USER=petclinic -e POSTGRES_PASSWORD=petclinic -e POSTGRES_DB=petclinic -p 5432:5432 postgres:18.1
```

Further documentation is provided for [MySQL](https://github.com/spring-petclinic/spring-petclinic-langchain4j/blob/main/src/main/resources/db/mysql/petclinic_db_setup_mysql.txt)
and [PostgreSQL](https://github.com/spring-petclinic/spring-petclinic-langchain4j/blob/main/src/main/resources/db/postgres/petclinic_db_setup_postgres.txt).

Instead of vanilla `docker` you can also use the provided `docker-compose.yml` file to start the database containers. Each one has a service named after the Spring profile:

```bash
docker compose up mysql
```

or

```bash
docker compose up postgres
```

## Test Applications

At development time we recommend you use the test applications set up as `main()` methods in `PetClinicIntegrationTests` (using the default H2 database and also adding Spring Boot Devtools), `MySqlTestApplication` and `PostgresIntegrationTests`. These are set up so that you can run the apps in your IDE to get fast feedback and also run the same classes as integration tests against the respective database. The MySql integration tests use Testcontainers to start the database in a Docker container, and the Postgres tests use Docker Compose to do the same thing.

## Compiling the CSS

There is a `petclinic.css` in `src/main/resources/static/resources/css`. It was generated from the `petclinic.scss` source, combined with the [Bootstrap](https://getbootstrap.com/) library. If you make changes to the `scss`, or upgrade Bootstrap, you will need to re-compile the CSS resources using the Maven profile "css", i.e. `./mvnw package -P css`. There is no build profile for Gradle to compile the CSS.

## Working with Petclinic in your IDE

### Prerequisites

The following items should be installed in your system:

- Java 25 or newer (full JDK, not a JRE)
- [Git command line tool](https://help.github.com/articles/set-up-git)
- Your preferred IDE
  - Eclipse with the m2e plugin. Note: when m2e is available, there is an m2 icon in `Help -> About` dialog. If m2e is
  not there, follow the install process [here](https://www.eclipse.org/m2e/)
  - [Spring Tools Suite](https://spring.io/tools) (STS)
  - [IntelliJ IDEA](https://www.jetbrains.com/idea/)
  - [VS Code](https://code.visualstudio.com)

### Steps

1. On the command line run:

    ```bash
    git clone https://github.com/spring-petclinic/spring-petclinic-langchain4j.git
    ```

1. Inside Eclipse or STS:

    Open the project via `File -> Import -> Maven -> Existing Maven project`, then select the root directory of the cloned repo.

    Then either build on the command line `./mvnw generate-resources` or use the Eclipse launcher (right-click on project and `Run As -> Maven install`) to generate the CSS. Run the application's main method by right-clicking on it and choosing `Run As -> Java Application`.

1. Inside IntelliJ IDEA:

    In the main menu, choose `File -> Open` and select the Petclinic [pom.xml](pom.xml). Click on the `Open` button.

    - CSS files are generated from the Maven build. You can build them on the command line `./mvnw generate-resources` or right-click on the `spring-petclinic` project then `Maven -> Generates sources and Update Folders`.

    - A run configuration named `PetClinicApplication` should have been created for you if you're using a recent Ultimate version. Otherwise, run the application by right-clicking on the `PetClinicApplication` main class and choosing `Run 'PetClinicApplication'`.

1. Navigate to the Petclinic

    Visit [http://localhost:8080](http://localhost:8080) in your browser.

## Looking for something in particular?

|Spring Boot Configuration | Class or Java property files  |
|--------------------------|---|
|The Main Class | [PetClinicApplication](https://github.com/spring-petclinic/spring-petclinic-langchain4j/blob/main/src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java) |
|Properties Files | [application.properties](https://github.com/spring-petclinic/spring-petclinic-langchain4j/blob/main/src/main/resources) |
|Caching | [CacheConfiguration](https://github.com/spring-petclinic/spring-petclinic-langchain4j/blob/main/src/main/java/org/springframework/samples/petclinic/system/CacheConfiguration.java) |

## Interesting Spring Petclinic branches and forks

The Spring Petclinic "main" branch in the [spring-projects](https://github.com/spring-projects/spring-petclinic)
GitHub org is the "canonical" implementation based on Spring Boot and Thymeleaf. There are
[quite a few forks](https://spring-petclinic.github.io/docs/forks.html) in the GitHub org
[spring-petclinic](https://github.com/spring-petclinic). If you are interested in using a different technology stack to implement the Pet Clinic, please join the community there.

## Interaction with other open-source projects

One of the best parts about working on the Spring Petclinic application is that we have the opportunity to work in direct contact with many Open Source projects. We found bugs/suggested improvements on various topics such as Spring, Spring Data, Bean Validation and even Eclipse! In many cases, they've been fixed/implemented in just a few days.
Here is a list of them:

| Name | Issue |
|------|-------|
| Spring JDBC: simplify usage of NamedParameterJdbcTemplate | [SPR-10256](https://github.com/spring-projects/spring-framework/issues/14889) and [SPR-10257](https://github.com/spring-projects/spring-framework/issues/14890) |
| Bean Validation / Hibernate Validator: simplify Maven dependencies and backward compatibility |[HV-790](https://hibernate.atlassian.net/browse/HV-790) and [HV-792](https://hibernate.atlassian.net/browse/HV-792) |
| Spring Data: provide more flexibility when working with JPQL queries | [DATAJPA-292](https://github.com/spring-projects/spring-data-jpa/issues/704) |

## Contributing

The [issue tracker](https://github.com/spring-petclinic/spring-petclinic-langchain4j/issues) is the preferred channel for bug reports, feature requests and submitting pull requests.

For pull requests, editor preferences are available in the [editor config](.editorconfig) for easy use in common text editors. Read more and download plugins at <https://editorconfig.org>. All commits must include a __Signed-off-by__ trailer at the end of each commit message to indicate that the contributor agrees to the Developer Certificate of Origin.
For additional details, please refer to the blog post [Hello DCO, Goodbye CLA: Simplifying Contributions to Spring](https://spring.io/blog/2025/01/06/hello-dco-goodbye-cla-simplifying-contributions-to-spring).

## License

The Spring PetClinic sample application is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
