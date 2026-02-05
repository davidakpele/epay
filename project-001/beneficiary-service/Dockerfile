FROM golang:1.23 AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod tidy
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o beneficiary-service .
FROM alpine:latest  
RUN apk --no-cache add ca-certificates curl
WORKDIR /root/
COPY --from=builder /app/beneficiary-service .
COPY --from=builder /app/.env ./.env 
EXPOSE 8084
CMD ["./beneficiary-service"]