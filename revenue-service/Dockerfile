FROM golang:1.23 AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod tidy
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o revenue-service ./cmd
FROM alpine:latest  
RUN apk --no-cache add ca-certificates curl
WORKDIR /root/
COPY --from=builder /app/revenue-service .
EXPOSE 8083
CMD ["./revenue-service"]