android-task1_1
===============

## 题目描述

实现一个Android客户端的页面展示和后台逻辑，从豌豆荚上抓取信息保存到数据库，并实现一个接口，在android客户端向接口请求数据，并将接口返回的数据展示出来。具体说明如下：

### 步骤一

从[豌豆夹](http://m.wandoujia.com)上抓取不少于100个应用的详情信息，存储到数据库(mysql、sqlserver或是其他均可)。

应用详情包括：应用的图标的url，应用名称，类型，大小，下载次数，应用描述等。

以神庙逃亡2为例，抓取地址

http://m.wandoujia.com/apps/com.imangi.templerun2
![神庙逃亡](http://img.wdjimg.com/mms/screenshot/c/91/b591ffb0d34db8a5775a6e90a9c2091c_320_533.jpeg)

抓取的信息有下列几点：

图标地址：http://img.wdjimg.com/mms/icon/v1/d/6a/2a3464e218409b2bd251591bc3ce06ad_48_48.png
名称：神庙逃亡2：Temple Run 2
类型：动作竞技 
大小：25.15M
下载次数：1126万次安装
应用描述：
微信是一款手机通信软件，支持通过手机网络发送语音…...

### 步骤二

写一个接口(在自己本机或是远程服务器均可)，用PHP或是其他语言均可。接口可以有一个GET参数(翻页用，名字任意)，也可不要。接口返回刚才抓到数据库中的数据，格式可以是XML或是JSON的：

返回数据的JSON的格式可以参考下面的：

```
[
	{
		name: "UC浏览器",
		package: "com.UCMobile",
		icon: "http://img.wdjimg.com/app/icons/1/59/35b91e11fd900b9cb8750afa34467591_48x48nq2.png",
		type: "浏览器",
		download_url: "http://apps.wandoujia.com/redirect?url=http%3A%2F%2Fpmt.wdjcdn.com%2Fstatic%2Fuploads%2Fa%2F310%2Fcom.UCMobile.1345618782317.apk&pos=mobileweb/detail//noref",
		size: "7.01 MB",
		download_times: "21727600"
	},
	{
		name: "布卡漫画",
		package: "cn.ibuka.manga.ui",
		icon: "http://img.wdjimg.com/app/icons/a/e3/6b7a527fdce1a5ffc9afe1179e063e3a_48x48nq2.png",
		type: "阅读",
		download_url: "http://apps.wandoujia.com/redirect?url=http%3A%2F%2Fm.163.com%2Fsoftware%2Fdownload%3Fprod%3D3gmarket%26id%3D12383%26pf%3D2%26cg%3DCA8MTH2H9N5%26sign%3D849a8c4337e19a40a80da1a640d268b9%26url%3Dhttp%253A%252F%252Ffile.m.163.com%252Fapp%252Ffree%252F201208%252F08%252Fbuka_setup.apk&pos=mobileweb/detail//noref",
		size: "1.12 MB",
		download_times: "473890"
	}
]
```


### 步骤三

搭建android开发环境，根据第二步写的接口，做一个简单的android客户端的演示，解析接口返回的数据并展示如下（由于数据过多，建议分页）。点击应用跳转到新的页面用于展示详细信息（下面未给出图片）

### 注意

由于插入数据过多，所以推荐使用php写爬虫爬下数据，并且自己做接口展示。作为一名android的程序员，做客户端时能够自己写接口能够省去对接口和等接口的很多麻烦。
但是本次主要考察android应用展示。若觉得接口实现难度过大，可手动插入数据优先展示客户端。
