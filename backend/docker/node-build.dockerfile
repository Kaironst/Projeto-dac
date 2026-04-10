FROM node:current-alpine AS build

WORKDIR /app/

ARG PROJECT_DIR

COPY . .
WORKDIR /app/${PROJECT_DIR}

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
