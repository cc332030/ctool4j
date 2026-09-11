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
 * @author c332030
 * @since 2026/8/14
 */
@CustomLog
public class CJwtUtilsTests {

    private static final String SECRET = "test-secret-key-12345";

    // ---------- create ----------

    /**
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
     * 2.1 正确密钥验证通过
     */
    @Test
    public void verify_correctSecret() {
        // 正例：正确密钥验证通过
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Assertions.assertTrue(CJwtUtils.verify(jwt, SECRET));
    }

    /**
     * 2.2 错误密钥验证失败
     */
    @Test
    public void verify_wrongSecret() {
        // 反例：错误密钥验证失败
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Assertions.assertFalse(CJwtUtils.verify(jwt, "wrong-secret"));
    }

    /**
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
     * 2.4 空 jwt 不校验签名直接返回 false
     */
    @Test
    public void verify_emptyJwt_returnsFalse() {
        // 边界：null/空 jwt 不校验签名，直接返回 false
        Assertions.assertFalse(CJwtUtils.verify(null, SECRET));
        Assertions.assertFalse(CJwtUtils.verify("", SECRET));
    }

    /**
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
     * 3.2 parseJwt：空 jwt 返回 null
     */
    @Test
    public void parseJwt_empty_returnsNull() {
        // 边界：null/空 jwt 返回 null（纯空白串走 split 返回非空数组，不属于此场景）
        Assertions.assertNull(CJwtUtils.parseJwt(null));
        Assertions.assertNull(CJwtUtils.parseJwt(""));
    }

    /**
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
     * 4.2 getJson：null/空数组返回 null
     */
    @Test
    public void getJson_nullArr() {
        // 边界：null/空数组返回 null
        Assertions.assertNull(CJwtUtils.getJson(null, 0));
        Assertions.assertNull(CJwtUtils.getJson(new String[0], 0));
    }

    /**
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
     * 4.5 getHeaderJson：空 jwt 返回 null
     */
    @Test
    public void getHeaderJson_emptyJwt() {
        // 边界：null/空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.getHeaderJson((String) null));
        Assertions.assertNull(CJwtUtils.getHeaderJson(""));
    }

    /**
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
     * 4.7 getBodyJson：空 jwt 返回 null
     */
    @Test
    public void getBodyJson_emptyJwt() {
        // 边界：null/空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.getBodyJson((String) null));
        Assertions.assertNull(CJwtUtils.getBodyJson(""));
    }

    /**
     * 4.8 getBodyJson：body 段为空返回 null
     */
    @Test
    public void getBodyJson_emptySegment_returnsNull() {
        // 边界：body 段为空时返回 null
        Assertions.assertNull(CJwtUtils.getBodyJson(new String[]{"eyJhbGciOiJIUzI1NiJ9", "", "sig"}));
    }

    // ---------- parseHeader / parseBody ----------

    /**
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
     * 5.2 parseHeader：空 jwt 返回 null
     */
    @Test
    public void parseHeader_emptyJwt_returnsNull() {
        // 边界：空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.parseHeader("", Map.class));
        Assertions.assertNull(CJwtUtils.parseHeader(null, Map.class));
    }

    /**
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
     * 6.2 parseBody：空 jwt 返回 null
     */
    @Test
    public void parseBody_emptyJwt_returnsNull() {
        // 边界：空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.parseBody("", Map.class));
        Assertions.assertNull(CJwtUtils.parseBody(null, Map.class));
    }

    /**
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
