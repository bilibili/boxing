## boxing
---
Android multi-media selector based on MVP mode.[中文文档](README_CN.md)

#### boxing Inside: 
[![bili](screenshot/bili.webp)](https://play.google.com/store/apps/details?id=tv.danmaku.bili)

### Feature
---
- Multiple/single selection, preview and crop function
- Gif support
- Video selection
- Image compression
- Custom UI for selector.

### Download
---
Core version: only contain the core function.

UI version: contain UI implements base on core version.

- Maven

Core version

		<dependency>                       
  		<groupId>com.bilibili</groupId>  
  		<artifactId>boxing</artifactId>  
  		<version>0.1.0</version>
  		<type>pom</type>                 
		</dependency>                      		
		
UI version

		<dependency>                          
  		<groupId>com.bilibili</groupId>     
  		<artifactId>boxing-impl</artifactId>
  		<version>0.1.0</version>   
  		<type>pom</type>                    
		</dependency>                         


- Gradle

Core version              

		compile 'com.bilibili:boxing:0.1.0'
		
UI version

		compile 'com.bilibili:boxing-impl:0.1.0'
		

### Preview

![multi_image](screenshot/multi_image.webp)
![single_image_crop](screenshot/single_image_crop.webp)
![video](screenshot/video.webp)

### Getting Started

- Media loading initialization(required)

		IBoxingMediaLoader loader = new BoxingFrescoLoader(this); // use fresco implements IBoxingMediaLoader
		BoxingMediaLoader.getInstance().init(loader); 

		BoxingMediaLoader.getInstance().displayThumbnail(); // load thumbnail
		BoxingMediaLoader.getInstance().displayRaw(); // load raw image
		
- Image cropping initialization(optional)

		BoxingCrop.getInstance().init(new BoxingUcrop()); // use Ucrop implements IBoxingCrop
		
		BoxingCrop.getInstance().onStartCrop(); //  start cropping
		BoxingCrop.getInstance().onCropFinish(); // get target Uri 

- Build BoxingConfig
Specify the mode(Mode.SINGLE_IMG, Mode.MULTI_IMG, Mode.VIDEO) with camera and gif support. 

		BoxingConfig config = new BoxingConfig(Mode); // Mode：Mode.SINGLE_IMG, Mode.MULTI_IMG, Mode.VIDEO
		config.needCamera().needGif() // camera and gif support
		
		
- Get Boxing, set Intent and call start

		Boxing.of(config).withIntent(context, class).start(callerActivity, REQUEST_CODE);
		// call of() by default use Mode.MULTI_IMG 
		Boxing.of().withIntent(context, class).start(callerActivity, REQUEST_CODE);
    
- Get Result

		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			List<BaseMedia> medias = Boxing.getResult(data);
			// avoid null
		
		}
		
### Advanced usage
Media loading and image cropping initialization are the same as Simple Usage.

- Customize Activity and Fragment
Extends AbsBoxingViewActivity and AbsBoxingViewFragment.
call `Boxing.of(config).withIntent(context, class).start(callerActivity, REQUEST_CODE);` to start.

- Only customize Fragment
Extends AbsBoxingViewFragment,no AbsBoxingViewActivity.
call `Boxing.of(BoxingConfig).setupFragment(AbsBoxingViewFragment, OnFinishListener);` to start.


### FileProvider                                                                                   
Use camera in Android N, add this in AndroidManifest.xml                                                        
                                                                                                  
        <provider                                                                                 
            android:name="android.support.v4.content.FileProvider"                                
            android:authorities="${applicationId}.file.provider" >                                                   
                                                                                                  
            <meta-data                                                                            
                android:name="android.support.FILE_PROVIDER_PATHS"                                
                android:resource="@xml/boxing_file_provider"/>                                    
                                                                                                  	
        </provider>                                                                               
        

### TODO
Support different config at the same moment.

### License
----
Copyright 2017 Bilibili
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.