package com.system.debugger.data

data class FeatureInfo(
    val id: String,
    val name: String,
    val description: String,
    val requiredPermissions: List<String>,
    val useCases: List<String>,
    val icon: String,
    val color: String
)

object FeatureData {
    val features = listOf(
        FeatureInfo(
            id = "freezer",
            name = "应用冻结器",
            description = "通过 Shizuku 冻结/解冻应用进程，阻止后台运行与自启动，节省电量与内存。",
            requiredPermissions = listOf("Shizuku 权限", "QUERY_ALL_PACKAGES"),
            useCases = listOf("冻结不常用应用", "阻止应用自启动", "释放后台资源"),
            icon = "ac_unit",
            color = "#1E2A50"
        ),
        FeatureInfo(
            id = "privacy",
            name = "隐私盾",
            description = "动态管理应用的敏感权限，一键禁用摄像头、麦克风、定位等权限，保护隐私。",
            requiredPermissions = listOf("Shizuku 权限", "WRITE_SECURE_SETTINGS"),
            useCases = listOf("防止应用偷拍", "禁止后台录音", "隐藏地理位置"),
            icon = "security",
            color = "#0E3A3D"
        ),
        FeatureInfo(
            id = "file",
            name = "文件探险家",
            description = "以系统级权限浏览与管理根目录文件，访问受保护的系统文件与数据目录。",
            requiredPermissions = listOf("Shizuku 权限", "DUMP"),
            useCases = listOf("查看系统日志", "导出应用数据", "清理缓存文件"),
            icon = "folder",
            color = "#2A1A4A"
        ),
        FeatureInfo(
            id = "tuner",
            name = "系统调谐器",
            description = "修改系统底层参数，调整动画速度、DPI、分辨率等，自定义设备外观与性能。",
            requiredPermissions = listOf("Shizuku 权限", "WRITE_SECURE_SETTINGS"),
            useCases = listOf("调整窗口动画速度", "修改屏幕密度", "开启开发者选项"),
            icon = "tune",
            color = "#0E3D2E"
        ),
        FeatureInfo(
            id = "audit",
            name = "安全审计",
            description = "扫描已安装应用的权限使用情况，识别高危应用与敏感行为，提供安全报告。",
            requiredPermissions = listOf("Shizuku 权限", "PACKAGE_USAGE_STATS", "DUMP"),
            useCases = listOf("检测恶意应用", "审查权限滥用", "生成安全报告"),
            icon = "audit",
            color = "#3D2B0E"
        ),
        FeatureInfo(
            id = "automation",
            name = "自动化场景",
            description = "基于触发器实现自动化操作，定时执行冻结/解冻、权限切换等任务，无人值守。",
            requiredPermissions = listOf("Shizuku 权限", "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"),
            useCases = listOf("夜间自动冻结应用", "定时切换隐私模式", "基于位置的自动化"),
            icon = "auto_awesome",
            color = "#3D1A2C"
        )
    )

    fun getById(id: String): FeatureInfo? = features.find { it.id == id }
}