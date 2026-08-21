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
 * @see doc/design/base/CAbstractProcessor.adoc
 * @see doc/design/base/CAbstractProcessorTests.adoc
 */
public abstract class CAbstractProcessor extends AbstractProcessor {

    /**
     * 支持的 Java 源版本
     *
     * @return Java 8
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

}
