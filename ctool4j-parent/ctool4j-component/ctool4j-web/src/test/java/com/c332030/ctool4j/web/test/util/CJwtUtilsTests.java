package com.c332030.ctool4j.web.test.util;

import cn.hutool.jwt.JWTException;
import com.c332030.ctool4j.web.util.CJwtUtils;
import lombok.CustomLog;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * Description: CJwtUtilsTests
 * </p>
 *
 * <p>覆盖 jwt 创建、验证、解析（header/body/JSON 提取）等容易出错的方法</p>
 *
 * @since 2026/8/14
 */
@CustomLog
public class CJwtUtilsTests {

    private static final String SECRET = "test-secret-key-12345";

    // ---------- create ----------

    @Test
    public void create_mapSecret() {
        // 正例：Map body 创建 jwt
        String jwt = CJwtUtils.create(Collections.singletonMap("userId", 1L), SECRET);
        Assertions.assertNotNull(jwt);
        Assertions.assertTrue(jwt.contains("."));
        // 创建后可正常验证
        Assertions.assertTrue(CJwtUtils.verify(jwt, SECRET));
    }

    @Test
    public void create_objectSecret() {
        // 正例：Object body 创建 jwt（经 CBeanUtils.toMap 转换）
        val jwt = CJwtUtils.create(new UserDto(2L, "tom"), SECRET);
        Assertions.assertNotNull(jwt);
        Assertions.assertTrue(CJwtUtils.verify(jwt, SECRET));
    }

    @Test
    public void create_nullSecret_throws() {
        // 异常路径：null secret 抛出 NPE
        Assertions.assertThrowsExactly(
            NullPointerException.class,
            () -> CJwtUtils.create(Collections.singletonMap("a", 1), null)
        );
    }

    @Test
    public void create_emptySecret() {
        // 反例：空 secret 抛出异常（hutool JWTUtil 报 Empty key，JDK HmacSHA256 空 key 抛 InvalidKeyException）
        Assertions.assertThrowsExactly(
            InvalidKeyException.class,
            () -> CJwtUtils.create(Collections.singletonMap("a", 1), "")
        );
    }

    // ---------- verify ----------

    @Test
    public void verify_correctSecret() {
        // 正例：正确密钥验证通过
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Assertions.assertTrue(CJwtUtils.verify(jwt, SECRET));
    }

    @Test
    public void verify_wrongSecret() {
        // 反例：错误密钥验证失败
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Assertions.assertFalse(CJwtUtils.verify(jwt, "wrong-secret"));
    }

    @Test
    public void verify_invalidJwt() {
        // 反例：非法 jwt（段数不足）抛出 JWTException
        Assertions.assertThrowsExactly(
            JWTException.class,
            () -> CJwtUtils.verify("not-a-jwt", SECRET)
        );
    }

    @Test
    public void verify_nullJwt() {
        // 异常路径：null jwt 抛出异常（hutool JWTUtil.verify 对非法输入抛 JWTException）
        Assertions.assertThrowsExactly(
            JWTException.class,
            () -> CJwtUtils.verify(null, SECRET)
        );
    }

    // ---------- parseJwt ----------

    @Test
    public void parseJwt() {
        // 正例：jwt 按 . 拆分三段
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        val arr = CJwtUtils.parseJwt(jwt);
        Assertions.assertNotNull(arr);
        Assertions.assertEquals(3, arr.length);
    }

    @Test
    public void parseJwt_empty_returnsNull() {
        // 边界：null/空 jwt 返回 null（纯空白串走 split 返回非空数组，不属于此场景）
        Assertions.assertNull(CJwtUtils.parseJwt(null));
        Assertions.assertNull(CJwtUtils.parseJwt(""));
    }

    @Test
    public void parseJwt_noDot() {
        // 边界：无 . 的字符串拆分为单元素
        val arr = CJwtUtils.parseJwt("abcdef");
        Assertions.assertNotNull(arr);
        Assertions.assertEquals(1, arr.length);
    }

    // ---------- getJson ----------

    @Test
    public void getJson() {
        // 正例：提取并 base64 解码第 index 段
        String jwt = CJwtUtils.create(Collections.singletonMap("userId", 7L), SECRET);
        val arr = CJwtUtils.parseJwt(jwt);
        val headerJson = CJwtUtils.getJson(arr, 0);
        Assertions.assertNotNull(headerJson);
        Assertions.assertTrue(headerJson.contains("alg"));
    }

    @Test
    public void getJson_nullArr() {
        // 边界：null/空数组返回 null
        Assertions.assertNull(CJwtUtils.getJson(null, 0));
        Assertions.assertNull(CJwtUtils.getJson(new String[0], 0));
    }

    @Test
    public void getJson_outOfIndex() {
        // 边界：越界 index 返回 null
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        val arr = CJwtUtils.parseJwt(jwt);
        Assertions.assertNull(CJwtUtils.getJson(arr, 99));
    }

    // ---------- getHeaderJson / getBodyJson ----------

    @Test
    public void getHeaderJson() {
        // 正例：jwt 字符串提取 header json
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        val headerJson = CJwtUtils.getHeaderJson(jwt);
        Assertions.assertNotNull(headerJson);
        Assertions.assertTrue(headerJson.contains("alg"));
    }

    @Test
    public void getHeaderJson_emptyJwt() {
        // 边界：null/空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.getHeaderJson((String) null));
        Assertions.assertNull(CJwtUtils.getHeaderJson(""));
    }

    @Test
    public void getBodyJson() {
        // 正例：jwt 字符串提取 body json
        String jwt = CJwtUtils.create(Collections.singletonMap("userId", 7L), SECRET);
        val bodyJson = CJwtUtils.getBodyJson(jwt);
        Assertions.assertNotNull(bodyJson);
        Assertions.assertTrue(bodyJson.contains("userId"));
    }

    @Test
    public void getBodyJson_emptyJwt() {
        // 边界：null/空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.getBodyJson((String) null));
        Assertions.assertNull(CJwtUtils.getBodyJson(""));
    }

    @Test
    public void getBodyJson_emptySegment_returnsNull() {
        // 边界：body 段为空时返回 null
        Assertions.assertNull(CJwtUtils.getBodyJson(new String[]{"eyJhbGciOiJIUzI1NiJ9", "", "sig"}));
    }

    // ---------- parseHeader / parseBody ----------

    @Test
    @SuppressWarnings("unchecked")
    public void parseHeader() {
        // 正例：header 解析为 Map（raw Map.class 传参导致 unchecked，测试刻意使用）
        String jwt = CJwtUtils.create(Collections.singletonMap("a", 1), SECRET);
        Map<String, Object> header = CJwtUtils.parseHeader(jwt, Map.class);
        Assertions.assertNotNull(header);
    }

    @Test
    public void parseHeader_emptyJwt_returnsNull() {
        // 边界：空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.parseHeader("", Map.class));
        Assertions.assertNull(CJwtUtils.parseHeader(null, Map.class));
    }

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

    @Test
    public void parseBody_emptyJwt_returnsNull() {
        // 边界：空 jwt 返回 null
        Assertions.assertNull(CJwtUtils.parseBody("", Map.class));
        Assertions.assertNull(CJwtUtils.parseBody(null, Map.class));
    }

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
    public static class UserDto {
        private Long id;
        private String name;

        public UserDto() {
        }

        public UserDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
