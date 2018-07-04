package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

public class Payment {
	
	private Transaction transaction;
	private Amount amount;
	private MerchantData merchantData;
	
	public Transaction getTransaction() {
		return transaction;
	}
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	public Amount getAmount() {
		return amount;
	}
	public void setAmount(Amount amount) {
		this.amount = amount;
	}
	public MerchantData getMerchantData() {
		return merchantData;
	}
	public void setMerchantData(MerchantData merchantData) {
		this.merchantData = merchantData;
	}
	

	public static class Transaction {
		
		private String transactionID;
		private String pwgcTrackingID;
		private String timestampUTC;
		private String pwgcHash;
		public String getTransactionID() {
			return transactionID;
		}
		public void setTransactionID(String transactionID) {
			this.transactionID = transactionID;
		}
		public String getPwgcTrackingID() {
			return pwgcTrackingID;
		}
		public void setPwgcTrackingID(String pwgcTrackingID) {
			this.pwgcTrackingID = pwgcTrackingID;
		}
		public String getTimestampUTC() {
			return timestampUTC;
		}
		public void setTimestampUTC(String timestampUTC) {
			this.timestampUTC = timestampUTC;
		}
		public String getPwgcHash() {
			return pwgcHash;
		}
		public void setPwgcHash(String pwgcHash) {
			this.pwgcHash = pwgcHash;
		}
	}
	public static class Amount {
		
		private Double amountValue;
		private String currencyCode;
		
		public Double getAmountValue() {
			return amountValue;
		}
		public void setAmountValue(Double amountValue) {
			this.amountValue = amountValue;
		}
		public String getCurrencyCode() {
			return currencyCode;
		}
		public void setCurrencyCode(String currencyCode) {
			this.currencyCode = currencyCode;
		}
	}
	public static class MerchantData {
		
		private String merchantToken;
		private String merchantHash;
		private String publicKey;
		private String merchantTrackingID;
		private String productID;
		private String itemDescription;
		private String ratingModel;
		private String productRating;
		
		public String getMerchantToken() {
			return merchantToken;
		}
		public void setMerchantToken(String merchantToken) {
			this.merchantToken = merchantToken;
		}
		public String getMerchantHash() {
			return merchantHash;
		}
		public void setMerchantHash(String merchantHash) {
			this.merchantHash = merchantHash;
		}
		public String getPublicKey() {
			return publicKey;
		}
		public void setPublicKey(String publicKey) {
			this.publicKey = publicKey;
		}
		public String getMerchantTrackingID() {
			return merchantTrackingID;
		}
		public void setMerchantTrackingID(String merchantTrackingID) {
			this.merchantTrackingID = merchantTrackingID;
		}
		public String getProductID() {
			return productID;
		}
		public void setProductID(String productID) {
			this.productID = productID;
		}
		public String getItemDescription() {
			return itemDescription;
		}
		public void setItemDescription(String itemDescription) {
			this.itemDescription = itemDescription;
		}
		public String getRatingModel() {
			return ratingModel;
		}
		public void setRatingModel(String ratingModel) {
			this.ratingModel = ratingModel;
		}
		public String getProductRating() {
			return productRating;
		}
		public void setProductRating(String productRating) {
			this.productRating = productRating;
		}
	}
}