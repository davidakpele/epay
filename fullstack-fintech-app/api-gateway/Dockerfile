FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 8292

# Set environment variables 
ENV PORT=8292
ENV BACKEND_URL=http://localhost:8000/v1
ENV BACKEND_WS_URL=ws://localhost:8000
ENV RATE_LIMIT_PER_MINUTE=100
CMD ["npm", "run", "dev"]