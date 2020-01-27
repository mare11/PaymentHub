package org.sep.paypalservice.model;

import com.paypal.http.HttpRequest;
import org.springframework.http.MediaType;

public abstract class PostRequest<T> extends HttpRequest<T> {

    private static final String HTTP_POST = "POST";

    public PostRequest(final String path, final Class<T> responseClass) {
        super(path, HTTP_POST, responseClass);
        this.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    }

    public PostRequest<T> prefer(final String prefer) {
        this.header("Prefer", prefer);
        return this;
    }

    @Override
    public PostRequest<T> requestBody(final Object object) {
        super.requestBody(object);
        return this;
    }
}