package modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

@Converter
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth fecha) {
        return fecha == null ? null : fecha.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String fechaGuardada) {
        return fechaGuardada == null || fechaGuardada.isBlank() ? null : YearMonth.parse(fechaGuardada);
    }
}
