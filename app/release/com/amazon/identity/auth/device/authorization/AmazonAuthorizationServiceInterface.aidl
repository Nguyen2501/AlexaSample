package com.amazon.identity.auth.device.authorization;

/** This is the service that talks to Authorization Manager SDK */
interface AmazonAuthorizationServiceInterface {
    
    /** Triggers the OAuth workflow to authorize an app*/
    Bundle authorize(in Bundle options, String packageName, in String[] scopes);

    /** Get an authorization token scoped for the specified scopes for the given package */
    Bundle getToken(in Bundle options, String packageName, in String[] scopes);

    /** Clear all tokens for the given package */
    Bundle clearAuthorizationState(in Bundle options, String packageName);
    
    /** Open an interactive workflow */
    Bundle openWorkflow(in Bundle options, String packageName, String workflowUrl, String workflowToken);
    
    /** Returns MetaData of the service that is serving the request*/
    Bundle getMetaData();
}