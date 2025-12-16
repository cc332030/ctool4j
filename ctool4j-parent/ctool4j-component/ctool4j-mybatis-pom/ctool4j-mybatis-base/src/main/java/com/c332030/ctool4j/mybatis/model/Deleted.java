package com.c332030.ctool4j.mybatis.model;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Description: Deleted
 * </p>
 *
 * @since 2025/12/16
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Deleted implements IDeleted {

    @TableLogic
    Boolean deleted;

}
