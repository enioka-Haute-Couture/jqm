#Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY ./jqm-all/jqm-ws/src/main/react ./
ENV JQM_CONSOLE_PORT=1789
RUN yarn install

# Production Stage
FROM nginx:stable-alpine AS production
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
