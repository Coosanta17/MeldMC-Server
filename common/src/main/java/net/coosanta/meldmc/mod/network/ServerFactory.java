package net.coosanta.meldmc.mod.network;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.coosanta.meldmc.mod.modsinfo.MeldData;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;

import static net.coosanta.meldmc.mod.MeldMC.LOGGER;

public class ServerFactory {
    public static MeldServer createServer(ServerConfig config, MeldData meldData) throws Exception {
        if (config.useHttps()) {
            return new MeldHttpsServerImpl(config, meldData);
        } else {
            return new MeldHttpServerImpl(config, meldData);
        }
    }

    private static class MeldHttpServerImpl extends AbstractMeldServer {
        public MeldHttpServerImpl(ServerConfig config, MeldData meldData) throws IOException {
            super(HttpServer.create(new InetSocketAddress(config.port()), 0), config, meldData);
            setupEndpoints();
        }

        @Override
        protected String serverType() {
            return "HTTP";
        }
    }

    private static class MeldHttpsServerImpl extends AbstractMeldServer {
        public MeldHttpsServerImpl(ServerConfig config, MeldData meldData) throws Exception {
            super(HttpsServer.create(new InetSocketAddress(config.port()), 0), config, meldData);
            setupSSL();
            setupEndpoints();
        }

        private void setupSSL() throws Exception {
            final SSLContext sslContext;

            if (config.autoSsl()) {
                sslContext = createAutoSSLContext();
            } else {
                sslContext = createSSLContext();
            }

            HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    SSLParameters sslParams = sslContext.getDefaultSSLParameters();

                    // Security hardening
                    sslParams.setProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
                    sslParams.setCipherSuites(getSecureCipherSuites());
                    sslParams.setWantClientAuth(false);
                    sslParams.setNeedClientAuth(false);

                    params.setSSLParameters(sslParams);
                }
            };

            ((HttpsServer) server).setHttpsConfigurator(httpsConfigurator);
        }

        private SSLContext createSSLContext() throws Exception {
            KeyStore keyStore = loadKeyStore();
            KeyStore trustStore = loadTrustStore();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            char[] keyPassword = config.keyStorePassword().isEmpty() ? null : config.keyStorePassword().toCharArray();
            kmf.init(keyStore, keyPassword);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            return sslContext;
        }

        private SSLContext createAutoSSLContext() throws Exception {
            LOGGER.debug("Creating Self-Signed certificate.");

            KeyStore keyStore = SelfSignedCertificateGenerator.generateSelfSignedKeyStore(config.queryAddress());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, new char[0]);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // Use default trust store - this is intentional for auto-SSL.

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            return sslContext;
        }

        private KeyStore loadKeyStore() throws Exception {
            KeyStore keyStore = KeyStore.getInstance(config.keyStoreType());
            char[] password = config.keyStorePassword().isEmpty() ? null : config.keyStorePassword().toCharArray();
            try (FileInputStream fis = new FileInputStream(config.keyStorePath())) {
                keyStore.load(fis, password);
            }
            return keyStore;
        }

        private KeyStore loadTrustStore() throws Exception {
            KeyStore trustStore = KeyStore.getInstance(config.trustStoreType());
            char[] password = config.trustStorePassword().isEmpty() ? null : config.trustStorePassword().toCharArray();
            try (FileInputStream fis = new FileInputStream(config.trustStorePath())) {
                trustStore.load(fis, password);
            }
            return trustStore;
        }

        private String[] getSecureCipherSuites() {
            return new String[]{
                    "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_AES_256_GCM_SHA384",
                    "TLS_AES_128_GCM_SHA256"
            };
        }

        @Override
        protected String serverType() {
            return "HTTPS";
        }
    }
}
