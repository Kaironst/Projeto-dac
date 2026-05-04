FROM node:current-alpine AS build

WORKDIR /app/

ARG PROJECT_DIR

COPY ./${PROJECT_DIR} ./${PROJECT_DIR}

WORKDIR /app/${PROJECT_DIR}

RUN npm ci
RUN npm run build

FROM node:current-alpine

WORKDIR /app/

ARG PROJECT_DIR

COPY --from=build /app/${PROJECT_DIR}/package*.json ./
COPY --from=build /app/${PROJECT_DIR}/dist dist/

RUN npm ci --omit=dev

ENTRYPOINT ["node", "dist/index.js"]