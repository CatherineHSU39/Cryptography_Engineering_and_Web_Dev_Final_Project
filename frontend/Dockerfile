# Stage 1: Build the Vue app
FROM node:lts AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: NGINX to serve the app
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html