package com.xjm.webmagic.dianping;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DianPingInfo extends BaseRowModel {
    @ExcelProperty(value = "公司名称" ,index = 0)
    private String title;
    @ExcelProperty(value = "地址" ,index = 1)
    private String address;
    @ExcelProperty(value = "电话" ,index = 2)
    private String mobile;
    @ExcelProperty(value = "电话2" ,index = 3)
    private String mobile2;
}
