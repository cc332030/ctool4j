package com.c332030.ctool4j.core.exception;

import com.c332030.ctool4j.core.util.CResUtils;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.definition.function.CTriFunction;
import com.c332030.ctool4j.definition.interfaces.ICRes;
import lombok.val;

/**
 * <p>
 * Description: ICBusinessExceptionProvider
 * </p>
 *
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code getExceptionFunction()}：默认实现——基于 {@code getMessageExceptionFunction} 创建异常，</li>
 *   <li>消息经 {@code CResUtils.formatResMessage(error, errorExtend)} 格式化。</li>
 *   <li>{@code getMessageExceptionFunction()}：默认实现抛 {@code UnsupportedOperationException("No impl")}，</li>
 *   <li>由实现类覆盖。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>getMessageExceptionFunction 默认调用</td>
 *     <td>抛 UnsupportedOperationException</td>
 *   </tr>
 *   <tr>
 *     <td>error 为 null</td>
 *     <td>消息仅含扩展信息</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>通过 SPI 自定义业务异常类型/创建逻辑。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>直接使用默认 getExceptionFunction 需覆盖 getMessageExceptionFunction，否则抛异常。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>默认实现提供消息格式化，异常创建留给实现类，保持 SPI 扩展灵活。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>默认异常函数</b></p>
 * <ul>
 *   <li>{@code getExceptionFunction()} 默认经 {@code formatResMessage} 生成 {@code [code] msg[: extend]} 消息，再委托</li>
 *   <li>{@code getMessageExceptionFunction} 创建异常。</li>
 *   <li>error 为 null 时消息仅含扩展信息。</li>
 * </ul>
 * <p><b>扩展点</b></p>
 * <ul>
 *   <li>实现类覆盖 {@code getMessageExceptionFunction} 以定制异常创建；默认实现抛 UnsupportedOperationException</li>
 *   <li>强制实现类覆盖。</li>
 * </ul>
 *
 * @since 2025/9/14
 * @version 1.0
 */
public interface ICBusinessExceptionProvider<T extends Throwable> {

    default CTriFunction<ICRes<?>, String, Throwable, T> getExceptionFunction() {
        return (error, errorExtend, cause) -> {

            val message = CResUtils.formatResMessage(error, errorExtend);
            return getMessageExceptionFunction().apply(message, cause);
        };
    }

    /**
     * 获取异常生成函数（默认实现抛出 UnsupportedOperationException）
     * @return 异常生成函数
     */
    default CBiFunction<String, Throwable, T> getMessageExceptionFunction() {
        return (message, cause) -> {
            throw new UnsupportedOperationException("No impl");
        };
    }

}
