package com.c332030.ctool4j.web.test.util;

import cn.hutool.jwt.JWTException;
import com.c332030.ctool4j.web.util.CJwtUtils;
import lombok.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * Description: CJwtUtilsTests
 * </p>
 * <p>`com.c332030.ctool4j.web.util.CJwtUtils` 的测试用例，覆盖 jwt 创建/验证/解析/JSON 提取等易出错方法。</p>
 *
 * <p><b>用例设计思路</b>：按 create / verify / parse / getJson 多个维度组织，覆盖正常、空值、异常路径。</p>
 * <p><b>设计依据</b>：依据 CJwtUtils 对 secret 非空白、jwt 空兜底等约定。</p>
 * <p><b>覆盖场景</b>：create（Map/Object secret、null/空 secret 抛异常）；verify（正确/错误 secret、非法 jwt、
 * 空 jwt、null/空 secret 抛异常）；parseJwt（正常/空/null/无点）；getJson（正常/null 数组/越界）；
 * getHeaderJson/getBodyJson（正常/空 jwt/空段）；parseHeader/parseBody（正常/空 jwt/往返）。</p>
 * <p><b>未覆盖</b>：无（已覆盖核心行为）。</p>
 *
 * <p><b>用例编号索引</b>：1 create（1.1-1.4）；2 verify（2.1-2.6）；3 parseJwt（3.1-3.3）；4 getJson/头载荷（4.1-4.8）；
 * 5 parseHeader（5.1-5.2）；6 parseBody（6.1-6.3）。各测试方法 javadoc 标注其编号与说明。</p>
 *
 * <h2>设计思路</h2>
 * <ul>
 *   <li>按 {@code create} / {@code verify} / {@code parseJwt} / {@code getJson} / {@code parseHeader} / {@code parseBody} 多个维度组织，覆盖正常、边界与异常路径。</li>
 *   <li>密钥（secret）维度单独覆盖：空白/null 快速失败（IllegalArgumentException），非空白正常使用。</li>
 *   <li>jwt 维度覆盖：合法 jwt、空/空白 jwt、格式非法（段数不足或非法 base64/JSON）三类；空 jwt 在 verify 中按约定返回 false，在解析类方法中返回 null。</li>
 *   <li>解析类方法覆盖段索引边界（头 / 载荷 / 越界）与「往返一致」（create 后 parse 回来字段一致）。</li>
 * </ul>
 * <h2>设计依据</h2>
 * <ul>
 *   <li>依据功能设计对 secret 非空白快速失败、verify 空 jwt 直接返回 false、解析空输入返回 null 的约定。</li>
 *   <li>依据等价类/边界值/异常路径/分支覆盖：合法/非法 jwt、空 secret、段索引越界、无分隔点。</li>
 * </ul>
 * <h2>覆盖场景与未覆盖</h2>
 * <ul>
 *   <li>覆盖：{@code create}（Map/Object 荷载 + secret，null/空 secret 抛异常）；{@code verify}（正确 secret 通过、错误 secret 不通过、非法 jwt、</li>
 *   <li>空 jwt、null/空 secret 抛异常）；{@code parseJwt}（正常三段、空、null、无点）；{@code getJson}（正常、null 数组、越界）；</li>
 *   <li>{@code getHeaderJson}/{@code getBodyJson}（正常、空 jwt、空段）；{@code parseHeader}/{@code parseBody}（正常、空 jwt、往返一致）。</li>
 *   <li>未覆盖：无（已覆盖核心行为）；依赖真实签名算法差异（如 RS256）的场景不在本工具类职责内。</li>
 * </ul>
 * <h2>create</h2>
 * <ul>
 *   <li>1.1 Map 荷载 + secret 创建（创建后可正常 verify）（create）</li>
 *   <li>1.2 Object 荷载 + secret 创建（create）</li>
 *   <li>1.3 null secret 抛 IllegalArgumentException（create）</li>
 *   <li>1.4 空白 secret 抛 IllegalArgumentException（create）</li>
 * </ul>
 * <h2>verify</h2>
 * <ul>
 *   <li>2.1 正确 secret 校验通过（verify）</li>
 *   <li>2.2 错误 secret 校验不通过（verify）</li>
 *   <li>2.3 格式非法 jwt 抛 JWTException（verify）</li>
 *   <li>2.4 空 jwt 直接返回 false（verify）</li>
 *   <li>2.5 null secret 抛 IllegalArgumentException（verify）</li>
 *   <li>2.6 空白 secret 抛 IllegalArgumentException（verify）</li>
 * </ul>
 * <h2>parseJwt</h2>
 * <ul>
 *   <li>3.1 正常 jwt 按 "." 拆分为三段（parseJwt）</li>
 *   <li>3.2 null / 空 jwt 返回 null（parseJwt_empty_returnsNull）</li>
 *   <li>3.3 无 "." 的字符串拆分为单元素数组（parseJwt_noDot）</li>
 * </ul>
 * <h2>getJson / 头载荷</h2>
 * <ul>
 *   <li>4.1 getJson 提取并 base64 解码第 index 段（getJson）</li>
 *   <li>4.2 getJson 数组为 null / 空返回 null（getJson_nullArr）</li>
 *   <li>4.3 getJson 段索引越界返回 null（getJson_outOfIndex）</li>
 *   <li>4.4 getHeaderJson 提取头段 JSON（getHeaderJson）</li>
 *   <li>4.5 getHeaderJson 空 jwt 返回 null（getHeaderJson_emptyJwt）</li>
 *   <li>4.6 getBodyJson 提取载荷段 JSON（getBodyJson）</li>
 *   <li>4.7 getBodyJson 空 jwt 返回 null（getBodyJson_emptyJwt）</li>
 *   <li>4.8 getBodyJson 载荷段为空返回 null（getBodyJson_emptySegment_returnsNull）</li>
 * </ul>
 * <h2>parseHeader</h2>
 * <ul>
 *   <li>5.1 解析头段为指定类型（parseHeader）</li>
 *   <li>5.2 空 jwt 返回 null（parseHeader）</li>
 * </ul>
 * <h2>parseBody</h2>
 * <ul>
 *   <li>6.1 解析载荷段为指定类型（parseBody）</li>
 *   <li>6.2 空 jwt 返回 null（parseBody）</li>
 *   <li>6.3 create 与 parseBody 往返字段一致（parseBody）</li>
 * </ul>
 *
 * @author c332030
 * @since 2026/8/14
 * @version 1.0
 * @see CJwtUtils
 */
