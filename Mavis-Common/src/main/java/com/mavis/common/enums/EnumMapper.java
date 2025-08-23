package com.mavis.common.enums;

import java.util.*;

public class EnumMapper {
    private final Map<String, List<EnumMapperVO>> factory = new LinkedHashMap<>();

    public void put(String key, Class<? extends EnumMapperType> clazz) {
        factory.put(key, toEnumVoList(clazz));
    }

    private List<EnumMapperVO> toEnumVoList(Class<? extends EnumMapperType> clazz) {
        EnumMapperType[] enumConstants = clazz.getEnumConstants();
        return Arrays.stream(enumConstants)
                .map(EnumMapperVO::new)
                .toList();
    }

    public List<EnumMapperVO> toEnumVoList(Set<? extends EnumMapperType> enums) {
        return enums.stream()
                .map(EnumMapperVO::new)
                .toList();
    }

    public List<EnumMapperVO> get(String key) {
        return factory.get(key);
    }
}
