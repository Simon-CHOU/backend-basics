package com.simon.case_study_statistics_by_month.mapper;

import com.simon.case_study_statistics_by_month.domain.StatisticsResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计查询Mapper接口
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 执行统计查询，获取月度累计统计数据
     * @return 统计结果列表
     */
    List<StatisticsResult> selectMonthlyStatistics(@Param("proactiveType") String proactiveType,
                                                   @Param("cooperativeType") String cooperativeType,
                                                   @Param("bfoProjectType") String bfoProjectType);

    /**
     * 插入统计结果到看板统计表
     * @param year 年份
     * @param month 月份
     * @param retailerName 零售商名称
     * @param retailerId 零售商ID
     * @param task 任务类型
     * @param subTask 子任务
     * @param result 统计结果
     * @param unit 单位
     * @param remark 备注
     * @return 插入条数
     */
    int insertDashboardStatistics(String year, String month, String retailerName, String retailerId,
                                String task, String subTask, String result, String unit, String remark);
}