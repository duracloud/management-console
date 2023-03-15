/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util;

import java.io.File;
import java.io.IOException;

import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class is the starting point for the account management database
 * utility. This tool provides a way to retrieve and store data in the
 * database which sits behind the Account Management App without the
 * need of a UI. There are four modes in which this tool can be run:
 *
 * GET - This mode pulls down the information stored in the database. A new
 * file is created in the work directory for each domain (table) in the
 * database, and all items (rows) are stored in XML markup.
 *
 * PUT - This mode pushes the information contained in the files in the work
 * directory into the database, overwriting any entries which already
 * exist. The best way to use this feature is to perform a GET first,
 * then edit the files which were produced to add/update any entries.
 *
 * CLEAR - This mode clears out all of the items (rows) in all of the database
 * domains (tables). As a safety precaution, a GET is performed prior
 * to the CLEAR, so that you have a backup of the data which was in
 * the database prior to the clear.
 *
 * Usage Notes:
 *
 * - If you want to remove items (rows) from the database, you can perform a
 * CLEAR, then edit the files which were created in the GET, followed by a
 * PUT. This will ensure that only the items (rows) included in the files
 * are in the database.
 *
 * - A PUT will only perform updates based on what is included in the files in
 * the work directory. If you only want to work with a single domain (table),
 * you can perform a GET, then delete all of the files produced other than
 * the one you'd like to edit.
 *
 * - Performing a GET will overwrite any files produced on a previous run if
 * it is pointed to the same work directory. This is also the case for the
 * GET which is performed automatically prior to a CLEAR. Be careful not to
 * overwrite your edited files when performing either of these functions.
 *
 * - When performing a PUT command, the items (rows) to be added are compared
 * to the existing database. If an item (row) already exists and all of the
 * values are equal, no update occurs (and the database counter is not
 * updated, as this only occurs when an item is changed.)
 *
 * @author Bill Branan
 * Date: Dec 21, 2010
 */
public class DbUtilDriver {

    private DbUtilDriver() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            usage("Two arguments are required, you supplied: " + args.length);
            System.exit(1);
        }

        DbUtil.COMMAND command = null;
        String commandArg = args[0];
        if (commandArg.equalsIgnoreCase(DbUtil.COMMAND.PUT.name())) {
            command = DbUtil.COMMAND.PUT;
        } else {
            usage("The first argument must PUT. " +
                  "The previously supported command GET and CLEAR " +
                  "have been removed since the move to MC 2.0.0 "
                  + "You supplied: " + commandArg);
            System.exit(1);
        }

        File workDir = new File(args[1]);
        if (!workDir.exists()) {
            usage("The work directory must exist: " + workDir.getPath());
            System.exit(1);
        } else if (!workDir.isDirectory()) {
            usage("The work directory must be a directory: " + workDir.getPath());
            System.exit(1);
        }

        ApplicationContext context =
            new ClassPathXmlApplicationContext("jpa-config.xml");
        DuracloudRepoMgr repoMgr = context.getBean("repoMgr", DuracloudRepoMgr.class);

        DbUtil dbUtil = new DbUtil(repoMgr, workDir);
        dbUtil.runCommand(command);
    }

    private static void usage(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error: " + msg);
        sb.append("\n\n");
        sb.append("Usage: ");
        sb.append("\n\t");
        sb.append("DbUtilDriver [get] [work-dir]");
        sb.append("\n\n\t\t");
        sb.append("GET - retrieves and stores all db data in work-dir");
        sb.append("\n\n\t");
        sb.append("where [work-dir] is a directory from which data will be");
        sb.append("\n\n\t\t");
        sb.append("read");

        System.out.println(sb);
    }
}
