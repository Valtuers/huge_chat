window.app = {
	/**
	 * 后端地址
	 */
	// serverUrl: "http://172.16.2.200:8011",
	serverUrl: "http://192.168.43.102:8011",
	/**
	 * 图片服务器的url地址
	 */
	// imgServerUrl: "http://172.16.2.130:88/hugeChat/",
	// imgServerUrl: "http://192.168.145.129:88/hugeChat/",
	// imgServerUrl: "http://172.16.2.200:8011",
	imgServerUrl: "http://192.168.43.102:8011",
	/**
	 * 判断字符串是否为空
	 * true: 不为空
	 * false: 为空
	 */
	isNotNull: function(str){
		if(str != null && str != "" && str != undefined){
			return true;
		}
		return false;
	},
	/**
	 * 封装消息提示框，默认mui的不支持居中和自定义icon，所以使用h5+
	 */
	showToast: function(msg,type){
		plus.nativeUI.toast(msg,{icon: "image/"+type+".png",verticalAlign:"center"})
	},
	/**
	 * 保存用户的全局对象
	 */
	setUserGlobalInfo: function(user){
		var userInfoStr = JSON.stringify(user);
		plus.storage.setItem("userInfo",userInfoStr);
	},
	/**
	 * 获取全局对象
	 */
	getUserGlobalInfo: function(){
		return JSON.parse(plus.storage.getItem("userInfo"));
	},
	/**
	 * 登出后，移除用户全局对象
	 */
	userLogout: function(){
		plus.storage.removeItem("userInfo");
	},
	/**
	 * 保存用户的联系人列表
	 */
	setContactList: function(contactList){
		var contactListStr = JSON.stringify(contactList);
		plus.storage.setItem("contactList",contactListStr);
	},
	/**
	 * 获取联系人列表
	 */
	getContactList: function(){
		var contactListStr = plus.storage.getItem("contactList");
		if(!this.isNotNull(contactListStr)){
			return [];
		}else{
			return JSON.parse(contactListStr);
		}
	}
};