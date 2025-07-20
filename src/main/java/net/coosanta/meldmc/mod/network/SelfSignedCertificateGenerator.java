package net.coosanta.meldmc.mod.network;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class SelfSignedCertificateGenerator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyStore generateSelfSignedKeyStore(String hostname) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate cert = generateX509Certificate(keyPair, hostname);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("server", keyPair.getPrivate(), new char[0], new X509Certificate[]{cert});

        return keyStore;
    }

    private static X509Certificate generateX509Certificate(KeyPair keyPair, String hostname) throws Exception {
        X500Name subject = new X500Name("CN=" + hostname + ", O=MeldMC");

        LocalDateTime now = LocalDateTime.now();
        Date validFrom = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date validTo = Date.from(now.plusYears(1).atZone(ZoneId.systemDefault()).toInstant());

        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                subject, // issuer
                serialNumber,
                validFrom,
                validTo,
                subject, // subject (same as issuer for self-signed)
                subjectPublicKeyInfo
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
    }
}