package com.metocs.common.datasource.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metocs.common.datasource.mapper.CommonMapper;

/**
 * @author metocs
 * @date 2024/1/30 21:32
 */

public class CommonServiceImpl<T> extends ServiceImpl<CommonMapper<T>,T> implements CommonService<T>{

}
