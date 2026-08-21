package com.c332030.ctool4j.mybatis.model.impl;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.c332030.ctool4j.mybatis.model.ICDeleted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: CDeleted
 * </p>
 *
 * @since 2025/12/16
 * @see "doc/design/mybatis/CDeleted.adoc"
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CDeleted implements ICDeleted {

    @TableLogic
    Boolean deleted;

}
