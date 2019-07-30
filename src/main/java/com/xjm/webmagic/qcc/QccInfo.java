package com.xjm.webmagic.qcc;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QccInfo extends BaseRowModel {
    @ExcelProperty(value = "公司名称" ,index = 0)
    private String title;
    @ExcelProperty(value = "法定代表人" ,index = 1)
    private String realName;
    @ExcelProperty(value = "电话" ,index = 2)
    private String mobile;
}
