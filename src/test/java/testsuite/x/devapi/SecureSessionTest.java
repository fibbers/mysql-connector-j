/*
  Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.

  The MySQL Connector/J is licensed under the terms of the GPLv2
  <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most MySQL Connectors.
  There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
  this software, see the FOSS License Exception
  <http://www.mysql.com/about/legal/licensing/foss-exception.html>.

  This program is free software; you can redistribute it and/or modify it under the terms
  of the GNU General Public License as published by the Free Software Foundation; version 2
  of the License.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this
  program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
  Floor, Boston, MA 02110-1301  USA

 */

package testsuite.x.devapi;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.mysql.cj.api.xdevapi.Session;
import com.mysql.cj.api.xdevapi.SqlResult;
import com.mysql.cj.core.conf.PropertyDefinitions;
import com.mysql.cj.core.exceptions.CJCommunicationsException;

public class SecureSessionTest extends DevApiBaseTestCase {
    String trustStoreUrl = "file:src/test/config/ssl-test-certs/ca-truststore";
    String trustStorePath = "src/test/config/ssl-test-certs/ca-truststore";
    String trustStorePassword = "password";

    String clientKeyStoreUrl = "file:src/test/config/ssl-test-certs/client-keystore";
    String clientKeyStorePath = "src/test/config/ssl-test-certs/client-keystore";
    String clientKeyStorePassword = "password";

    @Before
    public void setupSecureSessionTest() {
        if (this.isSetForXTests) {
            System.clearProperty("javax.net.ssl.trustStore");
            System.clearProperty("javax.net.ssl.trustStorePassword");
        }
    }

