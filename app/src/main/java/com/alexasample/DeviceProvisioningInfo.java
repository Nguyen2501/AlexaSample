package com.alexasample;

import java.util.ArrayList;
import java.util.List;

public class DeviceProvisioningInfo {
    private final String productId;
    private final String dsn;
    private final String sessionId;
    private final String codeChallenge;
    private final String codeChallengeMethod;

    public DeviceProvisioningInfo(String productId, String dsn, String sessionId, String codeChallenge, String codeChallengeMethod) {
        List<String> missingParams = new ArrayList<String>();

        if (productId.isEmpty()) {
            missingParams.add(AuthConstants.PRODUCT_ID);
        }

        if (dsn.isEmpty()) {
            missingParams.add(AuthConstants.DSN);
        }

        if (codeChallenge.isEmpty()) {
            missingParams.add(AuthConstants.CODE_CHALLENGE);
        }

        if (codeChallengeMethod.isEmpty()) {
            missingParams.add(AuthConstants.CODE_CHALLENGE_METHOD);
        }

        if (missingParams.size() != 0) {
            throw new MissingParametersException(missingParams);
        }

        this.productId = productId;
        this.dsn = dsn;
        this.sessionId = sessionId;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getProductId() {
        return productId;
    }

    public String getDsn() {
        return dsn;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public static class MissingParametersException extends IllegalArgumentException {
        public static final long serialVersionUID = 1L;
        private final List<String> missingParameters;

        public MissingParametersException(List<String> missingParameters) {
            super();
            this.missingParameters = missingParameters;
        }

        @Override
        public String getMessage() {
            return "The following parameters were missing or empty strings: "
                    + missingParameters.toString();
        }

        public List<String> getMissingParameters() {
            return missingParameters;
        }
    }
}
