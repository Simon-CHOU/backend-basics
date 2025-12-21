FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80
# Use environment variable BACKEND_URL, defaulting to http://backend:8089 if not set
ENV BACKEND_URL=http://backend:8089
CMD ["nginx", "-g", "daemon off;"]
