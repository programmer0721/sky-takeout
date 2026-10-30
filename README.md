运营数据导出接口为 `GET /admin/report/export`，它读取 `resources/template/运营数据报表模板.xlsx`，汇总近 30 天（不含当天）的营业额、有效订单数、订单完成率、平均客单价和新增用户数，并填充每日明细，最终以 `business-data.xlsx` 下载给浏览器。
