## para fazer o servidor de linguagem (autocompleção, error highlighting, etc) funcionar direito na sua ide, rodar ./gradlew publishToLocalRepository em ./shared

### Instruções para o build: (em ./docker')

1. rodar DOCKER_BUILDKIT=1 docker compose build
2. rodar docker compose up -d (-d roda sem terminal interativo)
3. para desligar e remover containers (dados se mantém salvos) rodar docker compose down
