# File Manager API (Spring Boot)

RESTful API para gerenciar arquivos em duas subpastas controladas (`dataset` e `credential`) dentro de um caminho base configurável.

## Requisitos

- Java 25
- Maven 3.9+

## Configuração

A base de armazenamento é configurada em `src/main/resources/application.yml`:

```yaml
file-manager:
  base-path: ${BASE_PATH:/opt/static_file_manager/}
```

- Se `BASE_PATH` não for definido, o padrão será `/opt/static_file_manager/`.
- Na inicialização, a aplicação garante a criação das subpastas:
  - `dataset`
  - `credential`

## Como executar

### 1) Entrar no diretório do projeto

```bash
cd /home/ubuntu/file-manager-api
```

### 2) (Opcional) Definir BASE_PATH

```bash
export BASE_PATH=/tmp/static_file_manager
```

### 3) Rodar a aplicação

```bash
mvn spring-boot:run
```

A API sobe por padrão em `http://localhost:8080`.

## Endpoints

### 1. Criar arquivo

**POST** `/api/files/{subdirectory}`

- `subdirectory`: `dataset` ou `credential`
- Body JSON:

```json
{
  "fileName": "my-file.json",
  "content": "{\"key\":\"value\"}"
}
```

### 2. Atualizar arquivo

**PUT** `/api/files/{subdirectory}`

- `subdirectory`: `dataset` ou `credential`
- Body JSON igual ao POST

### 3. Deletar arquivo

**DELETE** `/api/files/{subdirectory}?file_name=my-file.json`

### 4. Listar todos os arquivos

**GET** `/api/files`

Retorna lista de arquivos em `dataset` e `credential` (recursivo), com metadados.

### 5. Buscar conteúdo de um arquivo

**GET** `/api/files/{subdirectory}?file_name=my-file.json`

Retorna wrapper JSON:

```json
{
  "subdirectory": "dataset",
  "fileName": "my-file.json",
  "content": "{\"key\":\"value\"}"
}
```

## Exemplos com cURL

### Criar arquivo em dataset

```bash
curl -X POST "http://localhost:8080/api/files/dataset" \
  -H "Content-Type: application/json" \
  -d '{"fileName":"datasource.json","content":"{\"url\":\"jdbc:postgresql://db:5432/app\",\"user\":\"app\",\"password\":\"secret\"}"}'
```

### Criar arquivo em credential

```bash
curl -X POST "http://localhost:8080/api/files/credential" \
  -H "Content-Type: application/json" \
  -d '{"fileName":"client.json","content":"{\"client_secret\":\"secret\",\"client_key\":\"key\",\"claims\":[\"read\",\"write\"]}"}'
```

### Listar arquivos

```bash
curl "http://localhost:8080/api/files"
```

### Ler conteúdo de arquivo

```bash
curl "http://localhost:8080/api/files/dataset?file_name=datasource.json"
```

### Atualizar arquivo

```bash
curl -X PUT "http://localhost:8080/api/files/dataset" \
  -H "Content-Type: application/json" \
  -d '{"fileName":"datasource.json","content":"{\"url\":\"jdbc:postgresql://db:5432/newdb\",\"user\":\"new_user\",\"password\":\"new_password\"}"}'
```

### Deletar arquivo

```bash
curl -X DELETE "http://localhost:8080/api/files/dataset?file_name=datasource.json"
```

## Tratamento de erros

A API retorna erros em formato JSON com:

- `timestamp`
- `status`
- `error`
- `message`
- `details`

Exemplos: validação inválida, arquivo não encontrado, conflito de arquivo existente e erro interno.
