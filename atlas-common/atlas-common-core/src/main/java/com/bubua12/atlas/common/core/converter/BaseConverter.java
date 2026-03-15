package com.bubua12.atlas.common.core.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础转换器
 *
 * @author bubua12
 * @since 2025/11/21 20:49
 */
public abstract class BaseConverter<VO, PO> {

    public PO vo2po(VO vo) {
        return this.convertV(vo);
    }

    public VO po2vo(PO po) {
        return this.convertP(po);
    }

    public List<PO> vo2po4list(List<VO> voList) {
        return CollectionUtils.isEmpty(voList) ? Collections.emptyList() :
                voList.stream().map(this::vo2po).collect(Collectors.toList());
    }

    public List<VO> po2vo4list(List<PO> poList) {
        return CollectionUtils.isEmpty(poList) ? Collections.emptyList() :
                poList.stream().map(this::po2vo).collect(Collectors.toList());
    }

    @Named("convertT")
    public abstract PO convertV(VO vo);

    @Named("convertP")
    public abstract VO convertP(PO po);
}
