# Use the official Node.js image as the base image
FROM node:20-alpine AS build
WORKDIR /app

# Copy only the package.json and package-lock.json to install dependencies
COPY package*.json ./

# Install the dependencies
RUN npm ci

# Copy the rest of the application code
COPY . .

# Build the React application
RUN npm run build

# Use a lightweight server for serving the build files
FROM node:20-alpine
WORKDIR /app
RUN npm install -g serve

# Copy build output
COPY --from=build /app/build ./build

# Expose the port your app runs on
EXPOSE 5173

# Start the server
CMD ["serve", "-s", "build", "-l", "5173"]
