package com.c332030.ctool4j.base.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.SourceVersion;

/**
 * <p>
 * Description: CAbstractProcessor
 * </p>
 *
 * @author c332030
 * @since 2026/5/31
 */
public abstract class CAbstractProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

}
