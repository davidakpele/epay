FROM golang:1.23 AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod tidy
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o history-service .
FROM alpine:latest  
RUN apk --no-cache add ca-certificates curl
WORKDIR /root/
COPY --from=builder /app/history-service .
EXPOSE 8391
CMD ["./history-service"]