@CustomLog
public class CJwtUtilsTests {

    private static final String SECRET = "test-secret-key-12345";

    // ---------- create ----------

    /**
     * 对应测试用例 1.1：Map 荷载 + secret 创建（创建后可正常 verify）（create）
     * 1.1 Map secret 创建 jwt（创建后可正常验证）
     */
    @Test
    public void create_mapSecret() {
        // 正例：Map body 创建 jwt
        String jwt = CJwtUtils.create(Collections.singletonMap("userId", 1L), SECRET);
        Assertions.assertNotNull(jwt);
        Assertions.assertTrue(jwt.contains("."));
        // 创建后可正常验证
        Assertions.assertTrue(CJwtUtils.verify(jwt, SECRET));
    }

    /**
     * 对应测试用例 1.2：Object 荷载 + secret 创建（create）
     * 1.2 Object secret 创建 jwt（经 CBeanUtils.toMap 转换）
     */
    @Test
    public void create_objectSecret() {
        // 正例：Object body 创建 jwt（经 CBeanUtils.toMap 转换）
        val jwt = CJwtUtils.create(new UserDto(2L, "tom"), SECRET);
        Assertions.assertNotNull(jwt);
        Assertions.assertTrue(CJwtUtils.verify(jwt, SECRET));
    }

