# E-commerce com microsservicos

Projeto didatico em Java e Spring Boot com dois servicos independentes: produtos e estoque. O exercicio `src\Main.java` / `src\Cliente.java` permanece separado e inalterado.

O repositorio indicado como referencia (`ecommerce_empresa_x`) contem um prototipo HTML descrito como PHP/MySQL; ele nao fornece codigo ou modelo para os microsservicos Java deste projeto.

## Tecnologias

- Java 21 e Spring Boot 3.5.6
- Maven
- Spring Web e Spring Data JPA
- H2 em memoria, com banco distinto por servico
- Spring AMQP e RabbitMQ
- Spring Boot DevTools
- Jakarta Bean Validation
- JUnit 5, Mockito e AssertJ

## Arquitetura

```text
Cliente REST
    |
    +--> Product Service (8081) --> H2 productdb
                  |
                  +--> RabbitMQ: product.exchange / product.created
                                  |
                                  v
                    stock.queue --> Warehouse Service (8082) --> H2 warehousedb
```

Cada servico e um aplicativo Maven independente, com seu proprio `pom.xml`, classe de inicializacao, configuracao e banco. Nenhum deles acessa diretamente o banco do outro.

### Product Service

`ProductEntity` representa produtos com `name`, `description` e `price`. `ProductRepository` abstrai a persistencia JPA; `ProductService` aplica as operacoes de negocio e publica evento na criacao; `ProductController` oferece a API REST. Os DTOs mantem o formato da API e da mensagem separado da entidade persistida.

### Warehouse/Stock Service

`StockEntity` armazena `productId`, `quantity` e `status`. O status e `OUT_OF_STOCK` quando a quantidade e zero e `IN_STOCK` quando e maior que zero. O `ProductMessageConsumer` recebe o evento, e `StockService` registra o produto no estoque de forma idempotente, inicialmente com quantidade zero.

## Pre-requisitos

- JDK 21
- Maven 3.9 ou superior
- RabbitMQ em execucao e acessivel pela porta AMQP 5672 para criar produtos e consumir os eventos

Os servicos podem iniciar sem broker, mas a publicacao/consumo de mensagens nao funcionara ate RabbitMQ estar disponivel. Para outro host, porta ou credenciais, defina `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME` e `RABBITMQ_PASSWORD` no ambiente dos dois processos. Os valores padrao sao `localhost`, `5672` e `guest`/`guest`.

## Como executar

Inicie RabbitMQ primeiro. Em seguida, abra dois terminais PowerShell:

```powershell
cd .\product-service
mvn spring-boot:run
```

```powershell
cd .\warehouse-service
mvn spring-boot:run
```

Product Service usa a porta `8081`; Warehouse Service usa `8082`. Cada banco H2 e em memoria: seus dados existem enquanto o processo estiver ativo e sao descartados ao reiniciar o servico. O schema e criado/atualizado pelo JPA para facilitar o estudo local.

## Endpoints

### Produtos — `http://localhost:8081`

| Metodo | Caminho | Resultado |
|---|---|---|
| `POST` | `/api/products` | Cria produto e publica evento; responde `201 Created` |
| `GET` | `/api/products` | Lista produtos |
| `GET` | `/api/products/{id}` | Consulta um produto |
| `PUT` | `/api/products/{id}` | Atualiza nome, descricao e preco |
| `DELETE` | `/api/products/{id}` | Remove produto; responde `204 No Content` |

Exemplo PowerShell para criar produto:

```powershell
curl.exe -i -X POST http://localhost:8081/api/products `
  -H "Content-Type: application/json" `
  -d '{ "name": "Camisa", "description": "Camisa de algodao", "price": 79.90 }'
```

`name` e `description` sao obrigatorios; `price` deve ser maior ou igual a zero, com no maximo duas casas decimais.

### Estoque — `http://localhost:8082`

| Metodo | Caminho | Resultado |
|---|---|---|
| `GET` | `/api/stock` | Lista os estoques |
| `GET` | `/api/stock/{productId}` | Consulta o estoque pelo ID do produto |
| `PUT` | `/api/stock/{productId}` | Atualiza a quantidade de um produto ja registrado |

Exemplo para definir quantidade:

```powershell
curl.exe -i -X PUT http://localhost:8082/api/stock/1 `
  -H "Content-Type: application/json" `
  -d '{ "quantity": 8 }'
```

Quantidade negativa e rejeitada com `400 Bad Request`; produto ou estoque inexistente responde `404 Not Found`.

## Fluxo RabbitMQ

1. `POST /api/products` persiste o produto e o `ProductMessageProducer` serializa um evento JSON.
2. O producer envia o evento ao exchange `product.exchange` com routing key `product.created`.
3. A binding encaminha a mensagem para a fila duravel `stock.queue`.
4. O `ProductMessageConsumer` do Warehouse Service le o evento e pede ao `StockService` para registrar o ID do produto com quantidade zero.
5. A consulta do estoque e eventualmente consistente: aguarde o consumer processar a mensagem antes de consultar ou ajustar o estoque.

## Testes

Execute separadamente em cada pasta:

```powershell
mvn test
```

Os testes unitarios cobrem as regras dos services e o formato/producao/consumo do evento sem exigir RabbitMQ. Para validar a comunicacao real entre processos, inicie um broker e ambos os servicos e siga o fluxo das requisicoes acima.
