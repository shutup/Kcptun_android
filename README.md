# Kcptun_android
#项目说明
这是一个在安卓平台配置运行kcptun服务的应用，实现代理功能需要配合ShadowSocks使用。
#使用场景
我在使用kcptun的过程中，发现对于shadowsocks确实可以起到提高速度和稳定性的作用，因此希望可以方便的使用它。不过原作者目前只提供了命令行的程序，不是很方便使用，因此我打算做一个安卓的wrapper来包装一下，方便在安卓系统的使用。
按照我的理解，我本来打算安装到手机上，然后用手机上的shadowsocks
来直接连接，结果测试发现并不可行。不过倒是发现跑在手机上后，同一局域网的设备都可以通过它来代理了。考虑到我的路由器是mips架构，而我的电脑无法长时间开机运行，因此我考虑找个小东西来运行kcptun的服务，考虑过后，我发现我的电视盒子的是个不错的选择。因此便有了本项目的诞生。
#使用说明
安装应用

* 下载预编译的APK包
* 下载项目代码自行编译

配置应用

* 第一次运行时先进行配置，配置信息将会保留
* 以后启动APP时会先检查配置，进行连接尝试
