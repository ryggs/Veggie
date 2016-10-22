package com.greentopli.app.user;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.greentopli.app.R;
import com.greentopli.core.handler.CartDbHandler;
import com.greentopli.model.Product;
import com.greentopli.model.PurchasedItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rnztx on 20/10/16.
 */

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.ViewHolder>{
	private List<Product> mProducts;
	CartDbHandler cartDbHandler;
	private boolean extraControls;

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		@BindView(R.id.item_product_image) ImageView image;
		@BindView(R.id.item_product_checkbox) CheckBox checkBox;
		@BindView(R.id.item_product_name) TextView name;
		@BindView(R.id.item_product_price) TextView price;
		@BindView(R.id.subtract_image_button)ImageButton subtract;
		@BindView(R.id.add_image_button)ImageButton add;
		private Product product;
		private PurchasedItem cartItem;
		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this,itemView);
			itemView.setOnClickListener(this);
			checkBox.setVisibility(extraControls ?View.GONE:View.VISIBLE);
			subtract.setVisibility(extraControls ?View.VISIBLE:View.GONE);
			add.setVisibility(extraControls ?View.VISIBLE:View.GONE);
		}

		@Override
		public void onClick(View v) {
			if (v.getId()==R.id.item_product_view && !extraControls)
				updateToCart();
		}

		@OnClick(R.id.add_image_button)
		void onVolumeAdded(){
			int newVolume = cartItem.getVolume() + product.getVolumeSet();
			if (newVolume <= product.getMaximumVolume()) {
				cartDbHandler.updateVolume(product.getId(), newVolume);
				notifyItemChanged(getAdapterPosition());
			}
		}
		@OnClick(R.id.subtract_image_button)
		void onVolumeSubtracted(){
			int newVolume = cartItem.getVolume() - product.getVolumeSet();
			if (newVolume >= product.getMinimumVolume()) {
				cartDbHandler.updateVolume(product.getId(), newVolume);
				notifyItemChanged(getAdapterPosition());
			}
		}

		private void updateToCart(){
			Product product = mProducts.get(getAdapterPosition());
			if (checkBox.isChecked()){
				cartDbHandler.removeProductFromCart(product.getId());
				checkBox.setChecked(false);
			}else {
				cartDbHandler.addProductToCart(product.getId(),product.getMinimumVolume());
				checkBox.setChecked(true);
			}
		}

		public void setProduct(Product product) {
			this.product = product;
		}

		public void setCartItem(PurchasedItem cartItem) {
			this.cartItem = cartItem;
		}
	}

	public BrowseAdapter(){
		this(new ArrayList<Product>());
		extraControls = false;
	}

	public BrowseAdapter(boolean extraControls){
		this(new ArrayList<Product>());
		this.extraControls = extraControls;
	}

	public BrowseAdapter(List<Product> products) {
		this.mProducts = products;
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (cartDbHandler == null)
			cartDbHandler = new CartDbHandler(parent.getContext());

		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_product_view,parent,false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Product product = mProducts.get(position);

		holder.name.setText(String.format(Locale.ENGLISH,
						"%s / %s",product.getName_english(),product.getName_hinglish()));

		Glide.with(holder.image.getContext())
				.load(product.getImageUrl())
				.diskCacheStrategy(DiskCacheStrategy.SOURCE)
				.into(holder.image);
		if (extraControls){
			PurchasedItem purchasedItem = cartDbHandler.getPurchasedItem(product.getId());
			int price = calculatePrice(product.getPrice(),product.getMinimumVolume(),purchasedItem.getVolume());
			holder.price.setText(String.format(Locale.ENGLISH,
					"Rs. %d / %d %s",price,purchasedItem.getVolume(),product.getVolume().getExtenction()));

			holder.setCartItem(purchasedItem);
			holder.setProduct(product);
		}else {
			holder.price.setText(String.format(Locale.ENGLISH,
					"Rs. %s / %d %s",product.getPrice(),product.getMinimumVolume(),product.getVolume().getExtenction()));

			if (cartDbHandler.isProductAddedToCart(product.getId()))
				holder.checkBox.setChecked(true);
		}
	}
	private int calculatePrice(int priceForMinVolume , int minVolume, int requiredVolume){
		double result = Double.valueOf(requiredVolume)*(Double.valueOf(priceForMinVolume)/Double.valueOf(minVolume));
		return (int) result;
	}

	@Override
	public int getItemCount() {
		return mProducts.size();
	}

	public int getCartItemCount(){
		return cartDbHandler.getProductIdsFromCart().size();
	}
	public void addProduct(Product product){
		mProducts.add(product);
		notifyDataSetChanged();
	}

	public void addNewProducts(List<Product> list){
		mProducts.clear();
		mProducts.addAll(list);
		notifyDataSetChanged();
	}

}