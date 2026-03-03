package com.elevideo.backend.config.converter;

import com.elevideo.backend.enums.Platform;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PlatformConverter implements Converter<String, Platform> {

    @Override
    public Platform convert(String source) {
        return Platform.fromValue(source);
    }
}
