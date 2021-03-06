package com.bupt.xrf.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import com.bupt.xrf.entity.ReqGood;
import com.bupt.xrf.entity.Warehouse;
import com.bupt.xrf.entity.WhGood;

public class Idealpoints {
	
	private DistanceUtil distanceUtil = new DistanceUtil();
	private Sortwhbytimeasc sortwhbytimeasc = new Sortwhbytimeasc();
	
	private List<Warehouse> warehouses = null;
	private List<WhGood> whGoods = null;
	private List<ReqGood> reqGoods = null;
	private List<Warehouse> newwh = null;
	
	private double Tmin = 0;
	private double Tmax = 0;
	private double Cmin = 0;
	private double Cmax = 0;
	
	public Idealpoints(List<Warehouse> tmp1, List<WhGood> tmp2, List<ReqGood> tmp3) {
		warehouses = tmp1;
		whGoods = tmp2;
		reqGoods = tmp3;
		decomposeWh(warehouses);
		
	}
	
	public void algorithmformintime() {
		Tmin = getMintime();
//		for (WhGood tmp : whGoods) {
//			if(tmp.getIfuse().equals("1"))
//				System.out.println(tmp);
//		}
	}
	
	public List<Warehouse> algorithm(){
		Collections.sort(newwh, sortwhbytimeasc);
		return null;
	}
	
	public void adjustWh(List<Warehouse> warehouses, int time){
		Iterator<Warehouse> iterator = warehouses.iterator();
		while(iterator.hasNext()){
			if(iterator.next().getTimetoarrive() > time)
				iterator.remove();
		}
	}
	
	public void removeWh(List<Warehouse> warehouses, String wid){
		Iterator<Warehouse> iterator = warehouses.iterator();
		while(iterator.hasNext()){
			if(iterator.next().getWid().equals(wid))
				iterator.remove();
		}
	}
	
	public void decomposeWh(List<Warehouse> warehouses){
		newwh = new ArrayList<>();
		for (Warehouse tmp : warehouses) {
			String coordinate = tmp.getCoordinate();
			String[] coor = coordinate.split(",");
			Double lon = Double.valueOf(coor[0]);
			Double lat = Double.valueOf(coor[1]);
			String tools = tmp.getTransportation();
			if(tools.charAt(0) == '1'){
				Warehouse warehouse = new Warehouse();
				warehouse.setWid(tmp.getWid());			
				warehouse.setTimetoarrive(distanceUtil.GetDistance(lon, lat, 103.048991, 30.016365)*60/60);
				warehouse.setTool("汽车");
				newwh.add(warehouse);
			}
			if(tools.charAt(1) == '1'){
				Warehouse warehouse = new Warehouse();
				warehouse.setWid(tmp.getWid());			
				warehouse.setTimetoarrive(distanceUtil.GetDistance(lon, lat, 103.048991, 30.016365)*60/80);
				warehouse.setTool("火车");
				newwh.add(warehouse);		
			}
			if(tools.charAt(2) == '1'){
				Warehouse warehouse = new Warehouse();
				warehouse.setWid(tmp.getWid());			
				warehouse.setTimetoarrive(distanceUtil.GetDistance(lon, lat, 103.048991, 30.016365)*60/500);
				warehouse.setTool("飞机");
				newwh.add(warehouse);
			}
		}
	}
	
	public double getMintime(){
		double min = 0;
		Collections.sort(newwh, sortwhbytimeasc);
		List<String> result = new ArrayList<>();
		List<ReqGood> curgoods = freshcurgoods();
		int i = 0;
		while(ifEnough(curgoods) == false){
			boolean flag = false;
			Warehouse wh = newwh.get(i);

			for(int k = 0 ; k < reqGoods.size() ; k++){
				ReqGood reqGood = reqGoods.get(k);
				int needamount = Integer.valueOf(reqGood.getAmount());
				int hasamount = findgoodbywh(wh, reqGood);
				if (hasamount != 0) {
					int curamount = Integer.valueOf(curgoods.get(k).getAmount());
					if(curamount == needamount){
						continue;
					}
					else{
						int afteradd = Math.min(curamount+hasamount, needamount);
						curgoods.get(k).setAmount(afteradd+"");
						setgoodbywh(wh, reqGood, Math.min(hasamount, needamount-curamount));
						flag = true;
						min = Math.max(wh.getTimetoarrive(), min);
					}
				}
			}
			if(flag == false){
				i++;
				continue;
			}
			else{
				result.add(wh.getWid());
				setwh(wh);
				removeWh(newwh, wh.getWid());
				i = 0;
			}
		}
		return min;
	}
	
	public double getMaxtime(){
		Iterator<Warehouse> iterator = newwh.iterator();
		double max = 0;
		while(iterator.hasNext()){
			double tmp = iterator.next().getTimetoarrive();
			if(tmp > max)
				max = tmp;
		}
		return max;
	}
	
	public int getMincost(List<Warehouse> warehouses, List<WhGood> whGoods){
		return 0;
	}
	
	public int getMaxcost(List<Warehouse> warehouses, List<WhGood> whGoods){
		return 0;
	}
	
	public boolean ifEnough(List<ReqGood> curgoods){
		for(int i = 0 ; i < reqGoods.size() ; i++){
			if(reqGoods.get(i).getAmount().equals(curgoods.get(i).getAmount()) == false)
				return false;
		}
		return true;
	}
	
	public int findgoodbywh(Warehouse warehouse, ReqGood reqGood){
		int result = 0;
		for (WhGood tmp : whGoods) {
			if(tmp.getGood().getGid().equals(reqGood.getGood().getGid()) && tmp.getWarehouse().getWid().equals(warehouse.getWid())){
				result = tmp.getAmount();
				break;
			}
		}
		return result;
	}
	
	private void setgoodbywh(Warehouse wh, ReqGood reqGood, int usecount) {
		for (WhGood tmp : whGoods) {
			if(tmp.getGood().getGid().equals(reqGood.getGood().getGid()) && tmp.getWarehouse().getWid().equals(wh.getWid())){
				tmp.setIfuse("1");
				tmp.setUsecount(usecount);
				break;
			}
		}
	}
	
	private void setwh(Warehouse wh){
		for (Warehouse warehouse : warehouses) {
			if(warehouse.getWid().equals(wh.getWid())){
				warehouse.setFlag("1");
				warehouse.setTool(wh.getTool());
				warehouse.setTimetoarrive(wh.getTimetoarrive());
			}
		}
	}
	
	public void freshnewwh(){
		decomposeWh(warehouses);
	}
	
	public List<ReqGood> freshcurgoods(){
		List<ReqGood> curgoods = new ArrayList<>();
		for(int i = 0 ; i < reqGoods.size() ; i++){
			ReqGood tmp = new ReqGood();
			tmp.setGood(reqGoods.get(i).getGood());
			tmp.setRequirement(reqGoods.get(i).getRequirement());
			tmp.setAmount("0");
			curgoods.add(tmp);
		}
		return curgoods;
	}

}
