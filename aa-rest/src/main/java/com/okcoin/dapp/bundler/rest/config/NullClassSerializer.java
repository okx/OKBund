package com.okcoin.dapp.bundler.rest.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.okcoin.dapp.bundler.rest.api.resp.Null;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NullClassSerializer extends JsonSerializer<Null> {

    @Override
    public void serialize(Null value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNull();
    }
}
