# Kcptun_android
#项目说明
这是一个在安卓平台图形化配置并运行kcptun服务的应用，实现代理功能需要配合ShadowSocks使用。

#预编译安装包latest
[预编译安装包latest 下载](https://github.com/shutup/Kcptun_android/releases/latest)

#原理图
![logic](https://github.com/shutup/Kcptun_android/blob/master/logic.png "logic")

#使用场景
* 单机使用（手机上同时运行kcptun_android、shadowsocks）,shadowsocks需使用NAT模式，VPN模式目前不方便实现。
* 提供服务（运行在一个类似于电视盒子的android平台上，给局域网中的其它设备提供加速服务）

#使用说明
安装应用

* 下载预编译的APK包
* 下载项目代码自行编译

配置应用

* 第一次运行时先进行服务器信息配置，配置信息将会保留
* 以后启动APP时会先检查已有配置，进行连接尝试

#感谢
[KcpTun项目](https://github.com/xtaci/kcptun)

