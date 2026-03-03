package com.elevideo.backend.config.converter;

import com.elevideo.backend.enums.ProcessingMode;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;

@Component
public class ProcessingModeConverter implements Converter<String, ProcessingMode> {

    @Override
    public ProcessingMode convert(String source) {
        return ProcessingMode.fromValue(source);
    }
}
