package com.bupt.xrf.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bupt.xrf.dao.IReqGoodDao;
import com.bupt.xrf.entity.ReqGood;
import com.bupt.xrf.service.IReqGoodService;

@Service("reqgoodService")
public class ReqGoodServiceImpl implements IReqGoodService {

	@Autowired
	private IReqGoodDao reqgoodDao;
	
	@Override
	public Map<String, Object> findbypage(int page, int rows) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		List<ReqGood> reqGoods = reqgoodDao.findbypage(page, rows);
		for (ReqGood tmp : reqGoods) {

			Map<String, Object> resulttmp = new HashMap<String, Object>();
			resulttmp.put("gid", tmp.getGood().getGid());
			resulttmp.put("gname", tmp.getGood().getGname());
			resulttmp.put("kind", tmp.getGood().getKind());
			resulttmp.put("code", tmp.getGood().getCode());
			resulttmp.put("amount", tmp.getAmount() + tmp.getGood().getUnit());
			resulttmp.put("deadline", tmp.getRequirement().getDeadline());
			res.add(resulttmp);
		}
		
		result.put("total", reqgoodDao.countall());
		result.put("rows", res);
		return result;
	}

	@Override
	public List<ReqGood> findall() {
		// TODO Auto-generated method stub
		return reqgoodDao.findall();
	}
	
	
}
