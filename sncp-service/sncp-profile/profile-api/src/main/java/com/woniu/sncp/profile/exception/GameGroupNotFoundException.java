/**
 * 
 */
package com.woniu.sncp.profile.exception;

import com.woniu.sncp.exception.web.WebBaseException;

/**
 * @author fuzl
 *
 */
public class GameGroupNotFoundException extends WebBaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String CODE = "710000";
	
	public GameGroupNotFoundException(Object[] args) {
		super(CODE,args);
	}
}
