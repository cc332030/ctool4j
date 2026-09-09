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
 * @author c332030
 * @see "doc/design/spring/CIpUtils.adoc"
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
