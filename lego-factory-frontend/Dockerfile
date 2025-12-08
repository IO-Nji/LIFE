# Stage 1: Build the React application
FROM node:18-alpine AS build
WORKDIR /app
COPY package.json yarn.lock ./ 
RUN yarn install --frozen-lockfile # or npm install
COPY . ./
RUN yarn build # or npm run build

# Stage 2: Serve the React application with Nginx
FROM nginx:alpine
# Remove default Nginx config
RUN rm /etc/nginx/conf.d/default.conf
# Copy custom Nginx config for this container
COPY ./nginx.conf /etc/nginx/conf.d/frontend.conf
# Copy the built React app from the build stage
COPY --from=build /app/dist/ /usr/share/nginx/html/ 
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]