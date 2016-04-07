package com.javachina.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blade.ioc.annotation.Inject;
import com.blade.ioc.annotation.Service;
import com.blade.jdbc.AR;
import com.javachina.kit.DateKit;
import com.javachina.model.Notice;
import com.javachina.model.Topic;
import com.javachina.model.User;
import com.javachina.service.NoticeService;
import com.javachina.service.TopicService;
import com.javachina.service.UserService;

import blade.kit.StringKit;

@Service
public class NoticeServiceImpl implements NoticeService {
	
	@Inject
	private TopicService topicService;
	
	@Inject
	private UserService userService;
	
	@Override
	public boolean save(String type, Long uid, Long to_uid, Long event_id) {
		if(StringKit.isNotBlank(type) && null != uid && null != to_uid && null != event_id){
			AR.update("insert into t_notice(type, uid, to_uid, event_id, create_time) values(?, ?, ?, ?, ?)", type,
					uid, to_uid, event_id, DateKit.getCurrentUnixTime()).executeUpdate();
			return true;
		}
		return false;
	}
	
	@Override
	public List<Map<String, Object>> getNotices(Long uid) {
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		if(null != uid){
			
			List<Notice> notices = AR.find("select * from t_notice where is_read = 0 and to_uid = ?", uid).list(Notice.class);
			
			if(null != notices && notices.size() > 0){
				for(Notice notice : notices){
					Map<String, Object> map = this.getNotice(notice);
					if(null != map && !map.isEmpty()){
						result.add(map);
					}
				}
			}
		}
		return result;
	}
	
	private Map<String, Object> getNotice(Notice notice){
		if(null == notice){
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		Long uid = notice.getUid();
		User user = userService.getUser(uid);
		Topic topic = topicService.getTopic(notice.getEvent_id());
		if(null == topic || null == user){
			return null;
		}
		map.put("tid", notice.getEvent_id());
		map.put("type", notice.getType());
		String title = topic.getTitle();
		map.put("title", title);
		map.put("user_name", user.getLogin_name());
		return map;
	}

	@Override
	public boolean read(Long to_uid) {
		if(null != to_uid){
			// 删除
			try {
				AR.update("update t_notice set is_del = 1 where to_uid = ?", to_uid).executeUpdate();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
