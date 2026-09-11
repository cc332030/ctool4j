package com.c332030.ctool4j.web.config;

import com.c332030.ctool4j.core.util.CSet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * <p>
 * Description: 内部接口 IP 白名单配置
 * </p>
 *
 * <p>配置内部接口（标注 {@code @CInnerApi} 的 Controller/方法）允许访问的 IP/IP 段白名单，
 * 所有标注 {@code @CInnerApi} 的内部接口共用本配置。白名单为空时全部放行。</p>
 *
 * <p>规则支持单个 IPv4（如 {@code 192.168.1.5}）与 CIDR 网段（如 {@code 192.168.1.0/24}），
 * 多个用逗号分隔，由 {@code CIpUtils} 解析判断。</p>
 *
 * <h2>能力目录</h2>
 * <p>字段：</p>
 * <ul>
 *   <li>{@code allowedIps}：内部接口允许访问的 IP/IP 段(CIDR)白名单，默认空（空则全部放行）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>未配置 {@code allowed-ips}</td>
 *     <td>默认空集合，拦截器视为全部放行</td>
 *   </tr>
 *   <tr>
 *     <td>规则非法（如非 CIDR、前缀越界）</td>
 *     <td>由 {@code CIpUtils} 记录日志并跳过该规则，不影响其它规则</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>通过 {@code inner-api.allowed-ips} 配置内部接口允许访问的内网 IP/IP 段：
 *   单个 IPv4（如 {@code 127.0.0.1}）或 CIDR 网段（如 {@code 10.0.0.0/8}、{@code 192.168.1.0/24}），由 {@code CIpUtils} 解析判断。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅配置模型，实际生效依赖 {@code CInnerApiInterceptor} 读取并校验。</li>
 *   <li>规则仅支持 IPv4 与 IPv4 CIDR；配置 IPv6 规则会被 {@code CIpUtils} 判为非法规则跳过（不会报错但等效于未命中，见 {@code CIpUtils}）。</li>
 * </ul>
 * <h2>波及影响</h2>
 * <ul>
 *   <li>白名单为<b>全局唯一</b>，所有标注 {@code @CInnerApi} 的接口共用（取舍：不支持逐接口差异化名单）。调整白名单会同时影响全部内部接口的放行范围。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>配置绑定</b></p>
 * <ul>
 *   <li>使用 {@code inner-api} 前缀绑定（与 {@code cors}/{@code logging.request-log} 等既有配置风格一致）。</li>
 *   <li>由 {@code CSpringConfiguration} 的 {@code @ConfigurationPropertiesScan}（扫描 {@code com.c332030.ctool4j}）自动注册为 Bean。</li>
 * </ul>
 * <p><b>白名单语义</b></p>
 * <ul>
 *   <li>仅白名单：命中 {@code allowedIps} 中任一 IP/IP 段(CIDR)才放行，否则拒绝。</li>
 *   <li>空名单即放行：未配置白名单时不限制（避免误伤内部调用），由使用方按需配置。</li>
 * </ul>
 * <p><b>字段级</b></p>
 * <ul>
 *   <li>{@code allowedIps}（配置键 {@code inner-api.allowed-ips}）：</li>
 *   <li><b>默认值语义（易误用/安全）</b>：默认空集合，语义为"全部放行"。若标注了 {@code @CInnerApi} 却<b>忘记配置白名单</b>，拦截器因空名单直接放行——等于没启用 IP 保护。使用者须意识到：<b>只有显式配置 {@code allowed-ips} 后 IP 白名单才真正生效</b>，这与直觉（标注=保护）相反，属易踩坑点。</li>
 *   <li>单值/多值：Set 支持多个元素，配置文件用列表或逗号分隔均可绑定。</li>
 * </ul>
 * <p><b>线程安全与变更生效</b></p>
 * <ul>
 *   <li>作为 Spring 单例 {@code @ConfigurationProperties} Bean，经 setter 绑定；运行期一般不再变更。若在运行期动态改写 {@code allowedIps} 集合引用，{@code CInnerApiInterceptor} 每次请求读取最新引用，可即时生效，但非线程安全的并发写需由使用方自行保证。</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/9/9
 * @version 1.0
 */
@Data
@ConfigurationProperties("inner-api")
public class CInnerApiConfig {

    /**
     * 内部接口允许访问的 IP/IP 段(CIDR)白名单，为空时全部放行
     */
    Set<String> allowedIps = CSet.of();

}
