package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;

/**
 * <p>
 * Description: CBusinessExceptionProvider
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>（error/msgExtend/cause 三元构造）。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无自定义 SPI 实现</td>
 *     <td>使用本提供者创建 CBusinessException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>项目默认业务异常创建，经 SPI 加载。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要自定义异常类型时，通过 SPI 提供自定义 {@code ICBusinessExceptionProvider}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>作为默认实现，保证无 SPI 配置时业务异常可用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认实现</b></p>
 * <ul>
 *   <li>作为 SPI 自定义实现的默认回退。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 */
public class CBusinessExceptionProvider implements ICBusinessExceptionProvider<CBusinessException> {

    /**
     * 获取创建业务异常的函数（默认即 CBusinessException 构造引用）
     *
     * @return 业务异常创建函数
     */
    @Override
    public CTriFunction<ICRes<?>, String, Throwable, CBusinessException> getExceptionFunction() {
        return CBusinessException::new;
    }

}
