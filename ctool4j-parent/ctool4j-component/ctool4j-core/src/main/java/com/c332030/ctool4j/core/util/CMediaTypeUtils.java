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

    public static final Set<String> TEXT_KEYS = CSet.of(
            "text",
            "plain",
            "html",
            "json",
            "xml",
            "form"
    );

    public boolean isText(String mediaType) {
        for (val key : TEXT_KEYS) {
            if (mediaType.contains(key)) {
                return true;
            }
        }
        return false;
    }

    public Set<MediaType> getSetWithJson5(Collection<MediaType> mediaTypes) {

        val set = new LinkedHashSet<>(mediaTypes);
        set.add(CMimeTypeEnum.JSON5.getMimeType());
        return set;
    }

    public List<MediaType> getListWithJson5(Collection<MediaType> mediaTypes) {
        return new ArrayList<>(getSetWithJson5(mediaTypes));
    }

}
