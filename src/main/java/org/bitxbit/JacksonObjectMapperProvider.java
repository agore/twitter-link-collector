package org.bitxbit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.ext.ContextResolver;

/**
 * Created by agore on 12/7/15.
 */
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private ObjectMapper objectMapper;

    @Override
    public ObjectMapper getContext(Class<?> aClass) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        }

        return objectMapper;
    }
}
