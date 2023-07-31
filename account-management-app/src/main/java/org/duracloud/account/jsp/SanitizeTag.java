/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.jsp;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Uses JSoup.clean to sanitize html markup.
 *
 * @author Daniel Bernstein
 */
public class SanitizeTag extends SimpleTagSupport {

    private String text;

    public void setText(String text) {
        this.text = text;
    }

    public void doTag() throws IOException {
        if (text != null) {
            JspWriter out = getJspContext().getOut();
            out.println(Jsoup.clean(text, Safelist.basic()));
        }

    }

}
