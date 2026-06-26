FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .

ARG VITE_NEW_RELIC_ACCOUNT_ID
ARG VITE_NEW_RELIC_APP_ID
ARG VITE_NEW_RELIC_LICENSE_KEY
ENV VITE_NEW_RELIC_ACCOUNT_ID=$VITE_NEW_RELIC_ACCOUNT_ID
ENV VITE_NEW_RELIC_APP_ID=$VITE_NEW_RELIC_APP_ID
ENV VITE_NEW_RELIC_LICENSE_KEY=$VITE_NEW_RELIC_LICENSE_KEY
RUN npm run build

FROM nginx:alpine AS final
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8880/ || exit 1
EXPOSE 80
