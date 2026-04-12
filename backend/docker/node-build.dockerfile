FROM node:current-alpine AS build

WORKDIR /app/

#project dir é a pasta a ser acessada e é passada pela dockerfile
ARG PROJECT_DIR

COPY ./${PROJECT_DIR} ./${PROJECT_DIR}

WORKDIR /app/${PROJECT_DIR}

#roda ci para installar o compilador de typescript, por mais que haja instalação novamente na proxima etapa
RUN npm ci
RUN npx tsc

FROM node:current-alpine

WORKDIR /app/

ARG PROJECT_DIR

COPY --from=build /app/${PROJECT_DIR}/package*.json ./
COPY --from=build /app/${PROJECT_DIR}/dist dist/

RUN chown -R node:node /app
USER node:node

RUN npm ci --omit=dev

ENTRYPOINT [ "node", "dist/server.js" ]