    /**
     * Tests non-secure {@link Session}s created via URL and properties map.
     */
    @Test
    public void testNonSecureSession() {
        if (!this.isSetForXTests) {
            return;
        }

        Session testSession = this.fact.getSession(this.baseUrl);
        assertNonSecureSession(testSession);
        testSession.close();

        testSession = this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslEnable, "false"));
        assertNonSecureSession(testSession);
        testSession.close();

        testSession = this.fact.getSession(this.testProperties);
        assertNonSecureSession(testSession);
        testSession.close();

        Properties props = new Properties(this.testProperties);
        props.setProperty(PropertyDefinitions.PNAME_sslEnable, "false");
        testSession = this.fact.getSession(props);
        assertNonSecureSession(testSession);
        testSession.close();
    }

    /**
     * Tests secure, non-verifying server certificate {@link Session}s created via URL and properties map.
     */
    @Test
    public void testSecureSessionNoVerifyServerCertificate() {
        if (!this.isSetForXTests) {
            return;
        }

        Session testSession = this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslEnable, "true"));
        assertSecureSession(testSession);
        testSession.close();

        testSession = this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslEnable, "true")
                + makeParam(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "false"));
        assertSecureSession(testSession);
        testSession.close();

        Properties props = new Properties(this.testProperties);
        props.setProperty(PropertyDefinitions.PNAME_sslEnable, "true");
        testSession = this.fact.getSession(props);
        assertSecureSession(testSession);
        testSession.close();

        props.setProperty(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "false");
        testSession = this.fact.getSession(props);
        assertSecureSession(testSession);
        testSession.close();
    }

    /**
     * Tests secure, verifying server certificate {@link Session}s created via URL and properties map.
     */
    @Test
    public void testSecureSessionVerifyServerCertificate() {
        if (!this.isSetForXTests) {
            return;
        }

        Session testSession = this.fact.getSession(
                this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslEnable, "true") + makeParam(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true")
                        + makeParam(PropertyDefinitions.PNAME_sslTrustStoreUrl, this.trustStoreUrl)
                        + makeParam(PropertyDefinitions.PNAME_sslTrustStorePassword, this.trustStorePassword));
        assertSecureSession(testSession);
        SqlResult rs = testSession.sql("SHOW SESSION STATUS LIKE 'mysqlx_ssl_version'").execute();
        String actual = rs.fetchOne().getString(1);

        System.out.println(actual);

        testSession.close();

        testSession = this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true")
                + makeParam(PropertyDefinitions.PNAME_sslTrustStoreUrl, this.trustStoreUrl)
                + makeParam(PropertyDefinitions.PNAME_sslTrustStorePassword, this.trustStorePassword));
        assertSecureSession(testSession);
        testSession.close();

        testSession = this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslTrustStoreUrl, this.trustStoreUrl)
                + makeParam(PropertyDefinitions.PNAME_sslTrustStorePassword, this.trustStorePassword));
        assertSecureSession(testSession);
        testSession.close();

        Properties props = new Properties(this.testProperties);
        props.setProperty(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true");
        props.setProperty(PropertyDefinitions.PNAME_sslTrustStoreUrl, this.trustStoreUrl);
        props.setProperty(PropertyDefinitions.PNAME_sslTrustStorePassword, this.trustStorePassword);
        testSession = this.fact.getSession(props);
        assertSecureSession(testSession);
        testSession.close();

        props.setProperty(PropertyDefinitions.PNAME_sslEnable, "true");
        testSession = this.fact.getSession(props);
        assertSecureSession(testSession);
        testSession.close();
    }

    /**
     * Tests secure, verifying server certificate {@link Session}s created via URL and properties map.
     */
    @Test
    public void testSecureSessionVerifyServerCertificateUsingSystemProps() {
        if (!this.isSetForXTests) {
            return;
        }

        System.setProperty("javax.net.ssl.trustStore", this.trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", this.trustStorePassword);

        Session testSession = this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslEnable, "true")
                + makeParam(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true"));
        assertSecureSession(testSession);
        testSession.close();

        testSession = this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true"));
        assertSecureSession(testSession);
        testSession.close();

        Properties props = new Properties(this.testProperties);
        props.setProperty(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true");
        testSession = this.fact.getSession(props);
        assertSecureSession(testSession);
        testSession.close();

        props.setProperty(PropertyDefinitions.PNAME_sslEnable, "true");
        testSession = this.fact.getSession(props);
        assertSecureSession(testSession);
        testSession.close();
    }

    /**
     * Tests exception thrown on missing truststore for a secure {@link Session}.
     */
    @Test
    public void testSecureSessionMissingTrustStore1() {
        if (!this.isSetForXTests) {
            return;
        }

        assertThrows(CJCommunicationsException.class, () -> this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslEnable, "true")
                + makeParam(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true")));
    }

    /**
     * Tests exception thrown on missing truststore for a secure {@link Session}.
     */
    @Test
    public void testSecureSessionMissingTrustStore2() {
        if (!this.isSetForXTests) {
            return;
        }

        assertThrows(CJCommunicationsException.class,
                () -> this.fact.getSession(this.baseUrl + makeParam(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true")));
    }

    /**
     * Tests exception thrown on missing truststore for a secure {@link Session}.
     */
    @Test
    public void testSecureSessionMissingTrustStore3() {
        if (!this.isSetForXTests) {
            return;
        }

        final Properties props = new Properties(this.testProperties);
        props.setProperty(PropertyDefinitions.PNAME_sslEnable, "true");
        props.setProperty(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true");
        assertThrows(CJCommunicationsException.class, () -> this.fact.getSession(props));
    }

    /**
     * Tests exception thrown on missing truststore for a secure {@link Session}.
     */
    @Test
    public void testSecureSessionMissingTrustStore4() {
        if (!this.isSetForXTests) {
            return;
        }

        final Properties props = new Properties(this.testProperties);
        props.setProperty(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "true");
        assertThrows(CJCommunicationsException.class, () -> this.fact.getSession(props));
    }

    private void assertNonSecureSession(Session sess) {
        assertSessionStatusEquals(sess, "mysqlx_ssl_cipher", "");
    }

    private void assertSecureSession(Session sess) {
        assertSessionStatusNotEquals(sess, "mysqlx_ssl_cipher", "");
    }

    private String makeParam(String key, String value) {
        return "&" + key + "=" + value;
    }

    /**
     * Tests fix for Bug#25494338, ENABLEDSSLCIPHERSUITES PARAMETER NOT WORKING AS EXPECTED WITH X-PLUGIN.
     */
    @Test
    public void testBug25494338() {
        if (!this.isSetForXTests) {
            return;
        }

        Session testSession = null;

        try {
            Properties props = new Properties(this.testProperties);
            testSession = this.fact.getSession(props);

            testSession.sql("CREATE USER 'bug25494338user'@'%' IDENTIFIED WITH mysql_native_password BY 'pwd' REQUIRE CIPHER 'AES128-SHA'").execute();

            props.setProperty(PropertyDefinitions.PNAME_sslVerifyServerCertificate, "false");
            props.setProperty(PropertyDefinitions.PNAME_sslEnable, "true");
            props.setProperty(PropertyDefinitions.PNAME_sslTrustStoreUrl, this.trustStoreUrl);
            props.setProperty(PropertyDefinitions.PNAME_sslTrustStorePassword, this.trustStorePassword);
            props.setProperty(PropertyDefinitions.PNAME_clientCertificateKeyStoreUrl, this.clientKeyStoreUrl);
            props.setProperty(PropertyDefinitions.PNAME_clientCertificateKeyStorePassword, this.clientKeyStorePassword);

            // 1. Allow only TLS_DHE_RSA_WITH_AES_128_CBC_SHA cipher
            props.setProperty(PropertyDefinitions.PNAME_enabledSSLCipherSuites, "TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
            Session sess = this.fact.getSession(props);
            assertSessionStatusEquals(sess, "mysqlx_ssl_cipher", "DHE-RSA-AES128-SHA");
            sess.close();

            // 2. Allow only TLS_RSA_WITH_AES_128_CBC_SHA cipher
            props.setProperty(PropertyDefinitions.PNAME_enabledSSLCipherSuites, "TLS_RSA_WITH_AES_128_CBC_SHA");
            sess = this.fact.getSession(props);
            assertSessionStatusEquals(sess, "mysqlx_ssl_cipher", "AES128-SHA");
            assertSessionStatusEquals(sess, "ssl_cipher", "");
            sess.close();

            // 3. Check connection with required client certificate 
            props.setProperty(PropertyDefinitions.PNAME_user, "bug25494338user");
            props.setProperty(PropertyDefinitions.PNAME_password, "pwd");

            sess = this.fact.getSession(props);
            assertSessionStatusEquals(sess, "mysqlx_ssl_cipher", "AES128-SHA");
            assertSessionStatusEquals(sess, "ssl_cipher", "");
            sess.close();

        } catch (Throwable t) {
            throw t;
        } finally {
            if (testSession != null) {
                testSession.sql("DROP USER bug25494338user").execute();
            }
        }
    }

    @Test
    public void testBug23597281() {
        if (!this.isSetForXTests) {
            return;
        }

        Properties props = new Properties(this.testProperties);
        props.setProperty(PropertyDefinitions.PNAME_sslEnable, "true");
        props.setProperty(PropertyDefinitions.PNAME_sslTrustStoreUrl, this.trustStoreUrl);
        props.setProperty(PropertyDefinitions.PNAME_sslTrustStorePassword, this.trustStorePassword);

        Session nSession;
        for (int i = 0; i < 100; i++) {
            nSession = this.fact.getSession(props);
            nSession.close();
            nSession = null;
        }
    }
}