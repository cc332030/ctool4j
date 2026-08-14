package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.enums.CMimeTypeEnum;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.http.MediaType;

import java.util.*;

/**
 * <p>
 * Description: CMediaTypeUtils
 * </p>
 *
 * @since 2025/9/21
 */
@UtilityClass
public class CMediaTypeUtils {

    /**
     * 文本类型关键字集合
     */
    public static final Set<String> TEXT_KEYS = CSet.of(
            "text",
            "plain",
            "html",
            "json",
            "xml",
            "form"
    );

    /**
     * 判断媒体类型是否为文本类型
     *
     * @param mediaType 媒体类型字符串
     * @return 是否为文本类型
     */
    public boolean isText(String mediaType) {
        for (val key : TEXT_KEYS) {
            if (mediaType.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在媒体类型集合中加入 JSON5
     *
     * @param mediaTypes 媒体类型集合
     * @return 加入 JSON5 后的有序集合
     */
    public Set<MediaType> getSetWithJson5(Collection<MediaType> mediaTypes) {

        val set = new LinkedHashSet<>(mediaTypes);
        set.add(CMimeTypeEnum.JSON5.getMimeType());
        return set;
    }

    /**
     * 在媒体类型集合中加入 JSON5，返回列表
     *
     * @param mediaTypes 媒体类型集合
     * @return 加入 JSON5 后的列表
     */
    public List<MediaType> getListWithJson5(Collection<MediaType> mediaTypes) {
        return new ArrayList<>(getSetWithJson5(mediaTypes));
    }

}
