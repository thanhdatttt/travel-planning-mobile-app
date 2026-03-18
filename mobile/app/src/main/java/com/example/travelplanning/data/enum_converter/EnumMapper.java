package com.example.travelplanning.data.enum_converter;

public class EnumMapper {

    /**
     * Hàm dùng chung cho mọi Enum trong dự án
     * @param enumClass Kiểu Class của Enum (ví dụ: UserRole.class)
     * @param text Chuỗi String nhận từ Backend API
     * @param defaultValue Giá trị trả về an toàn nếu String bị null hoặc lỗi
     * @param <T> Đại diện cho mọi Enum implements MappableEnum
     */
    public static <T extends Enum<T> & MappableEnum> T fromString(Class<T> enumClass, String text, T defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getStringValue().equalsIgnoreCase(text)) {
                return enumConstant;
            }
        }

        return defaultValue;
    }
}
