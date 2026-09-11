package com.c332030.ctool4j.doc.openapi2.test.controller;

import com.c332030.ctool4j.doc.annotation.COperation;
import com.c332030.ctool4j.doc.annotation.CTag;
import com.c332030.ctool4j.doc.openapi2.test.model.CTextEnumTestDTO;
import com.c332030.ctool4j.web.enums.CRequestHeaderEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Description: 枚举 text 展示集成测试用 Controller：覆盖 model 字段枚举（@RequestBody DTO）
 * 与 query 参数枚举（@RequestParam），验证两者枚举允许值保持可提交的「枚举名」、
 * 可读的「枚举名(text)」说明写入 description
 * </p>
 *
 * <h2>功能说明</h2>
 * <p>{@code CTextEnumTestController} 为枚举 text 展示集成测试辅助 Controller（@RestController + @CTag 供 Docket 收集）： 枚举字段/参数均未标注 @CSchema/@CParameter，故 text 说明会写入 description（验证「允许值=枚举名、text 进 description」）：</p>
 * <ul>
 *   <li>{@code body(@RequestBody CTextEnumTestDTO)}：POST {@code /c-text-enum/body}，DTO 含实现 {@code ICText} 的枚举字段</li>
 *   <li>（验证 model 字段枚举允许值可提交、description 展示 text）。</li>
 *   <li>{@code query(@RequestParam CRequestHeaderEnum header)}：POST {@code /c-text-enum/query}，枚举请求参数</li>
 *   <li>（验证 query 参数枚举允许值可提交、description 展示 text）。</li>
 * </ul>
 * <h2>适用场景</h2>
 * <ul>
 *   <li>{@code CTextEnumIntegrationTests} 集成测试，作为真实接口入口验证 model 字段与 query 参数枚举展示。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>测试辅助类；仅提供两个测试接口。</li>
 * </ul>
 *
 * @author c332030
 * @since 1.0
 * @version 1.0
 */
@RestController
@CTag("枚举 text 集成测试")
public class CTextEnumTestController {

    /**
     * body 字段枚举：DTO 含实现 ICText 的枚举字段
     *
     * @param dto 请求体
     * @return 成功标识
     */
    @COperation("枚举 text body")
    @PostMapping("/c-text-enum/body")
    public String body(@RequestBody CTextEnumTestDTO dto) {
        return "ok";
    }

    /**
     * query 参数枚举：@RequestParam 枚举参数
     *
     * @param header 枚举参数
     * @return 成功标识
     */
    @COperation("枚举 text query")
    @PostMapping("/c-text-enum/query")
    public String query(@RequestParam("header") CRequestHeaderEnum header) {
        return "ok";
    }

}
