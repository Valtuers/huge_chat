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
	 * netty服务后端发布的url地址
	 */
	nettyServerUrl: "ws://192.168.43.102:8088/ws",
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
	},
	/**
	 * 根据用户id，从本地缓存(联系人列表)中获取朋友的信息
	 * @param {Object} friendId
	 */
	getFriendFromContactList: function(friendId){
		var contactListStr = plus.storage.getItem("contactList");
		//判断是否为空
		if(this.isNotNull(contactListStr)){
			//不为空，则把用户信息返回
			var contactList = JSON.parse(contactListStr);
			for(var i=0;i<contactList.length;i++){
				var friend = contactList[i];
				if(friend.friendUserId == friendId){
					return friend;
					break;
				}
			}
		}else{
			return null;
		}
		
	},
	/**
	 * 用于保存用户的聊天记录
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag 判断本条消息是我发送的，还是朋友发送，1：我，2：朋友
	 */
	saveUserChatHistory: function(myId,friendId,msg,flag){
		var me = this;
		var chatKey = "chat-"+myId+"-"+friendId;
		//从本地缓存获取聊天记录是否存在
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		var chatHistoryList;
		if(me.isNotNull(chatHistoryListStr)){
			//如果不为空
			chatHistoryList = JSON.parse(chatHistoryListStr);
		}else{
			//如果为空，赋一个空的list
			chatHistoryList = [];
		}
		//构建聊天记录对象
		var singleMsg = new me.ChatHistory(myId,friendId,msg,flag);
		//向list中追加msg对象
		chatHistoryList.push(singleMsg);
		plus.storage.setItem(chatKey,JSON.stringify(chatHistoryList));
	},
	/**
	 * 获取用户聊天记录
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	getUserChatHistory: function(myId,friendId){
		var me = this;
		var chatKey = "chat-"+myId+"-"+friendId;
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		
		if(me.isNotNull(chatHistoryListStr)){
			//如果不为空
			return JSON.parse(chatHistoryListStr);
		}else{
			//如果为空，赋一个空的list
			return [];
		}
	},
	/**
	 * 删除我和朋友的聊天记录
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	deleteUserChatHistory: function(myId,friendId){
		var chatKey = "chat-"+myId+"-"+friendId;
		plus.storage.removeItem(chatKey);
		
	},
	/**
	 * 聊天记录的快照，仅仅保存每次和朋友聊天的最后一条消息
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead
	 */
	saveUserChatSnapshot: function(myId,friendId,msg,isRead){
		var me = this;
		var shapshotKey = "chat-snapshot-"+myId;
		
		//从本地缓存获取聊天快照记录是否存在
		var chatSnapshotListStr = plus.storage.getItem(shapshotKey);
		var chatSnapshotList;
		if(me.isNotNull(chatSnapshotListStr)){
			//如果不为空
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			//循环快照list，并且判断每个元素是否包含(匹配)friendId，如果匹配，则删除
			for(var i=0;i<chatSnapshotList.length;i++){
				if(chatSnapshotList[i].friendId == friendId){
					//删除已经存在的friendId所对应的快照对象
					chatSnapshotList.splice(i,1);
					break;
				}
			}
		}else{
			//如果为空，赋一个空的list
			chatSnapshotList = [];
		}
		//构建聊天快照对象
		var singleMsg = new me.ChatSnapshot(myId,friendId,msg,isRead);
		//向list中追加msg对象
		chatSnapshotList.unshift(singleMsg);
		plus.storage.setItem(shapshotKey,JSON.stringify(chatSnapshotList));
	},
	/**
	 * 获取用户聊天快照列表
	 * @param {Object} myId
	 */
	getUserChatSnapshot: function(myId){
		var me = this;
		var shapshotKey = "chat-snapshot-"+myId;
		var chatSnapshotListStr = plus.storage.getItem(shapshotKey);
		
		if(me.isNotNull(chatSnapshotListStr)){
			//如果不为空
			return JSON.parse(chatSnapshotListStr);
		}else{
			//如果为空，赋一个空的list
			return [];
		}
	},
	/**
	 * 删除我和朋友的聊天快照
	 * @param {Object} myId
	 * @param {Object} friendId
	 */
	deleteUserChatSnapshot: function(myId,friendId){
		var me = this;
		var shapshotKey = "chat-snapshot-"+myId;
		var chatSnapshotListStr = plus.storage.getItem(shapshotKey);
		var chatSnapshotList;
		if(me.isNotNull(chatSnapshotListStr)){
			//如果不为空
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			//循环这个list，判断是否存在好友，比对friendId
			//如果有，在list中的原有位置删除该快照对象
			for(var i=0;i<chatSnapshotList.length;i++){
				if(chatSnapshotList[i].friendId == friendId){
					//删除已经存在的friendId所对应的快照对象
					chatSnapshotList.splice(i,1);
					break;
				}
			}
			//替换原有的快照列表
			plus.storage.setItem(shapshotKey,JSON.stringify(chatSnapshotList));
		}else{
			return;
		}
	},
	/**
	 * 标记未读消息为已读状态
	 * @param {Object} myid
	 * @param {Object} friendId
	 */
	readUserChatSnapshot: function(myId,friendId){
		var me = this;
		var shapshotKey = "chat-snapshot-"+myId;
		var chatSnapshotListStr = plus.storage.getItem(shapshotKey);
		var chatSnapshotList;
		if(me.isNotNull(chatSnapshotListStr)){
			//如果不为空
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			//循环这个list，判断是否存在好友，比对friendId
			//如果有，在list中的原有位置删除该快照对象，然后重新放入标记已读的快照对象
			for(var i=0;i<chatSnapshotList.length;i++){
				if(chatSnapshotList[i].friendId == friendId){
					//删除已经存在的friendId所对应的快照对象
					chatSnapshotList[i].isRead = true;		//标记为已读
					break;
				}
			}
			//替换原有的快照列表
			plus.storage.setItem(shapshotKey,JSON.stringify(chatSnapshotList));
		}else{
			return;
		}
		
	},
	/**
	 * 和后端的枚举对应
	 */
	CONNECT: 1,		//第一次(或重连)初始化连接
	CHAT: 2,		//聊天消息
	SIGNED: 3,		//消息签收
	KEEPALIVE: 4,	//客户端保持心跳
	PULL_FRIEND: 5,	//拉取好友
	/**
	 * 和后端的ChatMsg 聊天模型对象保持一致
	 * @param {Object} senderId
	 * @param {Object} receiverId
	 * @param {Object} msg
	 * @param {Object} msgId
	 */
	ChatMsg: function(senderId,receiverId,msg,msgId){
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.msg = msg;
		this.msgId = msgId;
	},
	/**
	 * 构建DataContent消息模型对象
	 * @param {Object} action
	 * @param {Object} chatMsg
	 * @param {Object} extend
	 */
	DataContent: function(action,chatMsg,extend){
		this.action = action;
		this.chatMsg = chatMsg;
		this.extend = extend;
	},
	/**
	 * 单个聊天记录的对象
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} flag
	 */
	ChatHistory: function(myId,friendId,msg,flag){
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.flag = flag;
	},
	/**
	 * 快照对象
	 * @param {Object} myId
	 * @param {Object} friendId
	 * @param {Object} msg
	 * @param {Object} isRead 用于判断消息是已读或未读
	 */
	ChatSnapshot: function(myId,friendId,msg,isRead){
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.isRead = isRead;
	}
};