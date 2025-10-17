# Gerenciador de Reservas de Salas

Um sistema simples para gerenciar e agendar reservas de salas, desenvolvido com Spring Boot.

## Como Executar o Projeto

1.  **Pré-requisitos:** É necessário ter o Java (JDK 17 ou superior) instalado.
2.  **Execute o comando:** Navegue até a pasta raiz do projeto no seu terminal e execute o seguinte comando:

    ```bash
    ./mvnw spring-boot:run
    ```

    O sistema iniciará e estará pronto para uso quando a mensagem `Started ReservasApplication` aparecer no console.

## Como Acessar a Aplicação

Após iniciar o projeto, abra o seu navegador e acesse a seguinte URL:

- **Página Principal:** `http://localhost:8080/`

## Como Acessar o Banco de Dados (H2 Console)

O projeto utiliza um banco de dados em memória (H2) que pode ser acessado através de uma interface web.

1.  **Acesse a URL:**

    - `http://localhost:8080/h2-console`

2.  **Use as seguintes credenciais para conectar:**
    - **JDBC URL:** `jdbc:h2:mem:testdb`
    - **User Name:** `sa`
    - **Password:** (deixe este campo em branco)
