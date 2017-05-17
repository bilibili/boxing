## boxing
---
基于MVP模式的Android多媒体选择器。[![Build Status](https://travis-ci.org/Bilibili/boxing.svg?branch=master)](https://travis-ci.org/Bilibili/boxing)

#### boxing Inside: 
[![bili](screenshot/bili.webp)](https://play.google.com/store/apps/details?id=tv.danmaku.bili)

### 特性
---
- 支持自定义UI
- 支持多/单图片选择和预览，单图裁剪功能
- 支持gif
- 支持视频选择功能
- 提供图片压缩

### Download                                                                  
---
核心版本：只包含核心功能。
实现版本：在核心功能之上添加了实现界面。                                                                                       
- Maven 
  ​                                                  
  核心版本                                                                                                                                            
```xml
<dependency>                                                      
  	<groupId>com.bilibili</groupId>                                    
  	<artifactId>boxing</artifactId>                                    
  	<version>0.7.5</version>                                       
  	<type>pom</type>                                                
</dependency> 
```
实现版本                                                                   
```xml
<dependency>                                                          
  	<groupId>com.bilibili</groupId>                                    
  	<artifactId>boxing-impl</artifactId>                              
  	<version>0.7.5</version>                                       
  	<type>pom</type>                                                  
</dependency>                                                      
```
- Gradle   
  ​                                                    
  核心版本                                                                 
```java                                                                         
compile 'com.bilibili:boxing:0.7.5'                              
```
实现版本                                                                   
```java                                                                        
compile 'com.bilibili:boxing-impl:0.7.5'               
```

### 预览图

![multi_image](screenshot/multi_image.webp)
![single_image_crop](screenshot/single_image_crop.webp)
![video](screenshot/video.webp)


#### 简单用法

- 初始化图片加载（必选）
```java
BoxingMediaLoader.getInstance().init(new IBoxingMediaLoader()); // 需要实现IBoxingMediaLoader 
```
- 初始化图片裁剪（可选）
```java
BoxingCrop.getInstance().init(new IBoxingCrop());  // 需要实现 IBoxingCrop 
```

- 构造参数
  指定模式：图片单选，多选，视频单选，是否支持gif和相机。
```java
BoxingConfig config = new BoxingConfig(Mode); // Mode：Mode.SINGLE_IMG, Mode.MULTI_IMG, Mode.VIDEO
config.needCamera(cameraRes).needGif().withMaxCount(9); // 支持gif，相机，设置最大选图数
.withMediaPlaceHolderRes(resInt) // 设置默认图片占位图，默认无
.withAlbumPlaceHolderRes(resInt) // 设置默认相册占位图，默认无
.withVideoDurationRes(resInt) // 视频模式下，时长的图标，默认无
```
- 初始化Boxing，构造Intent并启动
```java
// 启动缩略图界面, 依赖boxing-impl.
Boxing.of(config).withIntent(context, BoxingActivity.class).start(callerActivity, REQUEST_CODE); 

// 启动预览原图界面，依赖boxing-impl.
Boxing.of(config).withIntent(context, BoxingViewActivity.class).start(callerActivity, REQUEST_CODE); 

// 调用of方法默认使用Mode.MULTI_IMG
Boxing.of().withIntent(context, class).start(callerActivity, REQUEST_CODE);
```

- 取结果
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  	List<BaseMedia> medias = Boxing.getResult(data);
  	//注意判断null
}
```

### 进阶用法
初始化图片加载和裁剪同上。

- 自定义Activity与Fragment
  继承AbsBoxingViewActivity和AbsBoxingViewFragment。
  调用`Boxing.of(config).withIntent(context, class).start(callerActivity, REQUEST_CODE);`启动。

- 仅自定义Fragment
  继承AbsBoxingViewFragment，不依赖AbsBoxingViewActivity。
  调用`Boxing.of(BoxingConfig).setupFragment(AbsBoxingViewFragment, OnFinishListener);`完成配置。

### FileProvider
Android N 版本使用相机必须在AndroidManifest.xml中添加
```xml
<provider                                                 
	android:name="android.support.v4.content.FileProvider"
	android:authorities="${applicationId}.file.provider" >               
	<meta-data                                            
		android:name="android.support.FILE_PROVIDER_PATHS"
		android:resource="@xml/boxing_file_provider"/>
</provider>                 
```

### TODO
支持同时存在多个不同的config。

### License
----
Copyright 2016 Bilibili
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.