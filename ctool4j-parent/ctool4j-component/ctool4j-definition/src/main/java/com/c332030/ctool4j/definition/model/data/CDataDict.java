package com.c332030.ctool4j.definition.model.data;

import com.c332030.ctool4j.definition.interfaces.ICDataDict;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CDataDict
 * </p>
 *
 * @since 2026/1/7
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CDataDict<T> implements ICDataDict<T> {

    T value;

    String text;

}
