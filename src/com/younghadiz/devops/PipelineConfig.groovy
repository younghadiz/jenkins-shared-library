package com.younghadiz.devops

class PipelineConfig implements Serializable {

    static String required(Map config, String key) {
        def value = config[key]

        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Required pipeline configuration '${key}' was not provided."
            )
        }

        return value.toString()
    }

    static String optional(
        Map config,
        String key,
        String defaultValue = ''
    ) {
        def value = config[key]

        if (value == null || value.toString().trim().isEmpty()) {
            return defaultValue
        }

        return value.toString()
    }
}
