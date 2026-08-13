# language: zh-CN
@内存泄漏修复 @dss-common-server-webank @dss-standard-common
功能: DSS 内存泄漏修复
  作为 DSS 长期运行服务（dss-server、dss-apps-server）的运维与开发人员
  我希望修复 HttpStaffInfoGetter 的 STAFF_INFO_MAP 无限增长与 AppStandardClassUtils 的 ClassLoader 未 close 问题
  以便消除堆内存与 jar 文件句柄的单调累积，避免长期运行后频繁 Full GC 与 Too many open files

  背景:
    假如 DSS 服务已部署 dev-1.23.0-SNAPSHOT（含本次修复代码）
    并且 运行环境为 JDK 8，dss-server 与 dss-apps-server 进程正常启动
    并且 SIT 环境可达，MySQL 数据库连接正常
    并且 ESB HR 员工信息接口可达（EsbConf 中 ESB_APPID/ESB_TOKEN/ESB_HTTP_URL/ESB_HR_STAFF_URL 配置正确）
    并且 至少已安装 2 个 AppConn（如 workflow、scriptis）
    并且 HttpStaffInfoGetter.StaffThread 按 scheduleAtFixedRate 每小时 1 次全量拉取 ESB 员工数据
    而且 AppStandardClassUtils.refreshClassloader 已包含 closeClassLoader 修复逻辑

  # 关联：AC-01 / TC-LEAK-001 / IT-01 / IT-02
  场景: 员工信息连续刷新不累积
    假如 dss-apps-server 正常运行，记录初始 STAFF_INFO_MAP.size() 与堆内存占用
    当 连续等待或手动触发 StaffThread 全量刷新 5 次（模拟 5 小时运行），且第 2 次刷新时 ESB 返回数据集的 key 集合与第 1 次不同（模拟 key 漂移、离职人员未移除）
    那么 每次刷新后 STAFF_INFO_MAP.size() 等于当次 ESB 返回的有效条数，不随刷新次数单调增长
    并且 第 2 次刷新后 STAFF_INFO_MAP 不包含第 1 次独有的 key（旧数据已被 clear）
    而且每次刷新后日志输出包含关键字:
      """
      Staff info refreshed, current size: N
      """
    而且 dump 堆内存用 MAT 分析，HttpStaffInfoGetter 的 ConcurrentHashMap$Node[] 占用回落至单份全量数据大小
    而且不再保持修复前 apps-server 的 23,939,272 bytes（27.79%）残留

  # 关联：AC-01 边界 / TC-LEAK-002 / IT-01
  场景: 员工信息刷新失败保留旧数据
    假如 StaffThread 已成功刷新 1 次，STAFF_INFO_MAP 持有当前全量员工数据
    当 ESB HR 接口请求异常（如网络超时或返回非预期 JSON）
    那么 STAFF_INFO_MAP 不执行 clear()，保持上一次成功刷新的数据
    并且 tempMap 随方法栈销毁，不写入 STAFF_INFO_MAP
    而且日志输出 "fail to get esb response, reason is ..." 且不输出 "Staff info refreshed"
    而且查询接口仍可正常返回旧数据，服务可用性不受影响

  # 关联：AC-04 / TC-LEAK-003 / TC-LEAK-004 / IT-05
  场景: 员工信息查询命中与未命中
    假如 StaffThread 至少成功刷新 1 次，STAFF_INFO_MAP 已填充当前全量员工数据
    当 调用 getStaffInfoByUsername("存在的用户")
    那么 返回对应的 StaffInfo 对象
    当 调用 getStaffInfoByUsername("不存在的用户")
    那么 返回 null
    当 调用 getAllUsers()
    那么 返回 List<StaffInfo>，其大小等于 STAFF_INFO_MAP.size()
    当 调用 getAllUsernames()
    那么 返回 Set<String>，其大小等于 STAFF_INFO_MAP.size()
    当 调用 getAllDepartments()
    那么 返回 List<String>，来源于当前全量数据的去重 orgFullName
    当 调用 getFullOrgNameByUsername("存在的用户")
    那么 返回该用户的 orgFullName
    而且以上 5 个查询接口行为与修复前完全一致（回归不破坏）

  # 关联：AC-04 / TC-LEAK-003
  场景: 员工信息查询-null 入参返回默认部门
    假如 StaffThread 至少成功刷新 1 次，STAFF_INFO_MAP 已填充数据
    当 调用 getStaffInfoByUsername(null)
    那么 返回 null
    当 调用 getFullOrgNameByUsername(null)
    那么 返回 DEFAULT_DEPARTMENT（"基础科技产品部-大数据平台室"）
    而且行为与修复前一致（查询接口的 null 兜底逻辑未改动）

  # 关联：AC-06 / TC-LEAK-005
  场景: 原子替换期间并发读不抛异常
    假如 dss-apps-server 运行中，多线程并发模拟员工信息查询
    当 StaffThread 执行 synchronized(LOCK) 原子替换期间（clear + putAll），并发调用 getStaffInfoByUsername
    那么 查询不抛出异常
    并且 替换瞬间可接受短暂返回 null 或默认值（由 LOCK 保护范围决定）
    而且替换完成后查询返回正确的新数据
    而且不出现数据不一致（如部分新部分旧的中间态崩溃）
    而且 STAFF_INFO_MAP 为 ConcurrentHashMap，读操作无锁，并发安全性未降低

  # 关联：AC-02 / TC-LEAK-006 / IT-03 / IT-04
  场景: AppConn 连续加载卸载 jar 句柄不增长
    假如 dss-server 正常运行，记录初始 jar 文件句柄数
    当 反复执行 AppConn 加载/卸载（appconn-install.sh + appconn-refresh.sh）10 次以上，每次触发 AppStandardClassUtils.refreshClassloader
    那么 jar 文件句柄数在 10 次刷新后保持稳定（允许小幅波动，不单调增长），校验命令为:
      """
      lsof -p <pid> | grep jar | wc -l
      """
    而且日志每次刷新输出包含关键字:
      """
      Closed old URLClassLoader for appConn: ...
      """
    而且 CLASS_LOADER_MAP.size() 与当前已加载 AppConn 数一致
    而且旧 ClassLoader 被 close 后不再持有 jar 文件句柄

  # 关联：AC-02 / TC-LEAK-006 / TC-LEAK-007 / IT-03
  场景: AppConn 刷新 close 旧 ClassLoader 日志验证
    假如 dss-server 中 CLASS_LOADER_MAP 已存在 appConnName=workflow 的 ClassLoader
    当 触发 refreshClassloader("workflow", supplier) 替换旧 ClassLoader
    那么 refreshClassloader 返回新建的 ClassLoader 实例（与旧实例不同）
    并且 旧 ClassLoader 的 close() 被调用（因 AppConnClassLoader instanceof URLClassLoader）
    而且日志输出:
      """
      Closed old URLClassLoader for appConn: ...
      """
    而且 close 在 synchronized(AppStandardClassUtils.class) 同步块内 remove 之后执行，不影响正在使用新 ClassLoader 的线程

  # 关联：AC-02 / TC-LEAK-008
  场景: 非 URLClassLoader 不调用 close 不抛异常
    假如 单元测试环境已搭建，AppStandardClassUtils 注入非 URLClassLoader 类型的 ClassLoader（如系统 AppClassLoader）
    当 触发 closeClassLoader(非 URLClassLoader 实例)
    那么 不抛出 ClassCastException
    并且 不调用 close()（instanceof URLClassLoader 判断为 false）
    而且 refreshClassloader 主流程不受影响，正常返回新 ClassLoader

  # 关联：AC-05 / TC-LEAK-009 / IT-06
  场景: close 异常降级 warn 不阻断刷新
    假如 单元测试环境已搭建，AppStandardClassUtils 注入 mock 的 URLClassLoader
    并且 mock 的 URLClassLoader.close() 抛出 IOException("jar file already deleted")
    当 触发 refreshClassloader(appConnName, supplier)
    那么 refreshClassloader 正常返回新建的 ClassLoader（不抛异常，不阻断主流程）
    并且日志输出 warn 级别:
      """
      Failed to close old URLClassLoader for appConn
      """
    而且 close 失败仅影响句柄释放（降级为依赖 GC 与操作系统回收），不影响 AppConn 加载

  # 关联：AC-03 / TC-LEAK-006 / IT-07
  场景: URLJarFile 实例数下降
    假如 在 prod 或类 prod 环境运行 dss-server，已完成 AC-02 的 AppConn 反复加载/卸载（10 次以上）
    当 dump 堆内存用 MAT 分析 sun.net.www.protocol.jar.URLJarFile 实例数与占用
    那么 URLJarFile 实例数较修复前（prod 568 实例）显著下降
    而且 URLJarFile 占用不再为 MAT Problem Suspect（修复前为 9,190,544 bytes / 10.29%）
    而且连续触发 AppConn 刷新后 URLJarFile 实例数稳定，不随刷新次数增长
    而且随 AppStandardClassUtils.refreshClassloader 的 closeClassLoader 调用同步解决

  # 关联：AC-05 / IT-06
  场景: AppConn 热部署功能回归
    假如 已安装至少 2 个 AppConn（如 workflow、scriptis）
    当 执行 appconn-refresh.sh 热部署刷新
    那么 AppConn 刷新成功，无 ClassNotFoundException 与 NoClassDefFoundError
    并且 工作流创建功能正常
    并且 工作流编辑功能正常
    而且 工作流执行功能正常
    而且第三方系统（Schedulis、Visualis、Qualitis）集成功能正常
    而且CommonAppConnLoader.getAppConn 调用路径与返回的 AppConn 实例行为不变

  # 关联：AC-07 / 边界外待协调
  场景: HDFS ViewFS 功能回归-边界外如实标注
    假如 DSS 侧 HDFS 访问调用点已复用全局 Configuration（若设计阶段确认可修复）
    当 触发工作流产物读写、结果导出等 HDFS 访问操作
    那么 HDFS 读写功能正常
    而且MAT 分析 DSS 侧 Configuration 实例数下降（dev 修复前 39 个实例 / 56,703,128 bytes / 13.86%）
    而且ViewFileSystem 相关功能正常
    而且边界外部分依赖 Linkis 协调（如 fs.viewfs.impl.disable.cache 配置、EngineConn Configuration 创建频率），不强制量化
    而且ViewFileSystem 修复前 dev 环境 4 实例 / 158,190,400 bytes / 38.68% 的归属部分在 Linkis/Hadoop 客户端侧，DSS 不承诺完全消除
