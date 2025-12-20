package com.c332030.ctool4j.core.test.classes;

import com.c332030.ctool4j.core.classes.CLambdaUtils;
import com.c332030.ctool4j.core.classes.CReflectUtils;
import com.c332030.ctool4j.definition.entity.base.CLongId;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Description: CLambdaUtilsTests
 * </p>
 *
 * @author c332030
 * @since 2025/12/20
 */
public class CLambdaUtilsTests {

    @Test
    public void getFieldGetLambda() {

        val id = 332030L;

        val longId = CLongId.builder()
                .id(id)
                .build();

        val field = CReflectUtils.getField(CLongId.class, "id");
        val lambda = CLambdaUtils.getFieldGetLambda(CLongId.class, field);

        Assertions.assertEquals(id, lambda.apply(longId));

    }

    @Test
    public void getFieldSetLambda() {

        val id = 332030L;

        val longId = new CLongId();

        val field = CReflectUtils.getField(CLongId.class, "id");
        val lambda = CLambdaUtils.getFieldSetLambda(CLongId.class, field);

        lambda.accept(longId, id);
        Assertions.assertEquals(id, longId.getId());

    }

}
