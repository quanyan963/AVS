/**
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * You may not use this file except in compliance with the License. A copy of the License is located the "LICENSE.txt"
 * file accompanying this source. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */
package com.txtled.avs.avs.amazonlogin;

import android.content.Context;
import android.util.Log;

import com.txtled.avs.R;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ProvisioningClient {
    private String endpoint;
    private SSLSocketFactory pinnedSSLSocketFactory;


    public ProvisioningClient(Context context) throws Exception {
        this.pinnedSSLSocketFactory = getPinnedSSLSocketFactory(context);
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public DeviceProvisioningInfo getDeviceProvisioningInfo() throws JSONException, IOException {
        URL companionInfoEndpoint = new URL(endpoint + "/cgi-bin/getinfo.cgi");
        HttpURLConnection connection = (HttpURLConnection) companionInfoEndpoint.openConnection();

        JSONObject response = doRequest(connection);
        if (response == null){
            return new DeviceProvisioningInfo("","","","","");
        }else {
            List<String> missingParameters = new ArrayList<String>();
            if (!response.has(AuthConstants.PRODUCT_ID)) {
                missingParameters.add(AuthConstants.PRODUCT_ID);
            }

            if (!response.has(AuthConstants.DSN)) {
                missingParameters.add(AuthConstants.DSN);
            }

            if (!response.has(AuthConstants.SESSION_ID)) {
                missingParameters.add(AuthConstants.SESSION_ID);
            }

            if (!response.has(AuthConstants.CODE_CHALLENGE)) {
                missingParameters.add(AuthConstants.CODE_CHALLENGE);
            }

            if (!response.has(AuthConstants.CODE_CHALLENGE_METHOD)) {
                missingParameters.add(AuthConstants.CODE_CHALLENGE_METHOD);
            }

            if (missingParameters.size() != 0) {
                throw new DeviceProvisioningInfo.MissingParametersException(missingParameters);
            }

            String productId = response.getString(AuthConstants.PRODUCT_ID);
            String dsn = response.getString(AuthConstants.DSN);
            String sessionId = response.getString(AuthConstants.SESSION_ID);
            String codeChallenge = response.getString(AuthConstants.CODE_CHALLENGE);
            String codeChallengeMethod = response.getString(AuthConstants.CODE_CHALLENGE_METHOD);

            DeviceProvisioningInfo ret = new DeviceProvisioningInfo(productId, dsn, sessionId, codeChallenge, codeChallengeMethod);
            return ret;
        }

    }

    public void postCompanionProvisioningInfo(CompanionProvisioningInfo companionProvisioningInfo) throws IOException, JSONException {
        String jsonString = companionProvisioningInfo.toJson().toString();
        Log.e("TAG", jsonString);
        try {
            URL companionInfoEndpoint = new URL(endpoint + "/cgi-bin/settoken.cgi");

            HttpURLConnection connection2 = (HttpURLConnection) companionInfoEndpoint.openConnection();
            doRequest(connection2, jsonString);
        } catch (Exception e) {
            return;
        }
    }


    JSONObject doRequest(HttpURLConnection connection) throws IOException, JSONException {
        return doRequest(connection, null);
    }

    JSONObject doRequest(HttpURLConnection connection, String data) throws IOException, JSONException {
        int responseCode = -1;
        InputStream response = null;
        DataOutputStream outputStream = null;

        try {
//            if (connection instanceof HttpsURLConnection) {
//                ((HttpsURLConnection) connection).setSSLSocketFactory(pinnedSSLSocketFactory);
//
//            }

            connection.setRequestProperty("Content-Type", "application/json");

            if (data != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                outputStream = new DataOutputStream(connection.getOutputStream());

                outputStream.write(data.getBytes());
                outputStream.flush();
                outputStream.close();
            } else {
                connection.setRequestMethod("GET");
            }

            responseCode = connection.getResponseCode();
            response = connection.getInputStream();


            if (responseCode != 204) {
                String responseString = IOUtils.toString(response);
                Log.e("RES", responseString);
                JSONObject jsonObject = new JSONObject(responseString);

                return jsonObject;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (responseCode < 200 || responseCode >= 300) {
                response = connection.getErrorStream();
                if (response != null) {
                    String responseString = IOUtils.toString(response);
                    return null;
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(response);
        }
    }

    private SSLSocketFactory getPinnedSSLSocketFactory(Context context) throws Exception {
        InputStream caCertInputStream = null;
        try {
            caCertInputStream = context.getResources().openRawResource(R.raw.ca);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate caCert = cf.generateCertificate(caCertInputStream);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("myca", caCert);

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();

//            sslClientKeyStoreIp = context.getResources().openRawResource(R.raw.client);
//            caCertInputStream = context.getResources().openRawResource(R.raw.ca);
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            Certificate caCert = cf.generateCertificate(caCertInputStream);
//            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            trustStore.load(null, null);
//            trustStore.setCertificateEntry("myca", caCert);
//            //设定证书
//            TrustManagerFactory trustManagerFactory =
//                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(trustStore);
//            // 设定可信密钥库
//            KeyStore keyStore = KeyStore.getInstance("PKCS12");
//            keyStore.load(sslClientKeyStoreIp,"123456".toCharArray());
//            //设定私钥
//            KeyManagerFactory keyManagerFactory =
//                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            keyManagerFactory.init(keyStore, "123456".toCharArray());
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
//            Log.e("TTTTTT","CREAT SSL IS RIGHT");
//            return sslContext.getSocketFactory();
        } finally {
            IOUtils.closeQuietly(caCertInputStream);
        }
    }

}
