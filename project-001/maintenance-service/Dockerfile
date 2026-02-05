FROM rust:latest
WORKDIR /app
COPY . .
RUN cargo install sqlx-cli --no-default-features --features postgres
RUN cargo build --release

EXPOSE 8390
CMD sqlx migrate run && ./target/release/maintenance-service