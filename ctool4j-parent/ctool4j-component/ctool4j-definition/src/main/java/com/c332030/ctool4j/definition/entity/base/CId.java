package com.c332030.ctool4j.definition.entity.base;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * <p>
 * Description: CId
 * </p>
 *
 * @since 2025/5/26
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CId<ID extends Serializable> implements ICId<ID> {

    @TableId
    ID id;

}
