package com.woniu.sncp.cbss.core.model.constant;

public class NameFactory {
	public enum request_head {
		accessVerify("accessVerify"),accessId("accessId"),accessType("accessType"),accessPasswd("accessPasswd");

		private String value;

		private request_head() {

		}

		private request_head(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum request_otherinfo {
		encryptcharset, traceState;

		private String value;

		private request_otherinfo() {

		}

		private request_otherinfo(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum zookeeper_constant {
		accessSecurityInfoPath("/access/security/info"), accessSecurityResourcesPath("/access/security/resource"),
		accessSecurityInfoPath2("/cbss/api/access/security/info"), accessSecurityResourcesPath2("/cbss/api/access/security/resource"), accessSecurityInfoPathAdd("/access/security/info/add"), accessSecurityResourcesPathAdd(
				"/access/security/resource/add");

		private String value;

		private zookeeper_constant() {

		}

		private zookeeper_constant(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum default_constant {
		INFPARAM_HTTPPARAM_ALLMETHOD("ALLMETHOD"), ISDEBUG("DEBUG");

		private String value;

		private default_constant() {

		}

		private default_constant(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum default_charset {
		utf8("utf-8");

		private String value;

		private default_charset() {

		}

		private default_charset(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

}
