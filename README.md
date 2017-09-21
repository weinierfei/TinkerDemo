## 概述
[Tinker](https://github.com/Tencent/tinker)是微信团队开源的Android热修复解决方案,我们主要将这项技术用于在不发版的情况修复线上app存在的问题.

我们的项目一直使用Jenkins构建来完成上线,在接入Tinker后我们也希望在发布补丁的时候也能通过Jenkins一键构建完成,因此有了这篇文字.第一次写博客,如有不足之处,请多多指正.

## Tinker的接入
Tinker官方[文档](https://github.com/Tencent/tinker/wiki)给出了详细的接入教程,然后很多同学在接入上还是觉得太难,太麻烦!我在接入的时候,采取的简单粗暴的方式,直接照搬官方demo[tinker-sample-android](https://github.com/Tencent/tinker/tree/master/tinker-sample-android)来完成接入.由于要在多个项目中使用,因此将其当做一个library提供给多个项目依赖;如图:

![Paste_Image.png](http://upload-images.jianshu.io/upload_images/442695-4b941fc91356d1a6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

本文demo地址: [TinkerDemo](https://github.com/weinierfei/TinkerDemo)

#### 用法
**下面的做法建立在Application改造完成的前提下**

1.主工程的[build.gradle](https://github.com/weinierfei/TinkerDemo/blob/master/build.gradle)文件添加tinker插件和资源混淆组件(AndResGuard)插件

```
classpath 'com.tencent.mm:AndResGuard-gradle-plugin:1.2.3'
classpath "com.tencent.tinker:tinker-patch-gradle-plugin:${TINKER_VERSION}"
```
2.主Module依赖tinkelib

3.拷贝config文件到根目录

4.主Module引入tinker.gradle和resguard.gradle配置

```
apply from: '../config/resguard.gradle'
apply from: '../config/tinker.gradle'
```
5.TINKER_ID的配置

5.1  build.gradle文件末尾添加,通过git commit来作为TINKER_ID

```
def getTinkerIdValue() {
    return hasProperty("TINKER_ID") ? TINKER_ID : gitSha()
}

def gitSha() {
    try {
        String gitRev = 'git rev-parse --short HEAD'.execute(null, project.rootDir).text.trim()
        if (gitRev == null) {
            throw new GradleException("can't get git rev, you should add git to system path or just input test value, such as 'testTinkerId'")
        }
        return gitRev
    } catch (Exception e) {
        throw new GradleException("can't get git rev, you should add git to system path or just input test value, such as 'testTinkerId'")
    }
}
```
5.2  主module的defaultConfig中添加

```
buildConfigField "String", "TINKER_ID", "\"${getTinkerIdValue()}\""
```
注:当然有人想用app版本号作为TINKER_ID,只要将getTinkerIdValue()改为对应的版本号即可

6.运行**gradle resguardRelease**命令,即可生成发布包(基准包),同时在工程根目录下会自动备份tinker需要的文件(apk、R、mapping、Resource mapping);
图:
![Paste_Image.png](http://upload-images.jianshu.io/upload_images/442695-e106446ca21a2105.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

7.生成补丁时,只需要执行**gradle tinkerPatchRelease**命令即可

至此,gradle的相关配置就完成了!

## 与Jenkins的结合使用
Jenkins的搭建及相关用法请自行google.

我们的Jenkins有一个线上job,由于我们使用master分支上线,因此在Jenkins的Branch Specifier中保持master不变
![Paste_Image.png](http://upload-images.jianshu.io/upload_images/442695-4d0f9bab75cc7dc3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

发布版本的时候使用**clean resguardRelease**命令,如图:
![Paste_Image.png](http://upload-images.jianshu.io/upload_images/442695-d575c7f9ab5c4127.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

发布补丁时将tasks修改为**tinkerPatchRelease**即可

我们的渠道包使用美团的旧版生成方案,这一步工作是在服务器将apk拷入相应文件夹来完成,同时再执行一些其他脚本;
### demo地址
 [TinkerDemo](https://github.com/weinierfei/TinkerDemo)
## 参考
tinker.gradle中的备份脚本来着[w4lle](https://w4lle.github.io/)的[Gradle模块化配置](https://w4lle.github.io/2017/01/22/gradle-modules/);感谢w4lle同学
