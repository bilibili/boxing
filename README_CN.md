## boxing
---
基于MVP模式的Android多媒体选择器。

#### boxing Inside: 
[![bili](screenshot/bili.webp)](https://play.google.com/store/apps/details?id=tv.danmaku.bili)

### 特性
---
- 支持多/单图片选择和预览，单图裁剪功能
- 支持gif
- 支持视频选择功能
- 提供图片压缩
- 支持自定义UI

### Download                                                                  
---                                                                           
核心版本：只包含核心功能。
                                 
实现版本：在核心功能之上添加了实现界面。                  
                                                                              
- Maven
                                                                       
核心版本                                                                 
                                                                              
		<dependency>                                                              
  		<groupId>com.bilibili</groupId>                                         
  		<artifactId>boxing</artifactId>                                         
  		<version>0.1.0</version>                                       
  		<type>pom</type>                                                        
		</dependency>                      		                                    
		                                                                          
实现版本                                                                   
                                                                              
		<dependency>                                                              
  		<groupId>com.bilibili</groupId>                                         
  		<artifactId>boxing-impl</artifactId>                                    
  		<version>0.1.0</version>                                       
  		<type>pom</type>                                                        
		</dependency>                                                             
                                                                              
                                                                              
- Gradle
                                                                    
核心版本                                                                 
                                                                              
		compile 'com.bilibili:boxing:0.1.0'                              
		                                                                          
实现版本                                                                   
                                                                              
		compile 'com.bilibili:boxing-impl:0.1.0'               
		          

### 预览图

![multi_image](screenshot/multi_image.webp)
![single_image_crop](screenshot/single_image_crop.webp)
![video](screenshot/video.webp)


#### 简单用法

- 初始化图片加载（必选）

		IBoxingMediaLoader loader = new BoxingFrescoLoader(this); // 使用fresco实现IBoxingMediaLoader
		BoxingMediaLoader.getInstance().init(loader); 

		BoxingMediaLoader.getInstance().displayThumbnail(); // 加载缩略图
		BoxingMediaLoader.getInstance().displayRaw(); //加载原始图
		
- 初始化图片裁剪（可选）


		BoxingCrop.getInstance().init(new BoxingUcrop()); // 使用Ucrop实现IBoxingCrop
		
		BoxingCrop.getInstance().onStartCrop(); // 调用裁剪
		BoxingCrop.getInstance().onCropFinish(); // 取到结果Uri 

- 构造参数
指定模式：图片单选，多选，视频单选，是否支持gif和相机。


		BoxingConfig config = new BoxingConfig(Mode); // Mode：Mode.SINGLE_IMG, Mode.MULTI_IMG, Mode.VIDEO
		config.needCamera().needGif() // 支持gif和相机
		
		
- 初始化Boxing，构造Intent并启动


		Boxing.of(config).withIntent(context, class).start(callerActivity, REQUEST_CODE);
    
		// 提供一个多图选择模式的of重载
		Boxing.of().withIntent(context, class).start(callerActivity, REQUEST_CODE);
    
- 取结果
  

		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			List<BaseMedia> medias = Boxing.getResult(data);
			//注意判断null
		
		}
		
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

        <provider                                                 
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.file.provider" >                   
                                                                  
            <meta-data                                            
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/boxing_file_provider"/>    
                                                                  
        </provider>                 
                                      

### TODO
支持同时存在多个不同的config。

### License
----
Copyright 2016 Bilibili
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.