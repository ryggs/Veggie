package com.greentopli.core.presenter;

import android.content.Context;

import com.greentopli.core.handler.CartDbHandler;
import com.greentopli.core.handler.UserDbHandler;
import com.greentopli.core.presenter.base.BasePresenter;
import com.greentopli.model.OrderHistory;
import com.greentopli.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rnztx on 28/10/16.
 */

public class OrderHistoryPresenter extends BasePresenter<OrderHistoryView> {
	CartDbHandler cartDbHandler;

	public static OrderHistoryPresenter bind(OrderHistoryView mvpView, Context context){
		OrderHistoryPresenter presenter = new OrderHistoryPresenter();
		presenter.attachView(mvpView,context);
		return presenter;
	}
	@Override
	public void attachView(OrderHistoryView mvpView, Context context) {
		super.attachView(mvpView, context);
		cartDbHandler = new CartDbHandler(context);
	}

	public void requestOrderHistory(){
		String userId = new UserDbHandler(getContext()).getSignedUserInfo().getEmail();
		requestOrderHistory(userId);
	}
	
	public void requestOrderHistory(String userId){
		HashMap<Long,Integer> pair = cartDbHandler.getOrderHistoryDates(userId);
		if (pair.size()==0){
			getmMvpView().onEmpty(true);
			return;
		}

		List<OrderHistory> orderHistoryList = new ArrayList<>();

		for (long date : pair.keySet()){
			// create Obj
			OrderHistory orderHistory = new OrderHistory(userId,date);
			List<Product> products = cartDbHandler.getProductsFromCart(true,date);
			if (products.size()>0){
				orderHistory.setProducts(products);
				orderHistory.setTotalItems(products.size());
				orderHistory.setTotalPrice(pair.get(date));
				orderHistoryList.add(orderHistory);
			}
		}

		if (orderHistoryList.size()>0){
			// sort by date
			Collections.sort(orderHistoryList, new Comparator<OrderHistory>() {
				@Override
				public int compare(OrderHistory o1, OrderHistory o2) {
					Date d1 = new Date(o1.getOrderDate());
					Date d2 = new Date(o2.getOrderDate());
//					return d1.compareTo(d2); // Ascending order
					return d2.compareTo(d1); // Descending order
				}
			});

			getmMvpView().onDataReceived(orderHistoryList);
		}
	}
}