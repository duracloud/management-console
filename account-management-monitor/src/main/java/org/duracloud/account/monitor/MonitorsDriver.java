/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.monitor;

import static org.duracloud.account.monitor.MonitorsDriver.Monitor.DUPLICATION;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.util.GlobalPropertiesConfigService;
import org.duracloud.account.email.EmailUtil;
import org.duracloud.account.email.EmailUtilImpl;
import org.duracloud.account.monitor.duplication.DuplicationMonitorDriver;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the command-line driver for executing monitors for
 * instance health, storage reporting, and duplication checks that
 * are run across DuraCloud accounts managed by the Management Console that is
 * defined by the configuration credentials.
 *
 * @author Andrew Woods
 * Date: 7/18/11
 */
public class MonitorsDriver {

    private Logger log = LoggerFactory.getLogger(MonitorsDriver.class);

    private static final String PREFIX = "monitor.";

    private static final String PREFIX_AWS = PREFIX + "aws.";
    private static final String AWS_USERNAME = PREFIX_AWS + "username";

    private static final String AWS_PASSWORD = PREFIX_AWS + "password";
    private static final String PREFIX_EMAIL = PREFIX + "email.";

    private static final String FROM_ADDRESS = PREFIX_EMAIL + "from";
    private static final String TO_ADDRESS = PREFIX_EMAIL + "to.";

    private Properties props;
    private EmailUtil emailUtil;
    private DuracloudRepoMgr repoMgr;
    private GlobalPropertiesConfigService configService;

    /**
     * This enum defines the types of monitors available through this driver.
     */
    public enum Monitor {
        DUPLICATION;

        public Runnable getMonitorDriver(Properties props, GlobalPropertiesConfigService configService) {
            if (this.equals(DUPLICATION)) {
                return new DuplicationMonitorDriver(props);

            } else {
                throw new DuraCloudRuntimeException("Unknown type: " + this);
            }
        }

        public String toString() {
            return name().toLowerCase();
        }
    }

    public MonitorsDriver(Properties props) {
        this.props = props;
        this.emailUtil = buildEmailUtil(props);
    }

    public DuracloudRepoMgr getRepoMgr() {
        return repoMgr;
    }

    public GlobalPropertiesConfigService getGlobalConfigService() {
        return configService;
    }

    /**
     * This method invokes the .run() method on the provided arg target.
     *
     * @param target to monitor
     */
    public void monitor(Monitor target) {
        log.info("starting monitor: {}", target);

        try {
            target.getMonitorDriver(props, configService).run();

        } catch (Exception e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream error = new PrintStream(out);

            error.println("Error in MonitorsDriver: " + e.getMessage());
            e.printStackTrace(error);

            error.flush();
            IOUtils.closeQuietly(out);

            String msg = out.toString();
            log.error(msg);
            sendEmail("Management Console Monitors Error", msg);
        }
    }

    protected void sendEmail(String subject, String body) {
        log.info("Sending email.\nsubject: {} \nbody \n{}", subject, body);
        emailUtil.sendEmail(subject, body);
    }

    private EmailUtil buildEmailUtil(Properties props) {
        String username = getProperty(props, AWS_USERNAME);
        String password = getProperty(props, AWS_PASSWORD);
        String fromAddress = getProperty(props, FROM_ADDRESS);
        List<String> recipients = getEmailRecipients(props, TO_ADDRESS);

        return new EmailUtilImpl(username, password, fromAddress, recipients);
    }

    private List<String> getEmailRecipients(Properties props, String prefix) {
        List<String> recipients = new ArrayList<String>();
        String recipient = getProperty(props, prefix + 0);
        while (null != recipient) {
            recipients.add(recipient);
            try {
                recipient = getProperty(props, prefix + recipients.size());

            } catch (Exception e) {
                recipient = null;
            }
        }
        return recipients;
    }

    protected String getProperty(Properties props, String key) {
        String property = props.getProperty(key);
        if (null == property) {
            throw new DuraCloudRuntimeException("Property not found: " + key);
        }
        return property;
    }

    private static Properties parseProperties(InputStream stream)
        throws IOException {
        Properties props = new Properties();
        props.load(stream);
        return props;
    }

    /**
     * Main
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(usage("Invalid number of args: " + args.length));
            System.exit(1);
        }

        String targetName = args[0].toLowerCase();
        Monitor target = null;
        try {
            target = Monitor.valueOf(targetName.toUpperCase());

        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("Target must be '");
            msg.append(DUPLICATION);
            msg.append("'");
            System.err.println(usage(msg.toString()));
            System.exit(1);
        }

        File configFile = new File(args[1]);
        if (!configFile.exists()) {
            String msg = "File does not exist: " + configFile.getAbsolutePath();
            System.err.println(usage(msg));
            System.exit(1);
        }

        InputStream inputStream = null;
        Properties props = null;
        try {
            inputStream = new FileInputStream(configFile);
            props = parseProperties(inputStream);

        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
            System.exit(1);

        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        MonitorsDriver driver = new MonitorsDriver(props);
        driver.monitor(target);
    }

    private static String usage(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\n");
        sb.append("Error: " + msg);
        sb.append("\n\n");
        sb.append("Usage: ");
        sb.append("MonitorsDriver ");
        sb.append("<instance|store_reporter|duplication> ");
        sb.append("<properties-file>");
        sb.append("\n\t");
        sb.append("Where '");
        sb.append(DUPLICATION);
        sb.append("' must be provided to indicate the monitoring target.");
        sb.append("\n\t");
        sb.append("And where 'properties-file' contains the necessary ");
        sb.append("initialization config.");
        sb.append("\n\t");
        sb.append("See the example in this jar's 'resources' directory.");
        sb.append("\n");
        sb.append("-----------------------------------------\n");

        return sb.toString();
    }

}
