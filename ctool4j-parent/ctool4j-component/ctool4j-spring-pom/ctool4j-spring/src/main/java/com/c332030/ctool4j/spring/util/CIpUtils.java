package com.c332030.ctool4j.spring.util;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.StrUtil;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;

/**
 * <p>
 * Description: IP 工具类
 * </p>
 *
 * <p>提供 IPv4 地址与 CIDR 网段的公共解析与判断方法，供 IP 白名单/黑名单控制等场景复用。</p>
 *
 * <p><b>版本与扩展性：</b>入口方法（{@link #contains(String, Collection)} 等）版本无关，
 * 调用方不感知 IPv4/IPv6 差异。当前仅实现 IPv4（复用 hutool {@link Ipv4Util}）；
 * 对 IPv6 字符串与 IPv6 规则安全返回 {@code false}，不会抛异常或空指针。
 * 将来如需支持 IPv6：在版本识别处（IP 含 {@code :}）路由到独立的 v6 匹配实现（{@code matchV6}）即可，入口无需变更。</p>
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>规则支持两种形式：</li>
 *   <li>单个 IPv4：如 {@code 192.168.1.5}。</li>
 *   <li>CIDR 网段：如 {@code 192.168.1.0/24}（前缀 0~32，{@code /0} 表示全量网段）。</li>
 *   <li><b>版本无感入口 + 底层拆分扩展</b>：</li>
 *   <li>入口 {@code contains} 不暴露 IPv4/IPv6 差异，调用方无需关心版本。</li>
 *   <li>当前仅实现 IPv4；对 IPv6（含 {@code :}）地址或规则安全返回 {@code false}，不抛异常、不空指针。</li>
 *   <li>将来支持 IPv6 时，在版本识别处（IP 含 {@code :}）路由到独立 v6 匹配实现即可，入口无需变更。</li>
 *   <li><b>优先复用 hutool</b>（{@code cn.hutool.core.net.Ipv4Util}）：</li>
 *   <li>IP 转 {@code long}：{@code Ipv4Util.ipv4ToLong(String, long default)}（带默认值，非法返回哨兵）。</li>
 *   <li>CIDR 掩码位 1~32：{@code Ipv4Util.getBeginIpStr}/{@code getEndIpStr} 计算起止区间，再转 {@code long} 比较。</li>
 *   <li>掩码位范围校验：{@code Ipv4Util.IP_MASK_MAX}。</li>
 *   <li>hutool 不支持 {@code maskBit=0}（{@code /0} 全量），故 {@code maskBit=0} 特判为全量放行（命中所有合法 IPv4）。</li>
 *   <li>健壮性：目标 IP 为空、非法或 IPv6 返回 {@code false}（安全优先）；非法规则（含 IPv6 规则）记录日志并跳过，不影响其它规则判断。</li>
 *   <li>纯 JDK + hutool，不依赖 servlet/web，可跨模块复用。</li>
 * </ul>
 * <p><b>方法级</b></p>
 * <ul>
 *   <li><b>易误用</b>：IPv6 地址或规则会<b>静默返回 {@code false}</b>（无异常）。若调用方意图支持 IPv6，会把"不支持"误判为"不在白名单内"；当前阶段仅在 IPv4 场景使用，勿用本方法判断 IPv6。</li>
 *   <li><b>易误用</b>：{@code rules} 为 {@code null} 或空集合返回 {@code false}（空名单=不放行，而非"全放行"）。"白名单为空=全放行"的语义由上层 {@code CInnerApiConfig}/{@code CInnerApiInterceptor} 决定，本工具不隐含该策略，调用方需自行决定空名单语义。</li>
 *   <li>返回值即"命中与否"，不区分"IP 非法"与"未命中"，调用方如需区分请自行预校验。</li>
 *   <li>{@code match}/{@code matchCidr}（私有）：对非法规则（含 IPv6 规则、掩码位越界）记录日志并跳过，不中断对其它规则的判断——单个坏配置不致整体失效，但<b>静默跳过</b>可能掩盖配置错误，生产建议先对配置规则做合法性校验。</li>
 * </ul>
 * <p><b>性能</b></p>
 * <ul>
 *   <li>每次调用即时解析（{@code Ipv4Util.getBeginIpStr}/{@code getEndIpStr} 逐条规则计算区间），未做结果缓存。白名单规模很小、调用频次低，开销可忽略；若做高频超大规则集匹配需考虑缓存。</li>
 * </ul>
 * <p><b>线程安全与副作用</b></p>
 * <ul>
 *   <li>{@code @UtilityClass} 无状态静态方法，线程安全；不持有可变共享状态，无并发问题。</li>
 *   <li>每次调用内部解析，无副作用，可重复调用。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>IP 为空/非法/为 IPv6</td>
 *     <td>返回 {@code false}（当前仅支持 IPv4，安全拒绝，不抛异常）</td>
 *   </tr>
 *   <tr>
 *     <td>规则非法（非 CIDR、掩码位越界、IPv6 规则）</td>
 *     <td>记录日志并跳过，不影响其它规则</td>
 *   </tr>
 *   <tr>
 *     <td>{@code maskBit=0}（{@code /0} 全量）</td>
 *     <td>特判为全量放行（命中所有合法 IPv4）</td>
 *   </tr>
 *   <tr>
 *     <td>IP 为 null</td>
 *     <td>返回 {@code false}</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <p>IP 白名单/黑名单校验、按 IP 段放行/拦截等运行时判断。</p>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>当前仅支持 IPv4；IPv6 暂未实现（入口与命名已预留扩展，见"已知限制"）。</li>
 *   <li>解析为长整型比较，不做域名解析。</li>
 *   <li>非法 IP 哨兵值 {@code INVALID_IP = -1L}：IPv4 最大 {@code 0xFFFFFFFF} 恒小于 {@code -1}（long），不会与合法值冲突，作为"非法/IPv6"标记安全。</li>
 *   <li>{@code maskBit} 范围 [0,32]：hutool {@code isMaskBitValid} 视 0 为非法，故 {@code maskBit=0}（{@code /0}）由本类特判为全量放行（命中所有合法 IPv4）；超出 0~32 的掩码位判为非法规则并跳过。</li>
 *   <li>IP/规则解析复用 hutool 带默认值 {@code ipv4ToLong(String, long)}，格式非法返回哨兵，不抛异常。</li>
 * </ul>
 * <h2>波及影响</h2>
 * <ul>
 *   <li>本工具被 {@code CInnerApiInterceptor} 用于内部接口 IP 白名单校验：若将来其行为变化（如 IPv6 支持、空名单语义调整），需同步评估对 IP 白名单拦截语义的影响。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@CustomLog
@UtilityClass
public class CIpUtils {

    /**
     * 非法 IP 哨兵值（IPv4 最大 0xFFFFFFFF 远小于该值，不会与其相等）
     */
    private static final long INVALID_IP = -1L;

    private static final char CIDR_SEPARATOR = '/';

    /**
     * 判断 ip 是否命中 rules 中任一规则
     * <p>规则支持单个 IPv4（如 {@code 192.168.1.5}）或 CIDR 网段（如 {@code 192.168.1.0/24}）；
     * ip 为空、非法或为 IPv6（当前仅支持 IPv4）返回 {@code false}；非法规则将被跳过并记录日志，
     * 不影响其它规则判断。</p>
     *
     * @param ip    待判断 IP 地址
     * @param rules IP/IP 段规则集合
     * @return 是否命中任一规则
     */
    public boolean contains(String ip, String... rules) {
        if (rules == null) {
            return false;
        }
        return contains(ip, Arrays.asList(rules));
    }

    /**
     * 判断 ip 是否命中 rules 中任一规则
     *
     * @param ip    待判断 IP 地址
     * @param rules IP/IP 段规则集合
     * @return 是否命中任一规则
     * @see #contains(String, String...)
     */
    public boolean contains(String ip, Collection<String> rules) {
        if (StrUtil.isBlank(ip) || rules == null || rules.isEmpty()) {
            return false;
        }
        long ipLong = ipToLong(ip);
        if (ipLong == INVALID_IP) {
            // 非法或 IPv6：当前仅支持 IPv4，安全拒绝（不抛异常），留待将来 IPv6 扩展
            log.debug("非法或 IPv6 地址，当前仅支持 IPv4: {}", ip);
            return false;
        }
        for (String rule : rules) {
            if (StrUtil.isBlank(rule)) {
                continue;
            }
            if (match(ipLong, rule.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将 IP 解析为 long（仅支持 IPv4，版本无关入口的内部解析）
     * <p>为空、非法或含 IPv6 分隔符（{@code :}）返回 {@link #INVALID_IP}。</p>
     *
     * @param ip IP 地址
     * @return long 值；非法或 IPv6 返回 {@link #INVALID_IP}
     */
    private long ipToLong(String ip) {
        if (StrUtil.isBlank(ip) || ip.indexOf(':') >= 0) {
            // IPv6 或空：返回哨兵（扩展口：IPv6 支持时在此按 ':' 路由到 v6 解析）
            return INVALID_IP;
        }
        return Ipv4Util.ipv4ToLong(ip, INVALID_IP);
    }

    /**
     * 单个规则是否命中（当前为 IPv4 匹配，委托 hutool {@link Ipv4Util}）
     * <p>规则为 CIDR 网段时按起止 IP 区间比较；规则为单个 IP 时按相等比较。
     * 非法规则（含 IPv6 规则）返回 {@code false} 并记录日志。</p>
     *
     * @param ipLong IP long 值
     * @param rule   单个 IPv4 或 CIDR 网段
     * @return 是否命中
     */
    private boolean match(long ipLong, String rule) {
        int slashIdx = rule.indexOf(CIDR_SEPARATOR);
        if (slashIdx >= 0) {
            return matchCidr(ipLong, rule, slashIdx);
        }
        long single = ipToLong(rule);
        if (single == INVALID_IP) {
            log.warn("非法或 IPv6 地址规则，忽略: {}", rule);
            return false;
        }
        return ipLong == single;
    }

    /**
     * CIDR 网段匹配（掩码位 1~32 委托 hutool {@link Ipv4Util#getBeginIpStr} / {@link Ipv4Util#getEndIpStr} 计算起止；
     * 掩码位 0 表示全量网段，hutool 不支持，故特判为全量放行）
     *
     * @param ipLong   IP long 值
     * @param rule     CIDR 网段，如 {@code 192.168.1.0/24}
     * @param slashIdx 斜杠分隔位置
     * @return 是否在网段区间内
     */
    private boolean matchCidr(long ipLong, String rule, int slashIdx) {
        if (slashIdx <= 0 || slashIdx == rule.length() - 1) {
            log.warn("非法 CIDR 网段，忽略: {}", rule);
            return false;
        }
        String cidrIp = rule.substring(0, slashIdx);
        String maskPart = rule.substring(slashIdx + 1);
        if (!StrUtil.isNumeric(maskPart)) {
            log.warn("非法 CIDR 掩码位，忽略: {}", rule);
            return false;
        }
        int maskBit;
        try {
            maskBit = Integer.parseInt(maskPart);
        } catch (NumberFormatException e) {
            log.warn("非法 CIDR 掩码位，忽略: {}", rule, e);
            return false;
        }
        if (maskBit < 0 || maskBit > Ipv4Util.IP_MASK_MAX) {
            log.warn("非法 CIDR 掩码位，忽略: {}", rule);
            return false;
        }
        if (ipToLong(cidrIp) == INVALID_IP) {
            log.warn("非法或 IPv6 地址规则，忽略: {}", rule);
            return false;
        }
        if (maskBit == 0) {
            // /0 全量网段：hutool isMaskBitValid 视 0 为非法，此处特判，命中所有合法 IPv4
            return true;
        }
        long begin = Ipv4Util.ipv4ToLong(Ipv4Util.getBeginIpStr(cidrIp, maskBit), INVALID_IP);
        long end = Ipv4Util.ipv4ToLong(Ipv4Util.getEndIpStr(cidrIp, maskBit), INVALID_IP);
        return ipLong >= begin && ipLong <= end;
    }

}
