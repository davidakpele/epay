package pesco.notification_service.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.activation.DataSource;

public class ByteArrayDataSource implements DataSource {
    private final byte[] data;
    private final String type;

    public ByteArrayDataSource(byte[] data, String type) {
        this.data = data;
        this.type = type;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Cannot write to this data source");
    }

    @Override
    public String getContentType() {
        return type;
    }

    @Override
    public String getName() {
        return "attachment";
    }
}