    /**
     * 对应测试用例 1.3：null secret 抛 IllegalArgumentException（create）
     * 1.3 null secret 快速失败（IllegalArgumentException）
     */
    @Test
    public void create_nullSecret_throws() {
        // 异常路径：null secret 快速失败（IllegalArgumentException）
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CJwtUtils.create(Collections.singletonMap("a", 1), null)
        );
    }

    /**
     * 对应测试用例 1.4：空白 secret 抛 IllegalArgumentException（create）
     * 1.4 空/纯空白 secret 快速失败（IllegalArgumentException）
     */
    @Test
    public void create_emptySecret_throws() {
        // 反例：空/纯空白 secret 快速失败（IllegalArgumentException）
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CJwtUtils.create(Collections.singletonMap("a", 1), "")
        );
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CJwtUtils.create(Collections.singletonMap("a", 1), "   ")
        );
    }

    // ---------- verify ----------

    /**
     * 对应测试用例 2.1：正确 secret 校验通过（verify）
     * 2.1 正确密钥验证通过
     */
    @Test
    public void verify_correctSecret() {
        // 正例：正确密钥验证通过
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Assertions.assertTrue(CJwtUtils.verify(jwt, SECRET));
    }

    /**
     * 对应测试用例 2.2：错误 secret 校验不通过（verify）
     * 2.2 错误密钥验证失败
     */
    @Test
    public void verify_wrongSecret() {
        // 反例：错误密钥验证失败
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Assertions.assertFalse(CJwtUtils.verify(jwt, "wrong-secret"));
    }

    /**
     * 对应测试用例 2.3：格式非法 jwt 抛 JWTException（verify）
     * 2.3 非法 jwt（段数不足）抛 JWTException
     */
    @Test
    public void verify_invalidJwt() {
        // 反例：非法 jwt（段数不足）抛出 JWTException
        Assertions.assertThrowsExactly(
            JWTException.class,
            () -> CJwtUtils.verify("not-a-jwt", SECRET)
        );
    }

    /**
     * 对应测试用例 2.4：空 jwt 直接返回 false（verify）
     * 2.4 空 jwt 不校验签名直接返回 false
     */
    @Test
    public void verify_emptyJwt_returnsFalse() {
        // 边界：null/空 jwt 不校验签名，直接返回 false
        Assertions.assertFalse(CJwtUtils.verify(null, SECRET));
        Assertions.assertFalse(CJwtUtils.verify("", SECRET));
    }

    /**
     * 对应测试用例 2.5：null secret 抛 IllegalArgumentException（verify）
     * 2.5 null secret 快速失败（IllegalArgumentException）
     */
    @Test
    public void verify_nullSecret_throws() {
        // 异常路径：null secret 快速失败（IllegalArgumentException）
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CJwtUtils.verify("not-a-jwt", null)
        );
    }

    /**
     * 对应测试用例 2.6：空白 secret 抛 IllegalArgumentException（verify）
     * 2.6 空/纯空白 secret 快速失败（IllegalArgumentException）
     */
    @Test
    public void verify_emptySecret_throws() {
        // 反例：空/纯空白 secret 快速失败（IllegalArgumentException）
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CJwtUtils.verify("not-a-jwt", "")
        );
        Assertions.assertThrowsExactly(
            IllegalArgumentException.class,
            () -> CJwtUtils.verify("not-a-jwt", "   ")
        );
    }

    // ---------- parseJwt ----------

    /**
     * 对应测试用例 3.1：正常 jwt 按 "." 拆分为三段
     * 3.1 parseJwt：按 . 拆分三段
     */
    @Test
    public void parseJwt() {
        // 正例：jwt 按 . 拆分三段
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        val arr = CJwtUtils.parseJwt(jwt);
        Assertions.assertNotNull(arr);
        Assertions.assertEquals(3, arr.length);
    }

    /**
     * 对应测试用例 3.2：null / 空 jwt 返回 null
     * 3.2 parseJwt：空 jwt 返回 null
     */
    @Test
    public void parseJwt_empty_returnsNull() {
        // 边界：null/空 jwt 返回 null（纯空白串走 split 返回非空数组，不属于此场景）
        Assertions.assertNull(CJwtUtils.parseJwt(null));
        Assertions.assertNull(CJwtUtils.parseJwt(""));
    }

    /**
     * 对应测试用例 3.3：无 "." 的字符串拆分为单元素数组
     * 3.3 parseJwt：无 . 的字符串拆分为单元素
     */
    @Test
    public void parseJwt_noDot() {
        // 边界：无 . 的字符串拆分为单元素
        val arr = CJwtUtils.parseJwt("abcdef");
        Assertions.assertNotNull(arr);
        Assertions.assertEquals(1, arr.length);
    }

    // ---------- getJson ----------

    /**
     * 对应测试用例 4.1：getJson 提取并 base64 解码第 index 段
     * 4.1 getJson：提取并 base64 解码第 index 段
     */
    @Test
    public void getJson() {
        // 正例：提取并 base64 解码第 index 段
        String jwt = CJwtUtils.create(Collections.singletonMap("userId", 7L), SECRET);
        val arr = CJwtUtils.parseJwt(jwt);
        val headerJson = CJwtUtils.getJson(arr, 0);
        Assertions.assertNotNull(headerJson);
        Assertions.assertTrue(headerJson.contains("alg"));
    }

    /**
     * 对应测试用例 4.2：getJson 数组为 null / 空返回 null
     * 4.2 getJson：null/空数组返回 null
     */
    @Test
    public void getJson_nullArr() {
        // 边界：null/空数组返回 null
        Assertions.assertNull(CJwtUtils.getJson(null, 0));
        Assertions.assertNull(CJwtUtils.getJson(new String[0], 0));
    }

    /**
     * 对应测试用例 4.3：getJson 段索引越界返回 null
     * 4.3 getJson：越界 index 返回 null
     */
    @Test
    public void getJson_outOfIndex() {
        // 边界：越界 index 返回 null
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        val arr = CJwtUtils.parseJwt(jwt);
        Assertions.assertNull(CJwtUtils.getJson(arr, 99));
    }

    // ---------- getHeaderJson / getBodyJson ----------

    /**
     * 对应测试用例 4.4：getHeaderJson 提取头段 JSON
     * 4.4 getHeaderJson：jwt 字符串提取 header json
     */
    @Test
    public void getHeaderJson() {
        // 正例：jwt 字符串提取 header json
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        val headerJson = CJwtUtils.getHeaderJson(jwt);
        Assertions.assertNotNull(headerJson);
        Assertions.assertTrue(headerJson.contains("alg"));
    }

    /**
     * 对应测试用例 4.5：getHeaderJson 空 jwt 返回 null
     * 4.5 getHeaderJson：空 jwt 返回 null
     */
    @Test
    public void getHeaderJson_emptyJwt() {
        // 边界：null/空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.getHeaderJson((String) null));
        Assertions.assertNull(CJwtUtils.getHeaderJson(""));
    }

    /**
     * 对应测试用例 4.6：getBodyJson 提取载荷段 JSON
     * 4.6 getBodyJson：jwt 字符串提取 body json
     */
    @Test
    public void getBodyJson() {
        // 正例：jwt 字符串提取 body json
        String jwt = CJwtUtils.create(Collections.singletonMap("userId", 7L), SECRET);
        val bodyJson = CJwtUtils.getBodyJson(jwt);
        Assertions.assertNotNull(bodyJson);
        Assertions.assertTrue(bodyJson.contains("userId"));
    }

    /**
     * 对应测试用例 4.7：getBodyJson 空 jwt 返回 null
     * 4.7 getBodyJson：空 jwt 返回 null
     */
    @Test
    public void getBodyJson_emptyJwt() {
        // 边界：null/空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.getBodyJson((String) null));
        Assertions.assertNull(CJwtUtils.getBodyJson(""));
    }

    /**
     * 对应测试用例 4.8：getBodyJson 载荷段为空返回 null
     * 4.8 getBodyJson：body 段为空返回 null
     */
    @Test
    public void getBodyJson_emptySegment_returnsNull() {
        // 边界：body 段为空时返回 null
        Assertions.assertNull(CJwtUtils.getBodyJson(new String[]{"eyJhbGciOiJIUzI1NiJ9", "", "sig"}));
    }

    // ---------- parseHeader / parseBody ----------

    /**
     * 对应测试用例 5.1：解析头段为指定类型
     * 5.1 parseHeader：header 解析为 Map
     */
    @Test
    @SuppressWarnings("unchecked")
    public void parseHeader() {
        // 正例：header 解析为 Map（raw Map.class 传参导致 unchecked，测试刻意使用）
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Map<String, Object> header = CJwtUtils.parseHeader(jwt, Map.class);
        Assertions.assertNotNull(header);
    }

    /**
     * 对应测试用例 5.2：空 jwt 返回 null（parseHeader）
     * 5.2 parseHeader：空 jwt 返回 null
     */
    @Test
    public void parseHeader_emptyJwt_returnsNull() {
        // 边界：空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.parseHeader("", Map.class));
        Assertions.assertNull(CJwtUtils.parseHeader(null, Map.class));
    }

    /**
     * 对应测试用例 6.1：解析载荷段为指定类型
     * 6.1 parseBody：body 解析为 Map，字段可回读
     */
    @Test
    @SuppressWarnings("unchecked")
    public void parseBody() {
        // 正例：body 解析为 Map，字段可回读（raw Map.class 传参导致 unchecked，测试刻意使用）
        Map<String, Object> bodyMap = new java.util.HashMap<>();
        bodyMap.put("name", "tom");
        String jwt = CJwtUtils.create(bodyMap, SECRET);
        Map<String, Object> body = CJwtUtils.parseBody(jwt, Map.class);
        Assertions.assertNotNull(body);
        Assertions.assertEquals("tom", body.get("name"));
    }

    /**
     * 对应测试用例 6.2：空 jwt 返回 null（parseBody）
     * 6.2 parseBody：空 jwt 返回 null
     */
    @Test
    public void parseBody_emptyJwt_returnsNull() {
        // 边界：空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.parseBody("", Map.class));
        Assertions.assertNull(CJwtUtils.parseBody(null, Map.class));
    }

    /**
     * 对应测试用例 6.3：create 与 parseBody 往返字段一致（parseBody）
     * 6.3 parseBody：create 后 parseBody 还原原始数据（往返）
     */
    @Test
    public void parseBody_roundTrip() {
        // 正例：create 后 parseBody 能还原原始数据（仅用字符串值避免数值类型差异）
        Map<String, Object> original = new java.util.HashMap<>();
        original.put("role", "admin");
        original.put("name", "tom");
        val jwt = CJwtUtils.create(original, SECRET);
        Map<String, Object> body = CJwtUtils.parseBody(jwt, Map.class);
        Assertions.assertEquals(original, body);
    }

    // 内部测试用 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String name;
    }

}
