package com.ruoyi.system.config;

import com.ruoyi.system.datasource.ClickHouseQueryHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * ClickHouse 查询配置
 * <p>
 * 仅用于导入 ClickHouseQueryHelper，已不需 Bean 声明。
 *
 * @author ruoyi
 */
@Configuration
@Import(ClickHouseQueryHelper.class)
public class ClickHouseQueryConfig {
}
