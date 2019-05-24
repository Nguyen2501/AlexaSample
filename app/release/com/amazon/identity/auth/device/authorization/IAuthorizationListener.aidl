package com.amazon.identity.auth.device.authorization;

import com.amazon.identity.auth.device.IAuthError;

interface IAuthorizationListener
{
	void onSuccess(in Bundle response);
	
	void onError(in IAuthError e);
	
	void onCancel(in Bundle cause);
}